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

package test_support.entity.indexing;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;
import test_support.entity.TestEmbeddableEntity;

import javax.persistence.*;
import java.util.List;

@JmixEntity
@Table(name = "TEST_EMB_ROOT_ENTITY")
@Entity(name = "test_EmbRootEntity")
public class TestEmbeddedRootEntity extends BaseEntity {
    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    @AttributeOverrides({
            @AttributeOverride(name = "textValue", column = @Column(name = "EMBEDDED_TEXT_VALUE")),
            @AttributeOverride(name = "enumValue", column = @Column(name = "EMBEDDED_ENUM_VALUE")),
            @AttributeOverride(name = "intValue", column = @Column(name = "EMBEDDED_INT_VALUE"))
    })
    private TestEmbeddableEntity embedded;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumn(name = "ONE_TO_ONE_REF_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private TestEmbeddedRefEntity oneToOneRef;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "manyToOneRef")
    private List<TestEmbeddedRefEntity> oneToManyRef;

    public List<TestEmbeddedRefEntity> getOneToManyRef() {
        return oneToManyRef;
    }

    public void setOneToManyRef(List<TestEmbeddedRefEntity> oneToManyRef) {
        this.oneToManyRef = oneToManyRef;
    }

    public TestEmbeddedRefEntity getOneToOneRef() {
        return oneToOneRef;
    }

    public void setOneToOneRef(TestEmbeddedRefEntity oneToOneRef) {
        this.oneToOneRef = oneToOneRef;
    }

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