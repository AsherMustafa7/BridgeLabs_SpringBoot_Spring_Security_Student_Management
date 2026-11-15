package com.asher.Student_management.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.setContentType("application/json");

        response.getWriter().write(
            "{\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource , Well Well Well would you look at that asher seems like your access was stopped by the inbuild security chain, you did authenticate but you \n dont have permission for this lad huh and we made sure that the perticular exception wis handles by our customaccessdenied handler\"}"
        );
    }
}