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

package io.jmix.reportsui.action.list;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.*;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.screen.report.wizard.ReportsWizard;
import io.jmix.reportsui.screen.report.wizard.region.RegionEditor;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.StandardOutcome;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionType(EditFetchPlanAction.ID)
public class EditFetchPlanAction extends ListAction {

    public static final String ID = "editFetchPlanEntity";

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected Messages messages;

    @Autowired
    protected ReportsWizard reportsWizard;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected DataManager dataManager;

    protected Table<DataSet> dataSetsTable;
    protected CollectionContainer<BandDefinition> bandsDc;

    public EditFetchPlanAction() {
        this(ID);
    }

    public EditFetchPlanAction(String id) {
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

                    final EntityTree entityTree = reportsWizard.buildEntityTree(forEntityTreeModelMetaClass);
                    ReportRegion reportRegion = dataSetToReportRegion(dataSet, entityTree);

                    if (reportRegion != null) {
                        if (reportRegion.getRegionPropertiesRootNode() == null) {
                            notifications.create(Notifications.NotificationType.TRAY)
                                    .withCaption(messages.getMessage(getClass(), "dataSet.entityAliasInvalid"))
                                    .withDescription(getNameForEntityParameter(dataSet))
                                    .show();
                            //without that root node region editor form will not initialized correctly and became empty. just return
                        } else {
                            //Open editor and convert saved in editor ReportRegion item to Fetch plan
                            reportRegion.setReportData(dataManager.create(ReportData.class));
                            reportRegion.setBandNameFromReport(dataSet.getName());

                            Map<String, Object> editorParams = new HashMap<>();
                            editorParams.put("rootEntity", reportRegion.getRegionPropertiesRootNode());
                            editorParams.put("scalarOnly", Boolean.TRUE);

                            RegionEditor screen = screenBuilders.editor(ReportRegion.class, dataSetsTable.getFrame().getFrameOwner())
                                    .editEntity(reportRegion)
                                    .withScreenClass(RegionEditor.class)
                                    .withOpenMode(OpenMode.DIALOG)
                                    .withOptions(new MapScreenOptions(editorParams))
                                    .build();
                            screen.setAsFetchPlanEditor(true);
                            screen.setUpdatePermission(secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore));

                            screen.addAfterCloseListener(afterCloseEvent -> {
                                if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                                    RegionEditor editor = (RegionEditor) afterCloseEvent.getSource();
                                    reportRegion.setRegionProperties(editor.getEditedEntity().getRegionProperties());
                                    dataSet.setFetchPlan(reportRegionToFetchPlan(entityTree, reportRegion));
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
    @Nullable
    protected MetaClass findMetaClassByAlias(DataSet dataSet) {
        String dataSetAlias = getNameForEntityParameter(dataSet);
        if (dataSetAlias == null) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage(getClass(), "dataSet.entityAliasNull"))
                    .show();
            return null;
        }
        MetaClass byAliasMetaClass = findMetaClassByDataSetEntityAlias(dataSetAlias, dataSet.getType(),
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
            //Detect metaclass by current fetch plan for comparison
            MetaClass fetchPlanMetaClass = null;
            if (dataSet.getFetchPlan() != null) {
                fetchPlanMetaClass = metadata.getClass(dataSet.getFetchPlan().getEntityClass());
            }
            if (fetchPlanMetaClass != null && !byAliasMetaClass.getName().equals(fetchPlanMetaClass.getName())) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.formatMessage(getClass(), "dataSet.entityWasChanged", byAliasMetaClass.getName()))
                        .show();
            }
            return byAliasMetaClass;
        }
    }

    @Nullable
    public MetaClass findMetaClassByDataSetEntityAlias(final String alias, final DataSetType dataSetType, final List<ReportInputParameter> reportInputParameters) {
        if (reportInputParameters.isEmpty() || StringUtils.isBlank(alias)) {
            return null;
        }

        String realAlias = DataSetType.MULTI == dataSetType ? StringUtils.substringBefore(alias, "#") : alias;
        boolean isCollectionAlias = !alias.equals(realAlias);

        ReportInputParameter reportInputParameter = reportInputParameters.stream()
                .filter(inputParameter -> realAlias.equals(inputParameter.getAlias()))
                .filter(inputParameter -> suitableByDataSetType(dataSetType, isCollectionAlias, inputParameter.getType()))
                .findFirst()
                .orElse(null);

        return reportInputParameter != null ? metadata.getClass(reportInputParameter) : null;
    }

    protected boolean suitableByDataSetType(DataSetType dataSetType, boolean isCollectionAlias, ParameterType type) {
        if (DataSetType.MULTI == dataSetType) {
            //find param that is matched for a MULTI dataset
            if (isCollectionAlias) {
                return ParameterType.ENTITY == type;
            } else {
                return ParameterType.ENTITY_LIST == type;
            }
        } else if (DataSetType.SINGLE == dataSetType) {
            //find param that is matched for a SINGLE dataset
            return ParameterType.ENTITY == type;
        }
        return false;
    }

    @Nullable
    protected ReportRegion dataSetToReportRegion(DataSet dataSet, EntityTree entityTree) {
        boolean isTabulatedRegion;
        FetchPlan fetchPlan = null;
        String collectionPropertyName;
        switch (dataSet.getType()) {
            case SINGLE:
                isTabulatedRegion = false;
                fetchPlan = dataSet.getFetchPlan();
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
                        fetchPlan = findSubFetchPlanByCollectionPropertyName(dataSet.getFetchPlan(), collectionPropertyName);

                    }
                    if (fetchPlan == null) {
                        //Fetch plan was never created for current dataset.
                        //We must to create minimal fetch plan that contains collection property for ability of creating ReportRegion.regionPropertiesRootNode later
                        MetaClass metaClass = metadata.getClass(entityTree.getEntityTreeRootNode().getMetaClassName());
                        MetaProperty metaProperty = metaClass.getProperty(collectionPropertyName);
                        if (metaProperty.getDomain() != null && metaProperty.getRange().getCardinality().isMany()) {
                            fetchPlan = fetchPlans.builder(metaProperty.getDomain().getJavaClass()).build();
                        } else {
                            notifications.create(Notifications.NotificationType.TRAY)
                                    .withCaption(messages.formatMessage(getClass(), "dataSet.cantFindCollectionProperty",
                                            collectionPropertyName, metaClass.getName()))
                                    .show();
                            return null;
                        }
                    }
                } else {
                    fetchPlan = dataSet.getFetchPlan();
                }
                break;
            default:
                return null;
        }
        return reportsWizard.createReportRegionByFetchPlan(entityTree, isTabulatedRegion, fetchPlan, collectionPropertyName);
    }

    protected FetchPlan reportRegionToFetchPlan(EntityTree entityTree, ReportRegion reportRegion) {
        return reportsWizard.createFetchPlanByReportRegions(entityTree.getEntityTreeRootNode(), Collections.singletonList(reportRegion));
    }

    @Nullable
    public FetchPlan findSubFetchPlanByCollectionPropertyName(@Nullable FetchPlan fetchPlan, final String propertyName) {
        if (fetchPlan == null) {
            return null;
        }
        for (FetchPlanProperty fetchPlanProperty : fetchPlan.getProperties()) {
            if (propertyName.equals(fetchPlanProperty.getName())) {
                if (fetchPlanProperty.getFetchMode() != null) {
                    return fetchPlanProperty.getFetchPlan();
                }
            }

            if (fetchPlanProperty.getFetchMode() != null) {
                FetchPlan foundFetchPlan = findSubFetchPlanByCollectionPropertyName(fetchPlanProperty.getFetchPlan(), propertyName);
                if (foundFetchPlan != null) {
                    return foundFetchPlan;
                }
            }
        }
        return null;
    }

    @Nullable
    protected String getNameForEntityParameter(DataSet dataSet) {
        String dataSetAlias = null;
        switch (dataSet.getType()) {
            case SINGLE:
                dataSetAlias = dataSet.getEntityParamName();
                break;
            case MULTI:
                dataSetAlias = dataSet.getListEntitiesParamName();
                break;
            default:
                // no action
                break;
        }
        return dataSetAlias;
    }
}
