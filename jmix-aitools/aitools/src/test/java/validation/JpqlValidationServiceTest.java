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

import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
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

import static io.jmix.aitools.dataload.validation.validator.CommonNonJpqlConstructsValidator.CURRENT_FUNCTION_PARENTHESES_CODE;
import static io.jmix.aitools.dataload.validation.validator.CommonNonJpqlConstructsValidator.SQL_DATE_FUNCTION_CODE;
import static io.jmix.aitools.dataload.validation.validator.CommonNonJpqlConstructsValidator.SQL_PAGINATION_CODE;
import static io.jmix.aitools.dataload.validation.validator.JpqlSyntaxValidator.JPQL_SYNTAX_INVALID_CODE;
import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.*;
import static io.jmix.aitools.dataload.validation.validator.ParametersValidator.PARAMETER_MISSING_CODE;
import static io.jmix.aitools.dataload.validation.validator.ParametersValidator.PARAMETER_UNUSED_CODE;
import static io.jmix.aitools.dataload.validation.validator.ReadOnlyQueryValidator.JPQL_NATIVE_FUNCTION_CODE;
import static io.jmix.aitools.dataload.validation.validator.ReadOnlyQueryValidator.JPQL_NOT_SELECT_CODE;
import static io.jmix.aitools.dataload.validation.validator.ReadOnlyQueryValidator.JPQL_WRITE_OPERATION_CODE;
import static io.jmix.aitools.dataload.validation.validator.ReservedWordAliasValidator.RESERVED_ALIAS_CODE;
import static io.jmix.aitools.dataload.validation.validator.RootEntityValidator.ROOT_ENTITY_UNKNOWN_CODE;
import static io.jmix.aitools.dataload.validation.validator.SupportedJmixTemporalConstructsValidator.UNSUPPORTED_MACRO_CODE;
import static io.jmix.aitools.dataload.validation.validator.UsedEntitiesValidator.USED_ENTITY_UNKNOWN_CODE;
import static io.jmix.aitools.dataload.validation.validator.UsedPropertyPathsValidator.PROPERTY_PATH_INVALID_CODE;
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
                "select e from aitls_Order e where e.customer.name like :customerName",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                "Find orders by customer name",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().isEmpty());
    }

    @Test
    @DisplayName("Does not treat write keywords inside a string literal as a write operation")
    void testIgnoresWriteKeywordInStringLiteral() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e.number as n from aitls_Order e where e.number = 'please update this record'",
                List.of(),
                "String literal that contains a write keyword",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .noneMatch(issue -> issue.getCode().equals(JPQL_WRITE_OPERATION_CODE)));
    }

    @Test
    @DisplayName("Does not treat SQL pagination keywords inside a string literal as SQL pagination")
    void testIgnoresPaginationKeywordInStringLiteral() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e.number as n from aitls_Order e where e.number = 'no limit applies'",
                List.of(),
                "String literal that contains a pagination keyword",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .noneMatch(issue -> issue.getCode().equals(SQL_PAGINATION_CODE)));
    }

    @Test
    @DisplayName("Accepts property paths into embedded attributes")
    void testAcceptsEmbeddedPropertyPath() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.address.city = :city",
                List.of(new GeneratedJpqlParameter("city", "String", "Springfield")),
                "Orders by embedded address city",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .noneMatch(issue -> issue.getCode().equals(PROPERTY_PATH_INVALID_CODE)));
    }

    @Test
    @DisplayName("Rejects non-select and write JPQL")
    void testRejectsNonSelectAndWriteJpql() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "delete from aitls_Order e where e.id = :id",
                List.of(new GeneratedJpqlParameter("id", "UUID", null)),
                "Delete order",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(JPQL_NOT_SELECT_CODE)));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(JPQL_WRITE_OPERATION_CODE)));
    }

    @Test
    @DisplayName("Rejects native escape functions SQL() and FUNCTION()")
    void testRejectsNativeEscapeFunctions() {
        GeneratedJpqlResult functionResult = new GeneratedJpqlResult(
                "select FUNCTION('some_proc', e.id) as v from aitls_Order e",
                List.of(),
                "Uses FUNCTION escape",
                List.of()
        );

        JpqlValidationResult functionValidation = jpqlValidationService.validate(functionResult);

        assertFalse(functionValidation.isValid());
        assertTrue(functionValidation.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(JPQL_NATIVE_FUNCTION_CODE)));

        GeneratedJpqlResult sqlResult = new GeneratedJpqlResult(
                "select e.id as id from aitls_Order e where SQL('1 = 1')",
                List.of(),
                "Uses SQL escape",
                List.of()
        );

        JpqlValidationResult sqlValidation = jpqlValidationService.validate(sqlResult);

        assertFalse(sqlValidation.isValid());
        assertTrue(sqlValidation.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(JPQL_NATIVE_FUNCTION_CODE)));
    }

    @Test
    @DisplayName("Rejects unknown root entity extracted from JPQL")
    void testRejectsUnknownRootEntity() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Unknown e where e.number = :number",
                List.of(new GeneratedJpqlParameter("number", "String", "1001")),
                "Unknown root entity",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(ROOT_ENTITY_UNKNOWN_CODE)));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(USED_ENTITY_UNKNOWN_CODE)));
    }

    @Test
    @DisplayName("Rejects unknown used entities extracted from JPQL")
    void testRejectsUnknownUsedEntity() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e, aitls_UnknownEntity u where e.number = :number",
                List.of(new GeneratedJpqlParameter("number", "String", "1001")),
                "Unknown used entity",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertFalse(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(ROOT_ENTITY_UNKNOWN_CODE)));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(USED_ENTITY_UNKNOWN_CODE)));
    }

    @Test
    @DisplayName("Rejects invalid property paths extracted from JPQL")
    void testRejectsInvalidPropertyPath() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.customer.fullTitle like :customerName",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                "Invalid property path",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(PROPERTY_PATH_INVALID_CODE)));
    }

    @Test
    @DisplayName("Rejects parameter mismatches between JPQL and DTO")
    void testRejectsParameterMismatches() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.customer.name like :customerName and e.number = :number",
                List.of(
                        new GeneratedJpqlParameter("customerName", "String", "%Acme%"),
                        new GeneratedJpqlParameter("unused", "String", "x")
                ),
                "Parameter mismatch",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(PARAMETER_MISSING_CODE)));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(PARAMETER_UNUSED_CODE)));
    }

    @Test
    @DisplayName("Does not treat a colon-prefixed word inside a string literal as a parameter")
    void testIgnoresParameterLikeStringLiteral() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.number = ':deadline reached'",
                List.of(),
                "String literal that looks like a named parameter",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .noneMatch(issue -> issue.getCode().equals(PARAMETER_MISSING_CODE)));
    }

    @Test
    @DisplayName("Rejects SQL-style pagination and date functions")
    void testRejectsCommonNonJpqlConstructs() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.date >= DATE_SUB(CURRENT_DATE(), 1, 'month') limit :limit",
                List.of(new GeneratedJpqlParameter("limit", "Integer", 10)),
                "Invalid SQL constructs in JPQL",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(SQL_PAGINATION_CODE)));
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(SQL_DATE_FUNCTION_CODE)));
    }

    @Test
    @DisplayName("Rejects current JPQL functions with parentheses")
    void testRejectsCurrentFunctionsWithParentheses() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.date >= CURRENT_DATE()",
                List.of(),
                "Uses CURRENT_DATE with parentheses",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(CURRENT_FUNCTION_PARENTHESES_CODE)));
    }

    @Test
    @DisplayName("Accepts supported Jmix date macros")
    void testAcceptsSupportedJmixDateMacros() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where @between(e.orderDate, now-1, now, month)",
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
                "select e from aitls_Order e where e.orderDate >= FIRST_DAY_OF_CURRENT_MONTH and e.orderDate <= LAST_DAY_OF_CURRENT_MONTH",
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
                "select e from aitls_Order e where @unknownMacro(e.orderDate)",
                List.of(),
                "Unsupported macro",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(UNSUPPORTED_MACRO_CODE)));
    }

    @Test
    @DisplayName("Does not treat a macro-like string literal as an unsupported Jmix query macro")
    void testIgnoresMacroLikeStringLiteral() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e.number as n from aitls_Order e where e.number = '@unknownMacro(x)'",
                List.of(),
                "String literal that looks like a Jmix query macro",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .noneMatch(issue -> issue.getCode().equals(UNSUPPORTED_MACRO_CODE)));
    }

    @Test
    @DisplayName("Rejects unsupported relative date time constants")
    void testRejectsUnsupportedRelativeDateTimeConstants() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e where e.orderDate >= START_OF_LAST_MONTH",
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
                "select e from aitls_Order e where e.number = 'START_OF_LAST_MONTH'",
                List.of(),
                "String literal that looks like a relative constant",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
    }

    @Test
    @DisplayName("Rejects a reserved word used as a select alias")
    void testRejectsReservedWordAlias() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e.id as id from aitls_Order e",
                List.of(),
                "Reserved word 'id' used as alias",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .anyMatch(issue -> issue.getCode().equals(RESERVED_ALIAS_CODE)));
    }

    @Test
    @DisplayName("Accepts a non-reserved select alias")
    void testAcceptsNonReservedAlias() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e.id as orderId from aitls_Order e",
                List.of(),
                "Non-reserved alias",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream()
                .noneMatch(issue -> issue.getCode().equals(RESERVED_ALIAS_CODE)));
    }

    @Test
    @DisplayName("Rejects invalid JPQL syntax when QueryParser integration is available")
    void testRejectsInvalidJpqlSyntax() {
        GeneratedJpqlResult result = new GeneratedJpqlResult(
                "select e from aitls_Order e limit 10",
                List.of(),
                "Invalid JPQL syntax",
                List.of()
        );

        JpqlValidationResult validationResult = jpqlValidationService.validate(result);

        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getIssues().stream().anyMatch(issue -> issue.getCode().equals(JPQL_SYNTAX_INVALID_CODE)));
    }
}
