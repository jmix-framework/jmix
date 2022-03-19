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

package io.jmix.ui.widget.client.jqueryfileupload;

import io.jmix.ui.widget.JmixFileUpload;
import io.jmix.ui.widget.client.fileupload.JmixFileUploadClientRpc;
import io.jmix.ui.widget.client.fileupload.JmixFileUploadServerRpc;
import io.jmix.ui.widget.client.fileupload.JmixFileUploadState;
import com.vaadin.client.*;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.Icon;
import com.vaadin.shared.ui.Connect;

@Connect(JmixFileUpload.class)
public class JmixFileUploadConnector extends AbstractComponentConnector implements Paintable {

    public JmixFileUploadConnector() {
        //noinspection Convert2Lambda
        registerRpc(JmixFileUploadClientRpc.class, new JmixFileUploadClientRpc() {
            @Override
            public void continueUploading() {
                // check if attached
                if (getWidget().isAttached()) {
                    getWidget().continueUploading();
                }
            }
        });
    }

    @Override
    public boolean delegateCaptionHandling() {
        return false;
    }

    @Override
    public JmixFileUploadWidget getWidget() {
        return (JmixFileUploadWidget) super.getWidget();
    }

    @Override
    public JmixFileUploadState getState() {
        return (JmixFileUploadState) super.getState();
    }

    @Override
    public void onUnregister() {
        super.onUnregister();

        JmixFileUploadWidget widget = getWidget();
        widget.setIgnoreExceptions(true);
        widget.cancelAllUploads();
        widget.setDropZone(null, null);
    }

    @Override
    protected void init() {
        super.init();

        getWidget().filePermissionsHandler = new JmixFileUploadWidget.FilePermissionsHandler() {
            @Override
            public void fileSizeLimitExceeded(String filename) {
                getRpcProxy(JmixFileUploadServerRpc.class).fileSizeLimitExceeded(filename);
            }

            @Override
            public void fileExtensionNotAllowed(String filename) {
                getRpcProxy(JmixFileUploadServerRpc.class).fileExtensionNotAllowed(filename);
            }
        };

        getWidget().queueUploadListener = new JmixFileUploadWidget.QueueUploadListener() {
            @Override
            public void uploadFinished() {
                // send events to server only if widget is still attached to UI
                if (getWidget().isAttached()) {
                    getRpcProxy(JmixFileUploadServerRpc.class).queueUploadFinished();
                }
            }
        };

        getWidget().fileUploadedListener = new JmixFileUploadWidget.FileUploadedListener() {
            @Override
            public void fileUploaded(String fileName) {
                // send events to server only if widget is still attached to UI
                if (getWidget().isAttached()) {
                    getRpcProxy(JmixFileUploadServerRpc.class).fileUploaded(fileName);
                }
            }
        };
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("caption")
                || stateChangeEvent.hasPropertyChanged("captionAsHtml")) {
            VCaption.setCaptionText(getWidget().submitButton.captionElement, getState());

            if ("".equals(getState().caption) || getState().caption == null) {
                getWidget().submitButton.addStyleDependentName("empty-caption");
            } else {
                getWidget().submitButton.removeStyleDependentName("empty-caption");
            }
        }

        if (stateChangeEvent.hasPropertyChanged("resources")) {
            if (getWidget().submitButton.icon != null) {
                getWidget().submitButton.wrapper.removeChild(getWidget().submitButton.icon.getElement());
                getWidget().submitButton.icon = null;
            }
            Icon icon = getIcon();
            if (icon != null) {
                getWidget().submitButton.icon = icon;
                if (getState().iconAltText != null) {
                    icon.setAlternateText(getState().iconAltText);
                } else {
                    icon.setAlternateText("");
                }

                getWidget().submitButton.wrapper.insertBefore(icon.getElement(),
                        getWidget().submitButton.captionElement);
            }
        }

        if (stateChangeEvent.hasPropertyChanged("multiSelect")) {
            getWidget().setMultiSelect(getState().multiSelect);
        }

        if (stateChangeEvent.hasPropertyChanged("iconAltText")) {
            if (getWidget().submitButton.icon != null) {
                Icon icon = getWidget().submitButton.icon;
                if (getState().iconAltText != null) {
                    icon.setAlternateText(getState().iconAltText);
                } else {
                    icon.setAlternateText("");
                }
            }
        }

        if (stateChangeEvent.hasPropertyChanged("progressWindowCaption")) {
            getWidget().progressWindowCaption = getState().progressWindowCaption;
        }

        if (stateChangeEvent.hasPropertyChanged("cancelButtonCaption")) {
            getWidget().cancelButtonCaption = getState().cancelButtonCaption;
        }

        if (stateChangeEvent.hasPropertyChanged("unableToUploadFileMessage")) {
            getWidget().unableToUploadFileMessage = getState().unableToUploadFileMessage;
        }

        if (stateChangeEvent.hasPropertyChanged("accept")) {
            getWidget().setAccept(getState().accept);
        }

        if (stateChangeEvent.hasPropertyChanged("fileSizeLimit")) {
            getWidget().fileSizeLimit = getState().fileSizeLimit;
        }

        if (stateChangeEvent.hasPropertyChanged("permittedExtensions")) {
            getWidget().permittedExtensions = getState().permittedExtensions;
        }

        if (stateChangeEvent.hasPropertyChanged("dropZone")) {
            ComponentConnector dropZone = (ComponentConnector) getState().dropZone;

            getWidget().setDropZone(dropZone != null ? dropZone.getWidget() : null, getState().dropZonePrompt);
        }

        if (stateChangeEvent.hasPropertyChanged("pasteZone")) {
            ComponentConnector pasteZone = (ComponentConnector) getState().pasteZone;

            getWidget().setPasteZone(pasteZone != null ? pasteZone.getWidget() : null);
        }

        if (!isEnabled()) {
            getWidget().disableUpload();
        } else {
            getWidget().enableUpload();
        }
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (!isRealUpdate(uidl)) {
            return;
        }

        final String uploadUrl = client.translateVaadinUri(uidl
                .getStringVariable("uploadUrl"));

        getWidget().setUploadUrl(uploadUrl);
    }
}
