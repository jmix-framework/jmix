/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.components.table;

import com.google.common.base.Strings;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.TableFieldFactory;
import io.jmix.core.MetadataTools;
import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.Security;
import io.jmix.ui.components.CheckBox;
import io.jmix.ui.components.Component.BelongToFrame;
import io.jmix.ui.components.Field;
import io.jmix.ui.components.Table;
import io.jmix.ui.components.data.HasValueSource;
import io.jmix.ui.components.data.Options;
import io.jmix.ui.components.data.meta.EntityValueSource;
import io.jmix.ui.components.data.options.ContainerOptions;
import io.jmix.ui.components.data.value.ContainerValueSource;
import io.jmix.ui.components.factories.AbstractFieldFactory;
import io.jmix.ui.components.impl.WebAbstractTable;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.UiControllerUtils;

import javax.annotation.Nullable;
import java.util.Map;

public class WebTableFieldFactory<E extends Entity> extends AbstractFieldFactory implements TableFieldFactory {
    protected WebAbstractTable<?, E> webTable;
    protected Security security;
    protected MetadataTools metadataTools;

    public WebTableFieldFactory(WebAbstractTable<?, E> webTable, Security security, MetadataTools metadataTools) {
        this.webTable = webTable;
        this.security = security;
        this.metadataTools = metadataTools;
    }

    @SuppressWarnings("unchecked")
    @Override
    public com.vaadin.v7.ui.Field<?> createField(com.vaadin.v7.data.Container container,
                                                 Object itemId, Object propertyId, Component uiContext) {

        String fieldPropertyId = String.valueOf(propertyId);

        Table.Column columnConf = webTable.getColumnsInternal().get(propertyId);

        TableDataContainer tableDataContainer = (TableDataContainer) container;
        Entity entity  = (Entity) tableDataContainer.getInternalItem(itemId);
        InstanceContainer instanceContainer = webTable.getInstanceContainer((E) entity);

        io.jmix.ui.components.Component columnComponent =
                createField(new ContainerValueSource(instanceContainer, fieldPropertyId),
                        fieldPropertyId, columnConf.getXmlDescriptor());

        if (columnComponent instanceof Field) {
            Field jmixField = (Field) columnComponent;

            Map<Table.Column, String> requiredColumns = webTable.getRequiredColumnsInternal();
            if (requiredColumns != null && requiredColumns.containsKey(columnConf)) {
                jmixField.setRequired(true);
                jmixField.setRequiredMessage(requiredColumns.get(columnConf));
            }
        }

        if (!(columnComponent instanceof CheckBox)) { // todo get rid of concrete CheckBox class !
            columnComponent.setWidthFull();
        }

        if (columnComponent instanceof BelongToFrame) {
            BelongToFrame belongToFrame = (BelongToFrame) columnComponent;
            if (belongToFrame.getFrame() == null) {
                belongToFrame.setFrame(webTable.getFrame());
            }
        }

        applyPermissions(columnComponent);

        columnComponent.setParent(webTable);

        Component componentImpl = getComponentImplementation(columnComponent);
        if (componentImpl instanceof com.vaadin.v7.ui.Field) {
            return (com.vaadin.v7.ui.Field<?>) componentImpl;
        }

        return new EditableColumnFieldWrapper(componentImpl, columnComponent);
    }

    protected Component getComponentImplementation(io.jmix.ui.components.Component columnComponent) {
        com.vaadin.ui.Component composition = columnComponent.unwrapComposition(com.vaadin.ui.Component.class);
        Component componentImpl = composition;
        if (composition instanceof com.vaadin.v7.ui.Field
                && ((com.vaadin.v7.ui.Field) composition).isRequired()) {
            VerticalLayout layout = new VerticalLayout(); // vaadin8 replace with CssLayout
            layout.setMargin(false);
            layout.setSpacing(false);
            layout.addComponent(composition);

            if (composition.getWidth() < 0) {
                layout.setWidthUndefined();
            }

            componentImpl = layout;
        }
        return componentImpl;
    }

    protected void applyPermissions(io.jmix.ui.components.Component columnComponent) {
        if (columnComponent instanceof HasValueSource
                && columnComponent instanceof io.jmix.ui.components.Component.Editable) {
            HasValueSource component = (HasValueSource) columnComponent;
            MetaPropertyPath propertyPath = ((EntityValueSource) component.getValueSource()).getMetaPropertyPath();

            if (propertyPath != null) {
                io.jmix.ui.components.Component.Editable editable =
                        (io.jmix.ui.components.Component.Editable) component;

                editable.setEditable(editable.isEditable()
                        && security.isEntityAttrUpdatePermitted(propertyPath));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    protected Options getOptions(EntityValueSource valueSource, String property) {
        MetaClass metaClass = valueSource.getEntityMetaClass();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, property);
        Table.Column columnConf = webTable.getColumnsInternal().get(metaPropertyPath);

        CollectionContainer collectionContainer = findOptionsContainer(columnConf);
        if (collectionContainer != null) {
            return new ContainerOptions(collectionContainer);
        }

        /*
        TODO: legacy-ui
        CollectionDatasource ds = findOptionsDatasource(columnConf, property);
        if (ds != null) {
            return new DatasourceOptions(ds);
        }*/

        return null;
    }

    @Nullable
    protected CollectionContainer findOptionsContainer(Table.Column columnConf) {
        String optDcName = columnConf.getXmlDescriptor() != null ?
                columnConf.getXmlDescriptor().attributeValue("optionsContainer") : null;

        if (Strings.isNullOrEmpty(optDcName)) {
            return null;
        } else {
            ScreenData screenData = UiControllerUtils.getScreenData(webTable.getFrame().getFrameOwner());
            InstanceContainer container = screenData.getContainer(optDcName);

            if (container instanceof CollectionContainer) {
                return (CollectionContainer) container;
            }

            throw new IllegalStateException(
                    String.format("'%s' is not an instance of CollectionContainer", optDcName));
        }
    }

    /*
    TODO: legacy-ui
    @Nullable
    protected CollectionDatasource findOptionsDatasource(Table.Column columnConf, String propertyId) {
        String optDsName = columnConf.getXmlDescriptor() != null ?
                columnConf.getXmlDescriptor().attributeValue("optionsDatasource") : "";

        if (Strings.isNullOrEmpty(optDsName)) {
            return null;
        } else {
            if (webTable.getDatasource() == null) {
                throw new IllegalStateException("Table datasource is null");
            }

            DsContext dsContext = webTable.getDatasource().getDsContext();
            CollectionDatasource ds = (CollectionDatasource) dsContext.get(optDsName);
            if (ds == null) {
                throw new IllegalStateException(
                        String.format("Options datasource for table column '%s' not found: %s", propertyId, optDsName));
            }

            return ds;
        }
    }*/
}
