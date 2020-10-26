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

package io.jmix.reportsui.gui.template.edit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import io.jmix.core.common.util.ParamsMap;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.GroupDatasource;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.pivottable.*;
import io.jmix.reportsui.gui.report.run.ShowPivotTableController;
import io.jmix.reportsui.gui.template.edit.generator.RandomPivotTableDataGenerator;

import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.gui.OpenType;
import org.springframework.beans.factory.annotation.Autowired;
import javax.inject.Named;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PivotTableEditFrame extends DescriptionEditFrame {

    public static final Set<RendererType> C3_RENDERER_TYPES = Sets.newHashSet(
            RendererType.LINE_CHART, RendererType.BAR_CHART, RendererType.STACKED_BAR_CHART,
            RendererType.AREA_CHART, RendererType.SCATTER_CHART);

    public static final Set<RendererType> HEATMAP_RENDERER_TYPES = Sets.newHashSet(
            RendererType.HEATMAP, RendererType.COL_HEATMAP, RendererType.ROW_HEATMAP);

    @Autowired
    protected Datasource<PivotTableDescription> pivotTableDs;

    @Autowired
    protected CollectionDatasource<PivotTableAggregation, UUID> aggregationsDs;

    @Autowired
    protected GroupDatasource<PivotTableProperty, UUID> propertyDs;

    @Autowired
    protected GroupTable<PivotTableProperty> propertyTable;

    @Autowired
    protected Table<PivotTableAggregation> aggregationsTable;

    @Named("rendererGroup.defaultRenderer")
    protected LookupField defaultRenderer;

    @Autowired
    protected LookupField defaultAggregation;

    @Named("pivotTableGroup.bandName")
    protected LookupField bandName;

    @Autowired
    protected GroupBoxLayout customC3GroupBox;

    @Autowired
    protected GroupBoxLayout customHeatmapGroupBox;

    @Autowired
    protected PopupButton propertiesCreateButton;

    protected RandomPivotTableDataGenerator dataGenerator;

    @Override
    @SuppressWarnings("IncorrectCreateEntity")
    public void init(Map<String, Object> params) {
        super.init(params);
        dataGenerator = new RandomPivotTableDataGenerator();
        PivotTableDescription description = createDefaultPivotTableDescription();
        pivotTableDs.setItem(description);
        initAggregationTable();
        initPropertyTable();
        pivotTableDs.addItemPropertyChangeListener(e -> showPreview());
    }

    @Override
    public void setItem(ReportTemplate reportTemplate) {
        super.setItem(reportTemplate);
        setBands(reportTemplate.getReport().getBands());
        if (isApplicable(reportTemplate.getReportOutputType())) {
            if (reportTemplate.getPivotTableDescription() == null) {
                pivotTableDs.setItem(createDefaultPivotTableDescription());
            } else {
                pivotTableDs.setItem(reportTemplate.getPivotTableDescription());
            }
        }
        initRendererTypes();
        propertyTable.expandAll();
    }

    @Override
    public boolean applyChanges() {
        ValidationErrors errors = validatePivotTableDescription(getPivotTableDescription());
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
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
            Frame frame = openFrame(previewBox, ShowPivotTableController.PIVOT_TABLE_SCREEN_ID, ParamsMap.of(
                    "pivotTableJson", PivotTableDescription.toJsonString(pivotTableDescription),
                    "values", data));
            frame.setHeight("472px");
        }
    }

    protected PivotTableDescription getPivotTableDescription() {
        return pivotTableDs.getItem();
    }

    protected ValidationErrors validatePivotTableDescription(PivotTableDescription description) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (description.getBandName() == null) {
            validationErrors.add(getMessage("pivotTableEdit.bandRequired"));
        }
        if (description.getDefaultRenderer() == null) {
            validationErrors.add(getMessage("pivotTableEdit.rendererRequired"));
        }
        if (description.getAggregations().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.aggregationsRequired"));
        }
        if (description.getProperties().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.propertiesRequired"));
        }
        if (description.getAggregationProperties().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.aggregationPropertiesRequired"));
        }
        if (description.getColumnsProperties().isEmpty() && description.getRowsProperties().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.columnsOrRowsRequired"));
        }
        if (!Collections.disjoint(description.getRowsProperties(), description.getColumnsProperties())
                || !Collections.disjoint(description.getRowsProperties(), description.getAggregationProperties())
                || !Collections.disjoint(description.getColumnsProperties(), description.getAggregationProperties())) {
            validationErrors.add(getMessage("pivotTableEdit.propertyIntersection"));
        } else if (description.getProperties() != null) {
            Set<String> propertyNames = description.getProperties().stream()
                    .map(PivotTableProperty::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.size() != description.getProperties().size()) {
                validationErrors.add(getMessage("pivotTableEdit.propertyIntersection"));
            }
        }
        return validationErrors;
    }

    protected void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = bands.stream()
                .filter(bandDefinition -> bandDefinition.getParentBandDefinition() != null)
                .map(BandDefinition::getName)
                .collect(Collectors.toList());
        bandName.setOptionsList(bandNames);
    }

    protected void initRendererTypes() {
        initCustomGroups();
        initDefaultRenderer();

        pivotTableDs.addItemPropertyChangeListener(e -> {
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
        Supplier<Map<String, Object>> paramsSupplier = () -> ParamsMap.of("existingItems", aggregationsDs.getItems());
        CreateAction createAction = CreateAction.create(aggregationsTable);
        createAction.setWindowParamsSupplier(paramsSupplier);
        aggregationsTable.addAction(createAction);
        EditAction editAction = EditAction.create(aggregationsTable);
        editAction.setWindowParamsSupplier(paramsSupplier);
        aggregationsTable.addAction(editAction);
        aggregationsTable.addAction(RemoveAction.create(aggregationsTable));

        aggregationsDs.addCollectionChangeListener(e -> {
            if (e.getOperation() == CollectionDatasource.Operation.REMOVE) {
                defaultAggregation.setOptionsDatasource(aggregationsDs);
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
        defaultRenderer.setOptionsList(rendererTypes);
        defaultRenderer.setEnabled(rendererTypes.size() > 1);
    }

    protected void initPropertyTable() {
        propertyDs.addCollectionChangeListener(e -> {
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
        return RemoveAction.create(propertyTable);
    }

    protected EditAction createPropertyEditAction() {
        EditAction action = EditAction.create(propertyTable);
        action.setAfterCommitHandler(entity -> propertyTable.expandAll());
        return action;
    }

    protected CreateAction createPropertyCreateAction(PivotTablePropertyType propertyType) {
        CreateAction action = CreateAction.create(propertyTable, OpenType.THIS_TAB, "create_" + propertyType.getId());
        Map<String, Object> values = new HashMap<>();
        values.put("type", propertyType);
        action.setInitialValues(values);
        action.setCaption(messages.getMessage(propertyType));
        return action;
    }
}
