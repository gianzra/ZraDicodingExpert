package com.gianzra.expert.submission.tvshows

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gianzra.expert.core.data.Resource
import com.gianzra.expert.core.domain.model.Movie
import com.gianzra.expert.core.ui.MoviesAdapter
import com.gianzra.expert.core.utils.SortUtils
import com.gianzra.expert.submission.R
import com.gianzra.expert.submission.databinding.FragmentTvShowsBinding
import com.gianzra.expert.submission.detail.DetailActivity
import com.gianzra.expert.submission.home.HomeActivity
import com.gianzra.expert.submission.home.SearchViewModel
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
@FlowPreview
class TvShowsFragment : Fragment() {

    private var binding: FragmentTvShowsBinding? = null
    private val viewModel: TvShowsViewModel by viewModel()
    private lateinit var tvShowsAdapter: MoviesAdapter
    private val searchViewModel: SearchViewModel by viewModel()
    private lateinit var searchView: MaterialSearchView
    private var sort = SortUtils.RANDOM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTvShowsBinding.inflate(inflater, container, false)

        setupToolbar()
        setupOptionsMenu()
        setupSearchView()

        return binding!!.root
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity?)?.setSupportActionBar(toolbar)
    }

    private fun setupOptionsMenu() {
        setHasOptionsMenu(true)
    }

    private fun setupSearchView() {
        searchView = (requireActivity() as HomeActivity).findViewById(R.id.search_view)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvShowsAdapter = MoviesAdapter()
        setList(sort)
        observeSearchQuery()
        setSearchList()

        binding?.rvTvShows?.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = tvShowsAdapter
        }

        tvShowsAdapter.onItemClick = { selectedData ->
            startActivity(Intent(activity, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_MOVIE, selectedData)
            })
        }

        listOf(binding?.random, binding?.newest, binding?.popularity, binding?.vote)
            .forEach { button ->
                button?.setOnClickListener {
                    binding?.menu?.close(true)
                    sort = when (button) {
                        binding?.random -> SortUtils.RANDOM
                        binding?.newest -> SortUtils.NEWEST
                        binding?.popularity -> SortUtils.POPULARITY
                        binding?.vote -> SortUtils.VOTE
                        else -> SortUtils.RANDOM
                    }
                    setList(sort)
                }
            }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
    }

    private fun setList(sort: String) {
        viewModel.getTvShows(sort).observe(this, tvShowsObserver)
    }

    private val tvShowsObserver = Observer<Resource<List<Movie>>> { tvShow ->
        with(binding!!) {
            progressBar.visibility = when (tvShow) {
                is Resource.Loading -> View.VISIBLE
                else -> View.GONE
            }
            notFound.visibility = when (tvShow) {
                is Resource.Loading -> View.GONE
                is Resource.Success -> View.GONE
                is Resource.Error -> View.VISIBLE
            }
            notFoundText.visibility = when (tvShow) {
                is Resource.Loading, is Resource.Success -> View.GONE
                is Resource.Error -> View.VISIBLE
            }

            if (tvShow is Resource.Success) {
                tvShowsAdapter.setData(tvShow.data)
            }

            if (tvShow is Resource.Error) {
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun observeSearchQuery() {
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let(searchViewModel::setSearchQuery)
                return true
            }
        })
    }


    private fun setSearchList() {
        searchViewModel.tvShowResult.observe(viewLifecycleOwner) { tvShows ->
            with(binding) {
                if (this == null) return@with

                progressBar.visibility = if (tvShows.isNullOrEmpty()) View.GONE else View.VISIBLE
                notFound.visibility = if (tvShows.isNullOrEmpty()) View.VISIBLE else View.GONE
                notFoundText.visibility = if (tvShows.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            tvShowsAdapter.setData(tvShows)
        }

        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() = hideViews()
            override fun onSearchViewClosed() {
                hideViews()
                setList(sort)
            }

            private fun hideViews() {
                with(binding) {
                    if (this == null) return@with

                    progressBar.visibility = View.GONE
                    notFound.visibility = View.GONE
                    notFoundText.visibility = View.GONE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
