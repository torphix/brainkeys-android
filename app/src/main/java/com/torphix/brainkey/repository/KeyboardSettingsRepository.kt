package com.torphix.brainkey.repository

import android.content.Context
import android.content.SharedPreferences
import com.torphix.brainkey.Constant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class KeyboardSettingsRepository(private val context: Context){
    private val ActiveModelKey = "active_model"
    private val UserPrompts = "user_prompts"
    private val PREFERENCES_NAME = "keyboard_settings"
    private val ApiKeys = "api_keys"
    private val SystemPrompt = "system_prompt"
    private val MaxTokens = "max_tokens"

    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getActiveModel(): String {
        val sharedPreferences = getPreferences()
        return sharedPreferences.getString(ActiveModelKey, "") ?: ""
    }

    fun setActiveModel(model: String) {
        val sharedPreferences = getPreferences()
        with(sharedPreferences.edit()) {
            putString(ActiveModelKey, model)
            apply()
        }
    }

    fun saveUserPrompts(list: List<String>) {
        val jsonList = Gson().toJson(list)
        val sharedPreferences = getPreferences()
        with(sharedPreferences.edit()) {
            putString(UserPrompts, jsonList)
            apply()
        }
    }

    fun getUserPrompts(): List<String> {
        val sharedPreferences = getPreferences()
        val jsonList = sharedPreferences.getString(UserPrompts, "[]")
        if (jsonList == "[]") {
            saveUserPrompts( Constant.DEFAULT_USER_PROMPTS)
            return Constant.DEFAULT_USER_PROMPTS
        } else {
            return Gson().fromJson(jsonList, object : TypeToken<List<String>>() {}.type)
        }
    }

    fun setApiKeys(keys: Map<String, String>) {
        val jsonMap = Gson().toJson(keys)
        val sharedPreferences = getPreferences()
        with(sharedPreferences.edit()) {
            putString(ApiKeys, jsonMap)
            apply()
        }
    }

    fun getApiKeys(): Map<String, String> {
        val sharedPreferences = getPreferences()
        val jsonMap = sharedPreferences.getString(ApiKeys, "{}")
        return Gson().fromJson(jsonMap, object : TypeToken<Map<String, String>>() {}.type)
    }

    fun getApiKey(modelName: String): String? {
        val keyMap = getApiKeys()
        return keyMap[modelName]
    }

    fun getSystemPrompt(): String {
        val sharedPreferences = getPreferences()
        var systemPrompt =  sharedPreferences.getString(SystemPrompt, "") ?: ""
        if (systemPrompt == ""){
            systemPrompt = "Your response should be short and to the point"
        }
        return systemPrompt
    }

    fun setSystemPrompt(systemPrompt:String) {
        val sharedPreferences = getPreferences()
        with(sharedPreferences.edit()) {
            putString(SystemPrompt, systemPrompt)
            apply()
        }
    }
}
