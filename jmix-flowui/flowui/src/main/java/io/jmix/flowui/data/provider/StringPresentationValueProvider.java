/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.data.provider;

import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.lang.Nullable;

/**
 * Provides string representations of property values for entities.
 * <p>
 * This class implements the {@link ValueProvider} interface to retrieve and format property values
 * from an entity based on a given {@link MetaPropertyPath}. It utilizes {@link MetadataTools} to
 * handle the formatting of the property values.
 *
 * @param <T> the type of the entity whose property values are being provided
 */
public class StringPresentationValueProvider<T> implements ValueProvider<T, String> {

    protected MetaPropertyPath propertyPath;
    protected MetadataTools metadataTools;

    public StringPresentationValueProvider(@Nullable MetaPropertyPath propertyPath, MetadataTools metadataTools) {
        this.propertyPath = propertyPath;
        this.metadataTools = metadataTools;
    }

    @Override
    public String apply(T entity) {
        Object value = EntityValues.getValueEx(entity, propertyPath);
        return propertyPath != null
                ? metadataTools.format(value, propertyPath.getMetaProperty())
                : metadataTools.format(value);
    }
}
