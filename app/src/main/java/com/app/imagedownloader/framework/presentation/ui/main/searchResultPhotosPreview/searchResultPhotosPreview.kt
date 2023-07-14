package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.allViews
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.VibrateExtension
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.model.Photo
import com.app.imagedownloader.databinding.FragmentSearchResultPhotosPreviewBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewStateEvents
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class searchResultPhotosPreview : Fragment(R.layout.fragment_search_result_photos_preview),
    SearchResultPhotosPreviewAdapter.Interaction {

    @Inject
    lateinit var glideManager: GlideManager

    @Inject
    lateinit var generalAdsManager: GeneralAdsManager

    @Inject
    lateinit var vibrateExtension: VibrateExtension

    private val searchResultPhotosViewModel by activityViewModels<SearchResultPhotosViewModel>()

    lateinit var adapter: SearchResultPhotosPreviewAdapter
    var errorDialog: MaterialDialog? = null
    var binding: FragmentSearchResultPhotosPreviewBinding? = null

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (_: java.lang.ClassCastException) {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchResultPhotosPreviewBinding.bind(view)
        setHasOptionsMenu(true)
        initAdapter()
        initRecyclerView()
        subscribeObservers()
        addOnBackpressedCallback()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_result_preview_menu, menu)
        if (getAppTheme() == 1) {
            val filter = menu.findItem(R.id.showFilterDialog)
            filter.icon = (ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_filter_alt_24white))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.showFilterDialog) showFilterDialog()
        return true
    }

    private fun showFilterDialog() {
        val filterDialog = MaterialDialog(requireContext()).show {
            setContentView(R.layout.filter_dialog)
        }

        val dialogCreationTime = System.currentTimeMillis()
        val previousSortFilter =
            searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.sortFilter
        val previousOrientationFilter =
            searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.orientationFilterWithTagOrColorCombination
                ?: searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.orientationFilter
        val previousTagsFilter =
            searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.tagFilter
        val previousColorsFilter =
            searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.colorsFilter

        val sortBySpinner = filterDialog.findViewById<Spinner>(R.id.sortBySpinner)
        val sortByadapter = ArrayAdapter(requireContext(), R.layout.spinner_item, listOf(
            SortByFilter.Relevance.uiValue,
            SortByFilter.Likes.uiValue,
            SortByFilter.UploadDate.uiValue,
        ))
        sortBySpinner.adapter = sortByadapter

        val selectedSortSpinnerPosistion =
            if (searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.sortFilter is SortByFilter.Likes) 1
            else if (searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.sortFilter is SortByFilter.UploadDate) 2
            else 0

        sortBySpinner.setSelection(selectedSortSpinnerPosistion)
        sortBySpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (System.currentTimeMillis() >= dialogCreationTime + 1000L) vibrateExtension.vibrate()
                searchResultPhotosViewModel.onEvent(
                    searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.updateSortByFilter(
                        sortByFilter = when (p2) {
                            0 -> {
                                SortByFilter.Relevance
                            }
                            1 -> {
                                SortByFilter.Likes
                            }
                            2 -> {
                                SortByFilter.UploadDate
                            }
                            else -> SortByFilter.Relevance
                        }
                    )
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val orientationSpinner = filterDialog.findViewById<Spinner>(R.id.orientationSpinner)
        val orientationSpinneradapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, listOf(
                OrientationFilter.All.uiValue,
                OrientationFilter.Potrait.uiValue,
                OrientationFilter.Landscape.uiValue,
                OrientationFilter.Square.uiValue
            ))
        orientationSpinner.adapter = orientationSpinneradapter

        val selectedOrientatationSpinnerPosistion =
            if (previousOrientationFilter is OrientationFilter.Potrait) 1
            else if (previousOrientationFilter is OrientationFilter.Landscape) 2
            else if (previousOrientationFilter is OrientationFilter.Square) 3
            else 0

        var newOrientationFilter: OrientationFilter? = null
        orientationSpinner.setSelection(selectedOrientatationSpinnerPosistion)
        orientationSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (System.currentTimeMillis() >= dialogCreationTime + 1000L) vibrateExtension.vibrate()
                newOrientationFilter = when (p2) {
                    0 -> {
                        OrientationFilter.All
                    }
                    1 -> {
                        OrientationFilter.Potrait
                    }
                    2 -> {
                        OrientationFilter.Landscape
                    }
                    3 -> {
                        OrientationFilter.Square
                    }
                    else -> OrientationFilter.All
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val tagSpinner = filterDialog.findViewById<Spinner>(R.id.tagSpinner)
        val tagChipGroup = filterDialog.findViewById<ChipGroup>(R.id.tagChipGroup)
        val tagSpinneradapter = ArrayAdapter(requireContext(), R.layout.spinner_item,
            searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.distinctTagsList
                ?: listOf())
        tagSpinner.adapter = tagSpinneradapter


        addTagFilterChips(tagChipGroup, false, null)

        handleFilterDialogTagChipGroupClick(tagChipGroup)

        tagSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.let { state ->
                    if (p2 != 0 && state.tagFilter.contains(state.distinctTagsList[p2])) return
                    if (System.currentTimeMillis() >= dialogCreationTime + 1000L) vibrateExtension.vibrate()
                    val updatedTagList = if (p2 == 0) state.tagFilter else {
                        val list = state.tagFilter + listOf(state.distinctTagsList[p2])
                        list.distinct()
                    }
                    searchResultPhotosViewModel.onEvent(
                        searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.updateTagsFilter(
                            tagsList = updatedTagList)
                    )
                    if (p2 != 0) addTagFilterChips(tagChipGroup, true, state.distinctTagsList[p2])
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }


        initFilterDialpgColorChip(filterDialog)
        filterDialog.findViewById<Button>(R.id.clear_filter).setOnClickListener {
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateSortByFilter(
                SortByFilter.Relevance))
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateOrientationFilter(
                OrientationFilter.All))
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateColorsFilter(
                listOf()))
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateTagsFilter(
                listOf()))

            filterDialog.dismiss()
            vibrateExtension.vibrate()
            searchResultPhotosViewModel.onEvent(searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.FilterPhotos)
        }

        filterDialog.findViewById<Button>(R.id.cancel_filter).setOnClickListener {
            previousSortFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateSortByFilter(
                    it))
            }

            previousOrientationFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateOrientationFilter(
                    it))
            }
            previousTagsFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateTagsFilter(
                    it))
            }

            previousColorsFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateColorsFilter(
                    it))
            }
            filterDialog.dismiss()
            vibrateExtension.vibrate()
        }

        filterDialog.findViewById<Button>(R.id.apply_filter).setOnClickListener {
            filterDialog.dismiss()
            vibrateExtension.vibrate()
            if (newOrientationFilter != null) {
                searchResultPhotosViewModel.onEvent(searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.updateOrientationFilter(
                    newOrientationFilter!!))
            }
            searchResultPhotosViewModel.onEvent(searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.FilterPhotos)
        }
    }

    private fun initFilterDialpgColorChip(
        filterDialog: MaterialDialog,
    ) {
        lifecycleScope.launch {
            delay(500L)
            val progressbar = filterDialog.findViewById<ProgressBar>(R.id.progressbar)
            val filterColorChipGroup = filterDialog.findViewById<ChipGroup>(R.id.chipGroup)
            val filtertemplatechip = filterDialog.findViewById<Chip>(R.id.templateChip)
            val filtertemplatechip2 = filterDialog.findViewById<Chip>(R.id.templateChip2)
            filterColorChipGroup.visibility = View.GONE

            val colorList =
                searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.distinctColorsList

            colorList?.forEachIndexed { index, s ->
                if (s != 0) {
                    val chip = Chip(requireContext())
                    chip.id = index
                    chip.layoutParams = if (index % 2 == 0) filtertemplatechip.layoutParams else
                        filtertemplatechip2.layoutParams
                    chip.isCheckable = true
                    chip.checkedIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.chip_check)
                    chip.checkedIconTint = if (isDark(s))
                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                    else
                        ColorStateList.valueOf(Color.parseColor("#121212"))
                    chip.chipBackgroundColor =
                        ColorStateList.valueOf(s)
                    if (searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.colorsFilter?.contains(
                            s) == true
                    ) {
                        chip.isChecked = true
                    }
                    filterColorChipGroup.addView(chip)
                }
            }

            filterColorChipGroup.visibility = View.VISIBLE
            progressbar.visibility = View.GONE
            handleFilterDialogColorChipGroupClick(filterColorChipGroup)
        }
    }

    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }

    private fun addTagFilterChips(
        tagChipGroup: ChipGroup,
        singleMode: Boolean,
        singleChipText: String?,
    ) {
        if (singleMode) {
            val chip = Chip(requireContext())
            chip.isCheckable = true
            chip.isChecked = true
            chip.checkedIcon = ContextCompat.getDrawable(requireContext(), R.drawable.close)
            chip.checkedIconTint = ColorStateList.valueOf(Color.parseColor("#ffffff"))
            chip.text = singleChipText
            tagChipGroup.addView(chip)
            return
        }
        searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.tagFilter?.forEachIndexed { index, s ->
            val chip = Chip(requireContext())
            chip.isCheckable = true
            chip.isChecked = true
            chip.checkedIcon = ContextCompat.getDrawable(requireContext(), R.drawable.close)
            chip.checkedIconTint = ColorStateList.valueOf(Color.parseColor("#ffffff"))
            chip.text = s
            tagChipGroup.addView(chip)
        }
    }

    private fun addOnBackpressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                searchResultPhotosViewModel.resetPagination()
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            onBackPressedCallback)
    }

    private fun subscribeObservers() {
        searchResultPhotosViewModel.searchResultPhotosPreviewDataState.observe(viewLifecycleOwner) { dataState ->
            val it = dataState
            it.loading.let {
                if (!it) hidePaginationProgressbar()
                else showPaginationProgressbar()

            }
            it.data?.getContentIfNotHandled()?.let {
                if (!dataState.loading) {
                    searchResultPhotosViewModel.PAGINATION_EXECUTING = false
                }

                it.searchResultPhotos?.let {
                    searchResultPhotosViewModel.setSearchResultPhotosList(it)
                }

                it.apiSourcesInfo.let {
                    searchResultPhotosViewModel.setApiSourcesInfo(it)
                }
            }

            it.message?.let {
                it.getContentIfNotHandled()?.let {
                    searchResultPhotosViewModel.PAGINATION_EXECUTING = false
                    errorDialog?.hide()
                    errorDialog = MaterialDialog(requireContext()).show {
                        message(null, it)
                        title(null, "Retry Again")
                        positiveButton(null, "OK")
                    }
                }
            }
        }

        searchResultPhotosViewModel.searchResultPhotosPreviewViewState.observe(viewLifecycleOwner) {

            it.searchedKeyword?.let {
                uiCommunicationListener.setToolbarTitleText(it)
            }
            val isTagOrColorFilterApplied = it.colorsFilter.isNotEmpty() ||
                    it.tagFilter.isNotEmpty()

            if (isTagOrColorFilterApplied) binding?.clearFilterMessage?.visibility = View.VISIBLE
            else binding?.clearFilterMessage?.visibility = View.GONE

            it.apiSourcesInfo.let {
                it.unsplashTotalPhotos?.let {
                    Logger.log("68989898 view unsplashTotalPhotos = " + it)
                }
                it.unsplashPages?.let {
                    Logger.log("68989898 view unsplashPages = " + it)
                }
                it.pexelsTotalPhotos?.let {
                    Logger.log("68989898 view pexelsTotalPhotos = " + it)
                }
                it.pexelsPages?.let {
                    Logger.log("68989898 view pexelsPages = " + it)
                }
                it.pinterestNextQueryBookMark?.let {
                    Logger.log("68989898 view pinterestNextQueryBookMark = " + it)
                }
            }

            it.filteredSearchResultPhotos?.let {
                it.forEach {
                    Logger.log("68989898  source = " + it.photoSource.uiValue)
                }
                adapter.submitList(it)
            }
        }
    }

    private fun handleFilterDialogColorChipGroupClick(chipGroup: ChipGroup) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            vibrateExtension.vibrate()
            val selectedColor = mutableListOf<Int>()
            checkedIds.forEach {
                val colorInInt = group.findViewById<Chip>(it).chipBackgroundColor?.defaultColor
                if (colorInInt != null) {
                    selectedColor.add(colorInInt)
                }
            }
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateColorsFilter(
                selectedColor.map {
                    it
                }))
        }
    }

    private fun handleFilterDialogTagChipGroupClick(chipGroup: ChipGroup) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val updatedTagList = mutableListOf<String>()
            checkedIds.forEach {
                val tagTitle = group.findViewById<Chip>(it).text.toString()
                updatedTagList.add(tagTitle)
            }
            chipGroup.allViews.forEach {
                if (it is Chip && !updatedTagList.contains(it.text)) {
                    it.visibility = View.GONE
                }
            }
            vibrateExtension.vibrate()
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateTagsFilter(
                updatedTagList.distinct()))
        }
    }

    private fun initAdapter() {
        adapter = SearchResultPhotosPreviewAdapter(this, glideManager, generalAdsManager)
    }

    private fun initRecyclerView() {
        binding?.let {
            it.recyclerView.layoutManager =
                StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            it.recyclerView.adapter = adapter
            it.recyclerView.clearOnScrollListeners()
            it.recyclerView.addOnScrollListener(handlePagination())
        }
    }

    private fun handlePagination(): RecyclerView.OnScrollListener {
        val scrollrv = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layout = recyclerView.layoutManager as StaggeredGridLayoutManager
                    searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.let {
                        val firstitempos =
                            layout.findFirstCompletelyVisibleItemPositions(IntArray(2)).first()
                        val itemcount = layout.childCount

                        var allApiReachedTheirMaximumPage = true

                        val executeUnsplashApiRequest =
                            it.apiSourcesInfo.unsplashCurrentPageNo < (it.apiSourcesInfo.unsplashPages
                                ?: 2)
                        val executePexelsApiRequest =
                            it.apiSourcesInfo.pexelsCurrentPageNo < (it.apiSourcesInfo.pexelsPages
                                ?: 2)

                        if (executePexelsApiRequest || executeUnsplashApiRequest) {
                            allApiReachedTheirMaximumPage = false
                        }

                        val IsTagOrColorFilterApplied = it.colorsFilter.isNotEmpty() ||
                                it.tagFilter.isNotEmpty()

                        val loadcondition =
                            (!searchResultPhotosViewModel.PAGINATION_EXECUTING &&
                                    ((firstitempos + itemcount) >= it.filteredSearchResultPhotos!!.size)
                                    && !allApiReachedTheirMaximumPage) && !IsTagOrColorFilterApplied

                        if (loadcondition) {
                            Logger.log("848945984984 paginating = : " + loadcondition)
                            searchResultPhotosViewModel.PAGINATION_EXECUTING = true
                            showPaginationProgressbar()
                            searchResultPhotosViewModel.onEvent(
                                searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.searchPhotos(
                                    searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value!!.searchedKeyword!!
                                )
                            )
                        }
                    }
                }
            }
        }
        return scrollrv
    }

    private fun hidePaginationProgressbar() {
        binding?.let {
            it.progressbar.visibility = View.GONE
            it.loader.visibility = View.GONE
        }
    }

    private fun showPaginationProgressbar() {
        binding?.let {
            it.progressbar.visibility = View.VISIBLE
            it.loader.visibility = View.VISIBLE
        }
    }

    private fun getAppTheme(): Int {
        var theme = 1
        val nightModeFlags = requireActivity().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> {
                theme = 0
            }
        }
        return theme
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerView?.adapter = null
        binding = null
    }

    override fun onItemClicked(position: Int, item: Photo) {
        val bundle = Bundle().apply {
            putSerializable("onlinePreviewModel", item)
        }
        findNavController().navigate(
            R.id.action_searchResultPhotosPreview_to_singleImagePreview,
            bundle
        )
    }

    override suspend fun onMarkFavClicked(position: Int, item: Photo): Long {
        return searchResultPhotosViewModel.markPhotoAsFav(item)
    }

    override suspend fun onUnmarkFavClicked(position: Int, item: Photo): Int {
        return searchResultPhotosViewModel.unmarkPhotoAsFav(item)
    }

    override fun onItemLongClicked(position: Int, item: Photo) {
        val bundle = Bundle().apply {
            putSerializable("onlinePreviewModel", item)
            putInt("colorCode", item.colorCode)
        }
        ContextCompat.getDrawable(requireContext(), R.drawable.bottom_round_corner)
            ?.overrideColor(item.colorCode)
        findNavController().navigate(
            R.id.action_searchResultPhotosPreview_to_imageDetailsBottomSheet,
            bundle
        )
        FirebaseCrashlytics.getInstance().log("Search Results onItemLongClicked")
        try {
            throw Exception("Message : Clicked onItemLongClicked in Search Results")
        } catch (e: Exception) {
            e.message?.let {
                Logger.log("Debug logger = $it")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }
}