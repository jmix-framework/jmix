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
import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { TooltipController } from '@vaadin/component-base/src/tooltip-controller.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { SwitchMixin } from './jmix-switch-mixin.js';
import { switchStyles } from './jmix-switch-styles.js';

registerStyles('jmix-switch', switchStyles, { moduleId: 'jmix-switch-styles' });

export class JmixSwitch extends SwitchMixin(ElementMixin(ThemableMixin(PolymerElement))) {

    static get is() {
        return 'jmix-switch';
    }

    static get template() {
        return html`
            <div class="jmix-switch-container">
                <div part="switch" aria-hidden="true">
                    <span class="indicator"></span>
                </div>
                <slot name="input"></slot>
                <div part="label">
                    <slot name="label"></slot>
                    <div part="required-indicator" on-click="_onRequiredIndicatorClick"></div>
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
