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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.DataGrid;
import com.haulmont.cuba.gui.components.TreeDataGrid;
import com.haulmont.cuba.settings.CubaTreeDataGridSettingsBinder;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Grid;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Entity;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.formatter.CollectionFormatter;
import io.jmix.ui.component.impl.WebAbstractDataGrid;
import io.jmix.ui.component.valueprovider.FormatterBasedValueProvider;
import io.jmix.ui.component.valueprovider.StringPresentationValueProvider;
import io.jmix.ui.component.valueprovider.YesNoIconPresentationValueProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

@Deprecated
public class WebTreeDataGrid<E extends Entity> extends io.jmix.ui.component.impl.WebTreeDataGrid<E>
        implements TreeDataGrid<E> {

    @Override
    protected ComponentSettingsBinder getSettingsBinder() {
        return beanLocator.get(CubaTreeDataGridSettingsBinder.NAME);
    }

    @Override
    protected ColumnImpl<E> createColumn(String id, @Nullable MetaPropertyPath propertyPath, WebAbstractDataGrid<?, E> owner) {
        return new WebDataGrid.ColumnImpl<>(id, propertyPath, owner);
    }

    @Override
    public io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, ColumnGenerator<E, ?> generator) {
        return addGeneratedColumn(columnId, generator, columnsOrder.size());
    }

    @Override
    public io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, ColumnGenerator<E, ?> generator, int index) {
        Preconditions.checkNotNullArgument(columnId, "columnId is null");
        Preconditions.checkNotNullArgument(generator, "generator is null for column id '%s'", columnId);

        Function<ColumnGeneratorEvent<E>, ?> generatorFunction = (Function<ColumnGeneratorEvent<E>, Object>) columnGeneratorEvent ->
                generator.getValue(columnGeneratorEvent);

        DataGrid.Column<E> existingColumn = (DataGrid.Column<E>) getColumn(columnId);
        if (existingColumn != null) {
            index = columnsOrder.indexOf(existingColumn);
            removeColumn(existingColumn);
        }

        Grid.Column<E, Object> generatedColumn =
                component.addColumn(createGeneratedColumnValueProvider(columnId, generatorFunction));

        // Pass propertyPath from the existing column to support sorting
        WebDataGrid.ColumnImpl<E> column = new WebDataGrid.ColumnImpl<>(columnId,
                existingColumn != null ? existingColumn.getPropertyPath() : null,
                generator.getType(), this);
        if (existingColumn != null) {
            copyColumnProperties(column, existingColumn);
        } else {
            column.setCaption(columnId);
        }
        column.setGenerated(true);

        columns.put(column.getId(), column);
        columnsOrder.add(index, column);
        columnGenerators.put(column.getId(), generatorFunction);

        setupGridColumnProperties(generatedColumn, column);

        component.setColumnOrder(getColumnOrder());

        return column;
    }

    @Override
    public DataGrid.Column<E> addGeneratedColumn(String columnId, GenericColumnGenerator<E, ?> generator) {
        DataGrid.Column<E> column = (DataGrid.Column<E>) getColumn(columnId);
        if (column == null) {
            throw new DevelopmentException("Unable to set ColumnGenerator for non-existing column: " + columnId);
        }

        Class<? extends Renderer> rendererType = null;

        Renderer renderer = column.getRenderer();
        if (renderer != null) {
            Class<?>[] rendererInterfaces = renderer.getClass().getInterfaces();

            rendererType = (Class<? extends Renderer>) Arrays.stream(rendererInterfaces)
                    .filter(Renderer.class::isAssignableFrom)
                    .findFirst()
                    .orElseThrow(() ->
                            new DevelopmentException(
                                    "Renderer should be specified explicitly for generated column: " + columnId));
        }


        io.jmix.ui.component.DataGrid.Column<E> generatedColumn = addGeneratedColumn(columnId, new ColumnGenerator<E, Object>() {
            @Override
            public Object getValue(ColumnGeneratorEvent<E> event) {
                return generator.getValue(event);
            }

            @Override
            public Class<Object> getType() {
                return column.getGeneratedType();
            }
        });

        if (renderer != null) {
            generatedColumn.setRenderer(createRenderer(rendererType));
        }

        return column;
    }

    @Override
    public <T extends Renderer> T createRenderer(Class<T> type) {
        return beanLocator.getPrototype(type);
    }

    @Override
    protected ValueProvider getDefaultPresentationValueProvider(io.jmix.ui.component.DataGrid.Column<E> column) {
        MetaProperty metaProperty = column.getPropertyPath() != null
                ? column.getPropertyPath().getMetaProperty()
                : null;

        if (column.getFormatter() != null) {
            //noinspection unchecked
            return new FormatterBasedValueProvider<>(column.getFormatter());
        } else if (metaProperty != null) {
            if (Collection.class.isAssignableFrom(metaProperty.getJavaType())) {
                return new FormatterBasedValueProvider<>(new CollectionFormatter(metadataTools));
            }
            if (column instanceof DataGrid.Column
                    && ((DataGrid.Column<E>) column).getType() == Boolean.class) {
                return new YesNoIconPresentationValueProvider();
            }
        }

        return new StringPresentationValueProvider(metaProperty, metadataTools);
    }

    @Override
    protected com.vaadin.ui.renderers.Renderer getDefaultRenderer(io.jmix.ui.component.DataGrid.Column<E> column) {
        MetaProperty metaProperty = column.getPropertyPath() != null
                ? column.getPropertyPath().getMetaProperty()
                : null;

        return column instanceof DataGrid.Column
                && ((DataGrid.Column<E>) column).getType() == Boolean.class
                && metaProperty != null
                ? new com.vaadin.ui.renderers.HtmlRenderer()
                : new com.vaadin.ui.renderers.TextRenderer();
    }
}
