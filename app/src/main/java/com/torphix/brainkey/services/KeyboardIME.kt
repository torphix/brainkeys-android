package com.torphix.brainkey.services

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.ExtractedTextRequest
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.torphix.brainkey.Llm
import com.torphix.brainkey.databinding.ItemKeyboardHeaderBinding
import com.torphix.brainkey.databinding.KeyboardImeBinding
import com.torphix.brainkey.ui.main.MainActivity
import com.frogobox.libkeyboard.common.core.BaseKeyboardIME
import com.torphix.brainkey.models.KeyboardFeature
import com.torphix.brainkey.models.KeyboardFeatureType
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import com.torphix.brainkey.services.llm.LlmUtil
import com.frogobox.recycler.core.FrogoRecyclerNotifyListener
import com.frogobox.recycler.core.IFrogoBindingAdapter
import com.frogobox.recycler.ext.injectorBinding
import com.frogobox.sdk.ext.gone
import com.frogobox.sdk.ext.invisible
import com.frogobox.sdk.ext.visible
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import com.google.android.material.snackbar.Snackbar


class KeyboardIME : BaseKeyboardIME<KeyboardImeBinding>() {
    private val job = Job()

    // Define a CoroutineScope tied to job
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val keyboardSettingsRepository = KeyboardSettingsRepository(this)

    override fun setupViewBinding(): KeyboardImeBinding {
        return KeyboardImeBinding.inflate(LayoutInflater.from(this), null, false)
    }

    override fun initialSetupKeyboard() {
        binding?.keyboardMain?.setKeyboard(keyboard!!)
        binding?.mockMeasureHeightKeyboardMain?.setKeyboard(keyboard!!)
    }

    override fun setupBinding() {
        initialSetupKeyboard()
        binding?.keyboardMain?.mOnKeyboardActionListener = this
        binding?.keyboardEmoji?.mOnKeyboardActionListener = this
    }

    override fun invalidateKeyboard() {
        setupFeatureKeyboard()
    }

    override fun initCurrentInputConnection() {
        binding?.apply {
            keyboardEmoji.setInputConnection(currentInputConnection)
            keyboardPrompt.setInputConnection(currentInputConnection)
        }
    }

    override fun hideMainKeyboard() {
        binding?.apply {
            keyboardMain.gone()
            keyboardHeader.gone()
            mockMeasureHeightKeyboard.invisible()
        }
    }

    override fun showMainKeyboard() {
        binding?.apply {
            keyboardMain.visible()
            mockMeasureHeightKeyboard.gone()
            keyboardHeader.visible()
            keyboardPrompt.gone()
            keyboardEmoji.binding?.emojiList?.scrollToPosition(0)
        }
    }

    override fun showOnlyKeyboard() {
        binding?.keyboardMain?.visible()
    }

    override fun hideOnlyKeyboard() {
        binding?.keyboardMain?.gone()
    }

    override fun EditText.showKeyboardExt() {
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showOnlyKeyboard()
            }
        }
        setOnClickListener {
            showOnlyKeyboard()
        }
    }

    override fun initBackToMainKeyboard() {
        binding?.apply {
            keyboardEmoji.binding?.toolbarBack?.setOnClickListener {
                keyboardEmoji.binding?.emojiList?.scrollToPosition(0)
                showMainKeyboard()
            }
            keyboardPrompt.binding?.toolbarBack?.setOnClickListener {
                keyboardPrompt.gone()
                showMainKeyboard()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKey(code: Int) {
        var inputConnection = currentInputConnection
        onKeyExt(code, inputConnection)
    }

    override fun initView() {
        setupFeatureKeyboard()
        initBackToMainKeyboard()
    }

    override fun invalidateAllKeys() {
        binding?.keyboardMain?.invalidateAllKeys()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun runEmojiBoard() {
        hideMainKeyboard()
        binding?.keyboardEmoji?.openEmojiPalette()
    }

    private fun showErrorSnackbar(message: String) {
        binding?.root?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun inferenceLLM(prompt:String, callback: (String) -> Unit, formatPrompt:Boolean=true){
        binding?.apply {
            keyboardHeader.gone()
            keyboardLoading.visible()
        }
        // Send to Llama
        val extFilesDir = getExternalFilesDir(null)
        val llm = Llm.instance()
        coroutineScope.launch {
            val tag = "Loading Model"
            try {
                var modelName =  keyboardSettingsRepository.getActiveModel()
                if (modelName == ""){
                    throw Exception("Select a model in the app first")
                }
                var inputText = prompt
                if (formatPrompt) {
                    inputText = LlmUtil.formatPrompt(prompt, modelName)
                }
                Log.i("Input text","Input Text: $inputText")
                llm.load("${extFilesDir}/${modelName}")
                llm.send(inputText).catch {
                    Log.e("Inference Model", "send() failed", it)
                }.collect {
                    Log.i("Inference Model", "Token $it")
                    callback(it)
                }
                llm.unload()
            } catch (exc: IllegalStateException) {
                Log.e(tag, "load() failed", exc)
                showErrorSnackbar("Loading model failed, try selecting / re-downloading")
            }
            binding?.apply {
                keyboardHeader.visible()
                keyboardLoading.gone()
            }
    }}

    override fun setupFeatureKeyboard() {
        val maxMenu = 4
        val gridSize = if (KeyboardUtil().menuKeyboard().size <= maxMenu) {
            KeyboardUtil().menuKeyboard().size
        } else if (KeyboardUtil().menuKeyboard().size.mod(maxMenu) == 0) {
            maxMenu
        } else {
            maxMenu + 1
        }
        binding?.apply {
                keyboardHeader.visible()
                mockKeyboardHeader.visible()
                keyboardHeader.injectorBinding<KeyboardFeature, ItemKeyboardHeaderBinding>()
                    .addData(KeyboardUtil().menuKeyboard())
                    .addCallback(object :
                        IFrogoBindingAdapter<KeyboardFeature, ItemKeyboardHeaderBinding> {
                        override fun setViewBinding(parent: ViewGroup): ItemKeyboardHeaderBinding {
                            return ItemKeyboardHeaderBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        }
                        override fun setupInitComponent(
                            binding: ItemKeyboardHeaderBinding,
                            data: KeyboardFeature,
                            position: Int,
                            notifyListener: FrogoRecyclerNotifyListener<KeyboardFeature>,
                        ) {
                            binding.ivIcon.setImageResource(data.icon)
                            binding.tvTitle.text = data.type.title

                            if (data.state) {
                                binding.root.visible()
                            } else {
                                binding.root.gone()
                            }

                        }
                        override fun onItemClicked(
                            binding: ItemKeyboardHeaderBinding,
                            data: KeyboardFeature,
                            position: Int,
                            notifyListener: FrogoRecyclerNotifyListener<KeyboardFeature>,
                        ) {

                            when (data.type) {
                                KeyboardFeatureType.AUTO_TEXT -> {
                                    // Extract the text in the current focus
                                    val extractedTextRequest = ExtractedTextRequest()
                                    val extractedText = currentInputConnection?.getExtractedText(extractedTextRequest, 0)
                                    // Return the current text, or null if it can't be retrieved
                                    var inputText = extractedText?.text?.toString() ?: return
                                    // Delete the text
                                    currentInputConnection?.deleteSurroundingText(inputText.length, 0)
                                    inferenceLLM(
                                        inputText,
                                        callback={text ->currentInputConnection.commitText(text, 1)},
                                        formatPrompt=true)
                                }
                                KeyboardFeatureType.CONTINUE_TEXT -> {
                                    // Extract the text in the current focus
                                    val extractedTextRequest = ExtractedTextRequest()
                                    val extractedText = currentInputConnection?.getExtractedText(
                                        extractedTextRequest,
                                        0
                                    )
                                    // Return the current text, or null if it can't be retrieved
                                    var inputText = extractedText?.text?.toString() ?: return
                                    inputText = "<|assistant|>${inputText}"
                                    currentInputConnection.commitText(" ", 1)
                                    inferenceLLM(
                                        inputText,
                                        callback = { text ->
                                            currentInputConnection.commitText(
                                                text,
                                                1
                                            )
                                        },
                                        formatPrompt = false
                                    )
                                }
                                KeyboardFeatureType.PROMPTS -> {
                                    hideMainKeyboard()
                                    keyboardPrompt.visible()
                                }
                                KeyboardFeatureType.SETTING -> {
                                    binding.root.context.startActivity(
                                        Intent(binding.root.context, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        })
                                }

                            }
                        }
                        override fun onItemLongClicked(
                            binding: ItemKeyboardHeaderBinding,
                            data: KeyboardFeature,
                            position: Int,
                            notifyListener: FrogoRecyclerNotifyListener<KeyboardFeature>
                        ) {
                        }

                    })
                    .createLayoutGrid(gridSize)
                    .build()
        }
    }

}



