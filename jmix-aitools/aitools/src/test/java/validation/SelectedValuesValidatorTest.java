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

import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.validator.SelectedValuesValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import java.util.List;

import static io.jmix.aitools.dataload.validation.validator.SelectedValuesValidator.SELECTED_ENTITY_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class SelectedValuesValidatorTest {

    @Autowired
    SelectedValuesValidator validator;

    List<JpqlValidationIssue> validate(String jpql) {
        return validator.validate(new GeneratedJpqlResult(jpql, List.of(), "", List.of()));
    }

    void assertEntitySelected(String jpql) {
        List<JpqlValidationIssue> issues = validate(jpql);
        assertEquals(1, issues.size(), jpql);
        assertEquals(SELECTED_ENTITY_CODE, issues.get(0).getCode());
    }

    @Test
    void validate_rootAlias_reportsEntity() {
        assertEntitySelected("select a as approval from aitls_OrderApproval a");
    }

    @Test
    void validate_objectOfAlias_reportsEntity() {
        assertEntitySelected("select object(c) as customer from aitls_Customer c");
    }

    @Test
    void validate_joinedAlias_reportsEntity() {
        assertEntitySelected("select distinct c from aitls_Order o join o.customer c");
    }

    @Test
    void validate_referencePath_reportsEntity() {
        assertEntitySelected("select o.customer as customer from aitls_Order o");
    }

    @Test
    void validate_embeddablePath_reportsEntity() {
        assertEntitySelected("select o.address as address from aitls_Order o");
    }

    @Test
    void validate_entityAmongValues_reportsEntity() {
        assertEntitySelected("select o.number as number, o.customer as customer from aitls_Order o");
    }

    @Test
    void validate_attributePaths_noIssues() {
        assertTrue(validate("select o.number as number, o.customer.name as name, o.address.city as city "
                + "from aitls_Order o").isEmpty());
    }

    @Test
    void validate_aggregateOverAlias_noIssues() {
        assertTrue(validate("select count(o) as cnt from aitls_Order o").isEmpty());
        assertTrue(validate("select count(distinct c) as cnt from aitls_Order o join o.customer c").isEmpty());
        assertTrue(validate("select count(o.customer) as cnt from aitls_Order o").isEmpty());
    }

    @Test
    void validate_unknownAttribute_noIssues() {
        // An unknown path is UsedPropertyPathsValidator's to report, not this validator's.
        assertTrue(validate("select o.noSuchAttribute as x from aitls_Order o").isEmpty());
    }

    @Test
    void validate_unparsableText_noIssues() {
        assertTrue(validate("select from where").isEmpty());
    }
}
