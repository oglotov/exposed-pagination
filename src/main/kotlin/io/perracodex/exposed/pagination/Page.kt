/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

import kotlinx.serialization.Serializable

/**
 * Represents a single page of results within a paginated dataset,
 * encapsulating both the data content for a concrete page and metadata
 * about the pagination state.
 *
 * @param T The type of elements contained within the page.
 * @property details Metadata providing [Details] about the pagination state, such as total pages and current page index.
 * @property content The list of elements of type [T] contained in the page.
 */
@Serializable
public data class Page<out T : Any>(
    val details: Details,
    val content: List<T>,
) {
    /**
     * Contains metadata describing the state and configuration of a paginated [Page].
     *
     * @property totalPages The total number of pages available based on the pagination parameters.
     * @property pageIndex The zero-based index of the current page.
     * @property totalElements The aggregate number of elements across all pages.
     * @property elementsPerPage The maximum number of elements that can be contained in a single page.
     * @property elementsInPage The actual number of elements present in the current page.
     * @property isFirst Indicates whether the current page is the first one.
     * @property isLast Indicates whether the current page is the last one.
     * @property hasNext Indicates if there is a subsequent page available after the current one.
     * @property hasPrevious Indicates if there is a preceding page before the current one.
     * @property isOverflow Indicates whether the requested page index exceeds the total number of available pages.
     * @property sort The sorting criteria applied to the elements within the page, if any.
     */
    @Serializable
    public data class Details(
        val totalPages: Int,
        val pageIndex: Int,
        val totalElements: Int,
        val elementsPerPage: Int,
        val elementsInPage: Int,
        val isFirst: Boolean,
        val isLast: Boolean,
        val hasNext: Boolean,
        val hasPrevious: Boolean,
        val isOverflow: Boolean,
        val sort: List<Pageable.Sort>?,
    )

    public companion object {
        /**
         *  Factory method to construct a new [Page] instance with the specified content and pagination parameters.
         *
         * @param content The list of elements to include in the current page.
         * @param totalElements The total count of elements across all pages.
         * @param pageable The [Pageable] settings used to retrieve the content. `null` if no pagination was applied.
         * @return A [Page] containing the specified [content] and corresponding pagination [Details].
         */
        public fun <T : Any> build(content: List<T>, totalElements: Int, pageable: Pageable?): Page<T> {
            // Set default page size.
            val pageSize: Int = pageable?.size.takeIf { it != null && it > 0 } ?: totalElements

            // Calculate total pages, ensuring totalPages is 0 if there are no elements.
            val totalPages: Int = if (totalElements > 0 && pageSize > 0) {
                ((totalElements + pageSize - 1) / pageSize).coerceAtLeast(minimumValue = 1)
            } else {
                0
            }

            // Determine the current page index.
            val pageIndex: Int = pageable?.page ?: 0

            // Adjust pagination state based on total pages and content availability.
            val elementsInPage: Int = content.size
            val isFirst: Boolean = (totalPages == 0) || (pageIndex == 0)
            val isLast: Boolean = (totalPages == 0) || (pageIndex >= totalPages - 1)
            val hasNext: Boolean = (pageIndex < totalPages - 1) && (totalPages > 0)
            val hasPrevious: Boolean = (pageIndex > 0) && (totalPages > 0)
            val isOverflow: Boolean = if (totalPages > 0) {
                pageIndex >= totalPages
            } else {
                pageIndex > 0
            }

            // Construct the Page object with the determined states.
            return Page(
                details = Details(
                    totalPages = totalPages,
                    pageIndex = pageIndex,
                    totalElements = totalElements,
                    elementsPerPage = pageSize,
                    elementsInPage = elementsInPage,
                    isFirst = isFirst,
                    isLast = isLast,
                    hasNext = hasNext,
                    hasPrevious = hasPrevious,
                    isOverflow = isOverflow,
                    sort = pageable?.sort
                ),
                content = content
            )
        }

        /**
         * Factory method to create an empty [Page] instance with no content.
         * Useful for scenarios where a query returns no results but pagination metadata is still required.
         *
         * @param pageable The [Pageable] settings that were applied to the query, or `null` if no pagination was used.
         * @return An empty [Page] with appropriate pagination [Details].
         */
        public fun <T : Any> empty(pageable: Pageable?): Page<T> {
            return build(content = emptyList(), totalElements = 0, pageable = pageable)
        }
    }
}
