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
import {html} from 'lit';
import {ifDefined} from 'lit/directives/if-defined.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ComboBox} from '@vaadin/combo-box/src/vaadin-combo-box.js';
import {comboBoxStyles} from '@vaadin/combo-box/src/styles/vaadin-combo-box-base-styles.js';
import {inputFieldShared} from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import {comboBoxPickerStyles} from "./styles/jmix-combo-box-picker-base-styles";

// CAUTION: copied from @vaadin/combo-box [last update Vaadin 25.0.4]
class JmixComboBoxPicker extends ComboBox {

    static get is() {
        return 'jmix-combo-box-picker';
    }

    static get styles() {
        return [inputFieldShared, comboBoxStyles, comboBoxPickerStyles];
    }

    /** @protected */
    render() {
        return html`
            <div class="vaadin-combo-box-container">
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
                    <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
                    <!-- Jmix API -->
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

            <vaadin-combo-box-overlay
                    id="overlay"
                    exportparts="overlay, content, loader"
                    .owner="${this}"
                    .dir="${this.dir}"
                    .opened="${this._overlayOpened}"
                    ?loading="${this.loading}"
                    theme="${ifDefined(this._theme)}"
                    .positionTarget="${this._positionTarget}"
                    no-vertical-overlap
            >
                <slot name="overlay"></slot>
            </vaadin-combo-box-overlay>
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

defineCustomElement(JmixComboBoxPicker);

export {JmixComboBoxPicker}