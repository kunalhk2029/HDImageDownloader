package com.app.imagedownloader.Utils

import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity

object SystemUiVisibility {

    private var initialNacigationBarCcolor = -1


    fun Fragment.setDefaultBarColor() {
        try {
            val windowInsetsControllerCompat=WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            )
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.pin_red)
            requireActivity().window.navigationBarColor = initialNacigationBarCcolor

            windowInsetsControllerCompat.isAppearanceLightNavigationBars = isDark(initialNacigationBarCcolor) == false
            windowInsetsControllerCompat.isAppearanceLightStatusBars =
                isDark(ContextCompat.getColor(requireContext(), R.color.pin_red)) == false
        } catch (_: Exception) {
        }
    }

    fun Fragment.changeStatusAndNavigationBarColor(@ColorInt color: Int) {
        try {
            val windowInsetsControllerCompat=WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            )
            if (initialNacigationBarCcolor == -1) initialNacigationBarCcolor =
                requireActivity().window.navigationBarColor
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

    fun Fragment.hideStatusBar() {
        try {
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            ).systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            ).hide(WindowInsetsCompat.Type.statusBars())
        } catch (e: Exception) {

        }
    }

    fun Fragment.showStatusBar() {
        try {
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            ).show(WindowInsetsCompat.Type.statusBars())
        } catch (e: Exception) {

        }
    }

    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }
}