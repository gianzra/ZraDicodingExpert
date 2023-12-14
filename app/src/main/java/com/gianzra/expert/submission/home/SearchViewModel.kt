package com.gianzra.expert.submission.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.gianzra.expert.core.domain.usecase.MovieAppUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class SearchViewModel(private val movieAppUseCase: MovieAppUseCase) : ViewModel() {

    private val queryChannel = Channel<String>()

    fun setSearchQuery(search: String) = viewModelScope.launch {
        queryChannel.trySend(search)
    }

    private fun createSearchFlow(): Flow<String> =
        queryChannel.receiveAsFlow()
            .debounce(300)
            .distinctUntilChanged()
            .filter { it.trim().isNotEmpty() }

    val tvShowResult = createSearchFlow().flatMapLatest {
        movieAppUseCase.getSearchTvShows(it)
    }.asLiveData()
}
