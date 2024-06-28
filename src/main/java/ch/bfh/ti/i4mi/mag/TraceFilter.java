package ch.bfh.ti.i4mi.mag;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TraceFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public TraceFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Span span = this.tracer.currentSpan();
        if (span != null) {
            response.addHeader("traceId", span.context().traceId());
            response.addHeader("spanId", span.context().spanId());
        }
        filterChain.doFilter(request, response);
    }
}
