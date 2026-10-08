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

import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntitySummary;
import io.jmix.aitools.dataload.introspection.AvailableEntityService;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.core.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring AI tool that lets the model discover the application's domain model: list the entities
 * available to the user and load detailed metadata for the chosen ones.
 */
@Component("aitls_DomainModelDiscoveryTool")
public class DomainModelDiscoveryTool implements DataLoadAiTool, EntityDataLoadAiTool {

    public static final String AVAILABLE_ENTITIES_TOOL = "aitls_getAvailableEntities";
    public static final String DOMAIN_MODEL_FOR_ENTITIES_TOOL = "aitls_getDomainModelForEntities";

    private static final Logger log = LoggerFactory.getLogger(DomainModelDiscoveryTool.class);

    @Autowired
    protected AvailableEntityService availableEntityService;
    @Autowired
    protected AiToolStatusPublisher toolStatusPublisher;
    @Autowired
    protected Messages messages;

    /**
     * Lists the entities currently available to the user as compact summaries.
     *
     * @param toolContext Spring AI tool context used to publish status updates
     * @return available entity summaries (empty if none)
     */
    @Tool(name = AVAILABLE_ENTITIES_TOOL, description = """
            Returns compact metadata for all entities currently available to the user.
            Each item contains entity name, localized names, property names and property localized names.
            Entities and attributes hidden by application filtering or security are not returned.
            The result may be empty when the user has no available entities.
            
            Use this to:
                - Explore the currently available data model
                - Choose which exact entities should be inspected in more detail
                - Get correct entity names for follow-up tool calls
            """)
    public List<EntitySummary> getAvailableEntities(ToolContext toolContext) {
        log.debug("LLM tool call: getAvailableEntities()");

        String startStatus = messages.getMessage("DomainModelDiscoveryTool.getAvailableEntities.startStatus");
        toolStatusPublisher.update(startStatus, toolContext);

        List<EntitySummary> entitySummaries = availableEntityService.getEntitySummaries();

        String snippet = messages.formatMessage("",
                "DomainModelDiscoveryTool.getAvailableEntities.successStatus", entitySummaries.size());
        toolStatusPublisher.complete(startStatus, snippet, toolContext);

        return entitySummaries;
    }

    /**
     * Loads detailed metadata for the requested entity names, keeping only those available to the user.
     *
     * @param entityNames exact entity names to load metadata for
     * @param toolContext Spring AI tool context used to publish status updates
     * @return descriptors for the available requested entities (empty if none match)
     */
    @Tool(name = DOMAIN_MODEL_FOR_ENTITIES_TOOL, description = """
            Returns detailed domain model metadata for the specified entity names that are currently available to the user.
            Entities and attributes hidden by application filtering or security are omitted from the result.
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
            @ToolParam(description = "Exact entity names to load detailed metadata for.") List<String> entityNames,
            ToolContext toolContext) {
        log.debug("LLM tool call: getDomainModelForEntities({})", entityNames);

        String startStatus = messages.getMessage("DomainModelDiscoveryTool.getDomainModelForEntities.startStatus");
        toolStatusPublisher.update(startStatus, toolContext);

        List<EntityDescriptor> entityDescriptors = availableEntityService.findEntityDescriptorsByNames(entityNames);

        if (entityDescriptors.isEmpty()) {
            toolStatusPublisher.complete(startStatus,
                    messages.getMessage("DomainModelDiscoveryTool.getDomainModelForEntities.notFoundStatus"),
                    toolContext);
        } else {
            String entities = entityDescriptors.stream().map(EntityDescriptor::getName).reduce((s, s2) -> s + ", " + s2).get();
            toolStatusPublisher.complete(startStatus,
                    messages.formatMessage("", "DomainModelDiscoveryTool.getDomainModelForEntities.successStatus", entities),
                    toolContext);
        }

        return entityDescriptors;
    }
}
