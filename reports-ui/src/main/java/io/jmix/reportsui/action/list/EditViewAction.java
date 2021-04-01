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

package io.jmix.reportsui.action.list;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.Reports;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.app.service.ReportsWizard;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.screen.report.wizard.region.RegionEditor;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.StandardOutcome;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@StudioAction(target = "io.jmix.ui.component.ListComponent", description = "Edit action for an entity band")
@ActionType(EditViewAction.ID)
public class EditViewAction extends ListAction {

    public static final String ID = "editViewEntity";

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Reports reports;

    @Autowired
    protected ReportsWizard reportWizardService;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    protected Table<DataSet> dataSetsTable;
    protected CollectionContainer<BandDefinition> bandsDc;
    @Autowired
    protected DataManager dataManager;

    public EditViewAction() {
        this(ID);
    }

    public EditViewAction(String id) {
        super(id);
    }

    public void setDataSetsTable(Table<DataSet> dataSetsTable) {
        this.dataSetsTable = dataSetsTable;
    }

    public void setBandsDc(CollectionContainer<BandDefinition> bandsDc) {
        this.bandsDc = bandsDc;
    }

    @Override
    public void actionPerform(Component component) {
        if (dataSetsTable.getSingleSelected() != null) {
            final DataSet dataSet = dataSetsTable.getSingleSelected();
            if (DataSetType.SINGLE == dataSet.getType() || DataSetType.MULTI == dataSet.getType()) {
                MetaClass forEntityTreeModelMetaClass = findMetaClassByAlias(dataSet);
                if (forEntityTreeModelMetaClass != null) {

                    final EntityTree entityTree = reportWizardService.buildEntityTree(forEntityTreeModelMetaClass);
                    ReportRegion reportRegion = dataSetToReportRegion(dataSet, entityTree);

                    if (reportRegion != null) {
                        if (reportRegion.getRegionPropertiesRootNode() == null) {
                            notifications.create(Notifications.NotificationType.TRAY)
                                    .withCaption(messages.getMessage(getClass(), "dataSet.entityAliasInvalid"))
                                    .withDescription(getNameForEntityParameter(dataSet))
                                    .show();
                            //without that root node region editor form will not initialized correctly and became empty. just return
                            return;
                        } else {
                            //Open editor and convert saved in editor ReportRegion item to View
                            Map<String, Object> editorParams = new HashMap<>();
                            editorParams.put("asViewEditor", Boolean.TRUE);
                            editorParams.put("rootEntity", reportRegion.getRegionPropertiesRootNode());
                            editorParams.put("scalarOnly", Boolean.TRUE);
                            editorParams.put("updateDisabled", !secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore));

                            reportRegion.setReportData(dataManager.create(ReportData.class));
                            reportRegion.setBandNameFromReport(dataSet.getName());

                            RegionEditor screen = screenBuilders.editor(ReportRegion.class, dataSetsTable.getFrame().getFrameOwner())
                                    .editEntity(reportRegion)
                                    .withScreenClass(RegionEditor.class)
                                    .withOpenMode(OpenMode.DIALOG)
                                    .build();
                            screen.setRootNode(reportRegion.getRegionPropertiesRootNode());
                            screen.addAfterCloseListener(afterCloseEvent -> {
                                if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                                    RegionEditor editor = (RegionEditor) afterCloseEvent.getScreen();
                                    reportRegion.setRegionProperties(editor.getEditedEntity().getRegionProperties());
                                    dataSet.setFetchPlan(reportRegionToView(entityTree, reportRegion));
                                }
                            });
                            screen.show();
                        }
                    }
                }
            }
        }
    }

    //Detect metaclass by an alias and parameter
    protected MetaClass findMetaClassByAlias(DataSet dataSet) {
        String dataSetAlias = getNameForEntityParameter(dataSet);
        if (dataSetAlias == null) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage(getClass(), "dataSet.entityAliasNull"))
                    .show();
            return null;
        }
        MetaClass byAliasMetaClass = reports.findMetaClassByDataSetEntityAlias(dataSetAlias, dataSet.getType(),
                bandsDc.getItem().getReport().getInputParameters());

        //Lets return some value
        if (byAliasMetaClass == null) {
            //Can`t determine parameter and its metaClass by alias
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.formatMessage(getClass(), "dataSet.entityAliasInvalid", dataSetAlias))
                    .show();
            return null;
            //when byAliasMetaClass is null we return also null
        } else {
            //Detect metaclass by current view for comparison
            MetaClass viewMetaClass = null;
            if (dataSet.getFetchPlan() != null) {
                viewMetaClass = metadata.getClass(dataSet.getFetchPlan().getEntityClass());
            }
            if (viewMetaClass != null && !byAliasMetaClass.getName().equals(viewMetaClass.getName())) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.formatMessage(getClass(), "dataSet.entityWasChanged", byAliasMetaClass.getName()))
                        .show();
            }
            return byAliasMetaClass;
        }
    }

    protected ReportRegion dataSetToReportRegion(DataSet dataSet, EntityTree entityTree) {
        boolean isTabulatedRegion;
        FetchPlan view = null;
        String collectionPropertyName;
        switch (dataSet.getType()) {
            case SINGLE:
                isTabulatedRegion = false;
                view = dataSet.getFetchPlan();
                collectionPropertyName = null;
                break;
            case MULTI:
                isTabulatedRegion = true;
                collectionPropertyName = StringUtils.substringAfter(dataSet.getListEntitiesParamName(), "#");
                if (StringUtils.isBlank(collectionPropertyName) && dataSet.getListEntitiesParamName().contains("#")) {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messages.formatMessage(getClass(), "dataSet.entityAliasInvalid", getNameForEntityParameter(dataSet)))
                            .show();
                    return null;
                }
                if (StringUtils.isNotBlank(collectionPropertyName)) {

                    if (dataSet.getFetchPlan() != null) {
                        view = findSubViewByCollectionPropertyName(dataSet.getFetchPlan(), collectionPropertyName);

                    }
                    if (view == null) {
                        //View was never created for current dataset.
                        //We must to create minimal view that contains collection property for ability of creating ReportRegion.regionPropertiesRootNode later
                        MetaClass metaClass = entityTree.getEntityTreeRootNode().getWrappedMetaClass();
                        MetaProperty metaProperty = metaClass.getProperty(collectionPropertyName);
                        if (metaProperty.getDomain() != null && metaProperty.getRange().getCardinality().isMany()) {
                            view = fetchPlans.builder(metaProperty.getDomain().getJavaClass()).build();
                        } else {
                            notifications.create(Notifications.NotificationType.TRAY)
                                    .withCaption(messages.formatMessage(getClass(), "dataSet.cantFindCollectionProperty",
                                            collectionPropertyName, metaClass.getName()))
                                    .show();
                            return null;
                        }
                    }
                } else {
                    view = dataSet.getFetchPlan();
                }
                break;
            default:
                return null;
        }
        return reportWizardService.createReportRegionByView(entityTree, isTabulatedRegion, view, collectionPropertyName);
    }

    protected FetchPlan reportRegionToView(EntityTree entityTree, ReportRegion reportRegion) {
        return reportWizardService.createViewByReportRegions(entityTree.getEntityTreeRootNode(), Collections.singletonList(reportRegion));
    }

    public FetchPlan findSubViewByCollectionPropertyName(FetchPlan view, final String propertyName) {
        if (view == null) {
            return null;
        }
        for (FetchPlanProperty viewProperty : view.getProperties()) {
            if (propertyName.equals(viewProperty.getName())) {
                if (viewProperty.getFetchMode() != null) {
                    return viewProperty.getFetchPlan();
                }
            }

            if (viewProperty.getFetchMode() != null) {
                FetchPlan foundedView = findSubViewByCollectionPropertyName(viewProperty.getFetchPlan(), propertyName);
                if (foundedView != null) {
                    return foundedView;
                }
            }
        }
        return null;
    }

    protected String getNameForEntityParameter(DataSet dataSet) {
        String dataSetAlias = null;
        switch (dataSet.getType()) {
            case SINGLE:
                dataSetAlias = dataSet.getEntityParamName();
                break;
            case MULTI:
                dataSetAlias = dataSet.getListEntitiesParamName();
                break;
        }
        return dataSetAlias;
    }
}
