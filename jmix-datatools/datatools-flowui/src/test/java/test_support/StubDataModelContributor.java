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

package test_support;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.datatools.datamodel.AttributeDescriptor;
import io.jmix.datatools.datamodel.DataModelContributor;
import io.jmix.datatools.datamodel.EntityDescriptor;
import io.jmix.datatools.datamodel.RelationType;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.DataModelHostEntity;
import test_support.entity.DataModelStubEntity;

import java.util.Collection;
import java.util.List;

public class StubDataModelContributor implements DataModelContributor {

    public static final String STUB_STORE = "main";
    public static final String STUB_TABLE = "STUB_ENTITY";
    public static final String HOST_SIDE_TABLE = "DYN_TEST_DATA_MODEL_HOST_ENTITY";

    @Autowired
    protected Metadata metadata;

    @Override
    public Collection<MetaClass> getAdditionalEntities() {
        return List.of(metadata.getClass(DataModelStubEntity.class));
    }

    @Nullable
    @Override
    public EntityDescriptor describeEntity(MetaClass metaClass) {
        if (!DataModelStubEntity.class.equals(metaClass.getJavaClass())) {
            return null;
        }
        return new EntityDescriptor(STUB_STORE, STUB_TABLE, true);
    }

    @Nullable
    @Override
    public AttributeDescriptor describeAttribute(MetaClass entity, MetaProperty property) {
        if (DataModelStubEntity.class.equals(entity.getJavaClass())) {
            return describeStubAttribute(property);
        }
        if (DataModelHostEntity.class.equals(entity.getJavaClass())) {
            return describeHostAttribute(property);
        }
        return null;
    }

    @Nullable
    protected AttributeDescriptor describeStubAttribute(MetaProperty property) {
        return switch (property.getName()) {
            case "name" -> new AttributeDescriptor("name", "String", true, true,
                    new AttributeDescriptor.ColumnDescriptor(STUB_TABLE, "NAME", "varchar(50)"),
                    null,
                    new AttributeDescriptor.RelationDescriptor(
                            RelationType.MANY_TO_ONE, "test_DataModelHostEntity"));
            case "calculatedName" -> new AttributeDescriptor("calculatedName", "String", false, true,
                    null, "calculated", null);
            default -> null;
        };
    }

    @Nullable
    protected AttributeDescriptor describeHostAttribute(MetaProperty property) {
        if (!"sideValue".equals(property.getName())) {
            return null;
        }
        return new AttributeDescriptor("sideValue", "String", false, true,
                new AttributeDescriptor.ColumnDescriptor(HOST_SIDE_TABLE, "SIDE_VALUE", "varchar(100)"),
                null, null);
    }
}
