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

package io.jmix.aitools;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties("jmix.aitools.dataload")
public class AiToolsDataLoadProperties {

    /**
     * Whether the data-load autoconfiguration is enabled.
     */
    Boolean enabled;

    /**
     * Whether system-level entities are excluded from the model exposed to the AI.
     */
    Boolean excludeSystemLevelEntities;

    /**
     * Maximum number of attempts to repair an invalid generated query.
     */
    Integer maxRepairAttempts;

    /**
     * Default maximum number of rows applied when a query does not specify one.
     */
    Integer jpqlExecutionMaxResult;

    /**
     * Hard upper bound for the number of rows a single query may request. Any larger
     * {@code maxResults} (whether supplied by the query or coming from the default) is
     * capped to this value before execution.
     */
    Integer jpqlExecutionMaxResultLimit;

    /**
     * Entity names to explicitly include; never {@code null}.
     */
    List<String> includeEntities;

    /**
     * Entity names to explicitly exclude; never {@code null}.
     */
    List<String> excludeEntities;

    /**
     * Package prefixes to explicitly include; never {@code null}.
     */
    List<String> includePackages;

    /**
     * Package prefixes to exclude; never {@code null}.
     */
    List<String> excludePackages;

    public AiToolsDataLoadProperties(@DefaultValue("true") Boolean enabled,
                                     @DefaultValue("true") Boolean excludeSystemLevelEntities,
                                     @DefaultValue("1") Integer maxRepairAttempts,
                                     @DefaultValue("20") Integer jpqlExecutionMaxResult,
                                     @DefaultValue("200") Integer jpqlExecutionMaxResultLimit,
                                     List<String> includeEntities,
                                     List<String> excludeEntities,
                                     List<String> includePackages,
                                     @DefaultValue("io.jmix") List<String> excludePackages) {
        this.enabled = enabled;
        this.excludeSystemLevelEntities = excludeSystemLevelEntities;
        this.maxRepairAttempts = maxRepairAttempts;
        this.jpqlExecutionMaxResult = jpqlExecutionMaxResult;
        this.jpqlExecutionMaxResultLimit = jpqlExecutionMaxResultLimit;
        this.includeEntities = includeEntities == null ? List.of() : List.copyOf(includeEntities);
        this.excludeEntities = excludeEntities == null ? List.of() : List.copyOf(excludeEntities);
        this.includePackages = includePackages == null ? List.of() : List.copyOf(includePackages);
        this.excludePackages = excludePackages == null ? List.of("io.jmix") : List.copyOf(excludePackages);
    }

    /**
     * @see #enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @see #excludeSystemLevelEntities
     */
    public Boolean getExcludeSystemLevelEntities() {
        return excludeSystemLevelEntities;
    }

    /**
     * @see #maxRepairAttempts
     */
    public Integer getMaxRepairAttempts() {
        return maxRepairAttempts;
    }

    /**
     * @see #jpqlExecutionMaxResult
     */
    public Integer getJpqlExecutionMaxResult() {
        return jpqlExecutionMaxResult;
    }

    /**
     * @see #jpqlExecutionMaxResultLimit
     */
    public Integer getJpqlExecutionMaxResultLimit() {
        return jpqlExecutionMaxResultLimit;
    }

    /**
     * @see #includeEntities
     */
    public List<String> getIncludeEntities() {
        return includeEntities;
    }

    /**
     * @see #excludeEntities
     */
    public List<String> getExcludeEntities() {
        return excludeEntities;
    }

    /**
     * @see #includePackages
     */
    public List<String> getIncludePackages() {
        return includePackages;
    }

    /**
     * @see #excludePackages
     */
    public List<String> getExcludePackages() {
        return excludePackages;
    }
}
