package com.app.imagedownloader.business.data.SharedPreferencesRepository

import android.content.SharedPreferences
import com.app.imagedownloader.Utils.Constants.Constants.FCM_COMMON_SUBSCRIBED
import com.app.imagedownloader.Utils.Constants.Constants.ON_BOARDING
import com.app.imagedownloader.Utils.Constants.SettingsConstants.DISABLE_ADS_PROMO
import com.app.imagedownloader.Utils.Constants.SettingsConstants.FIRST_TIME_OPENED
import com.app.imagedownloader.Utils.Constants.SettingsConstants.RATING_DIALOG_NOTSHOW_AGAIN
import com.app.imagedownloader.Utils.Constants.SettingsConstants.RATING_DIALOG_NOT_NOW
import com.app.imagedownloader.Utils.Constants.SettingsConstants.RATING_DIALOG_NOT_NOW_TIME
import com.app.imagedownloader.Utils.Constants.SettingsConstants.RATING_GIVEN
import com.app.imagedownloader.Utils.Constants.SettingsConstants.RATING_SHOW_NEVER_BOX
import com.app.imagedownloader.Utils.Constants.SettingsConstants.THEME
import com.app.imagedownloader.Utils.Constants.SettingsConstants.VIBRATION
import com.app.imagedownloader.framework.Utils.Logger

class SharedPreferencesRepositoryImpl(private val pref: SharedPreferences) : SharedPrefRepository {

    override fun setRatingParams(boolean: Boolean) {
        var isSetThreeTimes = pref.getInt("isSetThreeTimes", 0)
        if (isSetThreeTimes > 3 && boolean) return
        isSetThreeTimes++
        pref.edit().apply {
            if (!boolean) {
                putInt("isSetThreeTimes", 0)
            } else {
                putInt("isSetThreeTimes", isSetThreeTimes)
            }
            apply()
        }
    }

    override fun get_FCM_COMMON_SUBSCRIBED(): Boolean {
        return pref.getBoolean(FCM_COMMON_SUBSCRIBED, false)
    }

    override fun set_HAPTIC_STATUS(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(VIBRATION, boolean)
            apply()
        }
    }

    override fun get_HAPTIC_STATUS(): Boolean {
        return pref.getBoolean(VIBRATION, true)
    }

    override fun set_FCM_COMMON_SUBSCRIBED(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(FCM_COMMON_SUBSCRIBED, boolean)
            apply()
        }
    }

    override fun get_RATING_DIALOG_NOT_NOW(): Boolean {
        return pref.getBoolean(RATING_DIALOG_NOT_NOW, false)
    }

    override fun set_RATING_DIALOG_NOT_NOW_TIME(time: Long) {
        pref.edit().apply {
            putLong(RATING_DIALOG_NOT_NOW_TIME, time)
            apply()
        }
    }

    override fun get_RATING_DIALOG_NOT_NOW_TIME(): Long {
        return pref.getLong(RATING_DIALOG_NOT_NOW_TIME, 0L)
    }

    override fun set_RATING_DIALOG_NOT_NOW(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(RATING_DIALOG_NOT_NOW, boolean)
            apply()
        }
    }

    override fun get_RATING_SHOW_NEVER_BOX(): Boolean {
        return pref.getBoolean(RATING_SHOW_NEVER_BOX, false)
    }

    override fun set_RATING_SHOW_NEVER_BOX(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(RATING_SHOW_NEVER_BOX, boolean)
            apply()
        }
    }

    override fun get_RATING_GIVEN(): Boolean {
        return pref.getBoolean(RATING_GIVEN, false)
    }

    override fun set_RATING_GIVEN(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(RATING_GIVEN, boolean)
            apply()
        }
    }

    override fun get_SHOW_RATING(): Boolean {
        val isSetThreeTimes = pref.getInt("isSetThreeTimes", 0)
        return isSetThreeTimes >= 3
    }

    override fun get_RATING_DIALOG_NOTSHOW_AGAIN(): Boolean {
        return pref.getBoolean(RATING_DIALOG_NOTSHOW_AGAIN, false)
    }

    override fun set_RATING_DIALOG_NOTSHOW_AGAIN(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(RATING_DIALOG_NOTSHOW_AGAIN, boolean)
            apply()
        }
    }

    override fun getOnBoarding(): Boolean {
        return pref.getBoolean(ON_BOARDING, true)
    }

    override fun setOnBoarding(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(ON_BOARDING, boolean)
            apply()
        }
    }

    override fun get_DisableAdsAndPromo(): Boolean {
        return pref.getBoolean(DISABLE_ADS_PROMO, true)
    }

    override fun set_DisableAdsAndPromo(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(DISABLE_ADS_PROMO, boolean)
            apply()
        }
    }

    override fun changetheme(id: Int) {
        pref.edit().apply {
            putInt(THEME, id)
            apply()
        }
    }

    override fun getTheme(): Int {
        return pref.getInt(THEME, 2)
    }

    override fun get_FirstTimeOpened(): Boolean {
        return pref.getBoolean(FIRST_TIME_OPENED, true)
    }

    override fun set_FirstTimeOpened(boolean: Boolean) {
        pref.edit().apply {
            putBoolean(FIRST_TIME_OPENED, boolean)
            apply()
        }
    }
}
