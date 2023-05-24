package io.jmix.reportsflowui.view.template.edit;

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
