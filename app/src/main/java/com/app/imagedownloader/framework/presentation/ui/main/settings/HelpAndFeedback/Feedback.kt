package com.app.imagedownloader.framework.presentation.ui.main.Settings.HelpAndFeedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.imagedownloader.R
import com.app.imagedownloader.databinding.FragmentFeedbackBinding
import com.app.imagedownloader.framework.Utils.sendEmailIntent


class Feedback : Fragment(R.layout.fragment_feedback) {

    var binding: FragmentFeedbackBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedbackBinding.bind(view)

        var category: String? = null
        binding?.let { binding ->
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.langarray_item,
                resources.getStringArray(R.array.feedbackcategory)
            )
            binding.autotextview.setAdapter(arrayAdapter)
            val k = AdapterView.OnItemClickListener { _, _, position, _ ->
                category = requireContext().resources.getStringArray(
                    R.array.feedbackcategory
                )[position]
            }
            binding.autotextview.onItemClickListener = k
            binding.slsdcard.setOnClickListener {
                category?.let {
                    val content = binding.feedbackcontent.text.toString()
                    if (content.isNotEmpty()) {
                        sendEmailIntent()
                            .sendIntent("instastorytale@gmail.com", it, content, requireContext())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.enterfeedback),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } ?: kotlin.run {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.select_category),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
