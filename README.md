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
| 1.0.4                 | \>= 0.56.0  | 3.0.2 | \>= 2.1.0  |

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
import io.perracodex.exposed.pagination.*

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
import io.perracodex.exposed.pagination.*

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
import io.perracodex.exposed.pagination.*

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
