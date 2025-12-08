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

package rest_invoker;

import io.jmix.restds.util.RestDataStoreUtils;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;

import test_support.BaseRestDsIntegrationTest;
import test_support.TestRestDsConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;


@ContextConfiguration(classes = {
        TestRestDsConfiguration.class,
        RestInvokerTracingTest.TracingTestConfig.class
})
class RestInvokerTracingTest extends BaseRestDsIntegrationTest {

    /**
     * Test configuration that sets up OpenTelemetry tracing with W3C trace context propagation.
     *
     * This configuration is required to test that RestClient properly propagates tracing headers
     * when making HTTP requests. It provides:
     *
     * 1. OpenTelemetry SDK with W3C trace context propagator for standard traceparent headers
     * 2. Micrometer-OpenTelemetry bridge for seamless integration with Spring's Observation framework
     * 3. ObservationRegistry with tracing handlers that inject/extract trace headers automatically
     * 4. RestClient.Builder configured with the observation registry to enable header propagation
     *
     * Without this configuration, RestClient would not propagate any tracing headers, making
     * distributed tracing impossible across service boundaries.
     */
    @Configuration
    @Import({TestRestDsConfiguration.class})
    static class TracingTestConfig {

        /**
         * Configures OpenTelemetry SDK with W3C trace context propagation.
         * This ensures that traces use standard W3C traceparent headers instead of vendor-specific formats.
         */
        @Bean
        @Primary
        public io.opentelemetry.api.OpenTelemetry openTelemetry() {
            return io.opentelemetry.sdk.OpenTelemetrySdk.builder()
                    .setTracerProvider(io.opentelemetry.sdk.trace.SdkTracerProvider.builder().build())
                    .setPropagators(io.opentelemetry.context.propagation.ContextPropagators.create(
                            io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator.getInstance()
                    ))
                    .build();
        }

        /**
         * Creates a Micrometer Tracer that bridges to OpenTelemetry.
         * This allows using Micrometer's tracing API while leveraging OpenTelemetry's implementation.
         */
        @Bean
        @Primary
        public Tracer tracer(io.opentelemetry.api.OpenTelemetry openTelemetry) {
            io.opentelemetry.api.trace.Tracer otelTracer =
                    openTelemetry.getTracer("test-service");

            io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext currentTraceContext =
                    new io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext();

            return new OtelTracer(
                    otelTracer,
                    currentTraceContext,
                    null,
                    new OtelBaggageManager(
                            currentTraceContext,
                            java.util.Collections.emptyList(),
                            java.util.Collections.emptyList()
                    )
            );
        }

        /**
         * Creates a propagator for injecting/extracting trace context from HTTP headers.
         * This is essential for cross-service trace propagation.
         */
        @Bean
        @Primary
        public io.micrometer.tracing.propagation.Propagator propagator(io.opentelemetry.api.OpenTelemetry openTelemetry) {
            return new io.micrometer.tracing.otel.bridge.OtelPropagator(
                    openTelemetry.getPropagators(),
                    openTelemetry.getTracerProvider().get("test-service")
            );
        }

        /**
         * Configures the ObservationRegistry with tracing handlers.
         * The PropagatingSender/ReceiverTracingObservationHandlers automatically inject and extract
         * trace headers from HTTP requests, enabling seamless distributed tracing.
         */
        @Bean
        @Primary
        public ObservationRegistry observationRegistry(Tracer tracer, io.micrometer.tracing.propagation.Propagator propagator) {
            ObservationRegistry registry = ObservationRegistry.create();
            registry.observationConfig()
                    .observationHandler(new PropagatingSenderTracingObservationHandler<>(tracer, propagator))
                    .observationHandler(new PropagatingReceiverTracingObservationHandler<>(tracer, propagator))
                    .observationHandler(new DefaultTracingObservationHandler(tracer));
            return registry;
        }
    }

    @Autowired
    RestDataStoreUtils restDataStoreUtils;


    @Autowired
    Tracer tracer;

    /**
     * Tests that RestClient properly propagates W3C traceparent headers in HTTP requests.
     *
     * This test verifies the complete tracing integration by:
     * 1. Creating an active span with a known trace ID
     * 2. Making an HTTP request via RestClient within the span context
     * 3. Calling a test HTTP controller that returns the received traceparent header
     * 4. Verifying that the trace ID in the received header matches our original span's trace ID
     *
     * The test HTTP controller (TracingTestController) acts as a simple echo service that
     * captures and returns whatever tracing headers it receives. This allows us to verify
     * that Spring's RestClient properly injects the W3C traceparent header on the client side
     * when making outbound HTTP requests.
     *
     * Without proper tracing configuration, no headers would be propagated and distributed
     * tracing across service boundaries would be impossible.
     */
    @Test
    void testTracingWithRestClient() {
        RestClient restClient = restDataStoreUtils.getRestClient("restService1");

        io.micrometer.tracing.Span micrometerSpan = tracer.nextSpan()
                .name("test-request")
                .start();

        String expectedTraceId = micrometerSpan.context().traceId();
        assertThat(expectedTraceId)
                .isNotEmpty();

        TracingResponse response;
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(micrometerSpan)) {
            response = restClient.get()
                    .uri("/rest/tracing-test")
                    .retrieve()
                    .body(TracingResponse.class);
        } finally {
            micrometerSpan.end();
        }

        assertThat(response)
                .isNotNull();

        String receivedTraceparent = response.traceparent();

        Pattern pattern = Pattern.compile("^00-([0-9a-f]{32})-([0-9a-f]{16})-[0-9a-f]{2}$");
        Matcher matcher = pattern.matcher(receivedTraceparent);

        assertThat(matcher.matches())
                .isTrue();

        String receivedTraceIdFromServer = matcher.group(1);

        assertThat(receivedTraceIdFromServer)
                .isEqualTo(expectedTraceId);
    }

    record TracingResponse(String traceparent, String xTraceId, String xSpanId, String timestamp) {
    }


}