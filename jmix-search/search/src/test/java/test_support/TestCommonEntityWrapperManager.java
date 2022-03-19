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

package test_support;

import io.jmix.core.*;
import test_support.entity.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * Allows to create and modify test entities
 */
public class TestCommonEntityWrapperManager {

    protected final Metadata metadata;
    protected final DataManager dataManager;

    public TestCommonEntityWrapperManager(Metadata metadata, DataManager dataManager) {
        this.metadata = metadata;
        this.dataManager = dataManager;
    }

    public TestRootEntityWrapper createTestRootEntity() {
        return new TestRootEntityWrapper();
    }


    public TestRootEntityWrapper wrap(TestRootEntity instance) {
        return new TestRootEntityWrapper(reload(instance));
    }

    public TestReferenceEntityWrapper createTestReferenceEntity() {
        return new TestReferenceEntityWrapper();
    }

    public TestReferenceEntityWrapper wrap(TestReferenceEntity instance) {
        return new TestReferenceEntityWrapper(reload(instance));
    }

    public TestSubReferenceEntityWrapper createTestSubReferenceEntity() {
        return new TestSubReferenceEntityWrapper();
    }

    public TestSubReferenceEntityWrapper wrap(TestSubReferenceEntity instance) {
        return new TestSubReferenceEntityWrapper(reload(instance));
    }

    public TestRootEntityHDWrapper createTestRootEntityHD() {
        return new TestRootEntityHDWrapper();
    }


    public TestRootEntityHDWrapper wrap(TestRootEntityHD instance) {
        return new TestRootEntityHDWrapper(reload(instance));
    }

    public TestReferenceEntityHDWrapper createTestReferenceEntityHD() {
        return new TestReferenceEntityHDWrapper();
    }

    public TestReferenceEntityHDWrapper wrap(TestReferenceEntityHD instance) {
        return new TestReferenceEntityHDWrapper(reload(instance));
    }

    public TestSubReferenceEntityHDWrapper createTestSubReferenceEntityHD() {
        return new TestSubReferenceEntityHDWrapper();
    }

    public TestSubReferenceEntityHDWrapper wrap(TestSubReferenceEntityHD instance) {
        return new TestSubReferenceEntityHDWrapper(reload(instance));
    }

    public void save(Object... instances) {
        SaveContext saveContext = new SaveContext();
        Stream.of(instances).map(this::reload).forEach(saveContext::saving);
        dataManager.save(saveContext);
    }

    public void remove(Object... instances) {
        SaveContext saveContext = new SaveContext();
        Stream.of(instances).map(this::reload).forEach(saveContext::removing);
        dataManager.save(saveContext);
    }

    protected <T> T reload(T instance) {
        return dataManager.load(Id.of(instance)).one();
    }

    public abstract class AbstractEntityWrapper<T> {

        protected T instance;
        protected Set<Object> affectedInstances = new HashSet<>();

        protected AbstractEntityWrapper(Class<T> entityClass) {
            this.instance = metadata.create(entityClass);
        }

        protected AbstractEntityWrapper(T instance) {
            this.instance = instance;
        }

        public T done() {
            return instance;
        }

        public T save() {
            SaveContext saveContext = new SaveContext().saving(affectedInstances).saving(instance);
            EntitySet saved = dataManager.save(saveContext);
            return saved.get(instance);
        }
    }

    public class TestRootEntityWrapper extends AbstractEntityWrapper<TestRootEntity> {

        private TestRootEntityWrapper() {
            super(TestRootEntity.class);
            instance.setName("Test Root Entity");
        }

        private TestRootEntityWrapper(TestRootEntity instance) {
            super(instance);
        }

        public TestRootEntityWrapper setName(String name) {
            this.instance.setName(name);
            return this;
        }

        public TestRootEntityWrapper setTextValue(String textValue) {
            this.instance.setTextValue(textValue);
            return this;
        }

        public TestRootEntityWrapper setEnumValue(TestEnum enumValue) {
            this.instance.setEnumValue(enumValue);
            return this;
        }

        public TestRootEntityWrapper setIntValue(Integer intValue) {
            this.instance.setIntValue(intValue);
            return this;
        }

        public TestRootEntityWrapper setDateValue(Date dateValue) {
            this.instance.setDateValue(dateValue);
            return this;
        }

        public TestRootEntityWrapper setOneToOneAssociation(TestReferenceEntity reference) {
            this.instance.setOneToOneAssociation(reference);
            return this;
        }

        public TestRootEntityWrapper setOneToManyAssociation(TestReferenceEntity... references) {
            return setOneToManyAssociation(Arrays.asList(references));
        }

        public TestRootEntityWrapper setOneToManyAssociation(List<TestReferenceEntity> references) {
            List<TestReferenceEntity> currentOneToMany = this.instance.getOneToManyAssociation();
            if (currentOneToMany != null) {
                currentOneToMany.forEach(ref -> ref.setTestRootEntityManyToOne(null));
                affectedInstances.addAll(currentOneToMany);
            }
            this.instance.setOneToManyAssociation(references);
            references.stream().filter(Objects::nonNull).forEach(ref -> ref.setTestRootEntityManyToOne(instance));
            affectedInstances.addAll(references);
            return this;
        }

        public TestRootEntityWrapper setManyToManyAssociation(TestReferenceEntity... references) {
            return setManyToManyAssociation(Arrays.asList(references));
        }

        public TestRootEntityWrapper setManyToManyAssociation(List<TestReferenceEntity> references) {
            this.instance.setManyToManyAssociation(references);
            this.affectedInstances.addAll(references);
            return this;
        }
    }

    public class TestReferenceEntityWrapper extends AbstractEntityWrapper<TestReferenceEntity> {

        private TestReferenceEntityWrapper() {
            super(TestReferenceEntity.class);
            instance.setName("Test Reference Entity");
        }

        private TestReferenceEntityWrapper(TestReferenceEntity instance) {
            super(instance);
        }

        public TestReferenceEntityWrapper setName(String name) {
            this.instance.setName(name);
            return this;
        }

        public TestReferenceEntityWrapper setTextValue(String textValue) {
            this.instance.setTextValue(textValue);
            return this;
        }

        public TestReferenceEntityWrapper setEnumValue(TestEnum enumValue) {
            this.instance.setEnumValue(enumValue);
            return this;
        }

        public TestReferenceEntityWrapper setIntValue(Integer intValue) {
            this.instance.setIntValue(intValue);
            return this;
        }

        public TestReferenceEntityWrapper setDateValue(Date dateValue) {
            this.instance.setDateValue(dateValue);
            return this;
        }

        public TestReferenceEntityWrapper setOneToOneAssociation(TestSubReferenceEntity reference) {
            this.instance.setOneToOneAssociation(reference);
            return this;
        }

        public TestReferenceEntityWrapper setOneToManyAssociation(TestSubReferenceEntity... references) {
            return setOneToManyAssociation(Arrays.asList(references));
        }

        public TestReferenceEntityWrapper setOneToManyAssociation(List<TestSubReferenceEntity> references) {
            List<TestSubReferenceEntity> currentOneToMany = this.instance.getOneToManyAssociation();
            if (currentOneToMany != null) {
                currentOneToMany.forEach(ref -> ref.setTestReferenceEntityManyToOne(null));
                affectedInstances.addAll(currentOneToMany);
            }
            this.instance.setOneToManyAssociation(references);
            references.stream().filter(Objects::nonNull).forEach(ref -> ref.setTestReferenceEntityManyToOne(instance));
            affectedInstances.addAll(references);
            return this;
        }

        public TestReferenceEntityWrapper setManyToManyAssociation(TestSubReferenceEntity... references) {
            return setManyToManyAssociation(Arrays.asList(references));
        }

        public TestReferenceEntityWrapper setManyToManyAssociation(List<TestSubReferenceEntity> references) {
            this.instance.setManyToManyAssociation(references);
            this.affectedInstances.addAll(references);
            return this;
        }
    }

    public class TestSubReferenceEntityWrapper extends AbstractEntityWrapper<TestSubReferenceEntity> {

        private TestSubReferenceEntityWrapper() {
            super(TestSubReferenceEntity.class);
            instance.setName("Test Sub-Reference Entity");
        }

        private TestSubReferenceEntityWrapper(TestSubReferenceEntity instance) {
            super(instance);
        }

        public TestSubReferenceEntityWrapper setName(String name) {
            this.instance.setName(name);
            return this;
        }

        public TestSubReferenceEntityWrapper setTextValue(String textValue) {
            this.instance.setTextValue(textValue);
            return this;
        }

        public TestSubReferenceEntityWrapper setEnumValue(TestEnum enumValue) {
            this.instance.setEnumValue(enumValue);
            return this;
        }

        public TestSubReferenceEntityWrapper setIntValue(Integer intValue) {
            this.instance.setIntValue(intValue);
            return this;
        }

        public TestSubReferenceEntityWrapper setDateValue(Date dateValue) {
            this.instance.setDateValue(dateValue);
            return this;
        }
    }

    public class TestRootEntityHDWrapper extends AbstractEntityWrapper<TestRootEntityHD> {

        private TestRootEntityHDWrapper() {
            super(TestRootEntityHD.class);
            instance.setName("Test Root Entity Hard Delete");
        }

        private TestRootEntityHDWrapper(TestRootEntityHD instance) {
            super(instance);
        }

        public TestRootEntityHDWrapper setName(String name) {
            this.instance.setName(name);
            return this;
        }

        public TestRootEntityHDWrapper setTextValue(String textValue) {
            this.instance.setTextValue(textValue);
            return this;
        }

        public TestRootEntityHDWrapper setEnumValue(TestEnum enumValue) {
            this.instance.setEnumValue(enumValue);
            return this;
        }

        public TestRootEntityHDWrapper setIntValue(Integer intValue) {
            this.instance.setIntValue(intValue);
            return this;
        }

        public TestRootEntityHDWrapper setDateValue(Date dateValue) {
            this.instance.setDateValue(dateValue);
            return this;
        }

        public TestRootEntityHDWrapper setOneToOneAssociation(TestReferenceEntityHD reference) {
            this.instance.setOneToOneAssociation(reference);
            return this;
        }

        public TestRootEntityHDWrapper setOneToManyAssociation(TestReferenceEntityHD... references) {
            return setOneToManyAssociation(Arrays.asList(references));
        }

        public TestRootEntityHDWrapper setOneToManyAssociation(List<TestReferenceEntityHD> references) {
            List<TestReferenceEntityHD> currentOneToMany = this.instance.getOneToManyAssociation();
            if (currentOneToMany != null) {
                currentOneToMany.forEach(ref -> ref.setTestRootEntityManyToOne(null));
                affectedInstances.addAll(currentOneToMany);
            }
            this.instance.setOneToManyAssociation(references);
            references.stream().filter(Objects::nonNull).forEach(ref -> ref.setTestRootEntityManyToOne(instance));
            affectedInstances.addAll(references);
            return this;
        }

        public TestRootEntityHDWrapper setManyToManyAssociation(TestReferenceEntityHD... references) {
            return setManyToManyAssociation(Arrays.asList(references));
        }

        public TestRootEntityHDWrapper setManyToManyAssociation(List<TestReferenceEntityHD> references) {
            this.instance.setManyToManyAssociation(references);
            this.affectedInstances.addAll(references);
            return this;
        }
    }

    public class TestReferenceEntityHDWrapper extends AbstractEntityWrapper<TestReferenceEntityHD> {

        private TestReferenceEntityHDWrapper() {
            super(TestReferenceEntityHD.class);
            instance.setName("Test Reference Entity Hard Delete");
        }

        private TestReferenceEntityHDWrapper(TestReferenceEntityHD instance) {
            super(instance);
        }

        public TestReferenceEntityHDWrapper setName(String name) {
            this.instance.setName(name);
            return this;
        }

        public TestReferenceEntityHDWrapper setTextValue(String textValue) {
            this.instance.setTextValue(textValue);
            return this;
        }

        public TestReferenceEntityHDWrapper setEnumValue(TestEnum enumValue) {
            this.instance.setEnumValue(enumValue);
            return this;
        }

        public TestReferenceEntityHDWrapper setIntValue(Integer intValue) {
            this.instance.setIntValue(intValue);
            return this;
        }

        public TestReferenceEntityHDWrapper setDateValue(Date dateValue) {
            this.instance.setDateValue(dateValue);
            return this;
        }

        public TestReferenceEntityHDWrapper setOneToOneAssociation(TestSubReferenceEntityHD reference) {
            this.instance.setOneToOneAssociation(reference);
            return this;
        }

        public TestReferenceEntityHDWrapper setOneToManyAssociation(TestSubReferenceEntityHD... references) {
            return setOneToManyAssociation(Arrays.asList(references));
        }

        public TestReferenceEntityHDWrapper setOneToManyAssociation(List<TestSubReferenceEntityHD> references) {
            List<TestSubReferenceEntityHD> currentOneToMany = this.instance.getOneToManyAssociation();
            if (currentOneToMany != null) {
                currentOneToMany.forEach(ref -> ref.setTestReferenceEntityManyToOne(null));
                affectedInstances.addAll(currentOneToMany);
            }
            this.instance.setOneToManyAssociation(references);
            references.stream().filter(Objects::nonNull).forEach(ref -> ref.setTestReferenceEntityManyToOne(instance));
            affectedInstances.addAll(references);
            return this;
        }

        public TestReferenceEntityHDWrapper setManyToManyAssociation(TestSubReferenceEntityHD... references) {
            return setManyToManyAssociation(Arrays.asList(references));
        }

        public TestReferenceEntityHDWrapper setManyToManyAssociation(List<TestSubReferenceEntityHD> references) {
            this.instance.setManyToManyAssociation(references);
            this.affectedInstances.addAll(references);
            return this;
        }
    }

    public class TestSubReferenceEntityHDWrapper extends AbstractEntityWrapper<TestSubReferenceEntityHD> {

        private TestSubReferenceEntityHDWrapper() {
            super(TestSubReferenceEntityHD.class);
            instance.setName("Test Sub-Reference Entity Hard Delete");
        }

        private TestSubReferenceEntityHDWrapper(TestSubReferenceEntityHD instance) {
            super(instance);
        }

        public TestSubReferenceEntityHDWrapper setName(String name) {
            this.instance.setName(name);
            return this;
        }

        public TestSubReferenceEntityHDWrapper setTextValue(String textValue) {
            this.instance.setTextValue(textValue);
            return this;
        }

        public TestSubReferenceEntityHDWrapper setEnumValue(TestEnum enumValue) {
            this.instance.setEnumValue(enumValue);
            return this;
        }

        public TestSubReferenceEntityHDWrapper setIntValue(Integer intValue) {
            this.instance.setIntValue(intValue);
            return this;
        }

        public TestSubReferenceEntityHDWrapper setDateValue(Date dateValue) {
            this.instance.setDateValue(dateValue);
            return this;
        }
    }
}
