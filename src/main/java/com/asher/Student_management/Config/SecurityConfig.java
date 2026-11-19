
package com.asher.Student_management.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import com.asher.Student_management.Config.CustomAuthenticationEntryPoint;
import com.asher.Student_management.Config.CustomAccessDeniedHandler;

// ---feature 4 imports here 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//-- Feature 4 import ends here

//imports of feature 04.2 DaoAuthenticationProvider
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;


@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig 
{

    // bean feature 04.2 DaoAuthenticationProvider
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, // = new customeuserdetailsservice (Dependency Injection happing here)
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);


        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }
    // @Bean
    // @Order(1)
    // public SecurityFilterChain apisecurityFilterChain(HttpSecurity http) throws Exception {

    //     http.securityMatcher("/students/**").csrf( csrf -> csrf.disable() ).authorizeHttpRequests( auth -> auth.anyRequest().authenticated() ).httpBasic(Customizer.withDefaults());

    //     return http.build();
    // }
    // feature 4 function one being added here for password encoding
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        // removed the inmemory user details manager cause we already configures the uerdetailservice class as a custom one in src\main\java\com\asher\Student_management\Security\CustomUserDetailsService.java
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityInfoFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .securityMatcher("/students/security-info")
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                // we can also do this , without actually taking it as parameters but its better as parameters cause the spring already has the bean
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/students/**").hasRole("ADMIN")
            )
            
            .httpBasic(Customizer.withDefaults());
            
        return http.build();
    }

    // Feature 4 function one being added here for password encoding

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .securityMatcher("/students/**")
            .exceptionHandling(exception -> exception
                
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.GET, "/students/**")
                .permitAll()

                .requestMatchers(HttpMethod.POST, "/students/**")
                .authenticated()

                .requestMatchers(HttpMethod.DELETE, "/students/**")
                .hasRole("ADMIN")
                // This we will see later in the next features. 
                .anyRequest()
                .authenticated()
            )
            .httpBasic(basic -> basic
            .authenticationEntryPoint(customBasicAuthenticationEntryPoint)
        );

        return http.build();
    }
    // @Bean
    // @Order(2)
    // public SecurityFilterChain apiSecurityFilterChain(
    //         HttpSecurity http) throws Exception {

    //     http.csrf(csrf -> csrf.disable())
    //         .securityMatcher("/students/**")
    //         .authorizeHttpRequests(auth -> auth

    //             .requestMatchers(HttpMethod.GET, "/students/**") // the get request will all be allowed
    //             .permitAll()

    //             .requestMatchers(HttpMethod.POST, "/students/**") // the post request will require it to be authenticated
    //             .authenticated()
    //             // ---------------------------
    //             .requestMatchers(HttpMethod.DELETE, "/students/**")
    //             .hasRole("ADMIN")
    //             // ---------------------------
    //             .anyRequest()
    //             .authenticated()

    //         )
    //         .httpBasic(Customizer.withDefaults());

    //     return http.build();
    // }


    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }


    }

