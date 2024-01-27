package com.torphix.brainkey.services

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import com.torphix.brainkey.R
import com.torphix.brainkey.models.KeyboardFeature
import com.torphix.brainkey.models.KeyboardFeatureType
import com.frogobox.sdk.delegate.preference.PreferenceDelegatesImpl
import org.koin.java.KoinJavaComponent

class KeyboardUtil {

    private val pref: PreferenceDelegatesImpl by KoinJavaComponent.inject(PreferenceDelegatesImpl::class.java)

    fun menuToggle(): List<KeyboardFeature> {
        return listOf(
            KeyboardFeature(
                KeyboardFeatureType.PROMPTS.id,
                KeyboardFeatureType.PROMPTS,
                R.drawable.ic_menu_book,
                pref.loadPrefBoolean(KeyboardFeatureType.PROMPTS.id, true)
            ),
            KeyboardFeature(
                KeyboardFeatureType.AUTO_TEXT.id,
                KeyboardFeatureType.AUTO_TEXT,
                R.drawable.ic_menu_auto_text,
                pref.loadPrefBoolean(KeyboardFeatureType.AUTO_TEXT.id, true)
            ),
            KeyboardFeature(
                KeyboardFeatureType.CONTINUE_TEXT.id,
                KeyboardFeatureType.CONTINUE_TEXT,
                R.drawable.ic_pan_right,
                pref.loadPrefBoolean(KeyboardFeatureType.CONTINUE_TEXT.id, true)
            ),
        ).sortedBy { it.state }
    }
    fun menuKeyboard(): List<KeyboardFeature> {
        val listFeature = mutableListOf<KeyboardFeature>()
        menuToggle().forEach { data ->
            if (data.state) {
                listFeature.add(data)
            }
        }
        return listFeature
    }

}