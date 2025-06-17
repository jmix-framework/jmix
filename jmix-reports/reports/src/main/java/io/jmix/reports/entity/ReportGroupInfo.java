/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.entity;

import io.jmix.core.Id;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.reports.ReportGroupRepository;

import java.util.UUID;

/**
 * Briefly describes a report group defined in code or in database.
 * Used as presentation-layer object, except code which works only with persistent entity.
 * <br/>
 * It can be obtained using one of {@link ReportGroupRepository} methods.
 * Can be converted to a full model object by using {@link ReportGroupRepository}.
 * @see ReportGroupRepository
 * @see ReportGroup
 */
@JmixEntity(name = "report_ReportGroupInfo")
@SystemLevel
public class ReportGroupInfo {

    @JmixId
    protected UUID id;

    protected String code;

    /**
     * Title, already localized to the target locale of the user who requested this info object.
     */
    @InstanceName
    protected String localizedTitle;

    protected ReportSource source;

    protected Boolean systemFlag;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocalizedTitle() {
        return localizedTitle;
    }

    public void setLocalizedTitle(String localizedTitle) {
        this.localizedTitle = localizedTitle;
    }

    public ReportSource getSource() {
        return source;
    }

    public void setSource(ReportSource source) {
        this.source = source;
    }

    public Boolean getSystemFlag() {
        return systemFlag;
    }

    public void setSystemFlag(Boolean systemFlag) {
        this.systemFlag = systemFlag;
    }

    public Id<ReportGroup> toEntityId() {
        return Id.of(getId(), ReportGroup.class);
    }
}
