/*
 * Copyright 2022 Haulmont.
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

package io.jmix.reportsflowui.view.template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.common.util.Preconditions;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;

import javax.annotation.Nullable;

public abstract class AbstractDescriptionEditFragment<C extends Component> extends Composite<C> {

    protected ReportTemplate reportTemplate;
    protected VerticalLayout previewBox;
    protected Dialog target;

    @Nullable
    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public void setReportTemplate(@Nullable ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    public Dialog getTarget() {
        return target;
    }

    public void setTarget(Dialog target) {
        Preconditions.checkNotNullArgument(target);
        this.target = target;
    }

    public VerticalLayout getPreviewBox() {
        return previewBox;
    }

    public void setPreviewBox(VerticalLayout previewBox) {
        Preconditions.checkNotNullArgument(previewBox);
        this.previewBox = previewBox;
    }

    public void showPreview() {
        previewBox.setVisible(true);
        previewBox.setSizeFull();
        previewBox.removeAll();

        target.setResizable(true);
        target.setWidth("80em");
//        target.center();

        initPreviewContent(previewBox);
    }

    public void hidePreview() {
        previewBox.setVisible(false);
        previewBox.removeAll();

        target.setSizeUndefined();
        target.setResizable(false);
//        parent.center();
    }

    public abstract boolean isSupportPreview();

    public abstract boolean isApplicable(ReportOutputType reportOutputType);

    public abstract boolean applyChanges();

    protected abstract void initPreviewContent(VerticalLayout previewBox);
}
