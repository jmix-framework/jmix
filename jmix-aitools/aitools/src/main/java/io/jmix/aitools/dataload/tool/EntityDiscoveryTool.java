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
import io.jmix.aitools.introspection.model.EntitySummary;
import io.jmix.aitools.introspection.AvailableEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("aitols_DomainModelDiscoveryTool")
public class EntityDiscoveryTool implements DataLoadAiTool, EntityDataLoadAiTool {

    public static final String AVAILABLE_ENTITIES_TOOL = "aitols_getAvailableEntities()";
    public static final String DOMAIN_MODEL_FOR_ENTITIES_TOOL = "aitols_getDomainModelForEntities()";

    private static final Logger log = LoggerFactory.getLogger(EntityDiscoveryTool.class);

    @Autowired
    protected AvailableEntityService availableEntityService;

    @Tool(name = AVAILABLE_ENTITIES_TOOL, description = """
            Returns compact metadata for all entities currently available to the user.
            Each item contains entity name, localized names, property names and property localized names.
            Entities hidden by application filtering or security are not returned.
            The result may be empty when the user has no available entities.
            
            Use this to:
                - Explore the currently available data model
                - Choose which exact entities should be inspected in more detail
                - Get correct entity names for follow-up tool calls
            """)
    public List<EntitySummary> getAvailableEntities() {
        log.debug("LLM tool call: getAvailableEntities()");

        return availableEntityService.getEntitySummaries();
    }

    @Tool(name = DOMAIN_MODEL_FOR_ENTITIES_TOOL, description = """
            Returns detailed domain model metadata for the specified entity names that are currently available to the user.
            Entities hidden by application filtering or security are omitted from the result.
            The result may be empty if none of the requested entities are available.
            
            MANDATORY: You MUST call this function for the entities you intend to query BEFORE any executeQuery() calls.
            
            Returns:
            - Exact attribute names (Java property names)
            - Entity relationships for JPQL joins
            - Property types and constraints
            - Enum properties in one map: enums.<ENUM_NAME>.id (+ optional enums.<ENUM_NAME>.description)
            
            ENUM RULE (CRITICAL):
                - When filtering enum properties in executeQuery(), use enums.<ENUM_NAME>.id, not enum constant names.
                - Example: if enums.PAID.id is 40, then pass parameter 40 (not "PAID").
            """)
    public List<EntityDescriptor> getDomainModelForEntities(
            @ToolParam(description = "Exact entity names to load detailed metadata for.") List<String> entityNames) {
        log.debug("LLM tool call: getDomainModelForEntities({})", entityNames);

        return availableEntityService.findEntityDescriptorsByNames(entityNames);
    }
}
