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
import {html, PolymerElement} from '@polymer/polymer';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import { TooltipController } from '@vaadin/component-base/src/tooltip-controller.js';
import { DelegateFocusMixin } from '@vaadin/component-base/src/delegate-focus-mixin.js';
import { FieldMixin } from '@vaadin/field-base/src/field-mixin.js';
import { InputConstraintsMixin } from '@vaadin/field-base/src/input-constraints-mixin.js';
import { SlotStylesMixin } from '@vaadin/field-base/src/slot-styles-mixin.js';
import {inputFieldShared} from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import {css, registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

/*
* CAUTION! Styles for 'jmix-upload-field' component are applied in wrong order.
* The 'inputFieldShared' from 'field-base/src/styles/input-field-shared-styles.js'
* takes precedence over
* 'inputFieldShared' from 'vaadin-lumo-styles/mixins/input-field-shared.js'
* that is registered in 'jmix-upload-field-styles.js'. However, the same approach of
* registering styles works correctly for Vaadin components.
*/
const uploadField = css`
  :host::before {
    display: inline-flex;
  }
  
  /*
   * Use "auto" width instead of default 12em, because upload field
   * with visible file name is not fit in.
   */
  [class$='container'] {
    width: var(--jmix-upload-field-default-width, var(--vaadin-field-default-width, auto));
  }
`;

registerStyles('jmix-upload-field', [inputFieldShared, uploadField], {
    moduleId: 'jmix-upload-field-styles'
});

export class JmixUploadField extends SlotStylesMixin(DelegateFocusMixin(InputConstraintsMixin(FieldMixin(ThemableMixin(
    ElementMixin(PolymerElement)))))) {

    static get is() {
        return 'jmix-upload-field';
    }

    static get template() {
        return html`
            <style>
                vaadin-input-container {
                    background-color: transparent;
                    padding: 0;
                    cursor: auto;
                }

                vaadin-input-container:after {
                    border: 0;
                }
            </style>

            <div class="upload-field-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true" on-click="focus"></span>
                </div>

                <vaadin-input-container
                        part="input-field"
                        readonly="[[readonly]]"
                        disabled="[[disabled]]"
                        invalid="[[invalid]]"
                        theme$="[[_theme]]">
                    <slot name="input"></slot>
                </vaadin-input-container>

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
        this.addController(this._tooltipController);
    }
}

customElements.define(JmixUploadField.is, JmixUploadField);