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
package io.jmix.reports.yarg.formatters.impl;

import com.google.common.base.Preconditions;
import io.jmix.reports.libintegration.GroovyScriptParametersProvider;
import io.jmix.reports.yarg.formatters.impl.inline.ContentInliner;
import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;
import io.jmix.reports.yarg.formatters.ReportFormatter;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportFieldFormat;
import io.jmix.reports.yarg.structure.ReportOutputType;
import io.jmix.reports.yarg.structure.ReportTemplate;
import io.jmix.reports.yarg.util.groovy.Scripting;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractFormatter implements ReportFormatter {
    public static final String SIMPLE_ALIAS_REGEXP = "\\$\\{([A-z0-9_]+?)\\}";

    public static final String ALIAS_GROUP = "([A-z0-9_\\.]+?)";
    public static final String STRING_FUNCTION_GROUP = "(\\[\\d+\\])";
    public static final String UNIVERSAL_ALIAS_REGEXP = "\\$\\{" + ALIAS_GROUP + " *" + STRING_FUNCTION_GROUP + "?\\}";
    public static final String ALIAS_WITH_BAND_NAME_REGEXP = UNIVERSAL_ALIAS_REGEXP;
    public static final String BAND_NAME_DECLARATION_REGEXP = "##band=([A-z_0-9]+) *";

    public static final Pattern UNIVERSAL_ALIAS_PATTERN = Pattern.compile(UNIVERSAL_ALIAS_REGEXP, Pattern.CASE_INSENSITIVE);
    public static final Pattern ALIAS_WITH_BAND_NAME_PATTERN = Pattern.compile(ALIAS_WITH_BAND_NAME_REGEXP);
    public static final Pattern BAND_NAME_DECLARATION_PATTERN = Pattern.compile(BAND_NAME_DECLARATION_REGEXP);
    public static final String VALUE = "value";


    protected BandData rootBand;
    protected ReportTemplate reportTemplate;
    protected ReportOutputType outputType;
    protected OutputStream outputStream;
    protected Set<ReportOutputType> supportedOutputTypes = new HashSet<>();
    protected DefaultFormatProvider defaultFormatProvider;

    @Autowired
    protected GroovyScriptParametersProvider groovyScriptParametersProvider;
    @Autowired
    protected Scripting scripting;

    /**
     * Chain of responsibility for content inliners
     */
    protected List<ContentInliner> contentInliners = new ArrayList<>();

    public void setScripting(Scripting scripting) {
        this.scripting = scripting;
    }

    protected AbstractFormatter(FormatterFactoryInput formatterFactoryInput) {
        checkNotNull(formatterFactoryInput.getRootBand(), "\"rootBand\" parameter can not be null");
        checkNotNull(formatterFactoryInput.getReportTemplate(), "\"reportTemplate\" parameter can not be null");

        this.rootBand = formatterFactoryInput.getRootBand();
        this.reportTemplate = formatterFactoryInput.getReportTemplate();
        this.outputType = (formatterFactoryInput.getOutputType() != null)
                ? formatterFactoryInput.getOutputType() : reportTemplate.getOutputType();
        this.outputStream = formatterFactoryInput.getOutputStream();
    }

    @Override
    public byte[] createDocument() {
        Preconditions.checkArgument(supportedOutputTypes.contains(outputType), String.format("%s formatter doesn't support %s output type", getClass(), outputType));
        checkThreadInterrupted();
        outputStream = new ByteArrayOutputStream();
        renderDocument();
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    public List<ContentInliner> getContentInliners() {
        return contentInliners;
    }

    public void setContentInliners(List<ContentInliner> contentInliners) {
        this.contentInliners = contentInliners;
    }

    public void setDefaultFormatProvider(DefaultFormatProvider defaultFormatProvider) {
        this.defaultFormatProvider = defaultFormatProvider;
    }

    protected String unwrapParameterName(String nameWithAlias) {
        checkThreadInterrupted();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(nameWithAlias);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    protected String formatValue(Object value, String parameterName, String fullParameterName) {
        return formatValue(value, parameterName, fullParameterName, null);
    }

    protected String formatValue(Object value, String parameterName, String fullParameterName, String stringFunction) {
        checkThreadInterrupted();
        String valueString;
        String formatString = getFormatString(parameterName, fullParameterName);
        if (formatString != null) {
            if (Boolean.TRUE.equals(isGroovyScript(parameterName, fullParameterName))) {
                Map<String, Object> params = groovyScriptParametersProvider.getParametersForFormatterParameters();
                params.put(VALUE, value);
                valueString = scripting.evaluateGroovy(formatString, params);
            } else if (formatString.startsWith("class:")) {
                String className = formatString.replaceFirst("class:", "");
                ValueFormat valueFormat;
                try {
                    Class<?> valueFormatterClass = ClassUtils.getClass(className);
                    valueFormat = (ValueFormat) ConstructorUtils.invokeConstructor(valueFormatterClass);
                } catch (ReflectiveOperationException e) {
                    throw new ReportingException("An error occurred while applying custom format", e);
                }
                valueString = valueFormat.format(value);
            } else if (value == null) {
                valueString = "";
            } else if (value instanceof Number) {
                DecimalFormat decimalFormat = new DecimalFormat(formatString);
                valueString = decimalFormat.format(value);
            } else if (value instanceof Date) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
                valueString = dateFormat.format(value);
            } else if (value instanceof TemporalAccessor) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatString);
                valueString = dateTimeFormatter.format((TemporalAccessor) value);
            } else if (value instanceof String && !formatString.startsWith("${")) {//do not use inliner alias as format string
                valueString = String.format(formatString, value);
            } else {
                valueString = value.toString();
            }
        } else {
            valueString = defaultFormat(value);
        }

        if (stringFunction != null) {
            valueString = applyStringFunction(valueString, stringFunction);
        }

        return valueString != null ? valueString : "";
    }

    protected String getFormatString(String parameterName, String fullParameterName) {
        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldFormats();
        String formatString = null;
        if (formats != null) {
            if (formats.containsKey(fullParameterName)) {
                formatString = formats.get(fullParameterName).getFormat();
            } else if (formats.containsKey(parameterName)) {
                formatString = formats.get(parameterName).getFormat();
            }
        }
        return formatString;
    }

    protected Boolean isGroovyScript(String parameterName, String fullParameterName) {
        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldFormats();
        Boolean groovyFormat = false;
        if (formats != null) {
            if (formats.containsKey(fullParameterName)) {
                groovyFormat = formats.get(fullParameterName).isGroovyScript();
            } else if (formats.containsKey(parameterName)) {
                groovyFormat = formats.get(parameterName).isGroovyScript();
            }
        }
        return groovyFormat;
    }

    protected String applyStringFunction(String valueString, String stringFunction) {
        if (stringFunction.matches(STRING_FUNCTION_GROUP)) {
            Integer index = Integer.valueOf(stringFunction.replaceAll("[^\\d]", ""));

            if (valueString.length() >= index) {
                return valueString.substring(index, index + 1);
            }
        }

        return valueString;
    }

    protected String defaultFormat(Object value) {
        if (defaultFormatProvider != null) {
            return defaultFormatProvider.format(value);
        } else {
            return value != null ? value.toString() : null;
        }
    }

    protected String insertBandDataToString(BandData bandData, String resultStr) {
        List<String> parametersToInsert = new ArrayList<>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(resultStr);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            Object value = bandData.getData().get(parameterName);
            String fullParameterName = bandData.getName() + "." + parameterName;
            String valueStr = formatValue(value, parameterName, fullParameterName);
            resultStr = inlineParameterValue(resultStr, parameterName, valueStr);
        }
        return resultStr;
    }

    protected String inlineParameterValue(String template, String parameterName, String value) {
        checkThreadInterrupted();
        String parameterRegex = UNIVERSAL_ALIAS_REGEXP.replace(ALIAS_GROUP, parameterName);
        return template.replaceAll(parameterRegex, Matcher.quoteReplacement(value));
    }

    protected boolean containsJustOneAlias(String value) {
        return !StringUtils.isBlank(value) && value.matches("\\$\\{[^\\$\\{\\}]*\\}");
    }

    protected List<String> getAllAliases(String value) {
        Pattern aliasPattern = Pattern.compile("\\$\\{[^\\$\\{\\}]*\\}");
        List<String> aliases = new ArrayList<>();
        Matcher m = aliasPattern.matcher(value);
        while (m.find()) {
            aliases.add(m.group());
        }
        return aliases;
    }

    protected BandData findBandByPath(String path) {
        if (rootBand.getName().equals(path)) return rootBand;

        String[] pathParts = path.split("\\.");
        BandData currentBand = rootBand;
        for (String pathPart : pathParts) {
            if (currentBand == null) return null;
            currentBand = currentBand.findBandRecursively(pathPart);
        }

        return currentBand;
    }

    protected BandPathAndParameterName separateBandNameAndParameterName(String alias) {
        List<String> bandPathList = new ArrayList<>();
        String[] pathParts = alias.split("\\.");
        BandData currentBand = rootBand;
        for (String pathPart : pathParts) {
            currentBand = currentBand.findBandRecursively(pathPart);
            if (currentBand != null) {
                bandPathList.add(pathPart);
            } else {
                break;
            }
        }

        if (bandPathList.isEmpty()) {
            return new BandPathAndParameterName("", alias);
        } else {
            String bandPathPart = StringUtils.join(bandPathList, ".");
            String paramNamePart = alias.replaceFirst(bandPathPart + ".", "");
            return new BandPathAndParameterName(bandPathPart, paramNamePart);
        }
    }

    protected ReportingException wrapWithReportingException(String message, Exception e) {
        if (e instanceof ReportingInterruptedException) {
            return (ReportingInterruptedException) e;
        } else {
            return new ReportFormattingException(message + ". Template name [" + reportTemplate.getDocumentName() + "]", e);
        }
    }

    protected ReportFormattingException wrapWithReportingException(String message) {
        return new ReportFormattingException(message + ". Template name [" + reportTemplate.getDocumentName() + "]");
    }

    protected static class InlinerAndMatcher {
        final ContentInliner contentInliner;
        final Matcher matcher;

        public InlinerAndMatcher(ContentInliner contentInliner, Matcher matcher) {
            this.contentInliner = contentInliner;
            this.matcher = matcher;
        }
    }

    protected InlinerAndMatcher getContentInlinerForFormat(String formatString) {
        if (formatString != null) {
            for (ContentInliner contentInliner : contentInliners) {
                Matcher matcher = contentInliner.getTagPattern().matcher(formatString);
                if (matcher.find()) {
                    return new InlinerAndMatcher(contentInliner, matcher);
                }
            }
        }
        return null;
    }

    public void checkThreadInterrupted() {
        if (Thread.interrupted()) {
            throw new ReportingInterruptedException("Formatting interrupted");
        }
    }

    public static class BandPathAndParameterName {
        protected final String bandPath;
        protected final String parameterName;

        public BandPathAndParameterName(String bandPath, String parameterName) {
            this.bandPath = bandPath;
            this.parameterName = parameterName;
        }

        public String getBandPath() {
            return bandPath;
        }

        public String getParameterName() {
            return parameterName;
        }
    }
}
