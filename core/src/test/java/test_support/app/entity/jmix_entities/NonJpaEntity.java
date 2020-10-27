/*
 * Copyright 2020 Haulmont.
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

package test_support.app.entity.jmix_entities;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@JmixEntity(name = "test_nonJpaEntity")
public class NonJpaEntity {

    private String name;

    private List<EntityWithJmix> entities;

    @Transient
    private Date oddDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EntityWithJmix> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityWithJmix> entities) {
        this.entities = entities;
    }

    public Date getOddDate() {
        return oddDate;
    }

    public void setOddDate(Date oddDate) {
        this.oddDate = oddDate;
    }
}
