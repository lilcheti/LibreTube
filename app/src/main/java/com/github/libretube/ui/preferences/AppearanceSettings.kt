package com.github.libretube.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.github.libretube.R
import com.github.libretube.constants.PreferenceKeys
import com.github.libretube.ui.activities.SettingsActivity
import com.github.libretube.ui.base.BasePreferenceFragment
import com.github.libretube.ui.dialogs.NavBarOptionsDialog
import com.github.libretube.ui.dialogs.RequireRestartDialog
import com.github.libretube.util.ThemeHelper
import com.google.android.material.color.DynamicColors

class AppearanceSettings : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.appearance_settings, rootKey)

        val settingsActivity = activity as? SettingsActivity
        settingsActivity?.changeTopBarText(getString(R.string.appearance))

        val themeToggle = findPreference<ListPreference>(PreferenceKeys.THEME_MODE)
        themeToggle?.setOnPreferenceChangeListener { _, _ ->
            val restartDialog = RequireRestartDialog()
            restartDialog.show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val pureTheme = findPreference<SwitchPreferenceCompat>(PreferenceKeys.PURE_THEME)
        pureTheme?.setOnPreferenceChangeListener { _, _ ->
            val restartDialog = RequireRestartDialog()
            restartDialog.show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val accentColor = findPreference<ListPreference>(PreferenceKeys.ACCENT_COLOR)
        updateAccentColorValues(accentColor!!)
        accentColor.setOnPreferenceChangeListener { _, _ ->
            val restartDialog = RequireRestartDialog()
            restartDialog.show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val iconChange = findPreference<ListPreference>(PreferenceKeys.APP_ICON)
        iconChange?.setOnPreferenceChangeListener { _, newValue ->
            ThemeHelper.changeIcon(requireContext(), newValue.toString())
            true
        }

        val labelVisibilityMode = findPreference<ListPreference>(PreferenceKeys.LABEL_VISIBILITY)
        labelVisibilityMode?.setOnPreferenceChangeListener { _, _ ->
            val restartDialog = RequireRestartDialog()
            restartDialog.show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val navBarOptions = findPreference<Preference>(PreferenceKeys.NAVBAR_ITEMS)
        navBarOptions?.setOnPreferenceClickListener {
            NavBarOptionsDialog().show(
                childFragmentManager,
                null
            )
            true
        }

        val legacySubscriptionView =
            findPreference<SwitchPreferenceCompat>(PreferenceKeys.LEGACY_SUBSCRIPTIONS)
        val legacySubscriptionColumns =
            findPreference<ListPreference>(PreferenceKeys.LEGACY_SUBSCRIPTIONS_COLUMNS)
        legacySubscriptionColumns?.isVisible = legacySubscriptionView?.isChecked!!
        legacySubscriptionView.setOnPreferenceChangeListener { _, newValue ->
            legacySubscriptionColumns?.isVisible = newValue as Boolean
            true
        }
    }

    // remove material you from accent color option if not available
    private fun updateAccentColorValues(pref: ListPreference) {
        val dynamicColorsAvailable = DynamicColors.isDynamicColorAvailable()
        if (!dynamicColorsAvailable) {
            val entries = pref.entries.toMutableList()
            entries -= entries[0]
            pref.entries = entries.toTypedArray()
            val values = pref.entryValues.toMutableList()
            values -= values[0]
            pref.entryValues = values.toTypedArray()
        }
    }
}
