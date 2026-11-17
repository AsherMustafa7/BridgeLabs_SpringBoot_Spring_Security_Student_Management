
package com.asher.Student_management.Security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService 
{

    private final Map<String, UserDetails> users = new HashMap<>();

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) 
    {

        users.put("asher",
                User.withUsername("asher")
                        .password(passwordEncoder.encode("admin123"))
                        .roles("ADMIN")
                        .build());

        users.put("richa",
                User.withUsername("richa")
                        .password(passwordEncoder.encode("student123"))
                        .roles("USER")
                        .build());

        users.put("guest",
                User.withUsername("guest")
                        .password(passwordEncoder.encode("guest123"))
                        .roles("USER")
                        .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

                System.out.println("CustomUserDetailsService called for ##################################### : " + username);
        UserDetails user = users.get(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found: This I am throwing for form the class custom user details service By the way, cause the user you wanted didnt match the name of any users that were present" + username
            );
        }

        return user;
    }
}
