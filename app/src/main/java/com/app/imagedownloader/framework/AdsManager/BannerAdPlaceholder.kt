package com.app.imagedownloader.framework.AdsManager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.imagedownloader.R
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
    private lateinit var templateView: TemplateView
    private lateinit var adpspace: TextView
    lateinit var ad: NativeAd

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
        try {
            inflateNativeAd(templateView,ad)
        }catch (e:Exception){

        }
        adpspace.visibility=View.GONE
        templateView.visibility=View.VISIBLE
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
        val newInstanceOfBannerAdPlaceholder =
            getBannerPlaceholder(position)
        newInstanceOfBannerAdPlaceholder.ad= this.ad
        GeneralAdsManager.bannerAdList[position] =newInstanceOfBannerAdPlaceholder
    }
}