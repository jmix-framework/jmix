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

    _updateHorizontalSizes(size, minSize, maxSize) {
        let resultSize = size;
        if (!size) {
            const cssValue = this.$.overlay._getStylePropertyValue('--jmix-side-dialog-horizontal-size');
            resultSize = cssValue ? cssValue : this.$.overlay._getStylePropertyValue('--_default-horizontal-size');;
        }
        this.$.overlay.style.setProperty('--_horizontal-size', resultSize);

        let resultMinSize = minSize;
        if (!minSize) {
            const cssValue = this.$.overlay._getStylePropertyValue('--jmix-side-dialog-horizontal-min-size');
            resultMinSize = cssValue ? cssValue : this.$.overlay._getStylePropertyValue('--_default-horizontal-min-size');
        }
        this.$.overlay.style.setProperty('--_horizontal-min-size', resultMinSize);

        let resultMaxSize = maxSize;
        if (!maxSize) {
            const cssValue = this.$.overlay._getStylePropertyValue('--jmix-side-dialog-horizontal-max-size');
            resultMaxSize = cssValue ? cssValue : this.$.overlay._getStylePropertyValue('--_default-horizontal-max-size');
        }
        this.$.overlay.style.setProperty('--_horizontal-max-size', resultMaxSize);
    }

    _updateVerticalSizes(size, minSize, maxSize) {
        let resultSize = size;
        if (!size) {
            const cssValue = this.$.overlay._getStylePropertyValue('--jmix-side-dialog-vertical-size');
            resultSize = cssValue ? cssValue : this.$.overlay._getStylePropertyValue('--_default-vertical-size');;
        }
        this.$.overlay.style.setProperty('--_vertical-size', resultSize);

        let resultMinSize = minSize;
        if (!minSize) {
            const cssValue = this.$.overlay._getStylePropertyValue('--jmix-side-dialog-vertical-min-size');
            resultMinSize = cssValue ? cssValue : this.$.overlay._getStylePropertyValue('--_default-vertical-min-size');
        }
        this.$.overlay.style.setProperty('--_vertical-min-size', resultMinSize);

        let resultMaxSize = maxSize;
        if (!maxSize) {
            const cssValue = this.$.overlay._getStylePropertyValue('--jmix-side-dialog-vertical-max-size');
            resultMaxSize = cssValue ? cssValue : this.$.overlay._getStylePropertyValue('--_default-vertical-max-size');
        }
        this.$.overlay.style.setProperty('--_vertical-max-size', resultMaxSize);
    }
}

defineCustomElement(JmixSideDialog);

export { JmixSideDialog };