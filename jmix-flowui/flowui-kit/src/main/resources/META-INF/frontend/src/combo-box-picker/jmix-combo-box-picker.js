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
import { html } from '@polymer/polymer';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ComboBox } from '@vaadin/combo-box/src/vaadin-combo-box.js';
import { registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-combo-box-picker', [],{
    moduleId: 'jmix-combo-box-picker-styles'
});

// CAUTION: copied from @vaadin/combo-box [last update Vaadin 24.4.4]
class JmixComboBoxPicker extends ComboBox {

    static get is() {
        return 'jmix-combo-box-picker';
    }

    static get template() {
        return html`
            <style>
                [part="action-part"] ::slotted(*) {
                    display: flex;
                }

                :host([readonly]) [part="action-part"] {
                    display: none;
                }

                :host([opened]) {
                    pointer-events: auto;
                }
            </style>

            <div class="value-picker-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true" on-click="focus"></span>
                </div>

                <vaadin-input-container
                        part="input-field"
                        readonly="[[readonly]]"
                        disabled="[[disabled]]"
                        invalid="[[invalid]]"
                        theme$="[[_theme]]"
                >
                    <slot name="prefix" slot="prefix"></slot>
                    <slot name="input"></slot>
                    <div id="toggleButton" part="toggle-button" slot="suffix" aria-hidden="true"></div>
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
            </div>

            <vaadin-combo-box-overlay
                    id="overlay"
                    opened="[[_overlayOpened]]"
                    loading$="[[loading]]"
                    theme$="[[_theme]]"
                    position-target="[[_positionTarget]]"
                    no-vertical-overlap
                    restore-focus-node="[[inputElement]]"
            ></vaadin-combo-box-overlay>

            <slot name="tooltip"></slot>
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

export { JmixComboBoxPicker }