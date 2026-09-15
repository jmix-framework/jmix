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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.datatools.datamodel.AttributeDescriptor;
import io.jmix.datatools.datamodel.DataModelContributor;
import io.jmix.datatools.datamodel.EntityDescriptor;

import java.util.Collection;

/**
 * A misbehaving contributor: every call fails. It is registered before
 * {@link StubDataModelContributor}, so the whole data model test suite runs with it in place and
 * shows that one broken contributor does not affect the rest of the data model.
 */
public class ThrowingDataModelContributor implements DataModelContributor {

    public static final String FAILURE_MESSAGE = "Deliberate contributor failure";

    @Override
    public Collection<MetaClass> getAdditionalEntities() {
        throw new IllegalStateException(FAILURE_MESSAGE);
    }

    @Override
    public EntityDescriptor describeEntity(MetaClass metaClass) {
        throw new IllegalStateException(FAILURE_MESSAGE);
    }

    @Override
    public AttributeDescriptor describeAttribute(MetaClass entity, MetaProperty property) {
        throw new IllegalStateException(FAILURE_MESSAGE);
    }
}
