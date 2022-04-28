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

import io.jmix.ui.widget.client.downloader.JmixFileDownloaderClientRPC;
import com.vaadin.server.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.UUID;

public class JmixFileDownloader extends AbstractExtension {

    public static final String DOWNLOAD_RESOURCE_PREFIX = "download-";
    public static final String VIEW_RESOURCE_PREFIX = "view-";

    protected boolean overrideContentType = true;

    protected FileNotFoundExceptionListener fileNotFoundExceptionListener;

    public interface FileNotFoundExceptionListener {
        boolean onHandle(Exception e, VaadinResponse response);
    }

    public void downloadFile(Resource resource) {
        String resourceId = DOWNLOAD_RESOURCE_PREFIX + UUID.randomUUID().toString();
        setResource(resourceId, resource);

        getRpcProxy(JmixFileDownloaderClientRPC.class).downloadFile(resourceId);
    }

    public void viewDocument(Resource resource) {
        String resourceId = VIEW_RESOURCE_PREFIX + UUID.randomUUID().toString();
        setResource(resourceId, resource);

        getRpcProxy(JmixFileDownloaderClientRPC.class).viewDocument(resourceId);
    }

    /**
     * Sets whether the content type of served resources should be overridden to
     * <code>application/octet-stream</code> to reduce the risk of a browser
     * plugin choosing to display the resource instead of downloading it. This
     * is by default set to <code>true</code>.
     * <p>
     * Please note that this only affects Connector resources (e.g.
     * {@link com.vaadin.server.FileResource} and {@link com.vaadin.server.ClassResource}) but not other resource
     * types (e.g. {@link com.vaadin.server.ExternalResource} or {@link com.vaadin.server.ThemeResource}).
     * </p>
     *
     * @param overrideContentType <code>true</code> to override the content type if possible;
     *                            <code>false</code> to use the original content type.
     */
    public void setOverrideContentType(boolean overrideContentType) {
        this.overrideContentType = overrideContentType;
    }

    /**
     * Checks whether the content type should be overridden.
     *
     * @return <code>true</code> if the content type will be overridden when
     * possible; <code>false</code> if the original content type will be
     * used.
     * @see #setOverrideContentType(boolean)
     */
    public boolean isOverrideContentType() {
        return overrideContentType;
    }

    @Override
    public void extend(AbstractClientConnector target) {
        super.extend(target);
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response,
                                          String path) throws IOException {
        if (path == null) {
            return false;
        }

        String targetResourceKey;
        DownloadStream stream;

        VaadinSession session = getSession();
        session.lock();
        try {
            String[] parts = path.split("/", 2);
            targetResourceKey = parts[0];
            if (targetResourceKey.isEmpty()) {
                return false;
            }

            Resource resource = getResource(targetResourceKey);
            if (resource == null) {
                return false;
            }

            boolean isViewDocumentRequest = targetResourceKey.startsWith(VIEW_RESOURCE_PREFIX);

            try {
                stream = ((ConnectorResource) resource).getStream();
            } catch (RuntimeException e) {
                if (!isViewDocumentRequest
                        || fileNotFoundExceptionListener == null
                        || !fileNotFoundExceptionListener.onHandle(e, response)) {
                    // send exception further
                    throw e;
                } else {
                    // exception is handled in listener
                    return true;
                }
            }

            String contentDisposition = stream.getParameter(DownloadStream.CONTENT_DISPOSITION);
            if (contentDisposition == null) {
                // Content-Disposition: attachment generally forces download
                contentDisposition = (isViewDocumentRequest ? "inline" : "attachment") + "; " +
                        DownloadStream.getContentDispositionFilename(stream.getFileName());
            }

            stream.setParameter(DownloadStream.CONTENT_DISPOSITION, contentDisposition);

            // Content-Type to block eager browser plug-ins from hijacking the file
            if (isOverrideContentType() && !isViewDocumentRequest && !isSafariOrIOS()) {
                stream.setContentType("application/octet-stream;charset=UTF-8");
            } else {
                if (StringUtils.isNotEmpty(stream.getContentType())) {
                    stream.setContentType(stream.getContentType() + ";charset=UTF-8");
                } else {
                    stream.setContentType(";charset=UTF-8");
                }
            }
        } finally {
            session.unlock();
        }

        stream.writeResponse(request, response);
        return true;
    }

    protected boolean isSafariOrIOS() {
        return Page.getCurrent().getWebBrowser().isSafari()
                || Page.getCurrent().getWebBrowser().isIOS();
    }

    public FileNotFoundExceptionListener getFileNotFoundExceptionListener() {
        return fileNotFoundExceptionListener;
    }

    public void setFileNotFoundExceptionListener(FileNotFoundExceptionListener notFoundExceptionListener) {
        this.fileNotFoundExceptionListener = notFoundExceptionListener;
    }
}