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
import './jquery/jquery.ui.touch-punch.min.js';
import './plugin/d3/d3.js';
import './plugin/c3/c3.js';
import './plugin/c3/c3.min.css';
import './pivot/pivot.js';
import './pivot/tips_data.min.js';
import './pivot/c3_renderers.min.js';
import './pivot/d3_renderers.min.js';
import './pivot/export_renderers.min.js';
import './jmix-pivot-table-parser.js';
import './jmix-pivot-table.css';

import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

import {registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {jmixPivotTableStyles} from './jmix-pivot-table-styles.js';

registerStyles('jmix-pivot-table', [jmixPivotTableStyles], {moduleId: 'jmix-pivot-table-styles'});

export class JmixPivotTable extends ElementMixin(ThemableMixin(PolymerElement)) {
    static get is() {
        return 'jmix-pivot-table';
    }

    static get template() {
        return html`
            <div class="jmix-pivot-table-wrapper">
                <div part="output" style="height: inherit; width: inherit;">
                    <slot name="output"></slot>
                </div>
            </div>

            <style>
                :host {
                    width: 100%;
                    height: 100%;
                }
            </style>
        `;
    }

    static get properties() {
        return {
            theme: {
                type: String,
                observer: '_onThemeChange',
                notify: true
            },
            /** @private */
            _options: {
                type: Object
            },

            /** @private */
            _dataSet: {
                type: Object
            }
        };
    }

    ready() {
        super.ready();

        this.initApplicationThemeObserver();
    }

    initApplicationThemeObserver() {
        this._applyTheme();

        this._applicationThemeObserver = new MutationObserver(mutations => {
            if (mutations.filter(mutation =>
                mutation.type === "attributes" && mutation.attributeName === "theme").length !== 0) {
                this._applyTheme()
            }
        });

        this._applicationThemeObserver.observe(document.documentElement, {
            attributes: true
        });
    }

    _applyTheme() {
        const currentTheme = document.documentElement.getAttribute("theme");

        if (currentTheme === "dark") {
            this.theme = "dark";
        } else if (currentTheme === "" || currentTheme === null) {
            this.theme = null;
        }
    }

    _onThemeChange() {
        this._recreatePivot();
    }

    _refreshHandler(pivotState) {
        this._updateOrderDirection("a.pvtRowOrder", pivotState.rowOrder);
        this._updateOrderDirection("a.pvtColOrder", pivotState.colOrder);

        const customEvent = new CustomEvent('jmix-pivottable:refresh', {
            detail: {
                rows: pivotState.rows,
                cols: pivotState.cols,
                renderer: pivotState.rendererName,
                aggregation: pivotState.aggregatorName,
                aggregationProperties: pivotState.vals,
                inlusions: pivotState.inclusions,
                exclusions: pivotState.exclusions,
                rowOrder: pivotState.rowOrder,
                colOrder: pivotState.colOrder
            }
        });
        this.dispatchEvent(customEvent);
    }

    _cellClickHandler(value, filters, pivotData) {
        var dataItemKeys = [];
        (function(pivotTable) {
            pivotData.forEachMatchingRecord(filters, function(record) {
                let itemIndex = pivotTable._dataSet.indexOf(record);
                if (itemIndex >= 0) {
                    dataItemKeys.push(pivotTable.itemIds[itemIndex]);
                }
            });
        })(this);

        const customEvent = new CustomEvent('jmix-pivottable:cellclick', {
            detail: {
                value: value,
                filters: filters,
                dataItemKeys: dataItemKeys
            }
        });

        this.dispatchEvent(customEvent);
    }

    _updateOrderDirection(orderElementClassName, order) {
        let nextToCurrent = {
            "key-a-to-z": "value-z-to-a",
            "value-a-to-z": "key-a-to-z",
            "value-z-to-a": "value-a-to-z"
        };
        $(orderElementClassName).val("").html("");

        let currentOrder = order.replace(/_/g, '-');
        $(orderElementClassName).removeClass(nextToCurrent[currentOrder]);
        $(orderElementClassName).addClass(currentOrder);
    }

    _onRendererChange() {
        this._recreatePivot();
    }

    _recreatePivot() {
        if (this._options) {
            (function(pivotTable){
                $.pivotUtilities.renderers = $.extend($.pivotUtilities.c3_renderers,
                    $.extend($.pivotUtilities.d3_renderers, $.pivotUtilities.renderers));
                pivotTable._initLocale();
                let options = pivotTable._options;
                $("#div-id").pivotUI(
                    pivotTable._dataSet,
                    {
                        onRefresh: (function(pivotTableCls){
                            return function(pivotState) {
                                pivotTableCls._refreshHandler(pivotState);
                            };
                        })(pivotTable),
                        showUI: options.showUI,
                        rows: options.rows, //already localized from server
                        cols: options.cols,
                        colOrder: options.colOrder,
                        rowOrder: options.rowOrder,
                        vals: options.vals,
                        exclusions: options.exclusions,
                        inclusions: options.inclusions,
                        aggregatorName: pivotTable._getLocalizedAggregatorName(),
                        rendererName: pivotTable._getLocalizedRendererName(),
                        renderers: pivotTable._getLocalizedRenderers(),
                        derivedAttributes: options.derivedProperties ? options.derivedProperties.properties : null,
                        localeStrings: options.localizedStrings,
                        rendererOptions: options.rendererOptions,
                        sorters: options.sorters,
                        rendererOptions: {
                            table: {
                                clickCallback: (function(pivotTableCls){
                                    return function(event, value, filters, pivotData) {
                                        pivotTableCls._cellClickHandler(value, filters, pivotData);
                                    };
                                })(pivotTable),
                                rowTotals: options.rowTotals ? options.rowTotals : true,
                                colTotals: options.colTotals ? options.colTotals : true
                            },
                            heatmap : {
                                colorScaleGenerator : pivotTable._getColorScaleGenerator(options.rendererOptions)
                            }
                        },
                        c3: {
                            size: (function(renderOptions) {
                                return renderOptions && renderOptions.c3
                                    ? renderOptions.c3.size
                                    : null;
                                })(options.renderOptions)
                        }
                    },
                    false,
                    options.localeCode
                );
            })(this);
        }
    }

    _getColorScaleGenerator(rendererOptions) {
        if (rendererOptions && rendererOptions.heatmap && rendererOptions.heatmap.colorScaleGenerator) {
            return rendererOptions.heatmap.colorScaleGenerator;
        }
        return this.theme == 'dark' ? this._heatmapColorScaleGeneratorForDarkTheme : null;
    }

    _heatmapColorScaleGeneratorForDarkTheme(values) {
        var max, min;
        min = Math.min.apply(Math, values);
        max = Math.max.apply(Math, values);
        return function(x) {
            let fromR = 255, fromG = 192, fromB = 192;
            let toR = 255, toG = 0, toB = 0;
            let interpolationRatio = (x - min) / (max - min);
            let r = fromR - Math.round((fromR - toR) * interpolationRatio);
            let g = fromG - Math.round((fromG - toG) * interpolationRatio);
            let b = fromB - Math.round((fromB - toB) * interpolationRatio);
            return "rgb(" + r + ", " + g + ", " + b +")";
        };
    }

    _initLocale() {
        var formatFloat, formatInt, formatPercent, numberFormat, aggregatorTemplates;
        numberFormat = $.pivotUtilities.numberFormat;
        aggregatorTemplates = $.pivotUtilities.aggregatorTemplates;
        let localizedStrings = this._options.localizedStrings;
        formatFloat = numberFormat({
            digitsAfterDecimal: localizedStrings.floatFormat.digitsAfterDecimal,
            scaler: localizedStrings.floatFormat.scaler,
            thousandsSep: localizedStrings.floatFormat.thousandsSep,
            decimalSep: localizedStrings.floatFormat.decimalSep,
            prefix: localizedStrings.floatFormat.prefix,
            suffix: localizedStrings.floatFormat.suffix,
            showZero: localizedStrings.floatFormat.showZero
        });
        formatInt = numberFormat({
            digitsAfterDecimal: localizedStrings.integerFormat.digitsAfterDecimal,
            scaler: localizedStrings.integerFormat.scaler,
            thousandsSep: localizedStrings.integerFormat.thousandsSep,
            decimalSep: localizedStrings.integerFormat.decimalSep,
            prefix: localizedStrings.integerFormat.prefix,
            suffix: localizedStrings.integerFormat.suffix,
            showZero: localizedStrings.integerFormat.showZero
        });
        formatPercent = numberFormat({
            digitsAfterDecimal: localizedStrings.percentFormat.digitsAfterDecimal,
            scaler: localizedStrings.percentFormat.scaler,
            thousandsSep: localizedStrings.percentFormat.thousandsSep,
            decimalSep: localizedStrings.percentFormat.decimalSep,
            prefix: localizedStrings.percentFormat.prefix,
            suffix: localizedStrings.percentFormat.suffix,
            showZero: localizedStrings.percentFormat.showZero
        });

        var allAggregators = {};
        allAggregators[localizedStrings.aggregation.count] = aggregatorTemplates.count(formatInt);
        allAggregators[localizedStrings.aggregation.countUniqueValues] = aggregatorTemplates.countUnique(formatInt);
        allAggregators[localizedStrings.aggregation.listUniqueValues] = aggregatorTemplates.listUnique(", ");
        allAggregators[localizedStrings.aggregation.sum] = aggregatorTemplates.sum(formatFloat);
        allAggregators[localizedStrings.aggregation.integerSum] = aggregatorTemplates.sum(formatInt);
        allAggregators[localizedStrings.aggregation.average] = aggregatorTemplates.average(formatFloat);
        allAggregators[localizedStrings.aggregation.minimum] = aggregatorTemplates.min(formatFloat);
        allAggregators[localizedStrings.aggregation.maximum] = aggregatorTemplates.max(formatFloat);
        allAggregators[localizedStrings.aggregation.sumOverSum] = aggregatorTemplates.sumOverSum(formatFloat);
        allAggregators[localizedStrings.aggregation.upperBound80] = aggregatorTemplates.sumOverSumBound80(true, formatFloat);
        allAggregators[localizedStrings.aggregation.lowerBound80] = aggregatorTemplates.sumOverSumBound80(false, formatFloat);
        allAggregators[localizedStrings.aggregation.sumAsFractionOfTotal] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.sum(), "total", formatPercent);
        allAggregators[localizedStrings.aggregation.sumAsFractionOfRows] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.sum(), "row", formatPercent);
        allAggregators[localizedStrings.aggregation.sumAsFractionOfColumns] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.sum(), "col", formatPercent);
        allAggregators[localizedStrings.aggregation.countAsFractionOfTotal] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.count(), "total", formatPercent);
        allAggregators[localizedStrings.aggregation.countAsFractionOfRows] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.count(), "row", formatPercent);
        allAggregators[localizedStrings.aggregation.countAsFractionOfColumns] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.count(), "col", formatPercent);

        var allRenderers = {};
        allRenderers[localizedStrings.renderer.table] = $.pivotUtilities.renderers["Table"];
        allRenderers[localizedStrings.renderer.tableBarchart] = $.pivotUtilities.renderers["Table Barchart"];
        allRenderers[localizedStrings.renderer.heatmap] = $.pivotUtilities.renderers["Heatmap"];
        allRenderers[localizedStrings.renderer.rowHeatmap] = $.pivotUtilities.renderers["Row Heatmap"];
        allRenderers[localizedStrings.renderer.colHeatmap] = $.pivotUtilities.renderers["Col Heatmap"];
        allRenderers[localizedStrings.renderer.lineChart] = $.pivotUtilities.c3_renderers["Line Chart"];
        allRenderers[localizedStrings.renderer.barChart] = $.pivotUtilities.c3_renderers["Bar Chart"];
        allRenderers[localizedStrings.renderer.stackedBarChart] =
            $.pivotUtilities.c3_renderers["Stacked Bar Chart"];
        allRenderers[localizedStrings.renderer.horizontalBarChart] =
            $.pivotUtilities.c3_renderers["Horizontal Bar Chart"];
        allRenderers[localizedStrings.renderer.horizontalStackedBarChart] =
            $.pivotUtilities.c3_renderers["Horizontal Stacked Bar Chart"];
        allRenderers[localizedStrings.renderer.areaChart] = $.pivotUtilities.c3_renderers["Area Chart"];
        allRenderers[localizedStrings.renderer.scatterChart] = $.pivotUtilities.c3_renderers["Scatter Chart"];
        allRenderers[localizedStrings.renderer.treemap] = $.pivotUtilities.d3_renderers["Treemap"];
        allRenderers[localizedStrings.renderer.TSVExport] = $.pivotUtilities.export_renderers["TSV Export"];

        $.pivotUtilities.locales[localizedStrings.localeCode] = {
            localeStrings: {
                renderError: localizedStrings.renderError,
                computeError: localizedStrings.computeError,
                uiRenderError: localizedStrings.uiRenderError,
                selectAll: localizedStrings.selectAll,
                selectNone: localizedStrings.selectNone,
                apply: localizedStrings.apply,
                cancel: localizedStrings.cancel,
                tooMany: localizedStrings.tooMany,
                filterResults: localizedStrings.filterResults,
                totals: localizedStrings.totals,
                vs: localizedStrings.vs,
                by: localizedStrings.by
            },
            aggregators: allAggregators,
            renderers: allRenderers,
            aggregatorsLocaleMapping: localizedStrings.aggregation,
            renderersLocaleMapping: localizedStrings.renderer
        };
    }

    _getLocalizedAttributes(attributes) {
        if (!this._options.properties) {
            return null;
        }
        let localizedAttributes = [];

        for (let attribute in attributes) {
            let localizedAttribute = this._options.properties[attribute];
            if (localizedAttribute === null || localizedAttribute === undefined || localizedAttribute === '') {
                localizedAttribute = attribute;
            }
            localizedAttributes.push(localizedAttribute);
        }

        return localizedAttributes;
    }

    _getLocalizedAggregatorName() {
        let aggregationMode = this._options.aggregation ? this._options.aggregation.mode : null;
        if (aggregationMode == null) {
            return null;
        }
        return this._options.localizedStrings.aggregation[aggregationMode];
    }

    _getLocalizedRendererName() {
        let renderMode = this._options.renderer ? this._options.renderer : null;
        if (renderMode == null) {
            return null;
        }
        return this._options.localizedStrings.renderer[renderMode];
    }

    _getLocalizedRenderers() {
        if (!this._options.renderers) {
            return null;
        }

        let localizedRenderers = {};
        for (let selectedRenderer of this._options.renderers.renderers) {
            let localizedKey = this._options.localizedStrings.renderer[selectedRenderer];

            localizedRenderers[localizedKey] = $.pivotUtilities.locales[this._options.localizedStrings.localeCode].renderers[localizedKey];

        }
        return localizedRenderers;
    }

    /**
     * @protected
     * @override
     */
    connectedCallback() {
        super.connectedCallback();
        // waiting for initialization
        setTimeout(() => this.$server.ready(), 200);
    }

    _updateOptions(changes) {
        this._options = changes.options;
        this._updateDerivedPropertiesPropertyName();
        this._processNativeJsFunctions(this._options)
        this._recreatePivot();
    }

    itemIds = [];
    itm = [];

    _updateDataSet(changes) {
        this._dataSet = changes.dataSet;
        this.items = {};
        if (changes.dataSet) {
            changes.dataSet.forEach(value => {
                this.itemIds.push(value.$k)
                delete value.$k;
            });
        }
        this._recreatePivot();
    }

    _updateDerivedPropertiesPropertyName() {
        if (this._options.derivedProperties && this._options.derivedProperties.properties) {
            let properties = this._options.derivedProperties.properties;
            for (let property in properties) {
                properties[property + "Function"] = properties[property];
                delete properties[property];
            }
        }
    }

    /**
     * @private
     */
    _processNativeJsFunctions(options) {
        let reFunction = /Function$/;

        for (let propertyName in options) {
            if (options.hasOwnProperty(propertyName) && propertyName.match(reFunction)) {
                let functionCode = options[propertyName];

                let startArgsIndex = functionCode.indexOf('(');
                let endArgsIndex = functionCode.indexOf(')');

                let startBodyIndex = functionCode.indexOf('{');
                let endBodyIndex = functionCode.lastIndexOf('}');

                if (startArgsIndex === -1 || endArgsIndex === -1 || startBodyIndex === -1 || endBodyIndex === -1) {
                    console.warn('Unparsable native JavaScript function: ' + functionCode);
                    continue;
                }

                let args = functionCode.slice(startArgsIndex + 1, endArgsIndex)
                    .split(',')
                    .map(element => element.trim());
                let body = functionCode.slice(startBodyIndex + 1, endBodyIndex).trim();

                options[propertyName.replace(reFunction, "")] = new Function(args, body);
                delete options[propertyName];
            } else if (typeof options[propertyName] == 'object') {
                this._processNativeJsFunctions(options[propertyName]);
            }
        }
    }
}

defineCustomElement(JmixPivotTable);