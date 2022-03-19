/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "ref$Plant")
@JmixEntity
@Table(name = "REF_PLANT")
public class Plant extends StandardEntity {

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    @ManyToMany
    @JoinTable(name = "REF_PLANT_MODEL_LINK",
            joinColumns = @JoinColumn(name = "PLANT_ID"),
            inverseJoinColumns = @JoinColumn(name = "MODEL_ID"))
    protected Set<Model> models;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOC_ID")
    protected Doc doc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Model> getModels() {
        return models;
    }

    public void setModels(Set<Model> models) {
        this.models = models;
    }

    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }
}
