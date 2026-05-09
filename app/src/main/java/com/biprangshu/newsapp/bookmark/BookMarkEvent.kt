package com.biprangshu.newsapp.bookmark

sealed class BookMarkEvent {
    object ToggleEditMode : BookMarkEvent()
    data class ToggleArticleSelection(val url: String): BookMarkEvent()
    object BulkDelete : BookMarkEvent()
    object RemoveSideEffect : BookMarkEvent()
}