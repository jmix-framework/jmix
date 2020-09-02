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

package io.jmix.reports.gui.template.edit;

import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.charts.*;
import io.jmix.reports.gui.report.run.ShowChartController;
import io.jmix.reports.gui.template.edit.generator.RandomChartDataGenerator;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChartEditFrame extends DescriptionEditFrame {
    @Autowired
    protected Datasource<PieChartDescription> pieChartDs;
    @Autowired
    protected Datasource<SerialChartDescription> serialChartDs;
    @Autowired
    protected CollectionDatasource.Sortable<ChartSeries, UUID> seriesDs;
    @Autowired
    protected LookupField<ChartType> type;
    @Autowired
    protected Table<ChartSeries> seriesTable;
    @Autowired
    protected GroupBoxLayout seriesBox;
    @Autowired
    protected FieldGroup pieChartFieldGroup;
    @Autowired
    protected FieldGroup serialChartFieldGroup;
    @Autowired
    private SourceCodeEditor serialJsonConfigEditor;
    @Autowired
    private SourceCodeEditor pieJsonConfigEditor;
    @Autowired
    private BeanLocator beanLocator;

    @Override
    @SuppressWarnings("IncorrectCreateEntity")
    public void init(Map<String, Object> params) {
        super.init(params);
        pieChartDs.setItem(new PieChartDescription());
        serialChartDs.setItem(new SerialChartDescription());
        type.setOptionsList(Arrays.asList(ChartType.values()));

        type.addValueChangeListener(e -> {
            pieChartFieldGroup.setVisible(ChartType.PIE == e.getValue());
            serialChartFieldGroup.setVisible(ChartType.SERIAL == e.getValue());
            seriesBox.setVisible(ChartType.SERIAL == e.getValue());
            serialJsonConfigEditor.setVisible(ChartType.SERIAL == e.getValue());
            pieJsonConfigEditor.setVisible(ChartType.PIE == e.getValue());
            showPreview();
        });

        pieChartFieldGroup.setVisible(false);
        serialChartFieldGroup.setVisible(false);
        seriesBox.setVisible(false);
        serialJsonConfigEditor.setVisible(false);
        pieJsonConfigEditor.setVisible(false);

        seriesTable.addAction(new CreateAction(seriesTable) {
            @Override
            public void actionPerform(Component component) {
                @SuppressWarnings("IncorrectCreateEntity")
                ChartSeries chartSeries = new ChartSeries();
                chartSeries.setOrder(seriesDs.getItems().size() + 1);
                seriesDs.addItem(chartSeries);
            }
        });
        seriesTable.addAction(new ChartSeriesMoveAction(true));
        seriesTable.addAction(new ChartSeriesMoveAction(false));

        pieChartDs.addItemPropertyChangeListener(e -> showPreview());

        serialChartDs.addItemPropertyChangeListener(e -> showPreview());

        seriesDs.addItemPropertyChangeListener(e -> showPreview());
        seriesDs.addCollectionChangeListener(e -> {
            checkSeriesOrder();
            showPreview();
        });

        serialJsonConfigEditor.addValueChangeListener(this::codeEditorChangeListener);
        pieJsonConfigEditor.addValueChangeListener(this::codeEditorChangeListener);

        Consumer<String> validator = beanLocator.getPrototype(JsonConfigValidator.NAME, getMessagesPack());
        serialJsonConfigEditor.addValidator(validator);
        pieJsonConfigEditor.addValidator(validator);

        serialJsonConfigEditor.setContextHelpIconClickHandler(this::jsonEditorContextHelpIconClickHandler);
        pieJsonConfigEditor.setContextHelpIconClickHandler(this::jsonEditorContextHelpIconClickHandler);
    }

    protected void codeEditorChangeListener(HasValue.ValueChangeEvent<String> event) {
        if (ChartType.SERIAL == type.getValue() && serialJsonConfigEditor.isValid()) {
            serialChartDs.getItem().setCustomJsonConfig(event.getValue());
        } else if (ChartType.PIE == type.getValue() && pieJsonConfigEditor.isValid()) {
            pieChartDs.getItem().setCustomJsonConfig(event.getValue());
        }
    }

    protected void jsonEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent event) {
        showMessageDialog(getMessage("chartEdit.jsonConfig"),
                event.getSource().getContextHelpText(),
                MessageType.CONFIRMATION_HTML);
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
                showNotification(getMessage("validationFail.caption"),
                        getMessage("chartEdit.seriesEmptyMsg"), NotificationType.TRAY);
                return false;
            }
            for (ChartSeries it : series) {
                if (it.getType() == null) {
                    showNotification(getMessage("validationFail.caption"),
                            getMessage("chartEdit.seriesTypeNullMsg"), NotificationType.TRAY);
                    return false;
                }
                if (it.getValueField() == null) {
                    showNotification(getMessage("validationFail.caption"),
                            getMessage("chartEdit.seriesValueFieldNullMsg"), NotificationType.TRAY);
                    return false;
                }
            }
        }
        return true;
    }

    protected void initPreviewContent(BoxLayout previewBox) {
        List<Map<String, Object>> data;
        String chartJson = null;
        if (ChartType.SERIAL == type.getValue()) {
            SerialChartDescription chartDescription = serialChartDs.getItem();
            data = new RandomChartDataGenerator().generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            chartJson = chartToJsonConverter.convertSerialChart(chartDescription, data);
        } else if (ChartType.PIE == type.getValue()) {
            PieChartDescription chartDescription = pieChartDs.getItem();
            data = new RandomChartDataGenerator().generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            chartJson = chartToJsonConverter.convertPieChart(chartDescription, data);
        }
        chartJson = chartJson == null ? "{}" : chartJson;
        Frame frame = openFrame(previewBox, ShowChartController.JSON_CHART_SCREEN_ID,
                Collections.singletonMap(ShowChartController.CHART_JSON_PARAMETER, chartJson));
        if (ChartType.SERIAL == type.getValue()) {
            frame.setHeight("700px");
        } else if (ChartType.PIE == type.getValue()) {
            frame.setHeight("350px");
        }
    }

    @Nullable
    protected AbstractChartDescription getChartDescription() {
        if (ChartType.SERIAL == type.getValue()) {
            return serialChartDs.getItem();
        } else if (ChartType.PIE == type.getValue()) {
            return pieChartDs.getItem();
        }
        return null;
    }

    protected void setChartDescription(@Nullable AbstractChartDescription chartDescription) {
        if (chartDescription != null) {
            if (ChartType.SERIAL == chartDescription.getType()) {
                serialChartDs.setItem((SerialChartDescription) chartDescription);
                serialJsonConfigEditor.setValue(chartDescription.getCustomJsonConfig());
            } else if (ChartType.PIE == chartDescription.getType()) {
                pieChartDs.setItem((PieChartDescription) chartDescription);
                pieJsonConfigEditor.setValue(chartDescription.getCustomJsonConfig());
            }
            type.setValue(chartDescription.getType());
        }
    }

    protected void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = bands.stream()
                .filter(bandDefinition -> bandDefinition.getParentBandDefinition() != null)
                .map(BandDefinition::getName)
                .collect(Collectors.toList());

        LookupField pieChartBandName = (LookupField) pieChartFieldGroup.getComponentNN("bandName");
        LookupField serialChartBandName = (LookupField) serialChartFieldGroup.getComponentNN("bandName");

        pieChartBandName.setOptionsList(bandNames);
        serialChartBandName.setOptionsList(bandNames);
    }

    protected void checkSeriesOrder() {
        Collection<ChartSeries> items = seriesDs.getItems();
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
            setCaption(getMessage(up ? "generalFrame.up" : "generalFrame.down"));
            this.up = up;
        }

        @Override
        public void actionPerform(Component component) {
            ChartSeries selected = seriesTable.getSingleSelected();
            //noinspection ConstantConditions
            Integer currentOrder = selected.getOrder();
            Integer newOrder = up ? currentOrder - 1 : currentOrder + 1;

            Collection<ChartSeries> items = seriesDs.getItems();

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
                        return up ? order > 1 : order < seriesDs.size();
                    }
                }
            }
            return false;
        }
    }

    protected void sortSeriesByOrder() {
        CollectionDatasource.Sortable.SortInfo<MetaPropertyPath> sortInfo = new CollectionDatasource.Sortable.SortInfo<>();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
        sortInfo.setPropertyPath(seriesDs.getMetaClass().getPropertyPath("order"));
        seriesDs.sort(new CollectionDatasource.Sortable.SortInfo[]{sortInfo});
    }
}
