package com.app.imagedownloader.framework.presentation.ui.main.singleImagePreview.bottomSheet

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.imagedownloader.R
import com.app.imagedownloader.business.data.cache.model.FavPhotosEntity
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.business.domain.model.PhotoSource
import com.app.imagedownloader.business.interactors.singleImagePreview.ShareMediaOrSetWallpaper
import com.app.imagedownloader.databinding.FragmentMoreOptionsBottomSheetBinding
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MoreOptionsBottomSheet : BottomSheetDialogFragment() {


    @Inject
    lateinit var shareMediaOrSetWallpaper: ShareMediaOrSetWallpaper

    @Inject
    lateinit var photosDao: PhotosDao

    private var binding: FragmentMoreOptionsBottomSheetBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleButtonBackgroundColor()
        handleButtonClick()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMoreOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        try {
            dialog.setOnShowListener {
                val bottomSheet = dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) as? FrameLayout

                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        } catch (_: Exception) {
        }
        return dialog
    }

    private fun handleButtonClick() {
        binding?.let {
            val model: Photo = requireArguments().getSerializable("onlinePreviewModel") as Photo

            it.ShareCard.setOnClickListener {
                shareMediaOrSetWallpaper(model.urls!!.hdUrl, false, requireContext())
                requireActivity().onBackPressed()
            }

            it.AboutUploaderCard.setOnClickListener {
                val intent = Intent().apply {
                    action=Intent.ACTION_VIEW
                    if (model.photoSource is PhotoSource.UnsplashApi) data= Uri.parse("https://unsplash.com/photos/${model.id}")
                    if (model.photoSource is PhotoSource.PexelsApi) data= Uri.parse("https://www.pexels.com/photo/${model.id}/")
                }
                val chooser = Intent.createChooser(intent,"Select Browser")
                requireActivity().startActivity(chooser)
                requireActivity().onBackPressed()
            }
            it.setWallpaperCard.setOnClickListener {
                shareMediaOrSetWallpaper(model.urls!!.hdUrl, true, requireContext())
                requireActivity().onBackPressed()
            }

            it.MoreDetailsCard.setOnClickListener {
                try {
                    val bundle = Bundle().apply {
                        putSerializable("onlinePreviewModel", model)
                        putSerializable("colorCode", model.colorCode)
                    }
                    findNavController().navigate(
                        R.id.action_moreOptionsBottomSheet_to_imageDetailsBottomSheet,
                        bundle
                    )
                }catch (_:Exception){ }
            }

            if (isFav(model)) it.markfavtext.text = getString(R.string.remove_fav)
            else it.markfavtext.text = getString(R.string.add_fav)

            it.MarkFavCard.setOnClickListener {_->
                if (isFav(model)) {
                    lifecycleScope.launch(IO) {
                        photosDao.deleteFavouritePhoto(model.id).let {_->
                            model.isFav=false
                            withContext(Main) {
                                it.markfavtext.text = getString(R.string.add_fav)
                            }
                        }
                    }
                } else {
                    lifecycleScope.launch(IO) {
                        photosDao.insertFavouritePhoto(model.mapToFavPhoto()).let {_->
                            model.isFav=true
                            withContext(Main) {
                                it.markfavtext.text = getString(R.string.remove_fav)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isFav(photoInfo: Photo): Boolean {
        return photoInfo.isFav
    }

    private fun handleButtonBackgroundColor() {
        val color = requireArguments().getInt("colorCode")
        binding?.let {
            it.sharetext.setTextColor(color)
            it.moredetailstext.setTextColor(color)
            it.aboutUploadertext.setTextColor(color)
            it.setWallpapertext.setTextColor(color)
            it.markfavtext.setTextColor(color)
            val colorr = if (isDark(color))
                Color.parseColor("#ffffff") else Color.parseColor("#121212")
            it.shareBtView.setBackgroundColor(colorr)
            it.AboutUploaderCardBtView.setBackgroundColor(colorr)
            it.setWallpaperBtView.setBackgroundColor(colorr)
            it.MoreDetailsCardBtView.setBackgroundColor(colorr)
            it.MarkFavCardBtView.setBackgroundColor(colorr)
        }
    }

    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}