/*
 * Copyright 2024 Haulmont.
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

import io.jmix.reports.ReportsProperties;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.DocxFormatter;
import io.jmix.reports.yarg.formatters.impl.docx.MultilineTextProcessor;
import io.jmix.reports.yarg.formatters.impl.docx.TableManager;
import io.jmix.reports.yarg.formatters.impl.docx.TextWrapper;
import org.docx4j.wml.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component("report_JmixDocxFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixDocxFormatter extends DocxFormatter {

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected MultilineTextProcessor multilineTextProcessor;

    public JmixDocxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    @Override
    protected void handleMultilineTexts() {
        if (reportsProperties.isMultilineStringsProcessingEnabled()) {
            Set<TextWrapper> texts = documentWrapper.getTexts();
            texts.forEach(textWrapper -> multilineTextProcessor.process(textWrapper));

            for (TableManager table : documentWrapper.getTables()) {
                List<Text> multilineTexts = table.getMultilineTexts();
                multilineTexts.forEach(text -> multilineTextProcessor.process(text));
            }
        }
    }


    @Override
    protected boolean isSupportedMultilineText(Text text) {
        if (!reportsProperties.isMultilineStringsProcessingEnabled()) {
            return false;
        }
        return super.isSupportedMultilineText(text);
    }
}
