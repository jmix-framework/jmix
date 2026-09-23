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
import {UploadManager} from '@vaadin/upload/src/vaadin-upload-manager.js';
import {jmixUploadFieldStyles} from "./styles/jmix-upload-field-base-styles";

/**
 * Bridges the field's `vaadin-upload-drop-zone` to the `jmix-upload-button`.
 *
 * The drop zone hands dropped files to an `UploadManager` and treats itself as
 * disabled unless it is linked to one, while the upload button is a
 * `vaadin-upload` that keeps its own file list and request handling. This
 * adapter satisfies the link and forwards the files to the button, so a dropped
 * file goes through the same accepted types, size and rejection handling as a
 * file picked through the button. It never uploads anything itself.
 *
 * @private
 */
class JmixUploadFieldDropManager extends UploadManager {

    constructor(field) {
        super();

        this.field = field;
    }

    /** @override */
    addFiles(files) {
        // The button is a light DOM descendant added by the server-side component,
        // so it is resolved on drop rather than when the manager is created.
        const uploadButton = this.field.querySelector('jmix-upload-button');
        if (uploadButton) {
            uploadButton._addFiles(files);
        }
    }
}

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

    /** @protected */
    get slotStyles() {
        return [];
    }

    /** @protected */
    ready() {
        super.ready();

        this._dropZone = this.shadowRoot.querySelector('vaadin-upload-drop-zone');
        this._dropZone.manager = new JmixUploadFieldDropManager(this);
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
    disconnectedCallback() {
        super.disconnectedCallback();

        // A detach in the middle of a drag would otherwise leave the highlight painted.
        this._onDragDone();
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