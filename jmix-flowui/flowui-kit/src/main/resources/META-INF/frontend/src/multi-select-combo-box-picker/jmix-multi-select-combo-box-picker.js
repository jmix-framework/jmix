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
import './jmix-multi-select-combo-box-internal.js';
import { html } from '@polymer/polymer';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { MultiSelectComboBox } from '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js';
import { registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-multi-select-combo-box-picker', [],{
    moduleId: 'jmix-multi-select-combo-box-picker-styles'
});

// CAUTION: copied from @vaadin/multi-select-combo-box [last update Vaadin 24.4.4]
class JmixMultiSelectComboBoxPicker extends MultiSelectComboBox {

    static get is() {
        return 'jmix-multi-select-combo-box-picker';
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
            </style>
            
            <div class="vaadin-multi-select-combo-box-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true" on-click="focus"></span>
                </div>

                <vaadin-multi-select-combo-box-internal
                        id="comboBox"
                        items="[[items]]"
                        item-id-path="[[itemIdPath]]"
                        item-label-path="[[itemLabelPath]]"
                        item-value-path="[[itemValuePath]]"
                        disabled="[[disabled]]"
                        readonly="[[readonly]]"
                        auto-open-disabled="[[autoOpenDisabled]]"
                        allow-custom-value="[[allowCustomValue]]"
                        overlay-class="[[overlayClass]]"
                        data-provider="[[dataProvider]]"
                        filter="{{filter}}"
                        last-filter="{{_lastFilter}}"
                        loading="{{loading}}"
                        size="{{size}}"
                        filtered-items="[[filteredItems]]"
                        selected-items="[[selectedItems]]"
                        selected-items-on-top="[[selectedItemsOnTop]]"
                        top-group="[[_topGroup]]"
                        opened="{{opened}}"
                        renderer="[[renderer]]"
                        keep-filter="[[keepFilter]]"
                        theme$="[[_theme]]"
                        on-combo-box-item-selected="_onComboBoxItemSelected"
                        on-change="_onComboBoxChange"
                        on-custom-value-set="_onCustomValueSet"
                        on-filtered-items-changed="_onFilteredItemsChanged"
                >
                    <vaadin-multi-select-combo-box-container
                            part="input-field"
                            auto-expand-vertically="[[autoExpandVertically]]"
                            readonly="[[readonly]]"
                            disabled="[[disabled]]"
                            invalid="[[invalid]]"
                            theme$="[[_theme]]"
                    >
                        <slot name="overflow" slot="prefix"></slot>
                        <div id="chips" part="chips" slot="prefix">
                            <slot name="chip"></slot>
                        </div>
                        <slot name="input"></slot>
                        <div id="toggleButton" class="toggle-button" part="toggle-button" slot="suffix"
                             aria-hidden="true"></div>
                        <!-- Jmix API -->
                        <div id="pickerAction" part="action-part" slot="suffix">
                            <slot name="actions"></slot>
                        </div>
                    </vaadin-multi-select-combo-box-container>
                </jmix-multi-select-combo-box-internal>

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

export { JmixMultiSelectComboBoxPicker }