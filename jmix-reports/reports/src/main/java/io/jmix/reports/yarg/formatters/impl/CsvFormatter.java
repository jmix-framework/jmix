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

import com.opencsv.CSVWriter;
import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.exception.UnsupportedFormatException;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportOutputType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static io.jmix.reports.yarg.formatters.impl.csv.SimpleSeparatorDetector.detectSeparator;

public class CsvFormatter extends AbstractFormatter {
    protected char separator;
    protected String[] header;
    protected List<String> parametersToInsert = new ArrayList<>();

    public CsvFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.csv);
        readTemplateData();
    }

    @Override
    public void renderDocument() {
        if (ReportOutputType.csv.equals(outputType)) {
            writeCsvDocument(rootBand, outputStream);
        } else {
            throw new UnsupportedFormatException();
        }
    }

    protected void writeCsvDocument(BandData rootBand, OutputStream outputStream) {
        try {
            List<BandData> actualData = getActualData(rootBand);
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), separator,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            writer.writeNext(header);

            for (BandData row : actualData) {
                String[] entries = new String[parametersToInsert.size()];
                for (int i = 0; i < parametersToInsert.size(); i++) {
                    String parameterName = parametersToInsert.get(i);
                    String fullParameterName = row.getName() + "." + parameterName;
                    entries[i] = formatValue(row.getData().get(parameterName), parameterName, fullParameterName);
                }
                writer.writeNext(entries);
            }

            writer.close();
        } catch (IOException e) {
            throw new ReportFormattingException("Error while writing a csv document", e);
        }
    }

    protected List<BandData> getActualData(BandData rootBand) {
        List<BandData> resultData = new ArrayList<>();
        Map<String, List<BandData>> childrenBands = rootBand.getChildrenBands();

        if (childrenBands != null && !childrenBands.isEmpty()) {
            childrenBands.forEach((s, bandDataList) -> bandDataList.forEach(bandData -> {
                if (bandData.getData() != null && !bandData.getData().isEmpty()) {
                    resultData.add(bandData);
                }
            }));
        }

        return resultData;
    }

    protected void readTemplateData() {
        checkThreadInterrupted();
        InputStream documentContent = reportTemplate.getDocumentContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(documentContent, StandardCharsets.UTF_8));

        StringBuilder headerData = new StringBuilder();
        try {
            String line;
            while ((line = in.readLine()) != null) {
                checkThreadInterrupted();
                Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(line);
                if (!matcher.find())
                    headerData.append(line);
                else {
                    separator = detectSeparator(line);
                    matcher.reset();
                    while (matcher.find()) {
                        String parameterName = unwrapParameterName(matcher.group());
                        parametersToInsert.add(parameterName);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            throw new ReportFormattingException("Error while reading template data");
        }

        header = headerData.toString().replaceAll(String.valueOf(CSVWriter.DEFAULT_QUOTE_CHARACTER), "").split(String.valueOf(separator));
    }
}