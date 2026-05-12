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

package io.jmix.texttodata;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties("texttodata")
public class TextToDataProperties {

    Boolean enabled;

    Integer maxRepairAttempts;

    Integer maxEntityCandidates;

    Integer relationExpansionDepth;

    Boolean excludeSystemLevelEntities;

    List<String> includeEntities;

    List<String> excludeEntities;

    List<String> includePackages;

    List<String> excludePackages;

    public TextToDataProperties(@DefaultValue("true") Boolean enabled,
                                @DefaultValue("2") Integer maxRepairAttempts,
                                @DefaultValue("10") Integer maxEntityCandidates,
                                @DefaultValue("1") Integer relationExpansionDepth,
                                @DefaultValue("true") Boolean excludeSystemLevelEntities,
                                List<String> includeEntities,
                                List<String> excludeEntities,
                                List<String> includePackages,
                                @DefaultValue("io.jmix") List<String> excludePackages) {
        this.enabled = enabled;
        this.maxRepairAttempts = maxRepairAttempts;
        this.maxEntityCandidates = maxEntityCandidates;
        this.relationExpansionDepth = relationExpansionDepth;
        this.excludeSystemLevelEntities = excludeSystemLevelEntities;
        this.includeEntities = includeEntities == null ? List.of() : List.copyOf(includeEntities);
        this.excludeEntities = excludeEntities == null ? List.of() : List.copyOf(excludeEntities);
        this.includePackages = includePackages == null ? List.of() : List.copyOf(includePackages);
        this.excludePackages = excludePackages == null ? List.of("io.jmix") : List.copyOf(excludePackages);
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getMaxRepairAttempts() {
        return maxRepairAttempts;
    }

    public Integer getMaxEntityCandidates() {
        return maxEntityCandidates;
    }

    public Integer getRelationExpansionDepth() {
        return relationExpansionDepth;
    }

    public Boolean getExcludeSystemLevelEntities() {
        return excludeSystemLevelEntities;
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
