/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

/**
 * Pagination concrete errors.
 *
 * @param errorCode A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
public sealed class PaginationError(
    public val errorCode: String,
    description: String,
    public val reason: String? = null,
    cause: Throwable? = null
) : Exception(description, cause) {
    /**
     * Error when provided sorting fields are ambiguous as they may exist in multiple tables.
     *
     * @param sort The sort directive that was provided.
     * @param reason Optional human-readable reason for the exception, providing more context.
     */
    public class AmbiguousSortField(sort: Pageable.Sort, reason: String) : PaginationError(
        errorCode = "AMBIGUOUS_SORT_FIELD",
        description = "Detected ambiguous field: ${sort.field}",
        reason = reason
    )

    /**
     * Error when the page attributes are invalid.
     * This is when only one either the page or size is present.
     * Both must be present or none of them.
     *
     * @param reason Optional human-readable reason for the exception, providing more context.
     * @param cause Optional underlying cause of the exception, if any.
     */
    public class InvalidPageablePair(reason: String? = null, cause: Throwable? = null) : PaginationError(
        errorCode = "INVALID_PAGEABLE_PAIR",
        description = "Page attributes mismatch. Expected both 'page' and 'size', or none of them.",
        reason = reason,
        cause = cause
    )

    /**
     * Error when the provided sort direction is invalid.
     *
     * @param direction The sort direction that was provided is not valid.
     * @param reason Optional human-readable reason for the exception, providing more context.
     * @param cause Optional underlying cause of the exception, if any.
     */
    public class InvalidOrderDirection(direction: String, reason: String? = null, cause: Throwable? = null) : PaginationError(
        errorCode = "INVALID_ORDER_DIRECTION",
        description = "Ordering sort direction is invalid. Received: '$direction'",
        reason = reason,
        cause = cause
    )

    /**
     * Error when provided sorting field is invalid.
     *
     * @param sort The sort directive that was provided.
     * @param reason Optional human-readable reason for the exception, providing more context.
     */
    public class InvalidSortDirective(sort: Pageable.Sort, reason: String) : PaginationError(
        errorCode = "INVALID_SORT_DIRECTIVE",
        description = "Unexpected sort directive: $sort",
        reason = reason
    )

    /**
     * Error when the provided sort directive is missing.
     * So, that no field name was specified.
     */
    public class MissingSortDirective : PaginationError(
        errorCode = "MISSING_SORT_DIRECTIVE",
        description = "Must specify a sort field name.",
    )
}
