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

import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Sort;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.charts.*;
import io.jmix.reportsui.screen.report.run.ShowChartLookup;
import io.jmix.reportsui.screen.template.edit.generator.RandomChartDataGenerator;
import io.jmix.ui.Actions;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@UiController("report_ChartEdit.fragment")
@UiDescriptor("chart-edit-frame.xml")
public class ChartEditFragment extends DescriptionEditFragment {
    @Autowired
    protected ComboBox<String> serialBandNameField;
    @Autowired
    protected ComboBox<String> pieBandNameField;
    @Autowired
    protected InstanceContainer<PieChartDescription> pieChartDc;
    @Autowired
    protected InstanceContainer<SerialChartDescription> serialChartDc;
    @Autowired
    protected CollectionContainer<ChartSeries> seriesDc;
    @Autowired
    protected ComboBox<ChartType> typeField;
    @Autowired
    protected Table<ChartSeries> seriesTable;
    @Autowired
    protected GroupBoxLayout seriesGroupBox;
    @Autowired
    protected Form pieChartForm;
    @Autowired
    protected Form serialChartForm;
    @Autowired
    protected SourceCodeEditor serialJsonConfigEditor;
    @Autowired
    protected SourceCodeEditor pieJsonConfigEditor;
    @Autowired
    protected BeanFactory beanFactory;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Actions actions;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Fragments fragments;

    @Subscribe
    @SuppressWarnings("IncorrectCreateEntity")
    protected void onInit(InitEvent event) {
        super.onInit(event);

        pieChartDc.setItem(new PieChartDescription());
        serialChartDc.setItem(new SerialChartDescription());
        typeField.setOptionsList(Arrays.asList(ChartType.values()));

        pieChartForm.setVisible(false);
        serialChartForm.setVisible(false);
        seriesGroupBox.setVisible(false);
        serialJsonConfigEditor.setVisible(false);
        pieJsonConfigEditor.setVisible(false);

        seriesTable.addAction(new ChartSeriesMoveAction(true));
        seriesTable.addAction(new ChartSeriesMoveAction(false));

        serialJsonConfigEditor.addValidator(new JsonConfigValidator(getClass()));
        pieJsonConfigEditor.addValidator(new JsonConfigValidator(getClass()));
    }

    @Subscribe("seriesTable.create")
    protected void onSeriesTableCreate(Action.ActionPerformedEvent event) {
        ChartSeries chartSeries = dataManager.create(ChartSeries.class);
        chartSeries.setOrder(seriesDc.getItems().size() + 1);
        seriesDc.getMutableItems().add(chartSeries);
    }

    @Subscribe(id = "seriesDc", target = Target.DATA_CONTAINER)
    protected void onSeriesDcCollectionChange(CollectionContainer.CollectionChangeEvent<ChartSeries> event) {
        checkSeriesOrder();
        showPreview();
    }

    @Subscribe(id = "seriesDc", target = Target.DATA_CONTAINER)
    protected void onSeriesDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ChartSeries> event) {
        showPreview();
    }

    @Subscribe(id = "serialChartDc", target = Target.DATA_CONTAINER)
    protected void onSerialChartDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<SerialChartDescription> event) {
        showPreview();
    }

    @Subscribe(id = "pieChartDc", target = Target.DATA_CONTAINER)
    protected void onPieChartDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<PieChartDescription> event) {
        showPreview();
    }

    @Install(to = "serialJsonConfigEditor", subject = "contextHelpIconClickHandler")
    protected void serialJsonConfigEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent event) {
        jsonEditorContextHelpIconClickHandler(event);
    }

    @Install(to = "pieJsonConfigEditor", subject = "contextHelpIconClickHandler")
    protected void pieJsonConfigEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent event) {
        jsonEditorContextHelpIconClickHandler(event);
    }

    @Subscribe("serialJsonConfigEditor")
    protected void onSerialJsonConfigEditorValueChange(HasValue.ValueChangeEvent<String> event) {
        codeEditorChangeListener(event);
    }

    @Subscribe("pieJsonConfigEditor")
    protected void onPieJsonConfigEditorValueChange(HasValue.ValueChangeEvent<String> event) {
        codeEditorChangeListener(event);
    }

    @Subscribe("typeField")
    protected void onTypeFieldValueChange(HasValue.ValueChangeEvent event) {
        pieChartForm.setVisible(ChartType.PIE == event.getValue());
        serialChartForm.setVisible(ChartType.SERIAL == event.getValue());
        seriesGroupBox.setVisible(ChartType.SERIAL == event.getValue());
        serialJsonConfigEditor.setVisible(ChartType.SERIAL == event.getValue());
        pieJsonConfigEditor.setVisible(ChartType.PIE == event.getValue());
        showPreview();
    }

    protected void codeEditorChangeListener(HasValue.ValueChangeEvent<String> event) {
        if (ChartType.SERIAL == typeField.getValue() && serialJsonConfigEditor.isValid()) {
            serialChartDc.getItem().setCustomJsonConfig(event.getValue());
        } else if (ChartType.PIE == typeField.getValue() && pieJsonConfigEditor.isValid()) {
            pieChartDc.getItem().setCustomJsonConfig(event.getValue());
        }
    }

    protected void jsonEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent event) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("chartEdit.jsonConfig"))
                .withMessage(event.getSource().getContextHelpText())
                .show();
    }

    @Override
    public void setItem(ReportTemplate reportTemplate) {
        super.setItem(reportTemplate);
        setBands(reportTemplate.getReport().getBands());
        if (isApplicable(reportTemplate.getReportOutputType())) {
            setChartDescription(reportTemplate.getChartDescription());
            sortSeriesByOrder();
        }
    }

    @Override
    public boolean applyChanges() {
        if (validateChart()) {
            AbstractChartDescription chartDescription = getChartDescription();
            getReportTemplate().setChartDescription(chartDescription);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CHART;
    }

    @Override
    public boolean isSupportPreview() {
        return true;
    }

    protected boolean validateChart() {
        AbstractChartDescription chartDescription = getChartDescription();
        if (chartDescription != null && chartDescription.getType() == ChartType.SERIAL) {
            List<ChartSeries> series = ((SerialChartDescription) chartDescription).getSeries();
            if (series == null || series.size() == 0) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.getMessage("validationFail.caption"))
                        .withDescription(messages.getMessage(getClass(), "chartEdit.seriesEmptyMsg"))
                        .show();
                return false;
            }
            for (ChartSeries it : series) {
                if (it.getType() == null) {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messages.getMessage("validationFail.caption"))
                            .withDescription(messages.getMessage(getClass(), "chartEdit.seriesTypeNullMsg"))
                            .show();
                    return false;
                }
                if (it.getValueField() == null) {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messages.getMessage("validationFail.caption"))
                            .withDescription(messages.getMessage(getClass(), "chartEdit.seriesValueFieldNullMsg"))
                            .show();
                    return false;
                }
            }
        }
        return true;
    }

    protected void initPreviewContent(BoxLayout previewBox) {
        List<Map<String, Object>> data;
        String chartJson = null;
        if (ChartType.SERIAL == typeField.getValue()) {
            SerialChartDescription chartDescription = serialChartDc.getItem();
            data = new RandomChartDataGenerator().generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = beanFactory.getBean(ChartToJsonConverter.class);
            chartJson = chartToJsonConverter.convertSerialChart(chartDescription, data);
        } else if (ChartType.PIE == typeField.getValue()) {
            PieChartDescription chartDescription = pieChartDc.getItem();
            data = new RandomChartDataGenerator().generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = beanFactory.getBean(ChartToJsonConverter.class);
            chartJson = chartToJsonConverter.convertPieChart(chartDescription, data);
        }
        chartJson = chartJson == null ? "{}" : chartJson;

        Map<String, Object> parmas = ParamsMap.of(ShowChartLookup.CHART_JSON_PARAMETER, chartJson);

        //todo chart
//        Fragment fragment = fragments.create(this, ShowChartLookup.JSON_CHART_SCREEN_ID, new MapScreenOptions(parmas))
//                .init()
//                .getFragment();
//
//        if (ChartType.SERIAL == typeField.getValue()) {
//            fragment.setHeight("700px");
//        } else if (ChartType.PIE == typeField.getValue()) {
//            fragment.setHeight("350px");
//        }
//        previewBox.add(fragment);
    }

    @Nullable
    protected AbstractChartDescription getChartDescription() {
        if (ChartType.SERIAL == typeField.getValue()) {
            return serialChartDc.getItem();
        } else if (ChartType.PIE == typeField.getValue()) {
            return pieChartDc.getItem();
        }
        return null;
    }

    protected void setChartDescription(@Nullable AbstractChartDescription chartDescription) {
        if (chartDescription != null) {
            if (ChartType.SERIAL == chartDescription.getType()) {
                serialChartDc.setItem((SerialChartDescription) chartDescription);
                serialJsonConfigEditor.setValue(chartDescription.getCustomJsonConfig());
            } else if (ChartType.PIE == chartDescription.getType()) {
                pieChartDc.setItem((PieChartDescription) chartDescription);
                pieJsonConfigEditor.setValue(chartDescription.getCustomJsonConfig());
            }
            typeField.setValue(chartDescription.getType());
        }
    }

    protected void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = bands.stream()
                .filter(bandDefinition -> bandDefinition.getParentBandDefinition() != null)
                .map(BandDefinition::getName)
                .collect(Collectors.toList());

        pieBandNameField.setOptionsList(bandNames);
        serialBandNameField.setOptionsList(bandNames);
    }

    protected void checkSeriesOrder() {
        Collection<ChartSeries> items = seriesDc.getItems();
        int i = 1;
        for (ChartSeries item : items) {
            if (!Objects.equals(i, item.getOrder())) {
                item.setOrder(i);
            }
            i += 1;
        }
    }

    protected class ChartSeriesMoveAction extends ItemTrackingAction {
        private final boolean up;

        ChartSeriesMoveAction(boolean up) {
            super(seriesTable, up ? "up" : "down");
            setCaption(messages.getMessage(getClass(), up ? "generalFrame.up" : "generalFrame.down"));
            this.up = up;
        }

        @Override
        public void actionPerform(Component component) {
            ChartSeries selected = seriesTable.getSingleSelected();
            //noinspection ConstantConditions
            Integer currentOrder = selected.getOrder();
            Integer newOrder = up ? currentOrder - 1 : currentOrder + 1;

            Collection<ChartSeries> items = seriesDc.getItems();

            ChartSeries changing = IterableUtils.get(items, currentOrder - 1);
            ChartSeries neighbor = IterableUtils.get(items, newOrder - 1);
            changing.setOrder(newOrder);
            neighbor.setOrder(currentOrder);

            sortSeriesByOrder();
        }

        @Override
        public boolean isPermitted() {
            if (super.isPermitted()) {
                Set<ChartSeries> items = seriesTable.getSelected();
                if (!CollectionUtils.isEmpty(items) && items.size() == 1) {
                    Integer order = (IterableUtils.get(items, 0)).getOrder();
                    if (order != null) {
                        return up ? order > 1 : order < seriesDc.getItems().size();
                    }
                }
            }
            return false;
        }
    }

    protected void sortSeriesByOrder() {
        seriesDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "order"));
    }
}
