/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplatesui.screen.emailtemplate.json;


import io.jmix.core.CoreProperties;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.emailtemplates.TemplateConverter;
import io.jmix.emailtemplates.entity.JsonEmailTemplate;
import io.jmix.emailtemplates.entity.TemplateBlock;
import io.jmix.emailtemplatesui.screen.html.HtmlSourceCodeScreen;
import io.jmix.grapesjs.component.GjsBlock;
import io.jmix.grapesjs.component.GrapesJsHtmlEditor;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.FileUploadField;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UiController("emltmp_JsonEmailTemplate.edit")
@UiDescriptor("json-email-template-edit.xml")
@EditedEntityContainer("emailTemplateDc")
public class JsonEmailTemplateEdit extends StandardEditor<JsonEmailTemplate> {

    @Autowired
    private CollectionContainer<TemplateBlock> templateBlocksDc;

    @Autowired
    protected DataLoader templateBlocksDl;

    @Autowired
    private Screens screens;

    @Autowired
    private ScreenBuilders screenBuilder;

    @Autowired
    private GrapesJsHtmlEditor templateEditor;

    @Autowired
    protected FileUploadField fileUpload;

    @Autowired
    private Downloader downloader;

    @Autowired
    private TemplateConverter templateConverter;


    @Autowired
    protected CollectionContainer<ReportInputParameter> parametersDc;

    private Report report;

    @Autowired
    private InstanceContainer<Report> reportDc;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initBlocks();
    }

    private void initBlocks() {
        templateBlocksDl.load();

        List<GjsBlock> gjsBlocks = templateBlocksDc.getItems().stream()
                .map(this::createGjsBlock)
                .collect(Collectors.toList());

        templateEditor.addBlocks(gjsBlocks);
    }

    protected GjsBlock createGjsBlock(TemplateBlock templateBlock) {
        GjsBlock gjsBlock = new GjsBlock();
        gjsBlock.setName(templateBlock.getName());
        gjsBlock.setLabel(templateBlock.getLabel());
        gjsBlock.setCategory(templateBlock.getCategory() != null ? templateBlock.getCategory().getName() : null);
        gjsBlock.setContent(templateBlock.getContent());
        if (templateBlock.getIcon() != null) {
            String icon = templateBlock.getIcon().startsWith("font-icon:") ?
                    templateBlock.getIcon().substring("font-icon:".length()).replace("_", "-") :
                    templateBlock.getIcon();
            gjsBlock.setAttributes(String.format("{\n" +
                    "              title: '%s',\n" +
                    "              class:'fa fa-%s'\n" +
                    "           }", templateBlock.getLabel(), icon.toLowerCase()));

        } else {
            gjsBlock.setAttributes(String.format("{\n" +
                    "              title: '%s'\n" +
                    "           }", templateBlock.getLabel()));
        }
        return gjsBlock;
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        report = templateConverter.convertToReport(getEditedEntity());

        reportDc.setItem(report);

        templateEditor.setValue(getEditedEntity().getHtml());

        templateEditor.addValueChangeListener(e -> {
            String value = e.getValue();
            getEditedEntity().setHtml(value);
        });
    }

    @Subscribe("fileUpload")
    protected void fileUploadOnFileUploadSucceedEvent(SingleFileUploadField.FileUploadSucceedEvent event) {
        try {
            byte[] bytes = fileUpload.getValue();
            getEditedEntity().setHtml(new String(bytes, StandardCharsets.UTF_8));
            templateEditor.setValue(getEditedEntity().getHtml());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe("exportHtml")
    public void exportHtml(Button.ClickEvent event) {
        HtmlSourceCodeScreen sourceCodeWindow = screens.create(HtmlSourceCodeScreen.class,
                OpenMode.DIALOG,
                new MapScreenOptions(ParamsMap.of("html", getEditedEntity().getHtml())));
        sourceCodeWindow.addAfterCloseListener(e -> {
            if (e.getCloseAction().equals(WINDOW_COMMIT_AND_CLOSE_ACTION)) {
                getEditedEntity().setHtml(sourceCodeWindow.getValue());
                templateEditor.setValue(sourceCodeWindow.getValue());
            }
        });
        sourceCodeWindow.show();
    }

    @Subscribe("viewHtml")
    public void viewHtml(Button.ClickEvent event) {
        String name = getEditedEntity().getName() != null ? getEditedEntity().getName() : "template";
        String html = getEditedEntity().getHtml() != null ? getEditedEntity().getHtml() : "";
        downloader.download(new ByteArrayDataProvider(html.getBytes(StandardCharsets.UTF_8), uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir()), name + ".html");
    }


    @Subscribe("exportReport")
    public void exportReport(Button.ClickEvent event) {
        Report report = templateConverter.convertToReport(getEditedEntity());

        screenBuilder.editor(Report.class, getWindow().getFrameOwner())
                .editEntity(report)
                .withOpenMode(OpenMode.NEW_TAB)
                .build()
                .show();
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreCommitEvent event) {
        getEditedEntity().setReportJson(getReportJson(reportDc.getItem()));

        List<Object> excludedEntities = new ArrayList<>();

        for (Object modifiedInstance : event.getModifiedInstances()) {
            if (isRelatedToReport(modifiedInstance)) {
                excludedEntities.add(modifiedInstance);
            }
        }
        excludedEntities.forEach(o -> event.getSource().evict(o));
    }

    private boolean isRelatedToReport(Object modifiedInstance) {
        return modifiedInstance instanceof Report || modifiedInstance instanceof ReportTemplate;
    }

    protected String getReportJson(Report report) {
        return reportsSerialization.convertToString(report);
    }
}