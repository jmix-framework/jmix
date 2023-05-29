/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.core.model.common;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * User access group.
 */
@Entity(name = "test$Group")
@JmixEntity
@Table(name = "TEST_GROUP")
@NamePattern("%s|name")
public class Group extends StandardEntity {

    private static final long serialVersionUID = -4581386806900761785L;

    @Column(name = "NAME", nullable = false, unique = true)
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    protected Group parent;

    @OneToMany(mappedBy = "group")
    @OrderBy("level")
    protected List<GroupHierarchy> hierarchyList;

    @OneToMany(mappedBy = "group")
    @Composition()
    @OnDelete(DeletePolicy.CASCADE)
    protected Set<Constraint> constraints;

    @OneToMany(mappedBy = "group")
    @Composition()
    @OnDelete(DeletePolicy.CASCADE)
    protected Set<SessionAttribute> sessionAttributes;

    @Transient
    protected boolean predefined;

    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    public List<GroupHierarchy> getHierarchyList() {
        return hierarchyList;
    }

    public void setHierarchyList(List<GroupHierarchy> hierarchyList) {
        this.hierarchyList = hierarchyList;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    public Set<SessionAttribute> getSessionAttributes() {
        return sessionAttributes;
    }

    public void setSessionAttributes(Set<SessionAttribute> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }

    public boolean isPredefined() {
        return predefined;
    }

    public void setPredefined(boolean predefined) {
        this.predefined = predefined;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
