/*
 * Copyright 2025 Haulmont.
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
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';
import { TooltipController } from '@vaadin/component-base/src/tooltip-controller.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { SwitchMixin } from './jmix-switch-mixin.js';
import { switchStyles } from './styles/jmix-switch-base-styles.js';

export class JmixSwitch extends SwitchMixin(ElementMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {
    static get is() {
        return 'jmix-switch';
    }

    static get styles() {
        return switchStyles;
    }

    render() {
        return html`
            <div class="jmix-switch-container">
                <div part="switch" aria-hidden="true">
                    <span class="indicator"></span>
                </div>
                <slot name="input"></slot>
                <div part="label">
                    <slot name="label"></slot>
                    <div part="required-indicator" @click="${this._onRequiredIndicatorClick}"></div>
                </div>
                <div part="helper-text">
                    <slot name="helper"></slot>
                </div>
                <div part="error-message">
                    <slot name="error-message"></slot>
                </div>
            </div>
            <slot name="tooltip"></slot>
        `;
    }

    /** @protected */
    ready() {
        super.ready();

        this._tooltipController = new TooltipController(this);
        this._tooltipController.setAriaTarget(this.inputElement);
        this.addController(this._tooltipController);
    }
}

defineCustomElement(JmixSwitch);
