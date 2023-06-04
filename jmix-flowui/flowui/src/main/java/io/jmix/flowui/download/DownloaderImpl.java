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

package io.jmix.flowui.download;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.CoreProperties;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.FileTypesHelper;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowuiProperties;
import io.jmix.flowui.component.filedownloader.JmixFileDownloader;
import io.jmix.flowui.exception.IllegalConcurrentAccessException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.io.IOException;
import java.io.PrintWriter;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Shows exported data in the web browser or downloads it.
 */
@Component("flowui_Downloader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DownloaderImpl implements Downloader {

    private static final Logger log = LoggerFactory.getLogger(DownloaderImpl.class);

    protected FlowuiProperties flowuiProperties;
    protected CoreProperties coreProperties;

    protected Messages messages;

    protected FileStorageLocator fileStorageLocator;
    protected FileStorage fileStorage;

    protected boolean newWindow;

    // Use flags from app.properties for show/download files
    protected boolean useViewList;

    /**
     * Constructor with newWindow=false
     */
    public DownloaderImpl() {
        this.newWindow = false;
        this.useViewList = true;
    }

    /**
     * @param newWindow if true, show data in the same browser window;
     *                  if false, open new browser window
     */
    public DownloaderImpl(boolean newWindow) {
        this.newWindow = newWindow;
        this.useViewList = false;
    }

    @Autowired
    public void setFlowuiProperties(FlowuiProperties flowuiProperties) {
        this.flowuiProperties = flowuiProperties;
    }

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        this.fileStorageLocator = fileStorageLocator;
    }

    @Override
    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public boolean isShowNewWindow() {
        return newWindow;
    }

    @Override
    public void setShowNewWindow(boolean showNewWindow) {
        this.newWindow = showNewWindow;

        // newWindow is set explicitly
        this.useViewList = false;
    }

    /**
     * Show/Download resource at client side
     *
     * @param dataProvider   DownloadDataProvider
     * @param resourceName   ResourceName for client side
     * @param downloadFormat DownloadFormat
     * @see FileRefDownloadDataProvider
     * @see ByteArrayDownloadDataProvider
     */
    public void download(DownloadDataProvider dataProvider,
                         String resourceName,
                         @Nullable DownloadFormat downloadFormat) {
        checkUIAccess();

        boolean showNewWindow = this.newWindow;

        if (useViewList) {
            String fileExt;

            if (downloadFormat != null) {
                fileExt = downloadFormat.getFileExt();
            } else {
                fileExt = FilenameUtils.getExtension(resourceName);
            }

            showNewWindow = flowuiProperties.getViewFileExtensions().contains(StringUtils.lowerCase(fileExt));
        }

        if (downloadFormat != null) {
            if (StringUtils.isEmpty(FilenameUtils.getExtension(resourceName))) {
                resourceName += "." + downloadFormat.getFileExt();
            }
        }

        JmixFileDownloader fileDownloader = new JmixFileDownloader();

        UI ui = UI.getCurrent();

        ui.add(fileDownloader);

        log.debug("added {} in {}", JmixFileDownloader.class.getSimpleName(), ui);

        fileDownloader.setFileName(resourceName);
        fileDownloader.setFileNotFoundExceptionHandler(this::handleFileNotFoundException);

        StreamResource resource = new StreamResource(resourceName, dataProvider::getStream);

        if (downloadFormat != null && StringUtils.isNotEmpty(downloadFormat.getContentType())) {
            resource.setContentType(downloadFormat.getContentType());
        } else {
            resource.setContentType(FileTypesHelper.getMIMEType(resourceName));
        }

        if (showNewWindow && isBrowserSupportsPopups() || isIPhone()) {
            fileDownloader.viewDocument(resource);
        } else {
            fileDownloader.downloadFile(resource);
        }
    }

    /**
     * Show/Download resource at client side
     *
     * @param dataProvider DownloadDataProvider
     * @param resourceName ResourceName for client side
     * @see FileRefDownloadDataProvider
     * @see ByteArrayDownloadDataProvider
     */
    @Override
    public void download(DownloadDataProvider dataProvider, String resourceName) {
        String extension = FilenameUtils.getExtension(resourceName);
        DownloadFormat format = DownloadFormat.getByExtension(extension);
        download(dataProvider, resourceName, format);
    }

    @Override
    public void download(FileRef fileReference) {
        DownloadFormat format = DownloadFormat.getByExtension(
                FilenameUtils.getExtension(fileReference.getFileName())
        );
        download(fileReference, format);
    }

    @Override
    public void download(FileRef fileReference, @Nullable DownloadFormat format) {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        String fileName = fileReference.getFileName();
        download(new FileRefDownloadDataProvider(fileReference, fileStorage), fileName, format);
    }

    @Override
    public void download(byte[] data, String resourceName) {
        ByteArrayDownloadDataProvider dataProvider = new ByteArrayDownloadDataProvider(data,
                flowuiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir());
        download(dataProvider, resourceName);
    }

    @Override
    public void download(byte[] data, String resourceName, @Nullable DownloadFormat format) {
        ByteArrayDownloadDataProvider dataProvider = new ByteArrayDownloadDataProvider(data,
                flowuiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir());
        download(dataProvider, resourceName, format);
    }

    protected void checkUIAccess() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();

        if (vaadinSession == null || !vaadinSession.hasLock()) {
            throw new IllegalConcurrentAccessException();
        }
    }

    protected boolean handleFileNotFoundException(JmixFileDownloader.FileNotFoundContext fileNotFoundEvent) {
        Exception exception = fileNotFoundEvent.getException();
        VaadinResponse response = fileNotFoundEvent.getResponse();

        if (!(exception instanceof FileStorageException)) {
            return false;
        }

        FileStorageException storageException = (FileStorageException) exception;
        if (storageException.getType() == FileStorageException.Type.FILE_NOT_FOUND) {
            try {
                String message = messages.getMessage("fileNotFound.message");
                String formattedMessage = String.format(message, storageException.getFileName());

                writeFileNotFoundException(response, formattedMessage);
                return true;
            } catch (IOException e) {
                log.debug("Can't write file not found exception to the response body for: {}",
                        storageException.getFileName(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    protected void writeFileNotFoundException(VaadinResponse response, String message) throws IOException {
        response.setStatus(SC_NOT_FOUND);
        response.setHeader("Content-Type", "text/html; charset=utf-8");

        PrintWriter writer = response.getWriter();
        writer.write("<h1 style=\"font-size:40px;\">404</h1><p style=\"font-size: 25px\">" + message + "</p>");
        writer.flush();
    }

    protected boolean isBrowserSupportsPopups() {
        return !VaadinSession.getCurrent().getBrowser().isSafari();
    }

    protected boolean isIPhone() {
        return VaadinSession.getCurrent().getBrowser().isIPhone();
    }
}
