# Library Management System - Complete Setup

This repository contains a complete library management system with Keycloak integration, featuring role-based access control and both web UI and REST API interfaces.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Keycloak      │    │ Library Web App │    │   Library API   │
│   (Port 8081)   │◄──►│   (Port 8082)   │    │   (Port 8083)   │
│                 │    │                 │    │                 │
│ - User Auth     │    │ - Web UI        │    │ - REST API      │
│ - Role Mgmt     │    │ - Book Mgmt     │    │ - Programmatic  │
│ - JWT Tokens    │    │ - Rentals       │    │   Access        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         ▲                       ▲                       ▲
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 ▼
                    ┌─────────────────┐
                    │ PostgreSQL DB   │
                    │   (Port 5432)   │
                    └─────────────────┘
```

## Quick Start

### 1. Start Infrastructure
```bash
cd ../..
docker-compose up -d
```

### 2. Configure Keycloak
```bash
cd poc/keycloak-poc
mvn exec:java
```

### 3. Start Applications

**Terminal 1 - Library Web App:**
```bash
cd poc/library-management
./run.sh
```
Access at: http://localhost:8082

**Terminal 2 - Library API (Optional):**
```bash
cd poc/library-api
./run.sh
```
Access at: http://localhost:8083

## User Roles & Permissions

| Role      | Permissions |
|-----------|-------------|
| **Admin** | Full access: manage books, rentals, orders, delete items |
| **Librarian** | View/edit books, approve/reject orders, view all rentals |
| **Student/Teacher** | View books, rent/return books, view own rentals |
| **Owner** | Request new book orders, view order status |

## Sample Users

| Username  | Password  | Role      | Capabilities |
|-----------|-----------|-----------|--------------|
| admin     | admin     | Admin     | Everything   |
| librarian | librarian | Librarian | Book mgmt, orders |
| student   | student   | Student   | View & rent books |
| teacher   | teacher   | Teacher   | View & rent books |
| owner     | owner     | Owner     | Request books |

## Project Structure

```
poc/
├── keycloak-poc/          # Keycloak setup & configuration
├── library-management/    # Main web application
│   ├── src/main/java/com/example/librarymanagement/
│   │   ├── controller/    # Web controllers
│   │   ├── entity/        # JPA entities
│   │   ├── repository/    # Data repositories
│   │   ├── service/       # Business logic
│   │   └── config/        # Security & configuration
│   └── src/main/resources/templates/  # Thymeleaf templates
└── library-api/           # REST API service
    └── src/main/java/com/example/libraryapi/
```

## Key Features

### 🔐 Security
- JWT-based authentication via Keycloak
- Role-based access control (RBAC)
- Method-level security with `@PreAuthorize`

### 📚 Book Management
- Add, edit, delete books
- Search by title, author, ISBN
- Status tracking (Available, Rented, Reserved)

### 🛒 Rental System
- Rent books with due dates
- Return books
- Rental history tracking
- Overdue management

### 📋 Order System
- Owners can request new books
- Librarians approve/reject requests
- Order status tracking

### 🎨 User Interface
- Responsive Bootstrap design
- Role-based menu visibility
- Intuitive navigation
- Real-time status updates

## API Documentation

### Authentication
Include `Authorization: Bearer <jwt-token>` header in requests.

### Sample API Calls

```bash
# Get all books
curl -H "Authorization: Bearer <token>" http://localhost:8082/api/books

# Rent a book
curl -X POST -H "Authorization: Bearer <token>" \
  http://localhost:8082/api/rentals/rent/1

# Create book order
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"New Book","author":"Author Name","isbn":"1234567890"}' \
  http://localhost:8082/api/orders
```

## Development

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### Building
```bash
# Build all projects
cd poc/library-management && mvn clean package
cd ../library-api && mvn clean package
cd ../keycloak-poc && mvn clean compile
```

### Testing
```bash
# Run tests
cd poc/library-management && mvn test
cd ../library-api && mvn test
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8081-8083 are available
2. **Keycloak connection**: Verify Keycloak is running at http://localhost:8081
3. **Database issues**: Check PostgreSQL container is healthy
4. **Authentication errors**: Ensure users are created with correct roles

### Logs
```bash
# View application logs
cd poc/library-management && mvn spring-boot:run
# Logs will appear in console

# View Keycloak logs
docker logs keycloak
```

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request

## License

MIT License - see individual project READMEs for details.