package com.app.imagedownloader.framework.presentation.ui.main.favPhotosPreview

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
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavPhotosPreviewAdapter(
    private val interaction: Interaction? = null,
    private val glideManager: GlideManager,
    private val generalAdsManager: GeneralAdsManager
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavPhotos>() {

        override fun areItemsTheSame(
            oldItem: FavPhotos,
            newItem: FavPhotos,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FavPhotos,
            newItem: FavPhotos,
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return DownloadedMediaPreviewViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.fav_photo_preview_item,
                parent,
                false
            ),
            interaction, glideManager
        ,generalAdsManager)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DownloadedMediaPreviewViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<FavPhotos>) {
        differ.submitList(list)
    }

    class DownloadedMediaPreviewViewHolder
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

        fun bind(item: FavPhotos) = with(itemView) {
            adView.visibility=View.GONE
            adViewCta.visibility=View.GONE
            adspaceholder.visibility=View.GONE

            if (item.previewUrl=="previewUrl"){
                adspaceholder.visibility=View.VISIBLE
                descriptionCard.visibility=View.GONE
                preview.visibility=View.GONE
                CoroutineScope(Dispatchers.Main).launch {
                    generalAdsManager.showNativeAdapterItemAd(adView,itemView).let {
                        if (it) adsFreeCard.visibility=View.GONE
                        else adsFreeCard.visibility=View.VISIBLE
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
                val color = item.colorCode
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
                interaction?.onItemSelected(bindingAdapterPosition, item)
            }

        }

        fun isDark(@ColorInt color: Int): Boolean {
            return ColorUtils.calculateLuminance(color) < 0.5
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: FavPhotos)
    }
}