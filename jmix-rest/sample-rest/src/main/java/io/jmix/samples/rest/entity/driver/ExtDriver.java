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

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.entity.annotation.ReplaceEntity;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

@Entity(name = "ref$ExtDriver")
@JmixEntity
@ReplaceEntity(Driver.class)
public class ExtDriver extends Driver {

    private static final long serialVersionUID = 5271478633053259678L;

    @Column(name = "INFO", length = 50)
    protected String info;

    // the field is so large that we don't want it to be loaded automatically
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "NOTES")
    @Lob
    protected String notes;

    @InstanceName
    public String getCaption() {
        return String.format("%s:(%s)", getName(), getInfo());
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
