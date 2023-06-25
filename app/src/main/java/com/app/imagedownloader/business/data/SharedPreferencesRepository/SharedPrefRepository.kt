package com.app.imagedownloader.business.data.SharedPreferencesRepository

import java.util.*

interface SharedPrefRepository {

    fun changetheme(id: Int)

    fun getTheme(): Int

    fun setOnBoarding(boolean: Boolean)

    fun getOnBoarding(): Boolean

    fun set_HAPTIC_STATUS(boolean: Boolean)

    fun get_HAPTIC_STATUS(): Boolean

    fun get_FCM_COMMON_SUBSCRIBED(): Boolean

    fun set_FCM_COMMON_SUBSCRIBED(boolean: Boolean)

    fun get_RATING_DIALOG_NOT_NOW(): Boolean

    fun set_RATING_DIALOG_NOT_NOW_TIME(time: Long)

    fun get_RATING_DIALOG_NOT_NOW_TIME(): Long

    fun set_RATING_DIALOG_NOT_NOW(boolean: Boolean)

    fun get_RATING_SHOW_NEVER_BOX(): Boolean

    fun set_RATING_SHOW_NEVER_BOX(boolean: Boolean)

    fun get_RATING_GIVEN(): Boolean

    fun set_RATING_GIVEN(boolean: Boolean)

    fun get_SHOW_RATING(): Boolean

    fun get_RATING_DIALOG_NOTSHOW_AGAIN(): Boolean

    fun set_RATING_DIALOG_NOTSHOW_AGAIN(boolean: Boolean)

    fun setRatingParams(boolean: Boolean)

    fun get_DisableAdsAndPromo(): Boolean

    fun set_DisableAdsAndPromo(boolean: Boolean)
}
