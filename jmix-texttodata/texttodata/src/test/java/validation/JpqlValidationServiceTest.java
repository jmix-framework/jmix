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

import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.validation.JpqlValidationResult;
import io.jmix.texttodata.validation.JpqlValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TextToDataTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TextToDataTestConfiguration.class})
class JpqlValidationServiceTest {

    @Autowired
    JpqlValidationService jpqlValidationService;

    @Test
    @DisplayName("Validates correct read-only JPQL result")
    void testValidatesCorrectResult() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from textdt_Order e where e.customer.name like :customerName",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                List.of("textdt_Order", "textdt_Customer"),
                List.of("customer.name"),
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
                "delete from textdt_Order e where e.id = :id",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("id", "UUID", null)),
                List.of("textdt_Order"),
                List.of("id"),
                "Delete order",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.notSelect")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.writeOperation")));
    }

    @Test
    @DisplayName("Rejects unknown root entity, used entity, and property path")
    void testRejectsUnknownMetadataReferences() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from textdt_Order e where e.customer.fullTitle like :customerName",
                "textdt_Unknown",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                List.of("textdt_Order", "textdt_UnknownEntity"),
                List.of("customer.fullTitle"),
                "Unknown metadata references",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("rootEntity.unknown")));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("usedEntity.unknown")));
    }

    @Test
    @DisplayName("Rejects invalid property paths for known root entity")
    void testRejectsInvalidPropertyPath() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from textdt_Order e where e.customer.fullTitle like :customerName",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                List.of("textdt_Order", "textdt_Customer"),
                List.of("customer.fullTitle"),
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
                "select e from textdt_Order e where e.customer.name like :customerName and e.number = :number",
                "textdt_Order",
                List.of(
                        new GeneratedJpqlParameter("customerName", "String", "%Acme%"),
                        new GeneratedJpqlParameter("unused", "String", "x")
                ),
                List.of("textdt_Order", "textdt_Customer"),
                List.of("customer.name", "number"),
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
                "select e from textdt_Order e where e.date >= DATE_SUB(CURRENT_DATE(), 1, 'month') limit :limit",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("limit", "Integer", 10)),
                List.of("textdt_Order"),
                List.of("date"),
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
                "select e from textdt_Order e where e.date >= CURRENT_DATE()",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("date"),
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
                "select e from textdt_Order e where @between(e.orderDate, now-1, now, month)",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("orderDate"),
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
                "select e from textdt_Order e where e.orderDate >= FIRST_DAY_OF_CURRENT_MONTH and e.orderDate <= LAST_DAY_OF_CURRENT_MONTH",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("orderDate"),
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
                "select e from textdt_Order e where @unknownMacro(e.orderDate)",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("orderDate"),
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
                "select e from textdt_Order e where e.orderDate >= START_OF_LAST_MONTH",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("orderDate"),
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
                "select e from textdt_Order e where e.number = 'START_OF_LAST_MONTH'",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of("number"),
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
                "select e from textdt_Order e limit 10",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of(),
                "Invalid JPQL syntax",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals("jpql.syntax.invalid")));
    }
}
