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

package test_support.entity.deserialization_bug;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.UUID;

@JmixEntity
@Table(name = "JMIXALL.DATA.TEST_CYCLIC_PARENT")
@Entity(name = "jmixall.data.test_CyclicParent")
public class CyclicParent {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Composition
    @OneToMany(mappedBy = "parent")
    private Set<CyclicChild> children;


    public Set<CyclicChild> getChildren() {
        return children;
    }

    public void setChildren(Set<CyclicChild> children) {
        this.children = children;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    //writeObject/readObject methods should not be added at enhancing if at least one of them already exists
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }
}