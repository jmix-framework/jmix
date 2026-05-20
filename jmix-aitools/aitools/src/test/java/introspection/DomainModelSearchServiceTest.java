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

package introspection;

import io.jmix.aitools.introspection.search.DomainModelSearchCandidate;
import io.jmix.aitools.introspection.search.DomainModelSearchService;
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
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class DomainModelSearchServiceTest {

    @Autowired
    DomainModelSearchService domainModelSearchService;

    @Test
    @DisplayName("Searches entities by entity name and captions")
    void testSearchesByEntityNameAndCaptions() {
        List<DomainModelSearchCandidate> candidates = domainModelSearchService.search("test order", 3);

        assertFalse(candidates.isEmpty());
        assertEquals("aitols_Order", candidates.get(0).getEntity().getName());
        assertTrue(candidates.get(0).getScore() > 0);
        assertTrue(candidates.get(0).getMatchedBy().contains("entityCaption")
                || candidates.get(0).getMatchedBy().contains("entityName"));
    }

    @Test
    @DisplayName("Searches entities by property names and comments")
    void testSearchesByPropertyNamesAndComments() {
        List<DomainModelSearchCandidate> numberCandidates = domainModelSearchService.search("number", 3);
        assertFalse(numberCandidates.isEmpty());
        assertEquals("aitols_Order", numberCandidates.get(0).getEntity().getName());

        List<DomainModelSearchCandidate> commentCandidates = domainModelSearchService.search("display", 3);
        assertFalse(commentCandidates.isEmpty());
        assertEquals("aitols_Customer", commentCandidates.get(0).getEntity().getName());
    }

    @Test
    @DisplayName("Searches entities by enum values and relation targets")
    void testSearchesByEnumValuesAndRelationTargets() {
        List<DomainModelSearchCandidate> enumCandidates = domainModelSearchService.search("open", 3);
        assertFalse(enumCandidates.isEmpty());
        assertEquals("aitols_Order", enumCandidates.get(0).getEntity().getName());

        List<DomainModelSearchCandidate> relationCandidates = domainModelSearchService.search("customer", 5);
        assertFalse(relationCandidates.isEmpty());
        assertEquals("aitols_Customer", relationCandidates.get(0).getEntity().getName());
        assertTrue(relationCandidates.stream().anyMatch(candidate -> candidate.getEntity().getName().equals("aitols_Order")));
    }

    @Test
    @DisplayName("Returns empty result for blank query and respects limit")
    void testBlankQueryAndLimit() {
        assertTrue(domainModelSearchService.search("   ").isEmpty());
        assertTrue(domainModelSearchService.search(null).isEmpty());

        List<DomainModelSearchCandidate> limitedCandidates = domainModelSearchService.search("order customer tag", 2);
        assertEquals(2, limitedCandidates.size());
    }
}
