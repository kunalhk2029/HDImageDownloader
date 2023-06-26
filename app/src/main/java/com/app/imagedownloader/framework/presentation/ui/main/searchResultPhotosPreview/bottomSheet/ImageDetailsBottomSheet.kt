package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.bottomSheet

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
import com.app.imagedownloader.databinding.FragmentImageDetailsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ImageDetailsBottomSheet : BottomSheetDialogFragment() {

    private var binding: FragmentImageDetailsBottomSheetBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleButtonBackgroundColor()
        setImageDetails()
    }

    private fun setImageDetails() {
        val model: UnsplashPhotoInfo.photoInfo =
            requireArguments().getSerializable("onlinePreviewModel") as UnsplashPhotoInfo.photoInfo
        binding?.let {
            val size = "Size : ${model.width}x${model.height}"
            val orientation = "Orientation : "+if (model.isPotrait) "Portrait" else "Landscape"
            var finalTag="Tags : "
            var tags = ""
            model.tags_preview?.let {list->
                list.forEach { tag ->
                    tags += ",$tag"
                }
                finalTag+=tags.substring(1)
            }?: kotlin.run {
            it.TagsCard.visibility = View.GONE
        }

        it.sizetext.text = size
        it.orientationtext.text = orientation
        it.tagstext.text = finalTag
    }
}

override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?,
): View? {
    binding = FragmentImageDetailsBottomSheetBinding.inflate(inflater, container, false)
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


private fun handleButtonBackgroundColor() {
    val color = Color.parseColor(requireArguments().getString("colorCode"))
    binding?.let {
        it.sizetext.setTextColor(color)
        it.aboutUploadertext.setTextColor(color)
        it.orientationtext.setTextColor(color)
        it.tagstext.setTextColor(color)
        val colorr = if (isDark(color))
            Color.parseColor("#ffffff") else Color.parseColor("#121212")
        it.SizeCardBtView.setBackgroundColor(colorr)
        it.AboutUploaderCardBtView.setBackgroundColor(colorr)
        it.TagsBtView.setBackgroundColor(colorr)
        it.OrientationBtView.setBackgroundColor(colorr)
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