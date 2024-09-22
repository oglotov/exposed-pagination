/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

import io.perracodex.exposed.utils.QuerySorter
import org.jetbrains.exposed.sql.Query

/**
 * Extension function to apply pagination to a database [Query] based on the provided [pageable] directives,
 * and map the results to a list of entities using the provided [mapper].
 *
 * If [pageable] is not null, it first applies the chosen sorting order (if included in pageable),
 * then calculates the start index based on the page number and size, and finally applies these as
 * limits to the query.
 *
 * If the page size is zero, no limit is applied, and all records are returned.
 *
 * If [pageable] is null, no pagination is applied, and a [Page] with the query results is returned.
 *
 * @param pageable An optional [Pageable] object containing pagination information.
 * @param mapper The [IEntityMapper] to use to map the query results to entities.
 * @return The [Page] of entities mapped from the query results.
 *
 * @see Page
 * @see Pageable
 * @see IEntityMapper
 * @see Query.paginate
 */
public fun <T : Any> Query.paginate(pageable: Pageable?, mapper: IEntityMapper<T>): Page<T> {
    return this.count().toInt().takeIf { it > 0 }?.let { totalElements ->
        this.paginate(pageable = pageable).map { resultRow ->
            mapper.from(row = resultRow)
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
 * Extension function to apply pagination to a database [Query] based on the provided [pageable] directives.
 *
 * If [pageable] is not null, it first applies the chosen sorting order (if included in pageable),
 * then calculates the start index based on the page number and size, and finally applies these as
 * limits to the query.
 *
 * If the page size is zero, no limit is applied, and all records are returned.
 *
 * If [pageable] is null, the original query is returned unchanged.
 *
 * @param pageable An optional [Pageable] object containing pagination information.
 * @return The [Query] with pagination applied if [pageable] is provided, otherwise the original Query.
 *
 * @see Pageable
 */
public fun Query.paginate(pageable: Pageable?): Query {
    pageable?.let {
        pageable.sort?.let { sortDirectives ->
            QuerySorter.applyOrder(query = this, sortDirectives = sortDirectives)
        }

        if (pageable.size > 0) {
            val startIndex: Int = pageable.page * pageable.size
            this.limit(n = pageable.size, offset = startIndex.toLong())
        }
    }

    return this
}
