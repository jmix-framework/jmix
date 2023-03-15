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
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-internal.js';
import { html } from '@polymer/polymer';
import { MultiSelectComboBox } from '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js';
import { registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-multi-select-combo-box-picker', [],{
    moduleId: 'jmix-multi-select-combo-box-picker-styles'
});

export class JmixMultiSelectComboBoxPicker extends MultiSelectComboBox {

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
                        items="[[__effectiveItems]]"
                        item-id-path="[[itemIdPath]]"
                        item-label-path="[[itemLabelPath]]"
                        item-value-path="[[itemValuePath]]"
                        disabled="[[disabled]]"
                        readonly="[[readonly]]"
                        auto-open-disabled="[[autoOpenDisabled]]"
                        allow-custom-value="[[allowCustomValue]]"
                        data-provider="[[dataProvider]]"
                        filter="{{filter}}"
                        last-filter="{{_lastFilter}}"
                        loading="{{loading}}"
                        size="{{size}}"
                        filtered-items="[[__effectiveFilteredItems]]"
                        selected-items="[[selectedItems]]"
                        opened="{{opened}}"
                        renderer="[[renderer]]"
                        theme$="[[_theme]]"
                        on-combo-box-item-selected="_onComboBoxItemSelected"
                        on-change="_onComboBoxChange"
                        on-custom-value-set="_onCustomValueSet"
                        on-filtered-items-changed="_onFilteredItemsChanged"
                >
                    <vaadin-multi-select-combo-box-container
                            part="input-field"
                            readonly="[[readonly]]"
                            disabled="[[disabled]]"
                            field-readonly="[[fieldReadonly]]"
                            invalid="[[invalid]]"
                            theme$="[[_theme]]"
                    >
                        <vaadin-multi-select-combo-box-chip
                                id="overflow"
                                slot="prefix"
                                part$="[[_getOverflowPart(_overflowItems.length)]]"
                                disabled="[[disabled]]"
                                readonly="[[readonly]]"
                                label="[[_getOverflowLabel(_overflowItems.length)]]"
                                title$="[[_getOverflowTitle(_overflowItems)]]"
                                hidden$="[[_isOverflowHidden(_overflowItems.length)]]"
                                on-mousedown="_preventBlur"
                        ></vaadin-multi-select-combo-box-chip>
                        <div id="chips" part="chips" slot="prefix"></div>
                        <slot name="input"></slot>
                        <div id="toggleButton" class="toggle-button" part="toggle-button" slot="suffix"
                             aria-hidden="true"></div>
                        <div id="pickerAction" part="action-part" slot="suffix">
                            <slot name="actions"></slot>
                        </div>
                    </vaadin-multi-select-combo-box-container>
                </vaadin-multi-select-combo-box-internal>

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

customElements.define(JmixMultiSelectComboBoxPicker.is, JmixMultiSelectComboBoxPicker);