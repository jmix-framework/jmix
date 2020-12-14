/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.compatibility;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.formatter.Formatter;

import javax.annotation.Nullable;

/**
 * Converts {@code captionProperty} XML attribute to item/options caption provider.
 */
public class CaptionAdapter implements Formatter<Object> {

    protected String captionProperty;

    protected Metadata metadata;
    protected MetadataTools metadataTools;

    public CaptionAdapter(@Nullable String captionProperty, Metadata metadata, MetadataTools metadataTools) {
        this.captionProperty = captionProperty;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    @Override
    public String apply(Object entity) {
        if (!(entity instanceof Entity)) {
            return "";
        }

        if (captionProperty != null) {

            MetaClass entityMetaClass;
            if (entity instanceof KeyValueEntity) {
                entityMetaClass = ((KeyValueEntity) entity).getInstanceMetaClass();
            } else {
                entityMetaClass = metadata.getClass(entity);
            }

            if (entityMetaClass.getPropertyPath(captionProperty) == null) {
                throw new IllegalArgumentException(String.format("Couldn't find property with name '%s'", captionProperty));
            }

            Object propertyValue = EntityValues.getValueEx(entity, captionProperty);
            return propertyValue != null
                    ? propertyValue.toString()
                    : " ";
        }

        return metadataTools.getInstanceName(entity);
    }

    @Nullable
    public String getCaptionProperty() {
        return captionProperty;
    }

    public void setCaptionProperty(@Nullable String captionProperty) {
        this.captionProperty = captionProperty;
    }
}
