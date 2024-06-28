/*
 * Copyright 2024 Haulmont.
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

import './pivot/jquery.js';
import './jquery-ui.min.js';
import './pivot/pivot.js';

import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {Debouncer} from '@vaadin/component-base/src/debounce.js';
import {LabelMixin} from "@vaadin/field-base/src/label-mixin.js";
import {FocusMixin} from '@vaadin/a11y-base/src/focus-mixin.js';
import {HelperController} from "@vaadin/field-base/src/helper-controller.js";
import {helper} from '@vaadin/vaadin-lumo-styles/mixins/helper.js';
import {registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {jmixPivotTableStyles} from './jmix-pivot-table-styles.js';

registerStyles('jmix-pivot-table', [jmixPivotTableStyles], {moduleId: 'jmix-pivot-table-styles'});

export class PivotTable extends ElementMixin(FocusMixin(LabelMixin(ThemableMixin(PolymerElement)))) {
    static get is() {
        return 'jmix-pivot-table';
    }

    static get template() {
        return html`
            <div class="jmix-pivot-table-wrapper">
                <div part="label">
                    <slot name="label"></slot>
                </div>

                <div part="output">
                    <slot name="output"></slot>
                </div>

                <div part="helper-text">
                    <slot name="helper"></slot>
                </div>
            </div>
        `;
    }

    static get properties() {
        return {
        };
    }

    constructor() {
        super();
    }

    /** @protected */
    ready() {
        super.ready();

        const editor = this.shadowRoot.querySelector('[part="output"]');

        $(function(){
                 /*     $('#outputDiv').pivot([], {});*/

                                $("#div-id").pivotUI(
                                    [
                                        {color: "blue", shape: "circle"},

                                        {color: "red", shape: "triangle"}
                                    ],
                                    {
                                        rows: ["color"],
                                        cols: ["shape"]
                                    }
                                );
                             });
    }
}

defineCustomElement(PivotTable);