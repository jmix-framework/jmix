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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.annotation.Internal;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Predicate;

@Internal
public class JmixFileDownloader extends Composite<Anchor> {

    private static final Logger log = LoggerFactory.getLogger(JmixFileDownloader.class);

    protected static final String CLICK_EXPRESSION = "this.click()";
    protected static final String SELF_REMOVE_EXPRESSION =
            "setTimeout((element) => {" +
                    " console.debug(element, 'has been removed');" +
                    " element.remove(); " +
                    "}, 60 * 1000, this)";

    protected String fileName;

    protected RequestHandler requestHandler;

    protected SerializableConsumer<OutputStream> contentWriter;
    protected boolean isViewDocumentRequest;
    protected String contentType;

    protected Predicate<FileNotFoundContext> fileNotFoundExceptionHandler;

    public JmixFileDownloader() {
        runBeforeClientResponse(this::beforeClientResponseDownloadHandler);
    }

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
     */
    public String getFileName(VaadinSession session, VaadinRequest request) {
        return fileName;
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

    public void downloadFile(StreamResource resource) {
        contentType = resource.getContentTypeResolver().apply(resource, null);
        isViewDocumentRequest = false;

        getElement().setAttribute("download", fileName != null ? fileName : "");

        runCommand(resource);
    }

    public void viewDocument(StreamResource resource) {
        contentType = resource.getContentTypeResolver().apply(resource, null);
        isViewDocumentRequest = true;

        getContent().setTarget(AnchorTarget.BLANK);

        runCommand(resource);
    }

    protected void runCommand(StreamResource resource) {
        contentWriter = (stream) -> {
            try (stream) {
                resource.getWriter().accept(stream, VaadinSession.getCurrent());
            } catch (IOException e) {
                throw new RuntimeException("Error copying stream");
            }
        };

        addDownloadFinishedListener(event -> getElement().executeJs(SELF_REMOVE_EXPRESSION));

        execute();
    }

    protected void execute() {
        getElement().executeJs(CLICK_EXPRESSION);
    }

    protected void beforeClientResponseDownloadHandler(UI ui) {
        String identifier = UUID.randomUUID().toString();

        requestHandler = (session, request, response) -> {
            if (request.getPathInfo().endsWith(identifier)) {

                String type = isViewDocumentRequest ? "inline" : "attachment";

                response.setStatus(200);
                response.setHeader(
                        "Content-Disposition",
                        ContentDisposition.builder(type)
                                .filename(getFileName(session, request), StandardCharsets.UTF_8)
                                .build()
                                .toString());

                if (isViewDocumentRequest && Strings.isNotEmpty(contentType)) {
                    response.setContentType(contentType);
                }

                try {
                    contentWriter.andThen(this::afterWriteHandler)
                            .accept(response.getOutputStream());

                    log.debug("response {} has been sent", response);
                } catch (IOException e) {
                    if (!isViewDocumentRequest
                            || fileNotFoundExceptionHandler == null
                            || !fileNotFoundExceptionHandler.test(new FileNotFoundContext(e, response))) {
                        // send exception further
                        throw e;
                    } else {
                        // exception is handled in listener
                        return true;
                    }
                }

                return true;
            }
            return false;
        };

        ui.getSession().addRequestHandler(requestHandler);

        getContent().setHref("./" + identifier);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        getUI().ifPresent(ui -> ui.getSession().removeRequestHandler(requestHandler));
    }

    protected void afterWriteHandler(OutputStream outputStream) {
        getUI().ifPresent(currentUi -> currentUi.access(this::accessCommand));
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                        this,
                        context -> command.accept(ui)
                )
        );
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
}
