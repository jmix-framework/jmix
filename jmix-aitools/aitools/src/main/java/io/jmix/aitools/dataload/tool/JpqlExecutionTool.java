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
            Validates, repairs if needed and executes a read-only JPQL query through Jmix DataManager.
            
            CRITICAL REQUIREMENTS:
            1. MUST call domain model tools first to get complete entity schema information
            2. MUST use AS aliases for ALL SELECT fields (security requirement)
            3. The "resultProperties" must list returned columns in exactly the same order as the select expressions
            4. Use exact entity names and attribute names from the schema
            5. Prefer tested and reliable functions over advanced/experimental features
            
            THE REQUEST MUST CONTAIN:
                - The original user text in "userText"
                - A JPQL select query in "jpql"
                - Structured parameters with name, type and value
                - The "resultProperties" listing returned columns in select-clause order
                - The "maxResults"
                - The "firstResult"

            GENERAL QUERY RULES:
            - Only select queries are allowed
            - Never use update, delete, insert, or merge
            - Use JPQL syntax only, never SQL syntax or vendor-specific SQL functions
            - Produce JPQL suitable for Jmix DataManager
            - Always select explicit scalar or property expressions that can be returned as tabular values
            - Do not return the root entity alias itself as the selected value
            - You may select properties of the root entity, properties reached through joins, and aggregate expressions
            
            PAGINATION RULES:
            - Results are limited to 50 rows by default (max 200).
            - If the result contains 'hasMore: true', more data is available in the database.
            - To fetch the next set of results, call this tool again with an increased "firstResult".
            - if the user asks for a row limit or offset, put them into "maxResults" and "firstResult" instead.
            - Use server-side aggregation (COUNT, SUM, AVG) whenever possible instead of fetching all rows.
            
            ALIAS REQUIREMENT:
            ✓ CORRECT: SELECT c.name AS clientName, COUNT(o) AS orderCount FROM Client c LEFT JOIN c.orders o GROUP BY c
            Then provide: resultProperties = ["clientName", "orderCount"]
            
            ✗ INCORRECT: SELECT c.name, COUNT(o) FROM Client c LEFT JOIN c.orders o GROUP BY c
            (Missing AS aliases and resultProperties parameter)
            
            JPQL SYNTAX RULES (Jmix/EclipseLink):
            - Use entity names not table names
            - Use attribute names not column names
            - Use entity relationships for joins not foreign keys
            - No SELECT * allowed - specify exact attributes
            - Use COUNT(entity) not COUNT(*)
            - AS aliases are MANDATORY for all SELECT fields (security and parsing requirement)
            - No subqueries in SELECT clause
            - Use GROUP BY entity not GROUP BY entity.id
            
            IMPORTANT - AVOID JPQL RESERVED WORDS AS ALIASES:
            Never use these words as AS aliases: position, user, order, table, group, where, select, from, join,
            left, right, inner, outer, on, and, or, not, in, exists, between, like, is, null, true, false,
            count, sum, avg, max, min, distinct, all, any, some, union, except, intersect, case, when, then,
            else, end, new, constructor, size, index, key, value, entry, type, treat, current_date, current_time,
            current_timestamp, local, date, time, timestamp, year, month, day, hour, minute, second.
            
            ✓ CORRECT: SELECT co.position AS jobPosition (not AS position)
            ✗ INCORRECT: SELECT co.position AS position
            
            DATE AND TIME RULES:
            - Never use SQL date functions such as DATE_SUB, DATE_ADD, NOW, CURDATE, or INTERVAL expressions
            - Use CURRENT_DATE, CURRENT_TIME, and CURRENT_TIMESTAMP without parentheses when they are needed in JPQL
            - You may use supported Jmix date macros when they fit the request: @between, @today, @dateEquals, @dateBefore, @dateAfter
            - You may use supported Jmix relative date time constants when they fit the request:
              FIRST_DAY_OF_CURRENT_YEAR, LAST_DAY_OF_CURRENT_YEAR, FIRST_DAY_OF_CURRENT_MONTH, LAST_DAY_OF_CURRENT_MONTH,
              FIRST_DAY_OF_CURRENT_WEEK, LAST_DAY_OF_CURRENT_WEEK, START_OF_CURRENT_DAY, END_OF_CURRENT_DAY, 
              START_OF_YESTERDAY, START_OF_TOMORROW, START_OF_CURRENT_HOUR, END_OF_CURRENT_HOUR, START_OF_CURRENT_MINUTE,
              END_OF_CURRENT_MINUTE
            - For requests such as today, yesterday, this month, last month, before today, or after today, prefer
              supported Jmix date macros or relative date time constants over SQL-specific date arithmetic.
            - If a date range requires application-side calculation, prefer named parameters such as :fromDate
              and :toDate instead of SQL-specific date arithmetic
            - Use ISO date literals in single quotes for fixed dates:
              - WHERE o.date >= '2024-01-01'
              - WHERE o.date BETWEEN '2024-01-01' AND '2024-01-31'
            
            MACROS AND TIME CONSTANTS EXAMPLES:
            - Last 30 days: @between(o.date, now-30, now, day)
            - Last month: @between(o.date, now-1, now, month)
            - Today only: @today(o.date)
            - Yesterday: @dateEquals(o.date, now-1)
            - Future dates: @dateAfter(o.date, now)
            - Past dates: @dateBefore(o.date, now-7)
            - Current month with constants: o.date >= FIRST_DAY_OF_CURRENT_MONTH and o.date <= LAST_DAY_OF_CURRENT_MONTH
            - Today with constants: o.createdAt >= START_OF_CURRENT_DAY and o.createdAt <= END_OF_CURRENT_DAY
            
            ✓ CORRECT for last 30 days:
            SELECT o.number AS orderNumber, o.total AS orderTotal FROM Order_ o WHERE @between(o.date, now-30, now, day)
            
            ✗ INCORRECT: Don't use CURRENT_DATE arithmetic like "CURRENT_DATE - 30"
            
            PARAMETER HANDLING:
            Parameters are automatically converted to appropriate types when possible:
            - Date strings → LocalDate/LocalDateTime/OffsetDateTime (e.g., "2024-01-15")
            - Numeric strings → BigDecimal/Integer/Long (e.g., "1500.50", "42")
            - UUID strings → UUID for entity IDs
            - Boolean strings → Boolean ("true", "false")
            - Other strings remain as strings (LIKE patterns, etc.)
            
            ENUM PARAMETERS (CRITICAL):
            - For enum properties, use id from domain model enums mapping (enums.<ENUM_NAME>.id).
            - Do NOT pass enum constant names when enums provides numeric/string IDs.
            - Example for Invoice.status: enums {NEW: {id: 10}, PENDING: {id: 20}, OVERDUE: {id: 30}, PAID: {id: 40}}
              ✓ CORRECT: WHERE i.status = :status with parameters {"status": 40}
              ✗ INCORRECT: WHERE i.status = :status with parameters {"status": "PAID"}
            - For IN filters, also pass mapped values list (e.g., {"statuses": [20, 30]}).
            
            JMIX JPQL EXTENSIONS AND FUNCTIONS:
            
            DATE/TIME FUNCTIONS:
            - EXTRACT(field FROM date) - Extract date/time parts: YEAR, MONTH, DAY, HOUR, MINUTE, SECOND
              Examples: EXTRACT(YEAR FROM o.date), EXTRACT(MONTH FROM o.date)
            - CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP - Current date/time values
            - DATE(datetime) - Extract date part from datetime
            - TIME(datetime) - Extract time part from datetime
            
            MATHEMATICAL FUNCTIONS:
            - Basic arithmetic: +, -, *, / (e.g., o.total * 2, o.total + 1000)
            - Parentheses for operation precedence
            - ABS(number) - Absolute value (limited support)
            - ROUND(number, digits) - Round to specified decimal places (limited support)
            
            STRING FUNCTIONS:
            - CONCAT(str1, str2, ...) - Concatenate strings
            - SUBSTRING(string, start, length) - Extract substring
            - LENGTH(string) - String length
            - LOCATE(substring, string, start) - Find substring position
            - UPPER(string) - Convert to uppercase
            - LOWER(string) - Convert to lowercase
            - TRIM(string) - Remove leading/trailing whitespace
            - LIKE with wildcards (%, _) - Pattern matching (recommended)
            
            CONDITIONAL FUNCTIONS:
            - CASE WHEN condition THEN result ELSE alternative END - Conditional expressions
            - COALESCE(value1, value2, ...) - Return first non-null value
            - NULLIF(value1, value2) - Return null if values are equal
            
            AGGREGATE FUNCTIONS:
            - COUNT(entity) - Count entities (use entity, not *)
            - SUM(expression) - Sum of values
            - AVG(expression) - Average value
            - MAX(expression) - Maximum value
            - MIN(expression) - Minimum value
            - DISTINCT - Use with aggregates for unique values
            
            COLLECTION FUNCTIONS:
            - SIZE(collection) - Collection size
            - IS EMPTY / IS NOT EMPTY - Check if collection is empty
            
            DATE MACROS (Jmix-specific):
            - @between(field, start, end, unit) - Date range queries
              Units: year, month, day, hour, minute, second
              Examples: @between(o.date, now-30, now, day), @between(o.date, now-1, now, month)
            - @today(field) - Today's date
            - @dateEquals(field, value) - Date equality
            - @dateBefore(field, value) - Date before
            - @dateAfter(field, value) - Date after
            - Special values: now, now-N (N units ago)
            
            BEST PRACTICES (TESTED AND RELIABLE):
            - Use EXTRACT for date parts instead of proprietary functions
            - Use LIKE with wildcards instead of REGEXP for pattern matching
            - Use basic arithmetic (+, -, *, /) instead of advanced mathematical functions
            - Use Jmix date macros (@between, @today) for date filtering
            - Test complex functions in development before using in production queries
            
            CRITICAL JPQL PARSER LIMITATIONS:
            ✗ INCORRECT: COUNT(CASE WHEN o.date >= '2025-01-01' THEN 1 END)
            ✗ INCORRECT: SUM(CASE WHEN @between(o.date, now-90, now, day) THEN o.total ELSE 0 END)
            
            ✓ CORRECT: Use separate queries with simple WHERE clauses for period comparisons.
            
            Without domain model tools first, queries may fail due to incorrect entity/attribute names.
            For date ranges, prefer Jmix macros over parameters for better handling.
            """)
    public JpqlExecutionResult executeQuery(
            @ToolParam(description = "Structured request containing the original user text and the JPQL draft to execute.")
            JpqlExecutionRequest request) {
        log.debug("LLM tool call: executeQuery(jpql={})", request == null ? null : request.getJpql());

        return jpqlExecutionService.execute(request);
    }
}
