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

package prompt;

import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
import io.jmix.texttodata.introspection.search.DomainModelSearchCandidate;
import io.jmix.texttodata.introspection.search.DomainModelSearchService;
import io.jmix.texttodata.dataload.prompt.PromptContextBuilder;
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
@ContextConfiguration(classes = TextToDataTestConfiguration.class)
class PromptContextBuilderTest {

    @Autowired
    PromptContextBuilder promptContextBuilder;

    @Autowired
    DomainModelSearchService domainModelSearchService;

    @Autowired
    DomainModelRegistry domainModelRegistry;

    @Test
    @DisplayName("Builds compact prompt context from search candidates")
    void testBuildsCompactPromptContext() {
        List<DomainModelSearchCandidate> candidates = domainModelSearchService.search("order customer open", 2);

        String context = promptContextBuilder.build(candidates);

        assertFalse(context.isBlank());
        assertTrue(context.contains("Entity textdt_Order"));
        assertTrue(context.contains("Entity textdt_Customer"));
        assertTrue(context.contains("datatypes:"));
        assertTrue(context.contains("enums:"));
        assertTrue(context.contains("relations:"));
        assertTrue(context.contains("number String persistent|required"));
        assertTrue(context.contains("status enum[CLOSED:C,OPEN:O] persistent|optional"));
        assertTrue(context.contains("plainStatus enum[DONE:DONE,NEW:NEW] persistent|optional storage=string"));
        assertTrue(context.contains("customer -> textdt_Customer MANY_TO_ONE persistent|required"));
    }

    @Test
    @DisplayName("Expands relation targets by default")
    void testExpandsRelationTargetsByDefault() {
        List<DomainModelSearchCandidate> candidates = List.of(orderCandidate());

        String context = promptContextBuilder.build(candidates);

        assertTrue(context.contains("Entity textdt_Order"));
        assertTrue(context.contains("Entity textdt_Customer"));
        assertTrue(context.contains("Entity textdt_OrderLine"));
    }

    @Test
    @DisplayName("Can disable relation expansion")
    void testCanDisableRelationExpansion() {
        List<DomainModelSearchCandidate> candidates = List.of(orderCandidate());

        String context = promptContextBuilder.build(candidates, 0);

        assertTrue(context.contains("Entity textdt_Order"));
        assertFalse(context.contains("Entity textdt_Customer"));
        assertFalse(context.contains("Entity textdt_OrderLine"));
    }

    @Test
    @DisplayName("Returns empty context for empty candidates")
    void testReturnsEmptyContextForEmptyCandidates() {
        assertEquals("", promptContextBuilder.build(List.of()));
    }

    protected DomainModelSearchCandidate orderCandidate() {
        return new DomainModelSearchCandidate(
                domainModelRegistry.getEntityDescriptor("textdt_Order"),
                1000,
                List.of("entityName")
        );
    }
}
