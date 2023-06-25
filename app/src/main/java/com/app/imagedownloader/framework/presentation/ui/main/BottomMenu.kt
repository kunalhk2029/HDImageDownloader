package com.app.imagedownloader.framework.presentation.ui.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity.Companion.premiumLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BottomMenu:BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.bottom_menu,container,false)
    }


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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        try {
            dialog.setOnShowListener {
                val bottomSheet = dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) as? FrameLayout

                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        } catch (e: Exception) {

        }
        return dialog
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireView().findViewById<TextView>(R.id.premiummenu).setOnClickListener {
            lifecycleScope.launch {
                premiumLiveData.send(1)
            }
        }

        requireView().findViewById<TextView>(R.id.settingsmenu).setOnClickListener {
            uiCommunicationListener.askForRating(true)
        }

        requireView().findViewById<TextView>(R.id.helpmenu).setOnClickListener {
            findNavController().navigate(
                R.id.feedback,
                null, NavOptions.Builder().setEnterAnim(R.anim.fromright)
                    .setExitAnim(R.anim.toleft).setPopExitAnim(R.anim.toright)
                    .setPopEnterAnim(R.anim.fromleft).build()
            )
        }

        requireView().findViewById<TextView>(R.id.sharemenu).setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hello I would like to recommend this app to you.Please give it a try. It is a very good productivity app that download instagram stories automatically. https://play.google.com/store/apps/details?id=com.app.autoinstastorytale"
                )
            }
            val chooser = Intent.createChooser(intent, "Share App")
            startActivity(chooser)
        }
    }
}