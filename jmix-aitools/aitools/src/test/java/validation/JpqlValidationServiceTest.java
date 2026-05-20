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

package validation;

import io.jmix.aitools.dataload.generation.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AiToolsTestConfiguration.class})
class JpqlValidationServiceTest {

    @Autowired
    JpqlValidationService jpqlValidationService;

    @Test
    @DisplayName("Validates correct read-only JPQL result")
    void testValidatesCorrectResult() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.customer.name like :customerName",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                "Find orders by customer name",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().isEmpty());
    }

    @Test
    @DisplayName("Rejects non-select and write JPQL")
    void testRejectsNonSelectAndWriteJpql() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "delete from aitols_Order e where e.id = :id",
                List.of(new GeneratedJpqlParameter("id", "UUID", null)),
                "Delete order",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.notSelect")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.writeOperation")));
    }

    @Test
    @DisplayName("Rejects unknown root entity extracted from JPQL")
    void testRejectsUnknownRootEntity() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Unknown e where e.number = :number",
                List.of(new GeneratedJpqlParameter("number", "String", "1001")),
                "Unknown root entity",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("rootEntity.unknown")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("usedEntity.unknown")));
    }

    @Test
    @DisplayName("Rejects unknown used entities extracted from JPQL")
    void testRejectsUnknownUsedEntity() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e, aitols_UnknownEntity u where e.number = :number",
                List.of(new GeneratedJpqlParameter("number", "String", "1001")),
                "Unknown used entity",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertFalse(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("rootEntity.unknown")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("usedEntity.unknown")));
    }

    @Test
    @DisplayName("Rejects invalid property paths extracted from JPQL")
    void testRejectsInvalidPropertyPath() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.customer.fullTitle like :customerName",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                "Invalid property path",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("propertyPath.invalid")));
    }

    @Test
    @DisplayName("Rejects parameter mismatches between JPQL and DTO")
    void testRejectsParameterMismatches() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.customer.name like :customerName and e.number = :number",
                List.of(
                        new GeneratedJpqlParameter("customerName", "String", "%Acme%"),
                        new GeneratedJpqlParameter("unused", "String", "x")
                ),
                "Parameter mismatch",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("parameter.missingInDto")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("parameter.unusedInJpql")));
    }

    @Test
    @DisplayName("Rejects SQL-style pagination and date functions")
    void testRejectsCommonNonJpqlConstructs() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.date >= DATE_SUB(CURRENT_DATE(), 1, 'month') limit :limit",
                List.of(new GeneratedJpqlParameter("limit", "Integer", 10)),
                "Invalid SQL constructs in JPQL",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.sqlPagination")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.sqlDateFunction")));
    }

    @Test
    @DisplayName("Rejects current JPQL functions with parentheses")
    void testRejectsCurrentFunctionsWithParentheses() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.date >= CURRENT_DATE()",
                List.of(),
                "Uses CURRENT_DATE with parentheses",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("jpql.currentFunctionParentheses")));
    }

    @Test
    @DisplayName("Accepts supported Jmix date macros")
    void testAcceptsSupportedJmixDateMacros() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where @between(e.orderDate, now-1, now, month)",
                List.of(),
                "Orders for last month",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
    }

    @Test
    @DisplayName("Accepts supported relative date time constants")
    void testAcceptsSupportedRelativeDateTimeConstants() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.orderDate >= FIRST_DAY_OF_CURRENT_MONTH and e.orderDate <= LAST_DAY_OF_CURRENT_MONTH",
                List.of(),
                "Orders for current month",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
    }

    @Test
    @DisplayName("Rejects unsupported Jmix query macros")
    void testRejectsUnsupportedJmixQueryMacros() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where @unknownMacro(e.orderDate)",
                List.of(),
                "Unsupported macro",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("jpql.unsupportedMacro")));
    }

    @Test
    @DisplayName("Rejects unsupported relative date time constants")
    void testRejectsUnsupportedRelativeDateTimeConstants() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.orderDate >= START_OF_LAST_MONTH",
                List.of(),
                "Unsupported relative constant",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals("jpql.unsupportedRelativeDateTimeConstant")));
    }

    @Test
    @DisplayName("Does not treat string literal value as relative date time constant")
    void testIgnoresRelativeDateTimeLikeStringLiteral() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e where e.number = 'START_OF_LAST_MONTH'",
                List.of(),
                "String literal that looks like a relative constant",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
    }

    @Test
    @DisplayName("Rejects invalid JPQL syntax when QueryParser integration is available")
    void testRejectsInvalidJpqlSyntax() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitols_Order e limit 10",
                List.of(),
                "Invalid JPQL syntax",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.syntax.invalid")));
    }
}
