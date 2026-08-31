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

package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.router.BeforeEnterEvent;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.jspecify.annotations.Nullable;

/**
 * Base class of entity read views: views that show an entity instance and cannot edit it.
 *
 * @param <E> entity class
 */
public class StandardReadView<E> extends StandardView implements ReadView<E> {

    public static final String DEFAULT_ROUTE_PARAM = "id";

    @Nullable
    private String serializedEntityIdToRead;

    /**
     * Create views using {@link ViewNavigators} or {@link DialogWindows}.
     */
    @Internal
    public StandardReadView() {
        addBeforeShowListener(this::onBeforeShow);
    }

    private void onBeforeShow(BeforeShowEvent event) {
        setupEntityToRead();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        findEntityId(event);

        super.beforeEnter(event);
    }

    @Internal
    @Override
    protected void processBeforeEnterInternal(BeforeEnterEvent event) {
        super.processBeforeEnterInternal(event);

        findEntityId(event);
    }

    protected void findEntityId(BeforeEnterEvent event) {
        String routeParamName = getRouteParamName();
        serializedEntityIdToRead = event.getRouteParameters().get(routeParamName)
                .orElseThrow(() ->
                        new IllegalStateException(String.format("Route parameter '%s' not found",
                                routeParamName)));
    }

    protected String getRouteParamName() {
        return DEFAULT_ROUTE_PARAM;
    }

    /**
     * Invoked on {@link BeforeShowEvent}. Sets the entity id on the loader; the load itself is performed
     * by the {@code dataLoadCoordinator} facet.
     */
    protected void setupEntityToRead() {
        if (serializedEntityIdToRead != null) {
            Object entityId = getUrlParamSerializer().deserialize(getSerializedIdType(),
                    serializedEntityIdToRead);
            getEntityLoader().setEntityId(entityId);
        }
    }

    @Override
    public void setEntityToRead(E entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nullable
    @Override
    public E getEntityOrNull() {
        return getEntityContainer().getItemOrNull();
    }

    protected InstanceContainer<E> getEntityContainer() {
        ReadEntityContainer annotation = getClass().getAnnotation(ReadEntityContainer.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    String.format("'%s' does not declare @%s", getClass(),
                            ReadEntityContainer.class.getSimpleName())
            );
        }

        if (annotation.value().contains(".")) {
            throw new UnsupportedOperationException(
                    String.format("Can't obtain shown entity container with id: '%s'", annotation.value()));
        }

        return getViewData().getContainer(annotation.value());
    }

    @SuppressWarnings("unchecked")
    protected InstanceLoader<E> getEntityLoader() {
        InstanceContainer<E> container = getEntityContainer();
        DataLoader loader = null;

        if (container instanceof HasLoader containerWithLoader) {
            loader = containerWithLoader.getLoader();
        }

        if (loader == null) {
            throw new IllegalStateException("Loader of shown entity container not found");
        }

        if (!(loader instanceof InstanceLoader)) {
            throw new IllegalStateException(String.format(
                    "Loader %s of shown entity container %s must implement %s",
                    loader, container, InstanceLoader.class.getSimpleName()));
        }

        return (InstanceLoader<E>) loader;
    }

    private Class<?> getSerializedIdType() {
        MetaClass entityMetaClass = getEntityContainer().getEntityMetaClass();
        MetaProperty primaryKeyProperty = getMetadataTools().getPrimaryKeyProperty(entityMetaClass);
        if (primaryKeyProperty == null) {
            throw new IllegalStateException(String.format(
                    "Entity %s has no primary key", entityMetaClass.getName()));
        }

        return primaryKeyProperty.getJavaType();
    }

    private MetadataTools getMetadataTools() {
        return getApplicationContext().getBean(MetadataTools.class);
    }

    private UrlParamSerializer getUrlParamSerializer() {
        return getApplicationContext().getBean(UrlParamSerializer.class);
    }
}
