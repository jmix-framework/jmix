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

package io.jmix.dynattrflowui.view.location;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattrflowui.DynAttrUiProperties;
import io.jmix.dynattrflowui.utils.GridHelper;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.Sorter;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_AttributeLocationViewFragment")
@ViewDescriptor("attribute-location-view-fragment.xml")
public class AttributeLocationViewFragment extends StandardView {

    private static final List<Integer> COL_POSITIONS = IntStream.range(1, 5).boxed().toList();

    @Autowired
    protected DynAttrUiProperties properties;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected GridHelper gridHelper;

    @ViewComponent
    protected JmixComboBox<Integer> columnsCountLookupField;
    @ViewComponent
    protected DataGrid<CategoryAttribute> sourceDataGrid;
    @ViewComponent
    protected CollectionContainer<CategoryAttribute> sourceDc;

    @Subscribe
    protected void onInit(InitEvent event) {
        columnsCountLookupField.setItems(COL_POSITIONS);
        columnsCountLookupField.addValueChangeListener(e -> sourceDataGrid.getDataProvider().refreshAll());
        Objects.requireNonNull(sourceDataGrid.getColumnByKey("columnPosition"))
                .setRenderer(createCategoryAttributeLocationRenderer());
        Objects.requireNonNull(sourceDataGrid.getColumnByKey("rowPosition"))
                .setRenderer(createCategoryAttributeRowLocationRenderer());
    }

    public void setAttributes(CollectionContainer<CategoryAttribute> attributes) {
        this.sourceDc.setItems(attributes.getItems());
        columnsCountLookupField.setValue(attributes.getItems().stream()
                .mapToInt(e -> e.getConfiguration().getColumnNumber() == null ? 1 : e.getConfiguration().getColumnNumber())
                .max()
                .orElse(1));

        sourceDataGrid.getDataProvider().refreshAll();
    }

    protected ComponentRenderer<JmixComboBox<Integer>, CategoryAttribute> createCategoryAttributeRowLocationRenderer() {
        return new ComponentRenderer<>(this::createCategoryAttributeRowLocationComponent, this::gradeComponentRowUpdater);
    }

    @SuppressWarnings("unchecked")
    protected JmixComboBox<Integer> createCategoryAttributeRowLocationComponent() {
        JmixComboBox<Integer> comboBox = uiComponents.create(JmixComboBox.class);
        int maxValue = sourceDc.getMutableItems().size();
        comboBox.setItems(IntStream.range(1, maxValue + 1).boxed().toList());
        comboBox.setWidth("60%");
        return comboBox;
    }

    protected void gradeComponentRowUpdater(JmixComboBox<Integer> jmixComboBox, CategoryAttribute categoryAttribute) {
        if (categoryAttribute.getConfiguration().getRowNumber() != null) {
            jmixComboBox.setValue(categoryAttribute.getConfiguration().getRowNumber());
        }
        jmixComboBox.addValueChangeListener(e -> {
            CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();
            if (configuration != null) {
                configuration.setRowNumber(e.getValue());
                categoryAttribute.setConfiguration((CategoryAttributeConfiguration) configuration.clone());
                getViewData().getDataContext().setModified(categoryAttribute, true);
                getViewData().getDataContext().merge(categoryAttribute);
            }
        });
    }

    protected ComponentRenderer<JmixComboBox<Integer>, CategoryAttribute> createCategoryAttributeLocationRenderer() {
        return new ComponentRenderer<>(this::createCategoryAttributeLocationComponent, this::gradeComponentUpdater);
    }

    @SuppressWarnings("unchecked")
    protected JmixComboBox<Integer> createCategoryAttributeLocationComponent() {
        JmixComboBox<Integer> comboBox = uiComponents.create(JmixComboBox.class);
        int maxValue = columnsCountLookupField.getValue() != null ? columnsCountLookupField.getValue() : 1;
        comboBox.setItems(IntStream.range(1, maxValue + 1).boxed().toList());
        comboBox.setWidth("60%");
        return comboBox;
    }

    protected void gradeComponentUpdater(JmixComboBox<Integer> jmixComboBox, CategoryAttribute categoryAttribute) {
        if (categoryAttribute.getConfiguration().getColumnNumber() != null) {
            jmixComboBox.setValue(categoryAttribute.getConfiguration().getColumnNumber());
        }
        jmixComboBox.addValueChangeListener(e -> {
            CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();
            if (configuration != null) {
                configuration.setColumnNumber(e.getValue());
                categoryAttribute.setConfiguration((CategoryAttributeConfiguration) configuration.clone());
                getViewData().getDataContext().setModified(categoryAttribute, true);
                getViewData().getDataContext().merge(categoryAttribute);
            }
        });
    }

    public void setDataContext(DataContext dataContext) {
        getViewData().setDataContext(dataContext);
    }
}
