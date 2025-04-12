# [Exposed Pagination Library](https://github.com/perracodex/exposed-pagination)

A Kotlin library providing pagination support for the [Exposed](https://github.com/JetBrains/Exposed) ORM framework,
including integration with the [Ktor](https://ktor.io/) server framework.

---

### Features

- **Easy Pagination**: Apply pagination to Exposed queries with a single function call.
- **Sorting Support**: Sort query results based on multiple fields and directions.
- **Page Information**: Access detailed pagination information like total pages, current page index, and more.
- **Ktor Integration**: Extract pagination directives from Ktor requests with a single function call.

**Note: The library is designed to work with Exposed DSL queries. There is no support DAO:**

---

### Installation

Add the library to your project gradle dependencies.

```kotlin
dependencies {
    implementation("io.github.perracodex:exposed-pagination:<VERSION>")
}
```

### Version Compatibility

| **ExposedPagination** | **Exposed** | Ktor  | **Kotlin** |
|-----------------------|-------------|-------|------------|
| 1.0.11                | \>= 0.61.0  | 3.1.2 | \>= 2.1.20 |
| 1.0.10                | \>= 0.59.0  | 3.1.1 | \>= 2.1.10 |

---

### Usage

_See also
the [API reference documentation](https://www.javadoc.io/doc/io.github.perracodex/exposed-pagination/latest/-exposed-pagination/io.perracodex.exposed.pagination/index.html)._

#### Ktor Integration

The library provides an extension function in the ApplicationCall class to obtain pagination and sorting information from a request.
Whenever receiving a request, use the dedicated extension function to extract pagination and sorting information.

`call.getPageable()`

**Example:**

```kotlin
fun Route.findAllEmployees() {
    get("v1/employees") {
        val pageable: Pageable? = call.getPageable() // Get the pagination directives, (if any).
        val employees: Page<Employee> = EmployeeService.findAll(pageable)
        call.respond(status = HttpStatusCode.OK, message = employees) // Respond with a Page object.
    }
}
```

#### Applying Pagination to Queries

Use the `paginate` extension function on your Exposed Query to apply pagination.

```kotlin
fun getAllEmployees(pageable: Pageable?): Page<Employee> {
    return transaction {
        EmployeeTable.selectAll().paginate(
            pageable = pageable,
            map = object : MapModel<Employee> {
                override fun from(row: ResultRow): Employee {
                    return Employee.from(
                        id = row[EmployeeTable.id],
                        firstName = row[EmployeeTable.firstName],
                        lastName = row[EmployeeTable.lastName]
                    )
                }
            }
        )
    }
}
```

#### Mapping Query Results within the Domain Model Companion Object

Alternatively, the model mapping can also be done in the domain model companion objects as follows:

```kotlin
fun getAllEmployees(pageable: Pageable?): Page<Employee> {
    return transaction {
        EmployeeTable.selectAll()
            .paginate(pageable = pageable, map = Employee)
    }
}
```

```kotlin
data class Employee(
    val id: Int,
    val firstName: String,
    val lastName: String,
) {
    companion object : MapModel<Employee> {
        override fun from(row: ResultRow): Employee {
            return Employee(
                id = row[EmployeeTable.id],
                firstName = row[EmployeeTable.firstName],
                lastName = row[EmployeeTable.lastName]
            )
        }
    }
}
```
---

#### Handling 1-to-Many Relationships

For complex queries involving multiple tables and producing 1-to-many relationships,
you can use the `map` overload function to map the query N results to the domain model,
groping by the parent entity.

Example:
Employee with a 1-to-many relationship to N Contact and N Employment records.

```kotlin
fun findAll(pageable: Pageable?): Page<Employee> { 
    return transaction {
        EmployeeTable
            .leftJoin(ContactTable)
            .leftJoin(EmploymentTable)
            .selectAll()
            .paginate(
                pageable = pageable,
                map = Employee,
                groupBy = EmployeeTable.id
            )
    }
}

data class Employee(
    val id: Uuid,
    val firstName: String,
    val lastName: String,
    val contact: List<Contact>,
    val employments: List<Employment>
) {
    companion object : MapModel<Employee> {
        override fun from(row: ResultRow): Employee {
            val firstName: String = row[EmployeeTable.firstName]
            val lastName: String = row[EmployeeTable.lastName]
            return Employee(
                    id = row[EmployeeTable.id],
                    firstName = firstName,
                    lastName = lastName,
                    contact = listOf(),
                    employments = listOf()
            )
        }
    
        override fun from(rows: List<ResultRow>): Employee? {
            if (rows.isEmpty()) {
                    return null
                }
    
            // As we are handling a 1 -> N relationship,
            // we only need the first row to extract the top-level record.
            val topLevelRecord: ResultRow = rows.first()
            val employee: Employee = from(row = topLevelRecord)
    
            // Extract Contacts. Each must perform its own mapping.
            val contact: List<Contact> = rows.mapNotNull { row ->
                    row.getOrNull(ContactTable.id)?.let {
                        // Contact must perform its own mapping.
                        Contact.from(row = row)
                    }
                }
    
            // Extract Employments. Each must perform its own mapping.
            val employments: List<Employment> = rows.mapNotNull { row ->
                    row.getOrNull(EmploymentTable.id)?.let {
                        // Employment must perform its own mapping.
                        Employment.from(row = row)
                    }
                }
    
            return employee.copy(
                    contact = contact,
                    employments = employments
                )
        }
    }
}
```

---

#### Integration with Ktor StatusPages plugin

If using the Ktor [StatusPages](https://ktor.io/docs/server-status-pages.html) plugin, you can handle exceptions thrown by the pagination
library
as follows:

```kotlin
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<PaginationError> { call: ApplicationCall, cause: PaginationError ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "${cause.errorCode} | ${cause.message} | ${cause.reason ?: ""}"
            )
        }

        // Other exception handlers...
    }
}
```

---

### Syntax

You can use HTTP query parameters to control pagination and sorting in your API endpoints.

- **Pagination:** `?page=0&size=10`

- **Sorting:** `?sort=fieldName,direction`

- **Sorting (multiple fields):** `?sort=fieldName_A,direction_A&sort=fieldName_B,direction_B`

**Note:** If no sort directive is specified, it will default to `ASC`.

--- 

### Samples:

Page 0, 10 elements per page:

```bash
GET http://localhost:8080/v1/employees?page=0&size=10
```

Page 5, 24 elements per page, sorted by first name ascending:

```bash
GET http://localhost:8080/v1/employees?page=5&size=24&sort=firstName,asc
```

Page 2, 50 elements per page, sorted by first name ascending and last name descending:

```bash
`GET` http://localhost:8080/v1/employees?page=2&size=50&sort=firstName,asc&sort=lastName,desc
```

No pagination, sorted by first name, default to ascending:

```bash
`GET` http://localhost:8080/v1/employees?sort=firstName
```

---

### Resolving Field Ambiguity

To avoid ambiguity when sorting by multiple fields sharing the same name across different tables,
the field name can be prefixed with the table name separated by a dot.

Syntax: `sort=tableName.fieldName,asc`

```bash
`GET` http://localhost:8080/v1/employees?page=0&size=10&sort=employee.firstName,asc&sort=managers.firstName,desc
```

---

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
