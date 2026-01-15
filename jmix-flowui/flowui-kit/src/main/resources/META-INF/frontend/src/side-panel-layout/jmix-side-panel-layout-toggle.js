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

import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { buttonStyles } from '@vaadin/button/src/vaadin-button-core-styles.js';
import { ButtonMixin } from '@vaadin/button/src/vaadin-button-mixin.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DirMixin } from '@vaadin/component-base/src/dir-mixin.js';
import { isEmptyTextNode } from '@vaadin/component-base/src/dom-utils.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { jmixSidePanelToggleLayout } from './jmix-side-panel-layout-toggle-styles.js';

registerStyles('jmix-side-panel-layout-toggle', [buttonStyles, jmixSidePanelToggleLayout], { moduleId: 'jmix-side-panel-layout-toggle-styles' });

class JmixSidePanelLayoutToggle extends ButtonMixin(DirMixin(ThemableMixin(PolymerElement))) {

    static get is() {
        return 'jmix-side-panel-layout-toggle';
    }

    static get template() {
        return html`
            <slot id="slot">
                <div part="icon"></div>
            </slot>
            <div part="icon" hidden$="[[!_showFallbackIcon]]"></div>
        `;
    }

    static get properties() {
       return {
           ariaLabel: {
               type: String,
               reflectToAttribute: true,
           },

           _showFallbackIcon: {
               type: Boolean,
               value: false,
           },
       };
    }

    constructor() {
        super();
    }

    ready() {
        super.ready();

        this._toggleFallbackIcon();

        this.$.slot.addEventListener('slotchange', () => this._toggleFallbackIcon());
    }

    _toggleFallbackIcon() {
      const nodes = this.$.slot.assignedNodes();

      // Show fallback icon if there are 1-2 empty text nodes assigned to the default slot.
      this._showFallbackIcon = nodes.length > 0 && nodes.every((node) => isEmptyTextNode(node));
    }
}

defineCustomElement(JmixSidePanelLayoutToggle);

export { JmixSidePanelLayoutToggle };
