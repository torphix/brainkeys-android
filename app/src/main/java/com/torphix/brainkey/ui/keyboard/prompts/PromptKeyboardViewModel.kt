package com.torphix.brainkey.ui.keyboard.prompts

import android.content.Context
import com.torphix.brainkey.repository.KeyboardSettingsRepository

class PromptKeyboardViewModel(val context: Context) {

    private fun getRepository() : KeyboardSettingsRepository {
        return KeyboardSettingsRepository(context)
    }

    fun getUserPrompts(): List<String> {
        var userPrompts = getRepository().getUserPrompts()
        return userPrompts
    }

}