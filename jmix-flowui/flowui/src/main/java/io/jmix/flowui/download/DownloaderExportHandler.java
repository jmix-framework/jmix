/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.download;

import com.google.common.base.Strings;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.TransferProgressListener;
import com.vaadin.flow.server.streams.TransferUtil;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.streams.TransferProgressNotifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ContentDisposition;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

/**
 * A handler for managing file download/view requests triggered by {@link Downloader}.
 */
@Component("flowui_DownloaderExportHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DownloaderExportHandler extends AbstractDownloadHandler<DownloaderExportHandler>
        implements TransferProgressNotifier, SupportDownloadSuccessHandler {

    protected final DownloadContext downloadContext;

    protected DownloadSuccessHandler successHandler;
    protected Predicate<FileNotFoundContext> fileNotFoundExceptionHandler;

    public DownloaderExportHandler(DownloadContext downloadContext) {
        this.downloadContext = downloadContext;
    }

    @Override
    public void setDownloadSuccessHandler(@Nullable SupportDownloadSuccessHandler.DownloadSuccessHandler handler) {
        this.successHandler = handler;
    }

    public void setFileNotFoundExceptionHandler(@Nullable Predicate<FileNotFoundContext> handler) {
        this.fileNotFoundExceptionHandler = handler;
    }

    @Override
    public void handleDownloadRequest(DownloadEvent event) throws IOException {
        String fileName = downloadContext.fileName();
        String contentType = downloadContext.contentType();
        event.setFileName(fileName);
        event.setContentLength(-1);

        String type = isInline() ? "inline" : "attachment";

        VaadinResponse response = event.getResponse();
        response.setStatus(200);
        response.setHeader(
                "Content-Disposition",
                ContentDisposition.builder(type)
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString());
        response.setHeader("Cache-Control", "private, max-age=%s"
                .formatted(downloadContext.cacheMaxAgeSec()));

        if (isInline() && !Strings.isNullOrEmpty(contentType)) {
            event.setContentType(contentType);
        }

        try (OutputStream outputStream = event.getOutputStream();
             InputStream inputStream = downloadContext.dataProvider().getStream()) {
            // Write data to the output stream
            TransferUtil.transfer(inputStream, outputStream,
                    getTransferContext(event), getListeners());
        } catch (IOException | RuntimeException e) {
            if (!isInline()
                    || fileNotFoundExceptionHandler == null
                    || !fileNotFoundExceptionHandler.test(new FileNotFoundContext(e, response))) {
                // send exception further
                throw e;
            }
        } finally {
            response.getOutputStream().close();
        }

        event.getUI().access(() -> {
            if (successHandler != null) {
                successHandler.complete(new DownloadSuccessContext(
                        event.getOwningComponent(),
                        fileName,
                        contentType
                ));
            }
        });
    }

    @Override
    public Registration addTransferProgressListener(TransferProgressListener listener) {
        return super.addTransferProgressListener(listener);
    }

    @Override
    public boolean isInline() {
        return !downloadContext.download();
    }

    @Override
    public DownloaderExportHandler inline() {
        throw new UnsupportedOperationException("Inline mode is considered based on the passed %s object"
                .formatted(DownloadContext.class.getSimpleName()));
    }

    @Override
    public boolean isAllowInert() {
        // 'Downloader' creates a link appended to the UI to trigger a programmatic download.
        // Because 'Downloader' can be called from a modal dialog window, we need to allow
        // inert elements to be invoked.
        return true;
    }

    /**
     * Represents context information about a file not found error during download.
     *
     * @param reason   the exception that caused the file not found error
     * @param response the Vaadin response associated with the download operation
     */
    public record FileNotFoundContext(Exception reason, VaadinResponse response) {
    }
}
