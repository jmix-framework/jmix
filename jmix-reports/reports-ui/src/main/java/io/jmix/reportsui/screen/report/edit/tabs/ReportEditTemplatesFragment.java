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

package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.UuidProvider;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UiController("report_ReportEditTemplates.fragment")
@UiDescriptor("report-edit-templates-fragment.xml")
public class ReportEditTemplatesFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    protected CollectionPropertyContainer<ReportTemplate> templatesDc;

    @Autowired
    protected Table<ReportTemplate> templatesTable;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Install(to = "templatesTable.create", subject = "afterCommitHandler")
    protected void templatesTableCreateAfterCommitHandler(ReportTemplate reportTemplate) {
        Report report = reportDc.getItem();
        ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate == null) {
            report.setDefaultTemplate(reportTemplate);
        }
    }

    @Install(to = "templatesTable.remove", subject = "afterActionPerformedHandler")
    protected void templatesTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ReportTemplate> event) {
        List<ReportTemplate> selected = event.getItems();

        Report report = reportDc.getItem();
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate != null && selected.contains(defaultTemplate)) {
            ReportTemplate newDefaultTemplate = null;

            if (templatesDc.getItems().size() == 1) {
                newDefaultTemplate = templatesDc.getItems().iterator().next();
            }

            report.setDefaultTemplate(newDefaultTemplate);
        }
    }

    @Install(to = "templatesTable.copy", subject = "enabledRule")
    protected boolean templatesTableCopyEnabledRule() {
        if (templatesTable != null) {
            Object selectedItem = templatesTable.getSingleSelected();
            return selectedItem != null && isUpdatePermitted();

        }
        return false;
    }

    @Subscribe("templatesTable.copy")
    protected void onTemplatesTableCopy(Action.ActionPerformedEvent event) {
        ReportTemplate template = templatesTable.getSingleSelected();
        if (template != null) {

            ReportTemplate copy = metadataTools.copy(template);
            copy.setId(UuidProvider.createUuid());
            copy.setVersion(null);

            String copyNamingPattern = messageBundle.getMessage("template.copyNamingPattern");
            String copyCode = String.format(copyNamingPattern, StringUtils.isEmpty(copy.getCode())
                    ? StringUtils.EMPTY
                    : copy.getCode());

            List<String> codes = templatesDc.getItems().stream()
                    .map(ReportTemplate::getCode)
                    .filter(o -> !StringUtils.isEmpty(o))
                    .collect(Collectors.toList());
            if (codes.contains(copyCode)) {
                String code = copyCode;
                int i = 0;
                while ((codes.contains(code))) {
                    i += 1;
                    code = copyCode + " " + i;
                }
                copyCode = code;
            }
            copy.setCode(copyCode);

            templatesDc.getMutableItems().add(copy);
        }
    }


    @Install(to = "templatesTable.default", subject = "enabledRule")
    protected boolean templatesTableDefaultEnabledRule() {
        if (templatesTable != null) {
            Object selectedItem = templatesTable.getSingleSelected();
            if (selectedItem != null) {
                return !Objects.equals(reportDc.getItem().getDefaultTemplate(), selectedItem) && isUpdatePermitted();
            }
        }

        return false;
    }

    @Subscribe("templatesTable.default")
    protected void onTemplatesTableDefault(Action.ActionPerformedEvent event) {
        ReportTemplate template = templatesTable.getSingleSelected();
        if (template != null) {
            reportDc.getItem().setDefaultTemplate(template);
        }
        event.getSource().refreshState();

        templatesTable.focus();
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }
}
