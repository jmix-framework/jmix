/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl.builder;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.Resources;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.*;
import io.jmix.reports.entity.*;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;
import io.jmix.reports.entity.table.TemplateTableDescription;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.libintegration.MultiEntityDataLoader;
import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.CustomValueFormatter;
import io.jmix.reports.yarg.structure.DefaultValueProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

@Component("report_AnnotatedReportBuilder")
public class AnnotatedReportBuilderImpl implements AnnotatedReportBuilder {

    protected final Metadata metadata;
    protected final MessageTools messageTools;
    protected final AnnotatedBuilderUtils annotatedBuilderUtils;
    protected final AnnotatedReportGroupHolder annotatedReportGroupHolder;
    protected final Resources resources;

    public AnnotatedReportBuilderImpl(Metadata metadata, MessageTools messageTools, AnnotatedBuilderUtils annotatedBuilderUtils,
                                      AnnotatedReportGroupHolder annotatedReportGroupHolder, Resources resources) {
        this.metadata = metadata;
        this.messageTools = messageTools;
        this.annotatedBuilderUtils = annotatedBuilderUtils;
        this.annotatedReportGroupHolder = annotatedReportGroupHolder;
        this.resources = resources;
    }

    @Override
    public Report createReportFromDefinition(Object definitionInstance) {
        ReportDef reportAnnotation = definitionInstance.getClass().getAnnotation(ReportDef.class);

        Report report = metadata.create(Report.class);
        assignReportParameters(report, reportAnnotation);
        assignReportDelegates(report, definitionInstance);

        report.setInputParameters(extractInputParameters(report, definitionInstance));
        report.setTemplates(extractTemplates(report, definitionInstance));
        report.setBands(extractBands(report, definitionInstance));
        report.setValuesFormats(extractValueFormats(report, definitionInstance));
        // todo roles and views
        return report;
    }

    protected void assignReportParameters(Report report, ReportDef annotation) {
        String nameValue = annotation.name();
        report.setName(messageTools.loadString(nameValue, messageTools.getDefaultLocale()));

        if (nameValue.startsWith(MessageTools.MARK)) {
            report.setLocaleNames(annotatedBuilderUtils.buildLocaleNames(nameValue));
        }

        if (annotation.code().isEmpty()) {
            throw new InvalidReportDefinitionException("Report code is mandatory");
        }
        report.setCode(annotation.code());

        if (!annotation.description().isEmpty()) {
            report.setDescription(annotation.description());
        }

        if (!annotation.uuid().isEmpty()) {
            try {
                report.setId(UUID.fromString(annotation.uuid()));
            } catch (IllegalArgumentException e) {
                throw new InvalidReportDefinitionException(e);
            }
        } // else keep automatically generated random id

        report.setRestAccess(annotation.restAccessible());
        report.setSystem(annotation.system());
        report.setSource(ReportSource.ANNOTATED_CLASS);

        assignGroup(report, annotation);
    }

    protected void assignGroup(Report report, ReportDef annotation) {
        if (Void.TYPE.equals(annotation.group())) {
            return;
        }

        ReportGroupDef groupAnnotation = annotation.group().getAnnotation(ReportGroupDef.class);
        if (groupAnnotation == null) {
            throw new InvalidReportDefinitionException(
                    "Report group class must be annotated with @ReportGroupDef: " + annotation.group());
        }
        ReportGroup group = annotatedReportGroupHolder.getGroupByCode(groupAnnotation.code());
        if (group == null) {
            throw new InvalidReportDefinitionException(
                    "Unregistered Report group: " + annotation.group());
        }
        report.setGroup(group);
    }

    protected void assignReportDelegates(Report report, Object definitionInstance) {
        for (Method method : definitionInstance.getClass().getMethods()) {
            if (!method.isAnnotationPresent(ReportDelegate.class)) {
                continue;
            }

            validateDelegateCommon(method);

            if (ParametersCrossValidator.class.equals(method.getReturnType())) {
                ParametersCrossValidator delegate = obtainDelegateFromDefinition(definitionInstance, method, ParametersCrossValidator.class);
                report.setParametersCrossValidator(delegate);
                report.setValidationOn(true);
            } else {
                throw new InvalidReportDefinitionException(String.format("Unsupported result type for delegate method: %s", method));
            }
        }
    }

    protected void validateDelegateCommon(Method method) {
        if (Modifier.isAbstract(method.getModifiers())) {
            throw new InvalidReportDefinitionException(String.format("Delegate method must be non-abstract: %s", method));
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new InvalidReportDefinitionException(String.format("Delegate method must be public: %s", method));
        }

        if (method.getParameterCount() != 0) {
            throw new InvalidReportDefinitionException(String.format("Delegate declaration method must have zero arguments: %s", method));
        }
    }

    protected List<ReportInputParameter> extractInputParameters(Report report, Object definitionInstance) {
        List<ReportInputParameter> inputParameters = new ArrayList<>();
        InputParameterDef[] annotations = definitionInstance.getClass().getDeclaredAnnotationsByType(InputParameterDef.class);

        int position = 0;
        for (InputParameterDef annotation : annotations) {
            ReportInputParameter parameter = convertToInputParameter(annotation);

            parameter.setReport(report);
            parameter.setPosition(position++);
            inputParameters.add(parameter);
        }

        validateUniqueness(inputParameters, ReportInputParameter::getAlias,
                "Duplicate input parameter alias within report definition");
        assignInputParameterDelegates(inputParameters, definitionInstance);

        return Collections.unmodifiableList(inputParameters);
    }

    protected ReportInputParameter convertToInputParameter(InputParameterDef annotation) {
        ReportInputParameter parameter = metadata.create(ReportInputParameter.class);
        parameter.setAlias(annotation.alias());

        String nameValue = annotation.name();
        parameter.setName(messageTools.loadString(nameValue, messageTools.getDefaultLocale()));
        if (nameValue.startsWith(MessageTools.MARK)) {
            parameter.setLocaleNames(annotatedBuilderUtils.buildLocaleNames(nameValue));
        }

        parameter.setRequired(annotation.required());
        parameter.setType(annotation.type());
        if (!Void.TYPE.equals(annotation.enumerationClass())) {
            if (!annotation.enumerationClass().isEnum()) {
                throw new InvalidReportDefinitionException(
                        "Attribute enumerationClass must reference Enum type: " + annotation.enumerationClass());
            }
            parameter.setEnumerationClass(annotation.enumerationClass().getName());
        }
        if (!annotation.defaultValue().isEmpty()) {
            parameter.setDefaultValue(annotation.defaultValue());
        }
        parameter.setDefaultDateIsCurrent(annotation.defaultDateIsCurrent());

        if (annotation.predefinedTransformationEnabled()) {
            parameter.setPredefinedTransformation(annotation.predefinedTransformation());
        }
        parameter.setHidden(annotation.hidden());

        EntityParameterDef entityAnnotation = annotation.entity();
        if (annotation.type() == ParameterType.ENTITY || annotation.type() == ParameterType.ENTITY_LIST) {
            if (Void.TYPE.equals(entityAnnotation.entityClass())) {
                throw new InvalidReportDefinitionException(
                        "entity.entityClass attribute is mandatory for " + annotation.type() + " parameter: " + annotation);
            }
            MetaClass metaClass = metadata.getClass(entityAnnotation.entityClass());
            parameter.setEntityMetaClass(metaClass.getName());

            if (!entityAnnotation.lookupViewId().isEmpty()) {
                parameter.setScreen(entityAnnotation.lookupViewId());
            }
        }

        if (annotation.type() == ParameterType.ENTITY) {
            parameter.setLookup(entityAnnotation.component() == EntityInputComponent.OPTION_LIST);

            if (entityAnnotation.component() == EntityInputComponent.OPTION_LIST) {
                if (!entityAnnotation.optionsQueryJoin().isEmpty()) {
                    parameter.setLookupJoin(entityAnnotation.optionsQueryJoin());
                }
                if (!entityAnnotation.optionsQueryWhere().isEmpty()) {
                    parameter.setLookupWhere(entityAnnotation.optionsQueryWhere());
                }
            }
        }

        return parameter;
    }

    @SuppressWarnings("rawtypes")
    protected void assignInputParameterDelegates(List<ReportInputParameter> inputParameters, Object definitionInstance) {
        for (Method method : definitionInstance.getClass().getMethods()) {
            if (!method.isAnnotationPresent(InputParameterDelegate.class)) {
                continue;
            }
            InputParameterDelegate annotation = method.getAnnotation(InputParameterDelegate.class);

            validateDelegateCommon(method);
            ReportInputParameter inputParameter = findElementInListByUniqueName(inputParameters, annotation.alias(),
                    ReportInputParameter::getAlias, annotation, "Report definition doesn't contain input parameter with alias");

            if (DefaultValueProvider.class.equals(method.getReturnType())) {
                DefaultValueProvider delegate = obtainDelegateFromDefinition(definitionInstance, method, DefaultValueProvider.class);
                inputParameter.setDefaultValueProvider(delegate);
            } else if (ParameterValidator.class.equals(method.getReturnType())) {
                ParameterValidator delegate = obtainDelegateFromDefinition(definitionInstance, method, ParameterValidator.class);
                inputParameter.setValidationDelegate(delegate);
                inputParameter.setValidationOn(true);
            } else if (ParameterTransformer.class.equals(method.getReturnType())) {
                ParameterTransformer delegate = obtainDelegateFromDefinition(definitionInstance, method, ParameterTransformer.class);
                inputParameter.setTransformationDelegate(delegate);
            } else {
                throw new RuntimeException(String.format("Unsupported result type for delegate: %s", method));
            }
        }
    }

    protected List<ReportTemplate> extractTemplates(Report report, Object definitionInstance) {
        List<ReportTemplate> templates = new ArrayList<>();
        TemplateDef[] annotations = definitionInstance.getClass().getDeclaredAnnotationsByType(TemplateDef.class);
        if (annotations.length == 0) {
            throw new InvalidReportDefinitionException("Report definition must have at least one template: " + definitionInstance.getClass());
        }

        boolean containsDefault = false;
        for (TemplateDef annotation : annotations) {
            ReportTemplate template = convertToTemplate(annotation);

            template.setReport(report);
            if (annotation.isDefault()) {
                if (containsDefault) {
                    throw new InvalidReportDefinitionException("More than one template definition with 'isDefault' flag set: " + annotation);
                }
                containsDefault = true;
                report.setDefaultTemplate(template);
            }
            templates.add(template);
        }
        validateUniqueness(templates, ReportTemplate::getCode, "Duplicate template code within report definition");

        assignTemplateDelegates(templates, definitionInstance);
        validateMandatoryTemplateDelegates(templates);

        return Collections.unmodifiableList(templates);
    }

    private void validateMandatoryTemplateDelegates(List<ReportTemplate> templates) {
        for (ReportTemplate t : templates) {
            if (t.isCustom() && t.getCustomDefinedBy() == CustomTemplateDefinedBy.DELEGATE && t.getDelegate() == null) {
                throw new InvalidReportDefinitionException("Template must have associated CustomReport delegate: " + t.getCode());
            }
        }
    }

    protected ReportTemplate convertToTemplate(TemplateDef annotation) {
        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setCode(annotation.code());
        template.setReportOutputType(annotation.outputType());
        template.setAlterable(annotation.alterableOutput());
        template.setOutputNamePattern(annotation.outputNamePattern());
        template.setGroovy(annotation.templateEngine() == HtmlTemplateEngine.GROOVY);

        if (!annotation.filePath().isEmpty()) {
            try (InputStream stream = resources.getResourceAsStream(annotation.filePath())) {
                if (stream == null) {
                    throw new InvalidReportDefinitionException("Template file does not exist: " + annotation.filePath());
                }
                byte[] content = IOUtils.toByteArray(stream);
                template.setContent(content);

                template.setName(FilenameUtils.getName(annotation.filePath()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to load template file: " + annotation.filePath(), e);
            }
        }

        CustomTemplateParameters customAnnotation = annotation.custom();
        if (customAnnotation.enabled()) {
            template.setCustom(true);

            if (customAnnotation.definedBy() == CustomTemplateDefinedBy.SCRIPT
                || customAnnotation.definedBy() == CustomTemplateDefinedBy.CLASS) {
                throw new InvalidReportDefinitionException(
                        customAnnotation.definedBy() + " is not supported for reports defined in code, use DELEGATE instead"
                );
            } else if (customAnnotation.definedBy() == CustomTemplateDefinedBy.URL) {
                template.setCustomDefinition(customAnnotation.urlScript());
            } else if (customAnnotation.definedBy() == CustomTemplateDefinedBy.DELEGATE) {
                // validation that delegate method exists is performed later
            }
            template.setCustomDefinedBy(customAnnotation.definedBy());
        }

        Set<ReportOutputType> requiringFile = Set.of(
                ReportOutputType.XLS,
                ReportOutputType.DOC,
                ReportOutputType.PDF,
                ReportOutputType.HTML,
                ReportOutputType.DOCX,
                ReportOutputType.XLSX,
                ReportOutputType.CUSTOM,
                ReportOutputType.CSV
        );

        if (requiringFile.contains(annotation.outputType())
                && !customAnnotation.enabled() && annotation.filePath().isEmpty()) {
            throw new InvalidReportDefinitionException(String.format("Template filePath is mandatory for %s output type. %s",
                    annotation.outputType(), annotation));
        }

        if (annotation.outputType() == ReportOutputType.TABLE) {
            template.setTemplateTableDescription(convertToTableDescription(annotation.table()));
        }
        return template;
    }

    private TemplateTableDescription convertToTableDescription(TemplateTableDef table) {
        TemplateTableDescription description = metadata.create(TemplateTableDescription.class);
        List<TemplateTableBand> tableBands = new ArrayList<>();
        int bandPosition = 0;
        for (TableBandDef bandDef : table.bands()) {
            TemplateTableBand band = metadata.create(TemplateTableBand.class);
            band.setPosition(bandPosition++);
            band.setBandName(bandDef.bandName());

            List<TemplateTableColumn> tableColumns = new ArrayList<>();
            int columnPosition = 0;
            for (TableColumnDef columnDef : bandDef.columns()) {
                TemplateTableColumn column = metadata.create(TemplateTableColumn.class);
                column.setPosition(columnPosition++);
                column.setKey(columnDef.key());

                // todo problem: we can't here adjust locale for different users
                column.setCaption(messageTools.loadString(columnDef.caption(), messageTools.getDefaultLocale()));

                tableColumns.add(column);
            }
            band.setColumns(Collections.unmodifiableList(tableColumns));
            tableBands.add(band);
        }

        description.setTemplateTableBands(Collections.unmodifiableList(tableBands));
        return description;
    }

    protected void assignTemplateDelegates(List<ReportTemplate> templates, Object definitionInstance) {
        for (Method method : definitionInstance.getClass().getMethods()) {
            if (!method.isAnnotationPresent(TemplateDelegate.class)) {
                continue;
            }
            TemplateDelegate annotation = method.getAnnotation(TemplateDelegate.class);

            validateDelegateCommon(method);
            ReportTemplate template = findElementInListByUniqueName(templates, annotation.code(), ReportTemplate::getCode,
                    annotation, "Report definition doesn't contain template with code");

            if (CustomReport.class.equals(method.getReturnType())) {
                CustomReport delegate = obtainDelegateFromDefinition(definitionInstance, method, CustomReport.class);
                if (!template.isCustom()) {
                    throw new InvalidReportDefinitionException(
                            "Wrong delegate target. Template must have custom.enabled set to true. " + method);
                }
                if (template.getCustomDefinedBy() != CustomTemplateDefinedBy.DELEGATE) {
                    throw new InvalidReportDefinitionException(
                            String.format("Wrong delegate target. Template custom.definedBy - %s, expected: %s. %s",
                                    template.getCustomDefinedBy(), CustomTemplateDefinedBy.DELEGATE, method));
                }
                template.setDelegate(delegate);
            } else {
                throw new InvalidReportDefinitionException(String.format("Unsupported result type for delegate: %s", method));
            }
        }
    }

    private Set<BandDefinition> extractBands(Report report, Object definitionInstance) {
        Set<BandDefinition> bands = new LinkedHashSet<>();
        BandDef[] annotations = definitionInstance.getClass().getDeclaredAnnotationsByType(BandDef.class);
        if (annotations.length == 0) {
            throw new InvalidReportDefinitionException("Report definition must have at least one band: " + definitionInstance.getClass());
        }

        int position = 0;
        boolean hasRoot = false;
        for (BandDef annotation : annotations) {
            BandDefinition band = metadata.create(BandDefinition.class);
            band.setName(annotation.name());
            band.setOrientation(annotation.orientation());
            band.setPosition(position++);
            band.setReport(report);

            if (!annotation.parent().isEmpty()) {
                if (annotation.root()) {
                   throw new InvalidReportDefinitionException(String.format("Root band cannot have parent: %s", annotation));
                }
                band.setParentBandDefinition(bands.stream()
                        .filter(b -> annotation.parent().equals(b.getName()))
                        .findAny()
                        .orElseThrow(() -> new InvalidReportDefinitionException(String.format(
                                "Parent band with name '%s' referenced from '%s' is not defined, or defined below its child",
                                annotation.parent(), annotation)
                        ))
                );
            } else {
                // no parent == root
                if (!annotation.root()) {
                    throw new InvalidReportDefinitionException(String.format("Non-root band must have parent: %s", annotation));
                }
                if (hasRoot) {
                    throw new InvalidReportDefinitionException(String.format("More than one root band defined in report: %s", annotation));
                }
                hasRoot = true;
            }

            band.setDataSets(extractDataSets(report, band, annotation.dataSets(), definitionInstance));
            band.setMultiDataSet(band.getDataSets().size() > 1);
            bands.add(band);
        }

        validateUniqueness(bands, BandDefinition::getName, "Duplicate band name within report definition");

        for (BandDefinition parentBand: bands) {
            parentBand.setChildrenBandDefinitions(bands.stream()
                    .filter(childBand -> parentBand.equals(childBand.getParentBandDefinition()))
                    .toList()
            );
        }

        assignDataSetDelegates(bands, definitionInstance);
        validateMandatoryDelegates(bands);

        return Collections.unmodifiableSet(bands);
    }

    protected void validateMandatoryDelegates(Set<BandDefinition> bands) {
        for (BandDefinition band : bands) {
            for (DataSet dataSet : band.getDataSets()) {
                if (dataSet.getType() == DataSetType.DELEGATE && dataSet.getLoaderDelegate() == null) {
                    throw new InvalidReportDefinitionException("Dataset must have associated ReportDataLoader delegate: " + dataSet.getName());
                }
                if (dataSet.getType() == DataSetType.JSON && dataSet.getJsonSourceType() == JsonSourceType.DELEGATE
                        && dataSet.getJsonInputProvider() == null) {
                    throw new InvalidReportDefinitionException("Dataset must have associated JsonInputProvider delegate: " + dataSet.getName());
                }
                if ((dataSet.getType() == DataSetType.SINGLE || dataSet.getType() == DataSetType.MULTI)
                        && BooleanUtils.isNotTrue(dataSet.getUseExistingFetchPLan())
                        && dataSet.getFetchPlanProvider() == null) {
                    throw new InvalidReportDefinitionException("Dataset must have associated FetchPlanProvider delegate: " + dataSet.getName());
                }
            }
        }
    }

    private List<DataSet> extractDataSets(Report report, BandDefinition bandDefinition, DataSetDef[] dataSetDefs, Object definitionInstance) {
        List<DataSet> dataSets = new ArrayList<>();

        for (DataSetDef annotation : dataSetDefs) {
            DataSet dataSet = metadata.create(DataSet.class);

            if (!annotation.name().isEmpty()) {
                dataSet.setName(annotation.name());
            } else {
                // allow to omit data set name for single data sets
                if (dataSetDefs.length == 1) {
                    dataSet.setName(bandDefinition.getName());
                } else {
                    throw new InvalidReportDefinitionException("Data set name is required for multiple data sets: " + annotation);
                }
            }
            dataSet.setType(annotation.type());
            if (!annotation.linkParameterName().isEmpty()) {
                dataSet.setLinkParameterName(annotation.linkParameterName());
            }
            if (!annotation.dataStore().isEmpty()) {
                dataSet.setDataStore(annotation.dataStore());
            }
            dataSet.setProcessTemplate(annotation.processTemplate());
            if (!annotation.query().isEmpty()) {
                dataSet.setText(annotation.query());
            }

            if (annotation.type() == DataSetType.JSON) {
                extractJsonDataSetParameters(report, annotation.json(), dataSet);
            }

            if (annotation.type() == DataSetType.SINGLE || annotation.type() == DataSetType.MULTI) {
                extractEntityDataSetParameters(annotation, report, dataSet, annotation.entity());
            }

            dataSet.setBandDefinition(bandDefinition);
            dataSets.add(dataSet);
        }

        return Collections.unmodifiableList(dataSets);
    }

    private void extractJsonDataSetParameters(Report report, JsonDataSetParameters jsonAnnotation, DataSet dataSet) {
        if (jsonAnnotation.source() == JsonSourceType.GROOVY_SCRIPT) {
            throw new InvalidReportDefinitionException(
                    jsonAnnotation.source() + " is not supported for reports defined in code, use DELEGATE instead"
            );
        }
        dataSet.setJsonSourceType(jsonAnnotation.source());
        dataSet.setJsonPathQuery(jsonAnnotation.jsonPathQuery());

        if (jsonAnnotation.source() == JsonSourceType.PARAMETER) {
            ReportInputParameter inputParameter = findElementInListByUniqueName(report.getInputParameters(),
                    jsonAnnotation.inputParameter(), ReportInputParameter::getAlias, jsonAnnotation,
                    "Report definition doesn't contain input parameter with alias");

            dataSet.setJsonSourceInputParameter(inputParameter);
        }

        if (jsonAnnotation.source() == JsonSourceType.URL) {
            if (jsonAnnotation.url().isEmpty()) {
                throw new InvalidReportDefinitionException("Url is required for JSON/URL data set: " + jsonAnnotation);
            }
            dataSet.setJsonSourceText(jsonAnnotation.url());
        }
    }

    private void extractEntityDataSetParameters(DataSetDef annotation, Report report, DataSet dataSet, EntityDataSetDef entityAnnotation) {
        findElementInListByUniqueName(report.getInputParameters(),
                entityAnnotation.parameterAlias(), ReportInputParameter::getAlias, entityAnnotation,
                "Report definition doesn't contain input parameter with alias");

        if (annotation.type() == DataSetType.SINGLE) {
            dataSet.setEntityParamName(entityAnnotation.parameterAlias());
        } else {
            if (entityAnnotation.nestedCollectionAttribute().isEmpty()) {
                dataSet.setListEntitiesParamName(entityAnnotation.parameterAlias());
            } else {
                // see io.jmix.reports.libintegration.MultiEntityDataLoader.loadData
                String combinedValue = entityAnnotation.parameterAlias() + MultiEntityDataLoader.NESTED_COLLECTION_SEPARATOR
                                       + entityAnnotation.nestedCollectionAttribute();
                dataSet.setListEntitiesParamName(combinedValue);
            }
        }

        if (!entityAnnotation.fetchPlanName().isEmpty()) {
            dataSet.setUseExistingFetchPLan(true);
            dataSet.setFetchPlanName(entityAnnotation.fetchPlanName());
        } else {
            dataSet.setUseExistingFetchPLan(false); // fetch plan provider is set and validated later
        }
    }

    private void assignDataSetDelegates(Set<BandDefinition> bands, Object definitionInstance) {
        List<DataSet> allDataSets = bands.stream()
                .flatMap(band -> band.getDataSets().stream())
                .toList();

        for (Method method : definitionInstance.getClass().getMethods()) {
            if (!method.isAnnotationPresent(DataSetDelegate.class)) {
                continue;
            }
            DataSetDelegate annotation = method.getAnnotation(DataSetDelegate.class);

            validateDelegateCommon(method);
            DataSet dataSet = findElementInListByUniqueName(allDataSets, annotation.name(), DataSet::getName, annotation,
                    "Report definition doesn't contain data set with name");

            if (ReportDataLoader.class.equals(method.getReturnType())) {
                ReportDataLoader delegate = obtainDelegateFromDefinition(definitionInstance, method, ReportDataLoader.class);
                if (dataSet.getType() != DataSetType.DELEGATE) {
                    throw new InvalidReportDefinitionException(
                            String.format("Wrong delegate target. Dataset type: %s, expected: %s. %s",
                                    dataSet.getType(), DataSetType.DELEGATE, method));
                }
                dataSet.setLoaderDelegate(delegate);
            } else if (FetchPlanProvider .class.equals(method.getReturnType())) {
                FetchPlanProvider delegate = obtainDelegateFromDefinition(definitionInstance, method, FetchPlanProvider.class);
                if (dataSet.getType() != DataSetType.SINGLE && dataSet.getType() != DataSetType.MULTI) {
                    throw new InvalidReportDefinitionException(
                            String.format("Wrong delegate target. Dataset type: %s, expected: %s or %s. %s",
                                    dataSet.getType(), DataSetType.SINGLE, DataSetType.MULTI, method));
                }
                dataSet.setFetchPlanProvider(delegate);
            } else if (JsonInputProvider.class.equals(method.getReturnType())) {
                JsonInputProvider delegate = obtainDelegateFromDefinition(definitionInstance, method, JsonInputProvider.class);
                if (dataSet.getType() != DataSetType.JSON) {
                    throw new InvalidReportDefinitionException(
                            String.format("Wrong delegate target. Dataset type: %s, expected: %s. %s",
                                    dataSet.getType(), DataSetType.JSON, method));
                }
                if (dataSet.getJsonSourceType() != JsonSourceType.DELEGATE) {
                    throw new InvalidReportDefinitionException(
                            String.format("Wrong delegate target. Dataset json.source: %s, expected: %s. %s",
                                    dataSet.getJsonSourceType(), JsonSourceType.DELEGATE, method));
                }
                dataSet.setJsonInputProvider(delegate);
            } else {
                throw new InvalidReportDefinitionException(String.format("Unsupported result type for delegate: %s", method));
            }
        }
    }

    private List<ReportValueFormat> extractValueFormats(Report report, Object definitionInstance) {
        List<ReportValueFormat> formats = new ArrayList<>();
        ValueFormatDef[] annotations = definitionInstance.getClass().getDeclaredAnnotationsByType(ValueFormatDef.class);

        for (ValueFormatDef annotation : annotations) {
            ReportValueFormat format = convertToValueFormat(report, annotation);

            format.setReport(report);
            formats.add(format);
        }

        validateUniqueness(formats, ReportValueFormat::getValueName, "Duplicate value format name within report definition");
        assignValueFormatDelegates(formats, definitionInstance);
        validateMandatoryFormatDelegates(formats);
        return Collections.unmodifiableList(formats);
    }

    private ReportValueFormat convertToValueFormat(Report report, ValueFormatDef annotation) {
        ReportValueFormat format = metadata.create(ReportValueFormat.class);

        if (!annotation.band().isEmpty()) {
            findElementInListByUniqueName(report.getBands(),
                    annotation.band(), BandDefinition::getName, annotation,
                    "Report definition doesn't contain band with name");
        }
        format.setValueName(getValueName(annotation.band(), annotation.field()));

        if (!annotation.format().isEmpty()) {
            format.setFormatString(annotation.format());
        }

        return format;
    }

    protected String getValueName(String band, String field) {
        // Format can be bound to field name, or for band name + field name. See:
        // io.jmix.reports.yarg.formatters.impl.AbstractFormatter.getFullParameterName
        // io.jmix.reports.yarg.formatters.impl.AbstractFormatter.getReportFieldFormat
        String valueName = band.isEmpty()
                ? field
                : band + "." + field;
        return valueName;
    }

    protected void assignValueFormatDelegates(List<ReportValueFormat> formats, Object definitionInstance) {
        for (Method method : definitionInstance.getClass().getMethods()) {
            if (!method.isAnnotationPresent(ValueFormatDelegate.class)) {
                continue;
            }
            ValueFormatDelegate annotation = method.getAnnotation(ValueFormatDelegate.class);

            validateDelegateCommon(method);
            ReportValueFormat format = findElementInListByUniqueName(
                    formats,
                    getValueName(annotation.band(), annotation.field()),
                    ReportValueFormat::getValueName,
                    annotation,
                    "Report definition doesn't contain value format with name"
            );

            if (CustomValueFormatter.class.equals(method.getReturnType())) {
                CustomValueFormatter delegate = obtainDelegateFromDefinition(definitionInstance, method, CustomValueFormatter.class);
                format.setCustomFormatter(delegate);
            } else {
                throw new InvalidReportDefinitionException(String.format("Unsupported result type for delegate: %s", method));
            }
        }
    }

    protected void validateMandatoryFormatDelegates(List<ReportValueFormat> formats) {
        for (ReportValueFormat format : formats) {
            if (format.getFormatString() == null && format.getCustomFormatter() == null) {
                throw new InvalidReportDefinitionException(String.format(
                        "Value Format '%s' must have either format string or formatter delegate assigned", format.getValueName()));
            }
        }
    }

    protected <E, A extends Annotation> E findElementInListByUniqueName(Collection<E> elements, String name, Function<E, String> elementNameExtractor,
                                                                      A annotation, String errorMessagePrefix) {
        return elements.stream()
                .filter(t -> name.equals(elementNameExtractor.apply(t)))
                .findAny()
                .orElseThrow(() -> new InvalidReportDefinitionException(
                        // Report definition doesn't contain element with name
                        String.format("%s '%s', requested by annotation '%s'",
                                errorMessagePrefix, name, annotation)
                ));
    }

    protected <T> T obtainDelegateFromDefinition(Object reportDefinition, Method method, Class<T> delegateClass) {
        T delegate = delegateClass.cast(ReflectionUtils.invokeMethod(method, reportDefinition));
        validateDelegateInstanceCommon(method, delegate);
        return delegate;
    }

    protected void validateDelegateInstanceCommon(Method method, @Nullable Object delegate) {
        if (delegate == null) {
            throw new InvalidReportDefinitionException(String.format("Delegate declaration method returned null: %s", method));
        }
    }

    protected <T> void validateUniqueness(Collection<T> objects, Function<T, String> uniqueAttributeGetter,
                                           String errorMessage) {
        Set<String> values = new HashSet<>();
        for (T object : objects) {
            String value = uniqueAttributeGetter.apply(object);
            boolean didNotAlreadyContain = values.add(value);
            if (!didNotAlreadyContain) {
                throw new InvalidReportDefinitionException(String.format("%s: '%s'", errorMessage, value));
            }
        }
    }
}
