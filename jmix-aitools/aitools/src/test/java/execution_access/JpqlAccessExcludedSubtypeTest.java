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

package execution_access;

import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SecuredDataLoadTestConfiguration;
import test_support.SecuredDataLoadTestSupport;
import test_support.entity.Document;
import test_support.entity.SecretDocument;
import test_support.role.SecDocumentReadRole;

import javax.sql.DataSource;
import java.util.List;

import static io.jmix.aitools.dataload.validation.validator.UsedEntitiesValidator.USED_ENTITY_UNKNOWN_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Records of a {@code @ExcludeFromAi} subclass stay out of a query over its visible base entity, wherever the base
 * occurs in the query. The user may read both entities, so only the annotation keeps the records out.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecuredDataLoadTestConfiguration.class)
@TestPropertySource(properties = {"eclipselink.ddl-generation=create-tables", "jmix.core.work-dir=build/test-home/work"})
class JpqlAccessExcludedSubtypeTest {

    @Autowired
    SecuredDataLoadTestSupport support;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    SystemAuthenticator systemAuthenticator;
    @Autowired
    DataSource dataSource;

    @BeforeEach
    void setUp() {
        systemAuthenticator.runWithSystem(() -> {
            Document plan = document(Document.class, 1L, "Public plan", null);
            SecretDocument secret = document(SecretDocument.class, 2L, "TAX-SECRET-001", null);
            secret.setSecretCode("code-001");
            Document childOfSecret = document(Document.class, 3L, "Child of secret", secret);
            Document childOfPlan = document(Document.class, 4L, "Child of plan", plan);
            dataManager.saveWithoutReload(plan, secret, childOfSecret, childOfPlan);
        });
        support.loginAs(SecDocumentReadRole.CODE);
    }

    @AfterEach
    void tearDown() {
        support.deleteData();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("update AITLS_DOCUMENT set PARENT_ID = null");
        jdbc.update("delete from AITLS_DOCUMENT");
    }

    <T extends Document> T document(Class<T> documentClass, Long id, String title, Document parent) {
        T document = metadata.create(documentClass);
        document.setId(id);
        document.setTitle(title);
        document.setParent(parent);
        return document;
    }

    List<Object> column(JpqlExecutionResult result, String property) {
        assertTrue(result.isExecuted(), () -> "not executed: " + result.getExecutionError());
        return result.getRows().stream().map(row -> row.get(property)).toList();
    }

    @Test
    void execute_rootOverBase_skipsExcludedSubtypeRecords() {
        JpqlExecutionResult result = support.execute(
                "select d.title as title from aitls_Document d order by d.title", "title");

        assertEquals(List.of("Child of plan", "Child of secret", "Public plan"), column(result, "title"));
    }

    @Test
    void execute_aggregateOverBase_skipsExcludedSubtypeRecords() {
        JpqlExecutionResult result = support.execute(
                "select count(d.id) as cnt from aitls_Document d", "cnt");

        assertEquals(List.of(3L), column(result, "cnt"));
    }

    @Test
    void execute_pathToBase_skipsExcludedSubtypeRecords() {
        JpqlExecutionResult result = support.execute(
                "select d.parent.title as title from aitls_Document d order by d.parent.title", "title");

        assertEquals(List.of("Public plan"), column(result, "title"));
    }

    @Test
    void execute_innerJoinToBase_skipsExcludedSubtypeRecords() {
        JpqlExecutionResult result = support.execute(
                "select p.title as title from aitls_Document d join d.parent p order by p.title", "title");

        assertEquals(List.of("Public plan"), column(result, "title"));
    }

    @Test
    void execute_leftJoinToBase_keepsRecordsWithoutReference() {
        JpqlExecutionResult result = support.execute(
                "select d.title as title from aitls_Document d left join d.parent p order by d.title", "title");

        // `Child of secret` refers to an excluded record: the row goes, as with a row-level condition.
        assertEquals(List.of("Child of plan", "Public plan"), column(result, "title"));
    }

    @Test
    void execute_baseInSubquery_refusedWithoutNamingSubtype() {
        JpqlExecutionResult result = support.execute(
                "select d.title as title from aitls_Document d "
                        + "where exists (select x.id from aitls_Document x where x.parent = d)", "title");

        assertFalse(result.isExecuted());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("cannot be narrowed") && error.contains("aitls_Document"), error);
        assertFalse(error.contains("aitls_SecretDocument"), error);
    }

    @Test
    void execute_excludedSubtypeInTypeComparison_rejectedByValidation() {
        JpqlExecutionResult result = support.execute(
                "select d.title as title from aitls_Document d where type(d) = aitls_SecretDocument", "title");

        assertFalse(result.isExecuted());
        assertTrue(result.getValidationResult().getIssues().stream()
                        .anyMatch(issue -> issue.getCode().equals(USED_ENTITY_UNKNOWN_CODE)),
                () -> result.getValidationResult().getIssues().toString());
    }
}
