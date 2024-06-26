package com.sevenexp.craftit.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sevenexp.craftit.Locator
import com.sevenexp.craftit.R
import com.sevenexp.craftit.data.source.database.entity.HistoryEntity
import com.sevenexp.craftit.databinding.FragmentHomeBinding
import com.sevenexp.craftit.ui.adapter.CraftItemAdapter
import com.sevenexp.craftit.ui.adapter.HistoryItemAdapter
import com.sevenexp.craftit.ui.adapter.LoadingAdapter
import com.sevenexp.craftit.ui.image_search.ImageSearchActivity
import com.sevenexp.craftit.ui.search_result.SearchResultActivity
import com.sevenexp.craftit.utils.ResultState
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeViewModel>(factoryProducer = { Locator.homeViewModelFactory })
    private val historyItemAdapter by lazy { HistoryItemAdapter() }
    private val fypAdapter by lazy { CraftItemAdapter() }
    private lateinit var binding: FragmentHomeBinding

    companion object {
        private const val UNAUTHORIZED_HTTP_RESPONSE = "http 401"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHomeBinding.inflate(inflater, container, false).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListener()
        setupButtons()
        setupSearchView()
    }


    private fun setupSearchView() {
        with(binding) {
            searchView.setupWithSearchBar(searchbar)

            searchView.editText.setOnEditorActionListener { textview, actionId, event ->
                searchbar.setText(searchView.text)
                searchView.hide()
                val intent = Intent(requireContext(), SearchResultActivity::class.java)
                intent.putExtra(SearchResultActivity.EXTRA_QUERY, searchView.text)
                startActivity(intent)
                false
            }

            requireActivity().onBackPressedDispatcher.addCallback {
                if (searchView.isShowing || searchView.isShown) {
                    searchView.hide()
                } else {
                    if (!requireActivity().onBackPressedDispatcher.hasEnabledCallbacks()) {
                        findNavController().popBackStack()
                    }
                }
            }

        }
    }

    private fun setupButtons() {
        with(binding) {
            btnImageSearch.setOnClickListener {
                startActivity(Intent(requireContext(), ImageSearchActivity::class.java))
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding) {
            with(rvForYou) {
                adapter = fypAdapter.withLoadStateFooter(footer = LoadingAdapter { fypAdapter.retry() })
                layoutManager = LinearLayoutManager(context)
            }
            with(rvHistory) {
                adapter = historyItemAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun setupListener() {
        lifecycleScope.launch {
            viewModel.getFypState.collect { state ->
                binding.greeting.text = getString(R.string.text_welcome, state.username)
                fypAdapter.submitData(lifecycle, state.resultGetFyp)
                Log.d("HOME FRAGMENT", state.resultGetFyp.toString())

                when (val result = state.resultGetHistory) {
                    is ResultState.Success -> handleHistorySuccess(result)
                    is ResultState.Error -> Unit
                    is ResultState.Loading -> Unit
                    is ResultState.Idle -> Unit
                }
            }
        }
    }

    private fun handleHistorySuccess(result: ResultState.Success<List<HistoryEntity>>) {
        val visible = View.VISIBLE
        result.data?.let { histories ->
            if (histories.isEmpty()) {
                hideHistory()
            } else {
                binding.rvHistory.visibility = visible
                binding.rlHistory.visibility = visible
                historyItemAdapter.setData(histories)
            }
        } ?: {
            hideHistory()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getFyp()
    }

    private fun hideHistory() {
        binding.rvHistory.visibility = View.GONE
        binding.rlHistory.visibility = View.GONE
    }

}