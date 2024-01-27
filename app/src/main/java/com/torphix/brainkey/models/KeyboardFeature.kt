package com.torphix.brainkey.models

enum class KeyboardFeatureType(val id: String, val title: String) {
    AUTO_TEXT("menu_auto_text","Query"),
    CONTINUE_TEXT("menu_continue_text", "Continue"),
    PROMPTS("menu_prompts", "Prompts"),
    SETTING("menu_setting","Setting")
}
data class KeyboardFeature(
    var id: String,
    var type: KeyboardFeatureType,
    var icon: Int,
    var state: Boolean = false
)