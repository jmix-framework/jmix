/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package io.jmix.ui.export;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinResponse;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Frame;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.upload.FileTypesHelper;
import io.jmix.ui.widget.JmixFileDownloader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Allows to show exported data in web browser or download it.
 */
@Component(ExportDisplay.NAME)
@Scope("prototype")
public class WebExportDisplay implements ExportDisplay {

    private static final Logger log = LoggerFactory.getLogger(WebExportDisplay.class);

    @Autowired
    protected BackgroundWorker backgroundWorker;

    @Autowired
    protected UiProperties uiProperties;

    protected Messages messages;

    protected FileStorageLocator fileStorageLocator;

    protected FileStorage fileStorage;

    protected boolean newWindow;

    // Use flags from app.properties for show/download files
    protected boolean useViewList = false;

    /**
     * Constructor with newWindow=false
     */
    public WebExportDisplay() {
        this(false);
        useViewList = true;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        this.fileStorageLocator = fileStorageLocator;
    }

    /**
     * @param newWindow if true, show data in the same browser window;
     *                  if false, open new browser window
     */
    public WebExportDisplay(boolean newWindow) {
        this.newWindow = newWindow;
    }

    /**
     * Show/Download resource at client side
     *
     * @param dataProvider ExportDataProvider
     * @param resourceName ResourceName for client side
     * @param exportFormat ExportFormat
     * @see io.jmix.ui.export.FileDataProvider
     * @see io.jmix.ui.export.ByteArrayDataProvider
     */
    @Override
    public void show(ExportDataProvider dataProvider, String resourceName, final ExportFormat exportFormat) {
        backgroundWorker.checkUIAccess();

        boolean showNewWindow = this.newWindow;

        if (useViewList) {
            String fileExt;

            if (exportFormat != null) {
                fileExt = exportFormat.getFileExt();
            } else {
                fileExt = FilenameUtils.getExtension(resourceName);
            }

            showNewWindow = uiProperties.getViewFileExtensions().contains(StringUtils.lowerCase(fileExt));
        }

        if (exportFormat != null) {
            if (StringUtils.isEmpty(FilenameUtils.getExtension(resourceName))) {
                resourceName += "." + exportFormat.getFileExt();
            }
        }

        JmixFileDownloader fileDownloader = AppUI.getCurrent().getFileDownloader();
        fileDownloader.setFileNotFoundExceptionListener(this::handleFileNotFoundException);

        StreamResource resource = new StreamResource(dataProvider::provide, resourceName);

        if (exportFormat != null && StringUtils.isNotEmpty(exportFormat.getContentType())) {
            resource.setMIMEType(exportFormat.getContentType());
        } else {
            resource.setMIMEType(FileTypesHelper.getMIMEType(resourceName));
        }

        if ((showNewWindow && isBrowserSupportsPopups()) || isIOS()) {
            fileDownloader.viewDocument(resource);
        } else {
            fileDownloader.downloadFile(resource);
        }
    }

    /**
     * Show/Download resource at client side
     *
     * @param dataProvider ExportDataProvider
     * @param resourceName ResourceName for client side
     * @see io.jmix.ui.export.FileDataProvider
     * @see io.jmix.ui.export.ByteArrayDataProvider
     */
    @Override
    public void show(ExportDataProvider dataProvider, String resourceName) {
        String extension = FilenameUtils.getExtension(resourceName);
        ExportFormat format = ExportFormat.getByExtension(extension);
        show(dataProvider, resourceName, format);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <R> void show(R fileReference, @Nullable ExportFormat format) {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        String fileName = fileStorage.getFileInfo(fileReference).toString();
        show(new FileDataProvider<>(fileReference), fileName, format);
    }

    @Override
    public void setFrame(Frame frame) {
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

    @Override
    public <R> void show(R fileReference) {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        String fileName = fileStorage.getFileInfo(fileReference).toString();
        ExportFormat format = ExportFormat.getByExtension(FilenameUtils.getExtension(fileName));
        show(new FileDataProvider<>(fileReference), format);
    }

    public void show(byte[] content, String resourceName, ExportFormat format) {
        show(new ByteArrayDataProvider(content), resourceName, format);
    }

    /**
     * @deprecated Use {@link #isShowNewWindow()}
     */
    @Deprecated
    public boolean isNewWindow() {
        return isShowNewWindow();
    }

    /**
     * @deprecated Use {@link #setShowNewWindow(boolean)}
     */
    @Deprecated
    public void setNewWindow(boolean newWindow) {
        this.setShowNewWindow(newWindow);
    }

    public boolean isBrowserSupportsPopups() {
        return !Page.getCurrent().getWebBrowser().isSafari();
    }

    protected boolean isIOS() {
        return Page.getCurrent().getWebBrowser().isIOS();
    }

    protected boolean handleFileNotFoundException(Exception exception, VaadinResponse response) {
        if (!(exception instanceof FileStorageException)) {
            return false;
        }

        FileStorageException storageException = (FileStorageException) exception;
        if (storageException.getType() == FileStorageException.Type.FILE_NOT_FOUND) {
            try {
                writeFileNotFoundException(response, messages.formatMessage(
                        getClass(), "fileNotFound.message", storageException.getFileName()));
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
}