package ch.bfh.ti.i4mi.mag;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TraceHeaderLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic, if any
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String traceparent = request.getHeader("traceparent");
        String tracestate = request.getHeader("tracestate");

        // Log the headers
        System.out.println("Traceparent: " + traceparent);
        System.out.println("Tracestate: " + tracestate);

        // Add the headers to MDC for logging
        MDC.put("traceparent", traceparent);
        MDC.put("tracestate", tracestate);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove("traceparent");
            MDC.remove("tracestate");
        }
    }

    @Override
    public void destroy() {
        // Cleanup logic, if any
    }
}
