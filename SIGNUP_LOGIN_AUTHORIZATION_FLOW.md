# UniCycle Authentication & Authorization Flow

Complete documentation of the signup, login, and role-based authorization system.

---

## Table of Contents
1. [Database Schema](#database-schema)
2. [Signup Flow](#signup-flow)
3. [Login Flow](#login-flow)
4. [Authorization Flow](#authorization-flow)
5. [Role Management](#role-management)
6. [JWT Details](#jwt-details)

---

## Database Schema

### Tables

#### `roles` table
```sql
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Seeded with:
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MANAGER'), ('ROLE_ADMIN');
```

#### `users` table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    university VARCHAR(100) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE
);
```

#### `user_roles` (Many-to-Many join table)
```sql
CREATE TABLE user_roles (
    user_id INT REFERENCES users(id),
    role_id INT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### Entity Relationships

```
User (1) ──────M──────> Role
  ↓ (Many-to-Many via user_roles table)
  └─→ UserDetails (Spring Security)
        └─→ GrantedAuthority (roles)
```

---

## Signup Flow

### Step-by-Step Process

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Client makes POST request to /api/v1/auth/signup             │
│    with JSON body:                                              │
│    {                                                             │
│        "firstName": "John",                                     │
│        "lastName": "Doe",                                       │
│        "email": "john@example.com",                            │
│        "password": "SecurePass123!",                           │
│        "university": "RPI"                                      │
│    }                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. AuthController.registerUser() receives request               │
│    - Passes to AuthService.registerUser()                       │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. SignupValidator validates input                              │
│    - Checks email format                                        │
│    - Checks password strength                                   │
│    - Checks for duplicate emails                                │
│    - Throws exception if invalid                                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Fetch default ROLE_USER from database                        │
│    - Query: roleRepository.findByName("ROLE_USER")              │
│    - Result: Role object with id=1, name="ROLE_USER"            │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Create User entity                                           │
│    User(                                                         │
│        firstName = "John",                                      │
│        lastName = "Doe",                                        │
│        email = "john@example.com",                             │
│        passwordHash = encoder.encode("SecurePass123!"),        │
│        university = "RPI",                                      │
│        isVerified = false,                                      │
│        roles = setOf(ROLE_USER)  ← Only ROLE_USER              │
│    )                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. Save User to database (with @Transactional)                 │
│    - userRepository.save(user)                                  │
│    - INSERT into users table                                    │
│    - INSERT into user_roles table: (user_id, role_id)          │
│      Values: (1, 1)  ← user 1 has role 1 (ROLE_USER)           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. Map User entity to UserDto.Readonly DTO                     │
│    - UserMapper.toUserReadonlyDto()                            │
│    - Excludes password hash for security                        │
│    - Returns public-safe user data                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 8. Return HTTP 201 Created with DTO                             │
│    Response:                                                     │
│    {                                                             │
│        "id": 1,                                                  │
│        "firstName": "John",                                     │
│        "lastName": "Doe",                                       │
│        "email": "john@example.com",                            │
│        "university": "RPI"                                      │
│    }                                                             │
└─────────────────────────────────────────────────────────────────┘
```

### Code Flow

```kotlin
// 1. Client calls this endpoint
@PostMapping("/signup")
fun registerUser(@Valid @RequestBody signupRequest: SignupRequest): ResponseEntity<UserDto.Readonly> {
    val userDto = authService.registerUser(signupRequest)
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto)
}

// 2. AuthService processes signup
@Transactional
fun registerUser(request: SignupRequest): UserDto.Readonly {
    // Validate input
    signupValidator.validate(request)
    
    // Get ROLE_USER from database
    val userRole = roleRepository.findByName("ROLE_USER")
        .orElseThrow { RuntimeException("Error: Default Role not found.") }
    
    // Create user with ONLY ROLE_USER (no way to add ADMIN here)
    val user = User(
        firstName = request.firstName,
        lastName = request.lastName,
        email = request.email,
        passwordHash = passwordEncoder.encode(request.password)!!,
        university = request.university,
        roles = setOf(userRole)  // ← Always just ROLE_USER
    )
    
    // Save to database
    val savedUser = userRepository.save(user)
    
    // Return DTO
    return userMapper.toUserReadonlyDto(savedUser)
}
```

### Key Points
- ✅ **All new users get ROLE_USER only** - no exceptions
- ✅ **Password is hashed** using PasswordEncoder before storage
- ✅ **Email is unique** - prevents duplicate accounts
- ✅ **No verification required** to register (is_verified defaults to false)
- ✅ **Cannot elevate to admin** during signup process

---

## Login Flow

### Step-by-Step Process

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Client makes POST request to /api/v1/auth/login              │
│    with JSON body:                                              │
│    {                                                             │
│        "email": "john@example.com",                            │
│        "password": "SecurePass123!"                            │
│    }                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. AuthController.loginUser() receives request                  │
│    - Passes to AuthService.loginUser()                          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. AuthenticationManager validates credentials                  │
│    - authenticationManager.authenticate(                        │
│        UsernamePasswordAuthenticationToken(email, password)     │
│      )                                                           │
│    - Calls UserDetailsService.loadUserByUsername(email)         │
│    - User entity loads from database with eager-loaded roles    │
│    - PasswordEncoder.matches(provided_pwd, stored_hash)         │
│    - If not match → throws BadCredentialsException              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Credentials verified ✓ - Query User from database            │
│    - userRepository.findByEmail(email)                          │
│    - Loads User entity with @ManyToMany roles                   │
│    - FetchType.EAGER loads roles immediately                    │
│    - User now has: id, email, roles=[ROLE_USER]                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Extract roles from User entity                               │
│    - roles = user.roles.map { it.name }.toList()                │
│    - Result: ["ROLE_USER"] or ["ROLE_USER", "ROLE_ADMIN"]       │
│      depending on user's actual roles in database               │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. Generate JWT token                                           │
│    - jwtUtil.generateToken(email, roles)                        │
│    - Creates JWT with claims:                                   │
│      {                                                           │
│          "sub": "john@example.com",  ← subject (email)          │
│          "roles": ["ROLE_USER"],     ← embedded roles           │
│          "iat": 1711270400,          ← issued at                │
│          "exp": 1711306400           ← expiration (10 hours)    │
│      }                                                           │
│    - Signed with HMAC-SHA256 using SECRET_KEY                   │
│    - Encoded to base64url format                                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. Return HTTP 200 OK with LoginResponse                        │
│    Response:                                                     │
│    {                                                             │
│        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",    │
│        "email": "john@example.com",                            │
│        "roles": ["ROLE_USER"]                                   │
│    }                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 8. Client stores JWT (usually in localStorage/sessionStorage)   │
│    - Stores in browser                                          │
│    - Sends with every subsequent request                        │
│    - Header: Authorization: Bearer <token>                      │
└─────────────────────────────────────────────────────────────────┘
```

### Code Flow

```kotlin
// 1. Client calls this endpoint
@PostMapping("/login")
fun loginUser(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
    val loginResponse = authService.loginUser(loginRequest)
    return ResponseEntity.ok(loginResponse)
}

// 2. AuthService processes login
fun loginUser(request: LoginRequest): LoginResponse {
    // Authenticate: loads User from DB, verifies password
    authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken(request.email, request.password)
    )
    
    // Query user again to get roles
    val user = userRepository.findByEmail(request.email)
        .orElseThrow { UsernameNotFoundException("User not found") }
    
    // Extract roles: ["ROLE_USER"] or ["ROLE_USER", "ROLE_ADMIN", ...]
    val roles = user.roles.map { it.name }.toList()
    
    // Generate JWT with roles embedded
    val jwt = jwtUtil.generateToken(user.email, roles)
    
    // Return response with token
    return LoginResponse(jwt, user.email, roles)
}
```

### JWT Structure

```
Header (decoded)
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload (decoded)
{
  "sub": "john@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1711270400,
  "exp": 1711306400
}

Signature
HMACSHA256(base64url(header) + "." + base64url(payload), SECRET_KEY)
```

### Key Points
- ✅ **Password verified** via PasswordEncoder.matches()
- ✅ **Roles extracted from database** (current roles at login time)
- ✅ **JWT is signed** - cannot be tampered with (signature validation will fail)
- ✅ **JWT expires in 10 hours** - must re-login after expiration
- ✅ **Roles embedded in JWT** - for quick reference on client
- ✅ **But roles NOT trusted** - always verified from database on server

---

## Authorization Flow (Subsequent Requests)

### Step-by-Step Process

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Client makes request with JWT                                │
│    GET /api/v1/users/123/profile                               │
│    Headers: {                                                    │
│        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIs..."       │
│    }                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. JwtAuthenticationFilter intercepts request                   │
│    - Spring's filter chain processes request                    │
│    - doFilterInternal() called                                  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. Extract JWT from Authorization header                        │
│    - authorizationHeader = request.getHeader("Authorization")   │
│    - Check if starts with "Bearer "                             │
│    - Extract substring(7): "eyJhbGciOiJIUzI1NiIs..."           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Extract email from JWT                                       │
│    - jwtUtil.extractEmail(jwt)                                  │
│    - Validates JWT signature using SECRET_KEY                   │
│    - Extracts subject claim (email)                             │
│    - Returns: "john@example.com"                                │
│    - If invalid → returns null → filter chain continues         │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Load User from database (UserDetailsService)                 │
│    - userDetailsService.loadUserByUsername(email)               │
│    - UserRepository.findByEmail(email)                          │
│    - Loads User entity with @ManyToMany eager roles             │
│    - User now has current roles from database                   │
│      (NOT from JWT - fresh from DB!)                            │
│    - Returns User entity (which implements UserDetails)         │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. Validate JWT signature & expiration                          │
│    - jwtUtil.validateToken(jwt, email)                          │
│    - Verify signature (HMAC-SHA256)                             │
│    - Check if not expired                                       │
│    - Verify email matches                                       │
│    - Returns: true or false                                     │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. Create Authentication object with UserDetails                │
│    if token is valid:                                           │
│    - UsernamePasswordAuthenticationToken(                       │
│        principal = userDetails (User entity),                   │
│        credentials = null,                                      │
│        authorities = userDetails.getAuthorities()               │
│      )                                                           │
│    - user.getAuthorities() calls:                               │
│        roles.map { SimpleGrantedAuthority(it.name) }            │
│      Result: [GrantedAuthority("ROLE_USER"), ...]               │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 8. Set Authentication in SecurityContext                        │
│    - SecurityContextHolder.getContext().authentication = auth   │
│    - Now Spring Security knows:                                 │
│      - User is authenticated                                    │
│      - User has these authorities/roles                         │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 9. Request continues to controller                              │
│    if @PreAuthorize("hasRole('ADMIN')")                         │
│    - Spring reads SecurityContextHolder                         │
│    - Gets authorities: [ROLE_USER]                              │
│    - Checks if contains "ROLE_ADMIN"                            │
│    - NOT FOUND → throws AccessDeniedException                   │
│    - Returns HTTP 403 Forbidden                                 │
│                                                                  │
│    if @PreAuthorize("hasRole('USER')")                          │
│    - Gets authorities: [ROLE_USER]                              │
│    - Checks if contains "ROLE_USER"                             │
│    - FOUND ✓ → method executes                                  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 10. Controller executes with authenticated user context         │
│    - Can access SecurityContextHolder to get user info          │
│    - Returns HTTP 200 OK with response                          │
└─────────────────────────────────────────────────────────────────┘
```

### Code Flow

```kotlin
// 1. Filter intercepts every request
@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authorizationHeader = request.getHeader("Authorization")
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extract JWT from "Bearer <token>"
            val jwt = authorizationHeader.substring(7)
            
            // Extract email from JWT
            val email = jwtUtil.extractEmail(jwt)
            
            if (email != null && SecurityContextHolder.getContext().authentication == null) {
                // Load User from database (with current roles)
                val userDetails: UserDetails = userDetailsService.loadUserByUsername(email)
                
                // Validate JWT signature & expiration
                if (jwtUtil.validateToken(jwt, userDetails.username)) {
                    // Create authentication with UserDetails authorities
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    
                    // Set in SecurityContext
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response)
    }
}

// 2. User entity's getAuthorities() method
class User(...) : UserDetails {
    var roles: Set<Role> = HashSet()
    
    override fun getAuthorities(): Collection<GrantedAuthority> =
        roles.map { SimpleGrantedAuthority(it.name) }  // ← Database roles!
}

// 3. Controller with authorization check
@RestController
@RequestMapping("/api/users")
class UserController {
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // ← Reads from SecurityContext authorities
    fun deleteUser(@PathVariable id: Long): ResponseEntity<String> {
        // If execution reaches here, user has ROLE_ADMIN
        return ResponseEntity.ok("User deleted")
    }
}
```

### Key Points
- ✅ **JWT is validated on every request** - signature and expiration checked
- ✅ **User is reloaded from database** - ensures current roles are used
- ✅ **Database roles override JWT roles** - true authority source
- ✅ **@PreAuthorize reads from SecurityContext** - which comes from User entity
- ✅ **Real-time authorization** - role changes take effect on next request
- ✅ **Prevents privilege escalation** - user can't modify JWT to add admin role

---

## JWT Details

### JWT Format

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTcxMTI3MDQwMCwiZXhwIjoxNzExMzA2NDAwfQ.signature

Base64url(Header).Base64url(Payload).Signature
```

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "john@example.com",          // Subject (username)
  "roles": ["ROLE_USER"],              // Embedded roles
  "iat": 1711270400,                   // Issued At timestamp
  "exp": 1711306400                    // Expiration timestamp (10 hours)
}
```

### Signature
```
HMACSHA256(
  base64url(header) + "." + base64url(payload),
  SECRET_KEY
)
```

### JWT Generation Code

```kotlin
fun generateToken(email: String, roles: List<String>): String {
    val claims: MutableMap<String, Any> = HashMap()
    claims["roles"] = roles  // Add roles to JWT
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(email)  // Subject is email
        .setIssuedAt(Date(System.currentTimeMillis()))
        .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 hours
        .signWith(Keys.hmacShaKeyFor(SECRET_KEY.toByteArray()), SignatureAlgorithm.HS256)
        .compact()
}
```

### JWT Validation Code

```kotlin
fun validateToken(token: String, email: String): Boolean {
    val extractedEmail = extractEmail(token)
    return extractedEmail == email && !isTokenExpired(token)
}

private fun isTokenExpired(token: String): Boolean {
    return extractExpiration(token).before(Date())
}

private fun extractExpiration(token: String): Date {
    return extractClaim(token) { claims -> claims.expiration }
}
```

### Key Points About JWT in UniCycle
- ✅ **HMAC-SHA256** - Symmetric signing (SECRET_KEY used for both sign and verify)
- ✅ **Roles embedded** - Quick reference on client, but not trusted for authorization
- ✅ **10-hour expiration** - Configured in JwtUtil.kt
- ✅ **Email as subject** - Used as unique identifier
- ✅ **Signature cannot be faked** - Requires SECRET_KEY to sign
- ✅ **Roles can change** - Next request fetches from database

---

## Complete User Lifecycle

### Timeline Example

```
Day 1, 10:00 AM
├─ User registers on website
├─ Signup → User gets ROLE_USER
└─ JWT issued with roles: [ROLE_USER]

Day 1, 10:05 AM
├─ User logs in and browses marketplace
├─ JWT sent with each request
├─ Database checked for current roles: [ROLE_USER] ✓
└─ Access to user endpoints ✓

Day 1, 11:00 AM
├─ Admin promotes user to ROLE_MANAGER
├─ Database updated: roles = [ROLE_USER, ROLE_MANAGER]
└─ User's JWT still has old roles [ROLE_USER]

Day 1, 11:05 AM (User still has old JWT)
├─ User tries to access manager endpoint
├─ @PreAuthorize reads JWT: [ROLE_USER]
├─ Database check: [ROLE_USER, ROLE_MANAGER] ✓
├─ Authorization passes ✓
└─ Access to manager endpoints ✓

Day 1, 11:15 AM (JWT expires after 10 hours)
├─ User must re-login
├─ AuthenticationManager validates credentials ✓
├─ New JWT issued with current roles: [ROLE_USER, ROLE_MANAGER]
└─ User gets new JWT with updated roles

Day 2, 10:10 AM (After 10 hours)
├─ Old JWT expires automatically
├─ User must re-login
├─ New JWT issued
└─ Cannot use old token anymore
```

---

## Security Features Summary

### Authentication
- ✅ **Password Hashing** - PasswordEncoder (BCrypt) hashes passwords before storage
- ✅ **JWT Signature** - HMAC-SHA256 prevents token tampering
- ✅ **Token Expiration** - JWT expires after 10 hours
- ✅ **Unique Emails** - Prevents duplicate accounts

### Authorization
- ✅ **Role-Based Access Control** - @PreAuthorize checks Spring Security authorities
- ✅ **Database is Source of Truth** - User entity roles checked on every request
- ✅ **Cannot Self-Elevate** - Signup always assigns ROLE_USER only
- ✅ **Real-Time Updates** - Role changes take effect immediately on next request
- ✅ **JWT Not Trusted** - Roles embedded in JWT for reference only, not authorization

### Session Management
- ✅ **Stateless** - No server sessions, JWT-based
- ✅ **Automatic Expiration** - Users must re-login every 10 hours
- ✅ **One JWT Per User** - No multiple simultaneous sessions

---

## Troubleshooting

### User can't login
- Check email exists in users table
- Check password hash matches (use passwordEncoder.matches() to verify)
- Check isVerified flag if you enforce it

### User promoted but can't access endpoint
- User must re-login to get new JWT with updated roles
- Old JWT still has old roles until it expires/user logs in again
- Check database for user_roles entry

### @PreAuthorize returns 403 even with correct role
- Check database for correct role entry
- Verify Role.name exactly matches annotation (case-sensitive)
- Ensure user is authenticated (not just any request)

### JWT validation fails
- Check SECRET_KEY is same in signing and validation (application.properties)
- Check token hasn't expired
- Check JWT format is "Bearer <token>"

---

## Configuration Reference

### application.properties

```properties
# JWT Configuration
jwt.secret=your-secret-key-here-should-be-long-and-secure

# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/unicycle
spring.datasource.username=postgres
spring.datasource.password=your-password

# Security
spring.security.enable-method-security=true
```

### Dependencies (build.gradle.kts)

```kotlin
// JWT
implementation("io.jsonwebtoken:jjwt-api:0.11.5")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

// Spring Security
implementation("org.springframework.boot:spring-boot-starter-security")

// Database
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
runtimeOnly("org.postgresql:postgresql")
```

---

## Files Involved

```
src/main/kotlin/com/unicycle/unicycle_backend/
├── config/
│   └── SecurityConfig.kt              ← @EnableMethodSecurity(prePostEnabled=true)
├── features/
│   ├── auth/
│   │   ├── AuthController.kt          ← /api/v1/auth/signup, /login
│   │   ├── AuthService.kt            ← Business logic
│   │   ├── LoginRequest.kt           ← DTO
│   │   └── LoginResponse.kt          ← DTO
│   └── user/
│       ├── User.kt                   ← Entity with roles
│       ├── Role.kt                   ← Role entity
│       ├── UserRepository.kt         ← DB access
│       ├── RoleRepository.kt         ← DB access
│       ├── AdminManagementService.kt ← Role management
│       └── AdminUserController.kt    ← /api/v1/admin/users/*
├── filter/
│   └── JwtAuthenticationFilter.kt    ← Filter chain
└── util/
    └── JwtUtil.kt                    ← JWT creation/validation
```

---

## Next Steps

1. **For role-based endpoints**: Use `@PreAuthorize("hasRole('ROLE_NAME')")`
2. **For role management UI**: Expose AdminUserController endpoints to admin interface
3. **For role changes to take effect**: Ask users to re-login
4. **For production**: Consider additional security (API keys, rate limiting, audit logging)

