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

package io.jmix.flowui.kit.component.upload;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.upload.*;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.upload.event.*;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @param <C> type of upload field
 * @param <V> value type
 */
@Tag("jmix-upload-field")
@JsModule("./src/uploadfield/jmix-upload-field.js")
public abstract class AbstractSingleUploadField<C extends AbstractSingleUploadField<C, V>, V>
        extends AbstractField<C, V>
        implements HasLabel, HasHelper, HasSize, HasStyle, HasTooltip {

    protected static final String INPUT_CONTAINER_CLASS_NAME = "jmix-upload-field-input-container";
    protected static final String FILE_NAME_COMPONENT_CLASS_NAME = "jmix-upload-field-file-name";
    protected static final String FILE_NAME_COMPONENT_EMPTY_CLASS_NAME = "empty";
    protected static final String CLEAR_COMPONENT_CLASS_NAME = "jmix-upload-field-clear";

    protected static final String FILE_NOT_SELECTED = "File is not selected";
    protected static final String UPLOAD = "Upload";
    protected static final String CLEAR_COMPONENT_ARIA_LABEL = "Remove file";

    protected JmixUpload upload;
    protected HasComponents content;

    protected Component fileNameComponent;
    protected Component clearComponent;

    protected V internalValue;

    protected String uploadText;
    protected String fileNotSelectedText;

    protected boolean clearButtonVisible = false;
    protected boolean fileNameVisible = false;

    public AbstractSingleUploadField(V defaultValue) {
        super(defaultValue);

        content = createContentComponent();
        initContentComponent(content);

        upload = createUploadComponent();
        initUploadComponent(upload);

        fileNameComponent = createFileNameComponent();
        initFileNameComponent(fileNameComponent);

        clearComponent = createClearComponent();
        initClearComponent(clearComponent);

        updateComponentsVisibility();

        attachContent(content);
    }

    protected JmixUpload createUploadComponent() {
        return new JmixUpload();
    }

    protected HasComponents createContentComponent() {
        return new Div();
    }

    protected Component createFileNameComponent() {
        return new Button();
    }

    protected void initFileNameComponent(Component fileNameComponent) {
        addClassNames(fileNameComponent, FILE_NAME_COMPONENT_CLASS_NAME, FILE_NAME_COMPONENT_EMPTY_CLASS_NAME);

        if (fileNameComponent instanceof HasText) {
            String fileName = Strings.nullToEmpty(generateFileName());
            ((HasText) fileNameComponent).setText(fileName);
        }

        if (fileNameComponent instanceof Button) {
            ((Button) fileNameComponent).addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }

        setComponentEnabled(fileNameComponent, false);
    }

    protected Component createClearComponent() {
        return new NativeButton();
    }

    protected void initClearComponent(Component clearComponent) {
        addClassNames(clearComponent, CLEAR_COMPONENT_CLASS_NAME);
        setComponentClickListener(clearComponent, this::onClearButtonClick);
        setComponentAriaLabel(clearComponent, CLEAR_COMPONENT_ARIA_LABEL);
    }

    protected void onClearButtonClick(ClickEvent<?> clickEvent) {
        if (!isEnabled() || isReadOnly()) {
            return;
        }
        upload.clearFileList();
        setInternalValue(getEmptyValue());
    }

    protected void attachContent(HasComponents content) {
        content.add(upload, fileNameComponent);

        content.getElement().setAttribute("slot", "input");
        getElement().appendChild(content.getElement());
    }

    protected void initUploadComponent(JmixUpload upload) {
        upload.setReceiver(createUploadReceiver());

        Component uploadButtonComponent = createUploadButtonComponent();
        initUploadButtonComponent(uploadButtonComponent);
        upload.setUploadButton(uploadButtonComponent);
    }

    protected Component createUploadButtonComponent() {
        return new Button();
    }

    protected void initUploadButtonComponent(Component component) {
        setComponentText(component, UPLOAD);
    }

    protected void attachUploadEvents(JmixUpload upload) {
        upload.addStartedListener(this::onStartedEvent);
        upload.addProgressListener(this::onProgressEvent);
        upload.addFinishedListener(this::onFinishedEvent);
        upload.addFailedListener(this::onFailedEvent);
        upload.addFileRejectedListener(this::onFileRejectedEvent);
        upload.addSucceededListener(this::onSucceededEvent);

        upload.addUploadInternalError(this::onJmixUploadInternalError);
    }

    protected void initContentComponent(HasComponents component) {
        addClassNames(component, INPUT_CONTAINER_CLASS_NAME);

        if (component instanceof HasSize) {
            ((HasSize) component).setWidthFull();
        }
    }

    protected <T extends HasComponents> T getContent() {
        return (T) content;
    }

    protected Receiver createUploadReceiver() {
        return new MemoryBuffer();
    }

    @Override
    public void setValue(@Nullable V value) {
        setInternalValue(value);
    }

    @Nullable
    @Override
    public V getValue() {
        return internalValue;
    }

    @Override
    public V getEmptyValue() {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        upload.setReadOnly(isReadOnly());

        updateComponentsVisibility();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        upload.setEnabled(enabled);
    }

    /**
     * Adds file upload progress listener that is informed on upload progress.
     *
     * @param listener progress listener to add
     * @return registration for removal of listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addFileUploadProgressListener(ComponentEventListener<FileUploadProgressEvent<C>> listener) {
        return getEventBus().addListener(FileUploadProgressEvent.class, (ComponentEventListener) listener);
    }

    protected void onProgressEvent(ProgressUpdateEvent event) {
        getEventBus().fireEvent(new FileUploadProgressEvent<>(this, event.getReadBytes(),
                event.getContentLength()));
    }

    /**
     * Adds a succeeded listener that is informed on upload finished.
     *
     * @param listener listener to add
     * @return registration for removal of listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addFileUploadFinishedListener(ComponentEventListener<FileUploadFinishedEvent<C>> listener) {
        return getEventBus().addListener(FileUploadFinishedEvent.class, (ComponentEventListener) listener);
    }

    protected void onFinishedEvent(FinishedEvent event) {
        getEventBus().fireEvent(new FileUploadFinishedEvent<>(this, event.getFileName(), event.getMIMEType(),
                event.getContentLength()));
    }

    /**
     * Add a listener that is informed on upload failure.
     *
     * @param listener listener to add
     * @return registration for removal of listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addFileUploadFailedListener(ComponentEventListener<FileUploadFailedEvent<C>> listener) {
        return getEventBus().addListener(FileUploadFailedEvent.class, (ComponentEventListener) listener);
    }

    protected void onFailedEvent(FailedEvent event) {
        getEventBus().fireEvent(new FileUploadFailedEvent<>(this, event.getFileName(), event.getMIMEType(),
                event.getContentLength(), event.getReason()));
    }

    /**
     * Add a succeeded listener that is informed on upload start.
     *
     * @param listener listener to add
     * @return registration for removal of listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addFileUploadStartedListener(ComponentEventListener<FileUploadStartedEvent<C>> listener) {
        return getEventBus().addListener(FileUploadStartedEvent.class, (ComponentEventListener) listener);
    }

    protected void onStartedEvent(StartedEvent event) {
        getEventBus().fireEvent(new FileUploadStartedEvent<>(this, event.getFileName(), event.getMIMEType(),
                event.getContentLength()));
    }

    /**
     * Adds a listener for file-reject events fired when a file cannot be added due to some constrains:
     * <ul>
     *     <li>{@link #setMaxFileSize(int)}</li>
     *     <li>{@link #setAcceptedFileTypes(String...)}</li>
     * </ul>
     *
     * @param listener listener to add
     * @return registration for removal of listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addFileUploadFileRejectedListener(ComponentEventListener<FileUploadFileRejectedEvent<C>> listener) {
        return getEventBus().addListener(FileUploadFileRejectedEvent.class, (ComponentEventListener) listener);
    }

    protected void onFileRejectedEvent(FileRejectedEvent event) {
        getEventBus().fireEvent(new FileUploadFileRejectedEvent<>(this, event.getErrorMessage()));
    }

    /**
     * Add a succeeded listener that is informed on upload succeeded.
     *
     * @param listener listener to add
     * @return registration for removal of listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addFileUploadSucceededListener(ComponentEventListener<FileUploadSucceededEvent<C>> listener) {
        return getEventBus().addListener(FileUploadSucceededEvent.class, (ComponentEventListener) listener);
    }

    protected void onSucceededEvent(SucceededEvent event) {
        getEventBus().fireEvent(new FileUploadSucceededEvent<>(this, event.getFileName(), event.getMIMEType(),
                event.getContentLength(), upload.getReceiver()));
    }

    /**
     * @return the maximum allowed file size in the client-side, in bytes
     */
    public int getMaxFileSize() {
        return upload.getMaxFileSize();
    }

    public void setMaxFileSize(int maxFileSize) {
        upload.setMaxFileSize(maxFileSize);
    }

    /**
     * @return the list of accepted file types for upload
     */
    public List<String> getAcceptedFileTypes() {
        return upload.getAcceptedFileTypes();
    }

    /**
     * Specify the types of files that the server accepts. Syntax: a MIME type pattern (wildcards are allowed)
     * or file extensions. Notice that MIME types are widely supported, while file extensions are only implemented
     * in certain browsers, so it should be avoided.
     * <p>
     * Example: "video/*","image/tiff" or ".pdf","audio/mp3"
     *
     * @param acceptedFileTypes the allowed file types to be uploaded, or {@code null} to clear any restrictions
     * @see Upload#setAcceptedFileTypes(String...)
     */
    public void setAcceptedFileTypes(String... acceptedFileTypes) {
        upload.setAcceptedFileTypes(acceptedFileTypes);
    }

    /**
     * @return internationalization properties or {@code null} if not set
     */
    @Nullable
    public JmixUploadI18N getI18n() {
        return (JmixUploadI18N) upload.getI18n();
    }

    /**
     * Sets the internationalization properties.
     *
     * @param i18n properties to set
     */
    public void setI18n(JmixUploadI18N i18n) {
        Preconditions.checkNotNull(i18n);

        upload.setI18n(i18n);
    }

    /**
     * @return a component that is set as icon to upload button or {@code null} if not set
     */
    @Nullable
    public Component getUploadIcon() {
        Component uploadButton = upload.getUploadButton();
        if (uploadButton instanceof Button) {
            return ((Button) uploadButton).getIcon();
        }
        return null;
    }

    /**
     * Sets a component as icon to upload button.
     *
     * @param icon component to set as icon
     */
    public void setUploadIcon(@Nullable Component icon) {
        Component uploadButton = upload.getUploadButton();
        if (uploadButton instanceof Button) {
            ((Button) uploadButton).setIcon(icon);
        }
    }

    /**
     * @return {@code true} if file dropping is enabled, {@code false} otherwise.
     * @see Upload#isDropAllowed()
     */
    public boolean isDropAllowed() {
        return upload.isDropAllowed();
    }

    /**
     * Sets whether the component supports dropping files for uploading. The default value is {@code true}.
     * <p>
     * See {@link Upload#setDropAllowed(boolean)} for details.
     *
     * @param allowed {@code true} to enable dropping
     */
    public void setDropAllowed(boolean allowed) {
        upload.setDropAllowed(allowed);
    }

    /**
     * @return text that should be shown in the upload button or {@code null} if not set
     */
    @Nullable
    public String getUploadText() {
        return uploadText;
    }

    /**
     * Sets the text that should be shown in the upload button. {@code null} value resets the default value.
     *
     * @param uploadText text to set
     */
    public void setUploadText(@Nullable String uploadText) {
        this.uploadText = uploadText;

        setComponentText(upload.getUploadButton(),
                Strings.isNullOrEmpty(uploadText)
                        ? getDefaultUploadText()
                        : uploadText);
    }

    /**
     * @return text that is shown when file is not uploaded or {@code null} if not set
     */
    @Nullable
    public String getFileNotSelectedText() {
        return fileNotSelectedText;
    }

    /**
     * Sets text that is shown when file is not uploaded
     *
     * @param fileNotSelectedText text to set
     */
    public void setFileNotSelectedText(@Nullable String fileNotSelectedText) {
        this.fileNotSelectedText = fileNotSelectedText;

        setComponentText(fileNameComponent,
                Strings.isNullOrEmpty(fileNotSelectedText)
                        ? generateFileName()
                        : fileNotSelectedText);
    }

    /**
     * @return {@code true} if name of uploaded file is shown
     */
    public boolean isFileNameVisible() {
        return fileNameVisible;
    }

    /**
     * Enables or disables displaying name of uploaded file.
     * <p>
     * The default value is {@code false}.
     *
     * @param visible whether file name should be shown
     */
    public void setFileNameVisible(boolean visible) {
        fileNameVisible = visible;

        updateComponentsVisibility();
    }

    /**
     * @return {@code true} if clear button is shown
     */
    public boolean isClearButtonVisible() {
        return clearButtonVisible;
    }

    /**
     * Enables or disables displaying clear button.
     * <p>
     * The default value is {@code false}.
     *
     * @param visible whether clear button should be shown
     */
    public void setClearButtonVisible(boolean visible) {
        clearButtonVisible = visible;

        updateComponentsVisibility();
    }

    /**
     * @return aria-label of clear button or {@code null} if not set
     */
    @Nullable
    public String getClearButtonAriaLabel() {
        Element element = upload.getUploadButton().getElement();
        return element.getAttribute(ElementConstants.ARIA_LABEL_PROPERTY_NAME);
    }

    /**
     * Sets aria-label attribute to clear button.
     *
     * @param ariaLabel aria-label to set
     */
    public void setClearButtonAriaLabel(@Nullable String ariaLabel) {
        setComponentAriaLabel(upload.getUploadButton(), ariaLabel);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<C, V>> listener) {
        @SuppressWarnings("rawtypes")
        ComponentEventListener componentListener = event -> {
            ComponentValueChangeEvent<C, V> valueChangeEvent = (ComponentValueChangeEvent<C, V>) event;
            listener.valueChanged(valueChangeEvent);
        };

        return ComponentUtil.addListener(this, ComponentValueChangeEvent.class, componentListener);
    }

    protected void setInternalValue(@Nullable V value) {
        setInternalValue(value, false);
    }

    protected void setInternalValue(@Nullable V value, boolean fromClient) {
        if (valueEquals(internalValue, value)) {
            return;
        }

        V oldValue = internalValue;
        internalValue = value;

        // update presentation
        setPresentationValue(value);

        ComponentValueChangeEvent<AbstractSingleUploadField<C, V>, V> event =
                new ComponentValueChangeEvent<>(this, this, oldValue, fromClient);
        fireEvent(event);
    }

    @Override
    protected void setPresentationValue(@Nullable V newPresentationValue) {
        getContent().remove(fileNameComponent, clearComponent);
        removeClassNames(fileNameComponent, FILE_NAME_COMPONENT_EMPTY_CLASS_NAME);

        String uploadedFileName = generateFileName();

        setComponentText(fileNameComponent, uploadedFileName);

        getContent().add(fileNameComponent);
        setComponentEnabled(fileNameComponent, newPresentationValue != null);

        if (newPresentationValue == null) {
            addClassNames(fileNameComponent, FILE_NAME_COMPONENT_EMPTY_CLASS_NAME);
        } else {
            getContent().add(clearComponent);
        }
    }

    protected abstract String generateFileName();

    protected abstract String getDefaultUploadText();

    protected void onJmixUploadInternalError(JmixUpload.JmixUploadInternalErrorEvent event) {
        handleJmixUploadInternalError(event.getFileName());
    }

    protected void handleJmixUploadInternalError(String fileName) {
        // used in inheritors
    }

    protected void addClassNames(HasElement component, String... classNames) {
        if (component instanceof HasStyle) {
            ((HasStyle) component).addClassNames(classNames);
        }
    }

    protected void removeClassNames(HasElement component, String... classNames) {
        if (component instanceof HasStyle) {
            ((HasStyle) component).removeClassNames(classNames);
        }
    }

    protected void setComponentEnabled(Component component, boolean enabled) {
        if (component instanceof HasEnabled) {
            ((HasEnabled) component).setEnabled(enabled);
        }
    }

    protected void setComponentText(Component component, String text) {
        if (component instanceof HasText) {
            ((HasText) component).setText(Strings.nullToEmpty(text));
        }
    }

    protected void setComponentClickListener(Component component, ComponentEventListener<ClickEvent<?>> listener) {
        if (component instanceof ClickNotifier) {
            ((ClickNotifier<?>) component).addClickListener((ComponentEventListener) listener);
        }
    }

    protected void setComponentAriaLabel(Component component, @Nullable String ariaLabel) {
        if (ariaLabel == null) {
            component.getElement().removeAttribute(ElementConstants.ARIA_LABEL_PROPERTY_NAME);
        } else {
            component.getElement().setAttribute(ElementConstants.ARIA_LABEL_PROPERTY_NAME, ariaLabel);
        }
    }

    protected void updateComponentsVisibility() {
        upload.setVisible(!isReadOnly());
        fileNameComponent.setVisible(fileNameVisible);
        clearComponent.setVisible(clearButtonVisible && !isReadOnly() && fileNameVisible);
    }
}
