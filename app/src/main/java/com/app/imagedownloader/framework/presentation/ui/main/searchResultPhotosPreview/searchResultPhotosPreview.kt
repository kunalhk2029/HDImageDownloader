package com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.allViews
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.business.domain.Filters.OrientationFilter
import com.app.imagedownloader.business.domain.Filters.SortByFilter
import com.app.imagedownloader.business.domain.model.UnsplashPhotoInfo
import com.app.imagedownloader.databinding.FragmentSearchResultPhotosPreviewBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.app.imagedownloader.framework.presentation.ui.main.searchResultPhotosPreview.state.SearchResultPhotosPreviewStateEvents
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class searchResultPhotosPreview : Fragment(R.layout.fragment_search_result_photos_preview),
    SearchResultPhotosPreviewAdapter.Interaction {

    @Inject
    lateinit var glideManager: GlideManager

    @Inject
    lateinit var generalAdsManager: GeneralAdsManager

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
        searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.searchedKeyword?.let {
            uiCommunicationListener.setToolbarTitleText(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_result_preview_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.showFilterDialog) showFilterDialog()
        return true
    }

    private fun showFilterDialog() {
        val filterDialog = MaterialDialog(requireContext()).show {
            setContentView(R.layout.filter_dialog)
        }

        val previousSortFilter = searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.sortFilter
        val previousOrientationFilter = searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.orientationFilter
        val previousTagsFilter = searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.tagFilter
        val previousColorsFilter = searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.colorsFilter

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
//                Logger.log("53355335 = "+p2)
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
                OrientationFilter.Landscape.uiValue
            ))
        orientationSpinner.adapter = orientationSpinneradapter

        val selectedOrientatationSpinnerPosistion =
            if (searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.orientationFilter is OrientationFilter.Potrait) 1
            else if (searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.orientationFilter is OrientationFilter.Landscape) 2
            else 0

        orientationSpinner.setSelection(selectedOrientatationSpinnerPosistion)
        orientationSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                searchResultPhotosViewModel.onEvent(
                    searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.updateOrientationFilter(
                        orientationFilter = when (p2) {
                            0 -> {
                                OrientationFilter.All
                            }
                            1 -> {
                                OrientationFilter.Potrait
                            }
                            2 -> {
                                OrientationFilter.Landscape
                            }
                            else -> OrientationFilter.All
                        }
                    )
                )
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


        addTagFilterChips(tagChipGroup,false,null)

        handleFilterDialogTagChipGroupClick(tagChipGroup)

        tagSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.let { state ->
                    if (p2!=0&&state.tagFilter.contains(state.distinctTagsList[p2])) return
                    val updatedTagList= if (p2 == 0) state.tagFilter else{
                        val list =  state.tagFilter + listOf(state.distinctTagsList[p2])
                        list.distinct()
                    }
                    searchResultPhotosViewModel.onEvent(
                        searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.updateTagsFilter(
                            tagsList =updatedTagList)
                    )
                    if (p2 != 0)  addTagFilterChips(tagChipGroup,true, state.distinctTagsList[p2])
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val filterColorChipGroup = filterDialog.findViewById<ChipGroup>(R.id.chipGroup)
        val filtertemplatechip = filterDialog.findViewById<Chip>(R.id.templateChip)
        val filtertemplatechip2 = filterDialog.findViewById<Chip>(R.id.templateChip2)
        val colorList =
            searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.distinctColorsList

        colorList?.forEachIndexed { index, s ->
            if (s!=""){
                val chip = Chip(requireContext())
                chip.id = index
                chip.layoutParams = if (index % 2 == 0) filtertemplatechip.layoutParams else
                    filtertemplatechip2.layoutParams
                chip.isCheckable = true
                chip.alpha = 0.85f
                chip.checkedIcon = ContextCompat.getDrawable(requireContext(), R.drawable.chip_check)
                chip.checkedIconTint = if (isDark(Color.parseColor(s)))
                    ColorStateList.valueOf(Color.parseColor("#ffffff"))
                else
                    ColorStateList.valueOf(Color.parseColor("#121212"))
                chip.chipBackgroundColor =
                    ColorStateList.valueOf(Color.parseColor(s))
                if (searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value?.colorsFilter?.contains(Color.parseColor(s).toString()) == true){
                    chip.isChecked=true
                }
                filterColorChipGroup.addView(chip)
            }
        }

        handleFilterDialogColorChipGroupClick(filterColorChipGroup)

        filterDialog.findViewById<Button>(R.id.cancel_filter).setOnClickListener {
            previousSortFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateSortByFilter(it))
            }

            previousOrientationFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateOrientationFilter(it))
            }
            previousTagsFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateTagsFilter(it))
            }

            previousColorsFilter?.let {
                searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateColorsFilter(it))
            }

            filterDialog.dismiss()
        }

        filterDialog.findViewById<Button>(R.id.apply_filter).setOnClickListener {
            filterDialog.dismiss()
            searchResultPhotosViewModel.onEvent(searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.FilterPhotos)
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
                requireActivity().onBackPressed()
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
            }
            it.data?.getContentIfNotHandled()?.let {
                if (!dataState.loading && searchResultPhotosViewModel.PAGINATION_EXECUTING) {
                    searchResultPhotosViewModel.PAGINATION_EXECUTING = false
                    searchResultPhotosViewModel.updateCurrentPage()
                }

                Logger.log("88985959  121 = " + it.currentPage)
                Logger.log("88985959  121 = " + it.totalPages)
                Logger.log("88985959  121 = " + it.totalPhotosItems)

                it.searchResultPhotos?.let {
                    searchResultPhotosViewModel.setSearchResultPhotosList(it)
                }

                it.totalPages?.let { it1 -> searchResultPhotosViewModel.setTotalPages(it1) }
                it.totalPhotosItems?.let { it1 -> searchResultPhotosViewModel.setTotalItems(it1) }
            }

            it.message?.let {
                it.getContentIfNotHandled()?.let {
                    searchResultPhotosViewModel.PAGINATION_EXECUTING = false
                    errorDialog?.hide()
                    errorDialog = MaterialDialog(requireContext()).show {
                        message(null, "Error")
                        title(null, "Retry Again")
                        positiveButton(null, "OK")
                    }
                }
            }
        }

        searchResultPhotosViewModel.searchResultPhotosPreviewViewState.observe(viewLifecycleOwner) {
            it.filteredSearchResultPhotos?.let {
                adapter.submitList(it)
            }
        }
    }

    private fun handleFilterDialogColorChipGroupClick(chipGroup: ChipGroup) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedColor = mutableListOf<Int>()
            checkedIds.forEach {
                val colorInInt = group.findViewById<Chip>(it).chipBackgroundColor?.defaultColor
                if (colorInInt != null) {
                    selectedColor.add(colorInInt)
                }
            }
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateColorsFilter(
                selectedColor.map {
                    it.toString()
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
                if (it is Chip && !updatedTagList.contains(it.text)){
                    it.visibility=View.GONE
                }
            }
            searchResultPhotosViewModel.onEvent(SearchResultPhotosPreviewStateEvents.updateTagsFilter(updatedTagList.distinct()))
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
                        Logger.log("88985959  684  8= " + firstitempos)
                        val itemcount = layout.childCount
                        val totalitemsinlist = it.searchResultPhotos?.size ?: (20 * it.currentPage)
                        val loadcondition =
                            (firstitempos + itemcount >= totalitemsinlist) && (it.currentPage < it.totalPages!!)
                                    && !searchResultPhotosViewModel.PAGINATION_EXECUTING

                        Logger.log("88985959 Firstitempos: " + firstitempos)
                        Logger.log("88985959 : Itemcountcurrent" + itemcount)
                        Logger.log("88985959 Inlist: " + totalitemsinlist)
                        Logger.log("88985959 PAGENO  : " + it.currentPage)
                        Logger.log("478948484 : " + loadcondition)
                        if (loadcondition) {
                            searchResultPhotosViewModel.PAGINATION_EXECUTING = true
                            showPaginationProgressbar()
                            searchResultPhotosViewModel.onEvent(
                                searchResultPhotosPreviewStateEvents = SearchResultPhotosPreviewStateEvents.searchPhotos(
                                    searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value!!.searchedKeyword!!,
                                    searchResultPhotosViewModel.searchResultPhotosPreviewViewState.value!!.currentPage
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerView?.adapter = null
        binding = null
    }

    override fun onItemClicked(position: Int, item: UnsplashPhotoInfo.photoInfo) {
        val bundle = Bundle().apply {
            putSerializable("onlinePreviewModel", item)
        }
        findNavController().navigate(
            R.id.action_searchResultPhotosPreview_to_singleImagePreview,
            bundle
        )
    }
}