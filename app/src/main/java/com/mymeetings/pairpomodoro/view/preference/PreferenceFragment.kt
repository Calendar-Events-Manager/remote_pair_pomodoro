package com.mymeetings.pairpomodoro.view.preference

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mymeetings.pairpomodoro.R

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
    }
}