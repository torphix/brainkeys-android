package com.torphix.brainkey.ui.keyboard.prompts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.torphix.brainkey.databinding.ItemKeyboardPromptBinding
import com.torphix.brainkey.databinding.KeyboardPromptBinding
import com.frogobox.libkeyboard.common.core.BaseKeyboard
import com.frogobox.recycler.core.FrogoRecyclerNotifyListener
import com.frogobox.recycler.core.IFrogoBindingAdapter
import com.frogobox.recycler.ext.injectorBinding

class PromptKeyboard(
    context: Context,
    attrs: AttributeSet?,
) : BaseKeyboard<KeyboardPromptBinding>(context, attrs) {

    override fun setupViewBinding(): KeyboardPromptBinding {
        return KeyboardPromptBinding.inflate(LayoutInflater.from(context), this, true)
    }
    override fun onCreate() {
        initView()
        setupData()
    }

    fun setupData() {
        val viewModel = PromptKeyboardViewModel(context)
        val userPrompts = viewModel.getUserPrompts()
        setupRv(userPrompts)
    }
    private fun initView() {
        binding?.apply {
            tvToolbarTitle.text = "Prompts"
        }
    }


    private fun setupRv(prompts: List<String>) {
        binding?.apply {

            val adapterCallback = object :
                IFrogoBindingAdapter<String, ItemKeyboardPromptBinding> {
                override fun onItemClicked(
                    binding: ItemKeyboardPromptBinding,
                    data: String,
                    position: Int,
                    notifyListener: FrogoRecyclerNotifyListener<String>,
                ) {
                    val output = data
                    currentInputConnection?.commitText(output, 1)
                }

                override fun onItemLongClicked(
                    binding: ItemKeyboardPromptBinding,
                    data: String,
                    position: Int,
                    notifyListener: FrogoRecyclerNotifyListener<String>,
                ) {
                }

                override fun setViewBinding(parent: ViewGroup): ItemKeyboardPromptBinding {
                    return ItemKeyboardPromptBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false)
                }

                override fun setupInitComponent(
                    binding: ItemKeyboardPromptBinding,
                    data: String,
                    position: Int,
                    notifyListener: FrogoRecyclerNotifyListener<String>,
                ) {
                    binding.apply {
                        tvItemKeyboardMain.text = data
                    }
                }
            }

            rvKeyboardMain.injectorBinding<String, ItemKeyboardPromptBinding>()
                .addData(prompts)
                .createLayoutLinearVertical(false)
                .addCallback(adapterCallback)
                .build()
        }
    }
}