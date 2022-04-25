import '@vaadin/input-container/src/vaadin-input-container.js';
import { html, PolymerElement } from '@polymer/polymer';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { InputController } from '@vaadin/field-base/src/input-controller.js';
import { InputFieldMixin } from '@vaadin/field-base/src/input-field-mixin.js';
import { LabelledInputController } from '@vaadin/field-base/src/labelled-input-controller.js';
import { inputFieldShared } from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-value-picker', inputFieldShared, {
    moduleId: 'jmix-value-picker-styles'
});

export class JmixValuePicker extends InputFieldMixin(ThemableMixin(ElementMixin(PolymerElement))) {

    static get is() {
        return 'jmix-value-picker';
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
                        theme$="[[theme]]"
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
            })
        );
        this.addController(new LabelledInputController(this.inputElement, this._labelController));
    }


    _onInput(event) {
        if (!this.allowCustomValue) {
            this.inputElement.value = this.value || '';
        }

        super._onInput(event);
    }
}

customElements.define(JmixValuePicker.is, JmixValuePicker);