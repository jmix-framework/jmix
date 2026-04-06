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

import { html, LitElement } from 'lit';
import { ifDefined } from 'lit/directives/if-defined.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DirMixin } from '@vaadin/component-base/src/dir-mixin.js';
import { OverlayMixin } from '@vaadin/overlay/src/vaadin-overlay-mixin.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

import { sidePanelLayoutDialogOverlayStyles } from './styles/jmix-side-panel-layout-dialog-overlay-base-styles.js';

class JmixSidePanelLayoutDialogOverlay extends OverlayMixin(DirMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
      return 'jmix-side-panel-layout-dialog-overlay';
    }

    static get styles() {
      return sidePanelLayoutDialogOverlayStyles;
    }

    /**
     * Override method from OverlayFocusMixin to use dialog as focus trap root.
     * @protected
     * @override
     */
    get _focusTrapRoot() {
      return this.getRootNode().host;
    }

    render() {
        return html`
            <div part="backdrop" id="backdrop" ?hidden="${!this.withBackdrop}"></div>
            <div part="overlay" id="overlay">
                <section id="resizerContainer" class="resizer-container">
                    <div part="content" id="content">
                        <slot name="sidePanelContentSlot"></slot>
                    </div>
                </section>
            </div>
        `;
    }
}

defineCustomElement(JmixSidePanelLayoutDialogOverlay);

export { JmixSidePanelLayoutDialogOverlay };