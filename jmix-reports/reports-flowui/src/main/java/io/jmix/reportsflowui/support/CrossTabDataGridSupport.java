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

package io.jmix.reportsflowui.support;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Class provides helper methods to work with {@link DataSet}.
 */
@Component("report_CrossTabOrientationDataGridSupport")
public class CrossTabDataGridSupport {

    protected static final String HORIZONTAL_TPL = "dynamic_header";
    protected static final String VERTICAL_TPL = "master_data";

    protected DataSetFactory dataSetFactory;
    protected UiComponents uiComponents;
    protected SecureOperations secureOperations;
    protected PolicyStore policyStore;
    protected Metadata metadata;
    protected DataComponents dataComponents;
    protected Messages messages;

    public CrossTabDataGridSupport(DataSetFactory dataSetFactory, UiComponents uiComponents,
                                   SecureOperations secureOperations, PolicyStore policyStore, Metadata metadata,
                                   DataComponents dataComponents, Messages messages) {
        this.dataSetFactory = dataSetFactory;
        this.uiComponents = uiComponents;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
        this.metadata = metadata;
        this.dataComponents = dataComponents;
        this.messages = messages;
    }

    public void decorate(JmixGrid<DataSet> dataSetsDataGrid,
                         CollectionContainer<DataSet> dataSetsDc,
                         InstanceContainer<BandDefinition> bandDefinitionDc) {
        dataSetsDataGrid.addComponentColumn(entity -> {
            TypedTextField<String> field = uiComponents.create(TypedTextField.class);
            field.setValue(entity.getName() == null ? field.getEmptyValue() : entity.getName());
            field.setWidthFull();
            field.addValueChangeListener(valueChanged -> {
                entity.setName(valueChanged.getValue());

                // Avoiding bug with not selected edited row
                dataSetsDc.setItem(entity);
                dataSetsDataGrid.select(entity);
            });

            InstanceContainer<DataSet> instanceContainer = dataComponents.createInstanceContainer(DataSet.class);
            instanceContainer.setItem(entity);

            field.setValueSource(new ContainerValueSource<>(instanceContainer, "name"));
            field.setReadOnly(isVerticalOrHorizontalCrossField(bandDefinitionDc, entity) || !isUpdatePermitted());
            return field;
        }).setHeader(messages.getMessage(ReportDetailView.class, "bandsTab.dataSetsDataGrid.nameColumn.header"));

        bandDefinitionDc.addItemPropertyChangeListener(e ->
                onBandDefinitionDcItemPropertyChange(e, dataSetsDataGrid, dataSetsDc, bandDefinitionDc));
    }

    protected void onBandDefinitionDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<BandDefinition> event,
                                                        JmixGrid<DataSet> dataSetsDataGrid,
                                                        CollectionContainer<DataSet> dataSetsDc,
                                                        InstanceContainer<BandDefinition> bandDefinitionDc) {
        if ("orientation".equals(event.getProperty())) {
            Orientation orientation = (Orientation) event.getValue();
            Orientation prevOrientation = (Orientation) event.getPrevValue();

            if (orientation == prevOrientation) return;

            if (Orientation.CROSS == orientation || Orientation.CROSS == prevOrientation) {
                onOrientationChange(dataSetsDc, bandDefinitionDc);
            }

            if (Orientation.CROSS == orientation) {
                DataSet horizontal = getOrCreateDataSet(bandDefinitionDc, dataSetsDc, Orientation.HORIZONTAL);
                DataSet vertical = getOrCreateDataSet(bandDefinitionDc, dataSetsDc, Orientation.VERTICAL);

                dataSetsDc.getMutableItems().add(horizontal);
                dataSetsDc.getMutableItems().add(vertical);
            }

            // Select first item
            dataSetsDataGrid.deselectAll();
            DataSet item = dataSetsDc.getItems().iterator().next();
            dataSetsDc.setItem(item);
            dataSetsDataGrid.select(item);
        }

        if ("name".equals(event.getProperty()) && bandDefinitionDc.getItem().getOrientation() == Orientation.CROSS) {
            DataSet horizontal = getOrCreateDataSet(bandDefinitionDc, dataSetsDc, Orientation.HORIZONTAL);
            DataSet vertical = getOrCreateDataSet(bandDefinitionDc, dataSetsDc, Orientation.VERTICAL);

            onHorizontalSetChange(horizontal);
            onVerticalSetChange(vertical);
        }
    }

    protected boolean isVerticalOrHorizontalCrossField(InstanceContainer<BandDefinition> bandDefinitionDc,
                                                       DataSet dataSet) {
        return Orientation.CROSS == bandDefinitionDc.getItem().getOrientation()
                && !Strings.isNullOrEmpty(dataSet.getName())
                && (dataSet.getName().endsWith(HORIZONTAL_TPL) || dataSet.getName().endsWith(VERTICAL_TPL));
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected void onHorizontalSetChange(DataSet dataSet) {
        dataSet.setName(String.format("%s_" + HORIZONTAL_TPL, dataSet.getBandDefinition().getName()));
    }

    protected void onVerticalSetChange(DataSet dataSet) {
        dataSet.setName(String.format("%s_" + VERTICAL_TPL, dataSet.getBandDefinition().getName()));
    }

    @Nullable
    protected DataSet getCrossDataSet(CollectionContainer<DataSet> dataSetsDc, Orientation orientation) {
        for (DataSet dataSet : dataSetsDc.getItems()) {
            if (orientation == Orientation.HORIZONTAL) {
                if (!Strings.isNullOrEmpty(dataSet.getName()) && dataSet.getName().endsWith(HORIZONTAL_TPL)) {
                    return dataSet;
                }
            }

            if (orientation == Orientation.VERTICAL) {
                if (!Strings.isNullOrEmpty(dataSet.getName()) && dataSet.getName().endsWith(VERTICAL_TPL)) {
                    return dataSet;
                }
            }
        }
        return null;
    }

    protected DataSet createDataSet(InstanceContainer<BandDefinition> bandDefinitionDc, Orientation orientation) {
        DataSet dataSet = dataSetFactory.createEmptyDataSet(bandDefinitionDc.getItem());

        if (Orientation.HORIZONTAL == orientation) {
            onHorizontalSetChange(dataSet);
        } else if (Orientation.VERTICAL == orientation) {
            onVerticalSetChange(dataSet);
        }

        return dataSet;
    }

    protected DataSet getOrCreateDataSet(InstanceContainer<BandDefinition> bandDefinitionDc,
                                         CollectionContainer<DataSet> dataSetsDc,
                                         Orientation orientation) {
        DataSet dataSet = getCrossDataSet(dataSetsDc, orientation);
        return dataSet != null ? dataSet : createDataSet(bandDefinitionDc, orientation);
    }

    protected void onOrientationChange(CollectionContainer<DataSet> dataSetsDc, InstanceContainer<BandDefinition> bandDefinitionDc) {
        dataSetsDc.getMutableItems().clear();
        dataSetsDc.getMutableItems().add(dataSetFactory.createEmptyDataSet(bandDefinitionDc.getItem()));
    }
}
