package com.app.imagedownloader.framework.presentation.ui.main.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.VibrateExtension
import com.app.imagedownloader.databinding.FragmentHomeBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.SearchResultPhotosViewModel
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewStateEvents
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var adsManager: GeneralAdsManager

    @Inject
    lateinit var vibrateExtension: VibrateExtension

    @Inject
    lateinit var glideManager: GlideManager

    var previousSelectedChipId = 0
    private var binding: FragmentHomeBinding? = null
    private val searchResultPhotosViewModel by activityViewModels<SearchResultPhotosViewModel>()

    companion object {
        val selectedKeyword: Channel<String> = Channel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        handleChipGroupClick()

        handleSearchBarClick()

        subscribeObservers()

        listenToFinalKeywordChannel()

    }

    private fun listenToFinalKeywordChannel() {
        selectedKeyword.receiveAsFlow().onEach {
            searchResultPhotosViewModel.onEvent(searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.searchPhotos(
                it,
                searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.currentPage
                    ?: 1))
        }.launchIn(lifecycleScope)
    }

    private fun handleChipGroupClick() {
        binding?.let { binding ->
            binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                if (previousSelectedChipId != 0 && previousSelectedChipId != checkedIds.firstOrNull())
                    group.findViewById<Chip>(previousSelectedChipId)?.let {
                        it.chipBackgroundColor =
                            ContextCompat.getColorStateList(requireContext(), R.color.pin_red)
                    }

                checkedIds.firstOrNull()?.let {
                    previousSelectedChipId = it
                    val chip = group.findViewById<Chip>(it)
                    chip.chipBackgroundColor =
                        ContextCompat.getColorStateList(requireContext(), R.color.blue)
                    searchResultPhotosViewModel.onEvent(searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.searchPhotos(
                        chip.text.toString(),
                        searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.currentPage
                            ?: 1))
                }
            }
        }
    }

    private fun handleSearchBarClick() {
        binding?.let { binding ->
            binding.searchbarPlaceholderCard.setOnClickListener {
                transitionToSearchScreen()
            }
        }
    }

    private fun transitionToSearchScreen() {
        vibrateExtension.vibrate()
        findNavController().navigate(R.id.action_home_to_searchPhotos)
    }

    private fun subscribeObservers() {

        searchResultPhotosViewModel.searchResultPhotosPreviewDataState.observe(viewLifecycleOwner) {

            it.loading.let {
                if (it) binding?.progressBar?.visibility = View.VISIBLE
            }

            it.data?.peekContent()?.searchResultPhotos?.let {
                searchResultPhotosViewModel.searchResultPhotosPreviewDataState.removeObservers(
                    viewLifecycleOwner)
                binding?.let {
                    lifecycleScope.launch {
                        adsManager.showNativeHomeScreenAd(it.nativeAdView).let {
                            delay(5000L)
                            binding?.chipGroup?.visibility = View.VISIBLE
                            binding?.progressBar?.visibility = View.GONE
                            try {
                                findNavController().navigate(R.id.action_home_to_searchResultPhotosPreview)
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }

            it.message?.let {
                it.getContentIfNotHandled()?.let {
                    binding?.progressBar?.visibility = View.GONE
                    MaterialDialog(requireContext()).show {
                        message(null, "Error")
                        title(null, "Retry Again")
                        positiveButton(null, "OK")
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