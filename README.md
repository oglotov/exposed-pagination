# [Exposed Pagination Library](https://github.com/perracodex/pagination)

A Kotlin library providing pagination support for the [Exposed](https://github.com/JetBrains/Exposed) ORM framework,
with integration to the [Ktor](https://ktor.io/) server framework.

## Features

- **Easy Pagination**: Apply pagination to your Exposed queries with minimal setup.
- **Sorting Support**: Sort query results based on multiple fields and directions.
- **Page Information**: Access detailed pagination information like total pages, current page index, and more.
- **Ktor Integration**: Extract pagination and sorting directives from Ktor requests with a single function call.

## Installation

Add the library to your project gradle dependencies. Make sure to replace `1.0.0` with the latest version.

```kotlin
dependencies {
    implementation("io.github.perracodex:exposed-pagination:1.0.0")
}
```

## Usage

### Ktor Integration

The library provides an extension function in the ApplicationCall class to obtain pagination and sorting information from a request.
Whenever receiving a request, use the dedicated extension function to extract pagination and sorting information.

`call.getPageable()`

**Example:**
```kotlin   
internal fun Route.findAllEmployeesRoute() {
    get("v1/employees") {
        val pageable: Pageable? = call.getPageable() // Get pagination and sorting information, (if any).
        val employees: Page<Employee> = EmployeeService.findAll(pageable = pageable)
        call.respond(status = HttpStatusCode.OK, message = employees)
    }
}
```

### Applying Pagination to Queries

Use the `paginate` extension function on your Exposed Query to apply pagination.

```kotlin
import org.jetbrains.exposed.sql.*
import perracodex.exposed.pagination.paginate

fun findAllEmployees(pageable: Pageable?): Page<Employee> {
    return transaction {
        EmployeeTable.selectAll()
            .paginate(pageable = pageable, mapper = Employee) // Apply pagination to the query.
    }
}
```

### Setting the Query ResultRow Mapper in the Entities

Implement in your entity companion objects the [EntityMapper](./src/main/kotlin/io/perracodex/exposed/pagination/IEntityMapper.kt) interface.
This interface is used by the pagination library to map database ResultRows from a query output to your entity class.

```kotlin
data class Employee(
    val id: Int,
    val firstName: String,
    val lastName: String,
) {
    companion object : IEntityMapper<Employee> { // Implement IEntityMapper interface.
        override fun from(row: ResultRow): Employee {
            // Map the ResultRow to your entity class as needed.
            return Employee(
                id = row[EmployeeTable.id],
                firstName = row[EmployeeTable.firstName],
                lastName = row[EmployeeTable.lastName]
            )
        }
    }
}
```
### Integration with Ktor StatusPages plugin

If using the Ktor [StatusPages](https://ktor.io/docs/server-status-pages.html) plugin, you can handle exceptions thrown by the pagination library
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

## Examples

### Pagination and Sorting

You can use HTTP query parameters to control pagination and sorting in your API endpoints.

### Syntax for pagination and sorting:

- **Pagination:** `?page=0&size=10`

- **Sorting:** `?sort=fieldName,direction`

- **Sorting (multiple fields):** `?sort=fieldName_A,direction_A&sort=fieldName_B,direction_B`

**Note:** If no sort directive is specified, it will default to `ASC`.

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

### Resolving Ambiguity

To avoid ambiguity when sorting by multiple fields sharing the same name across different tables,
the field name can be prefixed with the table name separated by a dot.

Example: `sort=employee.firstName,desc`

```bash
`GET` http://localhost:8080/v1/employees?page=0&size=10&sort=employee.firstName,asc&sort=managers.firstName,desc
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
