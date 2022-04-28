/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.event.SerializableEventListener;
import io.jmix.ui.widget.client.fileupload.JmixFileUploadClientRpc;
import io.jmix.ui.widget.client.fileupload.JmixFileUploadServerRpc;
import io.jmix.ui.widget.client.fileupload.JmixFileUploadState;
import com.vaadin.server.*;
import com.vaadin.server.communication.FileUploadHandler;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.LegacyComponent;
import com.vaadin.util.ReflectTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@WebJarResource({
        "jquery:jquery.min.js",
        "jquery-ui:jquery-ui.min.js",
        "jquery-file-upload:jquery-fileupload.min.js",
        "jquery-file-upload:jquery-iframe-transport.min.js"
})
public class JmixFileUpload extends AbstractComponent
        implements Component.Focusable, UploadComponent, LegacyComponent {

    /**
     * The output of the upload is redirected to this receiver.
     */
    protected Receiver receiver;

    protected boolean isUploading;

    protected long contentLength = -1;

    protected Map<String, String> mimeTypes = new HashMap<>();

    protected boolean interrupted = false;

    /*
     * Handle to terminal via Upload monitors and controls the upload during it is being streamed.
     */
    protected com.vaadin.server.StreamVariable streamVariable;

    public JmixFileUpload() {
        registerRpc(new JmixFileUploadServerRpc() {
            @Override
            public void fileUploaded(String fileName) {
                fireUploadSuccess(fileName, mimeTypes.get(fileName), contentLength);

                getRpcProxy(JmixFileUploadClientRpc.class).continueUploading();
            }

            @Override
            public void fileSizeLimitExceeded(String fileName) {
                fireFileSizeLimitExceeded(fileName);
            }

            @Override
            public void fileExtensionNotAllowed(String fileName) {
                fireFileExtensionNotAllowed(fileName);
            }

            @Override
            public void queueUploadFinished() {
                // trigger UI update after uploading
                markAsDirty();

                mimeTypes.clear();

                fireQueueUploadFinished();
            }
        });
    }

    // set error handler for background upload thread
    protected void setUploadingErrorHandler() {
        setErrorHandler(event -> {
            //noinspection ThrowableResultOfMethodCallIgnored
            Throwable ex = event.getThrowable();
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(ex);
            Logger log = LoggerFactory.getLogger(JmixFileUpload.class);
            if (StringUtils.contains(rootCauseMessage, "The multipart stream ended unexpectedly")
                    || StringUtils.contains(rootCauseMessage, "Unexpected EOF read on the socket")) {
                log.warn("Unable to upload file, it seems upload canceled or network error occurred");
            } else {
                log.error("Unexpected error in JmixFileUpload", ex);
            }

            if (isUploading) {
                endUpload();
            }
        });
    }

    protected void resetUploadingErrorHandler() {
        setErrorHandler(null);
    }

    @Override
    protected JmixFileUploadState getState() {
        return (JmixFileUploadState) super.getState();
    }

    @Override
    protected JmixFileUploadState getState(boolean markAsDirty) {
        return (JmixFileUploadState) super.getState(markAsDirty);
    }

    @Override
    public int getTabIndex() {
        return getState(false).tabIndex;
    }

    @Override
    public void setTabIndex(int tabIndex) {
        if (getTabIndex() != tabIndex) {
            getState().tabIndex = tabIndex;
        }
    }

    /**
     * Returns the icon's alt text.
     *
     * @return String with the alt text
     */
    public String getIconAlternateText() {
        return getState(false).iconAltText;
    }

    public void setIconAlternateText(String iconAltText) {
        if (!Objects.equals(getIconAlternateText(), iconAltText)) {
            getState().iconAltText = iconAltText;
        }
    }

    /**
     * Return HTML rendering setting
     *
     * @return <code>true</code> if the caption text is to be rendered as HTML, <code>false</code> otherwise
     */
    public boolean isHtmlContentAllowed() {
        return getState(false).captionAsHtml;
    }

    /**
     * Set whether the caption text is rendered as HTML or not. You might need to re-theme button to allow higher
     * content than the original text style.
     * <p>
     * If set to true, the captions are passed to the browser as html and the developer is responsible for ensuring no
     * harmful html is used. If set to false, the content is passed to the browser as plain text.
     *
     * @param htmlContentAllowed <code>true</code> if caption is rendered as HTML, <code>false</code> otherwise
     */
    public void setHtmlContentAllowed(boolean htmlContentAllowed) {
        if (isHtmlContentAllowed() != htmlContentAllowed) {
            getState().captionAsHtml = htmlContentAllowed;
        }
    }

    /**
     * Sets the component's icon and alt text.
     * <p>
     * An alt text is shown when an image could not be loaded, and read by assistive devices.
     *
     * @param icon        the icon to be shown with the component's caption.
     * @param iconAltText String to use as alt text
     */
    public void setIcon(Resource icon, String iconAltText) {
        super.setIcon(icon);

        getState().iconAltText = iconAltText;
    }

    public boolean isMultiSelect() {
        return getState(false).multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        if (isMultiSelect() != multiSelect) {
            getState().multiSelect = multiSelect;
        }
    }

    public String getUnableToUploadFileMessage() {
        return getState(false).unableToUploadFileMessage;
    }

    public void setUnableToUploadFileMessage(String message) {
        if (!Objects.equals(getUnableToUploadFileMessage(), message)) {
            getState().unableToUploadFileMessage = message;
        }
    }

    public String getProgressWindowCaption() {
        return getState(false).progressWindowCaption;
    }

    public void setProgressWindowCaption(String progressWindowCaption) {
        if (!Objects.equals(getProgressWindowCaption(), progressWindowCaption)) {
            getState().progressWindowCaption = progressWindowCaption;
        }
    }

    public String getCancelButtonCaption() {
        return getState(false).cancelButtonCaption;
    }

    public void setCancelButtonCaption(String cancelButtonCaption) {
        if (!Objects.equals(getCancelButtonCaption(), cancelButtonCaption)) {
            getState().cancelButtonCaption = cancelButtonCaption;
        }
    }

    @Nullable
    @Override
    public String getAccept() {
        return getState(false).accept;
    }

    /**
     * Note: this is just a hint for browser, user may select files that do not meet this property
     *
     * @param accept mime types, comma separated
     */
    @Override
    public void setAccept(@Nullable String accept) {
        if (!Objects.equals(accept, getAccept())) {
            getState().accept = accept;
        }
    }

    @Nullable
    public Set<String> getPermittedExtensions() {
        return getState(false).permittedExtensions;
    }

    public void setPermittedExtensions(@Nullable Set<String> permittedExtensions) {
        getState().permittedExtensions = permittedExtensions;
    }

    public double getFileSizeLimit() {
        return getState(false).fileSizeLimit;
    }

    /**
     * @param fileSizeLimit file size limit in bytes
     */
    public void setFileSizeLimit(long fileSizeLimit) {
        getState().fileSizeLimit = fileSizeLimit;
    }

    @Nullable
    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(@Nullable Receiver receiver) {
        this.receiver = receiver;
    }

    @Nullable
    public Component getDropZone() {
        return (Component) getState(false).dropZone;
    }

    public void setDropZone(@Nullable Component component) {
        if (getDropZone() != component) {
            getState().dropZone = component;
        }
    }

    public void setPasteZone(@Nullable Component component) {
        if (getPasteZone() != component) {
            getState().pasteZone = component;
        }
    }

    @Nullable
    public Component getPasteZone() {
        return (Component) getState(false).pasteZone;
    }

    @Nullable
    public String getDropZonePrompt() {
        return getState(false).dropZonePrompt;
    }

    public void setDropZonePrompt(@Nullable String dropZonePrompt) {
        if (!Objects.equals(getDropZonePrompt(), dropZonePrompt)) {
            getState().dropZonePrompt = dropZonePrompt;
        }
    }

    protected com.vaadin.server.StreamVariable getStreamVariable() {
        if (streamVariable == null) {
            streamVariable = new com.vaadin.server.StreamVariable() {
                private StreamingStartEvent lastStartedEvent;

                @Override
                public boolean listenProgress() {
                    return false;
                }

                @Override
                public void onProgress(StreamingProgressEvent event) {
                }

                @Override
                public boolean isInterrupted() {
                    return interrupted;
                }

                @Override
                public OutputStream getOutputStream() {
                    if (getReceiver() == null) {
                        throw new IllegalStateException(
                                "Upload cannot be performed without a receiver set");
                    }
                    OutputStream receiveUpload = getReceiver().receiveUpload(
                            lastStartedEvent.getFileName(),
                            lastStartedEvent.getMimeType());
                    lastStartedEvent = null;
                    return receiveUpload;
                }

                @Override
                public void streamingStarted(StreamingStartEvent event) {
                    startUpload();

                    mimeTypes.put(event.getFileName(), event.getMimeType());

                    contentLength = event.getContentLength();
                    lastStartedEvent = event;

                    if (hasInvalidExtension(event.getFileName())) {
                        Logger log = LoggerFactory.getLogger(JmixFileUpload.class);
                        log.warn("Unable to start upload. File extension is not allowed.");

                        interruptUpload();
                        return;
                    }

                    double fileSizeLimit = getFileSizeLimit();
                    if (fileSizeLimit > 0 && event.getContentLength() > fileSizeLimit) {
                        Logger log = LoggerFactory.getLogger(JmixFileUpload.class);
                        log.warn("Unable to start upload. File size limit exceeded, but client-side checks ignored.");

                        interruptUpload();
                        return;
                        // here client sends file to us bypassing client-side checks, just stop uploading
                    }

                    fireStarted(event.getFileName(), event.getMimeType());
                }


                private boolean hasInvalidExtension(String name) {
                    if (getPermittedExtensions() != null && !getPermittedExtensions().isEmpty()) {
                        if (name.lastIndexOf(".") > 0) {
                            String fileExtension = name.substring(name.lastIndexOf("."), name.length());
                            return !getPermittedExtensions().contains(fileExtension.toLowerCase());
                        } else {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void streamingFinished(StreamingEndEvent event) {
                    endUpload();
                }

                @Override
                public void streamingFailed(StreamingErrorEvent event) {
                    Exception exception = event.getException();
                    if (exception instanceof FileUploadHandler.UploadInterruptedException) {
                        endUpload();
                    }

                    if (exception instanceof NoInputStreamException) {
                        fireNoInputStream(event.getFileName(), event.getMimeType(), 0);
                    } else if (exception instanceof NoOutputStreamException) {
                        fireNoOutputStream(event.getFileName(), event.getMimeType(), 0);
                    } else {
                        fireUploadInterrupted(event.getFileName(), event.getMimeType(), 0, exception);
                    }
                }
            };
        }
        return streamVariable;
    }

    /**
     * Go into upload state. This is to prevent double uploading on same
     * component.
     * <p>
     * Warning: this is an internal method used by the framework and should not
     * be used by user of the Upload component. Using it results in the Upload
     * component going in wrong state and not working. It is currently public
     * because it is used by another class.
     */
    protected void startUpload() {
        if (isUploading) {
            throw new IllegalStateException("uploading already started");
        }
        isUploading = true;

        setUploadingErrorHandler();
    }

    /**
     * Interrupts the upload currently being received. The interruption will be done by the receiving thread so this
     * method will return immediately and the actual interrupt will happen a bit later.
     */
    protected void interruptUpload() {
        if (isUploading) {
            interrupted = true;
        }
    }

    /**
     * Go into state where new uploading can begin.
     * <p>
     * Warning: this is an internal method used by the framework and should not be used by user of the Upload component.
     */
    protected void endUpload() {
        isUploading = false;
        contentLength = -1;
        interrupted = false;
        markAsDirty();

        resetUploadingErrorHandler();
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        // Post file to this stream variable
        target.addVariable(this, "uploadUrl", getStreamVariable());
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
    }

    protected void fireStarted(String fileName, String MIMEType) {
        fireEvent(new StartedEvent(this, fileName, MIMEType,
                contentLength));
    }

    protected void fireNoInputStream(String fileName, String MIMEType, long length) {
        fireEvent(new NoInputStreamEvent(this, fileName, MIMEType,
                length));
    }

    protected void fireNoOutputStream(String fileName, String MIMEType, long length) {
        fireEvent(new NoOutputStreamEvent(this, fileName, MIMEType,
                length));
    }

    protected void fireUploadInterrupted(String fileName, String MIMEType, long length, Exception e) {
        fireEvent(new FailedEvent(this, fileName, MIMEType, length, e));
    }

    protected void fireUploadSuccess(String fileName, String MIMEType, long length) {
        fireEvent(new SucceededEvent(this, fileName, MIMEType, length));
    }

    protected void fireFileSizeLimitExceeded(String fileName) {
        fireEvent(new FileSizeLimitExceededEvent(this, fileName));
    }

    protected void fireFileExtensionNotAllowed(String fileName) {
        fireEvent(new FileExtensionNotAllowedEvent(this, fileName));
    }

    protected void fireQueueUploadFinished() {
        fireEvent(new QueueFinishedEvent(this));
    }

    /**
     * Interface that must be implemented by the upload receivers to provide the JmixFileUpload component an output
     * stream to write the uploaded data.
     */
    public interface Receiver extends Serializable {

        /**
         * Invoked when a new upload arrives.
         *
         * @param fileName the desired fileName of the upload, usually as specified
         *                 by the client.
         * @param mimeType the MIME type of the uploaded file.
         * @return Stream to which the uploaded file should be written.
         */
        OutputStream receiveUpload(String fileName, String mimeType);
    }

    /**
     * JmixFileUpload.FinishedEvent is sent when the upload receives a file, regardless of whether the reception was
     * successful or failed. If you wish to distinguish between the two cases, use either SucceededEvent or FailedEvent,
     * which are both subclasses of the FinishedEvent.
     */
    public static class FinishedEvent extends Component.Event {

        /**
         * Length of the received file.
         */
        private final long contentLength;

        /**
         * MIME type of the received file.
         */
        private final String type;

        /**
         * Received file name.
         */
        private final String fileName;

        /**
         * @param source        the source of the file.
         * @param fileName      the received file name.
         * @param MIMEType      the MIME type of the received file.
         * @param contentLength the contentLength of the received file.
         */
        public FinishedEvent(JmixFileUpload source, String fileName, String MIMEType,
                             long contentLength) {
            super(source);
            type = MIMEType;
            this.fileName = fileName;
            this.contentLength = contentLength;
        }

        /**
         * Uploads where the event occurred.
         *
         * @return the Source of the event.
         */
        public JmixFileUpload getUpload() {
            return (JmixFileUpload) getSource();
        }

        /**
         * Gets the file name.
         *
         * @return the fileName.
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Gets the MIME Type of the file.
         *
         * @return the MIME type.
         */
        public String getMIMEType() {
            return type;
        }

        /**
         * Gets the contentLength of the file.
         *
         * @return the contentLength.
         */
        public long getContentLength() {
            return contentLength;
        }
    }

    /**
     * JmixFileUpload.FailedEvent event is sent when the upload is received, but the reception is interrupted for some
     * reason.
     */
    public static class FailedEvent extends FinishedEvent {
        private Exception reason = null;

        public FailedEvent(JmixFileUpload source, String fileName, String MIMEType,
                           long length, Exception reason) {
            this(source, fileName, MIMEType, length);
            this.reason = reason;
        }

        public FailedEvent(JmixFileUpload source, String fileName, String MIMEType,
                           long length) {
            super(source, fileName, MIMEType, length);
        }

        /**
         * Gets the exception that caused the failure.
         *
         * @return the exception that caused the failure, null if n/a
         */
        public Exception getReason() {
            return reason;
        }
    }

    public static class FileSizeLimitExceededEvent extends Component.Event {

        private String fileName;

        /**
         * @param source   the source of the file.
         * @param fileName the received file name.
         */
        public FileSizeLimitExceededEvent(JmixFileUpload source, String fileName) {
            super(source);

            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class FileExtensionNotAllowedEvent extends Component.Event {

        private String fileName;

        /**
         * @param source   the source of the file.
         * @param fileName the received file name.
         */
        public FileExtensionNotAllowedEvent(JmixFileUpload source, String fileName) {
            super(source);

            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    /**
     * FailedEvent that indicates that an output stream could not be obtained.
     */
    public static class NoOutputStreamEvent extends FailedEvent {

        public NoOutputStreamEvent(JmixFileUpload source, String fileName,
                                   String MIMEType, long length) {
            super(source, fileName, MIMEType, length);
        }
    }

    /**
     * FailedEvent that indicates that an input stream could not be obtained.
     */
    public static class NoInputStreamEvent extends FailedEvent {

        public NoInputStreamEvent(JmixFileUpload source, String fileName,
                                  String MIMEType, long length) {
            super(source, fileName, MIMEType, length);
        }
    }

    /**
     * JmixFileUpload.SucceededEvent event is sent when the upload is received successfully.
     */
    public static class SucceededEvent extends FinishedEvent {

        public SucceededEvent(JmixFileUpload source, String fileName, String MIMEType,
                              long length) {
            super(source, fileName, MIMEType, length);
        }
    }

    /**
     * JmixFileUpload.StartedEvent event is sent when the upload is started to received.
     */
    public static class StartedEvent extends Component.Event {

        private final String fileName;
        private final String type;
        /**
         * Length of the received file.
         */
        private final long contentLength;

        public StartedEvent(JmixFileUpload source, String fileName, String MIMEType,
                            long contentLength) {
            super(source);
            this.fileName = fileName;
            type = MIMEType;
            this.contentLength = contentLength;
        }

        /**
         * Uploads where the event occurred.
         *
         * @return the Source of the event.
         */
        public JmixFileUpload getUpload() {
            return (JmixFileUpload) getSource();
        }

        /**
         * Gets the file name.
         *
         * @return the fileName.
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Gets the MIME Type of the file.
         *
         * @return the MIME type.
         */
        public String getMIMEType() {
            return type;
        }

        /**
         * @return the contentLength of the file that is being uploaded
         */
        public long getContentLength() {
            return contentLength;
        }
    }

    /**
     * JmixFileUpload.StartedEvent event is sent when the queue upload is finished.
     */
    public static class QueueFinishedEvent extends Component.Event {

        /**
         * Constructs a new event with the specified source component.
         *
         * @param source the source component of the event
         */
        public QueueFinishedEvent(JmixFileUpload source) {
            super(source);
        }
    }

    /**
     * Receives the events when the upload starts.
     */
    public interface StartedListener extends SerializableEventListener {

        /**
         * Upload has started.
         *
         * @param event the Upload started event.
         */
        void uploadStarted(StartedEvent event);
    }

    /**
     * Receives the events when the uploads are ready.
     */
    public interface FinishedListener extends SerializableEventListener {

        /**
         * Upload has finished.
         *
         * @param event the Upload finished event.
         */
        void uploadFinished(FinishedEvent event);
    }

    public interface QueueFinishedListener extends SerializableEventListener {

        /**
         * Upload has finished.
         *
         * @param event the Upload finished event.
         */
        void queueUploadFinished(QueueFinishedEvent event);
    }

    /**
     * Receives events when the uploads are finished, but unsuccessful.
     */
    public interface FailedListener extends SerializableEventListener {

        /**
         * Upload has finished unsuccessfully.
         *
         * @param event the Upload failed event.
         */
        void uploadFailed(FailedEvent event);
    }

    /**
     * Receives events when the uploads are successfully finished.
     */
    public interface SucceededListener extends SerializableEventListener {

        /**
         * Upload successful.
         *
         * @param event the Upload successful event.
         */
        void uploadSucceeded(SucceededEvent event);
    }

    /**
     * Receives events when the file size is greater than {@link #getFileSizeLimit()}.
     */
    public interface FileSizeLimitExceededListener extends SerializableEventListener {

        void fileSizeLimitExceeded(FileSizeLimitExceededEvent e);
    }

    /**
     * Receives events when the file extension is not included in {@link #getPermittedExtensions()}.
     */
    public interface FileExtensionNotAllowedListener extends SerializableEventListener {

        void fileExtensionNotAllowed(FileExtensionNotAllowedEvent e);
    }

    private static final Method UPLOAD_FINISHED_METHOD = ReflectTools.findMethod(
            FinishedListener.class, "uploadFinished", FinishedEvent.class);

    private static final Method QUEUE_UPLOAD_FINISHED_METHOD = ReflectTools.findMethod(
            QueueFinishedListener.class, "queueUploadFinished", QueueFinishedEvent.class);

    private static final Method UPLOAD_FAILED_METHOD = ReflectTools.findMethod(
            FailedListener.class, "uploadFailed", FailedEvent.class);

    private static final Method UPLOAD_STARTED_METHOD = ReflectTools.findMethod(
            StartedListener.class, "uploadStarted", StartedEvent.class);

    private static final Method UPLOAD_SUCCEEDED_METHOD = ReflectTools.findMethod(
            SucceededListener.class, "uploadSucceeded", SucceededEvent.class);

    private static final Method FILESIZE_LIMIT_EXCEEDED_METHOD = ReflectTools.findMethod(
            FileSizeLimitExceededListener.class, "fileSizeLimitExceeded", FileSizeLimitExceededEvent.class);

    private static final Method EXTENSION_NOT_ALLOWED_METHOD = ReflectTools.findMethod(
            FileExtensionNotAllowedListener.class, "fileExtensionNotAllowed", FileExtensionNotAllowedEvent.class);

    public void addStartedListener(StartedListener listener) {
        addListener(StartedEvent.class, listener, UPLOAD_STARTED_METHOD);
    }

    public void removeStartedListener(StartedListener listener) {
        removeListener(StartedEvent.class, listener, UPLOAD_STARTED_METHOD);
    }

    public void addFinishedListener(FinishedListener listener) {
        addListener(FinishedEvent.class, listener, UPLOAD_FINISHED_METHOD);
    }

    public void removeFinishedListener(FinishedListener listener) {
        removeListener(FinishedEvent.class, listener, UPLOAD_FINISHED_METHOD);
    }

    public void addFailedListener(FailedListener listener) {
        addListener(FailedEvent.class, listener, UPLOAD_FAILED_METHOD);
    }

    public void removeFailedListener(FailedListener listener) {
        removeListener(FailedEvent.class, listener, UPLOAD_FAILED_METHOD);
    }

    public void addSucceededListener(SucceededListener listener) {
        addListener(SucceededEvent.class, listener, UPLOAD_SUCCEEDED_METHOD);
    }

    public void removeSucceededListener(SucceededListener listener) {
        removeListener(SucceededEvent.class, listener, UPLOAD_SUCCEEDED_METHOD);
    }

    public void addFileSizeLimitExceededListener(FileSizeLimitExceededListener listener) {
        addListener(FileSizeLimitExceededEvent.class, listener, FILESIZE_LIMIT_EXCEEDED_METHOD);
    }

    public void addFileExtensionNotAllowedListener(FileExtensionNotAllowedListener listener) {
        addListener(FileExtensionNotAllowedEvent.class, listener, EXTENSION_NOT_ALLOWED_METHOD);
    }

    public void removeFileExtensionNotAllowedListener(FileExtensionNotAllowedListener listener) {
        removeListener(FileExtensionNotAllowedEvent.class, listener, EXTENSION_NOT_ALLOWED_METHOD);
    }

    public void removeFileSizeLimitExceededListener(FileSizeLimitExceededListener listener) {
        removeListener(FileSizeLimitExceededEvent.class, listener, FILESIZE_LIMIT_EXCEEDED_METHOD);
    }

    public void addQueueUploadFinishedListener(QueueFinishedListener listener) {
        addListener(QueueFinishedEvent.class, listener, QUEUE_UPLOAD_FINISHED_METHOD);
    }

    public void removeQueueUploadFinishedListener(QueueFinishedListener listener) {
        removeListener(QueueFinishedEvent.class, listener, QUEUE_UPLOAD_FINISHED_METHOD);
    }

    @Override
    public void focus() {
        super.focus();
    }
}