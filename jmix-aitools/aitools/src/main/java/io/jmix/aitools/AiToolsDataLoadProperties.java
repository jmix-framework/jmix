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

@ConfigurationProperties("aitools.dataload")
public class AiToolsDataLoadProperties {

    Boolean enabled;

    Boolean excludeSystemLevelEntities;

    Integer maxRepairAttempts;

    /**
     * Defines the default value for max results when executing a query if the LLM does not provide one.
     */
    Integer defaultMaxResult;

    List<String> includeEntities;

    List<String> excludeEntities;

    List<String> includePackages;

    List<String> excludePackages;

    public AiToolsDataLoadProperties(@DefaultValue("true") Boolean enabled,
                                     @DefaultValue("true") Boolean excludeSystemLevelEntities,
                                     @DefaultValue("1") Integer maxRepairAttempts,
                                     @DefaultValue("20") Integer defaultMaxResult,
                                     List<String> includeEntities,
                                     List<String> excludeEntities,
                                     List<String> includePackages,
                                     @DefaultValue("io.jmix") List<String> excludePackages) {
        this.enabled = enabled;
        this.excludeSystemLevelEntities = excludeSystemLevelEntities;
        this.maxRepairAttempts = maxRepairAttempts;
        this.defaultMaxResult  = defaultMaxResult;
        this.includeEntities = includeEntities == null ? List.of() : List.copyOf(includeEntities);
        this.excludeEntities = excludeEntities == null ? List.of() : List.copyOf(excludeEntities);
        this.includePackages = includePackages == null ? List.of() : List.copyOf(includePackages);
        this.excludePackages = excludePackages == null ? List.of("io.jmix") : List.copyOf(excludePackages);
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getExcludeSystemLevelEntities() {
        return excludeSystemLevelEntities;
    }

    public Integer getMaxRepairAttempts() {
        return maxRepairAttempts;
    }

    public Integer getDefaultMaxResult() {
        return defaultMaxResult;
    }

    public List<String> getIncludeEntities() {
        return includeEntities;
    }

    public List<String> getExcludeEntities() {
        return excludeEntities;
    }

    public List<String> getIncludePackages() {
        return includePackages;
    }

    public List<String> getExcludePackages() {
        return excludePackages;
    }
}
