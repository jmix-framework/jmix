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

import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aitols_JpqlExecutionTool")
public class JpqlExecutionTool implements DataLoadAiTool {

    private static final Logger log = LoggerFactory.getLogger(JpqlExecutionTool.class);

    @Autowired
    protected JpqlExecutionService jpqlExecutionService;

    @Tool(description = """
        Validates, repairs if needed, converts parameters and executes a read-only JPQL query through Jmix DataManager.

        Use this only after:
            - you selected the target entity with getAvailableEntities()
            - you loaded detailed metadata with getDomainModelForEntities()

        The request must contain:
            - the original user text in userText
            - a JPQL select query in jpql
            - rootEntityName
            - structured parameters with name, type and value
            - usedEntities and usedPropertyPaths consistent with the JPQL query
            - resultProperties listing returned columns in select-clause order for loadValues execution

        The tool returns:
            - the final JPQL draft after validation/repair
            - validation result
            - query rows serialized as structured JSON objects
            - execution error if the query could not be run
        """)
    public JpqlExecutionResult executeQuery(
            @ToolParam(description = "Structured request containing the original user text and the JPQL draft to execute.")
            JpqlExecutionRequest request) {
        log.debug("LLM tool call: executeQuery(jpql={})",
                request == null ? null : request.getJpql());

        return jpqlExecutionService.execute(request);
    }
}
