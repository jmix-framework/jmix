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

// If a project contains another jQuery library, a library conflict is possible.
// So PivotTable stores its own jQuery variable to isolate the PivotTable.js data.
var $pivotTablejQuery = jQuery.noConflict();

import './plugin/d3/d3.js';
import './plugin/c3/c3.js';
import './plugin/c3/c3.min.css';
import './pivot/pivot.js';
import './pivot/c3_renderers.min.js';
import './pivot/d3_renderers.min.js';
import './pivot/export_renderers.min.js';

import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {DisabledMixin} from '@vaadin/a11y-base/src/disabled-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {html, LitElement} from 'lit';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

import {PivotTableParser} from './jmix-pivot-table-parser.js';

/**
 * @class JmixPivotTable is the client part of the PivotTable component that integrates the
 * {@link https://github.com/nicolaskruchten/pivottable}[Pivot]
 */
export class JmixPivotTable extends ElementMixin(DisabledMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
        return 'jmix-pivot-table';
    }

    render() {
        return html`
            <div class="jmix-pivot-table-wrapper" style="height: inherit; width: inherit;">
                <slot name="pivot-table"></slot>
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
            _items: {
                type: Object
            }
        };
    }

    constructor() {
        super();

        this.$jQuery = $pivotTablejQuery;
    }

    ready() {
        super.ready();

        this._outputDiv = this._layout();
        this._outputDiv.setAttribute('slot', 'pivot-table');
        this.appendChild(this._outputDiv);

        this.initApplicationThemeObserver();
    }

    /** @private */
    _layout() {
        const container = document.createElement('div');
        container.className = 'pivot-table-output';
        return container;
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

        let reflectedProperties = {};
        if (this._options.properties) {
            for (let property in this._options.properties) {
                reflectedProperties[this._options.properties[property]] = property;
            }
        }

        function reflectProperties(properties, reflectedProperties) {
            let result = [];
            if (properties) {
                for (let i = 0; i < properties.length; i++) {
                    result[i] = reflectedProperties[properties[i]];
                }
            }
            return result;
        };

        function reflectMapProperties(properties, reflectedProperties) {
            let result = {};
            if (properties) {
                for (let property in properties) {
                    result[reflectedProperties[property]] = properties[property];
                }
            }
            return result;
        };

        const customEvent = new CustomEvent('jmix-pivottable:refresh', {
            detail: {
                rows: reflectProperties(pivotState.rows, reflectedProperties),
                columns: reflectProperties(pivotState.cols, reflectedProperties),
                renderer: renderer,
                aggregationMode: aggregation,
                aggregationProperties: reflectProperties(pivotState.vals, reflectedProperties),
                inclusions: reflectMapProperties(pivotState.inclusions, reflectedProperties),
                exclusions: reflectMapProperties(pivotState.exclusions, reflectedProperties),
                rowOrder: pivotState.rowOrder,
                columnOrder: pivotState.colOrder
            }
        });
        this.dispatchEvent(customEvent);
    }

    _cellClickHandler(value, filters, pivotData) {
        let itemsKeys = [];
        (function(pivotTable) {
            pivotData.forEachMatchingRecord(filters, function(record) {
                let itemIndex = pivotTable._items.indexOf(record);
                if (itemIndex >= 0) {
                    itemsKeys.push(pivotTable.itemIds[itemIndex]);
                }
            });
        })(this);

        const customEvent = new CustomEvent('jmix-pivottable:cellclick', {
            detail: {
                value: value,
                filters: filters,
                itemsKeys: itemsKeys
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

        let pivotTableOutputElement = this.$jQuery(this._outputDiv);

        pivotTableOutputElement.find(orderElementClassName).val("").html("");

        let currentOrder = order.replace(/_/g, '-');
        pivotTableOutputElement.find(orderElementClassName).removeClass(nextToCurrent[currentOrder]);
        pivotTableOutputElement.find(orderElementClassName).addClass(currentOrder);
    }

    _onRendererChange() {
        this._recreatePivot();
    }

    _recreatePivot() {
        if (this._options && this._outputDiv) {
            let outputDiv = this.$jQuery(this._outputDiv);
            if (!this._items || Object.keys(this._items).length == 0) {
                outputDiv.html(this._options.emptyDataMessage);
                return;
            }
            (function(pivotTable) {
                pivotTable.$jQuery.pivotUtilities.renderers = pivotTable.$jQuery.extend(pivotTable.$jQuery.pivotUtilities.c3_renderers,
                    pivotTable.$jQuery.extend(pivotTable.$jQuery.pivotUtilities.d3_renderers, pivotTable.$jQuery.pivotUtilities.renderers));
                pivotTable._initLocale();
                let options = pivotTable._preparePivotTableOptions();
                let showUI = pivotTable._options.showUI;
                options.showUI = showUI;
                let showPivotFunction = showUI ? outputDiv.pivotUI : outputDiv.pivot;
                showPivotFunction.call(outputDiv, pivotTable._items,
                    options,
                    true,
                    pivotTable._options.localeCode);
                if (pivotTable.disabled) {
                    pivotTable._disableElements();
                }
            })(this);
        }
    }

    _disabledChanged(disabled) {
        super._disabledChanged(disabled);

        this._recreatePivot();
    }

    _disableElements() {
        this.querySelectorAll('select').forEach(select => {
          select.disabled = true;
        });

        let pivotTableOutputElement = this.$jQuery(this._outputDiv);

        pivotTableOutputElement.find('.pvtAxisContainer').sortable('disable');
        pivotTableOutputElement.find('span.pvtAttr, li.ui-sortable-handle').addClass('disabled');
        pivotTableOutputElement.find('a.pvtRowOrder, a.pvtColOrder').unbind("click").addClass('disabled');
    }

    _preparePivotTableOptions() {
        let options = this._options;
        let aggregationOptions = this._getAggregationOptions();
        let renderOptions = this._getRenderOptions();
        let resultOptions = {
            onRefresh: (function(pivotTable) {
                return function(pivotState) {
                    pivotTable._refreshHandler(pivotState);
                };
            })(this),
            showUI: options.showUI,
            rows: this._localizeProperties(options.rows),
            cols: this._localizeProperties(options.columns),
            colOrder: options.columnOrder,
            rowOrder: options.rowOrder,
            aggregatorName: aggregationOptions.aggregatorName,
            aggregator: aggregationOptions.aggregator,
            aggregators: aggregationOptions.aggregators,
            vals: this._localizeProperties(options.aggregationProperties),
            exclusions: this._localizeMapProperties(options.exclusions),
            inclusions: this._localizeMapProperties(options.inclusions),
            rendererName: renderOptions.rendererName,
            renderers: renderOptions.renderers,
            renderer: renderOptions.renderer,
            derivedAttributes: options.derivedProperties ? options.derivedProperties.properties : null,
            localeStrings: options.localizedStrings,
            sorters: options.sorters,
            hiddenAttributes: this._localizeProperties(options.hiddenProperties),
            hiddenFromAggregators: this._localizeProperties(options.hiddenFromAggregations),
            hiddenFromDragDrop: this._localizeProperties(options.hiddenFromDragDrop),
            unusedAttrsVertical: this._getUnusedPropertiesVertical(options.unusedPropertiesVertical),
            autoSortUnusedAttrs: options.autoSortUnusedProperties,
            menuLimit: options.menuLimit,
            rendererOptions: {
                table: {
                    clickCallback: (function(pivotTable){
                        return function(event, value, filters, pivotData) {
                            pivotTable._cellClickHandler(value, filters, pivotData);
                        };
                    })(this),
                    rowTotals: options.showRowTotals,
                    colTotals: options.showColumnTotals
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

    _getUnusedPropertiesVertical(unusedPropertiesVertical) {
        if (unusedPropertiesVertical) {
            if (unusedPropertiesVertical.intVal) {
                return unusedPropertiesVertical.intVal;
            }
            return unusedPropertiesVertical.boolVal;
        }
        return null;
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
        if (!this._options.localizedStrings) {
            this._options.localizedStrings = this._createDefaultLocalizedStrings();
            this._options.localeCode = "en";
        }
        let formatFloat, formatInt, formatPercent, numberFormat, aggregatorTemplates;
        numberFormat = this.$jQuery.pivotUtilities.numberFormat;
        aggregatorTemplates = this.$jQuery.pivotUtilities.aggregatorTemplates;
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
        allRenderers[localizedStrings.renderer.table] = this.$jQuery.pivotUtilities.renderers["Table"];
        allRenderers[localizedStrings.renderer.tableBarchart] = this.$jQuery.pivotUtilities.renderers["Table Barchart"];
        allRenderers[localizedStrings.renderer.heatmap] = this.$jQuery.pivotUtilities.renderers["Heatmap"];
        allRenderers[localizedStrings.renderer.rowHeatmap] = this.$jQuery.pivotUtilities.renderers["Row Heatmap"];
        allRenderers[localizedStrings.renderer.colHeatmap] = this.$jQuery.pivotUtilities.renderers["Col Heatmap"];
        allRenderers[localizedStrings.renderer.lineChart] = this.$jQuery.pivotUtilities.c3_renderers["Line Chart"];
        allRenderers[localizedStrings.renderer.barChart] = this.$jQuery.pivotUtilities.c3_renderers["Bar Chart"];
        allRenderers[localizedStrings.renderer.stackedBarChart] =
            this.$jQuery.pivotUtilities.c3_renderers["Stacked Bar Chart"];
        allRenderers[localizedStrings.renderer.horizontalBarChart] =
            this.$jQuery.pivotUtilities.c3_renderers["Horizontal Bar Chart"];
        allRenderers[localizedStrings.renderer.horizontalStackedBarChart] =
            this.$jQuery.pivotUtilities.c3_renderers["Horizontal Stacked Bar Chart"];
        allRenderers[localizedStrings.renderer.areaChart] = this.$jQuery.pivotUtilities.c3_renderers["Area Chart"];
        allRenderers[localizedStrings.renderer.scatterChart] = this.$jQuery.pivotUtilities.c3_renderers["Scatter Chart"];
        allRenderers[localizedStrings.renderer.treemap] = this.$jQuery.pivotUtilities.d3_renderers["Treemap"];
        allRenderers[localizedStrings.renderer.TSVExport] = this.$jQuery.pivotUtilities.export_renderers["TSV Export"];

        this.$jQuery.pivotUtilities.locales[this._options.localeCode] = {
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

    _createDefaultLocalizedStrings() {
        return {
            "floatFormat": {
               "decimalSep": ".",
               "digitsAfterDecimal": "2",
               "showZero": "false",
               "scaler": "1",
               "prefix": "",
               "suffix": "",
               "thousandsSep": ","
           },
           "integerFormat": {
               "decimalSep": ".",
               "digitsAfterDecimal": "0",
               "showZero": "false",
               "scaler": "1",
               "prefix": "",
               "suffix": "",
               "thousandsSep": ","
           },
           "percentFormat": {
               "decimalSep": ".",
               "digitsAfterDecimal": "1",
               "showZero": "false",
               "scaler": "100",
               "prefix": "",
               "suffix": "%",
               "thousandsSep": ","
           },
           "percentFormat.suffix": "%",
           "renderError": "An error occurred rendering the PivotTable results.",
           "computeError": "An error occurred computing the PivotTable results.",
           "uiRenderError": "An error occurred rendering the PivotTable UI.",
           "selectAll": "Select all",
           "selectNone": "Select none",
           "apply": "Apply",
           "cancel": "Cancel",
           "tooMany": "(too many to list)",
           "filterResults": "Filter results",
           "totals": "Totals",
           "vs": "vs",
           "by": "by",
           "aggregation": {
               "count": "Count",
               "countUniqueValues": "Count unique values",
               "listUniqueValues": "List unique values",
               "sum": "Sum",
               "integerSum": "Integer sum",
               "average": "Average",
               "minimum": "Minimum",
               "maximum": "Maximum",
               "sumOverSum": "Sum over sum",
               "upperBound80": "80% Upper bound",
               "lowerBound80": "80% Lower bound",
               "sumAsFractionOfTotal": "Sum as fraction of total",
               "sumAsFractionOfRows": "Sum as fraction of rows",
               "sumAsFractionOfColumns": "Sum as fraction of columns",
               "countAsFractionOfTotal": "Count as fraction of total",
               "countAsFractionOfRows": "Count as fraction of rows",
               "countAsFractionOfColumns": "Count as fraction of columns"
           },
           "renderer": {
               "table": "Table",
               "tableBarchart": "Table barchart",
               "heatmap": "Heatmap",
               "rowHeatmap": "Row heatmap",
               "colHeatmap": "Col heatmap",
               "lineChart": "Line chart",
               "barChart": "Bar chart",
               "stackedBarChart": "Stacked bar chart",
               "horizontalBarChart": "Horizontal bar chart",
               "horizontalStackedBarChart": "Horizontal stacked bar chart",
               "areaChart": "Area chart",
               "scatterChart": "Scatter chart",
               "treemap": "Treemap",
               "TSVExport": "TSV export"
           }
       }
    }

    _getAggregationOptions() {
        let allAggregators = this.$jQuery.pivotUtilities.locales[this._options.localeCode].aggregators;
        let localeMapping = this.$jQuery.pivotUtilities.locales[this._options.localeCode].aggregatorsLocaleMapping;

        let aggregationOptions = {
            aggregatorName: null,
            aggregator: null,
            aggregators: []
        };

        if (this._options.showUI) {
            if (this._options.aggregations) {
                if (this._options.aggregations.selected) {
                    aggregationOptions.aggregatorName = localeMapping[this._options.aggregations.selected];
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
                } else {
                    aggregationOptions.aggregators = allAggregators;
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
                        aggregator = aggregator(this._localizeProperties(this._options.aggregation.properties));
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

    _localizeProperties(properties) {
        let localizedProperties = [];
        if (properties) {
            for (let i = 0; i < properties.length; i++) {
                localizedProperties[i] = this._options.properties != null && this._options.properties.hasOwnProperty(properties[i]) 
                    ? this._options.properties[properties[i]] 
                    : properties[i];
            }
        }
        return localizedProperties;
    }

    _localizeMapProperties(properties) {
        let localizedProperties = {};
        if (properties) {
            for (let property in properties) {
                let localizedProperty = this._options.properties != null && this._options.properties.hasOwnProperty(property) 
                    ? this._options.properties[property] 
                    : property;
                localizedProperties[localizedProperty] = properties[property];
            }
        }
        return localizedProperties;
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
                if (this._options.aggregations.selected) {
                    return localizedAggregation[this._options.aggregations.selected];
                }
            }
        }

        let aggregationMode = this._options.aggregation ? this._options.aggregation.mode : null;
        if (aggregationMode == null) {
            return localizedAggregation.count;
        }
        return localizedAggregation[aggregationMode];
    }

    _getRenderOptions() {
        let localizedRendererName = this._getLocalizedRendererName();
        let localizedRenderers = this.$jQuery.pivotUtilities.locales[this._options.localeCode].renderers;
        return {
            renderer: localizedRenderers[localizedRendererName],
            rendererName: localizedRendererName,
            renderers: this._getLocalizedRenderers()
        };
    }

    _getLocalizedRendererName() {
        let localizedRenderers = this._options.localizedStrings.renderer;
        if (this._options.showUI) {
            if (this._options.renderers) {
                if (this._options.renderers.selected) {
                    return localizedRenderers[this._options.renderers.selected];
                }
            }
        }

        if (!this._options.renderer) {
            return localizedRenderers.table;
        }
        return localizedRenderers[this._options.renderer];
    }

    _getLocalizedRenderers() {
        if (!this._options.renderers || !this._options.renderers.renderers) {
            return this.$jQuery.pivotUtilities.locales[this._options.localeCode].renderers;
        }
        let localizedRenderers = {};
        for (let selectedRenderer of this._options.renderers.renderers) {
            let localizedKey = this._options.localizedStrings.renderer[selectedRenderer];
            localizedRenderers[localizedKey] = this.$jQuery.pivotUtilities.locales[this._options.localeCode].renderers[localizedKey];
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

    _updateItems(changes) {
        this._items = changes.items;
        this.itemIds = [];
        if (changes.items) {
            changes.items.forEach(value => {
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