/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

import io.perracodex.exposed.utils.QuerySorter
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow

/**
 * Applies pagination to this [Query] based on the specified [pageable] parameters
 * and maps the results to a [Page] of domain models.
 *
 * This extension function performs the following operations:
 * 1. **Sorting:** If [pageable] includes sorting directives, they are applied to the [Query].
 * 2. **Limiting:** Uses page number and size to set the query’s limit and offset.
 * 3. **Mapping:** Executes the [Query] and maps each [ResultRow] to a domain model of type [T].
 * 4. **Paging Metadata:** Constructs a [Page] object containing the mapped domain models along with the pagination details.
 *
 * If [pageable] is `null`, or the defined page size is `0`, the entire result set is retrieved without any pagination,
 * and a [Page] containing all domain models is returned.
 *
 * @param pageable Optional [Pageable] containing pagination and sorting information. If `null`, no pagination is applied.
 * @param map Implementation of [MapModel] used to map [ResultRow] instances into domain models of type [T].
 * @return A [Page] containing the list of mapped domain models and associated pagination metadata.
 *
 * @see [Page]
 * @see [Pageable]
 * @see [MapModel]
 * @see [Query.paginate]
 */
public fun <T : Any> Query.paginate(pageable: Pageable?, map: MapModel<T>): Page<T> {
    return this.count().toInt().takeIf { it > 0 }?.let { totalElements ->
        this.paginate(pageable = pageable).map { resultRow ->
            map.from(row = resultRow)
        }.let { content ->
            Page.build(
                content = content,
                totalElements = totalElements,
                pageable = pageable
            )
        }
    } ?: Page.empty(pageable = pageable)
}

/**
 * Applies pagination directives to this [Query] based on the provided [pageable] parameters.
 *
 * This extension function modifies the query in the following ways:
 * 1. **Sorting:** If [pageable] includes sorting directives, they are applied to the [Query].
 * 2. **Limiting:** Uses page number and size to set the query’s limit and offset.
 *
 * If [pageable] is `null`, or the defined page size is `0`, the entire result set is retrieved without any pagination,
 * and a [Page] containing all domain models is returned.
 *
 * @param pageable Optional [Pageable] containing pagination and sorting information. If `null`, no pagination is applied.
 * @return The modified [Query] with pagination and sorting applied if [pageable] is provided;
 * otherwise, the original [Query] is returned unaltered.
 *
 * @see [Pageable]
 */
public fun Query.paginate(pageable: Pageable?): Query {
    pageable?.let {
        pageable.sort?.let { sortDirectives ->
            QuerySorter.applyOrder(query = this, sortDirectives = sortDirectives)
        }

        if (pageable.size > 0) {
            val startIndex: Int = pageable.page * pageable.size
            this.limit(count = pageable.size)
                .offset(start = startIndex.toLong())
        }
    }

    return this
}
