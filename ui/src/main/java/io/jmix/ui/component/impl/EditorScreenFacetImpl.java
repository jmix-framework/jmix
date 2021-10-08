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

package io.jmix.ui.component.impl;

import io.jmix.core.DevelopmentException;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.builder.EditMode;
import io.jmix.ui.builder.EditorBuilder;
import io.jmix.ui.component.EditorScreenFacet;
import io.jmix.ui.component.Frame;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class EditorScreenFacetImpl<E, S extends Screen & EditorScreen<E>>
        extends AbstractEntityAwareScreenFacet<E, S>
        implements EditorScreenFacet<E, S> {

    protected Supplier<E> entityProvider;

    protected Supplier<DataContext> parentDataContextProvider;
    protected Consumer<E> initializer;
    protected Function<E, E> transformation;

    protected EditMode editMode = EditMode.CREATE;
    protected boolean addFirst = false;

    @Override
    public void setEntityProvider(@Nullable Supplier<E> entityProvider) {
        this.entityProvider = entityProvider;
    }

    @Nullable
    @Override
    public Supplier<E> getEntityProvider() {
        return entityProvider;
    }

    @Override
    public void setInitializer(@Nullable Consumer<E> initializer) {
        this.initializer = initializer;
    }

    @Nullable
    @Override
    public Consumer<E> getInitializer() {
        return initializer;
    }

    @Override
    public void setParentDataContextProvider(@Nullable Supplier<DataContext> parentDataContextProvider) {
        this.parentDataContextProvider = parentDataContextProvider;
    }

    @Nullable
    @Override
    public Supplier<DataContext> getParentDataContextProvider() {
        return parentDataContextProvider;
    }

    @Override
    public void setTransformation(Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Override
    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    @Override
    public void setAddFirst(boolean addFirst) {
        this.addFirst = addFirst;
    }

    @Override
    public EditMode getEditMode() {
        return editMode;
    }

    @Override
    public boolean getAddFirst() {
        return addFirst;
    }

    @Override
    public S create() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Screen facet is not attached to Frame");
        }

        EditorBuilder<E> editorBuilder = createEditorBuilder(owner, getEntityToEdit());

        screen = createScreen(editorBuilder);

        initScreenListeners(screen);
        injectScreenProperties(screen, properties);
        applyScreenConfigurer(screen);

        return screen;
    }

    @Override
    public S show() {
        return (S) create().show();
    }

    protected S createScreen(EditorBuilder<E> builder) {
        return (S) builder
                .withListComponent(listComponent)
                .withField(entityPicker)
                .withContainer(container)
                .withAddFirst(addFirst)
                .withOpenMode(openMode)
                .withOptions(getScreenOptions())
                .withInitializer(initializer)
                .withTransformation(transformation)
                .withParentDataContext(getParentDataContext())
                .build();
    }

    @SuppressWarnings("unchecked")
    protected EditorBuilder<E> createEditorBuilder(Frame owner, @Nullable E entityToEdit) {
        EditorBuilder<E> builder;

        ScreenBuilders screenBuilders = applicationContext.getBean(ScreenBuilders.class);

        if (entityClass != null) {
            builder = screenBuilders.editor(entityClass, owner.getFrameOwner());
        } else if (entityToEdit != null) {
            builder = (EditorBuilder<E>) screenBuilders.editor(entityToEdit.getClass(), owner.getFrameOwner());
        } else if (listComponent != null) {
            builder = screenBuilders.editor(listComponent);
        } else if (entityPicker != null) {
            builder = screenBuilders.editor(entityPicker);
        } else {
            throw new IllegalStateException(
                    "Unable to create EditorScreenFacet. At least one of entityClass," +
                            "listComponent or field must be specified");
        }

        if (editMode == EditMode.CREATE) {
            builder.newEntity(entityProvider != null ? entityToEdit : null);
        } else {
            if (entityToEdit != null) {
                builder.editEntity(entityToEdit);
            } else {
                throw new DevelopmentException("No entity to edit is passed for EditorScreen");
            }
        }

        if (screenClass != null) {
            builder = builder.withScreenClass(screenClass);
        } else {
            builder.withScreenId(screenId);
        }

        return builder;
    }

    @Nullable
    protected E getEntityToEdit() {
        E entity = null;

        if (entityProvider != null) {
            entity = entityProvider.get();
        }

        if (entity == null
                && listComponent != null) {
            entity = listComponent.getSingleSelected();
        }

        if (entity == null
                && entityPicker != null) {
            entity = entityPicker.getValue();
        }

        return entity;
    }

    @Nullable
    protected DataContext getParentDataContext() {
        return parentDataContextProvider != null
                ? parentDataContextProvider.get()
                : null;
    }
}
