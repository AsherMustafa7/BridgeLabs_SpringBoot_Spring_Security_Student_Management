# Feature 01 — Spring Security Fundamentals

<text>**Branch:** `feature/spring-security-fundamentals`</text>

## 1. Overview

The objective of this feature is to understand the fundamentals of Spring Security before writing custom security configurations.

We will implement and explore these concepts in our existing Student Management REST API.

### Topics Covered

| Topic | What we'll understand |
|---|---|
| Authentication | Who are you? |
| Authorization | What are you allowed to access? |
| Security Filter Chain | How Spring Security intercepts HTTP requests |
| SecurityContext | Where authenticated user's information is stored |
| SecurityContextHolder | How we access the current security context |
| UserDetails | How Spring Security represents a user |
| UserDetailsService | How Spring Security retrieves users |
| PasswordEncoder | How passwords are securely encoded |
| Default Security | What happens when we add the Spring Security dependency |

## 2. Feature Documentation Structure

Instead of putting all technical documentation inside the project-level README, we maintain separate documentation for individual features.

```text
Student-Management/
│
├── docs/
│   └── features/
│       └── 01-spring-security-fundamentals/
│           └── README.md
│
├── src/
├── pom.xml
└── README.md
```

**Why this structure?**

- Each feature has its own documentation.
- We can understand the implementation history of individual features.
- Documentation remains organized even after merging feature branches into `develop`.
- The main README provides an overview of the entire project.

## 3. Authentication vs Authorization

These are the two fundamental concepts behind Spring Security.

### 3.1 Authentication

**Authentication verifies the identity of a user.**

Imagine someone tries to access our Student Management API.

The application needs to establish:

- Who are you?
- What is your username?
- Can you prove your identity using your password?

If the supplied credentials are valid, authentication succeeds.

**Example:**

A user sends a request with:

```text
Username: admin
Password: admin123
```

The application verifies the credentials and establishes the user's identity if they are valid.

### 3.2 Authorization

**Authorization determines what an authenticated user is permitted to do.**

Imagine we have two users:

| User | Permissions |
|---|---|
| Admin | Add, update, delete and view students |
| Student | View student details |

Both users can successfully authenticate.

However, their permissions are different.

For example:

| Request | Admin | Student |
|---|---|---|
| `GET /students` | Allowed | Allowed |
| `POST /students` | Allowed | Forbidden |
| `PUT /students/5` | Allowed | Forbidden |
| `DELETE /students/5` | Allowed | Forbidden |

### 3.3 How Authentication and Authorization Work Together

```text
             HTTP Request
                  |
                  v
            Authentication
                  |
          Are you really who
             you claim to be?
                  |
          +-------+-------+
          |               |
       Invalid           Valid
          |               |
          v               v
       401 Error     Authorization
                          |
                  Are you allowed
                  to perform this?
                          |
                   +------+------+
                   |             |
                 Denied        Allowed
                   |             |
                   v             v
                403 Error    Access Granted
                                  |
                                  v
                           Execute Operation
```

### 3.4 Difference Between 401 and 403

| HTTP Status | Meaning |
|---|---|
| 401 Unauthorized | Authentication has not been successfully established. |
| 403 Forbidden | The user is authenticated but does not have permission to perform the operation. |

**Remember:**

- 401 → You haven't successfully established your identity.
- 403 → Your identity is known, but you aren't allowed to perform the operation.

## 4. Introducing Spring Security

Before writing any custom security configuration, we first introduce Spring Security into our existing Spring Boot application.

### 4.1 Add the Dependency

Open `pom.xml` and add the following dependency inside the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Important:** We don't need to specify a version when using the Spring Boot parent in our POM. Spring Boot manages compatible dependency versions.

### 4.2 Install Dependencies

Run:

```bash
mvn clean install
```

### 4.3 Start the Application

Run:

```bash
mvn spring-boot:run
```

### 4.4 What Happens Automatically?

Something interesting happens when we add the Spring Security dependency.

Spring Boot automatically configures default security.

Without writing any security configuration ourselves, Spring Security:

1. Registers its security filters.
2. Intercepts incoming HTTP requests.
3. Checks whether requests are authenticated.
4. Rejects unauthenticated requests to protected endpoints.
5. Generates a default user and password.

The generated password is printed in the application console.

The default username is:

```text
user
```

The password is generated when the application starts.

**Important:** The generated password is temporary and should not be treated as a permanent application credential.

## 5. Testing Default Spring Security Using Postman

We tested our existing Student Management API to observe the effect of enabling Spring Security.

### 5.1 Experiment 1 — Without Authentication

Send the following request through Postman:

```http
GET http://localhost:8081/students
```

No authentication credentials are provided.

**Observed response:**

```text
401 Unauthorized
```

This confirms that Spring Security is intercepting the request and denying access because authentication has not been established.

### 5.2 Experiment 2 — With Basic Authentication

First, locate the generated security password in the application console.

Look for a message similar to:

```text
Using generated security password: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Now open Postman:

1. Select the **Authorization** tab.
2. Choose **Basic Auth**.
3. Enter the username: `user`.
4. Enter the generated password.
5. Send the GET request again.

**Observed response:**

```text
200 OK
```

The endpoint is now accessible because valid credentials were supplied.

### 5.3 Comparing Both Experiments

| Condition | Authentication | Result |
|---|---|---|
| Without credentials | Not established | 401 Unauthorized |
| With valid Basic Auth credentials | Successful | 200 OK |

This experiment confirms that Spring Security is active in our application.

## 6. What Did We Learn From This Experiment?

We haven't written a single line of custom security configuration.

Yet Spring Security has already:

- Registered security filters.
- Intercepted incoming HTTP requests.
- Checked authentication.
- Rejected unauthenticated requests.
- Allowed access when valid credentials were provided.

This behavior comes from Spring Boot's default security configuration.

The important lesson is that **Spring Security does not require us to manually configure every security component before it starts protecting our application.**

## 7. What Happens Next?

Now that we have observed default Spring Security in action, the next step is to understand how it actually works internally.

Our next topic will be:

### Spring Security Filter Chain

We will investigate:

- What a servlet filter is.
- What the Security Filter Chain does.
- How HTTP requests pass through security filters.
- How authentication is processed.
- How Spring Security decides whether a request can proceed.

We will build this understanding from the ground up rather than memorizing security configurations.

## 8. Key Takeaways

1. Authentication verifies the identity of a user.
2. Authorization determines what an authenticated user can access.
3. HTTP 401 indicates unsuccessful or missing authentication.
4. HTTP 403 indicates insufficient permissions.
5. Adding `spring-boot-starter-security` activates Spring Security's default security configuration.
6. Spring Boot generates a default username and password.
7. Basic Authentication allows us to test access using username and password through Postman.
8. We confirmed that our previously accessible REST endpoints now require authentication.

---

**Feature 01 Progress:** Default Spring Security successfully enabled and tested.

**Next Topic:** Understanding the Spring Security Filter Chain.