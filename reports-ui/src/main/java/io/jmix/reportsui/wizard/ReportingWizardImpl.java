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

package io.jmix.reportsui.wizard;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.Reports;
import io.jmix.reports.ReportsImpl;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.*;
import io.jmix.reports.entity.wizard.*;
import io.jmix.reports.util.DataSetFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.Temporal;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

@Component("report_ReportingWizardApi")
public class ReportingWizardImpl implements ReportingWizard {

    public static final String ROOT_BAND_DEFINITION_NAME = "Root";
    protected static final String DEFAULT_SINGLE_ENTITY_ALIAS = "entity";//cause Thesis used it for running reports from screens without selection input params
    protected static final String DEFAULT_LIST_OF_ENTITIES_ALIAS = "entities";//cause Thesis will use it for running reports from screens without selection input params

    private static final Logger log = LoggerFactory.getLogger(ReportsImpl.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Reports reports;
    @Autowired
    protected ReportsProperties reportsProperties;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected DataSetFactory dataSetFactory;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected FetchPlans fetchPlans;

    @Override
    public Report toReport(ReportData reportData, boolean temporary) {
        Report report = createReport(reportData, temporary);
        ReportInputParameter mainParameter = createParameters(reportData, report);
        BandDefinition rootReportBandDefinition = createRootBand(report);
        Set<BandDefinition> bands = createBands(report, rootReportBandDefinition, reportData, mainParameter);
        ReportTemplate defaultTemplate = createDefaultTemplate(report, reportData);
        report.setDefaultTemplate(defaultTemplate);

        HashSet<BandDefinition> childrenBandsDefinitionForRoot = new HashSet<>(bands);
        childrenBandsDefinitionForRoot.remove(rootReportBandDefinition);
        rootReportBandDefinition.getChildrenBandDefinitions().addAll(childrenBandsDefinitionForRoot);
        report.setName(reports.generateReportName(reportData.getName()));
        String xml = reports.convertToString(report);
        report.setXml(xml);

        if (!temporary) {
            report = reports.storeReportEntity(report);
        }

        return report;
    }

    protected Set<BandDefinition> createBands(Report report, BandDefinition rootReportBandDefinition,
                                              ReportData reportData, ReportInputParameter mainParameter) {
        int bandDefinitionPosition = 0;
        for (ReportRegion reportRegion : reportData.getReportRegions()) {
            if (reportRegion.isTabulatedRegion() &&
                    (reportData.getOutputFileType() == ReportOutputType.XLSX || TemplateFileType.XLSX.equals(reportData.getTemplateFileType()))) {
                BandDefinition headerBand = createHeaderBand(report, rootReportBandDefinition, bandDefinitionPosition++, reportRegion);
                report.getBands().add(headerBand);
            }

            createDefaultFormats(report, reportData, reportRegion);

            BandDefinition dataBand = createDataBand(report, rootReportBandDefinition, reportRegion.getNameForBand(), bandDefinitionPosition++);

            if (reportData.getReportType().isEntity()) {
                FetchPlan parameterView = createViewByReportRegions(reportData.getEntityTreeRootNode(), reportData.getReportRegions());
                createEntityDataSet(reportData, reportRegion, dataBand, mainParameter, parameterView);
            } else {
                createJpqlDataSet(reportData, reportRegion, dataBand);
            }

            report.getBands().add(dataBand);
        }

        return report.getBands();
    }

    @Nullable
    protected ReportInputParameter createParameters(ReportData reportData, Report report) {
        ReportInputParameter mainParameter = null;
        if (reportData.getReportType().isEntity()) {
            mainParameter = createMainInputParameter(report, reportData);
            report.getInputParameters().add(mainParameter);
        } else if (reportData.getQueryParameters() != null) {
            int i = 1;
            for (ReportData.Parameter queryParameter : reportData.getQueryParameters()) {
                ReportInputParameter parameter = createParameter(report, i++);
                parameter.setAlias(queryParameter.name);
                parameter.setName(StringUtils.capitalize(queryParameter.name));
                parameter.setType(queryParameter.parameterType);
                parameter.setParameterClass(queryParameter.javaClass);
                parameter.setDefaultValue(queryParameter.defaultValue);
                parameter.setPredefinedTransformation(queryParameter.predefinedTransformation);
                parameter.setHidden(queryParameter.hidden);

                if (queryParameter.parameterType == ParameterType.ENTITY
                        || queryParameter.parameterType == ParameterType.ENTITY_LIST) {
                    MetaClass metaClass = metadata.getClass(queryParameter.javaClass);
                    if (metaClass != null) {
                        parameter.setEntityMetaClass(metaClass.getName());
                    }
                } else if (queryParameter.parameterType == ParameterType.ENUMERATION && queryParameter.javaClass != null) {
                    parameter.setEnumerationClass(queryParameter.javaClass.getName());
                }

                report.getInputParameters().add(parameter);
            }
        }

        return mainParameter;
    }

    protected Report createReport(ReportData reportData, boolean isTmp) {
        Report report = metadata.create(Report.class);
        report.setIsTmp(isTmp);
        report.setReportType(ReportType.SIMPLE);
        report.setGroup(reportData.getGroup());
        report.setBands(new LinkedHashSet<>(reportData.getReportRegions().size() + 1)); //plus rootBand);
        report.setValuesFormats(new ArrayList<>());
        return report;
    }

    protected void createJpqlDataSet(ReportData reportData, ReportRegion reportRegion, BandDefinition dataBand) {
        DataSet dataSet = dataSetFactory.createEmptyDataSet(dataBand);
        dataSet.setName(messages.getMessage(getClass(), "dataSet"));
        dataSet.setType(DataSetType.JPQL);

        String query = new JpqlQueryBuilder(reportData, reportRegion).buildQuery();
        dataSet.setText(query);
        dataSet.setDataStore(reportData.getDataStore());
        dataBand.getDataSets().add(dataSet);
    }

    protected void createEntityDataSet(ReportData reportData, ReportRegion reportRegion, BandDefinition dataBand,
                                       ReportInputParameter mainParameter, FetchPlan parameterView) {
        DataSet dataSet = dataSetFactory.createEmptyDataSet(dataBand);
        dataSet.setName(messages.getMessage(getClass(), "dataSet"));
        if (ReportData.ReportType.LIST_OF_ENTITIES == reportData.getReportType()) {
            dataSet.setType(DataSetType.MULTI);
            dataSet.setListEntitiesParamName(mainParameter.getAlias());
            dataSet.setFetchPlan(parameterView);
        } else if (ReportData.ReportType.SINGLE_ENTITY == reportData.getReportType()) {
            if (reportRegion.isTabulatedRegion()) {
                dataSet.setType(DataSetType.MULTI);
                dataSet.setListEntitiesParamName(mainParameter.getAlias() + "#" + reportRegion.getRegionPropertiesRootNode().getName());
            } else {
                dataSet.setType(DataSetType.SINGLE);
                dataSet.setEntityParamName(mainParameter.getAlias());
            }
            dataSet.setFetchPlan(parameterView);
        }
        dataBand.getDataSets().add(dataSet);
    }

    protected BandDefinition createRootBand(Report report) {
        BandDefinition rootReportBandDefinition = metadata.create(BandDefinition.class);
        rootReportBandDefinition.setPosition(0);
        rootReportBandDefinition.setName(ROOT_BAND_DEFINITION_NAME);
        rootReportBandDefinition.setReport(report);
        report.getBands().add(rootReportBandDefinition);
        return rootReportBandDefinition;
    }

    protected ReportInputParameter createMainInputParameter(Report report, ReportData reportData) {
        ReportInputParameter reportInputParameter = createParameter(report, 1);

        reportInputParameter.setName(reportData.getEntityTreeRootNode().getLocalizedName());
        MetaClass wrapperMetaClass = reportData.getEntityTreeRootNode().getWrappedMetaClass();

        reportInputParameter.setEntityMetaClass(wrapperMetaClass.getName());
        if (ReportData.ReportType.LIST_OF_ENTITIES == reportData.getReportType()) {
            reportInputParameter.setType(ParameterType.ENTITY_LIST);
            reportInputParameter.setAlias(DEFAULT_LIST_OF_ENTITIES_ALIAS);
        } else {
            reportInputParameter.setType(ParameterType.ENTITY);
            reportInputParameter.setAlias(DEFAULT_SINGLE_ENTITY_ALIAS);
        }

        return reportInputParameter;
    }

    protected ReportInputParameter createParameter(Report report, int position) {
        ReportInputParameter reportInputParameter = metadata.create(ReportInputParameter.class);
        reportInputParameter.setReport(report);
        reportInputParameter.setRequired(Boolean.TRUE);
        reportInputParameter.setPosition(position);
        return reportInputParameter;
    }

    protected ReportTemplate createDefaultTemplate(Report report, ReportData reportData) {
        ReportTemplate reportTemplate = metadata.create(ReportTemplate.class);
        reportTemplate.setReport(report);
        reportTemplate.setCode(Reports.DEFAULT_TEMPLATE_CODE);

        reportTemplate.setName(reportData.getTemplateFileName());
        reportTemplate.setContent(reportData.getTemplateContent());
        reportTemplate.setCustom(Boolean.FALSE);
        Integer outputFileTypeId = reportData.getOutputFileType().getId();
        reportTemplate.setReportOutputType(ReportOutputType.fromId(outputFileTypeId));
        if (StringUtils.isNotEmpty(reportData.getOutputNamePattern())) {
            reportTemplate.setOutputNamePattern(reportData.getOutputNamePattern());
        }
        report.setDefaultTemplate(reportTemplate);
        report.setTemplates(Collections.singletonList(reportTemplate));

        return reportTemplate;
    }

    protected BandDefinition createDataBand(Report report, BandDefinition rootBandDefinition, String name, int bandDefPos) {
        BandDefinition bandDefinition = metadata.create(BandDefinition.class);
        bandDefinition.setParentBandDefinition(rootBandDefinition);
        bandDefinition.setOrientation(Orientation.HORIZONTAL);
        bandDefinition.setName(name);
        bandDefinition.setPosition(bandDefPos);
        bandDefinition.setReport(report);
        return bandDefinition;
    }

    protected void createDefaultFormats(Report report, ReportData reportData, ReportRegion reportRegion) {
        ArrayList<ReportValueFormat> formats = new ArrayList<>();
        if (!reportData.getTemplateFileName().endsWith(".html")) {
            for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                if (regionProperty.getEntityTreeNode().getWrappedMetaProperty().getJavaType().isAssignableFrom(Date.class)) {
                    ReportValueFormat rvf = new ReportValueFormat();
                    rvf.setReport(report);
                    rvf.setValueName(reportRegion.getNameForBand() + "." + regionProperty.getHierarchicalNameExceptRoot());
                    rvf.setFormatString(messages.getMessage("dateTimeFormat"));
                    AnnotatedElement annotatedElement = regionProperty.getEntityTreeNode().getWrappedMetaProperty().getAnnotatedElement();
                    if (annotatedElement != null && annotatedElement.isAnnotationPresent(Temporal.class)) {
                        switch (annotatedElement.getAnnotation(Temporal.class).value()) {
                            case TIME:
                                rvf.setFormatString(messages.getMessage("timeFormat"));
                                break;
                            case DATE:
                                rvf.setFormatString(messages.getMessage("dateFormat"));
                                break;
                        }
                    }
                    formats.add(rvf);
                }
            }
        }

        report.getValuesFormats().addAll(formats);
    }

    protected BandDefinition createHeaderBand(Report report,
                                              BandDefinition rootReportBandDefinition,
                                              int bandDefPos, ReportRegion reportRegion) {
        BandDefinition headerBandDefinition = metadata.create(BandDefinition.class);
        headerBandDefinition.setParentBandDefinition(rootReportBandDefinition);
        headerBandDefinition.setOrientation(Orientation.HORIZONTAL);
        headerBandDefinition.setName(reportRegion.getNameForHeaderBand());
        headerBandDefinition.setPosition(bandDefPos);
        headerBandDefinition.setReport(report);

        return headerBandDefinition;
    }

    @Override
    public FetchPlan createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions) {
        MetaClass rootWrapperMetaClass = entityTreeRootNode.getWrappedMetaClass();
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(rootWrapperMetaClass.getJavaClass());

        Map<EntityTreeNode, FetchPlanBuilder> viewsForNodes = new HashMap<>();
        viewsForNodes.put(entityTreeRootNode, fetchPlanBuilder);
        for (ReportRegion reportRegion : reportRegions) {
            for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                EntityTreeNode entityTreeNode = regionProperty.getEntityTreeNode();
                MetaClass metaClass = entityTreeNode.getWrappedMetaClass();
                if (metaClass != null) {
                    FetchPlanBuilder propertyFetchPlanBuilder = viewsForNodes.get(entityTreeNode);
                    if (propertyFetchPlanBuilder == null) {
                        propertyFetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());
                        viewsForNodes.put(entityTreeNode, propertyFetchPlanBuilder);
                    }

                    FetchPlanBuilder parentView = ensureParentViewsExist(entityTreeNode, viewsForNodes);
                    parentView.add(regionProperty.getName(), propertyFetchPlanBuilder);
                } else {
                    FetchPlanBuilder parentView = ensureParentViewsExist(entityTreeNode, viewsForNodes);
                    parentView.add(regionProperty.getName());
                }
            }
        }

        return fetchPlanBuilder.build();
    }

    /**
     * Create report region using view and whole entity model as entityTree param
     * For creating tabulated report region for collection of entity (when used # in alias of dataset) view and
     * parameters must to be non-nul values because otherwise necessary ReportRegion.regionPropertiesRootNode field value
     * will be null. That value is determined by that view.
     *
     * @param entityTree             the whole entity tree model
     * @param isTabulated            determine which region will be created
     * @param fetchPlan              by that view region will be created
     * @param collectionPropertyName must to be non-null for a tabulated region
     * @return report region
     */
    @Override
    public ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable FetchPlan fetchPlan, @Nullable String collectionPropertyName) {
        if (StringUtils.isNotBlank(collectionPropertyName) && fetchPlan == null) {
            //without view we can`t correctly set rootNode for region which is necessary for tabulated regions for a
            // collection of entities (when alias contain #)
            log.warn("Detected incorrect parameters for createReportRegionByView method. View must not to be null if " +
                    "collection collectionPropertyName is not null (" + collectionPropertyName + ")");
        }
        ReportRegion reportRegion = metadata.create(ReportRegion.class);

        EntityTreeNode entityTreeRootNode = entityTree.getEntityTreeRootNode();

        Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap = new HashMap<>();
        nodesToMap(entityTreeRootNode, allNodesAndHierarchicalPathsMap);
        boolean scalarOnly;//code below became less readable if we will use isTabulated parameter instead of that 'scalarOnly' variable
        if (isTabulated) {
            reportRegion.setIsTabulatedRegion(Boolean.TRUE);
            reportRegion.setRegionPropertiesRootNode(allNodesAndHierarchicalPathsMap.get(collectionPropertyName));
            scalarOnly = false;
        } else {
            reportRegion.setIsTabulatedRegion(Boolean.FALSE);
            reportRegion.setRegionPropertiesRootNode(entityTreeRootNode);
            scalarOnly = true;
        }
        if (fetchPlan != null) {
            iterateViewAndCreatePropertiesForRegion(scalarOnly, fetchPlan, allNodesAndHierarchicalPathsMap, reportRegion.getRegionProperties(), collectionPropertyName, 0);
        }
        return reportRegion;
    }

    /**
     * Search for view for parent node
     * If does not exists - createDataSet it and add property to parent of parent view
     */
    protected FetchPlanBuilder ensureParentViewsExist(EntityTreeNode entityTreeNode, Map<EntityTreeNode, FetchPlanBuilder> viewsForNodes) {
        EntityTreeNode parentNode = entityTreeNode.getParent();
        MetaClass wrapperMetaClass = parentNode.getWrappedMetaClass();

        FetchPlanBuilder parentFetchPlanBuilder = fetchPlans.builder(wrapperMetaClass.getJavaClass());

        if (parentFetchPlanBuilder == null && parentNode != null) {
            parentFetchPlanBuilder = fetchPlans.builder(wrapperMetaClass.getJavaClass());
            viewsForNodes.put(parentNode, parentFetchPlanBuilder);
            FetchPlanBuilder parentOfParentView = ensureParentViewsExist(parentNode, viewsForNodes);
            if (parentOfParentView != null) {
                parentOfParentView.add(parentNode.getName(), parentFetchPlanBuilder);
            }
        }

        return parentFetchPlanBuilder;
    }


    protected void iterateViewAndCreatePropertiesForRegion(final boolean scalarOnly, final FetchPlan parentView, final Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap, final List<RegionProperty> regionProperties, String pathFromParentView, long propertyOrderNum) {
        if (pathFromParentView == null) {
            pathFromParentView = "";
        }
        for (FetchPlanProperty viewProperty : parentView.getProperties()) {

            if (scalarOnly) {
                MetaClass metaClass = metadata.getClass(parentView.getEntityClass());
                MetaProperty metaProperty = metaClass.getProperty(viewProperty.getName());
                if (metaProperty != null && metaProperty.getRange().getCardinality().isMany()) {
                    continue;
                }
            }

            if (viewProperty.getFetchPlan() != null) {
                iterateViewAndCreatePropertiesForRegion(scalarOnly, viewProperty.getFetchPlan(), allNodesAndHierarchicalPathsMap, regionProperties, pathFromParentView + "." + viewProperty.getName(), propertyOrderNum);
            } else {
                EntityTreeNode entityTreeNode = allNodesAndHierarchicalPathsMap.get(StringUtils.removeStart(pathFromParentView + "." + viewProperty.getName(), "."));

                if (entityTreeNode != null) {
                    RegionProperty regionProperty = metadata.create(RegionProperty.class);
                    regionProperty.setOrderNum(++propertyOrderNum);
                    regionProperty.setEntityTreeNode(entityTreeNode);
                    regionProperties.add(regionProperty);
                }
            }
        }
    }

    protected void nodesToMap(EntityTreeNode node, final Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap) {
        if (!node.getChildren().isEmpty()) {
            allNodesAndHierarchicalPathsMap.put(node.getHierarchicalNameExceptRoot(), node);
            for (EntityTreeNode entityTreeNode : node.getChildren()) {
                nodesToMap(entityTreeNode, allNodesAndHierarchicalPathsMap);
            }
        } else {
            allNodesAndHierarchicalPathsMap.put(node.getHierarchicalNameExceptRoot(), node);
        }
    }

    @Override
    public boolean isEntityAllowedForReportWizard(final MetaClass effectiveMetaClass) {
        if (metadataTools.isSystemLevel(effectiveMetaClass)
                || metadataTools.isEmbeddable(effectiveMetaClass)
                || effectiveMetaClass.getProperties().isEmpty()) {
            return false;
        }
        List<String> whiteListedEntities = getWizardWhiteListedEntities();
        if (!whiteListedEntities.isEmpty()) {
            //use white list cause it has more meaningful priority
            if (!whiteListedEntities.contains(effectiveMetaClass.getName())) {
                return false;
            }
        } else {
            //otherwise filter by a blacklist
            if (getWizardBlackListedEntities().contains(effectiveMetaClass.getName())) {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        Collection<Object> propertiesNamesList = CollectionUtils.collect(effectiveMetaClass.getProperties(), new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((MetaProperty) input).getName();
            }
        });

        propertiesNamesList.removeAll(CollectionUtils.collect(getWizardBlackListedProperties(), new Transformer() {
            @Override
            public Object transform(Object input) {
                if (effectiveMetaClass.getName().equals(StringUtils.substringBefore((String) input, "."))) {
                    return StringUtils.substringAfter((String) input, ".");
                }
                return null;
            }
        }));
        return !propertiesNamesList.isEmpty();
    }

    @Override
    public boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty) {
        //here we can`t just to determine metaclass using property argument cause it can be an ancestor of it
        List<String> propertiesBlackList = reportsProperties.getWizardPropertiesBlackList();
        List<String> wizardPropertiesExcludedBlackList = reportsProperties.getWizardPropertiesExcludedBlackList();

        MetaClass originalMetaClass = getOriginalMetaClass(metaClass);
        MetaClass originalDomainMetaClass = getOriginalMetaClass(metaProperty.getDomain());
        String classAndPropertyName = originalMetaClass.getName() + "." + metaProperty.getName();
        return !(propertiesBlackList.contains(classAndPropertyName)
                || (propertiesBlackList.contains(originalDomainMetaClass.getName() + "." + metaProperty.getName())
                && !wizardPropertiesExcludedBlackList.contains(classAndPropertyName)));
    }

    protected List<String> getWizardBlackListedEntities() {
        List<String> entitiesBlackList = reportsProperties.getWizardEntitiesBlackList();
        return getEffectiveEntities(entitiesBlackList);
    }

    protected List<String> getWizardWhiteListedEntities() {
        List<String> entitiesWhiteList = reportsProperties.getWizardEntitiesWhiteList();
        return getEffectiveEntities(entitiesWhiteList);
    }

    protected List<String> getEffectiveEntities(List<String> entitiesList) {
        List<String> effectiveEntities = new ArrayList<>();
        for (String className : entitiesList) {
            MetaClass clazz = metadata.getClass(className);
            effectiveEntities.add(extendedEntities.getEffectiveMetaClass(clazz).getName());
        }
        return effectiveEntities;
    }

    protected List<String> getWizardBlackListedProperties() {
        return reportsProperties.getWizardPropertiesBlackList();
    }

    protected List<String> getWizardPropertiesExcludedBlackList() {
        return reportsProperties.getWizardPropertiesExcludedBlackList();
    }

    protected MetaClass getOriginalMetaClass(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass == null) {
            originalMetaClass = metaClass;
        }
        return originalMetaClass;
    }
}