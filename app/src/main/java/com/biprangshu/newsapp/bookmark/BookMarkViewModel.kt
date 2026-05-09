package com.biprangshu.newsapp.bookmark

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.newsapp.domain.usecases.NewsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookMarkViewModel @Inject constructor(
    private val newsUseCases: NewsUseCases
) : ViewModel() {

    private val _state = mutableStateOf(BookmarkState())
    val state: State<BookmarkState> = _state

    var sideEffect by mutableStateOf<String?>(null)
        private set

    init {
        getArticles()
    }

    fun onEvent(event: BookMarkEvent) {
        when (event) {
            is BookMarkEvent.ToggleEditMode -> _state.value = _state.value.copy(
                isEditMode = !_state.value.isEditMode,
                selectedUrls = emptySet()
            )
            is BookMarkEvent.ToggleArticleSelection -> {
                val current = _state.value.selectedUrls
                val updated = if (event.url in current) current - event.url else current + event.url
                _state.value = _state.value.copy(selectedUrls = updated)
            }
            is BookMarkEvent.DeleteSelectedArticles -> deleteSelectedArticles()
            is BookMarkEvent.RemoveSideEffect -> sideEffect = null
        }
    }

    private fun deleteSelectedArticles() {
        viewModelScope.launch {
            val urlsToDelete = _state.value.selectedUrls
            if (urlsToDelete.isEmpty()) return@launch
            val toDelete = _state.value.articles.filter { it.url in urlsToDelete }
            toDelete.forEach { newsUseCases.deleteArticle(it) }
            val count = toDelete.size
            sideEffect = "$count bookmark${if (count == 1) "" else "s"} deleted"
            _state.value = _state.value.copy(isEditMode = false, selectedUrls = emptySet())
        }
    }

    private fun getArticles() {
        newsUseCases.selectArticles().onEach {
            _state.value = _state.value.copy(articles = it.asReversed())
        }.launchIn(viewModelScope)
    }
}
