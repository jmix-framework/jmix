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

package io.jmix.messagetemplatesflowui.view.messagetemplate;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplates.entity.MessageTemplateBlock;
import io.jmix.messagetemplates.entity.TemplateType;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsBlock;
import io.jmix.messagetemplatesflowui.view.htmleditor.HtmlEditorView;
import io.jmix.messagetemplatesflowui.view.messagetemplateparameter.MessageTemplateParameterDetailView;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Route(value = "msgtmp/messagetemplate/:id", layout = DefaultMainViewParent.class)
@ViewController("msgtmp_MessageTemplate.detail")
@ViewDescriptor("message-template-detail-view.xml")
@EditedEntityContainer("messageTemplateDc")
@DialogMode(minWidth = "64em")
public class MessageTemplateDetailView extends StandardDetailView<MessageTemplate> {

    @ViewComponent
    protected GrapesJs grapesJsEditor;
    @ViewComponent
    protected FileUploadField importTemplateField;
    @ViewComponent
    protected VerticalLayout plainTextAreaLayout;
    @ViewComponent
    protected VerticalLayout grapesJsEditorLayout;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;

    protected boolean isEditPermitted;

    @Subscribe
    public void onInit(InitEvent event) {
        applySecurityConstraints();
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<MessageTemplate> event) {
        event.getEntity().setType(TemplateType.HTML);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initComponents();
        initBlocks();
        initGrapesJsEditor();

        updateContentAwareComponents();
    }

    protected void applySecurityConstraints() {
        MetaClass metaClass = metadata.getClass(MessageTemplate.class);

        UiEntityContext uiEntityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(uiEntityContext);

        isEditPermitted = uiEntityContext.isEditPermitted();
        importTemplateField.setEnabled(isEditPermitted);
    }

    protected void initComponents() {
        // to make it look like a button
        importTemplateField.setUploadText("");
        importTemplateField.getElement().setProperty("title", messageBundle.getMessage("importTemplateField.title"));
    }

    protected void initBlocks() {
        List<GrapesJsBlock> grapesJsBlocks = loadBlocks();
        grapesJsEditor.addBlocks(grapesJsBlocks);
    }

    protected void initGrapesJsEditor() {
        grapesJsEditor.setValue(getEditedEntity().getContent());
        grapesJsEditor.addValueChangeEventListener(this::onGrapesJsValueChange);
    }

    protected List<GrapesJsBlock> loadBlocks() {
        return dataManager.load(MessageTemplateBlock.class)
                .all()
                .sort(Sort.by("name"))
                .list()
                .stream()
                .map(this::blockMapper)
                .toList();
    }

    protected GrapesJsBlock blockMapper(MessageTemplateBlock block) {
        return GrapesJsBlock.create(block.getName())
                .withLabel(block.getLabel())
                .withCategory(block.getCategory())
                .withContent(block.getContent())
                .withAttributes(block.getAttributes())
                .withIcon(block.getIcon())
                .build();
    }

    protected void onGrapesJsValueChange(GrapesJs.GrapesJsValueChangedEvent event) {
        getEditedEntity().setContent(event.getValue());
    }

    @Subscribe("importTemplateField")
    public void onImportTemplateFieldUpload(FileUploadSucceededEvent<FileUploadField> event) {
        if (event.getContentLength() > 0 && event.getSource().getValue() != null) {
            String templateValue = new String(event.getSource().getValue(), UTF_8);
            grapesJsEditor.setValue(templateValue);
        }
    }

    @Subscribe("editCodeBtn")
    public void onEditCodeBtnClick(ClickEvent<JmixButton> event) {
        dialogWindows.view(this, HtmlEditorView.class)
                .withViewConfigurer(this::htmlEditorViewConfigurer)
                .withAfterCloseListener(this::htmlEditorAfterCloseListener)
                .open();
    }

    protected void htmlEditorAfterCloseListener(DialogWindow.AfterCloseEvent<HtmlEditorView> event) {
        if (event.closedWith(StandardOutcome.SAVE)) {
            String html = event.getView().getHtml();
            grapesJsEditor.setValue(html);
        }
    }

    protected void htmlEditorViewConfigurer(HtmlEditorView view) {
        view.setHtml(getEditedEntity().getContent());
        view.setReadOnly(!isEditPermitted);
    }

    @Subscribe("viewBtn")
    public void onViewBtnClick(ClickEvent<JmixButton> event) {
        ByteArrayDownloadDataProvider htmlDataProvider = new ByteArrayDownloadDataProvider(
                Strings.nullToEmpty(getEditedEntity().getContent()).getBytes(StandardCharsets.UTF_8),
                uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir()
        );

        String filename = "%s.html"
                .formatted(Objects.requireNonNullElse(getEditedEntity().getName(), "template"));

        downloader.download(htmlDataProvider, filename, DownloadFormat.HTML);
    }

    @Subscribe(id = "messageTemplateDc", target = Target.DATA_CONTAINER)
    public void onMessageTemplateDcItemPropertyChange(ItemPropertyChangeEvent<MessageTemplate> event) {
        if ("type".equals(event.getProperty())) {
            updateContentAwareComponents();
        }
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (getEditedEntity().getContent() == null) {
            event.addErrors(ValidationErrors.of(
                    messageBundle.getMessage("emptyContentValidationMessage")
            ));
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        grapesJsEditor.setReadOnly(readOnly);
    }

    protected void updateContentAwareComponents() {
        boolean isHtmlType = TemplateType.HTML.equals(getEditedEntity().getType());

        plainTextAreaLayout.setVisible(!isHtmlType);
        grapesJsEditorLayout.setVisible(isHtmlType);
    }

    @Install(to = "parametersDataGrid.create", subject = "viewConfigurer")
    public void parametersDataGridCreateViewConfigurer(MessageTemplateParameterDetailView detailView) {
        detailView.setParentTemplateParameters(getEditedEntity().getParameters());
    }

    @Install(to = "parametersDataGrid.edit", subject = "viewConfigurer")
    public void parametersDataGridEditViewConfigurer(MessageTemplateParameterDetailView detailView) {
        detailView.setParentTemplateParameters(getEditedEntity().getParameters());
    }
}
