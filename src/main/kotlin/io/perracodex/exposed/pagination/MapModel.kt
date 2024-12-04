/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.perracodex.exposed.pagination

import org.jetbrains.exposed.sql.ResultRow

/**
 * Contract for mapping a database [ResultRow] into an instance of type [T].
 *
 * Sample usage applying pagination in a query result:
 * ```
 * fun findAll(pageable: Pageable?): Page<Employee> {
 *     return transaction {
 *         EmployeeTable.selectAll().paginate(
 *             pageable = pageable,
 *             map = object : MapModel<Employee> {
 *                 override fun from(row: ResultRow): Employee {
 *                     return Employee(
 *                          id = row[EmployeeTable.id],
 *                          firstName = row[EmployeeTable.firstName],
 *                          lastName = row[EmployeeTable.lastName]
 *                     )
 *                 }
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * Alternatively, can also define a companion object in the domain model
 * to implement [MapModel] and map the results:
 * ```
 * fun findAll(pageable: Pageable?): Page<Employee> {
 *      return transaction {
 *          EmployeeTable.selectAll()
 *              .paginate(pageable = pageable, map = Employee)
 *      }
 * }
 * ```
 * ```
 * data class Employee(
 *     val id: Int,
 *     val firstName: String,
 *     val lastName: String,
 * ) {
 *     companion object : MapModel<Employee> {
 *         override fun from(row: ResultRow): Employee {
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
 * @param T The type of the domain model to map into.
 */
public interface MapModel<T> {
    /**
     * Maps a database [ResultRow] into a domain model of type [T].
     *
     * Implement this method to define how database results are mapped into domain models.
     *
     * @param row The [ResultRow] retrieved from a database query.
     * @return An instance of [T] representing the mapped domain model.
     */
    public fun from(row: ResultRow): T
}
