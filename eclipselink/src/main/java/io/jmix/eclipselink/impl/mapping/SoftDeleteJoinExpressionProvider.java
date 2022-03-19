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

package io.jmix.eclipselink.impl.mapping;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.ManyToOneMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.OneToOne;

/**
 * Generates expression to support soft delete feature in Jmix.
 */
@Component("eclipselink_SoftDeleteJoinExpressionProvider")
public class SoftDeleteJoinExpressionProvider extends AbstractJoinExpressionProvider {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    protected Expression processOneToManyMapping(OneToManyMapping mapping) {
        if (metadataTools.isSoftDeletable(mapping.getReferenceClass())) {
            String deletedDateProperty = metadataTools.findDeletedDateProperty(mapping.getReferenceClass());
            return new ExpressionBuilder().get(deletedDateProperty).isNull();
        }
        return null;
    }

    @Override
    protected Expression processOneToOneMapping(OneToOneMapping mapping) {
        MetaClass metaClass = metadata.getClass(mapping.getDescriptor().getJavaClass());
        MetaProperty metaProperty = metaClass.getProperty(mapping.getAttributeName());

        if (metadataTools.isSoftDeletable(mapping.getReferenceClass())) {
            OneToOne oneToOne = metaProperty.getAnnotatedElement().getAnnotation(OneToOne.class);
            if (oneToOne != null && !Strings.isNullOrEmpty(oneToOne.mappedBy())) {
                String deletedDateProperty = metadataTools.findDeletedDateProperty(mapping.getReferenceClass());
                return new ExpressionBuilder().get(deletedDateProperty).isNull();
            }
        }
        return null;
    }

    @Override
    protected Expression processManyToOneMapping(ManyToOneMapping mapping) {
        return null;
    }

    @Override
    protected Expression processManyToManyMapping(ManyToManyMapping mapping) {
        return null;
    }
}
