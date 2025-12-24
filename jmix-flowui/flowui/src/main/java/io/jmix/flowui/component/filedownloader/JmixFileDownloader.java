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

package io.jmix.flowui.component.filedownloader;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.TransferUtil;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.download.DownloadDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Predicate;

@Internal
public class JmixFileDownloader extends Composite<Anchor> {

    private static final Logger log = LoggerFactory.getLogger(JmixFileDownloader.class);

    protected static final String DOWNLOAD_RESOURCE_PREFIX = "download/";
    protected static final String CLICK_EXPRESSION = "this.click()";

    protected String fileName;
    protected int cacheMaxAgeSec;

    // TODO: gg, remove
    protected SerializableConsumer<OutputStream> contentWriter;
    // TODO: gg, remove
    protected boolean isViewDocumentRequest;
    // TODO: gg, remove
    protected String contentType;
    protected DownloadContext downloadContext;

    protected Predicate<FileNotFoundContext> fileNotFoundExceptionHandler;

    public JmixFileDownloader() {
    }

    /**
     * Called when the content of this composite is requested for the first
     * time.
     * <p>
     * This method should initialize the component structure for the composite
     * and return the root component.
     * <p>
     * By default, this method uses reflection to instantiate the component
     * based on the generic type of the sub class.
     *
     * @return the root component which this composite wraps, never {@code null}
     */
    @Override
    protected Anchor initContent() {
        Anchor anchor = super.initContent();
        anchor.setHref(createDownloadHandler());

        return anchor;
    }

    protected DownloadHandler createDownloadHandler() {
        return new AbstractDownloadHandler() {
            @Override
            public void handleDownloadRequest(DownloadEvent event) throws IOException {
                VaadinRequest request = event.getRequest();
                VaadinSession session = event.getSession();

                String fileName = getFileName(downloadContext, session, request);
                String contentType = downloadContext.getContentType();
                event.setFileName(fileName);
                event.setContentLength(-1);

                String type = isViewDocumentRequest ? "inline" : "attachment";

                VaadinResponse response = event.getResponse();
                response.setStatus(200);
                response.setHeader(
                        "Content-Disposition",
                        ContentDisposition.builder(type)
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build()
                                .toString());
                response.setHeader("Cache-Control", "private, max-age=%s"
                        .formatted(cacheMaxAgeSec));

                if (isViewDocumentRequest && !Strings.isNullOrEmpty(contentType)) {
                    event.setContentType(contentType);
                }

                try (OutputStream outputStream = event.getOutputStream();
                     InputStream inputStream = downloadContext.getDataProvider().getStream()) {
                    // Write data to the output stream
                    TransferUtil.transfer(inputStream, outputStream,
                            getTransferContext(event), Collections.emptyList());

                    // TODO: gg, try to use writer
//                    contentWriter.andThen(this::afterWriteHandler).accept(response.getOutputStream());
                } catch (IOException | RuntimeException e) {
                    if (!isViewDocumentRequest
                            || fileNotFoundExceptionHandler == null
                            || !fileNotFoundExceptionHandler.test(new FileNotFoundContext(e, response))) {
                        // send exception further
                        throw e;
                    }
                } finally {
                    response.getOutputStream().close();
                }

                event.getUI().access(() ->
                        accessCommand());
            }
        };
    }

    /**
     * Sets the maximum time in seconds during which the file will be considered relevant.
     * Makes sense for using the built-in PDF viewer in the Chrome browser.
     *
     * @param cacheMaxAgeSec the maximum time in seconds during which the file will be considered relevant
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control#response_directives">Cache-Control HTTP | MDN</a>
     */
    public void setCacheMaxAgeSec(int cacheMaxAgeSec) {
        this.cacheMaxAgeSec = cacheMaxAgeSec;
    }

    /**
     * Sets the filename of downloaded file.
     *
     * @param fileName the file name
     * @deprecated no direct replacement, {@link DownloadContext#getFileName()} provides file name instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the filename of downloaded file. Override if you want to generate the
     * name dynamically.
     *
     * @param session the vaadin session
     * @param request the vaadin request
     * @return the file name
     * @deprecated use {@link #getFileName(DownloadContext, VaadinSession, VaadinRequest)} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public String getFileName(VaadinSession session, VaadinRequest request) {
        return getFileName(downloadContext, session, request);
    }

    protected String getFileName(DownloadContext context, VaadinSession session, VaadinRequest request) {
        return Strings.isNullOrEmpty(fileName) ? context.getFileName() : fileName;
    }

    public void setFileNotFoundExceptionHandler(Predicate<FileNotFoundContext> fileNotFoundExceptionHandler) {
        this.fileNotFoundExceptionHandler = fileNotFoundExceptionHandler;
    }

    public Predicate<FileNotFoundContext> getFileNotFoundExceptionHandler() {
        return fileNotFoundExceptionHandler;
    }

    /**
     * Adds a listener that is executed when the file content has been streamed.
     * Note that the UI changes done in the listener don't necessarily happen
     * live if you don't have @{@link com.vaadin.flow.component.page.Push}
     * in use or use {@link UI#setPollInterval(int)} method.
     *
     * @param listener the listener
     * @return the {@link Registration}  you can use to remove this listener.
     */
    public Registration addDownloadFinishedListener(ComponentEventListener<DownloadFinishedEvent> listener) {
        return addListener(DownloadFinishedEvent.class, listener);
    }

    @Deprecated(since = "3.0", forRemoval = true)
    public void downloadFile(StreamResource resource) {
        contentType = resource.getContentTypeResolver().apply(resource, null);
        isViewDocumentRequest = false;

        getElement().setAttribute("download", fileName != null ? fileName : "");

        runCommand(resource);
        // TODO: gg, convert to download handler
    }

    @Deprecated(since = "3.0", forRemoval = true)
    public void viewDocument(StreamResource resource) {
        contentType = resource.getContentTypeResolver().apply(resource, null);
        isViewDocumentRequest = true;

        getContent().setTarget(AnchorTarget.BLANK);

        runCommand(resource);
        // TODO: gg, convert to download handler
    }

    @Deprecated(since = "3.0", forRemoval = true)
    protected void runCommand(StreamResource resource) {
        contentWriter = (stream) -> {
            try {
                resource.getWriter().accept(stream, VaadinSession.getCurrent());
            } catch (IOException e) {
                throw new RuntimeException("Error copying stream");
            }
        };

        execute();
    }

    public void downloadFile(DownloadContext context) {
        this.downloadContext = context;

        contentType = context.getContentType();
        isViewDocumentRequest = !context.isDownload();

        if (isViewDocumentRequest) {
            getContent().setTarget(AnchorTarget.BLANK);
            getElement().removeAttribute("download");
        } else {
            getContent().setTarget(AnchorTarget.DEFAULT);
            getElement().setAttribute("download", fileName != null ? fileName : "");
        }

        execute();
    }

    protected void execute() {
        getElement().executeJs(CLICK_EXPRESSION);
    }

    protected void afterWriteHandler(OutputStream outputStream) {
        getUI().ifPresent(currentUi -> currentUi.access(this::accessCommand));
    }

    protected void accessCommand() {
        JmixFileDownloader.this.getEventBus().fireEvent(
                new DownloadFinishedEvent(JmixFileDownloader.this, false)
        );
    }

    public static class DownloadFinishedEvent extends ComponentEvent<JmixFileDownloader> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public DownloadFinishedEvent(JmixFileDownloader source, boolean fromClient) {
            super(source, fromClient);
        }

    }

    public static class FileNotFoundContext {

        protected final Exception exception;
        protected final VaadinResponse response;

        public FileNotFoundContext(Exception exception, VaadinResponse response) {
            this.exception = exception;
            this.response = response;
        }

        public Exception getException() {
            return exception;
        }

        public VaadinResponse getResponse() {
            return response;
        }
    }

    public static class DownloadContext {

        protected final DownloadDataProvider dataProvider;
        protected final String fileName;
        protected final String contentType;
        protected final boolean isDownload;

        public DownloadContext(DownloadDataProvider dataProvider,
                               String fileName,
                               String contentType,
                               boolean isDownload) {
            this.dataProvider = dataProvider;
            this.fileName = fileName;
            this.contentType = contentType;
            this.isDownload = isDownload;
        }

        public DownloadDataProvider getDataProvider() {
            return dataProvider;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        // TODO: gg, invert?
        public boolean isDownload() {
            return isDownload;
        }
    }
}
