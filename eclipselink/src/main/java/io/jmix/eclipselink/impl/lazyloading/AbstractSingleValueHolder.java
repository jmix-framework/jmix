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

package io.jmix.eclipselink.impl.lazyloading;

import io.jmix.core.EntityAttributeVisitor;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.springframework.beans.factory.BeanFactory;

public abstract class AbstractSingleValueHolder extends AbstractValueHolder {
    private static final long serialVersionUID = -6300542559295657659L;

    public AbstractSingleValueHolder(BeanFactory beanFactory,
                                     ValueHolderInterface originalValueHolder,
                                     Object owner,
                                     MetaProperty metaProperty) {
        super(beanFactory, originalValueHolder, owner, metaProperty);
    }

    @Override
    protected void afterLoadValue(Object value) {
        if (value != null) {
            getMetadataTools().traverseAttributes(value, new SingleValuePropertyVisitor());
        }
    }

    protected class SingleValuePropertyVisitor implements EntityAttributeVisitor {
        @Override
        public void visit(Object entity, MetaProperty property) {
            MetadataTools metadataTools = getMetadataTools();
            if (metadataTools.isJpa(property) && !metadataTools.isEmbedded(property)) {
                MetaClass propertyClass = property.getRange().asClass();
                if (propertyClass.getJavaClass().isAssignableFrom(getOwner().getClass())) {
                    replaceToExistingReferences(entity, property, getOwner());
                }

                Object value = getLoadOptions().getHints().get(PersistenceHints.SOFT_DELETION);
                if (value == null || Boolean.TRUE.equals(value)) {
                    replaceLoadOptions(entity, property);
                }
            }
        }

        @Override
        public boolean skip(MetaProperty property) {
            return !property.getRange().isClass();
        }
    }
}
