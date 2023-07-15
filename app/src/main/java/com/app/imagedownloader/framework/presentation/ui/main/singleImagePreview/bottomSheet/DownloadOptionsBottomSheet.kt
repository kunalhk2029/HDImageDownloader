package com.app.imagedownloader.framework.presentation.ui.main.singleImagePreview.bottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.app.imagedownloader.R
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.business.interactors.singleImagePreview.SaveMediaInScopedStorage
import com.app.imagedownloader.databinding.FragmentDownloadOptionsBottomSheetBinding
import com.app.imagedownloader.framework.presentation.ui.main.singleImagePreview.SingleImagePreview
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
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
    ): View {
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


    private suspend fun getDownloadSizeMap(): HashMap<String, String> {
        return withContext(IO) {

            val map = HashMap<String, String>()

            val model =
                requireArguments().getSerializable("onlinePreviewModel") as Photo

            val fullHdUrlSizeJob = launch {
                val fullHdUrlSize = mediaSize(URL(model.urls.fullHdUrl))
                map["fullHdUrl"] = fullHdUrlSize
            }
            val hdUrlSizeJob = launch {
                val hdUrlSize = mediaSize(URL(model.urls.hdUrl))
                map["hdUrl"] = hdUrlSize
            }
            val regularUrlSizeJob = launch {
                val regularUrlSize = mediaSize(URL(model.urls.regularUrl))
                map["regularUrl"] = regularUrlSize
            }
            val smallUrlSizeJob = launch {
                val smallUrlSize = mediaSize(URL(model.urls.smallUrl))
                map["smallUrl"] = smallUrlSize
            }
            val thumbnailUrlSizeJob = launch {
                val thumbnailUrlSize = mediaSize(URL(model.urls.thumbnailUrl))
                map["thumbnailUrl"] = thumbnailUrlSize
            }

            while (
                !fullHdUrlSizeJob.isCompleted
                || !hdUrlSizeJob.isCompleted
                || !regularUrlSizeJob.isCompleted
                || !smallUrlSizeJob.isCompleted
                || !thumbnailUrlSizeJob.isCompleted
            ) {

            }

            map
        }
    }

    private fun mediaSize(url: URL): String {
        val connection = url.openConnection() as HttpURLConnection
        val size = try {
            connection.contentLength
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }

        return if (size != null) {
            val sizeInKb = size / 1024
            val sizeInMb = sizeInKb / 1024
            return if (sizeInMb > 0) " : ${sizeInMb}MB" else " : ${sizeInKb}kB"
        } else {
            ""
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleButtonClick() {
        lifecycleScope.launch {
            getDownloadSizeMap().let { sizeMap ->
                binding?.let {
                    it.progressbar.visibility = View.GONE
                    it.downloadOptions.visibility = View.VISIBLE
                    val model: Photo =
                        requireArguments().getSerializable("onlinePreviewModel") as Photo
                    it.fullhdtext.text = it.fullhdtext.text.toString() + sizeMap["fullHdUrl"]
                    it.hdtext.text = it.hdtext.text.toString() + sizeMap["hdUrl"]
                    it.normaltext.text = it.normaltext.text.toString() + sizeMap["regularUrl"]
                    it.mediumtext.text = it.mediumtext.text.toString() + sizeMap["smallUrl"]
                    it.thumbnailtext.text =
                        it.thumbnailtext.text.toString() + sizeMap["thumbnailUrl"]

                    it.fullHdCard.setOnClickListener {
                        saveMediaInScopedStorage(url = model.urls.fullHdUrl,
                            requireContext(),
                            model.colorCode) {
                            lifecycleScope.launch {
                                SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                            }
                        }
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    it.hdCard.setOnClickListener {
                        saveMediaInScopedStorage(url = model.urls.hdUrl,
                            requireContext(),
                            model.colorCode) {
                            lifecycleScope.launch {
                                SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                            }
                        }
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    it.NormalCard.setOnClickListener {
                        saveMediaInScopedStorage(url = model.urls.regularUrl,
                            requireContext(),
                            model.colorCode) {
                            lifecycleScope.launch {
                                SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                            }
                        }
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    it.mediumCard.setOnClickListener {
                        saveMediaInScopedStorage(url = model.urls.smallUrl,
                            requireContext(),
                            model.colorCode) {
                            lifecycleScope.launch {
                                SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                            }
                        }
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    it.thumnailCard.setOnClickListener {
                        saveMediaInScopedStorage(url = model.urls.thumbnailUrl,
                            requireContext(),
                            model.colorCode) {
                            lifecycleScope.launch {
                                SingleImagePreview.downloadCompletedPlaybackListener.send(true)
                            }
                        }
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    it.customSizeCard.setOnClickListener {
                        MaterialDialog(requireContext()).show {
                            title(null, getString(R.string.enterheight))
                            var height = 0
                            input(
                                inputType = InputType.TYPE_CLASS_NUMBER,
                                waitForPositiveButton = false
                            ) { materialDialog, charSequence ->
                                val inputFeild = materialDialog.getInputField()
                                var isValid = false
                                try {
                                    height = charSequence.toString().toInt()
                                    isValid = height > 10
                                } catch (e: Exception) {
                                }
                                inputFeild.error =
                                    if (isValid) null else getString(R.string.heighterror,
                                        model.height.toString())
                                materialDialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                            }
                            positiveButton(null, getString(R.string.ok)) {
                                val url = "${model.urls.fullHdUrl}&h=$height"
                                saveMediaInScopedStorage(url = url,
                                    requireContext(),
                                    model.colorCode) {
                                    lifecycleScope.launch {
                                        SingleImagePreview.downloadCompletedPlaybackListener.send(
                                            true)
                                    }
                                }
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleButtonBackgroundColor() {
        val color = requireArguments().getInt("colorCode")
        binding?.let {
            it.customSizetext.setTextColor(color)
            it.fullhdtext.setTextColor(color)
            it.hdtext.setTextColor(color)
            it.normaltext.setTextColor(color)
            it.mediumtext.setTextColor(color)
            it.fullhdtext.setTextColor(color)
            it.thumbnailtext.setTextColor(color)
            val colorr = if (isDark(color))
                Color.parseColor("#ffffff") else Color.parseColor("#121212")
            it.customSizeCardBtView.setBackgroundColor(colorr)
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