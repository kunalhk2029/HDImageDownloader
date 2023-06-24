package com.app.imagedownloader.framework.presentation.ui.OnBoarding.Features_screen

import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.presentation.ui.OnBoarding.OnBoardingViewModel
import kotlinx.coroutines.launch

class OnBoarding1 : Fragment(R.layout.fragment_on_boarding1) {
    val viewModel by activityViewModels<OnBoardingViewModel>()
    var card: CardView? = null
    var skipcard: CardView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        card = requireView().findViewById(R.id.nextcard)
        skipcard = requireView().findViewById(R.id.skipcard)

        card?.setOnClickListener {
            lifecycleScope.launch {
                viewModel.nextListener.send(true)
            }
        }
        skipcard?.setOnClickListener {
            lifecycleScope.launch {
                viewModel.skipListener.send(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skipcard = null
        card = null
    }
}