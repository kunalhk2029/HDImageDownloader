package com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.imagedownloader.R
import com.app.imagedownloader.business.domain.model.DownloadedMediaInfo
import com.app.imagedownloader.framework.Glide.GlideManager

class DownloadedMediaPreviewAdapter(
    private val interaction: Interaction? = null,
    private val glideManager: GlideManager,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DownloadedMediaInfo>() {

        override fun areItemsTheSame(
            oldItem: DownloadedMediaInfo,
            newItem: DownloadedMediaInfo,
        ): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(
            oldItem: DownloadedMediaInfo,
            newItem: DownloadedMediaInfo,
        ): Boolean {
            return oldItem.uri == newItem.uri
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return DownloadedMediaPreviewViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.downloaded_media_preview_item,
                parent,
                false
            ),
            interaction, glideManager
        )
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

    fun submitList(list: List<DownloadedMediaInfo>) {
        differ.submitList(list)
    }

    class DownloadedMediaPreviewViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val glideManager: GlideManager,
    ) : RecyclerView.ViewHolder(itemView) {

        val preview = itemView.findViewById<ImageView>(R.id.mediaPreview)

        fun bind(item: DownloadedMediaInfo) = with(itemView) {
//            glideManager.setImageFromUrl(preview, item.uri.toString())
            glideManager.setImageFromUrlWithPlaceHolder(preview,
                item.uri.toString(),
                null,
                null,
                R.drawable.image_placeholder)
            itemView.setOnClickListener {
                interaction?.onItemSelected(bindingAdapterPosition, item)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: DownloadedMediaInfo)
    }
}