/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

import io.ktor.http.*
import io.ktor.server.application.*
import io.perracodex.exposed.utils.SortParameterParser

/**
 * Extension function to construct a [Pageable] instance from [ApplicationCall] request parameters.
 *
 * Pagination parameters:
 * - 'page': The index of the page requested (0-based). If not provided, defaults to 0.
 * - 'size': The number of items per page. If not provided, defaults to 0, indicating no pagination.
 *
 * Sorting parameters:
 * - 'sort': A list of strings indicating the fields to sort by and their directions. Each string
 *           should follow the format "fieldName,direction". If 'direction' is omitted, ascending
 *           order is assumed. Multiple 'sort' parameters can be provided for multi-field sorting.
 *
 * A [PaginationError.InvalidPageablePair] is raised if only one pagination parameter
 * ('page' or 'size') is provided without the other.
 * Similarly, a [PaginationError.InvalidOrderDirection] is raised if an invalid direction is
 * specified in any 'sort' parameter.
 *
 * The sorting logic accommodates scenarios involving multiple tables. If the sorting field specification
 * includes a table name (denoted by a field name prefixed with the table name and separated by a dot,
 * like "table.fieldName"), the sorting is applied specifically to the identified table and field.
 * This explicit specification prevents ambiguity and ensures accurate sorting when queries involve
 * multiple tables with potentially overlapping field names.
 *
 * If the field name does not include a table prefix, and ambiguity arises due to multiple tables
 * sharing the same field name, an exception is raised. To avoid this, it is recommended to prefix
 * field names with table names for clarity and precision.
 *
 * If no pagination or sorting is requested, the function returns null, indicating the absence of
 * pageable constraints.
 *
 * @return A [Pageable] object containing the pagination and sorting configuration derived from the
 *         request's parameters, or null if no pagination or sorting is requested.
 * @throws PaginationError.InvalidPageablePair if pagination parameters are incomplete.
 * @throws PaginationError.InvalidOrderDirection if a sorting direction is invalid.
 */
public fun ApplicationCall.getPageable(): Pageable? {
    val parameters: Parameters = request.queryParameters
    val pageIndex: Int? = parameters["page"]?.toIntOrNull()
    val pageSize: Int? = parameters["size"]?.toIntOrNull()

    // If only one of the page parameters is provided, raise an error.
    if ((pageIndex == null).xor(other = pageSize == null)) {
        throw PaginationError.InvalidPageablePair()
    }

    // Retrieve the 'sort' parameters. Each can contain a field name and a sort direction.
    val sortParameters: List<String>? = parameters.getAll(name = "sort")

    // If no parameters are provided, means no pagination is requested.
    if (pageIndex == null && sortParameters.isNullOrEmpty()) {
        return null
    }

    // Parse sorting parameters into a list of Sort directives.
    val sort: List<Pageable.Sort>? = sortParameters?.let {
        SortParameterParser.getSortDirectives(sortParameters = sortParameters)
    }

    return Pageable(
        page = pageIndex ?: 0,
        size = pageSize ?: 0,
        sort = sort
    )
}
