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
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ifDefined} from 'lit/directives/if-defined.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {TooltipController} from '@vaadin/component-base/src/tooltip-controller.js';
import {DelegateFocusMixin} from '@vaadin/a11y-base/src/delegate-focus-mixin.js';
import {FieldMixin} from '@vaadin/field-base/src/field-mixin.js';
import {InputConstraintsMixin} from '@vaadin/field-base/src/input-constraints-mixin.js';
import {SlotStylesMixin} from '@vaadin/component-base/src/slot-styles-mixin.js';
import {inputFieldShared} from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {jmixUploadFieldStyles} from "./styles/jmix-upload-field-base-styles";

export class JmixUploadField extends SlotStylesMixin(DelegateFocusMixin(InputConstraintsMixin(FieldMixin(ThemableMixin(
    ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement)))))))) {

    static get is() {
        return 'jmix-upload-field';
    }

    static get styles() {
        return [inputFieldShared, jmixUploadFieldStyles];
    }

    render() {
        return html`
            <div class="upload-field-container">
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
                    <slot name="input"></slot>
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
        return {};
    }

    /** @protected */
    get slotStyles() {
        return [];
    }

    /** @protected */
    ready() {
        super.ready();

        this._tooltipController = new TooltipController(this);
        this._tooltipController.setPosition('top');
        this._tooltipController.setAriaTarget(this.inputElement);
        this.addController(this._tooltipController);
    }
}

defineCustomElement(JmixUploadField);