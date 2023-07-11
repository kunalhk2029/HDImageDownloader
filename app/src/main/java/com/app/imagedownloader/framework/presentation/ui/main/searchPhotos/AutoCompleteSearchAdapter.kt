package com.app.imagedownloader.framework.presentation.ui.main.searchPhotos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.imagedownloader.R

class AutoCompleteSearchAdapter(
    private val interaction: Interaction? = null,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return AutoCompleteSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.langarray_item,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AutoCompleteSearchViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<String>) {
        differ.submitList(list)
    }

    class AutoCompleteSearchViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        val textPreview = itemView.findViewById<TextView>(R.id.textholder)

        fun bind(item: String) = with(itemView) {
            textPreview.text=item
            itemView.setOnClickListener {
                interaction?.onItemClicked(item,bindingAdapterPosition)
            }
        }
    }

    interface Interaction {
        fun onItemClicked(item: String,position: Int)
    }
}