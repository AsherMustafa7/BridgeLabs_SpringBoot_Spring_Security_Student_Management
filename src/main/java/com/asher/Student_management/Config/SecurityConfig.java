
package com.asher.Student_management.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.asher.Student_management.Config.CustomAuthenticationEntryPoint;
import com.asher.Student_management.Config.CustomAccessDeniedHandler;


@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    // @Bean
    // @Order(1)
    // public SecurityFilterChain apisecurityFilterChain(HttpSecurity http) throws Exception {

    //     http.securityMatcher("/students/**").csrf( csrf -> csrf.disable() ).authorizeHttpRequests( auth -> auth.anyRequest().authenticated() ).httpBasic(Customizer.withDefaults());

    //     return http.build();
    // }


    @Bean
    @Order(1)
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
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }


    }

