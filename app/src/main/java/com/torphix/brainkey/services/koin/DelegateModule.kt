package com.torphix.brainkey.services.koin

import com.torphix.brainkey.Constant.PREF_ROOT_NAME
import com.frogobox.sdk.delegate.preference.PreferenceDelegatesImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Created by Faisal Amir on 08/01/23
 * Copyright (C) Frogobox
 */

val delegateModule = module {

    single {
        PreferenceDelegatesImpl(androidContext(), PREF_ROOT_NAME)
    }

}
