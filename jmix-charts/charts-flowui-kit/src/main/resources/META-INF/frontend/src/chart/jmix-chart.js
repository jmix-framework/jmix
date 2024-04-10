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

import * as echarts from 'echarts/dist/echarts.min';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {ResizeMixin} from '@vaadin/component-base/src/resize-mixin.js';

class JmixChart extends ResizeMixin(ElementMixin(PolymerElement)) {

    static get is() {
        return 'jmix-chart';
    }

    static get template() {
        return html`
            <style>
                :host {
                    width: 100%;
                    height: 100%;
                }
            </style>
            <div part="root" style="height: inherit; width: inherit;"/>
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
            _dataset: {
                type: Object
            },

            /** @private */
            _root: {
                type: Object
            }
        }
    }

    static get forwardedEventNames() {
        return ['click', 'dblclick', 'mousedown', 'mousemove', 'mouseup', 'mouseover', 'mouseout', 'globalout',
            'legendselectchanged', 'legendselectall', 'legendinverseselect', 'legendscroll', 'datazoom', 'restore',
            'magictypechanged', 'brush', 'brushend', 'brushselected', 'globalcursortaken', 'rendered', 'finished'
        ];
    }

    /**
     * @protected
     */
    ready() {
        super.ready();

        this.initApplicationThemeObserver();
    }

    _forwardEvents() {
        for (let eventName of JmixChart.forwardedEventNames) {
            this._root.on(eventName, (params) => {
                const detail = this._normalizeEventDetail({...params, event: undefined});
                const customEvent = new CustomEvent('jmix-chart:' + eventName,
                    {detail: detail});
                this.dispatchEvent(customEvent);
            });
        }
    }

    initApplicationThemeObserver() {
        // Apply current application theme as initial value
        this._applyTheme()

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
        const currentTheme = this._getCurrentApplicationTheme();

        if (currentTheme === "dark") {
            this.theme = "dark";
        } else if (currentTheme === "" || currentTheme === null) {
            this.theme = null;
        }
    }

    _onThemeChange() {
        if (this._root === undefined) {
            return;
        }

        this._destroyChart();
        const chart = this.shadowRoot.querySelector('[part="root"]');
        this._root = echarts.init(chart, this.theme);
        this._resetOptions();
        this._resetDataSet();
        this._forwardEvents();
    }

    /**
     * @protected
     */
    _getCurrentApplicationTheme() {
        return document.documentElement.getAttribute("theme");
    }

    /**
     * @protected
     * @override
     */
    _onResize() {
        if (this._root == null) {
            return;
        }

        this._root.resize();
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

    /**
     * @protected
     * @override
     */
    disconnectedCallback() {
        super.disconnectedCallback();
        this._destroyChart();
    }

    /**
     * @private
     */
    _destroyChart() {
        if (this._root == null) {
            return;
        }

        this._root.dispose();
        this._root = null;
    }

    _updateChart(changes) {
        if (this._root == null) {
            const chart = this.shadowRoot.querySelector('[part="root"]');
            this._root = echarts.init(chart, this.theme);
            this._forwardEvents();
        }

        // merge native json if exist
        if (changes.nativeJson !== undefined) {

            changes.options = this._deepAssign(changes.options, changes.nativeJson);
        }

        this._processNativeJsFunctions(changes.options);

        this._options = changes.options;
        this._resetOptions();
    }

    _updateChartDataset(changes) {
        this._dataset = changes.dataset

        this._resetDataSet();
    }

    _incrementalUpdateChartDataset(changes) {
        if (this._dataset === undefined) {
            return;
        }

        const items = changes.changedItems;

        const toAdd = items["add"];
        if (toAdd) {
            for (let toAddKey of toAdd) {
                this._dataset.source.push(toAddKey);
            }
        }

        const toRemove = items["remove"];
        if (toRemove) {
            for (let toRemoveKey of toRemove) {
                for (let i = 0; i < this._dataset.source.length; i++) {
                    if (this._dataset.source[i].$k === toRemoveKey.$k) {
                        this._dataset.source.splice(i, 1);
                        break;
                    }
                }
            }
        }

        const toUpdate = items["update"];
        if (toUpdate) {
            for (let toUpdateKey of toUpdate) {
                for (let i = 0; i < this._dataset.source.length; i++) {
                    if (this._dataset.source[i].$k === toUpdateKey.$k) {
                        this._dataset.source[i] = toUpdateKey;
                        break;
                    }
                }
            }
        }

        this._resetDataSet();
    }

    _resetOptions() {
        this._root.setOption(this._options);
    }

    _resetDataSet() {
        this._root.setOption({
            dataset: this._dataset
        });
    }

    /**
     * @private
     */
    _deepAssign(target, ...sources) {
        for (const source of sources) {
            for (let k in source) {
                let vs = source[k], vt = target[k]
                if (Object(vs) === vs && Object(vt) === vt) {
                    target[k] = this._deepAssign(vt, vs)
                    continue
                }
                target[k] = source[k]
            }
        }

        return target
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

    _normalizeEventDetail(detail) {
        if (detail.type === 'click' || detail.type === 'dblclick'
            || detail.type === 'mousedown' || detail.type === 'mousemove' || detail.type === 'mouseup'
            || detail.type === 'mouseout' || detail.type === 'mouseover') {

            if (!this._isObject(detail.data)) {
                detail.singleData = detail.data;
                detail.data = undefined;
            }
        }

        return detail;
    }

    /**
     * @private
     */
    _isObject(value) {
        return typeof value === 'object';
    }
}

defineCustomElement(JmixChart);

export {JmixChart};