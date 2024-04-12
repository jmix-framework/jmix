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

import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.XlsxFormatter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xlsx4j.sml.Cell;

@Component("report_JmixXlsxFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixXlsxFormatter extends XlsxFormatter {

    public JmixXlsxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    @Override
    protected Cell copyCell(Cell cell) {
        Object parent = cell.getParent();
        try {
            cell.setParent(null);
            return super.copyCell(cell);
        } finally {
            cell.setParent(parent);
        }
    }
}
