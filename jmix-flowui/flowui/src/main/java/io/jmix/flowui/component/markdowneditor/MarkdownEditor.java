/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.markdowneditor;

import com.google.common.base.Strings;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.markdowneditor.JmixMarkdownEditor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Consumer;

/**
 * A markdown editor field component that supports editing and rendered preview.
 * <p>
 * The component provides an Edit/Preview tab bar, a formatting toolbar with
 * common markdown actions and keyboard shortcuts, and renders the preview using
 * the built-in {@code vaadin-markdown} component.
 */
public class MarkdownEditor extends JmixMarkdownEditor
        implements SupportsValueSource<String>, SupportsValidation<String>, SupportsStatusChangeHandler<MarkdownEditor>,
        HasRequired, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected FieldDelegate<MarkdownEditor, String, String> fieldDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        initI18n();
    }

    protected void initI18n() {
        Messages messages = applicationContext.getBean(Messages.class);

        JmixMarkdownEditorI18n.Tabs tabs = new JmixMarkdownEditorI18n.Tabs()
                .setEdit(messages.getMessage("markdownEditor.i18n.tabs.edit"))
                .setPreview(messages.getMessage("markdownEditor.i18n.tabs.preview"));

        JmixMarkdownEditorI18n.Toolbar toolbar = new JmixMarkdownEditorI18n.Toolbar()
                .setAccessibleLabel(messages.getMessage("markdownEditor.i18n.toolbar.accessibleLabel"))
                .setHeading(messages.getMessage("markdownEditor.i18n.toolbar.heading"))
                .setBold(messages.getMessage("markdownEditor.i18n.toolbar.bold"))
                .setItalic(messages.getMessage("markdownEditor.i18n.toolbar.italic"))
                .setQuote(messages.getMessage("markdownEditor.i18n.toolbar.quote"))
                .setCode(messages.getMessage("markdownEditor.i18n.toolbar.code"))
                .setLink(messages.getMessage("markdownEditor.i18n.toolbar.link"))
                .setUnorderedList(messages.getMessage("markdownEditor.i18n.toolbar.unorderedList"))
                .setOrderedList(messages.getMessage("markdownEditor.i18n.toolbar.orderedList"))
                .setTaskList(messages.getMessage("markdownEditor.i18n.toolbar.taskList"))
                .setOverflow(messages.getMessage("markdownEditor.i18n.toolbar.overflow"));

        setI18n(new JmixMarkdownEditorI18n()
                .setTabs(tabs)
                .setToolbar(toolbar));
    }

    @Override
    protected void setModelValue(String newModelValue, boolean fromClient) {
        super.setModelValue(Strings.emptyToNull(newModelValue), fromClient);
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        super.setPresentationValue(Strings.nullToEmpty(newPresentationValue));
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        fieldDelegate.updateRequiredState();
    }

    @Override
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(getValue());
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<MarkdownEditor>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Override
    public Registration addValidator(Validator<? super String> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    protected void validate() {
        fieldDelegate.updateInvalidState();
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        // Method is called from constructor so delegate can be null
        if (fieldDelegate != null) {
            fieldDelegate.setInvalid(invalid);
        } else {
            getElement().setProperty("invalid", invalid);
        }
    }

    @Nullable
    @Override
    public ValueSource<String> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<String> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @SuppressWarnings("unchecked")
    protected FieldDelegate<MarkdownEditor, String, String> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }
}
