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

import './jquery/jquery.js';
import './jquery/jquery-ui.min.js';
import './jquery/pivot.min.js';
import './pivot/pivot.min.js';
import './pivot/tips_data.min.js';
import './pivot/pivot.min.css';
import './plugin/c3/c3.min.js';
import './plugin/c3/c3_renderers.min.js';
import './plugin/c3/c3/c3.min.css';
import './plugin/d3/d3.min.js';
import './plugin/d3/d3_renderers.min.js';
import './plugin/export_renderers.min.js';

@WebJarResource({
        "jquery:jquery.min.js",
        "jquery-ui:jquery-ui.min.js",
        "jquery-ui-touch-punch:jquery.ui.touch-punch.min.js",
        "pivottable:pivot.min.js",
        "pivottable:tips_data.min.js",
        "pivottable:plugins/d3/d3.min.js",
        "pivottable:plugins/c3/c3.min.js",
        "pivottable:c3_renderers.min.js",
        "pivottable:d3_renderers.min.js",
        "pivottable:export_renderers.min.js",
        "pivottable:pivot.min.css",
        "pivottable:plugins/c3/c3.min.css"
})


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


        $(function(){

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

        let rowOrder = $("a.pvtRowOrder");
        $(rowOrder).bind("click", function() {
            $(rowOrder).val("").html("");
            $(rowOrder).css("content", '\e754');
            $(rowOrder).css("font-family", 'Vaadin-Icons');
            $(rowOrder).css("color", 'var(--lumo-primary-text-color)');
        });

        let colOrder = $("a.pvtColOrder");
                $(colOrder).bind("click", function() {
                    $(colOrder).val("").html("");
                    $(colOrder).css("content", '\e752');
            $(colOrder).css("font-family", 'Vaadin-Icons');
            $(colOrder).css("color", 'var(--lumo-primary-text-color)');
                });

    }

    showData(pivotDataAsString) {
        let pivotDataJson = JSON.parse(pivotDataAsString);

        (function(pivotData) {
            $(function(){
                $("#div-id").pivotUI(
                    pivotData.values,
                    {
                        rows: pivotData.rows,
                        cols: pivotData.cols
                    }
                );
            });
        })(pivotDataJson);
    }
}

defineCustomElement(PivotTable);