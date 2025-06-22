# Spring Security Learning - Step 1: Basic Configuration

This is **Step 1** of the Spring Security learning journey. We're focusing on understanding the basic concepts and configuration of Spring Security.

## 🎯 What You'll Learn

- **Spring Security Architecture**: How Spring Security works under the hood
- **SecurityFilterChain**: The main configuration object
- **URL-based Security**: How to secure different endpoints
- **Authentication vs Authorization**: Understanding the difference
- **Basic User Management**: In-memory users with roles

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   - Application: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console

## 📚 Testing the Security Configuration

### Test Users
The application comes with two pre-configured users:

| Username | Password | Roles |
|----------|----------|-------|
| `user`   | `password` | USER |
| `admin`  | `admin123` | USER, ADMIN |

### Test Endpoints

#### 1. Public Endpoint (No Authentication Required)
```bash
curl http://localhost:8080/public/hello
```
**Expected Result:** JSON response with public information
**Security Level:** No authentication required

#### 2. Private Endpoint (Authentication Required)
```bash
# This will redirect to login page in browser
# Or use basic auth with curl:
curl -u user:password http://localhost:8080/private/hello
```
**Expected Result:** JSON response with user information
**Security Level:** Any authenticated user

#### 3. Admin Endpoint (Admin Role Required)
```bash
# Login as admin first, then access:
curl -u admin:admin123 http://localhost:8080/admin/hello
```
**Expected Result:** JSON response with admin information
**Security Level:** ADMIN role required

#### 4. Root Endpoint (Authentication Required)
```bash
curl -u user:password http://localhost:8080/
```
**Expected Result:** Welcome message with available endpoints
**Security Level:** Any authenticated user

## 🔍 Understanding the Configuration

### SecurityConfig.java
This is the main configuration class that defines:

1. **URL-based Security Rules:**
   ```java
   .authorizeHttpRequests(auth -> auth
       .requestMatchers("/public/**").permitAll()     // No auth required
       .requestMatchers("/admin/**").hasRole("ADMIN") // Admin role required
       .requestMatchers("/private/**").authenticated() // Any auth required
       .anyRequest().authenticated()                  // Default rule
   )
   ```

2. **Authentication Method:**
   ```java
   .formLogin(form -> form
       .loginPage("/login")
       .permitAll()
   )
   ```

3. **User Management:**
   ```java
   @Bean
   public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
       // Creates in-memory users
   }
   ```

### Key Concepts Explained

#### 1. SecurityFilterChain
- The main configuration object for Spring Security
- Defines the entire security configuration
- Uses method chaining for fluent configuration

#### 2. authorizeHttpRequests()
- Configures URL-based authorization rules
- Uses lambda expressions for configuration
- Rules are evaluated in order (most specific first)

#### 3. UserDetailsService
- Defines how users are loaded for authentication
- In this example, uses in-memory users
- In real applications, typically loads from database

#### 4. PasswordEncoder
- BCrypt is used for password hashing
- Automatically handles salt generation
- Resistant to rainbow table attacks

## 🧪 Testing Scenarios

### Scenario 1: Accessing Public Endpoint
1. Open browser to http://localhost:8080/public/hello
2. **Expected:** Should work without login
3. **Learning:** `permitAll()` allows access without authentication

### Scenario 2: Accessing Private Endpoint
1. Open browser to http://localhost:8080/private/hello
2. **Expected:** Redirected to login page
3. **Learning:** `authenticated()` requires login

### Scenario 3: Login as Regular User
1. Go to http://localhost:8080/private/hello
2. Login with `user` / `password`
3. **Expected:** Can access private endpoints, but not admin
4. **Learning:** Role-based access control

### Scenario 4: Login as Admin
1. Login with `admin` / `admin123`
2. Try accessing http://localhost:8080/admin/hello
3. **Expected:** Can access admin endpoints
4. **Learning:** `hasRole("ADMIN")` requires specific role

## 🔧 Configuration Details

### application.yml
- **H2 Database**: In-memory database for development
- **H2 Console**: Available at /h2-console for database inspection
- **Logging**: DEBUG level for Spring Security to see what's happening

### Security Rules Order
Spring Security evaluates rules in order:
1. `/public/**` - permitAll()
2. `/admin/**` - hasRole("ADMIN")
3. `/private/**` - authenticated()
4. `anyRequest()` - authenticated() (default)

**Important:** Order matters! More specific rules should come first.

## 🎯 Key Takeaways

1. **Spring Security is automatically configured** when you add the dependency
2. **SecurityFilterChain** is the main configuration object
3. **URL-based security** is configured with `authorizeHttpRequests()`
4. **Lambda expressions** are used for fluent configuration
5. **Roles vs Authorities** - Spring Security uses both concepts
6. **Password encoding** is important for security

## 🚀 Next Steps

After understanding this basic configuration, you'll be ready for:
- **Step 2**: Custom User Management with Database
- **Step 3**: JWT Authentication
- **Step 4**: OAuth2 Integration

## 🆘 Troubleshooting

### Common Issues

1. **Application won't start:**
   - Check Java version (needs 17+)
   - Verify Maven dependencies are downloaded

2. **Can't access endpoints:**
   - Check security rules in SecurityConfig
   - Verify user credentials
   - Check browser console for errors

3. **Login not working:**
   - Verify user credentials: `user/password` or `admin/admin123`
   - Check if form login is properly configured

### Debug Mode
The application runs with DEBUG logging for Spring Security. Check the console output to see:
- Security filter chain processing
- Authentication attempts
- Authorization decisions

---

**Happy Learning! 🎯**

This step provides the foundation for understanding Spring Security. Make sure you understand each concept before moving to the next step! 