package com.app.imagedownloader.framework.AdsManager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.imagedownloader.R
import com.google.android.ads.nativetemplates.TemplateView

class BannerAdPlaceholder : Fragment(R.layout.fragment_banner_ad_placeholder) {

    var position = -1
    lateinit var templateView: TemplateView
    lateinit var adpspace: TextView

    companion object {
        fun getBannerPlaceholder(position: Int): BannerAdPlaceholder {
            val args = Bundle().apply {
                putInt("position", position)
            }
            val instance = BannerAdPlaceholder()
            instance.arguments = args
            return instance
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        position = requireArguments().getInt("position")
        templateView = requireView().findViewById(R.id.my_template)
        adpspace = requireView().findViewById(R.id.adpspace)
        GeneralAdsManager.bannerAdList[position] = this
    }

    override fun onDestroy() {
        super.onDestroy()
        GeneralAdsManager.bannerAdList[position] = getBannerPlaceholder(position)
    }
}