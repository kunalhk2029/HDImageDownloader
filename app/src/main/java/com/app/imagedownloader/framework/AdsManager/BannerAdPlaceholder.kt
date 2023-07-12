package com.app.imagedownloader.framework.AdsManager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.imagedownloader.R
import com.app.imagedownloader.databinding.FragmentBannerAdPlaceholderBinding
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BannerAdPlaceholder : Fragment(R.layout.fragment_banner_ad_placeholder) {

    var position = -1
    var binding:FragmentBannerAdPlaceholderBinding?=null

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
        binding = FragmentBannerAdPlaceholderBinding.bind(view)
        position = requireArguments().getInt("position")
        val templateView = binding?.myTemplate
        val adpspace = binding?.adpspace
        GeneralAdsManager.bannerAdPlaceHolderList[position] = this
        try {
            GeneralAdsManager.bannerAdList[position]?.let {
                templateView?.let { it1 -> inflateNativeAd(it1, it) }
            }
        } catch (e: Exception) { }
        adpspace?.visibility = View.GONE
        templateView?.visibility = View.VISIBLE
    }

    private fun inflateNativeAd(template: TemplateView, ad: NativeAd) {
        val styles =
            NativeTemplateStyle.Builder()
                .build()
        template.setStyles(styles)
        template.setNativeAd(ad)
        template.premiumButton.setOnClickListener {
            CoroutineScope(IO).launch {
                MainActivity.premiumLiveData.send(2)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
        val newInstanceOfBannerAdPlaceholder =
            getBannerPlaceholder(position)
        GeneralAdsManager.bannerAdPlaceHolderList[position] = newInstanceOfBannerAdPlaceholder
    }
}