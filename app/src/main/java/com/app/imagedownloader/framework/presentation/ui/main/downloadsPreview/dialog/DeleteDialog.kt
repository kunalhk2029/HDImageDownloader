package com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.app.imagedownloader.business.domain.model.DownloadedMediaInfo
import com.app.imagedownloader.databinding.FragmentDeleteDialogBinding
import com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.DownloadsPreview.Companion.photoDeletionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class DeleteDialog : BottomSheetDialogFragment() {

    private var binding: FragmentDeleteDialogBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleButtonClick()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDeleteDialogBinding.inflate(inflater, container, false)
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
            val model: DownloadedMediaInfo =
                requireArguments().getSerializable("model") as DownloadedMediaInfo

            it.deletePhotoCard.setOnClickListener {
                CoroutineScope(IO).launch {
                    withContext(Main) {
                        it.visibility = View.GONE
                        binding?.progressbar?.visibility = View.VISIBLE
                    }
                    requireContext().contentResolver.delete(
                        model.uri,
                        null,
                        null
                    )
                    launch {
                        photoDeletionListener.send(true)
                    }
                    withContext(Main) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}