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
import '@vaadin/input-container/src/vaadin-input-container.js';
import '@vaadin/upload/src/vaadin-upload-drop-zone.js';
import '@vaadin/upload/src/vaadin-upload-file-list.js';
import {html, LitElement} from 'lit';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ifDefined} from 'lit/directives/if-defined.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {TooltipController} from '@vaadin/component-base/src/tooltip-controller.js';
import {DelegateFocusMixin} from '@vaadin/a11y-base/src/delegate-focus-mixin.js';
import {FieldMixin} from '@vaadin/field-base/src/field-mixin.js';
import {InputConstraintsMixin} from '@vaadin/field-base/src/input-constraints-mixin.js';
import {SlotStylesMixin} from '@vaadin/component-base/src/slot-styles-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {jmixUploadFieldStyles} from "./styles/jmix-upload-field-base-styles";

/**
 * `<jmix-upload-field>` is the client part of the single file upload fields.
 *
 * The server-side component owns a Vaadin upload manager and slots a `vaadin-upload-button`
 * linked to it. The field takes the client manager from that button (see `_syncUploadManager`)
 * and links its input area, a `vaadin-upload-drop-zone`, to the same manager. While a file is
 * uploading, the field shows its progress in a dialog with a Cancel button.
 *
 * @fires {CustomEvent} jmix-upload-internal-error - Fired when an upload fails, with the file in `detail.file`.
 */
export class JmixUploadField extends SlotStylesMixin(DelegateFocusMixin(InputConstraintsMixin(FieldMixin(ThemableMixin(
    ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement)))))))) {

    static get is() {
        return 'jmix-upload-field';
    }

    static get styles() {
        return jmixUploadFieldStyles;
    }

    render() {
        return html`
            <div class="upload-field-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
                </div>

                <vaadin-upload-drop-zone part="input-field"
                                         theme="${ifDefined(this._theme)}">
                    <slot name="input"></slot>
                </vaadin-upload-drop-zone>

                <div part="helper-text">
                    <slot name="helper"></slot>
                </div>

                <div part="error-message">
                    <slot name="error-message"></slot>
                </div>

                <slot name="tooltip"></slot>

                <slot name="dialog"></slot>
            </div>
        `;
    }

    static get properties() {
        return {
            /**
             * Whether files can be dropped onto the field. The server-side component
             * pushes the effective value here, so this is already combined with the
             * field's read-only and enabled state.
             */
            dropAllowed: {
                type: Boolean,
                value: true,
                observer: '_onDropAllowedChanged',
            },

            /**
             * The deprecated accepted file types joined with commas. A client-side hint only:
             * it is applied to the upload manager's `accept`, which filters the picker and
             * rejects other files, any token matching.
             */
            acceptedFileTypes: {
                type: String,
                observer: '_onAcceptedFileTypesChanged',
            },

            /**
             * The localized texts of the field: the upload dialog's title and Cancel button,
             * and the file status and error texts shown in the dialog.
             */
            jmixI18n: {
                type: Object,
                observer: '_onJmixI18nChanged',
            },

            /**
             * Set while files are dragged over the input area. Drives the highlight.
             *
             * @private
             */
            _dragover: {
                type: Boolean,
                value: false,
                reflectToAttribute: true,
                attribute: 'dragover',
                sync: true,
            }
        };
    }

    /**
     * Fired when an upload fails. A failure before the request reaches the upload handler,
     * e.g. on the container's request size limit, never reaches the server-side listeners,
     * so the server-side component listens to this event instead.
     *
     * @event jmix-upload-internal-error
     */

    constructor() {
        super();

        this.__managerListeners = {
            'upload-start': this._onUploadStart.bind(this),
            'upload-success': this._onUploadSuccess.bind(this),
            'upload-error': this._onUploadError.bind(this),
            'upload-abort': this._onUploadAbort.bind(this),
            'file-remove': this._onFileRemove.bind(this),
        };
    }

    /** @protected */
    get slotStyles() {
        return [];
    }

    /** @protected */
    ready() {
        super.ready();

        this._dropZone = this.shadowRoot.querySelector('vaadin-upload-drop-zone');
        // The server may have linked the manager before the first render.
        this._dropZone.manager = this._manager || null;
        this._updateDropZoneDisabled();

        this._dropZone.addEventListener('dragenter', this._onDragEnter.bind(this));
        this._dropZone.addEventListener('dragleave', this._onDragLeave.bind(this));
        this._dropZone.addEventListener('drop', this._onDragDone.bind(this));
        this._dropZone.addEventListener('dragend', this._onDragDone.bind(this));

        this._tooltipController = new TooltipController(this);
        this._tooltipController.setPosition('top');
        this._tooltipController.setAriaTarget(this.inputElement);
        this.addController(this._tooltipController);
    }

    /** @protected */
    connectedCallback() {
        super.connectedCallback();

        // A move on the client only, e.g. into a dialog, gets no new link from the server,
        // and the button keeps its manager.
        if (this._button) {
            this._syncUploadManager(this._button);
        }
    }

    /** @protected */
    disconnectedCallback() {
        super.disconnectedCallback();

        // A detach in the middle of a drag would otherwise leave the highlight painted.
        this._onDragDone();

        // A re-attach on the server creates a new manager and links it again.
        this._setManager(null);
        this._setUploadDialogOpened(false);
    }

    /**
     * Takes the upload manager of the given upload button. The server-side component calls it
     * after Vaadin links the button to the manager, on every attach of the button.
     *
     * @param {HTMLElement} button the `vaadin-upload-button` of the field
     * @protected
     */
    _syncUploadManager(button) {
        this._button = button;
        this._setManager(button.manager || null);
    }

    /** @private */
    _setManager(manager) {
        if (manager === this._manager) {
            return;
        }

        if (this._manager) {
            Object.entries(this.__managerListeners)
                .forEach(([type, listener]) => this._manager.removeEventListener(type, listener));
        }

        this._manager = manager;

        if (manager) {
            Object.entries(this.__managerListeners)
                .forEach(([type, listener]) => manager.addEventListener(type, listener));

            // An empty hint is never written here: it would wipe the accept value
            // pushed by the server for the accepted MIME types and file extensions.
            if (this.acceptedFileTypes) {
                manager.accept = this.acceptedFileTypes;
            }
        }

        if (this._dropZone) {
            this._dropZone.manager = manager;
        }

        if (this._uploadFileList) {
            this._uploadFileList.manager = manager;
        }
    }

    /** @private */
    _onAcceptedFileTypesChanged(acceptedFileTypes, oldAcceptedFileTypes) {
        if (!this._manager) {
            return;
        }

        if (acceptedFileTypes) {
            this._manager.accept = acceptedFileTypes;
        } else if (oldAcceptedFileTypes && this._manager.accept === oldAcceptedFileTypes) {
            // The observer runs after the whole server response was applied: the accepted MIME types or
            // extensions set in the same response have already replaced the hint and must stay.
            this._manager.accept = '';
        }
    }

    /** @private */
    _onJmixI18nChanged() {
        if (this._uploadDialog) {
            this._uploadDialog.headerTitle = this._getUploadDialogTitle();
        }

        if (this._uploadCancelButton) {
            this._uploadCancelButton.textContent = this._getUploadDialogCancelText();
        }

        if (this._uploadFileList && this.jmixI18n) {
            this._uploadFileList.i18n = this.jmixI18n;
        }
    }

    /** @private */
    _onUploadStart() {
        this._setUploadDialogOpened(true);
    }

    /** @private */
    _onUploadSuccess() {
        this._setUploadDialogOpened(false);

        // The field takes one file at a time: the uploaded file would otherwise keep
        // the button and the drop zone disabled.
        this._manager.files = [];
    }

    /** @private */
    _onUploadError(event) {
        this._setUploadDialogOpened(false);

        this._manager.files = [];

        this.dispatchEvent(new CustomEvent('jmix-upload-internal-error', {
            detail: {file: event.detail.file},
        }));
    }

    /** @private */
    _onUploadAbort() {
        this._setUploadDialogOpened(false);
    }

    /** @private */
    _onFileRemove() {
        this._setUploadDialogOpened(false);
    }

    /** @private */
    _setUploadDialogOpened(opened) {
        if (opened) {
            this._getUploadDialog().opened = true;
        } else if (this._uploadDialog) {
            this._uploadDialog.opened = false;
        }
    }

    /**
     * The dialog is created on the first upload. It is a light DOM child in the `dialog` slot,
     * so it is rendered, moves with the field, and the global `.jmix-upload-dialog*` styles apply.
     *
     * @private
     */
    _getUploadDialog() {
        if (!this._uploadDialog) {
            const dialog = document.createElement('vaadin-dialog');
            dialog.slot = 'dialog';
            dialog.className = 'jmix-upload-dialog';
            dialog.noCloseOnOutsideClick = true;
            dialog.noCloseOnEsc = true;
            dialog.headerTitle = this._getUploadDialogTitle();
            dialog.renderer = this._renderUploadDialog.bind(this);

            this.appendChild(dialog);
            this._uploadDialog = dialog;
        }

        return this._uploadDialog;
    }

    /** @private */
    _renderUploadDialog(root) {
        if (root.firstElementChild) {
            return;
        }

        const content = document.createElement('div');
        content.className = 'jmix-upload-dialog-content';

        // The list formats the progress and status texts of the uploading file from its i18n.
        const fileList = document.createElement('vaadin-upload-file-list');
        if (this.jmixI18n) {
            fileList.i18n = this.jmixI18n;
        }
        fileList.manager = this._manager || null;

        const cancelButton = document.createElement('vaadin-button');
        cancelButton.className = 'jmix-upload-dialog-cancel-button';
        cancelButton.textContent = this._getUploadDialogCancelText();
        cancelButton.addEventListener('click', this._onUploadDialogCancelClick.bind(this));

        content.append(fileList, cancelButton);
        root.appendChild(content);

        this._uploadFileList = fileList;
        this._uploadCancelButton = cancelButton;
    }

    /** @private */
    _onUploadDialogCancelClick() {
        const file = this._manager && this._manager.files.find((f) => f.uploading);
        if (file) {
            this._manager.abortUpload(file);
        } else {
            this._setUploadDialogOpened(false);
        }
    }

    /** @private */
    _getUploadDialogTitle() {
        return this.jmixI18n?.uploadDialog?.title ?? 'Uploading';
    }

    /** @private */
    _getUploadDialogCancelText() {
        return this.jmixI18n?.uploadDialog?.cancel ?? 'Cancel';
    }

    /** @private */
    _onDropAllowedChanged() {
        this._updateDropZoneDisabled();

        if (!this.dropAllowed) {
            this._onDragDone();
        }
    }

    /** @private */
    _updateDropZoneDisabled() {
        if (this._dropZone) {
            this._dropZone.disabled = !this.dropAllowed;
        }
    }

    /**
     * The highlight is driven by a depth counter rather than by the drop zone's own
     * `dragover` attribute. The element clears that attribute only on a `dragleave`
     * targeting the element itself, so a pointer leaving through the upload button or
     * the file name leaves it stuck on. Every `dragenter` is balanced by a `dragleave`,
     * so the counter reaches zero exactly when the drag leaves the input area, and does
     * not flicker while the pointer crosses between children.
     *
     * @private
     */
    _onDragEnter() {
        // A safety net in case the link from the server came before the button had a manager:
        // the drop zone ignores drops while it has none.
        if (this._button && this._button.manager !== this._manager) {
            this._syncUploadManager(this._button);
        }

        if (!this.dropAllowed) {
            return;
        }

        this._dragDepth = (this._dragDepth || 0) + 1;
        this._dragover = true;
    }

    /** @private */
    _onDragLeave() {
        this._dragDepth = Math.max(0, (this._dragDepth || 0) - 1);

        if (this._dragDepth === 0) {
            this._dragover = false;
        }
    }

    /** @private */
    _onDragDone() {
        this._dragDepth = 0;
        this._dragover = false;
    }
}

defineCustomElement(JmixUploadField);