/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core.model.selfinherited;

import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@Table(name = "TEST_CHILD_ENTITY_REFERRER")
@Entity(name = "test$ChildEntityReferrer")
@JmixEntity
public class ChildEntityReferrer extends StandardEntity {
    private static final long serialVersionUID = 3582114532586946444L;

    @Column(name = "INFO")
    protected String info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ENTITY_ID")
    protected ChildEntity childEntity;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ChildEntity getChildEntity() {
        return childEntity;
    }

    public void setChildEntity(ChildEntity childEntity) {
        this.childEntity = childEntity;
    }
}
