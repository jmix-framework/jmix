/*
 * Copyright 2026 Haulmont.
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

import { html } from '@polymer/polymer/polymer-element.js';
import { MediaQueryController } from '@vaadin/component-base/src/media-query-controller.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ControllerMixin } from '@vaadin/component-base/src/controller-mixin.js';
import { Dialog } from '@vaadin/dialog/src/vaadin-dialog.js';

import './jmix-side-dialog-overlay.js';

class JmixSideDialog extends ControllerMixin(Dialog) {

    static get is() {
        return 'jmix-side-dialog';
    }

    static get template() {
        return html`
            <style>
                :host {
                  display: none !important;
                }
            </style>

            <jmix-side-dialog-overlay
                id="overlay"
                role$="[[overlayRole]]"
                header-title="[[headerTitle]]"
                on-opened-changed="_onOverlayOpened"
                on-mousedown="_bringOverlayToFront"
                on-touchstart="_bringOverlayToFront"
                theme$="[[_theme]]"
                modeless="[[modeless]]"
                with-backdrop="[[!modeless]]"
                restore-focus-on-close
                focus-trap
                side-dialog-placement$="[[sideDialogPlacement]]"
            ></jmix-side-dialog-overlay>
        `;
    }

    static get properties() {
        return {
            sideDialogPlacement: {
                type: String,
                reflectToAttribute: true,
                value: 'right',
                notify: true,
                sync: true,
            },
            horizontalSize: {
                type: String,
                notify: true,
                sync: true,
            },
            horizontalMinSize: {
                type: String,
                notify: true,
                sync: true,
            },
            horizontalMaxSize: {
                type: String,
                notify: true,
                sync: true,
            },
            verticalSize: {
                type: String,
                notify: true,
                sync: true,
            },
            verticalMinSize: {
                type: String,
                notify: true,
                sync: true,
            },
            verticalMaxSize: {
                type: String,
                notify: true,
                sync: true,
            },
            fullscreenOnSmallDevices: {
                type: Boolean,
                value: true,
            },
            _fullscreenMediaQuery: {
                type: String,
                value: '(max-width: 600px), (max-height: 600px)',
            },
        }
    }

    static get observers() {
        return [
            '_updateHorizontalSizes(horizontalSize, horizontalMinSize, horizontalMaxSize)',
            '_updateVerticalSizes(verticalSize, verticalMinSize, verticalMaxSize)',
            '_onSideDialogPlacementChanged(sideDialogPlacement)',
        ];
    }

    ready() {
        super.ready();

        this.addController(
            new MediaQueryController(this._fullscreenMediaQuery, (matches) => {
                const fullscreen = this.fullscreenOnSmallDevices ? matches : false;
                this.$.overlay.toggleAttribute('fullscreen', fullscreen);
            }),
        );
    }

    /**
     * Observer for the `horizontalSize`, `horizontalMinSize` and `horizontalMaxSize` properties.
     *
     * @protected
     */
    _updateHorizontalSizes(size, minSize, maxSize) {
        if (this.sideDialogPlacement !== 'right'
                 && this.sideDialogPlacement !== 'left'
                 && this.sideDialogPlacement !== 'inline-start'
                 && this.sideDialogPlacement !== 'inline-end') {
            this.$.overlay.$.overlay.style.removeProperty('width');
            this.$.overlay.$.overlay.style.removeProperty('min-width');
            this.$.overlay.$.overlay.style.removeProperty('max-width');
            return;
        }

        if (size) {
            this.$.overlay.$.overlay.style.setProperty('width', size);
        } else {
            this.$.overlay.$.overlay.style.removeProperty('width');
        }

        if (minSize) {
            this.$.overlay.$.overlay.style.setProperty('min-width', minSize);
        } else {
            this.$.overlay.$.overlay.style.removeProperty('min-width');
        }

        if (maxSize) {
            this.$.overlay.$.overlay.style.setProperty('max-width', maxSize);
        } else {
            this.$.overlay.$.overlay.style.removeProperty('max-width');
        }
    }

    /**
     * Observer for the `verticalSize`, `verticalMinSize` and `verticalMaxSize` properties.
     *
     * @protected
     */
    _updateVerticalSizes(size, minSize, maxSize) {
        if (this.sideDialogPlacement !== 'top' && this.sideDialogPlacement !== 'bottom') {
            this.$.overlay.$.overlay.style.removeProperty('height');
            this.$.overlay.$.overlay.style.removeProperty('min-height');
            this.$.overlay.$.overlay.style.removeProperty('max-height');
            return;
        }

        if (size) {
            this.$.overlay.$.overlay.style.setProperty('height', size);
        } else {
            this.$.overlay.$.overlay.style.removeProperty('height');
        }

        if (minSize) {
            this.$.overlay.$.overlay.style.setProperty('min-height', minSize);
        } else {
            this.$.overlay.$.overlay.style.removeProperty('min-height');
        }

       if (maxSize) {
            this.$.overlay.$.overlay.style.setProperty('max-height', maxSize);
       } else {
            this.$.overlay.$.overlay.style.removeProperty('max-height');
       }
    }

    /**
     * Observer for the `sideDialogPlacement` property.
     *
     * @protected
     */
    _onSideDialogPlacementChanged(placement) {
        this._updateSizes();
    }

    /**
     * Server callable function.
     *
     * @private
     */
    _updateSizes() {
        this._updateHorizontalSizes(this.horizontalSize, this.horizontalMinSize, this.horizontalMaxSize);
        this._updateVerticalSizes(this.verticalSize, this.verticalMinSize, this.verticalMaxSize);
    }
}

defineCustomElement(JmixSideDialog);

export { JmixSideDialog };