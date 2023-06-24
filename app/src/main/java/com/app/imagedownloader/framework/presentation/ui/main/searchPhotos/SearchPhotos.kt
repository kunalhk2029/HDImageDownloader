package com.app.imagedownloader.framework.presentation.ui.main.searchPhotos

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.app.imagedownloader.R
import com.app.imagedownloader.databinding.FragmentSearchPhotosBinding
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.home.Home
import com.app.imagedownloader.framework.presentation.ui.main.searchPhotos.state.SearchPhotosStateEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchPhotos : Fragment(R.layout.fragment_search_photos),
    AutoCompleteSearchAdapter.Interaction {

    var binding: FragmentSearchPhotosBinding? = null
    var autoCompleteSearchJob = Job()
    private val viewModel by viewModels<SearchPhotosViewModel>()
    lateinit var adapter: AutoCompleteSearchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchPhotosBinding.bind(view)

        initAutoCompleteSearchAdapter()
        initAutoCompleteSearchRecyclerView()
        subscribeObservers()
        initSearchBarOnTextChangeListener()
        handleKeyoardEnterClick()
        binding?.searchBar?.showKeyboard()
    }

    fun EditText.showKeyboard() {
        post {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    private fun subscribeObservers() {
        viewModel.searchPhotosDataState.observe(viewLifecycleOwner) {
            it.data?.getContentIfNotHandled()?.autoCompletedRelatedSearchKeywords?.let {
                Logger.log("659595 = " + it)
                viewModel.setautoCompletedRelatedSearchKeywordsList(it)
            }
            it.loading.let {
                if (!it) binding?.progressBar?.visibility = View.GONE
            }
        }

        viewModel.searchPhotosViewState.observe(viewLifecycleOwner) {
            it.autoCompletedRelatedSearchKeywords?.let {
                adapter.submitList(it)
            }
        }
    }

    private fun handleKeyoardEnterClick() {
        binding?.searchBar?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    if (binding?.searchBar?.text.isNullOrBlank()) {
                        Toast.makeText(requireContext(), "Query is empty...", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnEditorActionListener false
                    }
                    finalSearchKeywordSelected(binding?.searchBar?.text.toString())
                    true
                }
                else -> false
            }
        }
    }

    private fun finalSearchKeywordSelected(keyword: String) {
        lifecycleScope.launch {
            Home.selectedKeyword.send(keyword)
            requireActivity().onBackPressed()
        }
    }

    private fun initSearchBarOnTextChangeListener() {
        binding?.let {
            it.searchBar.textChanged {
                autoCompleteSearchJob = Job()
                adapter.submitList(listOf())
                if (binding?.searchBar?.text.isNullOrBlank()) {
                    binding?.progressBar?.visibility = View.GONE
                    return@textChanged
                }
                binding?.progressBar?.visibility = View.VISIBLE
                lifecycleScope.launch(Main + autoCompleteSearchJob) {
                    delay(1000L)
                    viewModel.onEvent(
                        searchPhotosStateEvents = SearchPhotosStateEvents.searchBarTextChanged(
                            it.toString()
                        )
                    )
                }
            }
        }
    }

    private fun initAutoCompleteSearchRecyclerView() {
        binding?.let {
            it.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            it.recyclerView.adapter = adapter
        }
    }

    private fun initAutoCompleteSearchAdapter() {
        adapter = AutoCompleteSearchAdapter(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerView?.adapter = null
        binding = null
    }

    override fun onItemClicked(item: String, position: Int) {
        finalSearchKeywordSelected(item)
    }
}