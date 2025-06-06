/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl.annotated;

import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.test_support.report.UserProfileReport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InputParameterTest extends BaseAnnotatedReportExecutionTest {

    @Test
    public void testMissingRequiredParameter() {
        // given
        String reportCode = UserProfileReport.CODE; // it has 1 required parameter

        // when + then
        assertThatThrownBy(() -> reportRunner.byReportCode(reportCode)
                .run())
                .isInstanceOf(ReportingException.class)
                .hasMessageContaining("Required report parameter")
                .hasMessageContaining("not found");

    }
}

