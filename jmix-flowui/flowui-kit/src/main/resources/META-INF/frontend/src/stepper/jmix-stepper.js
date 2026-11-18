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

import {html, LitElement} from 'lit';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {stepperStyles} from './styles/jmix-stepper-base-styles.js';
import './jmix-step.js';

/**
 * `<jmix-stepper>` shows the headers of its steps and the content of the current step.
 *
 * Slots:
 * - `steps` — the `<jmix-step>` header items.
 * - default — the content of the current step. The server moves the content of the other steps to the
 *   `inactive` slot, which is not rendered.
 *
 * The component never changes the current step. A click, Enter or Space on a completed or visited step dispatches
 * `jmix-stepper-step-click` with `detail.index` — the index of the step among all `<jmix-step>` children — and
 * the server decides. Arrow keys move the focus between the steps that are not unavailable.
 *
 * Parts: `header`, `content`.
 *
 * @fires {CustomEvent} jmix-stepper-step-click - Fired when the user selects a completed or visited step.
 */
class JmixStepper extends ElementMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement)))) {

    /**
     * Fired when the user clicks a completed or visited step, or presses Enter or Space on it.
     * `detail.index` is the index of the step among all `<jmix-step>` children.
     * @event jmix-stepper-step-click
     */

    static get is() {
        return 'jmix-stepper';
    }

    static get styles() {
        return [stepperStyles];
    }

    static get properties() {
        return {
            orientation: {
                type: String,
                value: 'horizontal',
                reflectToAttribute: true
            },
            _collapsed: {
                type: Boolean,
                value: false
            }
        };
    }

    static get observers() {
        return ['__updateSteps(orientation, _collapsed)'];
    }

    render() {
        return html`
            <div part="header" role="list" @click="${this.__onClick}" @keydown="${this.__onKeyDown}">
                <slot name="steps" @slotchange="${this.__onStepsChange}"></slot>
            </div>
            <div part="content">
                <slot></slot>
            </div>
        `;
    }

    firstUpdated() {
        super.firstUpdated();

        this.__resizeObserver = new ResizeObserver(() => this.__updateCollapsed());
        this.__resizeObserver.observe(this.__getHeader());

        this.__themeObserver = new MutationObserver(() => this.__updateSteps());
        this.__themeObserver.observe(this, {attributes: true, attributeFilter: ['theme']});
    }

    disconnectedCallback() {
        super.disconnectedCallback();

        if (this.__resizeObserver) {
            this.__resizeObserver.disconnect();
        }
        if (this.__themeObserver) {
            this.__themeObserver.disconnect();
        }
    }

    connectedCallback() {
        super.connectedCallback();

        if (this.__resizeObserver) {
            this.__resizeObserver.observe(this.__getHeader());
        }
        if (this.__themeObserver) {
            this.__themeObserver.observe(this, {attributes: true, attributeFilter: ['theme']});
        }
    }

    /** @private */
    __getHeader() {
        return this.shadowRoot.querySelector('[part="header"]');
    }

    /** @private */
    __getSteps() {
        return [...this.querySelectorAll(':scope > jmix-step')];
    }

    /** @private */
    __findStep(event) {
        return event.composedPath().find((node) => node.localName === 'jmix-step' && node.parentElement === this);
    }

    /** @private */
    __onStepsChange() {
        this.__updateSteps();
        this.__updateCollapsed();
    }

    /** @private */
    __updateSteps() {
        const theme = this.getAttribute('theme');
        this.__getSteps().forEach((step) => {
            step.orientation = this.orientation;
            step.collapsed = this._collapsed;
            if (theme) {
                step.setAttribute('theme', theme);
            } else {
                step.removeAttribute('theme');
            }
        });
    }

    /** @private */
    __updateCollapsed() {
        const header = this.__getHeader();
        if (!header || this.orientation !== 'horizontal') {
            this._collapsed = false;
            return;
        }

        if (this._collapsed) {
            // Expand again once the header is as wide as the expanded row needed.
            if (header.clientWidth >= this.__expandedWidth) {
                this._collapsed = false;
            }
        } else if (header.scrollWidth > header.clientWidth) {
            this.__expandedWidth = header.scrollWidth;
            this._collapsed = true;
        }
    }

    /** @private */
    __onClick(event) {
        const step = this.__findStep(event);
        if (step) {
            this.__requestStep(step);
        }
    }

    /** @private */
    __onKeyDown(event) {
        const step = this.__findStep(event);
        if (!step || event.altKey || event.shiftKey || event.ctrlKey || event.metaKey) {
            return;
        }

        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            this.__requestStep(step);
            return;
        }

        const delta = this.__getFocusDelta(event.key);
        if (delta === 0) {
            return;
        }

        event.preventDefault();
        const focusable = this.__getSteps().filter((s) => !s.hidden && s.state !== 'unavailable');
        const index = focusable.indexOf(step);
        if (index >= 0) {
            focusable[(index + delta + focusable.length) % focusable.length].focus();
        }
    }

    /** @private */
    __getFocusDelta(key) {
        if (this.orientation === 'vertical') {
            return key === 'ArrowDown' ? 1 : key === 'ArrowUp' ? -1 : 0;
        }
        const rtl = this.getAttribute('dir') === 'rtl';
        if (key === (rtl ? 'ArrowLeft' : 'ArrowRight')) {
            return 1;
        }
        return key === (rtl ? 'ArrowRight' : 'ArrowLeft') ? -1 : 0;
    }

    /** @private */
    __requestStep(step) {
        if (step.hidden || (step.state !== 'completed' && step.state !== 'visited')) {
            return;
        }
        this.dispatchEvent(new CustomEvent('jmix-stepper-step-click', {
            detail: {index: this.__getSteps().indexOf(step)}
        }));
    }
}

defineCustomElement(JmixStepper);
