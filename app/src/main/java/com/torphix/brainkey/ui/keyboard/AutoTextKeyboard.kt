package com.torphix.brainkey.ui.keyboard.autotext

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.frogobox.libkeyboard.common.core.BaseKeyboard
import com.frogobox.recycler.core.FrogoRecyclerNotifyListener
import com.frogobox.recycler.core.IFrogoBindingAdapter
import com.frogobox.recycler.ext.injectorBinding
import com.torphix.brainkey.databinding.KeyboardAutotextBinding


class AutoTextKeyboard(
    context: Context,
    attrs: AttributeSet?,
) : BaseKeyboard<KeyboardAutotextBinding>(context, attrs) {

    override fun setupViewBinding(): KeyboardAutotextBinding {
        return KeyboardAutotextBinding.inflate(LayoutInflater.from(context), this, true)
    }
    override fun onCreate() {
        initView()
    }
    private fun initView() {
        binding?.apply {
            tvToolbarTitle.text = "Auto Text"
        }
    }
}