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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SingleFileUploadField extends UploadField, Component.Focusable, Buffered {

    /**
     * @return content of uploaded file.
     */
    @Nullable
    InputStream getFileContent();

    /**
     * Enable or disable displaying name of uploaded file next to upload button.
     */
    @StudioProperty(defaultValue = "false")
    void setShowFileName(boolean showFileName);

    /**
     * @return true if name of uploaded file is shown.
     */
    boolean isShowFileName();

    /**
     * Setup caption of upload button.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setUploadButtonCaption(@Nullable String caption);

    /**
     * @return upload button caption.
     */
    @Nullable
    String getUploadButtonCaption();

    /**
     * Setup upload button icon.
     */
    @StudioProperty(type = PropertyType.ICON_ID)
    void setUploadButtonIcon(@Nullable String icon);

    /**
     * @return upload button icon.
     */
    @Nullable
    String getUploadButtonIcon();

    /**
     * Setup upload button description.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setUploadButtonDescription(@Nullable String description);

    /**
     * @return upload button description.
     */
    @Nullable
    String getUploadButtonDescription();

    /**
     * Enable or disable displaying name of clear button.
     */
    @StudioProperty(defaultValue = "false")
    void setShowClearButton(boolean showClearButton);

    /**
     * @return true if clear button is shown.
     */
    boolean isShowClearButton();

    /**
     * Setup clear button caption.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setClearButtonCaption(@Nullable String caption);

    /**
     * @return clear button caption.
     */
    @Nullable
    String getClearButtonCaption();

    /**
     * Setup clear button icon.
     */
    @StudioProperty(type = PropertyType.ICON_ID)
    void setClearButtonIcon(@Nullable String icon);

    /**
     * @return clear button icon.
     */
    @Nullable
    String getClearButtonIcon();

    /**
     * Setup clear button description.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setClearButtonDescription(String description);

    /**
     * @return clear button description.
     */
    String getClearButtonDescription();

    /**
     * Adds file upload succeed listener. It is invoked when the uploads are successfully finished.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addFileUploadSucceedListener(Consumer<FileUploadSucceedEvent> listener);

    /**
     * Sets a callback interface which is invoked by the {@link SingleFileUploadField} before value
     * clearing when user use clear button.
     * <p>
     * Listener can prevent value clearing using {@link BeforeValueClearEvent#preventClearAction()}.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     * @see #setShowClearButton(boolean)
     */
    Subscription addBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener);

    /**
     * Adds a callback interface which is invoked by the {@link SingleFileUploadField} after value
     * has been cleared using clear button.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     * @see #setShowClearButton(boolean)
     */
    Subscription addAfterValueClearListener(Consumer<AfterValueClearEvent> listener);

    /**
     * Set content provider which contains file data.
     * <p>Passed content provider will be used for downloading by clicking the link with file name
     * or as source for {@link SingleFileUploadField#getFileContent()} method.</p>
     *
     * @param contentProvider content provider
     */
    void setContentProvider(@Nullable Supplier<InputStream> contentProvider);

    /**
     * @return FileContentProvider which can be used to read data from field
     */
    @Nullable
    Supplier<InputStream> getContentProvider();

    /**
     * Describes file upload succeeded event when the uploads are successfully finished.
     */
    class FileUploadSucceedEvent extends FileUploadEvent {
        public FileUploadSucceedEvent(UploadField source, String fileName, long contentLength) {
            super(source, fileName, contentLength);
        }
    }

    /**
     * Describes before value clear event. Event is invoked before value clearing when user use clear button.
     */
    class BeforeValueClearEvent extends EventObject {
        private boolean clearPrevented = false;

        public BeforeValueClearEvent(SingleFileUploadField target) {
            super(target);
        }

        @Override
        public SingleFileUploadField getSource() {
            return (SingleFileUploadField) super.getSource();
        }

        /**
         * @return true if clearing the file value is prevented
         */
        public boolean isClearPrevented() {
            return clearPrevented;
        }

        /**
         * Prevents the file value from being cleared.
         */
        public void preventClearAction() {
            this.clearPrevented = true;
        }
    }

    /**
     * Describes after value clear event.
     */
    class AfterValueClearEvent extends EventObject {
        private final boolean valueCleared;

        public AfterValueClearEvent(SingleFileUploadField target, boolean valueCleared) {
            super(target);
            this.valueCleared = valueCleared;
        }

        @Override
        public SingleFileUploadField getSource() {
            return (SingleFileUploadField) super.getSource();
        }

        /**
         * @return true if the file value has been cleared
         */
        public boolean isValueCleared() {
            return valueCleared;
        }
    }
}
