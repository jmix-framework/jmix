/*
 * Copyright 2026 Haulmont.
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

package observation;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two related concerns guarded here:
 *
 * <ol>
 *   <li><b>Micrometer behavior:</b> as of Micrometer 1.17 the registry no longer denies a second
 *   registration under the same Prometheus metric name with a different tag key set — both series are
 *   kept. (Earlier versions denied the conflicting key set with a single WARN per process.) This means
 *   the registry provides no defense-in-depth for {@code UiObservationSupport}'s wrap order; the cases
 *   below pin the current behavior so a future change back is noticed.</li>
 *
 *   <li><b>Our {@code MeterFilter}:</b> in legacy mode
 *   {@code LegacyUiTimerSupport.suppressObservationMetersInLegacyMode} installs a {@link MeterFilter}
 *   that denies registrations of {@code jmix.ui.*} meters carrying the modern-schema marker tag
 *   {@code lifecycle.name}. The {@code MeterFilter} cases below mimic that filter and verify it does
 *   what we expect.</li>
 * </ol>
 *
 * Because the registry no longer denies/rejects conflicting series, the explicit {@code MeterFilter} is the sole
 * guard keeping legacy-schema dashboards from being contaminated by modern series. If the MeterFilter set
 * fails, our filter logic broke (tag name renamed, predicate altered) and that contamination would return.
 *
 * <p>Deliberately a plain Java JUnit test, not a Spock specification: since Micrometer 1.17
 * {@code AbstractTimer} declares a field of type {@code org.LatencyUtils.PauseDetector} while LatencyUtils
 * is no longer a micrometer-core dependency. Java resolves the field type lazily and never touches it, but
 * Groovy builds its metaclass via {@code Class.getDeclaredFields()}, which eagerly resolves all field types —
 * any Groovy code dispatching on a {@code Timer} fails with {@code NoClassDefFoundError} unless LatencyUtils
 * is put back on the test classpath. Do not convert this test to Groovy without re-adding that dependency.
 */
class JmixUiMeterRegistrationTest {

    private PrometheusMeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    // -------- Micrometer behavior (registry no longer denies/rejects conflicting tag keys since 1.17) --------

    @Test
    @DisplayName("Micrometer: legacy and modern schemas both register under the same metric name")
    void testLegacyAndModernSchemasBothRegister() {
        // legacy schema timer registers first
        registry.timer("jmix.ui.views", "view", "MyView", "lifeCycle", "ready")
                .record(10, TimeUnit.MILLISECONDS);

        // modern schema timer registers second under the same metric name
        registry.timer("jmix.ui.views",
                        "view.id", "MyView",
                        "view.class", "com.foo.MyView",
                        "lifecycle.name", "ready",
                        "error", "none")
                .record(20, TimeUnit.MILLISECONDS);

        // as of Micrometer 1.17 the registry no longer denies the conflicting key set - both appear
        String scrape = registry.scrape();
        assertTrue(scrape.contains("jmix_ui_views_seconds_count{lifeCycle=\"ready\",view=\"MyView\"}"));
        assertTrue(scrape.contains("lifecycle_name=\"ready\""));
        assertTrue(scrape.contains("view_id=\"MyView\""));
    }

    @Test
    @DisplayName("Micrometer: modern-first registration order also keeps both schemas")
    void testModernFirstOrderAlsoKeepsBothSchemas() {
        // modern schema timer registers first
        registry.timer("jmix.ui.views",
                        "view.id", "MyView",
                        "view.class", "com.foo.MyView",
                        "lifecycle.name", "ready",
                        "error", "none")
                .record(20, TimeUnit.MILLISECONDS);

        // legacy schema timer registers second under the same metric name
        registry.timer("jmix.ui.views", "view", "MyView", "lifeCycle", "ready")
                .record(10, TimeUnit.MILLISECONDS);

        // together with the previous case: both schemas appear regardless of order
        String scrape = registry.scrape();
        assertTrue(scrape.contains("view_id=\"MyView\""));
        assertTrue(scrape.contains("lifecycle_name=\"ready\""));
        assertTrue(scrape.contains("jmix_ui_views_seconds_count{lifeCycle=\"ready\",view=\"MyView\"}"));
    }

    // -------- MeterFilter guard --------

    @Test
    @DisplayName("MeterFilter: legacy timer passes through (no lifecycle.name marker)")
    void testLegacyTimerPassesThrough() {
        installLegacyModeFilter();

        // legacy schema timer registers (no lifecycle.name tag)
        registry.timer("jmix.ui.views", "view", "MyView", "lifeCycle", "ready")
                .record(10, TimeUnit.MILLISECONDS);

        // the legacy series ends up in scrape
        assertTrue(registry.scrape().contains("jmix_ui_views_seconds_count{lifeCycle=\"ready\",view=\"MyView\"}"));
    }

    @Test
    @DisplayName("MeterFilter: modern Observation Timer for jmix.ui.* is denied")
    void testModernObservationTimerIsDenied() {
        installLegacyModeFilter();

        // Observation handler would register a Timer with the modern tag schema
        registry.timer("jmix.ui.views",
                        "view.id", "MyView",
                        "view.class", "com.foo.MyView",
                        "lifecycle.name", "ready",
                        "error", "none")
                .record(20, TimeUnit.MILLISECONDS);

        // nothing reaches scrape — modern series filtered out before registration
        String scrape = registry.scrape();
        assertFalse(scrape.contains("lifecycle_name=\"ready\""));
        assertFalse(scrape.contains("view_id=\"MyView\""));
    }

    @Test
    @DisplayName("MeterFilter: modern LongTaskTimer for jmix.ui.*.active is also denied")
    void testModernLongTaskTimerIsAlsoDenied() {
        installLegacyModeFilter();

        // Observation handler would register a LongTaskTimer on onStart
        registry.more().longTaskTimer("jmix.ui.views.active",
                        List.of(Tag.of("view.id", "MyView"), Tag.of("lifecycle.name", "ready")))
                .start()
                .stop();

        // no LongTaskTimer series appears in scrape
        assertFalse(registry.scrape().contains("jmix_ui_views_active_seconds"));
    }

    @Test
    @DisplayName("MeterFilter: non-jmix metric with lifecycle.name tag is not affected")
    void testNonJmixMetricIsNotAffected() {
        installLegacyModeFilter();

        // any other metric with lifecycle.name tag — for example a Spring HTTP metric — is registered
        registry.timer("http.server.requests", "lifecycle.name", "init", "method", "GET")
                .record(5, TimeUnit.MILLISECONDS);

        // it passes the filter — our prefix scope is intentionally narrow
        assertTrue(registry.scrape().contains("http_server_requests_seconds_count{lifecycle_name=\"init\",method=\"GET\"}"));
    }

    private void installLegacyModeFilter() {
        registry.config().meterFilter(MeterFilter.deny(id -> {
            String name = id.getName();
            return name != null
                    && name.startsWith("jmix.ui.")
                    && id.getTags().stream().anyMatch(t -> "lifecycle.name".equals(t.getKey()));
        }));
    }
}
