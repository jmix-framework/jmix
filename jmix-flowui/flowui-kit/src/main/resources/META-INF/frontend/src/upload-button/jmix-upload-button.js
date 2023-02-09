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
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/dialog/src/vaadin-dialog.js';

import {Upload} from '@vaadin/upload/src/vaadin-upload.js';

class JmixUploadButton extends Upload {

    static get is() {
        return 'jmix-upload-button';
    }

    static get properties() {
        return {
            readOnly: {
                type: Boolean,
                value: false,
            },
            enabled: {
                type: Boolean,
                value: true,
            },
            file: Object,
            jmixI18n: {
                type: Object,
                value: function () {
                    return {
                        uploadDialog: {
                            title: "Uploading",
                            cancel: "Cancel",
                        },
                    }
                },
                notify: true,
            }
        };
    }

    ready() {
        super.ready();

        this.$.fileList.hidden = true;
        this.$.dropLabelContainer.hidden = true;

        this.addEventListener("upload-progress", this._onUploadProgressEvent.bind(this));
        this.addEventListener('upload-success', this._onUploadSuccessEvent.bind(this));
        this.addEventListener('upload-error', this._onUploadFailedEvent.bind(this));

        this.addEventListener('file-abort', this._closeUploadDialogOnEvent.bind(this));
        this.addEventListener('file-reject', this._closeUploadDialogOnEvent.bind(this));
        this.addEventListener('upload-start', this._openUploadDialogOnEvent.bind(this));

        this._initUploadDialog();
    }

    static get observers() {
        return [
            '_onEnabledPropertyChanged(enabled)',
            '_onJmixI18nChanged(jmixI18n)',
        ]
    }

    /**
     * @param e
     * @private
     * @override
     */
    _onAddFilesTouchEnd(e) {
        // Don't open "add file" dialog if component is readOnly or disabled
        if (this.readOnly || !this.enabled) {
            e.stopPropagation();
            e.preventDefault();
            return
        }
        super._onAddFilesTouchEnd(e)
    }

    /**
     * @param e
     * @private
     * @override
     */
    _onAddFilesClick(e) {
        // Don't open "add file" dialog if component is readOnly or disabled
        if (this.readOnly || !this.enabled) {
            e.stopPropagation();
            e.preventDefault();
            return
        }
        super._onAddFilesClick(e)
    }

    _onUploadSuccessEvent(event) {
        // After uploading, button is not active because it reached files limit.
        // Clear uploaded files to enable uploading.
        this.files = [];
        this._closeUploadDialogOnEvent(event);
    }

    _onUploadFailedEvent(event) {
        // After failed uploading, button is not active because it reached files limit.
        // Clear uploaded files to enable uploading.
        this.files = [];
        this._closeUploadDialogOnEvent(event);

        // If server failed uploading file, e.g. TomCat threw FileSizeLimitExceededException,
        // the FailedEvent is not sent to the server-side of component. This is because the
        // exception is thrown before the upload request reaches StreamReceiverHandler (see Vaadin
        // issue https://github.com/vaadin/flow/issues/13770). So we send custom event to inform
        // that upload failed.
        this.dispatchEvent(new CustomEvent('jmix-upload-internal-error', {detail: {file: this.file, xhr: this.file.xhr}}));
    }

    _openUploadDialogOnEvent(event) {
        this._setUploadDialogOpened(true);
    }

    _closeUploadDialogOnEvent(event) {
        this._setUploadDialogOpened(false);
    }

    _initUploadDialog() {
        const uploadDialog = document.createElement("vaadin-dialog");
        uploadDialog.id = "jmixUploadDialog";
        uploadDialog.headerTitle = this.jmixI18n.uploadDialog.title;
        uploadDialog.noCloseOnOutsideClick = true;
        uploadDialog.noCloseOnEsc = true;
        uploadDialog.className = "jmix-upload-dialog";
        uploadDialog.renderer = this._uploadDialogRenderer();

        this.appendChild(uploadDialog);
    }

    _uploadDialogRenderer() {
        const uploadContext = this;
        return function (root, dialog) {
            if (root.children && root.children.length > 0) {
                const uploadFileElements = root.getElementsByTagName("vaadin-upload-file");
                if (uploadFileElements.length === 0) {
                    return;
                }
                const uploadFile = uploadFileElements[0];

                // hide control buttons
                const uploadFileButtons = uploadFile.shadowRoot.querySelectorAll("button");
                if (uploadFileButtons.length > 0) {
                    for (const btn of uploadFileButtons) {
                        btn.hidden = true;
                    }
                }

                // 'vaadin-upload-file' automatically updates bounded elements,
                // but using manually approach, it works only after value is unset.
                // Therefore, firstly unset the file, then set new one.
                uploadFile.file = {};
                uploadFile.file = uploadContext.file;
            } else {
                const content = document.createElement("div");
                content.className = "jmix-upload-dialog-content"

                const uploadFile = document.createElement("vaadin-upload-file");
                const cancelBtn = uploadContext._createUploadDialogCancelButton();

                uploadFile.file = uploadContext.file;

                content.appendChild(uploadFile);
                content.appendChild(cancelBtn);

                root.appendChild(content);
            }
        }
    }

    _createUploadDialogCancelButton() {
        const cancelBtn = document.createElement("vaadin-button");
        cancelBtn.className = "jmix-upload-dialog-cancel-button";
        cancelBtn.innerText = this.jmixI18n.uploadDialog.cancel;
        cancelBtn.addEventListener("click", this._onUploadDialogCancelButtonClick.bind(this));
        return cancelBtn;
    }

    _onUploadDialogCancelButtonClick(event) {
        this.dispatchEvent(new CustomEvent('file-abort', {detail: {file: this.file, xhr: this.file.xhr}}));
    }

    _onEnabledPropertyChanged(enabled) {
        // disable upload component
        const uploadComponent = this.shadowRoot.querySelector('slot').children[0];
        if (uploadComponent) {
            if (enabled) {
                uploadComponent.removeAttribute("disabled")
            } else {
                uploadComponent.setAttribute("disabled", "");
            }
        }
    }

    _onJmixI18nChanged(jmixI18n) {
        const dialog = this._getUploadDialog();
        if (dialog) {
            dialog.headerTitle = jmixI18n.uploadDialog.title;
        }
    }

    _onUploadProgressEvent(e) {
        this.file = e.detail.file;
        const dialog = this._getUploadDialog();
        if (dialog) {
            dialog.requestContentUpdate();
        }
    }

    _setUploadDialogOpened(opened) {
        const dialog = this._getUploadDialog();
        if (dialog) {
            dialog.opened = opened;
        }
    }

    _getUploadDialog() {
        const dialogs = this.getElementsByTagName("vaadin-dialog")
        if (dialogs.length <= 0) {
            return;
        }
        const dialog = Array.from(dialogs).filter((dialog) => {
            return dialog.id === "jmixUploadDialog";
        });
        return dialog.length > 0 ? dialog[0] : null;
    }
}

customElements.define(JmixUploadButton.is, JmixUploadButton);

export {JmixUploadButton};