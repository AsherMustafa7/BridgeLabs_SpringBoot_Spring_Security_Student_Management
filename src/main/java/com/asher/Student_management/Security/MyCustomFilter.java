package com.asher.Student_management.Security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.FilterConfig;
import java.io.IOException;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = "/students/*")
public class MyCustomFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException, ServletException {

        System.out.println("First Filter - Before Request received");

        chain.doFilter(request, response);

        System.out.println("First Filter - After Response completed");
    }

    @Override
    public void destroy() {
        System.out.println("Filter destroyed");
    }
}