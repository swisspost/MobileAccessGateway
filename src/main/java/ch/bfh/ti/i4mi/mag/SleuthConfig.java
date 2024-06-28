package ch.bfh.ti.i4mi.mag;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SleuthConfig {

    @Autowired
    private Tracer tracer;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public TraceFilter traceFilter() {
        return new TraceFilter(tracer);
    }

    public static class TraceFilter extends OncePerRequestFilter {

        private final Tracer tracer;
        private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);

        public TraceFilter(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String traceparent = request.getHeader("traceparent");

            logger.debug("Headers received:");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                logger.debug("{}: {}", headerName, request.getHeader(headerName));
            });

            Span span;
            if (traceparent != null) {
                String[] traceparentComponents = traceparent.split("-");
                if (traceparentComponents.length == 4) {
                    String traceId = traceparentComponents[1];
                    String spanId = traceparentComponents[2];
                    String traceFlags = traceparentComponents[3];

                    TraceContext context = TraceContext.newBuilder()
                            .traceIdHigh(Long.parseUnsignedLong(traceId.substring(0, 16), 16))
                            .traceId(Long.parseUnsignedLong(traceId.substring(16), 16))
                            .spanId(Long.parseUnsignedLong(spanId, 16))
                            .sampled((traceFlags.equals("01")))
                            .build();

                    logger.debug("Parsed traceparent:");
                    logger.debug("Trace ID: {}", traceId);
                    logger.debug("Span ID: {}", spanId);
                    logger.debug("Trace Flags: {}", traceFlags);

                    span = tracer.joinSpan(context);
                } else {
                    span = tracer.nextSpan();
                }
            } else {
                span = tracer.nextSpan();
            }

            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span.start())) {
                logger.debug("Span started: {}", span.context().traceIdString());
                filterChain.doFilter(request, response);
            } finally {
                logger.debug("Span finished: {}", span.context().traceIdString());
                span.finish();
            }
        }
    }
}
