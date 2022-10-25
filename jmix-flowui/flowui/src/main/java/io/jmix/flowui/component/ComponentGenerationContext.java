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

package io.jmix.flowui.component;

import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.model.CollectionContainer;
import org.dom4j.Element;

import javax.annotation.Nullable;

/**
 * Contains information for {@link ComponentGenerationStrategy} when creating
 * components using {@link UiComponentsGenerator}.
 */
@SuppressWarnings("rawtypes")
public class ComponentGenerationContext {

    protected final MetaClass metaClass;
    protected final String property;

    protected ValueSource valueSource;
    protected Element xmlDescriptor;
    protected Class<?> targetClass;

    protected DataProvider items;
    protected Class<?> enumItems;
    protected CollectionContainer<?> collectionItems;

    /**
     * Creates an instance of ComponentGenerationContext.
     *
     * @param metaClass the entity for which the component is created
     * @param property  the entity attribute for which the component is created
     */
    public ComponentGenerationContext(MetaClass metaClass, String property) {
        this.metaClass = metaClass;
        this.property = property;
    }

    /**
     * @return the entity for which the component is created
     */
    public MetaClass getMetaClass() {
        return metaClass;
    }

    /**
     * @return the entity attribute for which the component is created
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return a value source that can be used to create the component
     */
    @Nullable
    public ValueSource getValueSource() {
        return valueSource;
    }

    /**
     * Sets a value source, using fluent API method.
     *
     * @param valueSource a value source to set
     * @return this object
     */
    public ComponentGenerationContext setValueSource(@Nullable ValueSource valueSource) {
        this.valueSource = valueSource;
        return this;
    }

    @Nullable
    public DataProvider getItems() {
        return items;
    }

    public ComponentGenerationContext setItems(@Nullable DataProvider items) {
        this.items = items;
        return this;
    }

    @Nullable
    public Class<?> getEnumItems() {
        return enumItems;
    }

    public ComponentGenerationContext setEnumItems(@Nullable Class<?> enumItems) {
        this.enumItems = enumItems;
        return this;
    }

    @Nullable
    public CollectionContainer<?> getCollectionItems() {
        return collectionItems;
    }

    public ComponentGenerationContext setCollectionItems(@Nullable CollectionContainer<?> collectionItems) {
        this.collectionItems = collectionItems;
        return this;
    }

    /**
     * @return an XML descriptor which contains additional information
     */
    @Nullable
    public Element getXmlDescriptor() {
        return xmlDescriptor;
    }

    /**
     * Sets an XML descriptor which contains additional information, using fluent API method.
     *
     * @param xmlDescriptor an XML descriptor which contains additional information
     * @return this object
     */
    public ComponentGenerationContext setXmlDescriptor(@Nullable Element xmlDescriptor) {
        this.xmlDescriptor = xmlDescriptor;
        return this;
    }

    /**
     * @return a target class for which a component is created
     */
    @Nullable
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * Sets a target class for which a component is created, using fluent API method.
     * For instance, a target class can be a component or a screen.
     *
     * @param targetClass a target class for which a component is created
     * @return this object
     */
    public ComponentGenerationContext setTargetClass(@Nullable Class<?> targetClass) {
        this.targetClass = targetClass;
        return this;
    }
}
