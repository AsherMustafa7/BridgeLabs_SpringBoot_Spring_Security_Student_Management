package com.asher.Student_management.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        response.getWriter().write(
            "{\"error\":\"Unauthorized\",\"message\":\"Authentication is required, WEll would you look at that mr. UnAuthenticated it seems like the overload commence methord in the CustomAuthenticaionEntry caught you redhanded huh, now get me that code you get when starting the spring backend.\"}"
        );
    }
}