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

package execution;

import io.jmix.aitools.dataload.execution.EnumCaptionResultLocalizer;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.core.Messages;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.DataAccessTestConfiguration;
import test_support.entity.sales.Status;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DataAccessTestConfiguration.class})
class EnumCaptionResultLocalizerTest {

    @Autowired
    EnumCaptionResultLocalizer localizer;
    @Autowired
    Messages messages;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeEach
    void authenticate() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Replaces a raw enum id with its localized caption")
    void testLocalizesEnumIdToCaption() {
        JpqlExecutionResult result = createResult(
                "select o.number as orderNumber, o.status as status from aitls_Order o",
                List.of(Map.of("orderNumber", "A-1", "status", "O")));

        JpqlExecutionResult localized = localizer.localize(result, List.of("orderNumber", "status"));

        Map<String, Object> row = localized.getRows().get(0);
        assertEquals("A-1", row.get("orderNumber"));
        assertEquals(messages.getMessage(Status.OPEN), row.get("status"));
    }

    @Test
    @DisplayName("Formats an enum instance value directly")
    void testLocalizesEnumInstance() {
        JpqlExecutionResult result = createResult(
                "select o.status as status from aitls_Order o",
                List.of(Map.of("status", Status.CLOSED)));

        JpqlExecutionResult localized = localizer.localize(result, List.of("status"));

        assertEquals(messages.getMessage(Status.CLOSED), localized.getRows().get(0).get("status"));
    }

    @Test
    @DisplayName("Leaves values of non-property columns untouched")
    void testLeavesAggregateColumnsUntouched() {
        JpqlExecutionResult result = createResult(
                "select count(o) as cnt from aitls_Order o",
                List.of(Map.of("cnt", 5L)));

        JpqlExecutionResult localized = localizer.localize(result, List.of("cnt"));

        assertEquals(5L, localized.getRows().get(0).get("cnt"));
    }

    protected JpqlExecutionResult createResult(String jpql, List<Map<String, Object>> rows) {
        return new JpqlExecutionResult(
                new GeneratedJpqlResult(jpql, List.of(), "", List.of()),
                new JpqlValidationResult(true, List.of()),
                rows,
                20,
                0,
                false,
                false,
                true,
                null
        );
    }
}
