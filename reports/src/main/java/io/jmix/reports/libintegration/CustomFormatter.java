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
package io.jmix.reports.libintegration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.haulmont.yarg.formatters.CustomReport;
import com.haulmont.yarg.structure.BandData;
import io.jmix.core.ClassManager;
import io.jmix.core.CoreProperties;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.entity.CustomTemplateDefinedBy;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.ReportingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Component("report_CustomFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CustomFormatter implements CustomReport {
    private static final Logger log = LoggerFactory.getLogger(CustomFormatter.class);

    @Autowired
    protected ScriptEvaluator scriptEvaluator;

    @Autowired
    protected ClassManager classManager;

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected ApplicationContext applicationContext;

    public static final String PARAMS = "params";
    private static final String ROOT_BAND = "rootBand";
    private static final String PATH_GROOVY_FILE = "(\\w[\\w\\d_-]*/)*(\\w[\\w\\d-_]*\\.groovy)";

    protected static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(3,
                    new ThreadFactoryBuilder()
                            .setNameFormat("ReportCustomFormatter-%d")
                            .build()
            );

    protected Report report;
    protected ReportTemplate template;
    protected Map<String, Object> params;


    public CustomFormatter(Report report, ReportTemplate template) {
        this.report = report;
        this.template = template;
    }

    @Override
    public byte[] createReport(com.haulmont.yarg.structure.Report report, BandData rootBand, Map<String, Object> params) {
        this.params = params;//we set params here because they might change inside YARG (for instance - default values)
        return createDocument(rootBand);
    }

    public byte[] createDocument(BandData rootBand) {
        String customDefinition = template.getCustomDefinition();
        CustomTemplateDefinedBy definedBy = template.getCustomDefinedBy();
        if (CustomTemplateDefinedBy.CLASS == definedBy) {
            return generateReportWithClass(rootBand, customDefinition);
        } else if (CustomTemplateDefinedBy.SCRIPT == definedBy) {
            return generateReportWithScript(rootBand, customDefinition);
        } else if (CustomTemplateDefinedBy.URL == definedBy) {
            return generateReportWithUrl(rootBand, customDefinition);
        } else {
            throw new ReportingException(
                    format("The value of \"Defined by\" field is not supported [%s]", definedBy));
        }
    }

    protected byte[] generateReportWithClass(BandData rootBand, String customDefinition) {
        Class clazz = classManager.loadClass(customDefinition);
        try {
            CustomReport customReport = (CustomReport) clazz.newInstance();
            return customReport.createReport(report, rootBand, params);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReportingException(
                    format("Could not instantiate class for custom template [%s]. Report name [%s]",
                            template.getCustomDefinition(), report.getName()), e);
        }
    }

    protected byte[] generateReportWithScript(BandData rootBand, String customDefinition) {
        Object result;

        if (customDefinition.startsWith("/")) {
            customDefinition = StringUtils.removeStart(customDefinition, "/");
        }

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put(PARAMS, params);
        scriptParams.put(ROOT_BAND, rootBand);
        scriptParams.put("applicationContext", applicationContext);

        if (Pattern.matches(PATH_GROOVY_FILE, customDefinition)) {
            result = scriptEvaluator.evaluate(new ResourceScriptSource(new ClassPathResource(customDefinition)), scriptParams);
        } else {
            result = scriptEvaluator.evaluate(new StaticScriptSource(customDefinition), scriptParams);
        }

        if (result == null) {
            throw new ReportingException(
                    format("Result returned from custom report [%s] is null " +
                            "but only byte[] and strings are supported", customDefinition));
        }

        if (result instanceof byte[]) {
            return (byte[]) result;
        } else if (result instanceof CharSequence) {
            return result.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            throw new ReportingException(
                    format("Result returned from custom report is of type %s " +
                            "but only byte[] and strings are supported", result.getClass()));
        }
    }

    protected byte[] generateReportWithUrl(BandData rootBand, String customDefinition) {
        Map<String, Object> convertedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof Date) {
                convertedParams.put(entry.getKey(), new FormattedDate((Date) entry.getValue()));
            } else {
                convertedParams.put(entry.getKey(), entry.getValue());
            }
        }

        convertedParams.put(ROOT_BAND, rootBand);

        Object scriptResult = scriptEvaluator.evaluate(new StaticScriptSource("return \"" + customDefinition + "\""), convertedParams);

        if (scriptResult == null) {
            throw new ReportingException(
                    format("Can not get generated URL returned from custom report [%s]", customDefinition));
        }

        String url = scriptResult.toString();
        try {
            Future<byte[]> future = executor.submit(() ->
                    doReadBytesFromUrl(url)
            );

            byte[] bytes = future.get(reportsProperties.getCurlTimeout(), TimeUnit.SECONDS);

            return bytes;
        } catch (InterruptedException e) {
            throw new ReportingException(format("Reading data from url [%s] has been interrupted", url), e);
        } catch (ExecutionException e) {
            throw new ReportingException(format("An error occurred while reading data from url [%s]", url), e);
        } catch (TimeoutException e) {
            throw new ReportingException(format("Reading data from url [%s] has been terminated by timeout", url), e);
        }
    }

    protected static class FormattedDate extends Date {
        private static final long serialVersionUID = 6328140953372636008L;
        private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss aaa";

        public FormattedDate(Date date) {
            super(date.getTime());
        }

        @Override
        public String toString() {
            return new SimpleDateFormat(DATE_FORMAT).format(this);
        }
    }

    protected byte[] doReadBytesFromUrl(String url) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        Process proc = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            String curlToolPath = reportsProperties.getCurlPath();
            String curlToolParams = reportsProperties.getCurlParams();
            String command = format("%s %s %s", curlToolPath, curlToolParams, url);
            log.info("Reporting::CustomFormatter::Trying to load report from URL: [{}]", url);
            proc = runtime.exec(command);

            inputStream = proc.getInputStream();

            String tmpFileName = coreProperties
                    .getTempDir() + "/" + RandomStringUtils.randomAlphanumeric(12);

            outputStream = new FileOutputStream(tmpFileName);
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(outputStream);

            File tempFile = new File(tmpFileName);
            byte[] bytes = FileUtils.readFileToByteArray(tempFile);
            FileUtils.deleteQuietly(tempFile);

            return bytes;
        } catch (IOException e) {
            throw new ReportingException(format("Error while accessing remote url: [%s].", url), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);

            if (proc != null) {
                proc.destroy();
            }
        }
    }
}