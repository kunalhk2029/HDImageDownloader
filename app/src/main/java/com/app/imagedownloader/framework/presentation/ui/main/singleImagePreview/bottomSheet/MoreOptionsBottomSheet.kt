package com.app.imagedownloader.framework.presentation.ui.main.singleImagePreview.bottomSheet

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.business.interactors.singleImagePreview.ShareMediaOrSetWallpaper
import com.app.imagedownloader.databinding.FragmentMoreOptionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MoreOptionsBottomSheet : BottomSheetDialogFragment() {


    @Inject
    lateinit var shareMediaOrSetWallpaper: ShareMediaOrSetWallpaper

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
            val model: UnsplashPhotoInfo.photoInfo =
                requireArguments().getSerializable("onlinePreviewModel") as UnsplashPhotoInfo.photoInfo
            model.uploaderInfo?.let { _->
                it.AboutUploaderCard.visibility=View.VISIBLE
            }
            it.ShareCard.setOnClickListener {
                shareMediaOrSetWallpaper(model.uris.regularUrl, false, requireContext())
                requireActivity().onBackPressed()
            }
            it.AboutUploaderCard.setOnClickListener {
                requireActivity().onBackPressed()
            }
            it.setWallpaperCard.setOnClickListener {
                shareMediaOrSetWallpaper(model.uris.regularUrl, true, requireContext())
                requireActivity().onBackPressed()
            }
        }
    }

    private fun handleButtonBackgroundColor() {
        val color = Color.parseColor(requireArguments().getString("colorCode"))
        binding?.let {
            it.sharetext.setTextColor(color)
            it.aboutUploadertext.setTextColor(color)
            it.setWallpapertext.setTextColor(color)
            val colorr = if (isDark(color))
                Color.parseColor("#ffffff") else Color.parseColor("#121212")
            it.shareBtView.setBackgroundColor(colorr)
            it.AboutUploaderCardBtView.setBackgroundColor(colorr)
            it.setWallpaperBtView.setBackgroundColor(colorr)
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