# Feature 02 — Servlet Filters: Fundamentals, Implementation and Lifecycle

**Branch:** `feature/servlet-filters`

## 1. Overview

Before understanding the internal architecture of Spring Security, we need to understand the technology it is built upon: **Java Servlet Filters**.

Spring Security uses multiple Servlet Filters to handle authentication, authorization, exception handling, and other security-related operations.

In this feature, we explore Servlet Filters from the ground up, implement our own filters, register them using different mechanisms, and observe how they process HTTP requests.

### Topics Covered

| Topic | What we'll understand |
|---|---|
| Servlet Filter | What a filter is and why we need it |
| Filter Interface | How to implement a custom filter |
| FilterChain | How a request moves between filters |
| ServletRequest and ServletResponse | Objects representing incoming requests and outgoing responses |
| FilterConfig | How filters receive initialization parameters |
| Filter Registration | How Tomcat discovers and registers filters |
| `@WebFilter` | Servlet annotation-based registration |
| `@ServletComponentScan` | How Spring Boot discovers Servlet components |
| `FilterRegistrationBean` | Spring Boot-based filter registration |
| Multiple Filters | How multiple filters process the same request |
| Filter Ordering | How execution order is controlled |
| Filter Lifecycle | Understanding `init()`, `doFilter()` and `destroy()` |
| Filter Thread Safety | Why one filter instance must handle concurrent requests carefully |

---

# Part 1 — Understanding Servlet Filters

## 2. The Problem: Why Do We Need Filters?

Consider our existing Student Management API.

We have controller endpoints such as:

```java
@GetMapping("/students")
public ResponseEntity<List<Student>> getAllStudents() {
    return ResponseEntity.ok(studentService.getAllStudents());
}
```

When Postman sends:

```http
GET http://localhost:8081/students
```

The request eventually reaches our `StudentController`.

But what if we want to perform some operations before the controller receives the request?

For example:

- Check whether the user is authenticated.
- Check whether the user has permission.
- Log incoming HTTP requests.
- Check whether a request contains a particular header.
- Reject requests that don't satisfy certain conditions.

We could implement these checks inside every controller method.

However, imagine having 30 controllers and 100 endpoints.

Would we want to write authentication and logging logic repeatedly?

No. That would create unnecessary duplication and violate the principle of Separation of Concerns.

This is where Servlet Filters become useful.

## 3. What Is a Servlet Filter?

A **Servlet Filter** is a Java component that intercepts HTTP requests and responses before they reach their destination or before the response is returned to the client.

Think of it as a security checkpoint at the entrance of a building.

Before someone enters:

1. Security inspects the person.
2. Their credentials may be checked.
3. They are either allowed to proceed or denied entry.

A Servlet Filter works similarly.

It gets an opportunity to inspect and process an HTTP request before passing it to the next component.

**Important:** A filter doesn't necessarily reject requests. It can also inspect, modify, log, or otherwise process requests and responses.

### Simplified Request Flow

```text
Client / Postman
       |
       | HTTP Request
       v
Tomcat Servlet Container
       |
       v
     Filter
       |
       | Performs checks
       |
       +--------+
       |        |
     Allow    Reject
       |        |
       v        v
Dispatcher    Stop
 Servlet     Request
       |
       v
StudentController
```

The filter gets an opportunity to execute before the controller.

If it terminates the request, normal downstream processing does not continue.

## 4. What Is the Filter Interface?

Java provides an interface called:

```java
jakarta.servlet.Filter
```

It allows us to create our own Servlet Filters.

Since our project uses Spring Boot 4 and Jakarta Servlet APIs, we use the `jakarta.servlet` package rather than the older `javax.servlet` package.

A class that implements `Filter` can define how incoming requests and outgoing responses should be processed.

### The Three Important Filter Methods

```java
public class MyCustomFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Filter initialized");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        System.out.println("Request received");

        chain.doFilter(request, response);

        System.out.println("Response completed");
    }

    @Override
    public void destroy() {
        System.out.println("Filter destroyed");
    }
}
```

We will understand the lifecycle methods in detail in Part 5.

For now, the most important method is `doFilter()`.

## 5. Understanding FilterChain

Java provides an interface called:

```java
jakarta.servlet.FilterChain
```

It represents the chain of filters through which a request travels.

A filter can invoke:

```java
chain.doFilter(request, response);
```

This passes processing to the next filter in the chain or, eventually, to the target Servlet.

### Example: Three Filters

Imagine the following filters:

- Filter A
- Filter B
- Filter C

The request travels like this:

```text
HTTP Request
     |
     v
  Filter A
     |
     v
  Filter B
     |
     v
  Filter C
     |
     v
DispatcherServlet
     |
     v
Controller
```

When Filter A invokes:

```java
chain.doFilter(request, response);
```

It essentially tells the Servlet container:

"I'm done processing this request. Let the next component handle it."

Filter B then executes and can do the same thing.

### What Happens If We Don't Call `chain.doFilter()`?

The request does not continue through normal downstream processing.

For example:

```java
@Override
public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
) throws IOException, ServletException {

    boolean userIsAuthenticated = true;

    if (userIsAuthenticated) {
        chain.doFilter(request, response);
    } else {
        // Reject the request.
        // Do not continue the filter chain.
    }
}
```

Here:

- If the condition is true, processing continues.
- Otherwise, the filter can terminate the chain and return an appropriate response.

This mechanism is particularly important in Spring Security.

### An Important Detail: Before and After `chain.doFilter()`

Consider:

```java
System.out.println("Before Controller");

chain.doFilter(request, response);

System.out.println("After Controller");
```

Execution happens in this order:

1. The first statement executes.
2. `chain.doFilter()` passes control downstream.
3. Other filters and the controller execute.
4. Downstream processing returns.
5. The final statement executes.

For example:

```text
Filter A - Before
Filter B - Before
Student Controller Executed
Filter B - After
Filter A - After
```

The `After` statements appear in reverse order because execution returns through the filters that previously passed control downstream.

This is often described as a nested or stack-like execution pattern.

**Remember:** Code before `chain.doFilter()` processes the incoming request. Code after it executes when downstream processing returns, allowing us to perform response-side processing.

---

# Part 2 — Implementing Our First Servlet Filter

## 6. Creating MyCustomFilter

We implemented our first custom filter inside the existing Security package.

Project location:

```text
src/main/java/com/asher/Student_management/
│
└── Security/
    └── MyCustomFilter.java
```

### Implementation

```java
package com.asher.Student_management.Security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

public class MyCustomFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Filter initialized");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        System.out.println("Request received");

        chain.doFilter(request, response);

        System.out.println("Response completed");
    }

    @Override
    public void destroy() {
        System.out.println("Filter destroyed");
    }
}
```

### Understanding the Parameters

| Parameter | Meaning |
|---|---|
| `ServletRequest request` | Represents the incoming request |
| `ServletResponse response` | Represents the outgoing response |
| `FilterChain chain` | Allows processing to continue to the next component |

Notice that these are Servlet API types, not Spring-specific classes.

### Why Does Implementing Filter Alone Not Work?

An important distinction:

**Implementing the `Filter` interface does not automatically register the filter with Tomcat.**

We need to register it so the Servlet container knows that it should invoke our filter for matching requests.

This brings us to filter registration.

---

# Part 3 — Registering Servlet Filters

## 7. Registration Method 1: Using `@WebFilter`

The Servlet API provides an annotation:

```java
@WebFilter("/*")
```

We can use it to declare a Servlet Filter and specify its URL mapping.

Example:

```java
package com.asher.Student_management.Security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

@WebFilter("/*")
public class MyCustomFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        System.out.println("MyCustomFilter - Before");

        chain.doFilter(request, response);

        System.out.println("MyCustomFilter - After");
    }
}
```

### What Does `@WebFilter` Do?

It declares that the class is a Servlet Filter and provides its URL mapping.

For example:

```java
@WebFilter("/*")
```

means the filter is mapped to all URL paths.

However, Spring Boot must discover the Servlet annotation and register the component.

## 8. What Is `@ServletComponentScan`?

This was one of the questions we encountered during implementation.

**Question: Do we need to add `@ServletComponentScan` for `@WebFilter` to work?**

Yes, in our Spring Boot setup, we use `@ServletComponentScan` to enable discovery of Servlet components annotated with `@WebFilter`, `@WebServlet`, and `@WebListener`.

Add it to the main application class.

### Spring Boot 4 Implementation

For our Spring Boot 4 project, the import is:

```java
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
```

The main application class becomes:

```java
package com.asher.Student_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                StudentManagementApplication.class,
                args
        );
    }
}
```

### How Does It Work?

1. `@WebFilter` declares a Servlet Filter.
2. `@ServletComponentScan` enables discovery of Servlet components.
3. Spring Boot discovers the annotated filter.
4. The embedded Tomcat container registers it.
5. Tomcat can now invoke it for matching requests.

### Important: Spring Boot Version Differences

In older Spring Boot versions, the annotation was imported using:

```java
import org.springframework.boot.web.servlet.ServletComponentScan;
```

In our Spring Boot 4 project, we use:

```java
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
```

This was the reason our original import could not be resolved.

**Lesson:** When working with a new major Spring Boot version, don't blindly reuse imports from older tutorials. Package locations can change.

### When Is `@ServletComponentScan` Not Required?

When registering a filter through Spring Boot's `FilterRegistrationBean`, we do not need `@ServletComponentScan` for that filter.

These are two different registration mechanisms.

---

## 9. Registration Method 2: Using FilterRegistrationBean

After implementing our first filter using `@WebFilter`, we decided to create a second filter using the Spring Boot registration mechanism.

The purpose was to compare both approaches and observe how multiple filters execute for the same request.

### Our Question

**Does `FilterRegistrationBean` also require `@ServletComponentScan`?**

No.

`FilterRegistrationBean` is a Spring Boot registration mechanism. It registers the filter through a Spring configuration bean.

It does not depend on `@ServletComponentScan`.

### Comparing Both Methods

| `@WebFilter` | `FilterRegistrationBean` |
|---|---|
| Servlet API annotation | Spring Boot registration mechanism |
| Uses `@ServletComponentScan` for discovery in our setup | Does not require `@ServletComponentScan` |
| Uses annotation attributes for URL mapping | Uses methods such as `addUrlPatterns()` |
| Declares a Servlet component | Registers a filter through Spring configuration |
| Uses Servlet annotation-based registration | Uses Spring Boot configuration |

## 10. Creating a Second Filter

We created:

```text
Security/
├── MyCustomFilter.java
├── SecondCustomFilter.java
└── FilterConfiguration.java
```

The first filter uses `@WebFilter`.

The second filter uses `FilterRegistrationBean`.

### SecondCustomFilter.java

```java
package com.asher.Student_management.Security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

public class SecondCustomFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Second Filter initialized");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        System.out.println("Second Filter - Before");

        chain.doFilter(request, response);

        System.out.println("Second Filter - After");
    }

    @Override
    public void destroy() {
        System.out.println("Second Filter destroyed");
    }
}
```

Notice that we haven't used `@WebFilter` here.

The class simply implements the Servlet `Filter` interface.

We will register it through a Spring configuration class.

## 11. Registering the Second Filter

Create:

`FilterConfiguration.java`

```java
package com.asher.Student_management.Security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean<SecondCustomFilter> secondFilter() {

        FilterRegistrationBean<SecondCustomFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(new SecondCustomFilter());

        registration.addUrlPatterns("/students/*");

        registration.setName("secondCustomFilter");

        registration.setOrder(2);

        return registration;
    }
}
```

### Understanding the Important Methods

| Method | Responsibility |
|---|---|
| `setFilter()` | Specifies which filter should be registered |
| `addUrlPatterns()` | Specifies the URL patterns the filter applies to |
| `setName()` | Assigns a name to the filter registration |
| `setOrder()` | Sets its ordering value relative to other ordered registrations |

### Understanding `addUrlPatterns()`

We used:

```java
registration.addUrlPatterns("/students/*");
```

This maps the filter to matching student URL paths.

For example:

```text
/students
/students/1
/students/5
```

The exact matching behavior follows Servlet URL-pattern rules.

### Understanding `setOrder()`

We used:

```java
registration.setOrder(2);
```

A smaller order value generally means earlier execution among registrations whose ordering is controlled by Spring Boot.

**Important:** Our first filter is registered using `@WebFilter`, while the second is registered using `FilterRegistrationBean`.

Their relative ordering is not guaranteed simply by setting `setOrder(2)` on the second registration.

We therefore inspect the actual execution order instead of assuming it.

---

## 12. Our Two-Filter Experiment

We now have two filters:

| Filter | Registration mechanism |
|---|---|
| `MyCustomFilter` | `@WebFilter` |
| `SecondCustomFilter` | `FilterRegistrationBean` |

Our intended experiment:

```text
Postman
   |
   | GET /students/1
   v
MyCustomFilter
   |
   v
SecondCustomFilter
   |
   v
DispatcherServlet
   |
   v
StudentController
```

The diagram illustrates the intended comparison, not a guaranteed ordering between the two registration mechanisms.

### Testing the Filters

1. Save both filter classes.
2. Save `FilterConfiguration`.
3. Keep `@ServletComponentScan` on the main application class for `MyCustomFilter`.
4. Start the application:

```bash
mvn spring-boot:run
```

5. Open Postman.
6. Send:

```http
GET http://localhost:8081/students/1
```

7. Use the existing Basic Authentication credentials because Spring Security is still enabled.

### What Did We Observe?

Both filters worked successfully.

We could observe their messages in the console, along with the request processing and database activity.

This experiment demonstrated:

- Two different registration mechanisms can be used in the same application.
- Multiple filters can process the same request.
- Filters can perform work before and after downstream processing.
- Registration order matters when determining filter execution.
- The actual execution order should be verified rather than assumed.

### What If a Filter Does Not Call `chain.doFilter()`?

We also explored how a filter can stop request processing.

If a filter does not invoke:

```java
chain.doFilter(request, response);
```

the normal downstream chain does not continue.

This means the controller may never receive the request.

That is the fundamental mechanism through which filters can reject requests before they reach Spring MVC.

---

# Part 4 — Understanding the Filter Lifecycle

## 13. The Three Lifecycle Methods

We've already implemented:

- `init()`
- `doFilter()`
- `destroy()`

Now let's understand what actually happens behind the scenes.

| Method | Called by | Frequency |
|---|---|---|
| `init()` | Servlet container | Once per filter instance |
| `doFilter()` | Servlet container | For each matching request dispatch |
| `destroy()` | Servlet container | When the filter is taken out of service |

## 14. Understanding `init()`

```java
@Override
public void init(FilterConfig filterConfig) {
    System.out.println("Filter initialized");
}
```

The Servlet container calls `init()` when it initializes the filter.

It is useful for:

- Reading initialization parameters.
- Initializing resources.
- Preparing objects required by the filter.

### What Is FilterConfig?

`FilterConfig` is an object supplied by the Servlet container during filter initialization.

It provides access to the filter's configuration information, including initialization parameters.

For example, a filter can receive configuration values through its registration.

## 15. Understanding `doFilter()`

```java
@Override
public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
) throws IOException, ServletException {

    System.out.println("Request received");

    chain.doFilter(request, response);

    System.out.println("Response completed");
}
```

This is the actual request-processing method.

Whenever the container invokes the filter for a matching request dispatch, this method executes.

One filter instance can process multiple requests throughout its lifetime.

It can also process requests concurrently from multiple threads.

## 16. Understanding `destroy()`

```java
@Override
public void destroy() {
    System.out.println("Filter destroyed");
}
```

The Servlet container calls this method when it takes the filter out of service.

It can be used to release resources owned by the filter.

For example:

- Closing resources.
- Releasing allocated objects.
- Cleaning up resources initialized earlier.

---

## 17. One Instance, Multiple Requests

This is an important concept.

Imagine 100 users send requests to our application.

Does Tomcat normally create 100 instances of the same filter registration?

No.

Normally, the Servlet container creates one filter instance per filter registration and invokes it for multiple requests.

Conceptually:

```text
          One Filter Instance
                  |
             init() once
                  |
        +---------+---------+
        |         |         |
        v         v         v
     Request 1 Request 2 Request 3
        |         |         |
        v         v         v
     doFilter() doFilter() doFilter()
        |         |         |
        +---------+---------+
                  |
                  v
             destroy()
```

This is why understanding filter thread safety is important.

## 18. Filter Thread Safety

A single filter instance can receive concurrent requests from different threads.

Imagine two users send requests simultaneously.

If the filter stores request-specific information in a shared instance variable, one request may overwrite another request's data.

For example:

```java
public class MyCustomFilter implements Filter {

    private String currentUsername;

}
```

This is dangerous if `currentUsername` is updated for each request.

Imagine:

```text
Thread 1:
currentUsername = "Alice"

Thread 2:
currentUsername = "Bob"
```

Both threads share the same filter instance.

One thread may overwrite the value while the other thread is still processing its request.

### Better Approach

Prefer local variables inside `doFilter()` for request-specific information.

For example:

```java
@Override
public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
) throws IOException, ServletException {

    String currentRequestInfo = request.toString();

    System.out.println(currentRequestInfo);

    chain.doFilter(request, response);
}
```

Local variables belong to the executing method invocation, so concurrent requests do not overwrite each other's local values.

**Key takeaway:** Avoid storing mutable request-specific data in filter instance variables unless you deliberately handle thread safety.

---

## 19. Constructor vs. `init()`

We performed one additional experiment to understand the difference between Java object creation and Servlet filter initialization.

We added a constructor to `SecondCustomFilter`:

```java
public SecondCustomFilter() {
    System.out.println("Second Filter constructor called");
}
```

Now the filter contains:

```java
public class SecondCustomFilter implements Filter {

    public SecondCustomFilter() {
        System.out.println("Second Filter constructor called");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Second Filter initialized");
    }

    // doFilter() and destroy()
}
```

### What Did We Learn?

We observed two different events:

```text
Second Filter constructor called
Second Filter initialized
```

They represent different stages.

| Event | Meaning |
|---|---|
| Constructor | Java creates the object |
| `init()` | The Servlet container initializes the registered filter |

The constructor is part of Java object creation.

`init()` belongs to the Servlet Filter lifecycle.

This distinction becomes useful when we later study the Spring Bean lifecycle and Spring Security.

---

# Part 5 — Connecting Servlet Filters to Spring Security

## 20. Why Does Spring Security Use Filters?

So far, we have implemented ordinary Servlet Filters.

Now let's connect them to Spring Security.

Spring Security uses multiple filters to handle different security responsibilities.

For example:

| Filter | Responsibility |
|---|---|
| `UsernamePasswordAuthenticationFilter` | Processes username/password authentication |
| `BasicAuthenticationFilter` | Processes HTTP Basic Authentication |
| `ExceptionTranslationFilter` | Handles security-related exceptions and translates them into appropriate HTTP responses |
| `AuthorizationFilter` | Applies authorization rules |

These filters have different responsibilities.

Spring Security does not rely on one giant filter to handle every security operation.

Instead, it organizes multiple filters into a security filter chain.

This is why understanding the Servlet Filter mechanism is essential before studying Spring Security's internal architecture.

## 21. Three Names We Must Not Confuse

These three terms are related, but they are not interchangeable.

### 1. Servlet Filter

An individual component that intercepts and processes HTTP requests and responses.

Example:

```java
public class MyCustomFilter implements Filter {
    // Filter implementation
}
```

### 2. FilterChain

The Servlet API mechanism that allows a filter to pass processing to the next filter or destination.

Example:

```java
chain.doFilter(request, response);
```

### 3. SecurityFilterChain

A Spring Security abstraction representing a collection of security filters that should apply to matching requests.

A `SecurityFilterChain` is not the same thing as the Servlet API's `FilterChain`.

We will understand how Spring Security connects these concepts in the next feature.

---

# Part 6 — Understanding the Full Learning Journey

## 22. What We Have Built So Far

Our project now contains the following filter-related classes:

```text
Student_management/
│
├── Security/
│   ├── MyCustomFilter.java
│   ├── SecondCustomFilter.java
│   └── FilterConfiguration.java
│
└── StudentManagementApplication.java
```

The two filters use different registration mechanisms.

| Class | Purpose |
|---|---|
| `MyCustomFilter` | First custom filter, registered using `@WebFilter` |
| `SecondCustomFilter` | Second custom filter, registered using `FilterRegistrationBean` |
| `FilterConfiguration` | Spring configuration that registers the second filter |
| `StudentManagementApplication` | Enables Servlet component scanning for the annotation-based filter |

## 23. Questions and Answers — Revision

### Q1. What is the primary responsibility of a Servlet Filter?

**Answer:** To intercept and process HTTP requests and responses.

### Q2. What does `chain.doFilter(request, response)` do?

**Answer:** It passes processing to the next filter or destination in the chain.

### Q3. What happens if a filter does not invoke `chain.doFilter()`?

**Answer:** Normal downstream request processing is stopped.

### Q4. When does code after `chain.doFilter()` execute?

**Answer:** After downstream processing returns.

### Q5. Does implementing `Filter` automatically register it with Tomcat?

**Answer:** No. We must register the filter using an appropriate registration mechanism.

### Q6. Why do we use `@ServletComponentScan`?

**Answer:** It enables Spring Boot to discover Servlet components such as `@WebFilter`, `@WebServlet`, and `@WebListener`.

### Q7. Does `FilterRegistrationBean` require `@ServletComponentScan`?

**Answer:** No. It registers the filter through Spring Boot configuration.

### Q8. What is the difference between the constructor and `init()`?

**Answer:** The constructor creates the Java object, while `init()` is called by the Servlet container to initialize the filter.

### Q9. Does Tomcat normally create a new filter instance for every request?

**Answer:** No. Normally, one filter instance per registration handles multiple requests.

### Q10. Why should we avoid storing request-specific information in filter instance variables?

**Answer:** Because multiple request threads can access the same filter instance concurrently, potentially overwriting shared data.

### Q11. What is the difference between `FilterChain` and `SecurityFilterChain`?

**Answer:** `FilterChain` is part of the Servlet API and passes processing between components. `SecurityFilterChain` is a Spring Security abstraction representing security filters selected for matching requests.

---

# 24. Feature Progress Checklist

| Concept | Status |
|---|---|
| Understanding Servlet Filters | Completed |
| Understanding the `Filter` interface | Completed |
| Understanding `FilterChain` | Completed |
| Understanding `ServletRequest` and `ServletResponse` | Completed |
| Implementing `MyCustomFilter` | Completed |
| Understanding `@WebFilter` | Completed |
| Understanding `@ServletComponentScan` | Completed |
| Spring Boot 4 import correction | Completed |
| Implementing `SecondCustomFilter` | Completed |
| Understanding `FilterRegistrationBean` | Completed |
| Registering multiple filters | Completed |
| Testing filter execution | Completed |
| Understanding `chain.doFilter()` behavior | Completed |
| Understanding `init()` | Completed |
| Understanding `doFilter()` | Completed |
| Understanding `destroy()` | Completed |
| Understanding constructor vs. `init()` | Completed |
| Understanding filter instance lifecycle | Completed |
| Understanding filter thread safety | Completed |
| Filter ordering and URL mappings in depth | To consolidate and revise |
| Feature documentation and Git integration | To complete |

---

# 25. What's Next? — Spring Security Architecture

We now understand the underlying Servlet Filter mechanism.

The next step is to understand how Spring Security uses this mechanism internally.

Consider our actual API request:

```http
GET http://localhost:8081/students
```

The request travels through an architecture similar to:

```text
HTTP Request
     |
     v
Servlet Container (Tomcat)
     |
     v
DelegatingFilterProxy
     |
     v
FilterChainProxy
     |
     v
SecurityFilterChain
     |
     v
Spring Security Filters
     |
     v
DispatcherServlet
     |
     v
StudentController
```

We will investigate each component individually.

### Upcoming Topics

1. What is `DelegatingFilterProxy`?
2. What is `FilterChainProxy`?
3. What is `SecurityFilterChain`?
4. How does Spring Security select a matching filter chain?
5. How does authentication happen inside the security filters?
6. What happens when authentication fails?
7. How do multiple Spring Security filters execute in order?

We will also trace our successful Postman Basic Authentication request through the actual Spring Security architecture.

**Important:** We will not create additional Spring Security classes until we understand the architecture behind them.

---

## Final Takeaway

The most important thing to understand from this feature is that a Servlet Filter provides a mechanism to intercept and process HTTP requests and responses independently of our Controller, Service, and Repository layers.

We implemented two filters, registered them through two different mechanisms, observed how they execute, and explored their lifecycle.

This foundation will make the internal architecture of Spring Security much easier to understand.

**Next Feature: Spring Security Architecture — DelegatingFilterProxy, FilterChainProxy and SecurityFilterChain.**