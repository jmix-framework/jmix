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

import {html, LitElement} from 'lit';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {ButtonMixin} from '@vaadin/button/src/vaadin-button-mixin.js';

class JmixValuePickerButton extends ButtonMixin(ElementMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
        return 'jmix-value-picker-button';
    }

    /*static get styles() {
        return jmixValuePickerButtonStyles;
    }*/

    render() {
        return html`
            <div class="value-picker-button-container">
                <span part="icon">
                    <slot name="icon"></slot>
                </span>
            </div>
        `;
    }
}

defineCustomElement(JmixValuePickerButton);

export {JmixValuePickerButton}