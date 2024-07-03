package ch.bfh.ti.i4mi.mag;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TraceHeaderLoggingFilter implements Filter {

    @Override
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String traceparent = request.getHeader("traceparent");
        String tracestate = request.getHeader("tracestate");
        String b3 = request.getHeader("b3");
        String b3TraceId = request.getHeader("X-B3-TraceId");
        String b3SpanId = request.getHeader("X-B3-SpanId");

        // Log the headers
        System.out.println("Traceparent: " + traceparent);
        System.out.println("Tracestate: " + tracestate);
        System.out.println("B3: " + b3);
        System.out.println("X-B3-TraceId: " + b3TraceId);
        System.out.println("X-B3-SpanId: " + b3SpanId);

        // Add the headers to MDC for logging
        MDC.put("traceparent", traceparent);
        MDC.put("tracestate", tracestate);
        MDC.put("b3", b3);
        MDC.put("X-B3-TraceId", b3TraceId);
        MDC.put("X-B3-SpanId", b3SpanId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove("traceparent");
            MDC.remove("tracestate");
            MDC.remove("b3");
            MDC.remove("X-B3-TraceId");
            MDC.remove("X-B3-SpanId");
        }
    }
}