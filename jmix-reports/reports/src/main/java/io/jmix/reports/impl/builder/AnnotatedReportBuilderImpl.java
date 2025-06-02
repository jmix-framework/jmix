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
import io.jmix.reports.impl.AnnotatedReportGroupProvider;
import io.jmix.reports.libintegration.MultiEntityDataLoader;
import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.CustomValueFormatter;
import io.jmix.reports.yarg.structure.DefaultValueProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;

@Component("report_AnnotatedReportBuilder")
public class AnnotatedReportBuilderImpl implements AnnotatedReportBuilder {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedReportBuilderImpl.class);
    protected final Metadata metadata;
    protected final MessageTools messageTools;
    protected final AnnotatedBuilderUtils annotatedBuilderUtils;
    protected final AnnotatedReportGroupProvider annotatedReportGroupProvider;
    private final Resources resources;

    public AnnotatedReportBuilderImpl(Metadata metadata, MessageTools messageTools, AnnotatedBuilderUtils annotatedBuilderUtils,
                                      AnnotatedReportGroupProvider annotatedReportGroupProvider, Resources resources) {
        this.metadata = metadata;
        this.messageTools = messageTools;
        this.annotatedBuilderUtils = annotatedBuilderUtils;
        this.annotatedReportGroupProvider = annotatedReportGroupProvider;
        this.resources = resources;
    }

    @Override
    public Report createReportFromDefinition(Object definitionInstance) {
        Class<?> definitionClass = definitionInstance.getClass();
        ReportDef reportAnnotation = definitionClass.getAnnotation(ReportDef.class);

        Report report = metadata.create(Report.class);
        assignReportParameters(report, reportAnnotation, definitionClass);
        assignReportDelegates(report, definitionClass, definitionInstance);

        report.setInputParameters(extractInputParameters(report, definitionClass, definitionInstance));
        report.setTemplates(extractTemplates(report, definitionClass, definitionInstance));
        report.setBands(extractBands(report, definitionClass, definitionInstance));
        report.setValuesFormats(extractValueFormats(report, definitionClass, definitionInstance));
        // todo roles and views
        return report;
    }

    protected void assignReportParameters(Report report, ReportDef annotation, Class<?> definitionClass) {
        String nameValue = annotation.name();
        report.setName(messageTools.loadString(nameValue, messageTools.getDefaultLocale()));

        if (nameValue.startsWith(MessageTools.MARK)) {
            report.setLocaleNames(annotatedBuilderUtils.buildLocaleNames(nameValue));
        }

        report.setCode(annotation.code());
        if (!annotation.description().isEmpty()) {
            report.setDescription(annotation.description());
        }

        if (!annotation.uuid().isEmpty()) {
            report.setId(UUID.fromString(annotation.uuid()));
        }
        // else keep automatically generated random id

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
        ReportGroup group = annotatedReportGroupProvider.getGroupByCode(groupAnnotation.code());
        if (group == null) {
            throw new InvalidReportDefinitionException(
                    "Unregistered Report group: " + annotation.group());
        }
        report.setGroup(group);
    }

    protected void assignReportDelegates(Report report, Class<?> reportClass, Object reportDefinition) {
        for (Method method : reportClass.getMethods()) {
            if (!method.isAnnotationPresent(ReportDelegate.class)) {
                continue;
            }

            validateDelegateCommon(method);

            if (ParametersCrossValidator.class.equals(method.getReturnType())) {
                ParametersCrossValidator delegate = obtainDelegateFromDefinition(reportDefinition, method, ParametersCrossValidator.class);
                report.setParametersCrossValidator(delegate);
                report.setValidationOn(true);
            } else {
                throw new RuntimeException(String.format("Unsupported return type for delegate: %s", method));
            }
        }
    }

    protected void validateDelegateCommon(Method method) {
        if (Modifier.isAbstract(method.getModifiers())) {
            throw new RuntimeException(String.format("Delegate method must be non-abstract: %s", method));
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new RuntimeException(String.format("Delegate method must be public: %s", method));
        }

        if (method.getParameterCount() != 0) {
            throw new RuntimeException(String.format("Delegate declaration method must have zero arguments: %s", method));
        }
    }

    protected List<ReportInputParameter> extractInputParameters(Report report, Class<?> reportClass, Object reportDefinition) {
        List<ReportInputParameter> inputParameters = new ArrayList<>();
        InputParameterDef[] annotations = reportClass.getDeclaredAnnotationsByType(InputParameterDef.class);

        int position = 0;
        for (InputParameterDef annotation : annotations) {
            ReportInputParameter parameter = convertToInputParameter(annotation);

            parameter.setReport(report);
            parameter.setPosition(position++);
            inputParameters.add(parameter);
        }
        // todo validate unique aliases
        assignInputParameterDelegates(inputParameters, reportClass, reportDefinition);
        return inputParameters;
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
    protected void assignInputParameterDelegates(List<ReportInputParameter> inputParameters, Class<?> reportClass,
                                                 Object reportDefinition) {
        for (Method method : reportClass.getMethods()) {
            if (!method.isAnnotationPresent(InputParameterDelegate.class)) {
                continue;
            }
            InputParameterDelegate annotation = method.getAnnotation(InputParameterDelegate.class);

            validateDelegateCommon(method);
            ReportInputParameter inputParameter = findElementInListByUniqueName(inputParameters, annotation.alias(),
                    ReportInputParameter::getAlias, annotation);

            if (DefaultValueProvider.class.equals(method.getReturnType())) {
                DefaultValueProvider delegate = obtainDelegateFromDefinition(reportDefinition, method, DefaultValueProvider.class);
                inputParameter.setDefaultValueProvider(delegate);
            } else if (ParameterValidator.class.equals(method.getReturnType())) {
                ParameterValidator delegate = obtainDelegateFromDefinition(reportDefinition, method, ParameterValidator.class);
                inputParameter.setValidationDelegate(delegate);
                inputParameter.setValidationOn(true);
            } else if (ParameterTransformer.class.equals(method.getReturnType())) {
                ParameterTransformer delegate = obtainDelegateFromDefinition(reportDefinition, method, ParameterTransformer.class);
                inputParameter.setTransformationDelegate(delegate);
            } else {
                throw new RuntimeException(String.format("Unsupported return type for delegate: %s", method));
            }
        }
    }

    protected List<ReportTemplate> extractTemplates(Report report, Class<?> reportClass, Object reportDefinition) {
        List<ReportTemplate> templates = new ArrayList<>();
        TemplateDef[] annotations = reportClass.getDeclaredAnnotationsByType(TemplateDef.class);

        for (TemplateDef annotation : annotations) {
            ReportTemplate template = convertToTemplate(annotation);

            template.setReport(report);
            if (annotation.isDefault()) {
                // todo validate unique isDefault
                report.setDefaultTemplate(template);
            }
            templates.add(template);
        }
        // todo validate unique code
        assignTemplateDelegates(templates, reportClass, reportDefinition);
        // todo validate that custom DELEGATE report actually has delegate
        return templates;
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
                    throw new RuntimeException("Template file does not exist: " + annotation.filePath());
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
                throw new UnsupportedOperationException(
                        customAnnotation.definedBy() + " is not supported for reports defined in code, use DELEGATE instead"
                );
            } else if (customAnnotation.definedBy() == CustomTemplateDefinedBy.URL) {
                template.setCustomDefinition(customAnnotation.urlScript());
            } else if (customAnnotation.definedBy() == CustomTemplateDefinedBy.DELEGATE) {
                // todo should we validate here that delegate method exists?
            }
            template.setCustomDefinedBy(customAnnotation.definedBy());
        }
        if (annotation.outputType() == ReportOutputType.TABLE) {
            template.setTemplateTableDescription(convertToTableDescription(annotation.table()));
        }
        return template;
    }

    private TemplateTableDescription convertToTableDescription(TemplateTableDef table) {
        TemplateTableDescription description = metadata.create(TemplateTableDescription.class);
        description.setTemplateTableBands(new ArrayList<>());
        int bandPosition = 0;
        for (TableBandDef bandDef : table.bands()) {
            TemplateTableBand band = metadata.create(TemplateTableBand.class);
            band.setPosition(bandPosition++);
            band.setBandName(bandDef.bandName());
            band.setColumns(new ArrayList<>());

            int columnPosition = 0;
            for (TableColumnDef columnDef : bandDef.columns()) {
                TemplateTableColumn column = metadata.create(TemplateTableColumn.class);
                column.setPosition(columnPosition++);
                column.setKey(columnDef.key());

                // todo problem: we can't here adjust locale for different users
                column.setCaption(messageTools.loadString(columnDef.caption(), messageTools.getDefaultLocale()));

                band.getColumns().add(column);
            }

            description.getTemplateTableBands().add(band);
        }
        return description;
    }

    protected void assignTemplateDelegates(List<ReportTemplate> templates, Class<?> reportClass, Object reportDefinition) {
        for (Method method : reportClass.getMethods()) {
            if (!method.isAnnotationPresent(TemplateDelegate.class)) {
                continue;
            }
            TemplateDelegate annotation = method.getAnnotation(TemplateDelegate.class);

            validateDelegateCommon(method);
            ReportTemplate template = findElementInListByUniqueName(templates, annotation.code(), ReportTemplate::getCode, annotation);

            if (CustomReport.class.equals(method.getReturnType())) {
                CustomReport delegate = obtainDelegateFromDefinition(reportDefinition, method, CustomReport.class);
                template.setDelegate(delegate);
            } else {
                throw new RuntimeException(String.format("Unsupported return type for delegate: %s", method));
            }
        }
    }

    private Set<BandDefinition> extractBands(Report report, Class<?> definitionClass, Object definitionInstance) {
        Set<BandDefinition> bands = new LinkedHashSet<>();
        BandDef[] annotations = definitionClass.getDeclaredAnnotationsByType(BandDef.class);

        int position = 0;
        for (BandDef annotation : annotations) {
            BandDefinition band = metadata.create(BandDefinition.class);
            band.setName(annotation.name());
            band.setOrientation(annotation.orientation());
            band.setPosition(position++);
            band.setReport(report);

            if (!annotation.parent().isEmpty()) {
                if (annotation.root()) {
                   throw new RuntimeException(String.format("Root band cannot have parent: %s", annotation));
                }
                band.setParentBandDefinition(bands.stream()
                        .filter(b -> annotation.parent().equals(b.getName()))
                        .findAny()
                        .orElseThrow(() -> new RuntimeException(String.format(
                                "Band with name '%s' referenced from '%s' is not defined, or defined below its child",
                                annotation.parent(), annotation)
                        ))
                );
            } else {
                if (!annotation.root()) {
                    throw new RuntimeException(String.format("Non-root band must have parent: %s", annotation));
                }
            }

            band.setDataSets(extractDataSets(report, band, definitionClass, annotation.dataSets(), definitionInstance));
            band.setMultiDataSet(band.getDataSets().size() > 1);
            bands.add(band);
            // todo validate root is unique
            // todo validate unique band names
        }

        for (BandDefinition parentBand: bands) {
            parentBand.setChildrenBandDefinitions(bands.stream()
                    .filter(childBand -> parentBand.equals(childBand.getParentBandDefinition()))
                    .collect(Collectors.toList())
            );
        }

        assignDataSetDelegates(bands, definitionClass, definitionInstance);

        return bands;
    }

    private List<DataSet> extractDataSets(Report report, BandDefinition bandDefinition, Class<?> definitionClass,
                                          DataSetDef[] dataSetDefs, Object definitionInstance) {
        List<DataSet> dataSets = new ArrayList<>();

        for (DataSetDef annotation : dataSetDefs) {
            DataSet dataSet = metadata.create(DataSet.class);
            dataSet.setName(annotation.name());
            dataSet.setType(annotation.type());
            if (!annotation.linkParameterName().isEmpty()) {
                dataSet.setLinkParameterName(annotation.linkParameterName());
            }
            if (!annotation.dataStore().isEmpty()) {
                dataSet.setDataStore(annotation.dataStore());
            }
            dataSet.setProcessTemplate(annotation.processTemplate());
            dataSet.setText(annotation.query());

            if (annotation.type() == DataSetType.JSON) {
                extractJsonDataSetParameters(report, annotation.json(), dataSet);
            }

            if (annotation.type() == DataSetType.SINGLE || annotation.type() == DataSetType.MULTI) {
                extractEntityDataSetParameters(annotation, dataSet, annotation.entity());
            }

            dataSet.setBandDefinition(bandDefinition);
            dataSets.add(dataSet);
        }

        return dataSets;
    }

    private void extractJsonDataSetParameters(Report report, JsonDataSetParameters jsonAnnotation, DataSet dataSet) {
        if (jsonAnnotation.source() == JsonSourceType.GROOVY_SCRIPT) {
            throw new UnsupportedOperationException(
                    jsonAnnotation.source() + " is not supported for reports defined in code, use DELEGATE instead"
            );
        }
        dataSet.setJsonSourceType(jsonAnnotation.source());
        dataSet.setJsonPathQuery(jsonAnnotation.jsonPathQuery());

        if (jsonAnnotation.source() == JsonSourceType.PARAMETER) {
            ReportInputParameter inputParameter = findElementInListByUniqueName(report.getInputParameters(),
                    jsonAnnotation.inputParameter(), ReportInputParameter::getAlias, jsonAnnotation);

            dataSet.setJsonSourceInputParameter(inputParameter);
        }

        if (jsonAnnotation.source() == JsonSourceType.URL) {
            dataSet.setJsonSourceText(jsonAnnotation.url());
        }
    }

    private void extractEntityDataSetParameters(DataSetDef annotation, DataSet dataSet, EntityDataSetDef entityAnnotation) {
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
            dataSet.setUseExistingFetchPLan(false); // fetch plan provider will be set later
        }
    }

    private void assignDataSetDelegates(Set<BandDefinition> bands, Class<?> definitionClass, Object definitionInstance) {
        List<DataSet> allDataSets = bands.stream()
                .flatMap(band -> band.getDataSets().stream())
                .toList();

        for (Method method : definitionClass.getMethods()) {
            if (!method.isAnnotationPresent(DataSetDelegate.class)) {
                continue;
            }
            DataSetDelegate annotation = method.getAnnotation(DataSetDelegate.class);

            validateDelegateCommon(method);
            DataSet dataSet = findElementInListByUniqueName(allDataSets, annotation.name(), DataSet::getName, annotation);

            if (ReportDataLoader.class.equals(method.getReturnType())) {
                ReportDataLoader delegate = obtainDelegateFromDefinition(definitionInstance, method, ReportDataLoader.class);
                dataSet.setLoaderDelegate(delegate);
            } else if (FetchPlanProvider .class.equals(method.getReturnType())) {
                FetchPlanProvider delegate = obtainDelegateFromDefinition(definitionInstance, method, FetchPlanProvider.class);
                dataSet.setFetchPlanProvider(delegate);
            } else if (JsonInputProvider.class.equals(method.getReturnType())) {
                JsonInputProvider delegate = obtainDelegateFromDefinition(definitionInstance, method, JsonInputProvider.class);
                dataSet.setJsonInputProvider(delegate);
            } else {
                throw new RuntimeException(String.format("Unsupported return type for delegate: %s", method));
            }
        }
        // todo validate that JSON / DELEGATE data sets have delegate assigned
        // todo validate that fetchPlan delegate exists
    }

    private List<ReportValueFormat> extractValueFormats(Report report, Class<?> definitionClass, Object definitionInstance) {
        List<ReportValueFormat> formats = new ArrayList<>();
        ValueFormatDef[] annotations = definitionClass.getDeclaredAnnotationsByType(ValueFormatDef.class);

        for (ValueFormatDef annotation : annotations) {
            ReportValueFormat format = convertToValueFormat(annotation);

            format.setReport(report);
            formats.add(format);
        }
        // todo validate uniqueness of band + field
        assignValueFormatDelegates(formats, definitionClass, definitionInstance);
        return formats;
    }

    private ReportValueFormat convertToValueFormat(ValueFormatDef annotation) {
        ReportValueFormat format = metadata.create(ReportValueFormat.class);

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

    private void assignValueFormatDelegates(List<ReportValueFormat> formats, Class<?> definitionClass, Object definitionInstance) {
        for (Method method : definitionClass.getMethods()) {
            if (!method.isAnnotationPresent(ValueFormatDelegate.class)) {
                continue;
            }
            ValueFormatDelegate annotation = method.getAnnotation(ValueFormatDelegate.class);

            validateDelegateCommon(method);
            ReportValueFormat format = findElementInListByUniqueName(
                    formats,
                    getValueName(annotation.band(), annotation.field()),
                    ReportValueFormat::getValueName,
                    annotation
            );

            if (CustomValueFormatter.class.equals(method.getReturnType())) {
                CustomValueFormatter delegate = obtainDelegateFromDefinition(definitionInstance, method, CustomValueFormatter.class);
                format.setCustomFormatter(delegate);
            } else {
                throw new RuntimeException(String.format("Unsupported return type for delegate: %s", method));
            }
        }

        // validate that all objects have some sort of format value assigned
        for (ReportValueFormat format : formats) {
            if (format.getFormatString() == null && format.getCustomFormatter() == null) {
                throw new RuntimeException(String.format(
                        "Value Format '%s' must have either format string or formatter delegate assigned", format.getValueName()));
            }
        }
    }

    private <E, A extends Annotation> E findElementInListByUniqueName(List<E> elements, String name,
                                                                      Function<E, String> elementNameExtractor, A annotation) {
        return elements.stream()
                .filter(t -> name.equals(elementNameExtractor.apply(t)))
                .findAny()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Report definition doesn't contain element with name '%s', requested by annotation '%s'",
                                name, annotation)
                ));
    }

    protected <T> T obtainDelegateFromDefinition(Object reportDefinition, Method method, Class<T> delegateClass) {
        T delegate = delegateClass.cast(ReflectionUtils.invokeMethod(method, reportDefinition));
        validateDelegateInstanceCommon(method, delegate);
        return delegate;
    }

    protected void validateDelegateInstanceCommon(Method method, @Nullable Object delegate) {
        if (delegate == null) {
            throw new RuntimeException(String.format("Delegate declaration method returned null: %s", method));
        }
    }
}
