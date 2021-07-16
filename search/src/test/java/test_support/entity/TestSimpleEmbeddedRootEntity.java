/*
 * Copyright 2021 Haulmont.
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

package test_support.entity;

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@JmixEntity
@Entity(name = "test_SimpleEmbRootEntity")
@Table(name = "TEST_SIMPLE_EMB_ROOT_ENTITY")
public class TestSimpleEmbeddedRootEntity extends BaseEntity {

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    @AttributeOverrides({
            @AttributeOverride(name = "textValue", column = @Column(name = "EMBEDDED_TEXT_VALUE")),
            @AttributeOverride(name = "enumValue", column = @Column(name = "EMBEDDED_ENUM_VALUE"))
    })
    private TestEmbeddableEntity embedded;

    public TestEmbeddableEntity getEmbedded() {
        return embedded;
    }

    public void setEmbedded(TestEmbeddableEntity embedded) {
        this.embedded = embedded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
