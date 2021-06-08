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

package io.jmix.reportsui.screen.definition.edit.crosstab;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reportsui.screen.definition.edit.BandDefinitionEditor;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Class presents decorator been for add some extra behavior on report band orientation change
 *
 * @see BandDefinitionEditor#initDataSetListeners() ()
 */
public class CrossTabTableDecorator {

    protected static final String HORIZONTAL_TPL = "dynamic_header";
    protected static final String VERTICAL_TPL = "master_data";

    @Autowired
    protected DataSetFactory dataSetFactory;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected Metadata metadata;

    public void decorate(Table<DataSet> dataSetsTable,
                         final CollectionContainer<DataSet> dataSetsDc,
                         final InstanceContainer<BandDefinition> bandDefinitionDc) {
        dataSetsTable.addGeneratedColumn("name", entity -> {
            TextField<String> textField = uiComponents.create(TextField.class);
            textField.setParent(dataSetsTable);
            textField.setWidthFull();
            textField.setHeightAuto();
            textField.setValue(entity.getName());
            textField.setValueSource(new ContainerValueSource<>(dataSetsTable.getInstanceContainer(entity), "name"));
            textField.setEditable(!isVerticalOrHorizontalCrossField(bandDefinitionDc, entity) && isUpdatePermitted());
            return textField;
        });

        bandDefinitionDc.addItemChangeListener(band -> onTableReady(dataSetsDc, bandDefinitionDc));
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


    protected void onTableReady(CollectionContainer<DataSet> dataSetsDc, InstanceContainer<BandDefinition> bandDefinitionDs) {
        initCrossDatasets(dataSetsDc, bandDefinitionDs);
    }

    protected void initCrossDatasets(CollectionContainer<DataSet> dataSetsDc,
                                     InstanceContainer<BandDefinition> bandDefinitionDc) {
        if (bandDefinitionDc == null) {
            return;
        }

        initListeners(dataSetsDc, bandDefinitionDc);
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

    protected void initListeners(CollectionContainer<DataSet> dataSetsDc, InstanceContainer<BandDefinition> bandDefinitionDc) {
        bandDefinitionDc.addItemPropertyChangeListener(e -> {
            if ("orientation".equals(e.getProperty())) {
                Orientation orientation = (Orientation) e.getValue();
                Orientation prevOrientation = (Orientation) e.getPrevValue();

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
            }

            if ("name".equals(e.getProperty()) && bandDefinitionDc.getItem().getOrientation() == Orientation.CROSS) {
                DataSet horizontal = getOrCreateDataSet(bandDefinitionDc, dataSetsDc, Orientation.HORIZONTAL);
                DataSet vertical = getOrCreateDataSet(bandDefinitionDc, dataSetsDc, Orientation.VERTICAL);

                onHorizontalSetChange(horizontal);
                onVerticalSetChange(vertical);
            }
        });
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
