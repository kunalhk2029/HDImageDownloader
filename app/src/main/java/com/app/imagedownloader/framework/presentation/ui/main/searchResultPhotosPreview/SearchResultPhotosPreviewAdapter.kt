package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.imagedownloader.R
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class SearchResultPhotosPreviewAdapter(
    private val interaction: Interaction? = null,
    private val glideManager: GlideManager,
    private val generalAdsManager: GeneralAdsManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UnsplashPhotoInfo.photoInfo>() {

        override fun areItemsTheSame(
            oldItem: UnsplashPhotoInfo.photoInfo,
            newItem: UnsplashPhotoInfo.photoInfo
        ): Boolean {
            return oldItem.previewUrl == newItem.previewUrl
        }

        override fun areContentsTheSame(
            oldItem: UnsplashPhotoInfo.photoInfo,
            newItem: UnsplashPhotoInfo.photoInfo
        ): Boolean {
            return oldItem.previewUrl == newItem.previewUrl
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return SearchResultPhotosPreviewViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.searchresults_photo_preview_item,
                parent,
                false
            ),
            interaction, glideManager,generalAdsManager
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchResultPhotosPreviewViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<UnsplashPhotoInfo.photoInfo>) {
        differ.submitList(list)
    }

    class SearchResultPhotosPreviewViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val glideManager: GlideManager,
        private val generalAdsManager: GeneralAdsManager
    ) : RecyclerView.ViewHolder(itemView) {

        val preview = itemView.findViewById<ImageView>(R.id.mediaPreview)
        val description = itemView.findViewById<TextView>(R.id.descriptionHolder)
        val adspaceholder = itemView.findViewById<TextView>(R.id.adspaceholder)
        val descriptionCard = itemView.findViewById<CardView>(R.id.DescriptionCard)
        val adsFreeCard = itemView.findViewById<CardView>(R.id.adsFreeCard)
        val shimmer_layout = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_layout)
        val adView =itemView.findViewById(R.id.native_ad_view) as NativeAdView
        val adViewCta =itemView.findViewById(R.id.ctaCard) as CardView

        fun bind(item: UnsplashPhotoInfo.photoInfo) = with(itemView) {
            adView.visibility=View.GONE
            adViewCta.visibility=View.GONE
            adspaceholder.visibility=View.GONE

            if (item.previewUrl=="previewUrl"){
                adspaceholder.visibility=View.VISIBLE
                descriptionCard.visibility=View.GONE
                adsFreeCard.visibility=View.VISIBLE
                preview.visibility=View.GONE
                CoroutineScope(Main).launch {
                    generalAdsManager.showNativeAdapterItemAd(adView,itemView).let {
                        adspaceholder.visibility=View.GONE
                    }
                }
                return@with
            }

            adsFreeCard.visibility=View.GONE
            descriptionCard.visibility=View.VISIBLE
            preview.visibility=View.VISIBLE
            shimmer_layout.visibility = View.VISIBLE
            glideManager.setImageFromUrlWithPlaceHolder(
                preview, item.previewUrl, glideSuccessUnit = {
                    kotlin.run {
                        shimmer_layout.stopShimmer()
                        shimmer_layout.visibility = View.GONE
                    }
                }, placeholder =
                R.drawable.image_placeholder
            )


            description.visibility = View.GONE
            item.description?.let {
                val color = Color.parseColor(item.colorCode)
                description.setBackgroundColor(color)
                description.text = it
                if (!isDark(color))
                    description.setTextColor(Color.parseColor("#111111"))
                else description.setTextColor(Color.parseColor("#ffffff"))

                description.visibility = View.VISIBLE
            } ?: kotlin.run {
                description.visibility = View.GONE
            }
            itemView.setOnClickListener {
                interaction?.onItemClicked(bindingAdapterPosition, item)
            }
        }

        fun isDark(@ColorInt color: Int): Boolean {
            return ColorUtils.calculateLuminance(color) < 0.5
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int, item: UnsplashPhotoInfo.photoInfo)
    }
}