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

package io.jmix.ui.relatedentities;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import java.util.Collection;

public class RelatedEntitiesClassBuilder<S extends Screen> extends RelatedEntitiesBuilder {

    protected Class<S> screenClass;

    public RelatedEntitiesClassBuilder(RelatedEntitiesBuilder builder, Class<S> screenClass) {
        super(builder);

        this.screenClass = screenClass;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withProperty(String property) {
        super.withProperty(property);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withMetaProperty(MetaProperty metaProperty) {
        super.withMetaProperty(metaProperty);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withEntityClass(Class entityClass) {
        super.withEntityClass(entityClass);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withOpenMode(OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withScreenId(String screenId) {
        throw new IllegalStateException("RelatedEntitiesClassBuilder does not support screenId");
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withOptions(ScreenOptions options) {
        super.withOptions(options);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withMetaClass(MetaClass metaClass) {
        super.withMetaClass(metaClass);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withSelectedEntities(Collection<?> selectedEntities) {
        super.withSelectedEntities(selectedEntities);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withConfigurationName(String configurationName) {
        super.withConfigurationName(configurationName);
        return this;
    }

    /**
     * @return screen class
     */
    public Class<S> getScreenClass() {
        return screenClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S build() {
        return (S) this.handler.apply(this);
    }
}
