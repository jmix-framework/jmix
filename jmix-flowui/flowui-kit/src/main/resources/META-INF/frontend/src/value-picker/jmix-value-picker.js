/*
 * Copyright 2022 Haulmont.
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

import '@vaadin/input-container/src/vaadin-input-container.js';
import {html, LitElement} from 'lit';
import {ifDefined} from 'lit/directives/if-defined.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {TooltipController} from '@vaadin/component-base/src/tooltip-controller.js';
import {LabelledInputController} from '@vaadin/field-base/src/labelled-input-controller.js';
import {InputController} from '@vaadin/field-base/src/input-controller.js';
import {InputFieldMixin} from '@vaadin/field-base/src/input-field-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export class JmixValuePicker extends InputFieldMixin(ThemableMixin(ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
        return 'jmix-value-picker';
    }

    /*static get styles() {
        return [inputFieldShared, jmixValuePickerStyles];
    }*/

    render() {
        return html`
            <div class="value-picker-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
                </div>

                <vaadin-input-container
                        part="input-field"
                        .readonly="${this.readonly}"
                        .disabled="${this.disabled}"
                        .invalid="${this.invalid}"
                        theme="${ifDefined(this._theme)}"
                >
                    <slot name="prefix" slot="prefix"></slot>
                    <slot name="input"></slot>
                    <slot name="suffix" slot="suffix"></slot>
                    <div id="pickerAction" part="action-part" slot="suffix">
                        <slot name="actions"></slot>
                    </div>
                </vaadin-input-container>

                <div part="helper-text">
                    <slot name="helper"></slot>
                </div>

                <div part="error-message">
                    <slot name="error-message"></slot>
                </div>

                <slot name="tooltip"></slot>
            </div>
        `;
    }

    static get properties() {
        return {
            allowCustomValue: {
                type: Boolean,
                value: false,
            }
        };
    }

    constructor() {
        super();
        this._setType('text');
    }

    /** @protected */
    get clearElement() {
        return null;
    }

    /** @protected */
    ready() {
        super.ready();

        this.addController(
            new InputController(this, (input) => {
                this._setInputElement(input);
                this._setFocusElement(input);
                this.stateTarget = input;
                this.ariaTarget = input;
            }),
        );
        this.addController(new LabelledInputController(this.inputElement, this._labelController));

        this._tooltipController = new TooltipController(this);
        this._tooltipController.setPosition('top');
        this._tooltipController.setAriaTarget(this.inputElement);
        this.addController(this._tooltipController);
    }


    _onInput(event) {
        if (!this.allowCustomValue) {
            this.inputElement.value = this.value || '';
        }

        super._onInput(event);
    }

    checkValidity() {
        return !this.invalid && super.checkValidity();
    }
}

defineCustomElement(JmixValuePicker);