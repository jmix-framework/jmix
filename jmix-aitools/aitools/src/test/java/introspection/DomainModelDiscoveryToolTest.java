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

import io.jmix.aitools.dataload.tool.DomainModelDiscoveryTool;
import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntitySummary;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsTestConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class DomainModelDiscoveryToolTest {

    @Autowired
    DomainModelDiscoveryTool domainModelDiscoveryTool;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    @DisplayName("Returns compact metadata for all entities")
    void testReturnsCompactMetadataForAllEntities() {
        systemAuthenticator.begin();

        List<EntitySummary> documents = domainModelDiscoveryTool.getAvailableEntities(new ToolContext(Map.of()));

        systemAuthenticator.end();

        assertFalse(documents.isEmpty());

        EntitySummary orderDocument = documents.stream()
                .filter(document -> document.getEntityName().equals("aitols_Order"))
                .findFirst()
                .orElseThrow();

        assertTrue(orderDocument.getLocalizedNames().contains("Test order"));
        assertTrue(orderDocument.getPropertyNames().contains("number"));
        assertTrue(orderDocument.getPropertyLocalizedNames().contains("Number"));
    }

    @Test
    @DisplayName("Returns detailed entity descriptors for selected entities")
    void testReturnsDetailedEntityDescriptors() {
        systemAuthenticator.begin();

        List<EntityDescriptor> entityDescriptors = domainModelDiscoveryTool.getDomainModelForEntities(
                List.of("aitols_Order", "aitols_Customer"), new ToolContext(Map.of()));

        systemAuthenticator.end();

        assertEquals(2, entityDescriptors.size());

        EntityDescriptor orderDescriptor = entityDescriptors.stream()
                .filter(document -> document.getName().equals("aitols_Order"))
                .findFirst()
                .orElseThrow();

        assertTrue(orderDescriptor.getLocalizedNames().contains("Test order"));
        assertTrue(orderDescriptor.getProperties().stream().anyMatch(property -> property.getName().equals("number")));
    }
}
