package com.example.pc.ui.activities

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.example.pc.R

private const val TAG = "SettingsFragment"


class SettingsActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isNightTheme = prefs.getBoolean(getString(R.string.dark_mode), false)

        Log.i(TAG, "current theme: $isNightTheme")

        when (isNightTheme) {
            false ->{
                supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(
                        getColor(R.color.white_darker)
                    )
                )
            }

            true ->{
                supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(
                        getColor(R.color.even_darker_grey)
                    )
                )
            }
        }

        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val darkModePref: SwitchPreference? = findPreference(getString(R.string.dark_mode))

            darkModePref?.setOnPreferenceClickListener {

                val isChecked = it
                    .sharedPreferences
                    .getBoolean(
                        getString(R.string.dark_mode),
                        darkModePref.isChecked
                    )

                Log.i(TAG, "onCreatePreferences: dark mode is checked $isChecked")

                when(isChecked){
                    true -> {
                        Log.i(TAG, "onCreatePreferences: switching to dark mode")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    else -> {
                        Log.i(TAG, "onCreatePreferences: switching to light mode")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }

                true
            }
        }
    }
//    fun isDarkTheme{
//
//    }
}