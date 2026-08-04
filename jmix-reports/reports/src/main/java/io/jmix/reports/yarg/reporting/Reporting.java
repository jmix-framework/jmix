/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.reporting;

import com.google.common.base.Preconditions;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;
import io.jmix.reports.yarg.exception.ValidationException;
import io.jmix.reports.yarg.formatters.ReportFormatter;
import io.jmix.reports.yarg.formatters.StreamingReportFormatter;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.factory.ReportFormatterFactory;
import io.jmix.reports.yarg.formatters.impl.streaming.StreamingBandFeed;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.StreamingReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.structure.*;
import org.jspecify.annotations.Nullable;
import io.jmix.reports.yarg.util.converter.ObjectToStringConverter;
import io.jmix.reports.yarg.util.converter.ObjectToStringConverterImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Reporting implements ReportingAPI {

    protected ReportFormatterFactory formatterFactory;
    protected DataExtractor dataExtractor;
    protected ReportLoaderFactory loaderFactory;
    protected StreamingReportValidator streamingValidator = new StreamingReportValidator();

    protected ObjectToStringConverter objectToStringConverter = new ObjectToStringConverterImpl();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public void setFormatterFactory(ReportFormatterFactory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    public void setLoaderFactory(ReportLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
        if (loaderFactory != null && dataExtractor == null) {
            dataExtractor = new DataExtractorImpl(loaderFactory);
        }
    }

    public void setDataExtractor(DataExtractorImpl dataExtractor) {
        this.dataExtractor = dataExtractor;
    }

    public void setObjectToStringConverter(ObjectToStringConverter objectToStringConverter) {
        this.objectToStringConverter = objectToStringConverter;
    }

    @Override
    @NullMarked
    public ReportOutputDocument runReport(RunParams runParams, OutputStream outputStream) {
        Report report = runParams.report;
        ReportTemplate reportTemplate = runParams.reportTemplate;
        Map<String, Object> params = runParams.params;
        ReportOutputType outputType = runParams.outputType;

        try {
            Preconditions.checkNotNull(report, "\"report\" parameter can not be null");
            Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" can not be null");
            Preconditions.checkNotNull(params, "\"params\" can not be null");
            Preconditions.checkNotNull(outputStream, "\"outputStream\" can not be null");

            Map<String, Object> handledParams = handleParameters(report, params);
            logReport("Started report [%s] with parameters [%s]", report, handledParams);

            ReportOutputType finalOutputType = (outputType != null) ? outputType : reportTemplate.getOutputType();
            ReportBand streamingBand = findStreamingBand(report.getRootBand());
            String templateExtension = StringUtils.substringAfterLast(reportTemplate.getDocumentName(), ".");
            BandData rootBand = null;
            if (streamingBand != null && !reportTemplate.isCustom()
                    && formatterFactory.supportsStreaming(templateExtension)) {
                rootBand = runStreamingReport(report, reportTemplate, finalOutputType, outputStream,
                        handledParams, streamingBand);
            } else if (streamingBand != null) {
                // The streaming flag is set, but this template cannot be streamed (e.g. a custom template
                // or a non-xlsx type such as .xlsm, whose macros the SXSSF engine cannot preserve). Fall
                // back to fully materialized rendering, but do not do it silently: for the large data sets
                // streaming targets, materialization can exhaust the heap.
                logger.warn("Report [{}] has a streaming band [{}] but its template [{}] cannot be rendered "
                                + "by the streaming engine ({}); FALLING BACK to fully materialized band data — "
                                + "the streaming flag is IGNORED and memory usage is NOT bounded, so a large data "
                                + "set may exhaust the heap (OutOfMemoryError). To keep memory bounded, use a "
                                + "non-custom .xlsx template.",
                        report.getName(), streamingBand.getName(), reportTemplate.getDocumentName(),
                        reportTemplate.isCustom()
                                ? "custom template"
                                : "unsupported template type [" + templateExtension + "]");
            }
            if (rootBand == null) {
                // Streaming not applicable (non-streaming template or formatter, custom template,
                // no streaming flag): render with fully materialized band data.
                rootBand = loadBandData(report, handledParams);
                generateReport(report, reportTemplate, finalOutputType, outputStream, handledParams, rootBand);
            }

            logReport("Finished report [%s] with parameters [%s]", report, handledParams);

            String outputName = resolveOutputFileName(runParams, rootBand);
            return createReportOutputDocument(report, finalOutputType, outputName, rootBand);
        } catch (ReportingInterruptedException e) {
            logReport("Report is canceled by user request. Report [%s] with parameters [%s].", report, params);
            throw e;
        } catch (ReportingException e) {
            logReport("An error occurred while running report [%s] with parameters [%s].", report, params);
            logException(e);
            //validation exception is usually shown to clients, so probably there is no need to add report name there (to keep the original message)
            if (!(e instanceof ValidationException)) {
                e.setReportDetails(format(" Report name [%s]", report.getName()));
            }
            throw e;
        }
    }

    @Override
    @NullMarked
    public ReportOutputDocument runReport(RunParams runParams) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ReportOutputDocument reportOutputDocument = runReport(runParams, result);
        reportOutputDocument.setContent(result.toByteArray());
        return reportOutputDocument;
    }

    protected void generateReport(Report report, ReportTemplate reportTemplate, ReportOutputType outputType,
                                  OutputStream outputStream, Map<String, Object> handledParams, BandData rootBand) {
        String extension = StringUtils.substringAfterLast(reportTemplate.getDocumentName(), ".");
        if (reportTemplate.isCustom()) {
            try {
                byte[] bytes = reportTemplate.getCustomReport().createReport(report, rootBand, handledParams);
                IOUtils.write(bytes, outputStream);
            } catch (IOException e) {
                throw new ReportingException(format("An error occurred while processing custom template [%s].", reportTemplate.getDocumentName()), e);
            }
        } else {
            FormatterFactoryInput factoryInput =
                    new FormatterFactoryInput(extension, rootBand, reportTemplate, outputType, outputStream);
            ReportFormatter formatter = formatterFactory.createFormatter(factoryInput);
            formatter.renderDocument();
        }
    }

    @Nullable
    protected ReportBand findStreamingBand(@Nullable ReportBand band) {
        if (band == null) {
            return null;
        }

        if (Boolean.TRUE.equals(band.getStreaming())) {
            return band;
        }

        for (ReportBand child : band.getChildren()) {
            ReportBand found = findStreamingBand(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Streaming run path: small bands are extracted as usual (the streaming band excluded), then the
     * render runs inside the streaming loader's callback, pulling the hot band's rows from a live cursor.
     * The loader owns the transaction/connection for the whole render.
     *
     * <p>Returns {@code null} when the formatter factory claims streaming support for the template but
     * actually created a non-streaming formatter (a customized factory): the caller then falls back to
     * the materialized rendering path.
     */
    @Nullable
    protected BandData runStreamingReport(Report report, ReportTemplate reportTemplate,
                                          ReportOutputType outputType, OutputStream outputStream,
                                          Map<String, Object> handledParams, ReportBand streamingBand) {
        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>(handledParams));
        rootBand.addReportFieldFormats(report.getReportFieldFormats());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        String extension = StringUtils.substringAfterLast(reportTemplate.getDocumentName(), ".");
        FormatterFactoryInput factoryInput =
                new FormatterFactoryInput(extension, rootBand, reportTemplate, outputType, outputStream, true);
        ReportFormatter formatter = formatterFactory.createFormatter(factoryInput);

        if (!(formatter instanceof StreamingReportFormatter streamingFormatter)) {
            logger.warn("Report formatter factory declared streaming support for the [{}] template of report [{}] "
                            + "but created a non-streaming formatter [{}]; falling back to materialized rendering — "
                            + "the streaming flag is ignored and memory usage is not bounded",
                    extension, report.getName(), formatter.getClass().getName());
            return null;
        }

        Preconditions.checkNotNull(loaderFactory,
                "Report [%s] has a streaming band but no loader factory is configured; "
                        + "call Reporting.setLoaderFactory before running streaming reports", report.getName());

        List<StreamingReportValidator.Violation> violations =
                streamingValidator.validate(report.getRootBand(), loaderFactory);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(StreamingReportValidator.Violation::describe)
                    .collect(Collectors.joining("; "));
            throw new ReportingException(
                    "Report cannot be rendered by the streaming XLSX engine: " + details);
        }
        streamingFormatter.setReportBandNames(collectReportBandNames(report.getRootBand()));

        ReportQuery query = streamingBand.getReportQueries().get(0);
        ReportDataLoader loader = loaderFactory.createDataLoader(query.getLoaderType());
        if (!(loader instanceof StreamingReportDataLoader streamingLoader)) {
            throw new ReportingException(format(
                    "Streaming band [%s] uses loader type [%s] which does not support streaming; use sql or jpql",
                    streamingBand.getName(), query.getLoaderType()));
        }

        dataExtractor.extractData(report, handledParams, rootBand, Set.of(streamingBand.getName()));

        // The connection is held only while the feed is consumed; the result is written after
        // the loader releases the cursor.
        StreamingBandFeed feed;
        try {
            feed = streamingLoader.loadDataStreaming(query, rootBand, handledParams, rows -> {
                StreamingBandFeed bandFeed = new StreamingBandFeed(
                        streamingBand.getName(), rows, rootBand, dataExtractor.getPutEmptyRowIfNoDataSelected());
                streamingFormatter.setStreamingBandFeed(bandFeed);
                streamingFormatter.consumeData();
                return bandFeed;
            });
        } catch (RuntimeException | Error e) {
            // The loader may fail in teardown after a successful render; drop the spooled result
            // so gigabytes of SXSSF temp files do not outlive the run.
            streamingFormatter.discard();
            throw e;
        }

        streamingFormatter.completeRendering();
        // The fed rows are never attached to the band tree; keep just the first one so that
        // output file name patterns referencing the streaming band still resolve.
        if (feed != null && feed.getFirstRow() != null) {
            rootBand.addChild(feed.getFirstRow());
        }

        return rootBand;
    }

    protected Set<String> collectReportBandNames(ReportBand band) {
        Set<String> names = new HashSet<>();
        names.add(band.getName());
        for (ReportBand child : band.getChildren()) {
            names.addAll(collectReportBandNames(child));
        }
        return names;
    }

    protected BandData loadBandData(Report report, Map<String, Object> handledParams) {
        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>(handledParams));
        rootBand.addReportFieldFormats(report.getReportFieldFormats());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        dataExtractor.extractData(report, handledParams, rootBand);
        return rootBand;
    }

    protected Map<String, Object> handleParameters(Report report, Map<String, Object> params) {
        Map<String, Object> handledParams = new HashMap<String, Object>(params);
        for (ReportParameter reportParameter : report.getReportParameters()) {
            String paramName = reportParameter.getAlias();

            Object parameterValue = handledParams.get(paramName);
            if (parameterValue == null && reportParameter instanceof ReportParameterWithDefaultValue parameterWithDefault) {
                DefaultValueProvider<?> provider = parameterWithDefault.getDefaultValueProvider();
                if (provider != null) {
                    parameterValue = provider.getDefaultValue(reportParameter);
                    handledParams.put(paramName, parameterValue);
                } else {
                    String parameterDefaultValue = parameterWithDefault.getDefaultValue();
                    if (parameterDefaultValue != null) {
                        parameterValue = objectToStringConverter.convertFromString(reportParameter.getParameterClass(), parameterDefaultValue);
                        handledParams.put(paramName, parameterValue);
                    }
                }
            }

            if (Boolean.TRUE.equals(reportParameter.getRequired()) && parameterValue == null) {
                throw new ReportParametersValidationException(format("Required report parameter \"%s\" not found", paramName));
            }

            if (!handledParams.containsKey(paramName)) {//make sure map contains all user parameters, even if value == null
                handledParams.put(paramName, null);
            }
        }

        return handledParams;
    }

    protected void logReport(String caption, Report report, Map<String, Object> params) {
        StringBuilder parametersString = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            parametersString.append("\n").append(entry.getKey()).append(":").append(entry.getValue());
        }
        logger.info(format(caption, report.getName(), parametersString));
    }

    protected void logException(ReportingException e) {
        logger.info("Trace: ", e);
    }

    protected ReportOutputDocument createReportOutputDocument(Report report, ReportOutputType outputType, String outputName, BandData rootBand) {
        return new ReportOutputDocumentImpl(report, null, outputName, outputType);
    }

    protected String resolveOutputFileName(RunParams runParams, BandData rootBand) {
        ReportTemplate reportTemplate = runParams.reportTemplate;
        ReportOutputType outputType = runParams.outputType;
        String outputNamePattern = reportTemplate.getOutputNamePattern();
        if (StringUtils.isNotEmpty(runParams.outputNamePattern)) {
            outputNamePattern = runParams.outputNamePattern;
        }
        String outputName = reportTemplate.getDocumentName();
        Pattern pattern = Pattern.compile("\\$\\{([A-z0-9_]+)\\.([A-z0-9_]+)\\}");
        if (StringUtils.isNotBlank(outputNamePattern)) {
            Matcher matcher = pattern.matcher(outputNamePattern);
            if (matcher.find()) {
                String bandName = matcher.group(1);
                String paramName = matcher.group(2);

                BandData bandWithFileName = null;
                if (BandData.ROOT_BAND_NAME.equals(bandName)) {
                    bandWithFileName = rootBand;
                } else {
                    bandWithFileName = rootBand.findBandRecursively(bandName);
                }

                if (bandWithFileName != null) {
                    Object fileName = bandWithFileName.getData().get(paramName);

                    if (fileName == null) {
                        throw new ReportingException(
                                format("No data in band [%s] parameter [%s] found. " +
                                        "This band and parameter is used for output file name generation.", bandWithFileName, paramName));
                    } else {
                        outputName = matcher.replaceFirst(fileName.toString());
                    }
                } else {
                    throw new ReportingException(format("No data in band [%s] found.This band is used for output file name generation.", bandName));
                }
            } else {
                outputName = outputNamePattern;
            }
        }

        if (ReportOutputType.custom != reportTemplate.getOutputType()) {
            ReportOutputType finalOutputType = (outputType != null) ? outputType : reportTemplate.getOutputType();
            outputName = format("%s.%s", StringUtils.substringBeforeLast(outputName, "."), finalOutputType.getId());
        }

        return outputName;
    }
}