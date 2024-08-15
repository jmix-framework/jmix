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
import { html, PolymerElement } from '@polymer/polymer';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { InputFieldMixin } from '@vaadin/field-base/src/input-field-mixin.js';
import { css, registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import './jmix-twin-column-styles.js';

export class JmixTwinColumn extends InputFieldMixin(ThemableMixin(ElementMixin(PolymerElement))) {
    static get is() {
        return 'jmix-twin-column';
    }

    static get template() {
        return html`
        <vaadin-vertical-layout class="jmix-twin-column-container">
            <div part="label" on-click="focus">
              <slot name="label"></slot>
              <span part="required-indicator" aria-hidden="true"></span>
            </div>

            <div class="jmix-twin-column-input-container">
                <slot name="items-label"></slot>
                <slot name="selected-items-label"></slot>
                <slot name="items"></slot>
                <slot name="actions"></slot>
                <slot name="selected-items"></slot>
            </div>

            <div part="helper-text">
              <slot name="helper"></slot>
            </div>

            <div part="error-message">
              <slot name="error-message"></slot>
            </div>
        </vaadin-vertical-layout>

        <slot name="tooltip"></slot>
        `;
      }

    /** @protected */
    ready() {
        super.ready();

        let components = [
            this.getElementsByClassName("jmix-twin-column-actions-panel")[0],
            this.getElementsByClassName("jmix-twin-column-items-column")[0],
            this.getElementsByClassName("jmix-twin-column-selected-items-column")[0]
        ];

        (function(twinColumn) {
            for (let component of components) {
                component.addEventListener("focusin", () => {
                  twinColumn._setFocused(true);
                });
                component.addEventListener("focusout", () => {
                  twinColumn._setFocused(false);
                });
            }
        })(this);
    }
}

defineCustomElement(JmixTwinColumn);