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

import { css, html, LitElement } from 'lit';

import { DialogBaseMixin } from '@vaadin/dialog/src/vaadin-dialog-base-mixin.js';
import { ThemePropertyMixin } from '@vaadin/vaadin-themable-mixin/vaadin-theme-property-mixin.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';

import { ifDefined } from 'lit/directives/if-defined.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';

import { sidePanelLayoutDialog } from './styles/jmix-side-panel-layout-dialog-base-styles.js';
import './jmix-side-panel-layout-dialog-overlay.js';

class JmixSidePanelLayoutDialog extends DialogBaseMixin(ThemePropertyMixin(LumoInjectionMixin(PolylitMixin(LitElement)))) {

    static get is() {
        return 'jmix-side-panel-layout-dialog';
    }

    static get properties() {
        return {
            fullscreen: {
                type: Boolean,
            },
        };
    }

    static get styles() {
      return [sidePanelLayoutDialog];
    }

    render() {
        return html`
            <style>
                :host {
                    display: none;
                }
            </style>

            <jmix-side-panel-layout-dialog-overlay
                id="overlay"
                .opened="${this.opened}"
                @opened-changed="${this._onOverlayOpened}"
                @mousedown="${this._bringOverlayToFront}"
                @touchstart="${this._bringOverlayToFront}"
                @vaadin-overlay-outside-click="${this._close}"
                @vaadin-overlay-escape-press="${this._close}"
                theme="${ifDefined(this._theme)}"
                .modeless="${this.modeless}"
                .withBackdrop="${!this.modeless}"
                ?fullscreen="${this.fullscreen}"
                role="dialog"
                focus-trap
                exportparts="backdrop, overlay, content"
            ></jmix-side-panel-layout-dialog-overlay>
        `;
    }

    /**
     * @private
     */
    _close() {
      this.dispatchEvent(new CustomEvent('close'));
    }
}

defineCustomElement(JmixSidePanelLayoutDialog);