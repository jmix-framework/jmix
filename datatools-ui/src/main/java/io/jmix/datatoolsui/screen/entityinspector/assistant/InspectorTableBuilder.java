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

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TablePagination;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.model.CollectionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.isEmbedded;
import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.isMany;

@SuppressWarnings({"rawtypes","unchecked"})
@Component(InspectorTableBuilder.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorTableBuilder {

    public static final String NAME = "datatools_EntityInspectorTableBuilder";

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponents uiComponents;

    private final MetaClass metaClass;
    private final CollectionContainer collectionContainer;

    private Integer maxTextLength = 0;
    private Boolean withSystem = false;
    private Boolean withMultiselect = true;
    private Consumer<Table> buttonsPanelInitializer;

    public static InspectorTableBuilder from(BeanLocator beanLocator, CollectionContainer collectionContainer) {
        return beanLocator.getPrototype(InspectorTableBuilder.class, collectionContainer);
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

    public Table build() {
        Table table = uiComponents.create(Table.NAME);

        //collect properties in order to add non-system columns first
        List<Table.Column> nonSystemPropertyColumns = new ArrayList<>(10);
        List<Table.Column> systemPropertyColumns = new ArrayList<>(10);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            //don't show embedded, transient & multiple referred entities
            if (isEmbedded(metaProperty) || !metadataTools.isPersistent(metaProperty)) {
                continue;
            }

            if (isMany(metaProperty)) {
                continue;
            }

            Table.Column column = new Table.Column(metaClass.getPropertyPath(metaProperty.getName()));

            if (metaProperty.getJavaType().equals(String.class) && maxTextLength > 0) {
                column.setMaxTextLength(maxTextLength);
            }

            if (!metadataTools.isSystem(metaProperty)) {
                column.setCaption(getPropertyCaption(metaClass, metaProperty));
                nonSystemPropertyColumns.add(column);
            } else if (withSystem) {
                column.setCaption(getPropertyCaption(metaClass, metaProperty));
                systemPropertyColumns.add(column);
            }
        }
        for (Table.Column column : nonSystemPropertyColumns) {
            table.addColumn(column);
        }

        for (Table.Column column : systemPropertyColumns) {
            table.addColumn(column);
        }
        table.setSizeFull();

        table.setItems(new ContainerTableItems(collectionContainer));

        if (buttonsPanelInitializer!=null) {
            table.setButtonsPanel(uiComponents.create(ButtonsPanel.class));
            buttonsPanelInitializer.accept(table);
        }

        TablePagination tablePagination = uiComponents.create(TablePagination.NAME);
        table.setPagination(tablePagination);
        tablePagination.setTablePaginationTarget(table);

        if (table.getAction(EditAction.ID) != null) {
            table.setEnterPressAction(table.getAction(EditAction.ID));
            table.setItemClickAction(table.getAction(EditAction.ID));
        }
        table.setMultiSelect(withMultiselect);

        table.addStyleName("table-boolean-text");
        return table;
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        return messageTools.getPropertyCaption(metaClass, metaProperty.getName());
    }
}
