# UniCycle-BE: Beginner's Guide

## What is this project?
This is a **backend API** for a student marketplace (like eBay for students at a university). It's the "invisible server" that handles requests from apps/websites.

---

## 🎯 Key Concepts (Simple Explanation)

### 1. **What is Kotlin?**
- It's a programming language, similar to Java but simpler and modern.
- It runs on the JVM (Java Virtual Machine).

### 2. **What is Spring Boot?**
- A framework that makes building web servers super easy.
- Handles database connections, security, web requests automatically.
- Think of it as a "starter kit" for backends.

### 3. **What is an API?**
- A way for your frontend (mobile app, website) to talk to the backend.
- You send a request (e.g., "get user info"), and the backend responds (e.g., with user data).

### 4. **What is PostgreSQL?**
- A database (like a spreadsheet but more powerful).
- Stores user info, listings, transactions, etc.

---

## 📁 Project Structure (Simplified)

```
UniCycle-BE/
├── src/main/kotlin/                    ← Your code goes here
│   ├── UniCycleBackendApplication.kt  ← App starting point
│   ├── TestController.kt               ← A test endpoint (returns "Docker is working!")
│   ├── features/
│   │   ├── auth/                       ← Login/signup logic (mostly empty)
│   │   └── user/                       ← User management (mostly empty)
│   └── config/
│       └── SecurityConfig.kt           ← Security settings (login, permissions)
│
├── src/main/resources/
│   └── db/migration/
│       └── V1__Create_User_And_Roles.sql  ← Database setup (tables for users, roles)
│
├── build.gradle.kts                    ← List of libraries (dependencies) needed
└── Dockerfile & docker-compose.yml     ← For running in containers (optional)
```

---

## 🚀 How to Get Started

### Step 1: Set Up Your Environment
1. Install Java 21 (Spring Boot 4.x requires it)
2. Install PostgreSQL (or use Docker)
3. Open the project in IntelliJ IDEA

### Step 2: Set Up the Database
Create a PostgreSQL database and update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/unicycle
spring.datasource.username=your_postgres_user
spring.datasource.password=your_postgres_password
```

### Step 3: Run the App
In the terminal:
```bash
./gradlew bootRun
```
(Or `gradlew.bat bootRun` on Windows)

The app will start on `http://localhost:8080`

### Step 4: Test It
Visit `http://localhost:8080/test` in your browser or use:
```bash
curl http://localhost:8080/test
```
You should see: `"Docker is working!"`

---

## 📚 What Each Part Does

### `UniCycleBackendApplication.kt` (The Main File)
- This is where the app starts.
- `@SpringBootApplication` tells Spring to set everything up.
- `main()` function runs when you start the app.

### `TestController.kt` (An Endpoint)
- Handles HTTP requests to `/test`.
- `@GetMapping("/test")` = "Listen for GET requests to /test"
- Returns a simple string.
- **Real endpoints** (for users, listings, etc.) will look similar.

### `V1__Create_User_And_Roles.sql` (Database Setup)
- Creates tables:
  - `users` - stores student accounts
  - `roles` - stores permissions (USER, MANAGER, ADMIN)
  - `user_roles` - links users to roles
- Runs automatically when the app starts (thanks to Flyway).

### `SecurityConfig.kt` (Security Settings)
- Controls who can access what endpoints.
- Handles authentication (login).
- Empty/basic right now.

---

## 🔨 Next Steps to Learn

### 1. **Run the App**
- Get it working locally first.
- Visit the `/test` endpoint.

### 2. **Understand a Request Flow**
- Request comes in → Controller handles it → Returns response
- Example: User clicks "Get my profile" → `GET /api/users/me` → Controller queries database → Returns user data

### 3. **Create Your First Endpoint**
- Add a new controller in `features/user/`
- Create endpoints like:
  - `GET /api/users/{id}` - get a user
  - `POST /api/users` - create a user
  - `PUT /api/users/{id}` - update a user

### 4. **Learn Key Concepts**
- **JPA/Hibernate** - how to work with databases in Java/Kotlin
- **REST APIs** - HTTP methods (GET, POST, PUT, DELETE)
- **Spring Boot Annotations** - @RestController, @GetMapping, @Service, etc.

### 5. **Explore Features to Build**
- User authentication (login/signup)
- Product listings
- Shopping cart
- Reviews/ratings

---

## 🐛 Known Issue
There's a package mismatch in `TestController.kt`:
- File location: `com/unicycle/unicycle_backend/`
- But code says: `package com.example.unicycle`

This should be fixed to:
```kotlin
package com.unicycle.unicycle_backend
```

---

## 📖 Useful Resources
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kotlin for Java Developers](https://kotlinlang.org/docs/basic-syntax.html)
- [REST API Basics](https://restfulapi.net/)
- [PostgreSQL Basics](https://www.postgresql.org/docs/current/)

---

## 💡 Quick Commands
```bash
# Build the project
./gradlew build

# Run the app
./gradlew bootRun

# Run tests
./gradlew test

# Clean old builds
./gradlew clean

# Build Docker image (if using Docker)
docker-compose up
```

---

**Ready to start? Run the app and visit `/test` first!** 🎉

