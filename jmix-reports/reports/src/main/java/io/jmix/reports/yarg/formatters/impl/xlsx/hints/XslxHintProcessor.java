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

package io.jmix.reports.yarg.formatters.impl.xlsx.hints;

import com.google.common.base.Splitter;
import io.jmix.reports.yarg.formatters.impl.xlsx.CellReference;
import io.jmix.reports.yarg.formatters.impl.xlsx.Document;
import io.jmix.reports.yarg.formatters.impl.xlsx.Range;
import io.jmix.reports.yarg.structure.BandData;
import org.xlsx4j.sml.CTDefinedName;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.DefinedNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XslxHintProcessor {
    protected static String HINT_PREFIX = "hint";
    protected static String DELIMITER = "_";
    protected List<XlsxHint> hints = new ArrayList<XlsxHint>();
    protected List<HintDescriptor> descriptors = new ArrayList<HintDescriptor>();

    public XslxHintProcessor() {

    }

    public void init(Document template, Document result) {
        initHintList(template, result);
        initDescriptors(template, result);
    }

    protected void initHintList(Document template, Document result) {
        hints.add(new CustomCellStyleXlsxHint(result));
    }

    protected void initDescriptors(Document template, Document result) {
        DefinedNames definedNames = template.getWorkbook().getDefinedNames();
        if (definedNames != null) {
            for (CTDefinedName name : definedNames.getDefinedName()) {
                if (isHintDefinedName(name.getName())) {
                    List<String> names = Splitter.on(DELIMITER).splitToList(name.getName());
                    if (names.size() > 1) {
                        String hintName = names.get(1);
                        for (XlsxHint hint : hints) {
                            if (hint.getName().equals(hintName)) {
                                HintDescriptor descriptor = new HintDescriptor();
                                descriptor.hint = hint;
                                descriptor.range = Range.fromFormula(name.getValue());
                                descriptor.params = names.size() > 2 ? names.subList(2, names.size()) : Collections.<String>emptyList();
                                descriptors.add(descriptor);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isHintDefinedName(String definedName) {
        return definedName.startsWith(HINT_PREFIX);
    }

    public void add(CellReference templateRef, Cell templateCell, Cell resultCell, BandData bandData) {
        List<HintDescriptor> filtered = new ArrayList<HintDescriptor>();
        for (HintDescriptor descriptor : descriptors) {
            if (descriptor.range.contains(templateRef)) {
                filtered.add(descriptor);
            }
        }
        if (!filtered.isEmpty()) {
            for (HintDescriptor descriptor : filtered) {
                descriptor.hint.add(templateCell, resultCell, bandData, descriptor.params);
            }
        }
    }

    public void apply() {
        for (XlsxHint hint : hints) {
            hint.apply();
        }
    }

    protected static class HintDescriptor {
        protected XlsxHint hint;
        protected Range range;
        protected List<String> params;
    }
}
