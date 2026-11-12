package com.asher.Student_management.Security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

public class MyCustomFilter implements Filter 
{

    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException, ServletException {

        System.out.println("Before Controller");

        chain.doFilter(request, response);

        System.out.println("After Controller");
    }
}