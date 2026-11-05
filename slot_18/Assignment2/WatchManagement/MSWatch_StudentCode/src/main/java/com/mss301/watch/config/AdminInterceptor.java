package com.mss301.watch.config;

import com.mss301.watch.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// [TODO-6.14] Admin check on the role set by the gateway; runs before the request body is validated
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String ADMIN_ROLE = "1";
    private static final String WATCH_LIST_PATH = "/api/watches";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean publicList = HttpMethod.GET.matches(request.getMethod()) && WATCH_LIST_PATH.equals(request.getRequestURI());
        if (!publicList && !ADMIN_ROLE.equals(request.getHeader(ROLE_HEADER))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Administrator role is required");
        }
        return true;
    }
}
