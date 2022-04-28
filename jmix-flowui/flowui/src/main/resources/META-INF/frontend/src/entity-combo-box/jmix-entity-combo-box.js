import '@vaadin/input-container/src/vaadin-input-container.js';
import { html } from '@polymer/polymer';
import { ComboBox } from '@vaadin/combo-box/src/vaadin-combo-box.js';
import { registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-entity-combo-box', [],{
    moduleId: 'jmix-entity-combo-box-styles'
});

export class JmixEntityComboBox extends ComboBox {

    static get is() {
        return 'jmix-entity-combo-box';
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
                        field-readonly="[[fieldReadonly]]"
                        disabled="[[disabled]]"
                        invalid="[[invalid]]"
                        theme$="[[theme]]"
                >
                    <slot name="prefix" slot="prefix"></slot>
                    <slot name="input"></slot>
                    <div id="toggleButton" part="toggle-button" slot="suffix" aria-hidden="true"></div>
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

            <vaadin-combo-box-dropdown
                    id="dropdown"
                    opened="[[opened]]"
                    renderer="[[renderer]]"
                    position-target="[[_positionTarget]]"
                    restore-focus-on-close="[[__restoreFocusOnClose]]"
                    restore-focus-node="[[inputElement]]"
                    _focused-index="[[_focusedIndex]]"
                    _item-id-path="[[itemIdPath]]"
                    _item-label-path="[[itemLabelPath]]"
                    loading="[[loading]]"
                    theme="[[_theme]]"
            ></vaadin-combo-box-dropdown>
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

    /** @protected */
    ready() {
        super.ready();

        this._actionsBox = this.shadowRoot.querySelector('[part="action-part"]');
    }

    /**
     * @param {Event} event
     * @protected
     * @override
     */
    _onHostClick(event) {
        const path = event.composedPath();

        // Do not open dropdown when clicking on the picker actions
        if (!path.includes(this._actionsBox)) {
            super._onHostClick(event);
        }
    }
}

customElements.define(JmixEntityComboBox.is, JmixEntityComboBox);