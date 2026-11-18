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

import {html, LitElement, nothing} from 'lit';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {DirMixin} from '@vaadin/component-base/src/dir-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {stepStyles} from './styles/jmix-step-base-styles.js';

/**
 * `<jmix-step>` is the header item of a step in `<jmix-stepper>`.
 *
 * The server sets `label`, `description`, `number`, `state` and `last`; `<jmix-stepper>` sets `orientation`,
 * `collapsed` and the `theme` attribute. The step never changes its state by itself.
 *
 * Slots:
 * - `icon` — an icon shown in the indicator instead of the number and the check mark.
 *
 * Parts: `button`, `indicator`, `number`, `checkmark`, `text`, `label`, `description`, `connector`.
 *
 * State attributes: `state` (`current`, `completed`, `visited`, `unavailable`), `last`, `orientation`,
 * `collapsed`.
 */
class JmixStep extends ThemableMixin(DirMixin(PolylitMixin(LumoInjectionMixin(LitElement)))) {

    static get is() {
        return 'jmix-step';
    }

    static get styles() {
        return [stepStyles];
    }

    static get properties() {
        return {
            label: {
                type: String
            },
            description: {
                type: String
            },
            number: {
                type: Number
            },
            state: {
                type: String,
                value: 'unavailable',
                reflectToAttribute: true
            },
            last: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            orientation: {
                type: String,
                value: 'horizontal',
                reflectToAttribute: true
            },
            collapsed: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            _hasIcon: {
                type: Boolean,
                value: false
            }
        };
    }

    firstUpdated() {
        super.firstUpdated();

        if (!this.hasAttribute('role')) {
            this.setAttribute('role', 'listitem');
        }
    }

    render() {
        const current = this.state === 'current';
        return html`
            <div part="button" role="button"
                 tabindex="${current ? 0 : -1}"
                 aria-current="${current ? 'step' : 'false'}"
                 aria-disabled="${this.state === 'unavailable' ? 'true' : 'false'}">
                <span part="indicator" aria-hidden="true">
                    <slot name="icon" @slotchange="${this.__onIconSlotChange}"></slot>
                    ${this.__renderIndicatorContent()}
                </span>
                <span part="text">
                    <span part="label">${this.label}</span>
                    ${this.description ? html`<span part="description">${this.description}</span>` : nothing}
                </span>
            </div>
            <span part="connector" aria-hidden="true"></span>
        `;
    }

    /**
     * Moves the focus to the step's button.
     */
    focus(options) {
        const button = this.shadowRoot && this.shadowRoot.querySelector('[part="button"]');
        if (button) {
            button.focus(options);
        }
    }

    /** @private */
    __renderIndicatorContent() {
        if (this._hasIcon) {
            return nothing;
        }
        if (this.state === 'completed') {
            return html`
                <svg part="checkmark" viewBox="0 0 16 16" aria-hidden="true">
                    <path d="M6.2 11.2 2.9 7.9 1.5 9.3l4.7 4.7 8.3-8.3-1.4-1.4z"></path>
                </svg>`;
        }
        return html`<span part="number">${this.number}</span>`;
    }

    /** @private */
    __onIconSlotChange(event) {
        this._hasIcon = event.target.assignedNodes({flatten: true}).length > 0;
    }
}

defineCustomElement(JmixStep);
