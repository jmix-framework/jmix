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

package io.jmix.datatoolsui.screen.entityinspector.assistant;

import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.datatoolsui.DatatoolsUiProperties;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.SimplePagination;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.Convert;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.isEmbedded;
import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.isMany;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component("datatl_EntityInspectorTableBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorTableBuilder {

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DatatoolsUiProperties datatoolsUiProperties;

    private final MetaClass metaClass;
    private final CollectionContainer collectionContainer;

    private Integer maxTextLength = 0;
    private Boolean withSystem = false;
    private Boolean withMultiselect = true;
    private Consumer<Table> buttonsPanelInitializer;

    protected Consumer<SimplePagination> paginationInitializer;

    public static InspectorTableBuilder from(ApplicationContext applicationContext, CollectionContainer collectionContainer) {
        return applicationContext.getBean(InspectorTableBuilder.class, collectionContainer);
    }

    protected InspectorTableBuilder(CollectionContainer collectionContainer) {
        this.collectionContainer = collectionContainer;
        this.metaClass = collectionContainer.getEntityMetaClass();
    }

    public InspectorTableBuilder withMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
        return this;
    }

    public InspectorTableBuilder withSystem(boolean withSystem) {
        this.withSystem = withSystem;
        return this;
    }

    public InspectorTableBuilder withButtons(Consumer<Table> buttonsPanelInitializer) {
        this.buttonsPanelInitializer = buttonsPanelInitializer;
        return this;
    }

    public InspectorTableBuilder withSimplePagination(Consumer<SimplePagination> paginationInitializer) {
        this.paginationInitializer = paginationInitializer;
        return this;
    }

    public Table build() {
        Table table = uiComponents.create(Table.NAME);

        //collect properties in order to add non-system columns first
        List<MetaProperty> nonSystemProperties = new ArrayList<>(10);
        List<MetaProperty> systemProperties = new ArrayList<>(10);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            //don't show embedded, transient & multiple referred entities
            if (isEmbedded(metaProperty) || !metadataTools.isJpa(metaProperty)) {
                continue;
            }

            if (isMany(metaProperty)) {
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
            addMetaPropertyToTable(table, metaProperty);
        }

        for (MetaProperty metaProperty : systemProperties) {
            addMetaPropertyToTable(table, metaProperty);
        }

        table.setSizeFull();

        if (buttonsPanelInitializer != null) {
            table.setButtonsPanel(uiComponents.create(ButtonsPanel.class));
            buttonsPanelInitializer.accept(table);
        }

        SimplePagination simplePagination = uiComponents.create(SimplePagination.NAME);
        table.setPagination(simplePagination);
        initSimplePagination(simplePagination);

        table.setItems(new ContainerTableItems(collectionContainer));

        if (table.getAction(EditAction.ID) != null) {
            table.setEnterPressAction(table.getAction(EditAction.ID));
            table.setItemClickAction(table.getAction(EditAction.ID));
        }
        table.setMultiSelect(withMultiselect);

        table.addStyleName("table-boolean-text");
        return table;
    }

    protected void addMetaPropertyToTable(Table table, MetaProperty metaProperty) {
        MetaPropertyPath metaPropertyPath = metaClass.getPropertyPath(metaProperty.getName());
        if (metaPropertyPath == null) {
            return;
        }

        Table.Column column = table.addColumn(metaPropertyPath);

        if (metaProperty.getJavaType().equals(String.class) && maxTextLength > 0) {
            column.setMaxTextLength(maxTextLength);
        }

        column.setCaption(getPropertyCaption(metaClass, metaProperty));
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        return messageTools.getPropertyCaption(metaClass, metaProperty.getName());
    }

    protected void initSimplePagination(SimplePagination simplePagination) {
        if (paginationInitializer != null) {
            paginationInitializer.accept(simplePagination);
        } else {
            simplePagination.setItemsPerPageVisible(
                    datatoolsUiProperties.getEntityInspectorBrowse().isItemsPerPageVisible());
            simplePagination.setItemsPerPageUnlimitedOptionVisible(
                    datatoolsUiProperties.getEntityInspectorBrowse().isItemsPerPageUnlimitedOptionVisible());
            List<Integer> itemsPerPageOptions =
                    datatoolsUiProperties.getEntityInspectorBrowse().getItemsPerPageOptions();
            if (CollectionUtils.isNotEmpty(itemsPerPageOptions)) {
                simplePagination.setItemsPerPageOptions(itemsPerPageOptions);
            }
        }
    }
}
