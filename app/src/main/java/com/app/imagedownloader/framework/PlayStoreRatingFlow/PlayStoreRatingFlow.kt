package com.app.imagedownloader.framework.PlayStoreRatingFlow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.CheckBox
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.app.imagedownloader.R
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.framework.Utils.Logger
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayStoreRatingFlow
@Inject constructor(
    val sharedPrefRepository: SharedPrefRepository
) {

    fun get_RATING_DIALOG_NOTSHOW_AGAIN(): Boolean {
        return sharedPrefRepository.get_RATING_DIALOG_NOTSHOW_AGAIN()
    }

    fun set_RATING_DIALOG_NOTSHOW_AGAIN(boolean: Boolean) {
        sharedPrefRepository.set_RATING_DIALOG_NOTSHOW_AGAIN(boolean)
    }

    fun get_SHOW_RATING(): Boolean {
        return sharedPrefRepository.get_SHOW_RATING()
    }

    fun reset_SHOW_RATING() {
        sharedPrefRepository.setRatingParams(false)
    }

    fun get_RATING_DIALOG_NOT_NOW(): Boolean {
        return sharedPrefRepository.get_RATING_DIALOG_NOT_NOW()
    }

    fun set_RATING_DIALOG_NOT_NOW(boolean: Boolean) {
        sharedPrefRepository.set_RATING_DIALOG_NOT_NOW(boolean)
    }

    fun get_RATING_GIVEN(): Boolean {
        return sharedPrefRepository.get_RATING_GIVEN()
    }

    fun set_RATING_GIVEN(boolean: Boolean) {
        sharedPrefRepository.set_RATING_GIVEN(boolean)
    }

    fun get_RATING_SHOW_NEVER_BOX(): Boolean {
        return sharedPrefRepository.get_RATING_SHOW_NEVER_BOX()
    }

    fun set_RATING_SHOW_NEVER_BOX(boolean: Boolean) {
        sharedPrefRepository.set_RATING_SHOW_NEVER_BOX(boolean)
    }

    fun get_RATING_DIALOG_NOT_NOW_TIME(): Long {
        return sharedPrefRepository.get_RATING_DIALOG_NOT_NOW_TIME()
    }

    fun set_RATING_DIALOG_NOT_NOW_TIME(time: Long) {
        sharedPrefRepository.set_RATING_DIALOG_NOT_NOW_TIME(time)
    }

    fun askForRating(force: Boolean = false, context: Context) {
        var show = false
        var shownever = false
        if (force) {
            show = true
        } else {
            if (get_SHOW_RATING()) {
                if (!get_RATING_DIALOG_NOTSHOW_AGAIN()) {
                    if (!get_RATING_DIALOG_NOT_NOW()) {
                        if (!get_RATING_GIVEN()) {
                            show = true
                        } else {
                            Logger.debugToast(false, context, "Rating given....")
                        }
                    } else {
                        val fm = 43200000L
                        if (System.currentTimeMillis() >= (get_RATING_DIALOG_NOT_NOW_TIME() + fm)
                        ) {
                            set_RATING_DIALOG_NOT_NOW(false)
                            set_RATING_DIALOG_NOT_NOW_TIME(0L)
                            show = true
                        }
                    }
                } else {
                    if (!get_RATING_GIVEN()) {
                        Logger.debugToast(
                            true,
                            context,
                            "Rating will be never shown and not... given...."
                        )
                    } else {
                        Logger.debugToast(true, context, "Rating will be never shown and given....")
                    }
                }
            }
            shownever = get_RATING_SHOW_NEVER_BOX()
        }
        if (show) showDialog(shownever, context, force)
    }

    private fun showDialog(shownever: Boolean, context: Context, force: Boolean) {
        val rd = MaterialDialog(context).show {
            customView(R.layout.ratingask)
            cancelOnTouchOutside(false)
        }
        rd.findViewById<TextView>(R.id.notnowbt).setOnClickListener {
            rd.dismiss()
            if (force == false) {
                set_RATING_DIALOG_NOT_NOW(true)
                set_RATING_SHOW_NEVER_BOX(true)
                reset_SHOW_RATING()
                set_RATING_DIALOG_NOT_NOW_TIME(System.currentTimeMillis())
                FirebaseCrashlytics.getInstance().log("Clicked Not Now Rating button")
                try {
                    throw Exception("Logger message : Clicked Not Now Rating button")
                } catch (e: Exception) {
                    e.message?.let {
                        Logger.log("Debug logger = $it")
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }
        }
        rd.findViewById<TextView>(R.id.reviewitbt).setOnClickListener {
            rd.dismiss()
            if (sharedPrefRepository.get_DisableAdsAndPromo()) return@setOnClickListener
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data =
                    Uri.parse("https://play.google.com/store/apps/details?id=com.app.imagedownloader")
            }
            set_RATING_GIVEN(true)
            context.startActivity(intent)
            FirebaseCrashlytics.getInstance().log("Clicked Rating button")
            try {
                throw Exception("Logger message : Clicked Rating button")
            } catch (e: Exception) {
                e.message?.let {
                    Logger.log("Debug logger = $it")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
        if (shownever) {
//            rd.findViewById<CheckBox>(R.id.cb).visibility = View.VISIBLE
        }
        rd.findViewById<CheckBox>(R.id.cb).setOnCheckedChangeListener { _, b ->
//            set_RATING_DIALOG_NOTSHOW_AGAIN(b)
        }
    }

    private fun promptRatingDialog(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Logger.log("Rating success")
                // We got the ReviewInfo object
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Logger.log("Rating  1 success")
                    } else {
                        Logger.log("Rating 1 failed......")
                    }
                }
            } else {
                Logger.log("Rating failed......")
            }
        }
    }
}