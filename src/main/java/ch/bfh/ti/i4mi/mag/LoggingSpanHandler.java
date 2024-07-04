package ch.bfh.ti.i4mi.mag;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSpanHandler extends SpanHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoggingSpanHandler.class);

    @Override
    public boolean begin(TraceContext context, MutableSpan span, TraceContext parent) {
        logger.info("Trace ID: {}", context.traceIdString());
        logger.info("Span ID: {}", context.spanIdString());
        if (parent != null) {
            logger.info("Parent Span ID: {}", parent.spanIdString());
        }
        return true;
    }
}