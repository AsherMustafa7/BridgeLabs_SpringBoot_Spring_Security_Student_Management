
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//-- Feature 4 import ends here
@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig 
{

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
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityInfoFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .securityMatcher("/students/security-info")
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

