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

package io.jmix.aitools.dataload.tool;

import io.jmix.aitools.introspection.model.EntityDescriptor;
import io.jmix.aitools.introspection.search.DomainModelDocument;
import io.jmix.aitools.introspection.search.DomainModelSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("aitols_DomainModelDiscoveryTool")
public class DomainModelDiscoveryTool implements DataLoadAiTool {

    private static final Logger log = LoggerFactory.getLogger(DomainModelDiscoveryTool.class);

    @Autowired
    protected DomainModelSearchService domainModelSearchService;

    @Tool(description = """
        Returns compact metadata for all entities in the domain model.
        Each item contains entity name, localized names, property names and property localized names.

        Use this to:
            - Explore the complete data model
            - Choose which exact entities should be inspected in more detail
            - Get correct entity names for follow-up tool calls
        """)
    public List<DomainModelDocument> getAvailableEntities() {
        log.debug("LLM tool call: getAvailableEntities()");

        return domainModelSearchService.getAvailableEntities();
    }

    @Tool(description = """
        Returns detailed domain model metadata for the specified entities names.

        MANDATORY: You MUST call this function for the entities you intend to query BEFORE any executeQuery() calls.

        ENUM RULE (CRITICAL):
            - When filtering enum properties in executeQuery(), use enums.<ENUM_NAME>.id, not enum constant names.
            - Example: if enums.PAID.id is 40, then pass parameter 40 (not "PAID").
        """)
    public List<EntityDescriptor> getDomainModelForEntities(
            @ToolParam(description = "Exact entity names to load detailed metadata for.") List<String> entityNames) {
        log.debug("LLM tool call: getDomainModelForEntities({})", entityNames);

        return domainModelSearchService.getDomainModelForEntities(entityNames);
    }
}
