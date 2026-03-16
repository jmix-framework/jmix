/*
 * Copyright 2023 Haulmont.
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

import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-chip.js';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-container.js';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-item.js';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-overlay.js';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-scroller.js';
import {html, LitElement} from 'lit';
import {ifDefined} from 'lit/directives/if-defined.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {inputFieldShared} from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {
    multiSelectComboBoxStyles
} from '@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-base-styles.js';
import {MultiSelectComboBoxMixin} from '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-mixin.js';
import {jmixMultiSelectComboBoxPickerStyles} from "./styles/jmix-multi-select-combo-box-picker-base-styles";

// CAUTION: copied from @vaadin/multi-select-combo-box [last update Vaadin 25.0.4]
class JmixMultiSelectComboBoxPicker extends MultiSelectComboBoxMixin(
    ThemableMixin(ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
        return 'jmix-multi-select-combo-box-picker';
    }

    static get styles() {
        return [inputFieldShared, multiSelectComboBoxStyles, jmixMultiSelectComboBoxPickerStyles];
    }

    /** @protected */
    render() {
        return html`
            <div class="vaadin-multi-select-combo-box-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
                </div>

                <vaadin-multi-select-combo-box-container
                        part="input-field"
                        .autoExpandVertically="${this.autoExpandVertically}"
                        .readonly="${this.readonly}"
                        .disabled="${this.disabled}"
                        .invalid="${this.invalid}"
                        theme="${ifDefined(this._theme)}"
                >
                    <slot name="overflow" slot="prefix"></slot>
                    <div id="chips" part="chips" slot="prefix">
                        <slot name="chip"></slot>
                    </div>
                    <slot name="input"></slot>
                    <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
                    <!-- Jmix API -->
                    <div id="pickerAction" part="action-part" slot="suffix">
                        <slot name="actions"></slot>
                    </div>
                </vaadin-multi-select-combo-box-container>

                <div part="helper-text">
                    <slot name="helper"></slot>
                </div>

                <div part="error-message">
                    <slot name="error-message"></slot>
                </div>

                <slot name="tooltip"></slot>
            </div>

            <vaadin-multi-select-combo-box-overlay
                    id="overlay"
                    exportparts="overlay, content, loader"
                    .owner="${this}"
                    .dir="${this.dir}"
                    .opened="${this._overlayOpened}"
                    ?loading="${this.loading}"
                    theme="${ifDefined(this._theme)}"
                    .positionTarget="${this._inputField}"
                    no-vertical-overlap
            >
                <slot name="overlay"></slot>
            </vaadin-multi-select-combo-box-overlay>
        `;
    }

    /**
     * Used by `ClearButtonMixin` as a reference to the clear button element.
     * @protected
     * @return {!HTMLElement}
     */
    get clearElement() {
        // return 'null' to disable clean button
        return null;
    }
}

defineCustomElement(JmixMultiSelectComboBoxPicker);

export {JmixMultiSelectComboBoxPicker}