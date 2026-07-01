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
import { ButtonMixin } from '@vaadin/button/src/vaadin-button-mixin.js';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DirMixin } from '@vaadin/component-base/src/dir-mixin.js';
import { isEmptyTextNode } from '@vaadin/component-base/src/dom-utils.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { jmixSidePanelLayoutCloserStyles } from './styles/jmix-side-panel-layout-closer-base-styles.js';

class JmixSidePanelLayoutCloser extends ButtonMixin(DirMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
        return 'jmix-side-panel-layout-closer';
    }

    static get styles() {
        return jmixSidePanelLayoutCloserStyles;
    }

    static get properties() {
        return {
            ariaLabel: {
                type: String,
                reflectToAttribute: true,
            },
            /** @private */
            _showFallbackIcon: {
              type: Boolean,
              value: false,
            },
        };
    }

    render() {
        return html`
            <slot id="slot" @slotchange="${this._toggleFallbackIcon}">
              <div part="icon"></div>
            </slot>
            <div part="icon" ?hidden="${!this._showFallbackIcon}"></div>
        `;
    }

    ready() {
        super.ready();

        this.addEventListener('click', e => this._onClick(e));

        this._toggleFallbackIcon();
    }

    _onClick(e) {
        if (this.sidePanelElement && this.sidePanelElement._closeSidePanel) {
            this.sidePanelElement._closeSidePanel();
        }
    }

    /*
     * @private
     */
    _toggleFallbackIcon() {
      const nodes = this.$.slot.assignedNodes();

      // Show fallback icon if there are 1-2 empty text nodes assigned to the default slot.
      this._showFallbackIcon = nodes.length > 0 && nodes.every((node) => isEmptyTextNode(node));
    }
}

defineCustomElement(JmixSidePanelLayoutCloser);

export { JmixSidePanelLayoutCloser };
