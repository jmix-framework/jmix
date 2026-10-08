/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.impl;

import io.jmix.core.Metadata;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.libintegration.JmixFormatterFactory;
import io.jmix.reports.libintegration.JmixStreamingXlsxFormatter;
import io.jmix.reports.libintegration.JmixXlsxFormatter;
import io.jmix.reports.yarg.formatters.ReportFormatter;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class StreamingFormatterSelectionTest {

    @Autowired
    protected JmixFormatterFactory formatterFactory;

    @Autowired
    protected Metadata metadata;

    @Test
    void testStreamingInputSelectsStreamingFormatter() {
        ReportFormatter formatter = formatterFactory.createFormatter(factoryInput(true));

        assertThat(formatter).isInstanceOf(JmixStreamingXlsxFormatter.class);
    }

    @Test
    void testNonStreamingInputSelectsDefaultFormatter() {
        ReportFormatter formatter = formatterFactory.createFormatter(factoryInput(false));

        assertThat(formatter).isInstanceOf(JmixXlsxFormatter.class);
    }

    protected FormatterFactoryInput factoryInput(boolean streaming) {
        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setContent(new byte[0]);
        return new FormatterFactoryInput("xlsx", rootBand, template, ReportOutputType.xlsx,
                new ByteArrayOutputStream(), streaming);
    }
}
