package com.app.imagedownloader.framework.presentation.ui.main.home

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.VibrateExtension
import com.app.imagedownloader.business.data.cache.model.RecentSearch
import com.app.imagedownloader.databinding.FragmentHomeBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.dataSource.cache.PhotosDao
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.SearchResultPhotosViewModel
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewStateEvents
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger
import javax.inject.Inject


@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var adsManager: GeneralAdsManager

    @Inject
    lateinit var vibrateExtension: VibrateExtension

    @Inject
    lateinit var glideManager: GlideManager

    @Inject
    lateinit var photosDao: PhotosDao

    var previousSelectedChipId = 0
    var previousSelectedRecentChipId = 0
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

        handleReecentSearchChipViewPopulation()

        handleRecentSearchChipGroupClick()

        subscribeObservers()

        listenToFinalKeywordChannel()

    }

    private fun handleReecentSearchChipViewPopulation() {
        val chipGroup =  binding?.recentSearchChipGroup
        chipGroup?.removeAllViews()
        lifecycleScope.launch {
            searchResultPhotosViewModel.getRecentSearches().let {
                if (it.isNotEmpty()) binding?.recentSearchChipView?.visibility=View.VISIBLE
                if (it.isNotEmpty()) binding?.recentSearches?.visibility=View.VISIBLE
                val templateRecentSearchChip =  binding?.templateRecentSearchChip
                it.forEach {
                val chip = Chip(requireContext())
                    chip.layoutParams = templateRecentSearchChip?.layoutParams
                    chip.isCheckable=true
                    chip.isCheckedIconVisible=false
                    chip.closeIcon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_delete_24)
                    chip.isCloseIconVisible=true
                     chip.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                    chip.closeIconTint=
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white))
                    chip.setOnCloseIconClickListener {
                        MaterialDialog(requireContext()).show {
                            title(null,getString(R.string.delete_recent_search))
                            message(null,"${chip.text} will be deleted from search history")
                            positiveButton(null,getString(R.string.ok)) {
                                lifecycleScope.launch(IO){
                                    photosDao.deleteRecentSearchQuery(chip.text.toString()).let {
                                        withContext(Main){
                                            chipGroup?.removeView(chip)
                                        }
                                    }
                                }
                            }
                            negativeButton(null,getString(R.string.Cancel))
                        }
                    }
                    chip.text = it.query
                    chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.pin_red))
                    chipGroup?.addView(chip)
                }
            }
        }
    }

    private fun listenToFinalKeywordChannel() {
        selectedKeyword.receiveAsFlow().onEach {
            withContext(IO){
                photosDao.insertRecentSearchQuery(RecentSearch(it))
            }
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

    private fun handleRecentSearchChipGroupClick() {
        binding?.let { binding ->
            binding.recentSearchChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                if (previousSelectedRecentChipId != 0 && previousSelectedRecentChipId != checkedIds.firstOrNull())
                    group.findViewById<Chip>(previousSelectedRecentChipId)?.let {
                        it.chipBackgroundColor =
                            ContextCompat.getColorStateList(requireContext(), R.color.pin_red)
                    }

                checkedIds.firstOrNull()?.let {
                    previousSelectedRecentChipId = it
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
                binding?.let {binding->
                    lifecycleScope.launch {
                        adsManager.showNativeHomeScreenAd(binding.nativeAdView).let {
                            delay(250L)
                            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                            delay(3500L)
                            binding.chipGroup.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
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