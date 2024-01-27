package com.torphix.brainkey

object Constant {

    const val PREF_ROOT_NAME = "PREF_ROOT_NAME"

    const val PREF_KEYBOARD_TYPE = "PREF_KEYBOARD_TYPE"

    object Extra {
        const val EXTRA_DATA = "EXTRA_DATA"
    }

    val DEFAULT_USER_PROMPTS = listOf(
        "Check that my grammar is correct: ",
        "Translate this into ",
        "Respond to this email: ",
        "Write a flirty, funny response for: ",
        "Rewrite this to be more formal: ",
        "Write a cover letter for the internship: ",
        "Write an interesting response for: ",
        "Write a controversial tweet on: ",
        "Ask for a refund for: ",
        "Draft a message to decline an invitation: ",
        "Generate an out-of-office reply for email: ",
        "Draft a quick thank you note for: ",
        )

}