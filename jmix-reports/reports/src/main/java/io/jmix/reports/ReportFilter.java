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

package io.jmix.reports;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportOutputType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

/**
 * Contains possible filtering conditions when loading reports from {@link ReportRepository}.
 */
public class ReportFilter {

    /**
     * case-insensitive
     */
    @Nullable
    protected String nameContains;

    /*
     * case-insensitive
     */
    @Nullable
    protected String codeContains;

    @Nullable
    protected ReportGroup group;

    @Nullable
    protected Date updatedAfter;

    @Nullable
    protected Boolean restAccessible;

    /**
     * Filter reports whose list of views contains given viewId.
     */
    @Nullable
    protected String viewId;

    /**
     * Filter reports whose list of roles contains one of user's assigned resource roles.
     */
    @Nullable
    protected UserDetails user;

    /**
     * Filter reports that have an input parameter with given metaClass.
     */
    @Nullable
    protected MetaClass inputValueMetaClass;

    /**
     * Filter by <code>system</code> attribute.
     */
    @Nullable
    protected Boolean system;

    /**
     * Filter reports that have a template with given output type.
     */
    @Nullable
    protected ReportOutputType outputType;

    @Nullable
    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(@Nullable String nameContains) {
        this.nameContains = nameContains;
    }

    @Nullable
    public String getCodeContains() {
        return codeContains;
    }

    public void setCodeContains(@Nullable String codeContains) {
        this.codeContains = codeContains;
    }

    @Nullable
    public ReportGroup getGroup() {
        return group;
    }

    public void setGroup(@Nullable ReportGroup group) {
        this.group = group;
    }

    @Nullable
    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(@Nullable Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    @Nullable
    public Boolean getRestAccessible() {
        return restAccessible;
    }

    public void setRestAccessible(@Nullable Boolean restAccessible) {
        this.restAccessible = restAccessible;
    }

    @Nullable
    public String getViewId() {
        return viewId;
    }

    public void setViewId(@Nullable String viewId) {
        this.viewId = viewId;
    }

    @Nullable
    public UserDetails getUser() {
        return user;
    }

    public void setUser(@Nullable UserDetails user) {
        this.user = user;
    }

    @Nullable
    public MetaClass getInputValueMetaClass() {
        return inputValueMetaClass;
    }

    public void setInputValueMetaClass(@Nullable MetaClass inputValueMetaClass) {
        this.inputValueMetaClass = inputValueMetaClass;
    }

    @Nullable
    public Boolean getSystem() {
        return system;
    }

    public void setSystem(@Nullable Boolean system) {
        this.system = system;
    }

    @Nullable
    public ReportOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(@Nullable ReportOutputType outputType) {
        this.outputType = outputType;
    }
}
