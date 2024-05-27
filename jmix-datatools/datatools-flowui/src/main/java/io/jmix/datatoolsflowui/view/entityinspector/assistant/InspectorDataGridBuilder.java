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

package io.jmix.datatoolsflowui.view.entityinspector.assistant;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.datatoolsflowui.DatatoolsUiProperties;
import io.jmix.datatoolsflowui.view.entityinspector.EntityFormLayoutUtils;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorListView;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.model.CollectionContainer;
import jakarta.persistence.Convert;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("datatl_EntityInspectorDataGridBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorDataGridBuilder {

    protected MetadataTools metadataTools;
    protected MessageTools messageTools;
    protected UiComponents uiComponents;
    protected DatatoolsUiProperties datatoolsUiProperties;
    protected Messages messages;

    private final MetaClass metaClass;
    private final CollectionContainer<?> collectionContainer;

    private Boolean withSystem = false;

    protected InspectorDataGridBuilder(CollectionContainer<?> collectionContainer) {
        this.collectionContainer = collectionContainer;
        this.metaClass = collectionContainer.getEntityMetaClass();
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setDatatoolsUiProperties(DatatoolsUiProperties datatoolsUiProperties) {
        this.datatoolsUiProperties = datatoolsUiProperties;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    public static InspectorDataGridBuilder from(ApplicationContext applicationContext,
                                                CollectionContainer<?> collectionContainer) {
        return applicationContext.getBean(InspectorDataGridBuilder.class, collectionContainer);
    }

    public InspectorDataGridBuilder withSystem(boolean withSystem) {
        this.withSystem = withSystem;
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DataGrid build() {
        DataGrid<?> dataGrid = uiComponents.create(DataGrid.class);

        //collect properties in order to add non-system columns first
        List<MetaProperty> nonSystemProperties = new ArrayList<>(10);
        List<MetaProperty> systemProperties = new ArrayList<>(10);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            //don't show embedded, transient & multiple referred entities
            if (EntityFormLayoutUtils.isEmbedded(metaProperty) || !metadataTools.isJpa(metaProperty)) {
                continue;
            }

            if (EntityFormLayoutUtils.isMany(metaProperty)) {
                continue;
            }

            if (metadataTools.isAnnotationPresent(metaClass.getJavaClass(), metaProperty.getName(), Convert.class)) {
                continue;
            }

            if (!metadataTools.isSystem(metaProperty)) {
                nonSystemProperties.add(metaProperty);
            } else if (withSystem) {
                systemProperties.add(metaProperty);
            }
        }
        for (MetaProperty metaProperty : nonSystemProperties) {
            addMetaPropertyToDataGrid(dataGrid, metaProperty);
        }

        for (MetaProperty metaProperty : systemProperties) {
            addMetaPropertyToDataGrid(dataGrid, metaProperty);
        }

        dataGrid.setSizeFull();
        dataGrid.setAllRowsVisible(true);
        dataGrid.setMinHeight("20em");
        dataGrid.setColumnReorderingAllowed(true);

        dataGrid.setItems(new ContainerDataGridItems(collectionContainer));

        dataGrid.enableMultiSelect();
        return dataGrid;
    }

    protected void addMetaPropertyToDataGrid(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        MetaPropertyPath metaPropertyPath = metaClass.getPropertyPath(metaProperty.getName());
        if (metaPropertyPath == null) {
            return;
        }

        DataGrid.Column<?> column = dataGrid.addColumn(metaPropertyPath);
        if (metaProperty.getRange().isDatatype()
                && byte[].class.equals(metaProperty.getRange().asDatatype().getJavaClass())) {
            //noinspection unchecked
            column.setRenderer(createByteArrayRenderer(metaPropertyPath));
        }

        column.setHeader(getPropertyHeader(metaClass, metaProperty));

        column.setResizable(true);
        column.setAutoWidth(true);
    }

    protected com.vaadin.flow.component.Component getPropertyHeader(MetaClass metaClass, MetaProperty metaProperty) {
        String propertyCaption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());

        Span header = new Span(propertyCaption);
        Tooltip.forComponent(header).setText(propertyCaption);
        return header;
    }

    @SuppressWarnings("rawtypes")
    protected Renderer createByteArrayRenderer(MetaPropertyPath metaPropertyPath) {
        return new TextRenderer<>(entity -> byteArrayLabelGenerator(entity, metaPropertyPath));
    }

    protected String byteArrayLabelGenerator(Object entity, MetaPropertyPath metaPropertyPath) {
        byte[] value = EntityValues.getValueEx(entity, metaPropertyPath);

        if (value != null) {
            return messages.formatMessage(EntityInspectorListView.class, "entitiesDataGrid.byteArrayColumnText",
                    FileUtils.byteCountToDisplaySize(value.length));
        }

        return Strings.EMPTY;
    }
}
