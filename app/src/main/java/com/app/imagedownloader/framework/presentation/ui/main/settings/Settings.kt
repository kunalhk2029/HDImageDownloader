package com.app.imagedownloader.framework.presentation.ui.main.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.Utils.VibrateExtension
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPreferencesRepositoryImpl
import com.app.imagedownloader.databinding.FragmentSettingsBinding
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.app.imagedownloader.framework.presentation.ui.main.MainViewModel.Companion.appThemeChannel
import kotlinx.coroutines.launch

class  Settings : Fragment(R.layout.fragment_settings) {

    var binding: FragmentSettingsBinding? = null

    lateinit var vibrate: VibrateExtension

    lateinit var sharedPrefRepository: SharedPrefRepository

    var logout: ConstraintLayout? = null
    var vibratee: ConstraintLayout? = null
    var offlineeffect: ConstraintLayout? = null
    var homescreensettings: ConstraintLayout? = null
    var backupsettings: ConstraintLayout? = null
    var paymentsettings: ConstraintLayout? = null
    var autodownloadfeatures: ConstraintLayout? = null

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setuiCommunicationLiatener(null, context)
    }

    private fun setuiCommunicationLiatener(
        uICommunicationListener: UICommunicationListener?,
        context: Context?
    ) {
        if (uICommunicationListener != null) {
            uiCommunicationListener = uICommunicationListener
        } else {
            try {
                uiCommunicationListener = context as UICommunicationListener
            } catch (e: ClassCastException) {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logout = null
        vibratee = null
        offlineeffect = null
        homescreensettings = null
        backupsettings = null
        paymentsettings = null
        autodownloadfeatures = null
        binding = null
    }

    private fun inititalizeviews() {
        vibratee = requireView().findViewById(R.id.hapticsettings)
        paymentsettings = requireView().findViewById(R.id.premiumsettings)
        sharedPrefRepository= SharedPreferencesRepositoryImpl(
            requireContext().getSharedPreferences(Constants.MAIN_SHARED_PREFS, Context.MODE_PRIVATE)
        )
        vibrate = VibrateExtension(requireContext(), sharedPrefRepository)
        if (!vibrate.hasVibrator()) {
            vibratee?.visibility = View.GONE
        }
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        inititalizeviews()

        setVibrateStatus()

        vibratee?.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(null, getString(R.string.hapticfeedback))
                listItemsSingleChoice(
                    null, arrayListOf(
                        getString(R.string.enablefeedback), getString(
                            R.string.disablefeedback
                        )
                    )
                ) { _, index, _ ->
                    changehaptic(index)
                }
                positiveButton(null, getString(R.string.ok)) {
                    this.dismiss()
                }
            }
        }


        paymentsettings?.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_payment)
        }


        binding?.feedbacksettings?.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_feedback)
        }

        binding?.ratingsettings?.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data= Uri.parse("https://play.google.com/store/apps/details?id=com.app.imagedownloader")
            }
            startActivity(intent)
        }

        binding?.themesettings?.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(null, getString(R.string.choose_theme))
                listItemsSingleChoice(
                    null, arrayListOf(
                        getString(R.string.light_theme), getString(R.string.dark_theme), getString(
                            R.string.systemdefault
                        )
                    )
                ) { _, index, _ ->
                    changetheme(index)
                }
                positiveButton(null, getString(R.string.ok)) {
                    dismiss()
                }
            }
        }
    }

    private fun changetheme(index: Int) {
        sharedPrefRepository.changetheme(index)
        lifecycleScope.launch {
            appThemeChannel.send(index)
        }
        if (index == 2) {
            binding?.themevalue?.text = getString(R.string.systemdefault)
        } else if (index == 1) {
            binding?.themevalue?.text = getString(R.string.dark_theme)
        } else if (index == 0) {
            binding?.themevalue?.text = getString(R.string.light_theme)
        }
    }

    private fun setVibrateStatus(vibratee: Boolean = false) {
        if (get_HAPTIC_STATUS()) {
            binding?.hapticvalue?.text = getString(R.string.enabled)
            VibrateExtension.isenabledvibrate = true
            if (vibratee) vibrate.vibrate()
        } else {
            binding?.hapticvalue?.text = getString(R.string.disabled)
            VibrateExtension.isenabledvibrate = false
        }
    }

    fun set_HAPTIC_STATUS(boolean: Boolean) {
        sharedPrefRepository.set_HAPTIC_STATUS(boolean)
    }

    fun get_HAPTIC_STATUS(): Boolean {
        return sharedPrefRepository.get_HAPTIC_STATUS()
    }

    private fun changehaptic(index: Int) {
        if (index == 1) {
            set_HAPTIC_STATUS(false)
        } else {
            set_HAPTIC_STATUS(true)
        }
        setVibrateStatus(true)
    }
}