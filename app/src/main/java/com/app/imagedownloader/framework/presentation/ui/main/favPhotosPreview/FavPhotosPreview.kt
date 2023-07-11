package com.app.imagedownloader.framework.presentation.ui.main.favPhotosPreview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.imagedownloader.R
import com.app.imagedownloader.business.domain.model.FavPhotos
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.databinding.FragmentFavPhotosPreviewBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.Utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavPhotosPreview : Fragment(R.layout.fragment_fav_photos_preview),
    FavPhotosPreviewAdapter.Interaction {


    @Inject
    lateinit var generalAdsManager: GeneralAdsManager

    @Inject
    lateinit var glideManager: GlideManager

    var binding: FragmentFavPhotosPreviewBinding? = null
    lateinit var adapter: FavPhotosPreviewAdapter

    private val viewModel by viewModels<FavPhotosPreviewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavPhotosPreviewBinding.bind(view)

        initFavPhotosAdapter()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.favPhotosPreviewDataState.observe(viewLifecycleOwner) {
            it.data?.getContentIfNotHandled()?.list?.let {
                viewModel.setFavPhotosList(it)
            }

            it?.let {
                if (it.loading) binding?.progressbar?.visibility = View.VISIBLE

//                if (!it.loading && it.data?.peekContent()?.list!=null)
//                    uiCommunicationListener.showBannerAdOnLoadingFinished(null,true)
            }

            it.message?.let {
                it.getContentIfNotHandled()?.let {

                }
            }
        }

        viewModel.favPhotosPreviewViewState.observe(viewLifecycleOwner) {
            it.list?.let {
                binding?.progressbar?.visibility = View.GONE
                Logger.log("67564 = " + it)
                adapter.submitList(it)
            }
        }
    }

    private fun initFavPhotosAdapter() {
        adapter = FavPhotosPreviewAdapter(this,
            glideManager = glideManager,
            generalAdsManager = generalAdsManager)
        binding?.recyclerView?.let {
            it.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            it.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemSelected(position: Int, item: FavPhotos) {
        val bundle = Bundle().apply {
            putSerializable("onlinePreviewModel", item.mapToPhoto())
        }
        findNavController().navigate(R.id.action_favPhotosPreview_to_singleImagePreview,
            bundle)
    }
}