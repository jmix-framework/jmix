/*
 * Copyright 2023 Haulmont.
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

package io.jmix.gridexportflowui.exporter;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.data.grid.EntityDataGridItems;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractDataGridExporter<T extends AbstractDataGridExporter<?>> implements DataGridExporter {

    protected MessageTools messageTools;
    protected DatatypeRegistry datatypeRegistry;
    protected Messages messages;
    protected MetadataTools metadataTools;
    protected UiProperties uiProperties;
    protected CoreProperties coreProperties;

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    protected String fileName;

    protected Map<String, Function<ColumnValueContext, Object>> columnValueProviders;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @SuppressWarnings("unchecked")
    public T withFileName(String fileName) {
        setFileName(fileName);
        return (T) this;
    }

    @Override
    public void addColumnValueProvider(String columnId, Function<ColumnValueContext, Object> columnValueProvider) {
        if (columnValueProviders == null) {
            columnValueProviders = new HashMap<>();
        }

        columnValueProviders.put(columnId, columnValueProvider);
    }

    @Override
    public void removeColumnValueProvider(String columnId) {
        if (MapUtils.isNotEmpty(columnValueProviders)) {
            columnValueProviders.remove(columnId);
        }
    }

    @Nullable
    @Override
    public Function<ColumnValueContext, Object> getColumnValueProvider(String columnId) {
        return MapUtils.isNotEmpty(columnValueProviders)
                ? columnValueProviders.get(columnId)
                : null;
    }

    protected String getMetaClassName(MetaClass metaClass) {
        return messageTools.getEntityCaption(metaClass);
    }

    protected String getFileName(Grid<Object> dataGrid) {
        ListDataComponent<?> listDataComponent = (ListDataComponent<?>) dataGrid;

        return fileName == null
                ? getMetaClassName(((EntityDataGridItems<?>) listDataComponent.getItems()).getEntityMetaClass())
                : fileName;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object getColumnValue(Grid<?> dataGrid, Grid.Column<?> column, Object instance) {
        Function<ColumnValueContext, Object> columnValueProvider = MapUtils.isNotEmpty(columnValueProviders)
                ? columnValueProviders.get(column.getKey())
                : null;

        if (columnValueProvider != null) {
            return columnValueProvider.apply(new ColumnValueContext((ListDataComponent<?>) dataGrid, column, instance));
        }

        Object cellValue;

        MetaPropertyPath metaPropertyPath = ((EnhancedDataGrid) dataGrid).getColumnMetaPropertyPath(column);

        if (metaPropertyPath != null) {
            cellValue = EntityValues.getValueEx(instance, metaPropertyPath.getPath());
        } else {
            cellValue = EntityValues.getValueEx(instance, column.getKey());
        }

        return cellValue;
    }

    protected Function<Object, InstanceContainer<Object>> createInstanceContainerProvider(
            Grid<?> dataGrid, Object item) {
        return entity -> {
            throw new UnsupportedOperationException("ExcelExporter doesn't provider instance container");
        };
    }

    protected String formatValue(@Nullable Object cellValue, MetaPropertyPath metaPropertyPath) {
        if (cellValue == null) {
            if (metaPropertyPath.getRange().isDatatype()) {
                Class<?> javaClass = metaPropertyPath.getRange().asDatatype().getJavaClass();
                if (Boolean.class.equals(javaClass)) {
                    cellValue = false;
                }

            } else {
                return "";
            }
        }

        if (cellValue instanceof Number) {
            Number n = (Number) cellValue;
            Datatype<?> datatype = null;

            Range range = metaPropertyPath.getMetaProperty().getRange();
            if (range.isDatatype()) {
                datatype = range.asDatatype();
            }

            datatype = datatype == null ? datatypeRegistry.get(n.getClass()) : datatype;
            return datatype.format(n);
        } else if (cellValue instanceof Date) {
            Class<?> javaClass = null;
            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

            if (metaProperty.getRange().isDatatype()) {
                javaClass = metaProperty.getRange().asDatatype().getJavaClass();
            }
            Date date = ((Date) cellValue);

            if (Objects.equals(java.sql.Time.class, javaClass)) {
                return datatypeRegistry.get(java.sql.Time.class).format(date);
            } else if (Objects.equals(java.sql.Date.class, javaClass)) {
                return datatypeRegistry.get(java.sql.Date.class).format(date);
            } else {
                return datatypeRegistry.get(Date.class).format(date);
            }
        } else {
            return formatValue(cellValue);
        }
    }

    protected String formatValue(@Nullable Object cellValue) {
        if (cellValue == null) {
            return "";
        }

        if (cellValue instanceof Number) {
            Number n = (Number) cellValue;
            Datatype<?> datatype = datatypeRegistry.get(n.getClass());
            return datatype.format(n);
        } else if (cellValue instanceof java.sql.Time) {
            return datatypeRegistry.get(java.sql.Time.class).format(cellValue);
        } else if (cellValue instanceof java.sql.Date) {
            return datatypeRegistry.get(java.sql.Date.class).format(cellValue);
        } else if (cellValue instanceof Date) {
            return datatypeRegistry.get(Date.class).format(cellValue);
        } else if (cellValue instanceof Boolean) {
            return String.valueOf(cellValue);
        } else if (cellValue instanceof Enum) {
            return messages.getMessage((Enum<?>) cellValue);
        } else if (cellValue instanceof Entity) {
            return metadataTools.getInstanceName(cellValue);
        } else {
            return cellValue.toString();
        }
    }
}
