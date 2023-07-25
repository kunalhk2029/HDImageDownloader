package com.app.imagedownloader.framework.presentation.ui

import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity

object SystemUiVisibility {

    private var initialNavigationBarColor = -1
    private var initialStatusBarColor = -1

    fun Fragment.setDefaultBarColor() {
        try {
            val windowInsetsControllerCompat=WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            )
            requireActivity().window.statusBarColor =
              initialStatusBarColor
            requireActivity().window.navigationBarColor = initialNavigationBarColor

            windowInsetsControllerCompat.isAppearanceLightNavigationBars = isDark(
                initialNavigationBarColor) == false
            windowInsetsControllerCompat.isAppearanceLightStatusBars =
                isDark(initialStatusBarColor) == false
        } catch (_: Exception) {
        }
    }

    fun Fragment.changeStatusAndNavigationBarColor(@ColorInt color: Int) {
        try {
            val windowInsetsControllerCompat=WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            )
            if (initialNavigationBarColor == -1) initialNavigationBarColor =
                requireActivity().window.navigationBarColor

            if (initialStatusBarColor == -1) initialStatusBarColor =
                requireActivity().window.statusBarColor

            requireActivity().window.statusBarColor = color
            requireActivity().window.navigationBarColor = color

            windowInsetsControllerCompat.isAppearanceLightNavigationBars = isDark(color) == false
            windowInsetsControllerCompat.isAppearanceLightStatusBars = isDark(color) == false
        } catch (_: Exception) {
        }

    }

    fun Fragment.handleAdsOnBackPressed(): Boolean {
        val binding = (requireActivity() as MainActivity).binding
        if (binding?.root?.findViewById<View>(R.id.native_interstitial_ad)?.visibility == View.VISIBLE) {
            return true
        }
        return false
    }

    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }
}