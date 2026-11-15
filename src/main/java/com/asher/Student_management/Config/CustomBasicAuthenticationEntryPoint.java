
package com.asher.Student_management.Config;

import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import java.io.IOException;

@Component
public class CustomBasicAuthenticationEntryPoint
        extends BasicAuthenticationEntryPoint {

    public CustomBasicAuthenticationEntryPoint() {
        setRealmName("Student Management");
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authEx) throws IOException {

        response.addHeader(
                "WWW-Authenticate",
                "Basic realm=\"" + getRealmName() + "\""
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().println(
                "HTTP Status 401 OHH my god this is the basic one haha- "
                + authEx.getMessage()
        );
    }
}
