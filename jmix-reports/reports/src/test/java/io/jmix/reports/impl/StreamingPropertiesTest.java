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
package io.jmix.reports.impl;

import io.jmix.reports.ReportsProperties;
import io.jmix.reports.ReportsTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
@TestPropertySource(properties = "jmix.reports.streaming.fetch-size=42")
public class StreamingPropertiesTest {

    @Autowired
    protected ReportsProperties reportsProperties;

    @Test
    void testStreamingPropertiesBindWithDefaults() {
        assertThat(reportsProperties.getStreaming().getFetchSize()).isEqualTo(42);
        assertThat(reportsProperties.getStreaming().getCursorClearInterval()).isEqualTo(1000);
        assertThat(reportsProperties.getStreaming().getRowAccessWindowSize()).isEqualTo(100);
    }

    @Test
    void testNonPositiveStreamingKnobIsRejected() {
        assertThatThrownBy(() -> new ReportsProperties.Streaming(0, 1000, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReportsProperties.Streaming(1000, 0, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReportsProperties.Streaming(1000, 1000, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
