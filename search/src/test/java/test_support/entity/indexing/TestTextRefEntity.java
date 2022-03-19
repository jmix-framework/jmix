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
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import javax.persistence.*;
import java.util.List;

@JmixEntity
@Table(name = "TEST_TEXT_REF_ENTITY")
@Entity(name = "test_TextRefEntity")
public class TestTextRefEntity extends BaseEntity {
    @InstanceName
    @Column(name = "NAME")
    private String name;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumn(name = "ONE_TO_ONE_REF_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private TestTextSubRefEntity oneToOneRef;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "manyToOneRef")
    private List<TestTextSubRefEntity> oneToManyRef;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oneToOneRef")
    private TestTextRootEntity inverseOneToOneRef;

    @JoinColumn(name = "MANY_TO_ONE_REF_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TestTextRootEntity manyToOneRef;

    public TestTextRootEntity getManyToOneRef() {
        return manyToOneRef;
    }

    public void setManyToOneRef(TestTextRootEntity manyToOneRef) {
        this.manyToOneRef = manyToOneRef;
    }

    public TestTextRootEntity getInverseOneToOneRef() {
        return inverseOneToOneRef;
    }

    public void setInverseOneToOneRef(TestTextRootEntity inverseOneToOneRef) {
        this.inverseOneToOneRef = inverseOneToOneRef;
    }

    public List<TestTextSubRefEntity> getOneToManyRef() {
        return oneToManyRef;
    }

    public void setOneToManyRef(List<TestTextSubRefEntity> oneToManyRef) {
        this.oneToManyRef = oneToManyRef;
    }

    public TestTextSubRefEntity getOneToOneRef() {
        return oneToOneRef;
    }

    public void setOneToOneRef(TestTextSubRefEntity oneToOneRef) {
        this.oneToOneRef = oneToOneRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}