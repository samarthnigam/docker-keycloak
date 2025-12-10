# Library API

REST API service for the Library Management System, providing programmatic access to library operations.

## Features

- RESTful API for book management
- JWT-based authentication via Keycloak
- Role-based access control
- CRUD operations for books, rentals, and orders

## Endpoints

### Authentication
All endpoints require Bearer token authentication via Keycloak.

### Books API
- `GET /api/books` - List all books
- `GET /api/books/{id}` - Get book by ID
- `GET /api/books/search?query=` - Search books
- `POST /api/books` - Create new book (librarian/admin)
- `PUT /api/books/{id}` - Update book (librarian/admin)
- `DELETE /api/books/{id}` - Delete book (admin)

### Rentals API
- `GET /api/rentals/my` - Get user's rentals
- `POST /api/rentals/rent/{bookId}` - Rent a book
- `POST /api/rentals/return/{rentalId}` - Return a book

### Orders API
- `GET /api/orders/my` - Get user's orders (owner)
- `POST /api/orders` - Create book order (owner)
- `GET /api/orders/pending` - Get pending orders (librarian/admin)
- `POST /api/orders/{id}/approve` - Approve order (librarian/admin)
- `POST /api/orders/{id}/reject` - Reject order (librarian/admin)

## Running

```bash
mvn spring-boot:run
```

API will be available at: http://localhost:8083

## Configuration

Same Keycloak configuration as the main application:
- Client ID: library-api
- Auth Server: http://localhost:8081
- Realm: master