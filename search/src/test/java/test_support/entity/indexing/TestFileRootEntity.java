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
import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import test_support.entity.BaseEntity;

import javax.persistence.*;
import java.util.List;

@JmixEntity
@Table(name = "TEST_FILE_ROOT_ENTITY")
@Entity(name = "test_FileRootEntity")
public class TestFileRootEntity extends BaseEntity {
    @InstanceName
    @Column(name = "NAME")
    private String name;

    @PropertyDatatype("fileRef")
    @Column(name = "FILE_VALUE")
    private FileRef fileValue;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumn(name = "ONE_TO_ONE_REF_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private TestFileRefEntity oneToOneRef;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "manyToOneRef")
    private List<TestFileRefEntity> oneToManyRef;

    public List<TestFileRefEntity> getOneToManyRef() {
        return oneToManyRef;
    }

    public void setOneToManyRef(List<TestFileRefEntity> oneToManyRef) {
        this.oneToManyRef = oneToManyRef;
    }

    public TestFileRefEntity getOneToOneRef() {
        return oneToOneRef;
    }

    public void setOneToOneRef(TestFileRefEntity oneToOneRef) {
        this.oneToOneRef = oneToOneRef;
    }

    public FileRef getFileValue() {
        return fileValue;
    }

    public void setFileValue(FileRef fileValue) {
        this.fileValue = fileValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}