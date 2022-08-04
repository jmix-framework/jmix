/*
 * Copyright 2021 Haulmont.
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
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.pivottable.*;
import io.jmix.reportsui.screen.report.run.ShowPivotTableScreen;
import io.jmix.reportsui.screen.template.edit.generator.RandomPivotTableDataGenerator;
import io.jmix.ui.Actions;
import io.jmix.ui.Fragments;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@UiController("report_PivotTableEdit.fragment")
@UiDescriptor("pivot-table-edit-fragment.xml")
public class PivotTableEditFragment extends DescriptionEditFragment {

    public static final Set<RendererType> C3_RENDERER_TYPES = Collections.unmodifiableSet(Sets.newHashSet(
            RendererType.LINE_CHART, RendererType.BAR_CHART, RendererType.STACKED_BAR_CHART,
            RendererType.AREA_CHART, RendererType.SCATTER_CHART));

    public static final Set<RendererType> HEATMAP_RENDERER_TYPES = Collections.unmodifiableSet(Sets.newHashSet(
            RendererType.HEATMAP, RendererType.COL_HEATMAP, RendererType.ROW_HEATMAP));

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
    protected MessageBundle messageBundle;

    @Autowired
    protected Actions actions;

    @Autowired
    protected RandomPivotTableDataGenerator dataGenerator;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected DataManager dataManager;

    @Subscribe
    @SuppressWarnings("IncorrectCreateEntity")
    protected void onInit(InitEvent event) {
        super.onInit(event);

        PivotTableDescription description = createDefaultPivotTableDescription();
        pivotTableDc.setItem(description);
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
            screenValidation.showValidationErrors(this, errors);
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
        PivotTableDescription description = dataManager.create(PivotTableDescription.class);
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

            Map<String, Object> params = ParamsMap.of(
                    "pivotTableJson", PivotTableDescription.toJsonString(pivotTableDescription),
                    "values", data);
            Fragment fragment = fragments.create(this, ShowPivotTableScreen.PIVOT_TABLE_SCREEN_ID, new MapScreenOptions(params))
                    .init()
                    .getFragment();
            fragment.setHeight("472px");

            previewBox.add(fragment);
        }
    }

    protected PivotTableDescription getPivotTableDescription() {
        return pivotTableDc.getItem();
    }

    protected ValidationErrors validatePivotTableDescription(PivotTableDescription description) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (description.getBandName() == null) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.bandRequired"));
        }
        if (description.getDefaultRenderer() == null) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.rendererRequired"));
        }
        if (description.getAggregations().isEmpty()) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.aggregationsRequired"));
        }
        if (description.getProperties().isEmpty()) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.propertiesRequired"));
        }
        if (description.getAggregationProperties().isEmpty()) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.aggregationPropertiesRequired"));
        }
        if (description.getColumnsProperties().isEmpty() && description.getRowsProperties().isEmpty()) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.columnsOrRowsRequired"));
        }
        if (!Collections.disjoint(description.getRowsProperties(), description.getColumnsProperties())
                || !Collections.disjoint(description.getRowsProperties(), description.getAggregationProperties())
                || !Collections.disjoint(description.getColumnsProperties(), description.getAggregationProperties())) {
            validationErrors.add(messageBundle.getMessage("pivotTableEdit.propertyIntersection"));
        } else if (description.getProperties() != null) {
            Set<String> propertyNames = description.getProperties().stream()
                    .map(PivotTableProperty::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.size() != description.getProperties().size()) {
                validationErrors.add(messageBundle.getMessage("pivotTableEdit.propertyIntersection"));
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

    @Subscribe(id = "aggregationsDc", target = Target.DATA_CONTAINER)
    public void onAggregationsDcCollectionChange(CollectionContainer.CollectionChangeEvent<PivotTableAggregation> e) {
        if (e.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
            defaultAggregationField.setOptionsList(aggregationsDc.getItems());
        }
    }

    @Install(to = "aggregationsTable.create", subject = "screenOptionsSupplier")
    protected ScreenOptions aggregationsTableCreateScreenOptionsSupplier() {
        return new MapScreenOptions(ParamsMap.of("existingItems", aggregationsDc.getItems()));
    }

    @Install(to = "aggregationsTable.edit", subject = "screenOptionsSupplier")
    protected ScreenOptions aggregationsTableEditScreenOptionsSupplier() {
        return new MapScreenOptions(ParamsMap.of("existingItems", aggregationsDc.getItems()));
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

    @Subscribe(id = "propertyDc", target = Target.DATA_CONTAINER)
    public void onPropertyDcCollectionChange(CollectionContainer.CollectionChangeEvent<PivotTableProperty> event) {
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
    }

    protected void initPropertyTable() {
        CreateAction<PivotTableProperty> createAction = createPropertyCreateAction(PivotTablePropertyType.ROWS);
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

    @Install(to = "propertyTable.edit", subject = "afterCommitHandler")
    protected void propertyTableEditAfterCommitHandler(PivotTableProperty property) {
        propertyTable.expandAll();
    }

    protected CreateAction<PivotTableProperty> createPropertyCreateAction(PivotTablePropertyType propertyType) {
        CreateAction<PivotTableProperty> action = actions.create(CreateAction.class, "create_" + propertyType.getId());
        action.setOpenMode(OpenMode.THIS_TAB);
        action.setInitializer(property -> property.setType(propertyType));
        action.setCaption(messages.getMessage(propertyType));
        return action;
    }
}
