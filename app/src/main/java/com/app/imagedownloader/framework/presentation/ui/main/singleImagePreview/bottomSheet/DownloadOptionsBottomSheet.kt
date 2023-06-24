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
import androidx.lifecycle.lifecycleScope
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.business.interactors.singleImagePreview.SaveMediaInScopedStorage
import com.app.imagedownloader.databinding.FragmentDownloadOptionsBottomSheetBinding
import com.app.imagedownloader.framework.presentation.ui.main.singleImagePreview.SingleImagePreview
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class DownloadOptionsBottomSheet : BottomSheetDialogFragment() {
    @Inject
    lateinit var saveMediaInScopedStorage: SaveMediaInScopedStorage

    private var binding: FragmentDownloadOptionsBottomSheetBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleButtonBackgroundColor()
        handleButtonClick()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDownloadOptionsBottomSheetBinding.inflate(inflater, container, false)
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
            it.fullHdCard.setOnClickListener {
                saveMediaInScopedStorage(url = model.uris.fullHdUrl, requireContext(),model.colorCode) {
                    lifecycleScope.launch {
                        SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                    }
                }
                requireActivity().onBackPressed()
            }
            it.hdCard.setOnClickListener {
                saveMediaInScopedStorage(url = model.uris.fullHdUrl, requireContext(),model.colorCode) {
                    lifecycleScope.launch {
                        SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                    }
                }
                requireActivity().onBackPressed()

            }
            it.NormalCard.setOnClickListener {
                saveMediaInScopedStorage(url = model.uris.fullHdUrl, requireContext(),model.colorCode) {
                    lifecycleScope.launch {
                        SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                    }
                }
                requireActivity().onBackPressed()

            }
            it.mediumCard.setOnClickListener {
                saveMediaInScopedStorage(url = model.uris.fullHdUrl, requireContext(),model.colorCode) {
                    lifecycleScope.launch {
                        SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                    }
                }
                requireActivity().onBackPressed()

            }
            it.thumnailCard.setOnClickListener {
                saveMediaInScopedStorage(url = model.uris.fullHdUrl, requireContext(),model.colorCode) {
                lifecycleScope.launch {
                        SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                    }
                }
                requireActivity().onBackPressed()
            }
        }
    }

    private fun handleButtonBackgroundColor() {
        val color = Color.parseColor(requireArguments().getString("colorCode"))
        binding?.let {
            it.fullhdtext.setTextColor(color)
            it.hdtext.setTextColor(color)
            it.normaltext.setTextColor(color)
            it.mediumtext.setTextColor(color)
            it.fullhdtext.setTextColor(color)
            it.thumbnailtext.setTextColor(color)
            val colorr = if (isDark(color))
                Color.parseColor("#ffffff") else Color.parseColor("#121212")
            it.fullHdCardBtView.setBackgroundColor(colorr)
            it.hdCardBtView.setBackgroundColor(colorr)
            it.mediumBtView.setBackgroundColor(colorr)
            it.NormalCardBtView.setBackgroundColor(colorr)
            it.thumbnailBtView.setBackgroundColor(colorr)
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