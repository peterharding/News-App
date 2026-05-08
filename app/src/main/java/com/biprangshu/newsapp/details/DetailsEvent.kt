package com.biprangshu.newsapp.details

import com.biprangshu.newsapp.domain.model.Article

sealed class DetailsEvent {
    data class UpsertDeleteArticle(val article: Article): DetailsEvent()
    data class LoadArticle(val url: String): DetailsEvent()

    object RemoveSideEffect: DetailsEvent()

}