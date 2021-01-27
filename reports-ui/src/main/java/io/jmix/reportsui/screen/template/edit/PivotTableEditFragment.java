/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reportsui.screen.template.edit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.pivottable.*;
import io.jmix.reportsui.screen.template.edit.generator.RandomPivotTableDataGenerator;
import io.jmix.ui.Actions;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UiController("report_PivotTableEdit.fragment")
@UiDescriptor("pivottable-frame.xml")
public class PivotTableEditFragment extends DescriptionEditFragment {

    public static final Set<RendererType> C3_RENDERER_TYPES = Sets.newHashSet(
            RendererType.LINE_CHART, RendererType.BAR_CHART, RendererType.STACKED_BAR_CHART,
            RendererType.AREA_CHART, RendererType.SCATTER_CHART);

    public static final Set<RendererType> HEATMAP_RENDERER_TYPES = Sets.newHashSet(
            RendererType.HEATMAP, RendererType.COL_HEATMAP, RendererType.ROW_HEATMAP);

    @Autowired
    protected InstanceContainer<PivotTableDescription> pivotTableDc;

    @Autowired
    protected CollectionContainer<PivotTableAggregation> aggregationsDc;

    @Autowired
    protected CollectionContainer<PivotTableProperty> propertyDc;

    @Autowired
    protected GroupTable<PivotTableProperty> propertyTable;

    @Autowired
    protected Table<PivotTableAggregation> aggregationsTable;

    @Autowired
    protected ComboBox<RendererType> defaultRendererField;

    @Autowired
    protected ComboBox<PivotTableAggregation> defaultAggregationField;

    @Autowired
    protected ComboBox<String> bandNameField;

    @Autowired
    protected GroupBoxLayout customC3GroupBox;

    @Autowired
    protected GroupBoxLayout customHeatmapGroupBox;

    @Autowired
    protected PopupButton propertiesCreateButton;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Actions actions;

    @Autowired
    protected RandomPivotTableDataGenerator dataGenerator;

    @Subscribe
    @SuppressWarnings("IncorrectCreateEntity")
    protected void onInit(InitEvent event) {
        super.onInit(event);

        PivotTableDescription description = createDefaultPivotTableDescription();
        pivotTableDc.setItem(description);
        initAggregationTable();
        initPropertyTable();
        pivotTableDc.addItemPropertyChangeListener(e -> showPreview());
    }

    @Override
    public void setItem(ReportTemplate reportTemplate) {
        super.setItem(reportTemplate);
        setBands(reportTemplate.getReport().getBands());
        if (isApplicable(reportTemplate.getReportOutputType())) {
            if (reportTemplate.getPivotTableDescription() == null) {
                pivotTableDc.setItem(createDefaultPivotTableDescription());
            } else {
                pivotTableDc.setItem(reportTemplate.getPivotTableDescription());
            }
        }
        initRendererTypes();
        propertyTable.expandAll();
    }

    @Override
    public boolean applyChanges() {
        ValidationErrors errors = validatePivotTableDescription(getPivotTableDescription());
        if (!errors.isEmpty()) {
            //todo
//            showValidationErrors(errors);
            return false;
        }
        getReportTemplate().setPivotTableDescription(getPivotTableDescription());
        return true;
    }

    @Override
    public boolean isApplicable(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.PIVOT_TABLE;
    }

    @Override
    public boolean isSupportPreview() {
        return true;
    }

    protected PivotTableDescription createDefaultPivotTableDescription() {
        PivotTableDescription description = new PivotTableDescription();
        if (description.getDefaultRenderer() == null) {
            description.setDefaultRenderer(RendererType.TABLE);
            description.setRenderers(Sets.newHashSet(RendererType.TABLE));
        }
        return description;
    }

    @Override
    protected void initPreviewContent(BoxLayout previewBox) {
        previewBox.removeAll();
        PivotTableDescription pivotTableDescription = getPivotTableDescription();
        ValidationErrors errors = validatePivotTableDescription(pivotTableDescription);
        if (errors.isEmpty()) {
            List<KeyValueEntity> data = dataGenerator.generate(pivotTableDescription, 10);
            //todo
//            Frame frame = openFrame(previewBox, ShowPivotTableController.PIVOT_TABLE_SCREEN_ID, ParamsMap.of(
//                    "pivotTableJson", PivotTableDescription.toJsonString(pivotTableDescription),
//                    "values", data));
//            frame.setHeight("472px");
        }
    }

    protected PivotTableDescription getPivotTableDescription() {
        return pivotTableDc.getItem();
    }

    protected ValidationErrors validatePivotTableDescription(PivotTableDescription description) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (description.getBandName() == null) {
            validationErrors.add(messages.getMessage("pivotTableEdit.bandRequired"));
        }
        if (description.getDefaultRenderer() == null) {
            validationErrors.add(messages.getMessage("pivotTableEdit.rendererRequired"));
        }
        if (description.getAggregations().isEmpty()) {
            validationErrors.add(messages.getMessage("pivotTableEdit.aggregationsRequired"));
        }
        if (description.getProperties().isEmpty()) {
            validationErrors.add(messages.getMessage("pivotTableEdit.propertiesRequired"));
        }
        if (description.getAggregationProperties().isEmpty()) {
            validationErrors.add(messages.getMessage("pivotTableEdit.aggregationPropertiesRequired"));
        }
        if (description.getColumnsProperties().isEmpty() && description.getRowsProperties().isEmpty()) {
            validationErrors.add(messages.getMessage("pivotTableEdit.columnsOrRowsRequired"));
        }
        if (!Collections.disjoint(description.getRowsProperties(), description.getColumnsProperties())
                || !Collections.disjoint(description.getRowsProperties(), description.getAggregationProperties())
                || !Collections.disjoint(description.getColumnsProperties(), description.getAggregationProperties())) {
            validationErrors.add(messages.getMessage("pivotTableEdit.propertyIntersection"));
        } else if (description.getProperties() != null) {
            Set<String> propertyNames = description.getProperties().stream()
                    .map(PivotTableProperty::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.size() != description.getProperties().size()) {
                validationErrors.add(messages.getMessage("pivotTableEdit.propertyIntersection"));
            }
        }
        return validationErrors;
    }

    protected void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = bands.stream()
                .filter(bandDefinition -> bandDefinition.getParentBandDefinition() != null)
                .map(BandDefinition::getName)
                .collect(Collectors.toList());
        bandNameField.setOptionsList(bandNames);
    }

    protected void initRendererTypes() {
        initCustomGroups();
        initDefaultRenderer();

        pivotTableDc.addItemPropertyChangeListener(e -> {
            if ("renderers".equals(e.getProperty())) {
                PivotTableDescription description = getPivotTableDescription();
                Set<RendererType> rendererTypes = description.getRenderers();
                if (rendererTypes.size() == 1) {
                    description.setDefaultRenderer(Iterables.getFirst(rendererTypes, null));
                }
                initCustomGroups();
                initDefaultRenderer();
            }
        });
    }

    protected void initAggregationTable() {
        Supplier<Map<String, Object>> paramsSupplier = () -> ParamsMap.of("existingItems", aggregationsDc.getItems());

        CreateAction createAction = actions.create(CreateAction.class);
        createAction.setScreenOptionsSupplier(paramsSupplier);
        aggregationsTable.addAction(createAction);

        EditAction editAction = actions.create(EditAction.class);
        editAction.setScreenOptionsSupplier(paramsSupplier);
        aggregationsTable.addAction(editAction);

        aggregationsTable.addAction(actions.create(RemoveAction.class));

        aggregationsDc.addCollectionChangeListener(e -> {
            if (e.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
                defaultAggregationField.setOptionsList(aggregationsDc.getItems());
            }
        });
    }

    protected void initCustomGroups() {
        Set<RendererType> rendererTypes = getPivotTableDescription().getRenderers();
        customC3GroupBox.setVisible(!Collections.disjoint(rendererTypes, C3_RENDERER_TYPES));
        customHeatmapGroupBox.setVisible(!Collections.disjoint(rendererTypes, HEATMAP_RENDERER_TYPES));
    }

    protected void initDefaultRenderer() {
        List<RendererType> rendererTypes = new ArrayList<>(getPivotTableDescription().getRenderers());
        defaultRendererField.setOptionsList(rendererTypes);
        defaultRendererField.setEnabled(rendererTypes.size() > 1);
    }

    protected void initPropertyTable() {
        propertyDc.addCollectionChangeListener(e -> {
            PivotTableDescription description = getPivotTableDescription();
            description.getAggregationProperties().clear();
            description.getColumnsProperties().clear();
            description.getRowsProperties().clear();

            for (PivotTableProperty property : getPivotTableDescription().getProperties()) {
                if (property.getType() == PivotTablePropertyType.AGGREGATIONS) {
                    description.getAggregationProperties().add(property.getName());
                } else if (property.getType() == PivotTablePropertyType.COLUMNS) {
                    description.getColumnsProperties().add(property.getName());
                } else if (property.getType() == PivotTablePropertyType.ROWS) {
                    description.getRowsProperties().add(property.getName());
                }
            }
            propertyTable.expandAll();
            showPreview();
        });

        propertyTable.addAction(createPropertyRemoveAction());
        propertyTable.addAction(createPropertyEditAction());

        propertiesCreateButton.setCaption(messages.getMessage("actions.Create"));

        CreateAction createAction = createPropertyCreateAction(PivotTablePropertyType.ROWS);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);

        createAction = createPropertyCreateAction(PivotTablePropertyType.COLUMNS);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);

        createAction = createPropertyCreateAction(PivotTablePropertyType.AGGREGATIONS);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);

        createAction = createPropertyCreateAction(PivotTablePropertyType.DERIVED);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);
    }

    protected RemoveAction createPropertyRemoveAction() {
        RemoveAction removeAction = actions.create(RemoveAction.class);
        removeAction.setTarget(propertyTable);
        return removeAction;
    }

    protected EditAction createPropertyEditAction() {
        EditAction action = (EditAction) propertyTable.getAction(EditAction.ID);
        action.setAfterCommitHandler(entity -> propertyTable.expandAll());
        return action;
    }

    protected CreateAction createPropertyCreateAction(PivotTablePropertyType propertyType) {
        CreateAction action = actions.create(CreateAction.class, "create_" + propertyType.getId());
        action.setOpenMode(OpenMode.THIS_TAB);
        action.setScreenOptionsSupplier(() -> new MapScreenOptions(ParamsMap.of("type", propertyType)));
        action.setCaption(messages.getMessage(propertyType));
        return action;
    }
}
