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

package io.jmix.reportsui.screen.report.wizard;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.*;
import io.jmix.reports.entity.wizard.*;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.reportsui.screen.report.wizard.query.JpqlQueryBuilder;
import io.jmix.reportsui.screen.report.wizard.template.TemplateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.persistence.Temporal;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * API for report wizard
 */
@Component("report_ReportsWizard")
public class ReportsWizard {

    public static final String ROOT_BAND_DEFINITION_NAME = "Root";
    protected static final String DEFAULT_SINGLE_ENTITY_NAME = "Entity";
    protected static final String DEFAULT_LIST_OF_ENTITIES_NAME = "Entities";
    protected static final String DEFAULT_SINGLE_ENTITY_ALIAS = "entity";//cause Thesis used it for running reports from screens without selection input params
    protected static final String DEFAULT_LIST_OF_ENTITIES_ALIAS = "entities";//cause Thesis will use it for running reports from screens without selection input params

    private static final Logger log = LoggerFactory.getLogger(ReportsWizard.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ReportsPersistence reportsPersistence;

    @Autowired
    protected ReportsUtils reportsUtils;

    @Autowired
    protected ReportsSerialization reportsSerialization;

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
    protected TemplateGenerator templateGenerator;

    @Autowired
    protected Provider<EntityTreeModelBuilder> entityTreeModelBuilderApiProvider;

    @Autowired
    protected FetchPlans fetchPlans;

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
        report.setName(reportsUtils.generateReportName(reportData.getName()));
        String xml = reportsSerialization.convertToString(report);
        report.setXml(xml);

        if (!temporary) {
            report = reportsPersistence.save(report);
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

            if (reportData.getReportTypeGenerate().isEntity()) {
                FetchPlan parameterFetchPlan = createFetchPlanByReportRegions(reportData.getEntityTreeRootNode(), reportData.getReportRegions());
                createEntityDataSet(reportData, reportRegion, dataBand, mainParameter, parameterFetchPlan);
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
        if (reportData.getReportTypeGenerate().isEntity()) {
            mainParameter = createMainInputParameter(report, reportData);
            report.getInputParameters().add(mainParameter);
        } else if (reportData.getQueryParameters() != null) {
            int i = 1;
            for (QueryParameter queryParameter : reportData.getQueryParameters()) {
                ReportInputParameter parameter = createParameter(report, i++);
                parameter.setAlias(queryParameter.getName());
                parameter.setName(StringUtils.capitalize(queryParameter.getName()));
                parameter.setType(queryParameter.getParameterType());
                parameter.setParameterClassName(queryParameter.getJavaClassName());
                parameter.setDefaultValue(queryParameter.getDefaultValueString());
                parameter.setPredefinedTransformation(queryParameter.getPredefinedTransformation());
                parameter.setHidden(queryParameter.getHidden());

                if (queryParameter.getParameterType() == ParameterType.ENTITY
                        || queryParameter.getParameterType() == ParameterType.ENTITY_LIST) {
                    MetaClass metaClass = metadata.findClass(queryParameter.getEntityMetaClassName());
                    if (metaClass != null) {
                        parameter.setEntityMetaClass(metaClass.getName());
                    }
                } else if (queryParameter.getParameterType() == ParameterType.ENUMERATION && queryParameter.getJavaClassName() != null) {
                    parameter.setEnumerationClass(queryParameter.getJavaClassName());
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
        dataSet.setName(messages.getMessage("dataSet"));
        dataSet.setType(DataSetType.JPQL);

        String query = new JpqlQueryBuilder(reportData, reportRegion).buildFinalQuery();
        dataSet.setText(query);
        dataSet.setDataStore(reportData.getDataStore());
        dataBand.getDataSets().add(dataSet);
    }

    protected void createEntityDataSet(ReportData reportData, ReportRegion reportRegion, BandDefinition dataBand,
                                       ReportInputParameter mainParameter, FetchPlan parameterFetchPlan) {
        DataSet dataSet = dataSetFactory.createEmptyDataSet(dataBand);
        dataSet.setName(messages.getMessage("dataSet"));
        if (ReportTypeGenerate.LIST_OF_ENTITIES == reportData.getReportTypeGenerate()) {
            dataSet.setType(DataSetType.MULTI);
            dataSet.setListEntitiesParamName(mainParameter.getAlias());
            dataSet.setFetchPlan(parameterFetchPlan);
        } else if (ReportTypeGenerate.SINGLE_ENTITY == reportData.getReportTypeGenerate()) {
            if (reportRegion.isTabulatedRegion()) {
                dataSet.setType(DataSetType.MULTI);
                dataSet.setListEntitiesParamName(mainParameter.getAlias() + "#" + reportRegion.getRegionPropertiesRootNode().getName());
            } else {
                dataSet.setType(DataSetType.SINGLE);
                dataSet.setEntityParamName(mainParameter.getAlias());
            }
            dataSet.setFetchPlan(parameterFetchPlan);
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

        String metaClassName = reportData.getEntityTreeRootNode().getMetaClassName();

        reportInputParameter.setEntityMetaClass(metaClassName);
        if (ReportTypeGenerate.LIST_OF_ENTITIES == reportData.getReportTypeGenerate()) {
            reportInputParameter.setName(DEFAULT_LIST_OF_ENTITIES_NAME);
            reportInputParameter.setType(ParameterType.ENTITY_LIST);
            reportInputParameter.setAlias(DEFAULT_LIST_OF_ENTITIES_ALIAS);
        } else {
            reportInputParameter.setName(DEFAULT_SINGLE_ENTITY_NAME);
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
        reportTemplate.setCode(ReportTemplate.DEFAULT_TEMPLATE_CODE);

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
                EntityTreeNode entityTreeNode = regionProperty.getEntityTreeNode();
                MetaClass metaClass = metadata.getClass(entityTreeNode.getParentMetaClassName());
                MetaProperty metaProperty = metaClass.getProperty(entityTreeNode.getMetaPropertyName());

                if (metaProperty.getJavaType().isAssignableFrom(Date.class)) {
                    ReportValueFormat rvf = metadata.create(ReportValueFormat.class);
                    rvf.setReport(report);
                    rvf.setValueName(reportRegion.getNameForBand() + "." + regionProperty.getHierarchicalNameExceptRoot());
                    rvf.setFormatString(messages.getMessage("dateTimeFormat"));
                    AnnotatedElement annotatedElement = metaProperty.getAnnotatedElement();
                    if (annotatedElement != null && annotatedElement.isAnnotationPresent(Temporal.class)) {
                        switch (annotatedElement.getAnnotation(Temporal.class).value()) {
                            case TIME:
                                rvf.setFormatString(messages.getMessage("timeFormat"));
                                break;
                            case DATE:
                                rvf.setFormatString(messages.getMessage("dateFormat"));
                                break;
                            default:
                                // no action
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

    public FetchPlan createFetchPlanByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions) {
        MetaClass rootMetaClass = metadata.getClass(entityTreeRootNode.getMetaClassName());
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(rootMetaClass.getJavaClass());

        Map<EntityTreeNode, FetchPlanBuilder> fetchPlansForNodes = new HashMap<>();
        fetchPlansForNodes.put(entityTreeRootNode, fetchPlanBuilder);
        for (ReportRegion reportRegion : reportRegions) {
            for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                EntityTreeNode entityTreeNode = regionProperty.getEntityTreeNode();
                String metaClassName = entityTreeNode.getMetaClassName();

                if (metaClassName != null) {
                    MetaClass metaClass = metadata.getClass(metaClassName);
                    FetchPlanBuilder propertyFetchPlanBuilder = fetchPlansForNodes.get(entityTreeNode);
                    if (propertyFetchPlanBuilder == null) {
                        propertyFetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());
                        fetchPlansForNodes.put(entityTreeNode, propertyFetchPlanBuilder);
                    }

                    FetchPlanBuilder parentFetchPlan = ensureParentFetchPlansExist(entityTreeNode, fetchPlansForNodes);
                    parentFetchPlan.add(regionProperty.getName(), propertyFetchPlanBuilder);
                } else {
                    FetchPlanBuilder parentFetchPlan = ensureParentFetchPlansExist(entityTreeNode, fetchPlansForNodes);
                    parentFetchPlan.add(regionProperty.getName());
                }
            }
        }

        return fetchPlanBuilder.build();
    }

    /**
     * Create report region using fetch plan and whole entity model as entityTree param
     * For creating tabulated report region for collection of entity (when used # in alias of dataset) fetch plan and
     * parameters must to be non-nul values because otherwise necessary ReportRegion.regionPropertiesRootNode field value
     * will be null. That value is determined by that fetch plan.
     *
     * @param entityTree             the whole entity tree model
     * @param isTabulated            determine which region will be created
     * @param fetchPlan              by that fetch plan the region will be created
     * @param collectionPropertyName must to be non-null for a tabulated region
     * @return report region
     */
    public ReportRegion createReportRegionByFetchPlan(EntityTree entityTree, boolean isTabulated, @Nullable FetchPlan fetchPlan, @Nullable String collectionPropertyName) {
        if (StringUtils.isNotBlank(collectionPropertyName) && fetchPlan == null) {
            //without fetch plan we can`t correctly set rootNode for region which is necessary for tabulated regions for a
            // collection of entities (when alias contain #)
            log.warn("Detected incorrect parameters for createReportRegionByFetchPlan method. Fetch plan must not to be null if " +
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
            iterateFetchPlanAndCreatePropertiesForRegion(scalarOnly, fetchPlan, allNodesAndHierarchicalPathsMap, reportRegion.getRegionProperties(), collectionPropertyName, 0);
        }
        return reportRegion;
    }

    /**
     * Search for fetch plan for parent node
     * If does not exists - createDataSet it and add property to parent of parent fetch plan
     * @param entityTreeNode entity tree node
     * @param fetchPlansForNodes fetch plans for previous nodes
     * @return fetch plan builder
     */
    @Nullable
    protected FetchPlanBuilder ensureParentFetchPlansExist(EntityTreeNode entityTreeNode, Map<EntityTreeNode, FetchPlanBuilder> fetchPlansForNodes) {
        EntityTreeNode parentNode = entityTreeNode.getParent();
        FetchPlanBuilder parentFetchPlanBuilder = fetchPlansForNodes.get(parentNode);

        if (parentFetchPlanBuilder == null && parentNode != null) {
            MetaClass metaClass = metadata.getClass(parentNode.getMetaClassName());
            parentFetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());
            fetchPlansForNodes.put(parentNode, parentFetchPlanBuilder);
            FetchPlanBuilder parentOfParentFetchPlan = ensureParentFetchPlansExist(parentNode, fetchPlansForNodes);
            if (parentOfParentFetchPlan != null) {
                parentOfParentFetchPlan.add(parentNode.getName(), parentFetchPlanBuilder);
            }
        }

        return parentFetchPlanBuilder;
    }


    protected void iterateFetchPlanAndCreatePropertiesForRegion(final boolean scalarOnly, final FetchPlan parentFetchPlan,
                                                                final Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap,
                                                                final List<RegionProperty> regionProperties,
                                                                @Nullable String pathFromParentFetchPlan,
                                                                long propertyOrderNum) {
        if (pathFromParentFetchPlan == null) {
            pathFromParentFetchPlan = "";
        }
        for (FetchPlanProperty fetchPlanProperty : parentFetchPlan.getProperties()) {

            if (scalarOnly) {
                MetaClass metaClass = metadata.getClass(parentFetchPlan.getEntityClass());
                MetaProperty metaProperty = metaClass.findProperty(fetchPlanProperty.getName());
                if (metaProperty != null && metaProperty.getRange().getCardinality().isMany()) {
                    continue;
                }
            }

            if (fetchPlanProperty.getFetchPlan() != null) {
                iterateFetchPlanAndCreatePropertiesForRegion(scalarOnly, fetchPlanProperty.getFetchPlan(), allNodesAndHierarchicalPathsMap, regionProperties, pathFromParentFetchPlan + "." + fetchPlanProperty.getName(), propertyOrderNum);
            } else {
                EntityTreeNode entityTreeNode = allNodesAndHierarchicalPathsMap.get(StringUtils.removeStart(pathFromParentFetchPlan + "." + fetchPlanProperty.getName(), "."));

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

    public boolean isEntityAllowedForReportWizard(final MetaClass effectiveMetaClass) {
        if (metadataTools.isSystemLevel(effectiveMetaClass)
                || metadataTools.isJpaEmbeddable(effectiveMetaClass)
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

    public byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException {
        return templateGenerator.generateTemplate(reportData, templateFileType);
    }

    public EntityTree buildEntityTree(MetaClass metaClass) {
        return entityTreeModelBuilderApiProvider.get().buildEntityTree(metaClass);
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
