# Library Management System

A comprehensive library management system built with Spring Boot and Keycloak integration, featuring role-based access control.

## Features

### Role-Based Access Control
- **Admin**: Full system access, can delete books and orders
- **Librarian**: Can view and edit book status, approve/reject book orders
- **Student/Teacher**: Can view books and rent/return them
- **Owner**: Can request new book orders

### Core Functionality
- Book catalog management
- Book rental system
- Book order requests
- User authentication via Keycloak
- Responsive web interface

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Keycloak server running (configured in docker-compose.yml in parent directory)
- PostgreSQL database (via Docker)

## Setup

### 1. Start Keycloak and Database
```bash
cd ../..
docker-compose up -d
```

### 2. Configure Keycloak Roles
Run the Keycloak POC to create roles and users:
```bash
cd ../keycloak-poc
mvn exec:java
```

This creates:
- Realm roles: admin, librarian, student, teacher, owner
- Clients: library-app, library-api
- Sample users with different roles

### 3. Start the Library Management Application
```bash
cd ../library-management
mvn spring-boot:run
```

The application will be available at: http://localhost:8082

### 4. Start the Library API (Optional)
```bash
cd ../library-api
mvn spring-boot:run
```

The API will be available at: http://localhost:8083

## Sample Users

| Username  | Password  | Role      |
|-----------|-----------|-----------|
| admin     | admin     | Admin     |
| librarian | librarian | Librarian |
| student   | student   | Student   |
| teacher   | teacher   | Teacher   |
| owner     | owner     | Owner     |

## API Endpoints

### Books
- `GET /api/books` - List all books (authenticated users)
- `GET /api/books/{id}` - Get book details
- `GET /api/books/search?query=` - Search books
- `POST /api/books` - Add new book (librarian/admin)
- `PUT /api/books/{id}` - Update book (librarian/admin)
- `DELETE /api/books/{id}` - Delete book (admin only)

### Rentals
- `GET /api/rentals/my` - User's rentals
- `POST /api/rentals/rent/{bookId}` - Rent a book (student/teacher)
- `POST /api/rentals/return/{rentalId}` - Return a book

### Orders
- `GET /api/orders/my` - User's orders (owner)
- `POST /api/orders` - Request new book (owner)
- `GET /api/orders/pending` - Pending orders (librarian/admin)
- `POST /api/orders/{id}/approve` - Approve order (librarian/admin)
- `POST /api/orders/{id}/reject` - Reject order (librarian/admin)

## Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Security**: Keycloak, Spring Security
- **Database**: H2 (development), PostgreSQL (production)
- **Frontend**: Thymeleaf, Bootstrap 5
- **Build Tool**: Maven

## Project Structure

```
library-management/
├── src/main/java/com/example/librarymanagement/
│   ├── LibraryManagementApplication.java
│   ├── config/
│   │   ├── DataLoader.java
│   │   ├── KeycloakJwtAuthenticationConverter.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── BookController.java
│   │   ├── BookOrderController.java
│   │   ├── BookRentalController.java
│   │   └── WebController.java
│   ├── entity/
│   │   ├── Book.java
│   │   ├── BookOrder.java
│   │   └── BookRental.java
│   ├── repository/
│   │   ├── BookRepository.java
│   │   ├── BookOrderRepository.java
│   │   └── BookRentalRepository.java
│   └── service/
│       ├── BookService.java
│       ├── BookOrderService.java
│       └── BookRentalService.java
└── src/main/resources/
    ├── application.properties
    └── templates/
        ├── index.html
        ├── layout.html
        ├── books.html
        ├── manage-books.html
        ├── orders.html
        ├── pending-orders.html
        ├── my-rentals.html
        └── rentals.html
```

## Security Configuration

The application uses JWT tokens from Keycloak for authentication. Role-based access control is implemented using Spring Security's `@PreAuthorize` annotations.

Keycloak configuration:
- Auth Server URL: http://localhost:8081
- Realm: master
- Client ID: library-app
- Public Client: true

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package
java -jar target/library-management-0.0.1-SNAPSHOT.jar
```

### Database Console
When using H2 database, access the console at: http://localhost:8082/h2-console

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.