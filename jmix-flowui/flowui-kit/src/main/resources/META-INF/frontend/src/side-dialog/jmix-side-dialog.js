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
                resizable$="[[resizable]]"
                draggable$="[[draggable]]"
                restore-focus-on-close
                focus-trap
                side-panel-placement$="[[sidePanelPlacement]]"
            ></jmix-side-dialog-overlay>
        `;
    }

    static get properties() {
        return {
            sidePanelPlacement: {
                type: String,
                reflectToAttribute: true,
                value: 'right',
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

    ready() {
        super.ready();

        this.addController(
            new MediaQueryController(this._fullscreenMediaQuery, (matches) => {
                const fullscreen = this.fullscreenOnSmallDevices ? matches : false;
                this.$.overlay.toggleAttribute('fullscreen', fullscreen);
            }),
        );
    }
}

defineCustomElement(JmixSideDialog);

export { JmixSideDialog };