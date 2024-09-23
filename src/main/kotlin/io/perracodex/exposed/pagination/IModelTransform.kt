/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

import org.jetbrains.exposed.sql.ResultRow

/**
 * Contract for converting a database [ResultRow] into an instance of type [T].
 *
 * Sample Usage:
 * ```
 * data class Employee(
 *     val id: Int,
 *     val firstName: String,
 *     val lastName: String,
 * ) {
 *     // Implement the IModelTransform interface.
 *     companion object : IModelTransform<Employee> {
 *         override fun from(row: ResultRow): Employee {
 *             // Transform the ResultRow into the domain model as needed.
 *             return Employee(
 *                 id = row[EmployeeTable.id],
 *                 firstName = row[EmployeeTable.firstName],
 *                 lastName = row[EmployeeTable.lastName]
 *             )
 *         }
 *     }
 * }
 *```
 *
 * @param T The type of the domain model to transformed into.
 */
public interface IModelTransform<T> {
    /**
     * Transforms a database [ResultRow] into a domain model of type [T].
     *
     * Implement this method to define how raw database data is converted into the domain model.
     *
     * @param row The [ResultRow] retrieved from a database query.
     * @return An instance of [T] representing the transformed domain model.
     */
    public fun from(row: ResultRow): T
}
