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

        registration.setOrder(1);

        return registration;
    }
}