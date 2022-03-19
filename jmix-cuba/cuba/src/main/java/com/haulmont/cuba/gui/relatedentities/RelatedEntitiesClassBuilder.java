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

package com.haulmont.cuba.gui.relatedentities;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.screen.OpenMode;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.relatedentities.RelatedEntitiesBuilder;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;

import javax.annotation.Nullable;
import java.util.Collection;

@Deprecated
public class RelatedEntitiesClassBuilder<S extends Screen>
        extends io.jmix.ui.relatedentities.RelatedEntitiesClassBuilder<S> {

    public RelatedEntitiesClassBuilder(RelatedEntitiesBuilder builder, Class<S> screenClass) {
        super(builder, screenClass);
    }

    /**
     * Sets {@link Screens.LaunchMode} for the related entities screen and returns the builder for chaining.
     * <p>
     * For example: {@code builder.withLaunchMode(OpenMode.DIALOG).build();}
     *
     * @param launchMode launch mode
     * @return current instance of builder
     * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
     */
    @Deprecated
    public RelatedEntitiesClassBuilder<S> withLaunchMode(Screens.LaunchMode launchMode) {
        Preconditions.checkArgument(launchMode instanceof OpenMode,
                "Unsupported LaunchMode " + launchMode);

        withOpenMode(((OpenMode) launchMode).getOpenMode());
        return this;
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
    public RelatedEntitiesClassBuilder<S> withOpenMode(io.jmix.ui.screen.OpenMode openMode) {
        super.withOpenMode(openMode);
        return this;
    }

    @Override
    public RelatedEntitiesClassBuilder<S> withScreenId(String screenId) {
        return (RelatedEntitiesClassBuilder<S>) super.withScreenId(screenId);
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

    /**
     * Sets caption to filter in opened screen.
     *
     * @param filterCaption caption
     * @return current instance of builder
     * @deprecated Use {@link #withConfigurationName(String)} instead
     */
    @Deprecated
    public RelatedEntitiesClassBuilder<S> withFilterCaption(String filterCaption) {
        super.withConfigurationName(filterCaption);
        return this;
    }

    /**
     * Returns launch mode set by {@link #withLaunchMode(Screens.LaunchMode)}.
     *
     * @deprecated Use {@link #getOpenMode()} instead
     */
    @Deprecated
    public Screens.LaunchMode getLaunchMode() {
        return OpenMode.from(getOpenMode());
    }

    /**
     * @return filter caption set by {@link #withFilterCaption(String)}
     * @deprecated Use {@link #getConfigurationName()} instead
     */
    @Deprecated
    @Nullable
    public String getFilterCaption() {
        return super.getConfigurationName();
    }
}
