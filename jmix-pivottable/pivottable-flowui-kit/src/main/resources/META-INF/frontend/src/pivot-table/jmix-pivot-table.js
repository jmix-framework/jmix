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
import './jmix-pivot-table.css';

import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

import {registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {jmixPivotTableStyles} from './jmix-pivot-table-styles.js';

import {PivotTableParser} from './jmix-pivot-table-parser.js';

registerStyles('jmix-pivot-table', [jmixPivotTableStyles], {moduleId: 'jmix-pivot-table-styles'});

export class JmixPivotTable extends ElementMixin(ThemableMixin(PolymerElement)) {
    static get is() {
        return 'jmix-pivot-table';
    }

    static get template() {
        return html`
            <div class="jmix-pivot-table-wrapper" style="height: inherit; width: inherit;">
                <slot name="pivot-table-slot"></slot>
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

    _getTableElementData() {
        return PivotTableParser.parseToJson(this, this._options.localizedStrings);
    }

    _refreshHandler(pivotState) {
        this._updateOrderDirection("a.pvtRowOrder", pivotState.rowOrder);
        this._updateOrderDirection("a.pvtColOrder", pivotState.colOrder);

        let renderer = Object.keys(this._options.localizedStrings.renderer)
            .find(key => this._options.localizedStrings.renderer[key] === pivotState.rendererName);
        let aggregation = Object.keys(this._options.localizedStrings.aggregation)
            .find(key => this._options.localizedStrings.aggregation[key] === pivotState.aggregatorName);

        if (!aggregation && this._options.aggregations && this._options.aggregations.aggregations) {
            for (let aggregator of this._options.aggregations.aggregations) {
                if (pivotState.aggregatorName === aggregator.caption) {
                    aggregation = aggregator.mode;
                    break;
                }
            }
        }

        if (!aggregation && this._options.aggregation &&
                pivotState.aggregatorName === this._options.aggregation.caption) {
            aggregation = this._options.aggregation.mode;
        }

        const customEvent = new CustomEvent('jmix-pivottable:refresh', {
            detail: {
                rows: pivotState.rows,
                columns: pivotState.cols,
                renderer: renderer,
                aggregationMode: aggregation,
                aggregationProperties: pivotState.vals,
                inclusions: pivotState.inclusions,
                exclusions: pivotState.exclusions,
                rowOrder: pivotState.rowOrder,
                columnOrder: pivotState.colOrder
            }
        });
        this.dispatchEvent(customEvent);
    }

    _cellClickHandler(value, filters, pivotData) {
        let dataItemKeys = [];
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
        let outputDiv = $("div.pivot-table-output");
        if (this._options) {
            if (!this._dataSet || Object.keys(this._dataSet).length == 0) {
                outputDiv.html(this._options.emptyDataMessage);
                return;
            }
            (function(pivotTable) {
                $.pivotUtilities.renderers = $.extend($.pivotUtilities.c3_renderers,
                    $.extend($.pivotUtilities.d3_renderers, $.pivotUtilities.renderers));
                pivotTable._initLocale();
                let showPivotFunction = pivotTable._options.showUI ? outputDiv.pivotUI : outputDiv.pivot;
                showPivotFunction.call(outputDiv, pivotTable._dataSet,
                    pivotTable._preparePivotTableOptions(),
                    false,
                    pivotTable._options.localeCode);
            })(this);
        }
    }

    _preparePivotTableOptions() {
        let options = this._options;
        let aggregationOptions = this._getAggregationOptions();
        let resultOptions = {
            onRefresh: (function(pivotTable) {
                return function(pivotState) {
                    pivotTable._refreshHandler(pivotState);
                };
            })(this),
            showUI: options.showUI,
            rows: options.rows,
            cols: options.columns,
            colOrder: options.columnOrder,
            rowOrder: options.rowOrder,
            aggregatorName: aggregationOptions.aggregatorName,
            aggregator: aggregationOptions.aggregator,
            aggregators: aggregationOptions.aggregators,
            vals: options.aggregationProperties,
            exclusions: options.exclusions,
            inclusions: options.inclusions,
            rendererName: this._getLocalizedRendererName(),
            renderers: this._getLocalizedRenderers(),
            derivedAttributes: options.derivedProperties ? options.derivedProperties.properties : null,
            localeStrings: options.localizedStrings,
            rendererOptions: options.rendererOptions,
            sorters: options.sorters,
            rendererOptions: {
                table: {
                    clickCallback: (function(pivotTable){
                        return function(event, value, filters, pivotData) {
                            pivotTable._cellClickHandler(value, filters, pivotData);
                        };
                    })(this),
                    rowTotals: options.showRowTotals ? options.showRowTotals : true,
                    colTotals: options.showColumnTotals ? options.showColumnTotals : true
                },
                heatmap : {
                    colorScaleGenerator : this._getColorScaleGenerator(options.rendererOptions)
                }
            },
            c3: {
                size: (function(renderOptions) {
                    return renderOptions && renderOptions.c3
                        ? renderOptions.c3.size
                        : null;
                    })(options.renderOptions)
            }
        };

        return resultOptions;
    }

    _mergeOptionsWithNativeJsonOptions(dst, src) {
        for (let property in src) {
            if (src.hasOwnProperty(property)) {
                if (src[property] && typeof src[property] === "object") {
                    if (!dst[property]) {
                        dst[property] = src[property];
                    } else {
                        this._mergeOptionsWithNativeJsonOptions(dst[property], src[property]);
                    }
                } else {
                    dst[property] = src[property];
                }
            }
        }
    }

    _getColorScaleGenerator(rendererOptions) {
        if (rendererOptions && rendererOptions.heatmap && rendererOptions.heatmap.colorScaleGenerator) {
            return rendererOptions.heatmap.colorScaleGenerator;
        }
        return this.theme == 'dark' ? this._heatmapColorScaleGeneratorForDarkTheme : null;
    }

    _heatmapColorScaleGeneratorForDarkTheme(values) {
        let min = Math.min.apply(Math, values);
        let max = Math.max.apply(Math, values);
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
        let formatFloat, formatInt, formatPercent, numberFormat, aggregatorTemplates;
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

        let allAggregators = {};
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

        let allRenderers = {};
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

        $.pivotUtilities.locales[this._options.localeCode] = {
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

    _getAggregationOptions() {
        let allAggregators = $.pivotUtilities.locales[this._options.localeCode].aggregators;
        let localeMapping = $.pivotUtilities.locales[this._options.localeCode].aggregatorsLocaleMapping;

        let aggregationOptions = {
            aggregatorName: null,
            aggregator: null,
            aggregators: []
        };

        if (this._options.showUI) {
            if (this._options.aggregations) {
                if (this._options.aggregations.selectedAggregation) {
                    aggregationOptions.aggregatorName = localeMapping[this._options.aggregations.selectedAggregation];
                }

                let aggregations = this._options.aggregations.aggregations;
                if (aggregations) {
                    let aggregators = {};
                    let aggregatorsIds = {};
                    for (let i = 0; i < aggregations.length; i++) {
                        let aggregatorCaption = aggregations[i].caption;
                        let aggregationKey = aggregatorCaption;

                        if (aggregations[i].custom) {
                            aggregators[aggregationKey] = window.eval("(" + aggregations[i]["function"] + ")");
                        } else {
                            let aggregatorName = localeMapping[aggregations[i].mode];
                            let aggregatorFunc = allAggregators[aggregatorName];
                            if (aggregatorFunc) {
                                aggregationKey = aggregatorCaption ? aggregatorCaption : aggregatorName;
                                aggregators[aggregationKey] = aggregatorFunc;
                                if (aggregatorCaption && aggregationOptions.aggregatorName == aggregatorName) {
                                    aggregationOptions.aggregatorName = aggregatorCaption;
                                }
                            }
                        }
                        aggregatorsIds[aggregationKey] = aggregations[i].id;
                    }
                    aggregationOptions.aggregators = aggregators;
                }
            } else {
                aggregationOptions.aggregators = allAggregators;
            }
        } else {
            if (this._options.aggregation) {
                if (this._options.aggregation.custom) {
                    aggregationOptions.aggregator = window.eval("(" + this._options.aggregation["function"] + ")");
                } else {
                    let aggregator = allAggregators[localeMapping[this._options.aggregation.mode]];
                    if (this._options.aggregation.properties) {
                        aggregator = aggregator(this._options.aggregation.properties);
                    } else {
                        aggregator = aggregator();
                    }
                    aggregationOptions.aggregator = aggregator;
                }
            } else {
                // Explicitly set default aggregator in order to use localized version
                aggregationOptions.aggregator = allAggregators[localeMapping["count"]]();
                aggregationOptions.aggregatorName = localeMapping["count"];
            }
        }

        // If selected aggregator name is not initialized, try to get value from options.aggregation.
        // It may have an aggregator name if the pivot table is shown in read-only mode or its state is saved in settings.
        if (!aggregationOptions.aggregatorName && this._options.aggregation) {
            aggregationOptions.aggregatorName = this._options.aggregation.caption
                    ? this._options.aggregation.caption
                    : localeMapping[this._options.aggregation.mode];
        }

        return aggregationOptions;
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
        let localizedAggregation = this._options.localizedStrings.aggregation;
        if (this._options.showUI) {
            if (this._options.aggregations) {
                if (this._options.aggregations.selectedAggregation) {
                    return localizedAggregation[this._options.aggregations.selectedAggregation];
                }
            }
        }

        let aggregationMode = this._options.aggregation ? this._options.aggregation.mode : null;
        if (aggregationMode == null) {
            return localizedAggregation.count;
        }
        return localizedAggregation[aggregationMode];
    }

    _getLocalizedRendererName() {
        let localizedRenderers = this._options.localizedStrings.renderer;
        if (this._options.showUI) {
            if (this._options.renderers) {
                if (this._options.renderers.selectedRenderer) {
                    return localizedRenderers[this._options.renderers.selectedRenderer];
                }
            }
        }

        if (!this._options.renderer) {
            return localizedRenderers.table;
        }
        return localizedRenderers[this._options.renderer];
    }

    _getLocalizedRenderers() {
        if (!this._options.renderers) {
            return $.pivotUtilities.locales[this._options.localeCode].renderers;
        }
        let localizedRenderers = {};
        for (let selectedRenderer of this._options.renderers.renderers) {
            let localizedKey = this._options.localizedStrings.renderer[selectedRenderer];
            localizedRenderers[localizedKey] = $.pivotUtilities.locales[this._options.localeCode].renderers[localizedKey];
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
        if (this._options.nativeJson) {
            this._mergeOptionsWithNativeJsonOptions(this._options, window.eval("(" + this._options.nativeJson + ")"));
        }
        this._updateDerivedPropertiesPropertyName();
        this._processNativeJsFunctions(this._options)
        this._recreatePivot();
    }

    _updateDataSet(changes) {
        this._dataSet = changes.dataSet;
        this.itemIds = [];
        if (changes.dataSet) {
            changes.dataSet.forEach(value => {
                this.itemIds.push(value.$k);
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