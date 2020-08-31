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

package io.jmix.uiexport.exporter;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.utils.InstanceUtils;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.meta.EntityDataGridItems;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.uiexport.action.ExportAction;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base table exporter class.
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractTableExporter<T extends AbstractTableExporter> implements TableExporter {

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    protected String fileName;

    protected Map<String, Formatter> formatters;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public T withFileName(String fileName) {
        setFileName(fileName);
        return (T) this;
    }

    public Map<String, Formatter> getFormatters() {
        return formatters;
    }

    public void setFormatters(Map<String, Formatter> formatters) {
        this.formatters = formatters;
    }

    public T withFormatters(Map<String, Formatter> formatters) {
        setFormatters(formatters);
        return (T) this;
    }

    protected String getMetaClassName(MetaClass metaClass) {
        return messageTools.getEntityCaption(metaClass);
    }

    protected String getFileName(Table<JmixEntity> table) {
        String fileName = this.fileName;
        if (fileName == null) {
            return getMetaClassName(((ContainerTableItems) table.getItems()).getEntityMetaClass());
        }
        return fileName;
    }

    protected String getFileName(DataGrid<JmixEntity> dataGrid) {
        String fileName = this.fileName;
        if (fileName == null) {
            return getMetaClassName(((EntityDataGridItems) dataGrid.getItems()).getEntityMetaClass());
        }
        return fileName;
    }

    protected Object getColumnValue(Table table, Table.Column column, JmixEntity instance) {
        Object cellValue = null;

        if (column.getId() instanceof MetaPropertyPath) {
            MetaPropertyPath propertyPath = (MetaPropertyPath) column.getId();
            Table.Printable printable = table.getPrintable(column);
            if (printable != null) {
                cellValue = printable.getValue(instance);
            } else {
                Element xmlDescriptor = column.getXmlDescriptor();
                if (xmlDescriptor != null && StringUtils.isNotEmpty(xmlDescriptor.attributeValue("captionProperty"))) {
                    String captionProperty = xmlDescriptor.attributeValue("captionProperty");
                    cellValue = EntityValues.getValueEx(instance, captionProperty);
                } else {
                    cellValue = EntityValues.getValueEx(instance, propertyPath.getPath());
                }
                if (column.getFormatter() != null)
                    cellValue = column.getFormatter().apply(cellValue);
            }
        } else {
            Table.Printable printable = table.getPrintable(column);
            if (printable != null) {
                cellValue = printable.getValue(instance);
            } else if (column.getValueProvider() != null) {
                cellValue = column.getValueProvider().apply(instance);
            }
        }
        return cellValue;
    }

    protected Object getColumnValue(DataGrid dataGrid, DataGrid.Column column, JmixEntity instance) {

        Object cellValue = null;

        MetaPropertyPath propertyPath = null;
        Function<DataGrid.ColumnGeneratorEvent<JmixEntity>, ?> generator;
        if (column.getPropertyPath() != null) {
            propertyPath = column.getPropertyPath();

            cellValue = InstanceUtils.getValueEx(instance, propertyPath.getPath());

            if (column.getFormatter() != null) {
                cellValue = column.getFormatter().apply(cellValue);
            }
        } else if ((generator = dataGrid.getColumnGenerator(column.getId())) != null) {
            DataGrid.ColumnGeneratorEvent event = new DataGrid.ColumnGeneratorEvent(dataGrid, instance,
                    column.getId(), createInstanceContainerProvider(dataGrid, instance));
            cellValue = generator.apply(event);
        } else {
            cellValue = EntityValues.getValueEx(instance, column.getId());
        }

        if (column.getFormatter() != null) {
            cellValue = column.getFormatter().apply(cellValue);
        }

        return cellValue;
    }

    protected Function<JmixEntity, InstanceContainer<JmixEntity>> createInstanceContainerProvider(DataGrid dataGrid, JmixEntity item) {
        return entity -> {
            throw new UnsupportedOperationException("ExcelExporter doesn't provide instance container");
        };
    }

    protected String formatValue(@Nullable Object cellValue, MetaPropertyPath metaPropertyPath) {
        if (cellValue == null) {
            if (metaPropertyPath.getRange().isDatatype()) {
                Class javaClass = metaPropertyPath.getRange().asDatatype().getJavaClass();
                if (Boolean.class.equals(javaClass)) {
                    cellValue = false;
                }
            } else {
                return null;
            }
        }

        if (cellValue instanceof Number) {
            Number n = (Number) cellValue;
            Datatype datatype = null;
            Range range = metaPropertyPath.getMetaProperty().getRange();
            if (range.isDatatype()) {
                datatype = range.asDatatype();
            }

            datatype = datatype == null ? datatypeRegistry.get(n.getClass()) : datatype;
            return datatype.format(n);
        } else if (cellValue instanceof Date) {
            Class javaClass = null;
            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
            if (metaProperty.getRange().isDatatype()) {
                javaClass = metaProperty.getRange().asDatatype().getJavaClass();
            }
            Date date = (Date) cellValue;

            if (Objects.equals(java.sql.Time.class, javaClass)) {
                return datatypeRegistry.get(java.sql.Time.class).format(date);
            } else if (Objects.equals(java.sql.Date.class, javaClass)) {
                return datatypeRegistry.get(java.sql.Date.class).format(date);
            } else {
                return datatypeRegistry.get(Date.class).format(date);
            }
        } else if (cellValue instanceof Boolean) {
            return String.valueOf(cellValue);
        } else if (cellValue instanceof Enum) {
            return messages.getMessage((Enum) cellValue);
        } else if (cellValue instanceof JmixEntity) {
            JmixEntity entityVal = (JmixEntity) cellValue;
            return metadataTools.getInstanceName(entityVal);
        } else {
            return cellValue == null ? "" : cellValue.toString();
        }

    }
}
