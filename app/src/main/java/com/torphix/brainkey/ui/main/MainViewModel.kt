package com.torphix.brainkey.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torphix.brainkey.Llm
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel(private val llm: Llm = Llm.instance()): ViewModel() {
    companion object {
        @JvmStatic
        private val NanosPerSecond = 1_000_000_000.0
    }

    private val tag: String? = this::class.simpleName

    fun setLatestState(
        isKeyboardEnabled: Boolean,
        isUsingKeyboard:Boolean,
        totalMemory:Long,
        availableMemory:Long,
        activeModel: String,
        userPrompts: List<String>,
        keyboardSettingsRepository: KeyboardSettingsRepository
        ){
        updateKeyboardStatus(isKeyboardEnabled, isUsingKeyboard)
        updateMemoryStatus(totalMemory, availableMemory)
        updateActiveModel(activeModel, keyboardSettingsRepository)
        updateUserPrompts(userPrompts, keyboardSettingsRepository)

    }

    private val _activeModel = MutableLiveData<String>("")
    val activeModel: LiveData<String> = _activeModel

    fun updateActiveModel(activeModel: String, keyboardSettingsRepository: KeyboardSettingsRepository) {
        _activeModel.value = activeModel
        keyboardSettingsRepository.setActiveModel(activeModel)
    }

    private val _isKeyboardEnabled = MutableLiveData<Boolean>(false)
    val isKeyboardEnabled: LiveData<Boolean> = _isKeyboardEnabled

    private val _isUsingKeyboard = MutableLiveData<Boolean>(false)
    val isUsingKeyboard: LiveData<Boolean> = _isUsingKeyboard

    fun updateKeyboardStatus(isEnabled: Boolean, isUsing: Boolean) {
        _isKeyboardEnabled.value = isEnabled
        _isUsingKeyboard.value = isUsing
    }

    private val _totalMemory = MutableLiveData<Long>(0)
    val totalMemory: LiveData<Long> = _totalMemory

    private val _freeMemory = MutableLiveData<Long>(0)
    val freeMemory: LiveData<Long> = _freeMemory

    fun updateMemoryStatus(totalMemory: Long, freeMemory:Long){
        _freeMemory.value = freeMemory
        _totalMemory.value = totalMemory
    }

    private val _userPrompts = MutableLiveData<List<String>>(listOf())
    val userPrompts: LiveData<List<String>> = _userPrompts;
    fun updateUserPrompts(newPrompts: List<String>, keyboardSettingsRepository: KeyboardSettingsRepository) {
        _userPrompts.value = newPrompts
        keyboardSettingsRepository.saveUserPrompts(newPrompts)
    }
    // Messages & LLM state
    var messages by mutableStateOf(listOf("Initializing..."))
        private set

    var message by mutableStateOf("")
        private set

    override fun onCleared() {
        super.onCleared()

        viewModelScope.launch {
            try {
                llm.unload()
            } catch (exc: IllegalStateException) {
                messages += exc.message!!
            }
        }
    }

    fun send() {
        val text = message
        message = ""

        // Add to messages console.
        messages += text
        messages += ""

        viewModelScope.launch {
            llm.send(text)
                .catch {
                    Log.e(tag, "send() failed", it)
                    messages += it.message!!
                }
                .collect { messages = messages.dropLast(1) + (messages.last() + it) }
        }
    }


    val benchmarkResults = MutableLiveData<String>()

    fun bench(pp: Int, tg: Int, pl: Int, nr: Int = 1) {
        viewModelScope.launch {
            try {
                benchmarkResults.postValue("Loading...")
                val start = System.nanoTime()
                val warmupResult = llm.bench(pp, tg, pl, nr)
                val end = System.nanoTime()
                val warmup = (end - start).toDouble() / NanosPerSecond

                benchmarkResults.postValue("Warm up time: $warmup seconds, please wait...")
                benchmarkResults.postValue(llm.bench(pp, tg, pl, nr).toString())
                llm.unload()
            } catch (exc: IllegalStateException) {
                Log.e(tag, "bench() failed", exc)
                benchmarkResults.postValue("Benchmark failed, did you download the model?")
            }
        }
    }

    fun load(pathToModel: String) {
        viewModelScope.launch {
            try {
                llm.load(pathToModel)
                messages += "Loaded $pathToModel"
            } catch (exc: IllegalStateException) {
                Log.e(tag, "load() failed", exc)
                messages += exc.message!!
            }
        }
    }

    fun updateMessage(newMessage: String) {
        message = newMessage
    }

    fun clear() {
        messages = listOf()
    }

    fun addMessage(message: String) {
        messages += message
    }
}
