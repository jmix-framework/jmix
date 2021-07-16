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

import io.jmix.core.FileRef;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.PropertyDatatype;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@JmixEntity
@Entity(name = "test_SimpleFileRootEntity")
@Table(name = "TEST_SIMPLE_FILE_ROOT_ENTITY")
public class TestSimpleFileRootEntity extends BaseEntity {

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @PropertyDatatype("fileRef")
    @Column(name = "FILE_VALUE")
    private FileRef fileValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileRef getFileValue() {
        return fileValue;
    }

    public void setFileValue(FileRef fileValue) {
        this.fileValue = fileValue;
    }
}
