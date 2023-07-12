package com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.business.domain.model.DownloadedMediaInfo
import com.app.imagedownloader.databinding.FragmentDownloadedMediaBinding
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.state.DownloadsPreviewStateEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class DownloadsPreview : Fragment(R.layout.fragment_downloaded_media),
    DownloadedMediaPreviewAdapter.Interaction {

    @Inject
    lateinit var glideManager: GlideManager

    lateinit var adapter: DownloadedMediaPreviewAdapter
    private var binding: FragmentDownloadedMediaBinding? = null

    private val downloadsPreviewViewModel by viewModels<DownloadsPreviewViewModel>()

    companion object{
        val photoDeletionListener: Channel<Boolean> = Channel()
    }
    lateinit var uiCommunicationListener: UICommunicationListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (_: ClassCastException) {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDownloadedMediaBinding.bind(view)

        setupAdapter()
        setupRecyclerView()
        subscribeObservers()

        photoDeletionListener.receiveAsFlow().onEach {
            if (it) downloadsPreviewViewModel.onEvent(downloadsPreviewStateEvents = DownloadsPreviewStateEvents.getDownloadedMediaFromOfflineStorage)
        }.launchIn(lifecycleScope)
    }


    private fun setupRecyclerView() {
        binding?.let {
            it.recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            it.recyclerView.adapter = adapter
        }
    }

    private fun setupAdapter() {
        adapter = DownloadedMediaPreviewAdapter(this, glideManager)
    }

    private fun subscribeObservers() {
        downloadsPreviewViewModel.downloadsPreviewDataState.observe(viewLifecycleOwner) {
            it.data?.getContentIfNotHandled()?.list?.let {
                downloadsPreviewViewModel.setDownloadedMediaPreviewList(it)
            }

            it?.let {
                if (!it.loading && it.data?.peekContent()?.list!=null) uiCommunicationListener.showBannerAdOnLoadingFinished(null,true)
            }

            it.message?.let {
                it.getContentIfNotHandled()?.let {

                }
            }
        }

        downloadsPreviewViewModel.downloadsPreviewViewState.observe(viewLifecycleOwner) {
            it.list?.let {
                if (it.isNotEmpty()) showAlternateDeleteOption()
                binding?.progressBar?.visibility = View.GONE
                Logger.log("67564 = "+it)
                adapter.submitList(it)
            }
        }
    }

    private fun showAlternateDeleteOption(){
        if (downloadsPreviewViewModel.isFirstTimeOpened()){
            MaterialDialog(requireContext()).show {
                message(null,getString(R.string.alternate_delete_option))
                positiveButton(null,getString(R.string.ok))
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerView?.adapter=null
        binding = null
    }

    override fun onItemSelected(position: Int, item: DownloadedMediaInfo) {
        val bundle = Bundle().apply {
            putString("offlinePhotoUri", item.uri.toString())
            putString("offlinePhotoColorCode", item.colorCode)
        }
        findNavController().navigate(R.id.action_downloadedMedia_to_singleImagePreview,bundle)
    }

    override fun onItemLongClicked(position: Int, item: DownloadedMediaInfo) {
        val bundle = Bundle().apply {
            putSerializable("model", item)
        }
        findNavController().navigate(R.id.action_downloadedMedia_to_deleteDialog,bundle)
    }
}