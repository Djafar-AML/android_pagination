package com.example.android.codelabs.paging.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlin.math.max

private const val STARTING_KEY = 0
private val firstArticleCreatedTime = LocalDateTime.now()
private const val LOAD_DELAY_MILLIS = 3_000L

class ArticlePagingSource : PagingSource<Int, Article>() {

    /**
     * Ensure that the key is valid by returning the max of the starting key and the key.
     *
     * @param key The key to use for the encryption.
     */
    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)

    /* The refresh key is used for the initial load of the next PagingSource, after invalidation */
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null
        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {

        /* Start paging with the STARTING_KEY if this is the first load */
        val start = params.key ?: STARTING_KEY

        /*  Load as many items as hinted by params.loadSize */
        val range = start.until(start + params.loadSize)

        if (start != STARTING_KEY) delay(LOAD_DELAY_MILLIS)

        return LoadResult.Page(
            data = range.map { number ->
                Article(
                    id = number,
                    title = "Article $number",
                    description = "This describes article $number",
                    created = firstArticleCreatedTime.minusDays(number.toLong())
                )
            },
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
            nextKey = range.last + 1
        )
    }
}