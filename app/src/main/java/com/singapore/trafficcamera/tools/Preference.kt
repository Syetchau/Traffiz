package com.singapore.trafficcamera.tools

import com.pixplicity.easyprefs.library.Prefs

object Preference {

    private const val PREF_USERNAME = "PREF_USERNAME"
    private const val PREF_PASSWORD = "PREF_PASSWORD"

    var prefUsername: String
        get() = Prefs.getString(PREF_USERNAME, "")
        set(username) = Prefs.putString(PREF_USERNAME, username)

    var prefPassword: String
        get() = Prefs.getString(PREF_PASSWORD, "")
        set(password) = Prefs.putString(PREF_PASSWORD, password)
}