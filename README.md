📌 Task Management API (Spring Boot + JWT)

This project is a small REST API built with Spring Boot that provides:

✅ User authentication & authorization (JWT-based)

✅ User registration & login with hashed passwords (BCrypt)

✅ Secure task management (CRUD for user-specific tasks)

✅ Global exception handling for clean error responses

✅ Unit tests for controller endpoints

✅ H2 in-memory database for quick setup (no external DB required)

🚀 Features

Authentication

POST /auth/register → Register a new user

POST /auth/login → Login & receive JWT access token

POST /auth/logout → Logout (invalidate token or rely on expiration)

Task Management (requires JWT token in Authorization header)

POST /tasks → Create a new task

GET /tasks → Fetch all tasks for logged-in user

PUT /tasks/{id} → Update a task (e.g., status)

DELETE /tasks/{id} → Delete a task

🛠️ Tech Stack

Java 17

Spring Boot 3.x

Spring Security (JWT Authentication)

H2 Database (in-memory)

BCrypt Password Encoder

JUnit & Mockito (Unit Tests)

⚙️ Setup & Run
1. Clone the repository
git clone https://github.com/<your-username>/task-management-api.git
cd task-management-api

2. Build & run

Using Maven:

mvn clean install
mvn spring-boot:run


Or run directly from IDE.

3. H2 Database Console

URL: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb

Username: sa

Password: (leave empty)

🔑 Authentication Flow

Register a new user:

POST /auth/register
{
  "email": "user@example.com",
  "password": "mypassword",
  "name": "John Doe"
}


Login to receive JWT:

POST /auth/login
{
  "email": "user@example.com",
  "password": "mypassword"
}


Response:

{
  "accessToken": "eyJhbGciOiJIUzI1..."
}


Use JWT for task endpoints:

Authorization: Bearer <token>

📌 Example Requests
Create Task
POST /tasks
{
  "title": "Finish Spring Boot Assignment",
  "description": "Complete API with JWT and task management",
  "status": "OPEN"
}

Fetch Tasks
GET /tasks
Authorization: Bearer <your-token>


Response:

[
  {
    "id": "123",
    "title": "Finish Spring Boot Assignment",
    "description": "Complete API with JWT and task management",
    "status": "OPEN"
  }
]

🧪 Running Tests
mvn test

📂 Project Structure
src/main/java/com/assignment/task
│── controller   # REST Controllers
│── dto          # DTO classes
│── model        # Entity classes
│── repository   # Spring Data JPA Repositories
│── security     # JWT + Security Config
│── service      # Business logic
│── exception    # Global Exception Handling

✅ Evaluation Checklist

 REST endpoints with correct status codes

 JWT-based authentication

 Password hashing with BCrypt

 CRUD operations for tasks

 Global exception handling

 Unit tests for controller

 MongoDB to store in DB

 Clean project structure

📜 License

MIT License – Free to use, modify, and distribute.
