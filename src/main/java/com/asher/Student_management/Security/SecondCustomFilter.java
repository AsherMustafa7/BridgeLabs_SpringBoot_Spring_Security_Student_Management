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
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException, ServletException 
    {


        System.out.println("Second Filter - Before");
        String block = request.getParameter("block");

        if ("true".equals(block)) {
            System.out.println("Second Filter - Request blocked");

            response.setContentType("text/plain");
            response.getWriter().write("Request blocked by Second Filter");

            return;
        }

        chain.doFilter(request, response);

        System.out.println("Second Filter - After");
    }

    @Override
    public void destroy() {
        System.out.println("Second Filter destroyed");
    }
}