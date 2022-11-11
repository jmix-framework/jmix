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

package io.jmix.reportsui.screen.template.edit.pivottable.aggregation.edit;

import io.jmix.core.Messages;
import io.jmix.reports.entity.pivottable.AggregationMode;
import io.jmix.reports.entity.pivottable.PivotTableAggregation;
import io.jmix.ui.Dialogs;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Objects;

@UiController("report_PivotTableAggregation.edit")
@UiDescriptor("pivot-table-aggregation-edit.xml")
@EditedEntityContainer("aggregationDc")
public class PivotTableAggregationEdit extends StandardEditor<PivotTableAggregation> {

    @WindowParam
    protected Collection<PivotTableAggregation> existingItems;
    @Autowired
    protected SourceCodeEditor sourceCodeEditor;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected ScreenValidation screenValidation;

    @Install(to = "sourceCodeEditor", subject = "contextHelpIconClickHandler")
    protected void sourceCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("pivotTable.functionHelpCaption"))
                .withMessage(messageBundle.getMessage("pivotTable.aggregationFunctionHelp"))
                .withModal(false)
                .withWidth("560px")
                .withContentMode(ContentMode.HTML)
                .show();
    }

    @Subscribe(id = "aggregationDc", target = Target.DATA_CONTAINER)
    public void onAggregationDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<PivotTableAggregation> event) {
        if (StringUtils.equals(event.getProperty(), "mode")) {
            AggregationMode mode = getEditedEntity().getMode();
            String caption = mode != null ? messages.getMessage(mode) : null;
            getEditedEntity().setCaption(caption);
        }
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (!event.isCommitPrevented()) {
            PivotTableAggregation aggregation = getEditedEntity();
            boolean hasMatches = existingItems.stream().
                    anyMatch(e -> !Objects.equals(aggregation, e) &&
                            Objects.equals(aggregation.getCaption(), e.getCaption()));
            if (hasMatches) {
                ValidationErrors validationErrors = new ValidationErrors();
                validationErrors.add(messageBundle.getMessage("pivotTableEdit.uniqueAggregationOptionCaption"));

                screenValidation.showValidationErrors(this, validationErrors);
                event.preventCommit();
            }
        }
    }
}
