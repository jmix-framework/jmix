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
            
            CRITICAL REQUIREMENTS:
            1. MUST call domain model tools first to get complete entity schema information
            2. MUST use AS aliases for ALL SELECT fields (security requirement)
            3. Use exact entity names and attribute names from the schema
            4. Prefer tested and reliable functions over advanced/experimental features
            5. The "resultProperties" must list returned columns in exactly the same order as the select expressions
            
            The request must contain:
                - the original user text in userText
                - a JPQL select query in jpql
                - structured parameters with name, type and value
                - resultProperties listing returned columns in select-clause order for loadValues execution

            Constraints:
            General query rules:
            - only select queries are allowed
            - never use update, delete, insert, or merge
            - use JPQL syntax only, never SQL syntax or vendor-specific SQL functions
            - if the request is ambiguous, still return the best draft query and describe ambiguity in warnings
            - produce JPQL suitable for Jmix DataManager.loadValues()
            - always select explicit scalar or property expressions that can be returned as tabular values
            - do not return the root entity alias itself as the selected value
            - you may select properties of the root entity, properties reached through joins, and aggregate expressions
            
            Pagination rules:
            - never use LIMIT or OFFSET in JPQL
            - if the user asks for a row limit or offset, put them into maxResults and firstResult instead
            
            Date and time rules:
            - never use SQL date functions such as DATE_SUB, DATE_ADD, NOW, CURDATE, or INTERVAL expressions
            - use CURRENT_DATE, CURRENT_TIME, and CURRENT_TIMESTAMP without parentheses when they are needed in JPQL
            - you may use supported Jmix date macros when they fit the request: @between, @today, @dateEquals, @dateBefore, @dateAfter
            - you may use supported Jmix relative date time constants when they fit the request: FIRST_DAY_OF_CURRENT_YEAR, LAST_DAY_OF_CURRENT_YEAR, FIRST_DAY_OF_CURRENT_MONTH, LAST_DAY_OF_CURRENT_MONTH, FIRST_DAY_OF_CURRENT_WEEK, LAST_DAY_OF_CURRENT_WEEK, START_OF_CURRENT_DAY, END_OF_CURRENT_DAY, START_OF_YESTERDAY, START_OF_TOMORROW, START_OF_CURRENT_HOUR, END_OF_CURRENT_HOUR, START_OF_CURRENT_MINUTE, END_OF_CURRENT_MINUTE
            - for requests such as today, yesterday, this month, last month, before today, or after today, prefer supported Jmix date macros or relative date time constants over SQL-specific date arithmetic
            - if a date range requires application-side calculation, prefer named parameters such as :fromDate and :toDate instead of SQL-specific date arithmetic
            
            Examples:
            - Last 30 days: @between(o.date, now-30, now, day)
            - Last month: @between(o.date, now-1, now, month)
            - Today only: @today(o.date)
            - Yesterday: @dateEquals(o.date, now-1)
            - Future dates: @dateAfter(o.date, now)
            - Past dates: @dateBefore(o.date, now-7)
            - Current month with constants: o.date >= FIRST_DAY_OF_CURRENT_MONTH and o.date <= LAST_DAY_OF_CURRENT_MONTH
            - Today with constants: o.createdAt >= START_OF_CURRENT_DAY and o.createdAt <= END_OF_CURRENT_DAY
            
            Examples of valid resultProperties alignment:
            - select o.number, o.date -> resultProperties: ["number", "date"]
            - select c.name, count(o) -> resultProperties: ["customerName", "orderCount"]
            - select o.number, c.name, sum(l.amount) -> resultProperties: ["orderNumber", "customerName", "totalAmount"]
            
            The tool returns:
                - the final JPQL draft after validation/repair
                - validation result
                - query rows serialized as structured JSON objects
                - execution error if the query could not be run
            """)
    public JpqlExecutionResult executeQuery(
            @ToolParam(description = "Structured request containing the original user text and the JPQL draft to execute.")
            JpqlExecutionRequest request) {
        log.debug("LLM tool call: executeQuery(jpql={})", request == null ? null : request.getJpql());

        return jpqlExecutionService.execute(request);
    }
}
