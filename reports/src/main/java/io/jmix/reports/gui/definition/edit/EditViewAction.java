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

package io.jmix.reports.gui.definition.edit;

import com.haulmont.cuba.core.global.View;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import io.jmix.ui.gui.OpenType;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditViewAction extends AbstractAction {
    protected BandDefinitionEditor bandDefinitionEditor;

    public EditViewAction(BandDefinitionEditor bandDefinitionEditor) {
        super("editView");
        this.bandDefinitionEditor = bandDefinitionEditor;
    }

    @Override
    public void actionPerform(Component component) {
        if (bandDefinitionEditor.dataSets.getSingleSelected() != null) {
            final DataSet dataSet = bandDefinitionEditor.dataSets.getSingleSelected();
            if (DataSetType.SINGLE == dataSet.getType() || DataSetType.MULTI == dataSet.getType()) {
                MetaClass forEntityTreeModelMetaClass = findMetaClassByAlias(dataSet);
                if (forEntityTreeModelMetaClass != null) {

                    final EntityTree entityTree = bandDefinitionEditor.reportWizardService.buildEntityTree(forEntityTreeModelMetaClass);
                    ReportRegion reportRegion = dataSetToReportRegion(dataSet, entityTree);

                    if (reportRegion != null) {
                        if (reportRegion.getRegionPropertiesRootNode() == null) {
                            bandDefinitionEditor.showNotification(
                                    bandDefinitionEditor.formatMessage("dataSet.entityAliasInvalid",
                                            getNameForEntityParameter(dataSet)), Frame.NotificationType.TRAY);
                            //without that root node region editor form will not initialized correctly and became empty. just return
                            return;
                        } else {
                            //Open editor and convert saved in editor ReportRegion item to View
                            Map<String, Object> editorParams = new HashMap<>();
                            editorParams.put("asViewEditor", Boolean.TRUE);
                            editorParams.put("rootEntity", reportRegion.getRegionPropertiesRootNode());
                            editorParams.put("scalarOnly", Boolean.TRUE);
                            editorParams.put("updateDisabled", !bandDefinitionEditor.isUpdatePermitted());

                            Window.Editor regionEditor =
                                    bandDefinitionEditor.openEditor("report$Report.regionEditor",
                                            reportRegion, OpenType.DIALOG, editorParams, bandDefinitionEditor.dataSetsDs);
                            regionEditor.addCloseListener(actionId -> {
                                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                    dataSet.setView(reportRegionToView(entityTree, (ReportRegion) regionEditor.getItem()));
                                }
                            });
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
            bandDefinitionEditor.showNotification(
                    bandDefinitionEditor.formatMessage("dataSet.entityAliasNull"), Frame.NotificationType.TRAY);
            return null;
        }
        MetaClass byAliasMetaClass = bandDefinitionEditor.reportService.findMetaClassByDataSetEntityAlias(dataSetAlias, dataSet.getType(),
                bandDefinitionEditor.bandDefinitionDs.getItem().getReport().getInputParameters());

        //Lets return some value
        if (byAliasMetaClass == null) {
            //Can`t determine parameter and its metaClass by alias
            bandDefinitionEditor.showNotification(
                    bandDefinitionEditor.formatMessage("dataSet.entityAliasInvalid", dataSetAlias), Frame.NotificationType.TRAY);
            return null;
            //when byAliasMetaClass is null we return also null
        } else {
            //Detect metaclass by current view for comparison
            MetaClass viewMetaClass = null;
            if (dataSet.getView() != null) {
                viewMetaClass = bandDefinitionEditor.metadata.getClass(dataSet.getView().getEntityClass());
            }
            if (viewMetaClass != null && !byAliasMetaClass.getName().equals(viewMetaClass.getName())) {
                bandDefinitionEditor.showNotification(
                        bandDefinitionEditor.formatMessage("dataSet.entityWasChanged",
                                byAliasMetaClass.getName()), Frame.NotificationType.TRAY);
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
                view = dataSet.getView();
                collectionPropertyName = null;
                break;
            case MULTI:
                isTabulatedRegion = true;
                collectionPropertyName = StringUtils.substringAfter(dataSet.getListEntitiesParamName(), "#");
                if (StringUtils.isBlank(collectionPropertyName) && dataSet.getListEntitiesParamName().contains("#")) {
                    bandDefinitionEditor.showNotification(
                            bandDefinitionEditor.formatMessage("dataSet.entityAliasInvalid",
                                    getNameForEntityParameter(dataSet)), Frame.NotificationType.TRAY);
                    return null;
                }
                if (StringUtils.isNotBlank(collectionPropertyName)) {

                    if (dataSet.getView() != null) {
                        view = findSubViewByCollectionPropertyName(dataSet.getView(), collectionPropertyName);

                    }
                    if (view == null) {
                        //View was never created for current dataset.
                        //We must to create minimal view that contains collection property for ability of creating ReportRegion.regionPropertiesRootNode later
                        MetaClass metaClass = entityTree.getEntityTreeRootNode().getWrappedMetaClass();
                        MetaProperty metaProperty = metaClass.getProperty(collectionPropertyName);
                        if (metaProperty != null && metaProperty.getDomain() != null && metaProperty.getRange().getCardinality().isMany()) {
                            view = new View(metaProperty.getDomain().getJavaClass());
                        } else {
                            bandDefinitionEditor.showNotification(
                                    bandDefinitionEditor.formatMessage("dataSet.cantFindCollectionProperty",
                                            collectionPropertyName, metaClass.getName()), Frame.NotificationType.TRAY);
                            return null;
                        }
                    }
                } else {
                    view = dataSet.getView();
                }
                break;
            default:
                return null;
        }
        return bandDefinitionEditor.reportWizardService.createReportRegionByView(entityTree, isTabulatedRegion,
                view, collectionPropertyName);
    }

    protected View reportRegionToView(EntityTree entityTree, ReportRegion reportRegion) {
        return bandDefinitionEditor.reportWizardService.createViewByReportRegions(entityTree.getEntityTreeRootNode(), Collections.singletonList(reportRegion));
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
