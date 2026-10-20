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

import '@vaadin/input-container/src/vaadin-input-container.js';
import '@vaadin/upload/src/vaadin-upload-drop-zone.js';
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
 * and links its input area, a `vaadin-upload-drop-zone`, to the same manager.
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
            'upload-success': this._onUploadSuccess.bind(this),
            'upload-error': this._onUploadError.bind(this),
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
    }

    /** @private */
    _onAcceptedFileTypesChanged(acceptedFileTypes, oldAcceptedFileTypes) {
        if (!this._manager) {
            return;
        }

        if (acceptedFileTypes) {
            this._manager.accept = acceptedFileTypes;
        } else if (oldAcceptedFileTypes) {
            this._manager.accept = '';
        }
    }

    /** @private */
    _onUploadSuccess() {
        // The field takes one file at a time: the uploaded file would otherwise keep
        // the button and the drop zone disabled.
        this._manager.files = [];
    }

    /** @private */
    _onUploadError(event) {
        this._manager.files = [];

        this.dispatchEvent(new CustomEvent('jmix-upload-internal-error', {
            detail: {file: event.detail.file},
        }));
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