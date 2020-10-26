/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reportsui.gui.template.edit.pivottable.aggregation.edit;

import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import io.jmix.reports.entity.pivottable.PivotTableAggregation;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.ValidationErrors;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Objects;

public class PivotTableAggregationEdit extends AbstractEditor<PivotTableAggregation> {

    @WindowParam
    protected Collection<PivotTableAggregation> existingItems;
    @Autowired
    protected SourceCodeEditor sourceCodeEditor;

    @Override
    protected void postInit(){
        super.postInit();
        sourceCodeEditor.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("pivotTable.functionHelpCaption"), getMessage("pivotTable.aggregationFunctionHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(560f)));
    }

    @Override
    protected boolean preCommit() {
        if (super.preCommit()) {
            PivotTableAggregation aggregation = getItem();
            boolean hasMatches = existingItems.stream().
                    anyMatch(e -> !Objects.equals(aggregation, e) &&
                            Objects.equals(aggregation.getCaption(), e.getCaption()));
            if (hasMatches) {
                ValidationErrors validationErrors = new ValidationErrors();
                validationErrors.add(getMessage("pivotTableEdit.uniqueAggregationOptionCaption"));
                showValidationErrors(validationErrors);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean commit() {
        return true;
    }
}
