package com.app.imagedownloader.framework.presentation.ui.main.singleImagePreview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.presentation.ui.main.SystemUiVisibility.changeStatusAndNavigationBarColor
import com.app.imagedownloader.framework.presentation.ui.main.SystemUiVisibility.setDefaultBarColor
import com.app.imagedownloader.Utils.VibrateExtension
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.databinding.FragmentSingleImagePreviewBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.app.imagedownloader.framework.presentation.ui.main.SystemUiVisibility.handleAdsOnBackPressed
import com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.DownloadsPreview
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SingleImagePreview : Fragment(R.layout.fragment_single_image_preview) {

    @Inject
    lateinit var glideManager: GlideManager

    @Inject
    lateinit var vibrate: VibrateExtension

    @Inject
    lateinit var generalAdsManager: GeneralAdsManager

    var model: UnsplashPhotoInfo.photoInfo? = null
    var offlinePhotoUri: String? = null
    var offlinePhotoColorCode: String? = null
    var binding: FragmentSingleImagePreviewBinding? = null

    companion object {
        val downloadCompletedPlaybackListener: Channel<Boolean> = Channel()
    }

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
            uiCommunicationListener.hideToolbar()
        } catch (_: java.lang.ClassCastException) {
        }
    }

    override fun onResume() {
        super.onResume()
        model?.let {
            changeStatusAndNavigationBarColor(Color.parseColor(it.colorCode))
        }
        uiCommunicationListener.hideToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSingleImagePreviewBinding.bind(view)

        val backbtcallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (handleAdsOnBackPressed()) return
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backbtcallback)

        if (Build.VERSION.SDK_INT <= 32) {
            model =
                requireArguments().getSerializable("onlinePreviewModel")?.let {
                    it as UnsplashPhotoInfo.photoInfo
                }
        } else {
            model = requireArguments().getSerializable(
                "onlinePreviewModel",
                UnsplashPhotoInfo.photoInfo::class.java
            )
        }

        if (model != null) showOnlineImageUi() else {
            offlinePhotoUri = requireArguments().getString("offlinePhotoUri")
            offlinePhotoColorCode = requireArguments().getString("offlinePhotoColorCode")
            showOfflineImageUi()
        }
        loadImage()

        listenDownloadViewClick()
    }

    private fun listenDownloadViewClick() {
        downloadCompletedPlaybackListener.receiveAsFlow().onEach {
            findNavController().navigate(R.id.action_singleImagePreview_to_downloadedMedia2)
        }.launchIn(lifecycleScope)
    }

    private fun showMoreOptionsDialog() {
        val bundle = Bundle().apply {
            putSerializable("onlinePreviewModel", model)
            putString("colorCode", model!!.colorCode)
        }
        findNavController().navigate(
            R.id.action_singleImagePreview_to_moreOptionsBottomSheet,
            bundle
        )
    }

    private fun showDownloadOptionsDialog() {
        val bundle = Bundle().apply {
            putSerializable("onlinePreviewModel", model)
            putString("colorCode", model!!.colorCode)
        }
        findNavController().navigate(
            R.id.action_singleImagePreview_to_downloadOptionsBottomSheet,
            bundle
        )
    }

    private fun handleOfflineImageUiClick() {
        binding?.let {
            it.delete.setOnClickListener {
                handleDeletion()
            }
            it.share.setOnClickListener {
                handleShare()
            }
            it.viewingallery.setOnClickListener {
                handleViewImageInGallery()
            }
        }
    }

    private fun handleShare() {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse(offlinePhotoUri)
            )
        }
        val chooser = Intent.createChooser(intent, "Share")
        requireContext().startActivity(chooser)
    }

    private fun handleDeletion() {
        MaterialDialog(requireContext()).show {
            title(null, getString(R.string.areyousure))
            message(null, "Permanently delete from device ?")
            positiveButton(null, "Delete") {
                vibrate.vibrate()
                lifecycleScope.launch {
                    binding?.progressBar?.visibility=View.VISIBLE
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            withContext(Dispatchers.IO) {
                                requireContext().contentResolver.delete(
                                    Uri.parse(offlinePhotoUri),
                                    null,
                                    null
                                )
                            }

                        } else {
                            withContext(Dispatchers.IO) {
                                val file =
                                    File(Uri.parse(offlinePhotoUri).path!!)
                                file.delete()
                            }
                        }
                        Toast.makeText(
                            requireContext(),
                            "Photo Deleted!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        DownloadsPreview.photoDeletionListener.send(true)
                        requireActivity().onBackPressed()
                    } catch (_: Exception) {
                    }
                }
            }
            negativeButton(null, getString(R.string.Cancel)) {
                vibrate.vibrate()
            }
        }
    }

    private fun handleViewImageInGallery() {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(
                Uri.parse(offlinePhotoUri),
                "image/*"
            )
        }
        val chooser = Intent.createChooser(intent, "View In Gallery")
        requireContext().startActivity(chooser)
    }

    private fun handleOnlineImageUiClick() {
        binding?.let {
            it.downloadCard.setOnClickListener {
                showDownloadOptionsDialog()
            }
            it.MoreActionsCard.setOnClickListener {
                showMoreOptionsDialog()
            }
        }
    }

    private fun loadImage() {
        generalAdsManager.handleNativeFull(
            { binding?.let {
                    it.progressBar.visibility = View.VISIBLE
                    glideManager.setImageFromUrl(
                        it.imagePreview,
                        model?.uris?.regularUrl ?: offlinePhotoUri,
                        glideSuccessUnit = {
                            kotlin.run { it.progressBar.visibility = View.GONE }
                        }
                    )
                }
            },
            requireActivity() as MainActivity, 0, false
        )
    }

    private fun showOfflineImageUi() {
        handleOfflineImageUiClick()
        binding?.let {
            val color = Color.parseColor(offlinePhotoColorCode)
            it.offlineImageActionsView.visibility = View.VISIBLE
            changeStatusAndNavigationBarColor(color)
            it.viewingalleryimageview.drawable.overrideColor(color)
            it.shareimageview.drawable.overrideColor(color)
            it.deleteimageview.drawable.overrideColor(color)
            it.exosharetext.setTextColor(color)
            it.exoviewingalltext.setTextColor(color)
            it.exodeletetext.setTextColor(color)
        }
    }

    private fun showOnlineImageUi() {
        handleOnlineImageUiClick()
        binding?.let {
            it.onlinePreviewActionsView.visibility = View.VISIBLE
            it.downloadBtView.setBackgroundColor(Color.parseColor(model!!.colorCode))
            it.MoreActionsBtView.setBackgroundColor(Color.parseColor(model!!.colorCode))
            it.descriptionView.setBackgroundColor(Color.parseColor(model!!.colorCode))
            it.yg.setBackgroundColor(Color.parseColor(model!!.colorCode))
            changeStatusAndNavigationBarColor(Color.parseColor(model!!.colorCode))
            ContextCompat.getDrawable(requireContext(), R.drawable.bottom_round_corner)
                ?.overrideColor(Color.parseColor(model!!.colorCode))
            if (!isDark(Color.parseColor(model!!.colorCode))) it.downloadtextview.setTextColor(Color.parseColor(
                "#111111"))
            if (!isDark(Color.parseColor(model!!.colorCode))) it.moreActionstextview.setTextColor(
                Color.parseColor("#111111"))
            if (!isDark(Color.parseColor(model!!.colorCode))) it.descriptiontextview.setTextColor(
                Color.parseColor("#111111"))
            model!!.description?.let {
                binding?.descriptiontextview?.text = it
                binding?.descriptionCard?.visibility = View.VISIBLE
            }
        }
    }

    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        uiCommunicationListener.showToolbar()
        setDefaultBarColor()
    }

    fun Drawable.overrideColor(@ColorInt colorInt: Int) {
        when (this) {
            is GradientDrawable -> setColor(colorInt)
            is ShapeDrawable -> {
                paint.color = colorInt
            }
            is ColorDrawable -> color = colorInt

            is VectorDrawable -> {
                colorFilter = PorterDuffColorFilter(
                    colorInt, PorterDuff.Mode.MULTIPLY)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setDefaultBarColor()
    }
}