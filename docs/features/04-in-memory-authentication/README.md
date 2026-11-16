# Feature 04 --- Password Encoding & In-Memory Authentication

> **Project:** Student Management System\
> **Feature:** 04 --- PasswordEncoder and In-Memory Authentication

## 1. Objectives

This feature introduces password hashing, Spring Security's user
representation, an in-memory user store, HTTP Basic authentication, and
the distinction between roles and authorities.

By the end, you should understand:

-   `PasswordEncoder` and `BCryptPasswordEncoder`
-   `UserDetails` and `UserDetailsService`
-   `InMemoryUserDetailsManager`
-   The HTTP Basic authentication flow
-   Authentication versus authorization
-   `hasRole()` versus `hasAuthority()`
-   How to test successful and failed authentication in Postman

------------------------------------------------------------------------

## 2. Password encoding

Passwords should never be stored as plain text. Instead, the application
stores a password hash produced by a password-hashing algorithm.

``` mermaid
flowchart TD
    A["Raw password"] --> B["BCryptPasswordEncoder"]
    B --> C["BCrypt encoded hash"]
    C --> D["Store encoded value"]
```

BCrypt is a one-way password-hashing algorithm, not reversible
encryption. During login, the application does not decode the stored
value. It checks whether the submitted password matches the stored hash.

`PasswordEncoder` is a Spring Security interface with three important
methods:

  -----------------------------------------------------------------------------
  Method                                    Purpose
  ----------------------------------------- -----------------------------------
  `encode(rawPassword)`                     Encodes a raw password.

  `matches(rawPassword, encodedPassword)`   Checks whether a raw password
                                            matches an encoded password.

  `upgradeEncoding(encodedPassword)`        Checks whether an existing encoding
                                            should be upgraded.
  -----------------------------------------------------------------------------

`BCryptPasswordEncoder` is a concrete implementation.

### Add the PasswordEncoder bean

In your existing `Config/SecurityConfig.java`, add:

``` java
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
```

``` java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Do not replace your existing `SecurityFilterChain` methods. Add this
bean inside the existing configuration class.

-   `@Bean` registers the returned object in Spring's application
    context.
-   `PasswordEncoder` is the interface type.
-   `BCryptPasswordEncoder` is the concrete implementation.
-   Other Spring-managed components can inject this bean.

This is the same Java polymorphism principle used when returning a
`SecurityFilterChain` implementation through its interface type.

------------------------------------------------------------------------

## 3. `UserDetails`

Spring Security needs a standard representation of a user. The
`UserDetails` interface represents security-related information such as:

-   Username
-   Encoded password
-   Granted authorities
-   Whether the account is enabled
-   Whether the account is locked
-   Whether the account or credentials have expired

Spring Security provides a `User` class that implements `UserDetails`.

``` java
UserDetails admin = User.builder()
        .username("asher")
        .password(passwordEncoder.encode("admin123"))
        .roles("ADMIN")
        .build();
```

  -----------------------------------------------------------------------
  Builder method                      Purpose
  ----------------------------------- -----------------------------------
  `username()`                        Sets the username.

  `password()`                        Sets the encoded password.

  `roles()`                           Assigns roles and automatically
                                      adds the `ROLE_` prefix.

  `authorities()`                     Assigns exact authority strings
                                      without automatically adding
                                      `ROLE_`.

  `build()`                           Constructs the `UserDetails`
                                      object.
  -----------------------------------------------------------------------

For example, `.roles("ADMIN")` creates the authority `ROLE_ADMIN`. In
contrast, `.authorities("STUDENT_READ")` assigns exactly `STUDENT_READ`.

------------------------------------------------------------------------

## 4. `InMemoryUserDetailsManager`

Creating `UserDetails` objects is not enough; Spring Security also needs
a way to retrieve them.

`InMemoryUserDetailsManager` is an in-memory implementation of
`UserDetailsManager` and `UserDetailsService`. It stores user details in
application memory and can load a user by username.

``` mermaid
flowchart TD
    A["UserDetails: Asher"] --> D["InMemoryUserDetailsManager"]
    B["UserDetails: Richa"] --> D
    C["UserDetails: Guest"] --> D
    D --> E["Implements UserDetailsService"]
    E --> F["Spring Security loads user by username"]
```

These users are not persisted to a database. They are lost when the
application stops.

### Add the user-details bean

Add these imports:

``` java
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
```

Then add this bean below `passwordEncoder()`:

``` java
@Bean
public UserDetailsService userDetailsService(
        PasswordEncoder passwordEncoder) {

    UserDetails admin = User.builder()
            .username("asher")
            .password(passwordEncoder.encode("admin123"))
            .roles("ADMIN")
            .build();

    UserDetails student = User.builder()
            .username("richa")
            .password(passwordEncoder.encode("student123"))
            .roles("USER")
            .build();

    UserDetails guest = User.builder()
            .username("guest")
            .password(passwordEncoder.encode("guest123"))
            .roles("USER")
            .build();

    return new InMemoryUserDetailsManager(
            admin,
            student,
            guest
    );
}
```

What happens:

1.  Spring injects the `PasswordEncoder` bean.
2.  Three `UserDetails` objects are constructed.
3.  Each example password is encoded using BCrypt.
4.  Each user receives a role.
5.  The users are passed to `InMemoryUserDetailsManager`.
6.  Spring registers the manager as the `UserDetailsService` bean.

> These are demonstration credentials only. Do not reuse real passwords
> or commit real credentials to a public repository.

------------------------------------------------------------------------

## 5. Testing in-memory authentication with Postman

The configured users are:

  Username   Example password   Role    Granted authority
  ---------- ------------------ ------- -------------------
  `asher`    `admin123`         ADMIN   `ROLE_ADMIN`
  `richa`    `student123`       USER    `ROLE_USER`
  `guest`    `guest123`         USER    `ROLE_USER`

Use the existing endpoint:

``` text
GET http://localhost:8081/students/security-info
```

In Postman, open **Authorization**, select **Basic Auth**, enter the
credentials, and send the request.

### Authentication test cases

With the original configuration from the previous feature, GET requests
to `/students/**` are permitted. Therefore, a GET request without
credentials can still reach the controller as an anonymous user.

  -----------------------------------------------------------------------
  Test                    Credentials             Expected result under
                                                  the initial GET
                                                  `permitAll()` rule
  ----------------------- ----------------------- -----------------------
  Admin authentication    `asher` / `admin123`    200 if the controller
                                                  request succeeds;
                                                  authenticated as Asher

  Student authentication  `richa` / `student123`  200 if the controller
                                                  request succeeds;
                                                  authenticated as Richa

  Incorrect password      `asher` /               401 Unauthorized
                          `wrongpassword`         

  Unknown username        `unknown` /             401 Unauthorized
                          `anything123`           

  No authentication       No Auth                 GET is permitted;
                                                  controller sees
                                                  anonymous
                                                  authentication
  -----------------------------------------------------------------------

Successful authentication may produce:

``` text
Username: asher
Authorities: [ROLE_ADMIN]
Authenticated: true
```

For Richa:

``` text
Username: richa
Authorities: [ROLE_USER]
Authenticated: true
```

Without credentials:

``` text
Username: anonymousUser
Authorities: [ROLE_ANONYMOUS]
Authenticated: true
```

**Important:** `Authentication.isAuthenticated()` can be `true` for an
anonymous authentication token. It does not, by itself, prove that a
real user has logged in.

### HTTP Basic authentication flow

``` mermaid
sequenceDiagram
    participant P as Postman
    participant B as BasicAuthenticationFilter
    participant AM as AuthenticationManager
    participant AP as AuthenticationProvider
    participant U as UserDetailsService
    participant PE as PasswordEncoder
    participant SC as SecurityContextHolder

    P->>B: HTTP request with Basic credentials
    B->>B: Extract username and password
    B->>AM: Authenticate request
    AM->>AP: Delegate authentication
    AP->>U: Load user by username
    U-->>AP: UserDetails
    AP->>PE: matches(raw password, stored hash)
    PE-->>AP: Match result
    AP-->>AM: Authentication result
    AM-->>B: Authenticated result
    B->>SC: Store authentication in SecurityContext
    B-->>P: Continue request processing
```

If the username is unknown or the password does not match,
authentication fails and HTTP Basic failure handling returns an
unauthorized response.

------------------------------------------------------------------------

## 6. Roles versus authorities

A role is represented internally as a granted authority. Spring
Security's role convention uses the `ROLE_` prefix.

  -----------------------------------------------------------------------------------------
  User configuration               Authorization check              What is checked
  -------------------------------- -------------------------------- -----------------------
  `.roles("ADMIN")`                `hasRole("ADMIN")`               `ROLE_ADMIN`

  `.authorities("ROLE_ADMIN")`     `hasAuthority("ROLE_ADMIN")`     `ROLE_ADMIN`

  `.authorities("STUDENT_READ")`   `hasAuthority("STUDENT_READ")`   `STUDENT_READ`
  -----------------------------------------------------------------------------------------

`hasRole("ADMIN")` applies the `ROLE_` prefix by convention.
`hasAuthority("ROLE_ADMIN")` checks the exact authority string.

### Practical experiment

Temporarily add this specific authorization rule:

``` java
.requestMatchers(HttpMethod.GET, "/students/security-info")
    .hasRole("ADMIN")
```

Place it **before** the broader GET matcher:

``` java
.requestMatchers(HttpMethod.GET, "/students/**")
    .permitAll()
```

Otherwise, the broader matcher may match first and permit the request
before the specific rule is evaluated.

Now compare it with:

``` java
.requestMatchers(HttpMethod.GET, "/students/security-info")
    .hasAuthority("ROLE_ADMIN")
```

Both should enforce the same authority requirement when the user has
`ROLE_ADMIN`.

### Expected results for the ADMIN-only experiment

  Request                Expected result
  ---------------------- ----------------------------------------
  Asher (`ROLE_ADMIN`)   200 if the controller request succeeds
  Richa (`ROLE_USER`)    403 Forbidden
  No authentication      401 Unauthorized challenge

-   Asher is authenticated and has `ROLE_ADMIN`, so the rule allows the
    request.
-   Richa is authenticated but lacks `ROLE_ADMIN`, so access is denied
    with 403.
-   An anonymous caller is not authenticated for this protected rule, so
    Spring Security starts authentication, normally returning 401.

After the experiment, restore the intended authorization rules.

------------------------------------------------------------------------

## 7. Important concepts to retain

### Password encoding is not encryption

BCrypt hashes passwords; it does not provide a reversible way to recover
the raw password. Authentication verifies a submitted password with
`matches()`.

### `UserDetails` is Spring Security's user representation

It contains the username, encoded password, authorities, and
account-status information used by Spring Security.

### `UserDetailsService` loads a user

Spring Security uses it to retrieve user details by username.
`InMemoryUserDetailsManager` is one implementation. A later feature can
replace it with a database-backed implementation.

### Authentication and authorization are different

-   **Authentication:** verifies the user's identity.
-   **Authorization:** decides whether that user can perform the
    requested operation.

### 401 and 403 are different

-   **401 Unauthorized:** the request lacks valid authentication for a
    protected resource.
-   **403 Forbidden:** the caller is authenticated but does not have
    sufficient permission.

### Authentication does not imply a session

HTTP Basic can authenticate requests using credentials sent in the
`Authorization` header. Successful authentication does not inherently
mean an HTTP session must be created.

------------------------------------------------------------------------

## 8. Feature completion checklist

-   [ ] Add the `PasswordEncoder` bean.
-   [ ] Confirm the application starts.
-   [ ] Add the `UserDetailsService` bean.
-   [ ] Create the Asher, Richa, and Guest in-memory users.
-   [ ] Confirm passwords are encoded with BCrypt.
-   [ ] Test valid Admin credentials in Postman.
-   [ ] Test valid User credentials in Postman.
-   [ ] Test an incorrect password.
-   [ ] Test an unknown username.
-   [ ] Test a GET request without authentication under the initial
    `permitAll()` rule.
-   [ ] Compare `hasRole("ADMIN")` with `hasAuthority("ROLE_ADMIN")`.
-   [ ] Restore the intended authorization configuration after the
    experiment.
-   [ ] Review and commit the completed feature using the repository's
    Git workflow.

------------------------------------------------------------------------

## 9. Suggested Git workflow

Follow the branch naming and merge conventions already established in
this repository. If the feature branch has not yet been created, a
possible name is:

``` text
feature/04-password-encoder-inmemory-authentication
```

After implementation and testing:

``` powershell
git status
git add .
git commit -m "Add password encoding and in-memory authentication"
```

Then merge the feature branch into `develop` according to the workflow
used in this repository.

Do not commit actual secrets or real user passwords.

------------------------------------------------------------------------

## 10. What's next?

The next feature can move beyond hardcoded in-memory users and explore a
custom `UserDetailsService` and authentication customization. This
provides the foundation for loading users from persistent storage and
later implementing database-backed authentication.

------------------------------------------------------------------------

## Mermaid diagram preview

The diagrams above use fenced Mermaid blocks. GitHub renders Mermaid in
Markdown. In VS Code, use a Markdown preview extension that supports
Mermaid if the built-in preview does not render it. Open the preview
with `Ctrl + Shift + V`. If a diagram appears as plain code, the preview
renderer does not support Mermaid; the Markdown source is still
correctly formatted.
