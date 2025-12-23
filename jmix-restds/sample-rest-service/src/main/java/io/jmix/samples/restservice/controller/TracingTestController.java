/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.samples.restservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


/**
 * Test HTTP controller that acts as an echo service for distributed tracing headers.
 *
 * This controller is specifically designed to support tracing integration tests by:
 * 1. Capturing incoming HTTP tracing headers (both W3C and B3 formats)
 * 2. Logging the received headers for debugging purposes
 * 3. Returning the headers back to the client as a structured response
 *
 * The controller serves as a "mirror" that allows client-side code to verify
 * whether tracing headers are being properly propagated in outbound HTTP requests.
 * This is essential for testing distributed tracing functionality where we need
 * to confirm that trace context flows correctly across service boundaries.
 *
 * Without this test endpoint, it would be difficult to verify that HTTP clients
 * are correctly injecting tracing headers, as the headers are typically consumed
 * by tracing infrastructure and not visible in normal application responses.
 */
@RestController
public class TracingTestController {

    private static final Logger log = LoggerFactory.getLogger(TracingTestController.class);

    /**
     * Endpoint that captures and returns distributed tracing headers.
     *
     * This method extracts tracing headers from the incoming HTTP request and returns
     * them in a structured format. It supports both W3C trace context (traceparent)
     * and legacy B3 tracing headers for maximum compatibility.
     *
     * @param traceparent W3C trace context header containing trace ID, span ID, and flags
     * @param traceId B3 trace ID header (legacy Zipkin format)
     * @param spanId B3 span ID header (legacy Zipkin format)
     * @return TracingResponse containing all received tracing headers and processing timestamp
     */
    @GetMapping("/rest/tracing-test")
    public TracingResponse getTracingInfo(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @RequestHeader(value = "X-Span-Id", required = false) String spanId) {

        log.info("TracingTestController received - traceparent: {}, X-Trace-Id: {}, X-Span-Id: {}",
                 traceparent, traceId, spanId);

        return new TracingResponse(
                traceparent,
                traceId,
                spanId,
                String.valueOf(System.currentTimeMillis())
        );
    }


    /**
     * Response record containing tracing header information received by the test controller.
     *
     * @param traceparent W3C trace context header (format: 00-{traceId}-{spanId}-{flags})
     * @param xTraceId B3 trace ID header (legacy Zipkin format)
     * @param xSpanId B3 span ID header (legacy Zipkin format)
     * @param timestamp Server-side timestamp when the request was processed
     */
    record TracingResponse(String traceparent, String xTraceId, String xSpanId, String timestamp) {
    }
}