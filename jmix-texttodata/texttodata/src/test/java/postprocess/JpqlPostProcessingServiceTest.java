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

package postprocess;

import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.postprocess.JpqlPostProcessingService;
import io.jmix.texttodata.postprocess.PostProcessedResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TextToDataTestConfiguration.class)
class JpqlPostProcessingServiceTest {

    @Autowired
    JpqlPostProcessingService jpqlPostProcessingService;

    @Test
    @DisplayName("Extracts literal LIMIT and OFFSET into execution options")
    void testExtractsLiteralPagination() {
        PostProcessedResult postProcessedResult = jpqlPostProcessingService.process(new GeneratedJpqlResult(
                "select e from textdt_Order e order by e.number limit 10 offset 5",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("number"),
                "Orders with pagination",
                List.of()
        ));

        assertEquals("select e from textdt_Order e order by e.number", postProcessedResult.getGeneratedJpqlResult().getJpql());
        assertEquals(10, postProcessedResult.getMaxResults());
        assertEquals(5, postProcessedResult.getFirstResult());
        assertTrue(postProcessedResult.getGeneratedJpqlResult().getWarnings()
                .contains("Pagination clauses were normalized into execution options"));
    }

    @Test
    @DisplayName("Extracts parameterized LIMIT and OFFSET and removes pagination parameters")
    void testExtractsParameterizedPagination() {
        PostProcessedResult postProcessedResult = jpqlPostProcessingService.process(new GeneratedJpqlResult(
                "select e from textdt_Order e where e.customer.name like :customerName limit :limit offset :offset",
                "textdt_Order",
                List.of(
                        new GeneratedJpqlParameter("customerName", "String", "%Acme%"),
                        new GeneratedJpqlParameter("limit", "Integer", 10),
                        new GeneratedJpqlParameter("offset", "Integer", 20)
                ),
                List.of("textdt_Order", "textdt_Customer"),
                List.of("customer.name"),
                "Orders with pagination params",
                List.of()
        ));

        assertEquals("select e from textdt_Order e where e.customer.name like :customerName",
                postProcessedResult.getGeneratedJpqlResult().getJpql());
        assertEquals(10, postProcessedResult.getMaxResults());
        assertEquals(20, postProcessedResult.getFirstResult());
        assertEquals(1, postProcessedResult.getGeneratedJpqlResult().getParameters().size());
        assertEquals("customerName", postProcessedResult.getGeneratedJpqlResult().getParameters().get(0).getName());
    }

    @Test
    @DisplayName("Keeps JPQL unchanged when pagination values cannot be resolved")
    void testKeepsUnresolvedPaginationForFurtherRepair() {
        PostProcessedResult postProcessedResult = jpqlPostProcessingService.process(new GeneratedJpqlResult(
                "select e from textdt_Order e limit :limit",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("limit", "String", "many")),
                List.of("textdt_Order"),
                List.of(),
                "Unresolved pagination",
                List.of()
        ));

        assertEquals("select e from textdt_Order e limit :limit", postProcessedResult.getGeneratedJpqlResult().getJpql());
        assertFalse(postProcessedResult.getGeneratedJpqlResult().getWarnings()
                .contains("Pagination clauses were normalized into execution options"));
    }
}
