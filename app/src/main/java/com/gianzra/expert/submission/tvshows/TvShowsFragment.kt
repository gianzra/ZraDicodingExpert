package com.gianzra.expert.submission.tvshows

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
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
import com.gianzra.expert.submission.home.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
@FlowPreview
class TvShowsFragment : Fragment() {

    private var binding: FragmentTvShowsBinding? = null
    private val viewModel: TvShowsViewModel by viewModel()
    private lateinit var tvShowsAdapter: MoviesAdapter
    private lateinit var searchView: SearchView
    private var sort = SortUtils.RANDOM
    private val searchViewModel: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTvShowsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSortButtons()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity?)?.setSupportActionBar(toolbar)
    }

    private fun setupRecyclerView() {
        tvShowsAdapter = MoviesAdapter()
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
    }


    private fun setupSortButtons() {
        listOf(binding?.random, binding?.newest, binding?.popularity, binding?.vote)
            .forEach { button ->
                button?.setOnClickListener {
                    sort = when (button) {
                        binding?.random -> SortUtils.RANDOM
                        binding?.newest -> SortUtils.NEWEST
                        binding?.popularity -> SortUtils.POPULARITY
                        binding?.vote -> SortUtils.VOTE
                        else -> SortUtils.RANDOM
                    }
                    setList(sort)

                    // Menutup SearchView setelah melakukan sorting
                    searchView.onActionViewCollapsed()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)

        if (searchItem != null) {
            val actionView = searchItem.actionView

            if (actionView is SearchView) {
                searchView = actionView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (searchView != null) {
                            newText?.let(searchViewModel::setSearchQuery)
                        }
                        return true
                    }
                })

                searchView.setOnSearchClickListener {
                    // Handle search view click event
                }

                searchView.setOnCloseListener {
                    // Handle close event
                    setList(sort)
                    false
                }
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setList(sort: String) {
        viewModel.getTvShows(sort).observe(viewLifecycleOwner, tvShowsObserver)
    }

    private val tvShowsObserver = Observer<Resource<List<Movie>>> { tvShow ->
        with(binding!!) {
            when (tvShow) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    // Tambahan: Tambahkan log atau debugger di sini untuk melihat bahwa proses loading berlangsung.
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    notFound.visibility = View.GONE
                    notFoundText.visibility = View.GONE

                    // Tambahan: Tambahkan log atau debugger di sini untuk melihat data yang diterima.
                    tvShowsAdapter.setData(tvShow.data)
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    notFound.visibility = View.VISIBLE
                    notFoundText.visibility = View.VISIBLE

                    Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
