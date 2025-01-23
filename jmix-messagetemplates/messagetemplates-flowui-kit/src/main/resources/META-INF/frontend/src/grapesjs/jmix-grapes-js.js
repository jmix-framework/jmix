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

import grapesjs from 'grapesjs';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ResizeMixin} from '@vaadin/component-base/src/resize-mixin.js';
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {Iconset} from '@vaadin/icon/vaadin-iconset.js';

class JmixGrapesJs extends ResizeMixin(ThemableMixin(ElementMixin(PolymerElement))) {

    _plugins = [];

    static get is() {
        return 'jmix-grapes-js';
    }

    static get properties() {
        return {
            readonly: {
                type: Boolean,
                observer: '_onReadOnlyChange',
                value: false,
                notify: true
            }
        }
    }

    /** @private */
    _layout() {
        const container = document.createElement('div');
        container.className = 'jmix-grapes-js-container';
        container.style = 'height: 100%';
        container.innerHTML = `
            <div part="content"></div>
        `;

        return container;
    }

    /** @private */
    _onReadOnlyChange(readonly) {
        if (this._editor !== undefined) {
            this._updateReadOnlyMode(this._editor);
        }
    }

    /** @private */
    _updateReadOnlyMode(editor) {
        const previewButton = editor.Panels.getButton('options', 'preview');

        previewButton.set('active', this.readonly);
        if (this.readonly) {
            editor.getContainer().getElementsByClassName("gjs-off-prv")[0].style = "display: none";
        }
    }

    /** @protected */
    ready() {
        super.ready();

        this.appendChild(this._layout());

        this.$server.requestPlugins()
            .then(plugins => this.loadPlugins(plugins))
            .then(() => this._createEditor());
    }

    /** @private */
    _createEditor() {
        this._editor = this._initEditor({
            plugins: this.getPluginInstances(),
            pluginsOpts: this.getPluginOpts()
        });

        this._editor.Panels.getButton('options', 'sw-visibility')
            .set('active', 1);
        this._editor.Panels.getButton('views', 'open-blocks')
            .set('active', 1);
        this._editor.Panels.removeButton('options', 'export-template');
        this._editor.Panels.removeButton('options', 'gjs-open-import-template');

        this._editor.on('update', () => {
            if (this.readonly) {
                return;
            }

            if (this._pendingValueFromServer) {
                this._pendingValueFromServer = false;
                return;
            }

            const customEvent = new CustomEvent('value-changed',
                {detail: {value: this.getHtml(this._editor)}}
            );

            this.dispatchEvent(customEvent);
        });

        // delayed update of readonly state
        // (otherwise state change error before initialization)
        this._editor.onReady(() => {
            if (this.readonly) {
                this._updateReadOnlyMode(this._editor);
            }
        });
    }

    /** @private */
    _initEditor(plugins) {
        return grapesjs.init({
            container: this.querySelector('[part="content"]'),
            components: this._valueFromServer ?? '<body></body>',
            forceClass: false,
            height: '100%',
            storageManager: false,
            showOffsets: true,
            plugins: plugins.plugins,
            pluginsOpts: plugins.pluginsOpts,
            domComponents: {
                draggableComponents: false
            },
            assetManager: {
                embedAsBase64: true
            },
            styleManager: {
                clearProperties: true,
                sectors: this._sectors ?? []
            },
            blockManager: {
                blocks: this._blocksFromServer ?? []
            }
        });
    }

    /** @private */
    async loadPlugins(plugins) {
        for (let plugin of plugins) {
            let loadedPlugin = await this.loadPlugin(plugin.name);

            if (loadedPlugin !== null) {
                this._plugins.push({
                    name: plugin.name,
                    instance: loadedPlugin.default,
                    options: plugin.options !== undefined ? JSON.parse(plugin.options) : undefined
                });
            }
        }
    }

    /**
     * Dynamically loads plugins so that a static code analyzer can resolve paths to them.
     * The only way to dynamically import.
     * The related Vite issue: https://github.com/vitejs/vite/issues/14102
     *
     * @private
     */
    async loadPlugin(pluginName) {
        switch (pluginName) {
            case 'grapesjs-blocks-basic':
                return await import('grapesjs-blocks-basic');
            case 'grapesjs-custom-code':
                return await import('grapesjs-custom-code');
            case 'grapesjs-blocks-flexbox':
                return await import('grapesjs-blocks-flexbox');
            case 'grapesjs-plugin-forms':
                return await import('grapesjs-plugin-forms');
            case 'grapesjs-preset-newsletter':
                this._inlineCssEnabled = true;
                return await import('grapesjs-preset-newsletter');
            case 'grapesjs-parser-postcss':
                return await import('grapesjs-parser-postcss');
            case 'grapesjs-style-filter':
                // initial properties for grapesjs-style-filter plugin
                this._sectors = [
                    {
                        id: 'extra',
                        name: 'Extra',
                        properties: [
                            {extend: 'filter'},
                            {extend: 'filter', property: 'backdrop-filter'},
                        ],
                    }
                ];
                return await import('grapesjs-style-filter');
            case 'grapesjs-tabs':
                return await import('grapesjs-tabs');
            case 'grapesjs-tooltip':
                return await import('grapesjs-tooltip');
            case 'grapesjs-tui-image-editor':
                return await import('grapesjs-tui-image-editor');
            case 'grapesjs-preset-webpage':
                return await import('grapesjs-preset-webpage');
            default:
                return null;
        }
    }

    updateValue(value) {
        if (this._editor === undefined) {
            this._valueFromServer = value;
        } else {
            this._pendingValueFromServer = true;
            this._editor.setComponents(value);
        }
    }

    updateBlocks(blocks) {
        if (this._editor === undefined) {
            this._blocksFromServer = blocks.map(block => {
                if (block.attributes) {
                    block.attributes = this.parseAttributes(block.attributes);
                }

                if (block.icon) {
                    block.media = this.parseIcon(block.icon);
                }

                return block;
            });
        } else {
            blocks.forEach(block => {
                this._editor.BlockManager.add(block.id, {
                    label: block.label,
                    content: block.content,
                    category: block.category,
                    attributes: block.attributes ? this.parseAttributes(block.attributes) : undefined,
                    media: block.icon ? this.parseIcon(block.icon) : undefined
                });
            });
        }
    }

    removeBlock(blockId) {
        if (this._editor === undefined) {
            const blockToRemove = this._blocksFromServer.find(block => blockId === block.id);
            this._blocksFromServer.removeBlock(blockToRemove);
        } else {
            this._editor.BlockManager.remove(blockId)
        }
    }

    removeBlocks(blocks) {
        blocks.forEach(block => this.removeBlock(block));
    }

    /** @private */
    getHtml(editor) {
        let result;

        if (this._inlineCssEnabled) {
            result = editor.runCommand('gjs-get-inlined-html');
        } else {
            result = editor.getHtml() + `<style>${editor.getCss()}</style>`;
        }

        return result.replace('&amp;', '&')
            .replace('&lt;', '<')
            .replace('&gt;', '>')
            .replace('&quot;', '\"')
            .replace('&#039;', '\'');
    }

    runCommand(command, params) {
        if (this._editor === undefined) {
            return;
        }

        return params === undefined
            ? this._editor.runCommand(command)
            : this._editor.runCommand(command, JSON.parse(params));
    }

    stopCommand(command, params) {
        if (this._editor === undefined) {
            return;
        }

        return params === undefined
            ? this._editor.stopCommand(command)
            : this._editor.stopCommand(command, JSON.parse(params));
    }

    /** @private */
    getPluginInstances() {
        return this._plugins.map(plugin => plugin.instance);
    }

    /** @private */
    getPluginOpts() {
        const optionMap = new Map();
        for (let plugin of this._plugins) {
            optionMap[plugin.instance] = plugin.options;
        }

        return optionMap;
    }

    /** @private */
    getSvgIcon(iconName) {
        const iconSvg = Iconset.getIconSvg(iconName);
        if (typeof iconSvg.svg == 'symbol') {
            return undefined;
        }

        return `<svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                    ${iconSvg.svg.values[0].values[0]}
                    </svg>`;
    }

    parseAttributes(attributes) {
        try {
            return JSON.parse(attributes);
        } catch (e) {
            console.error(`Cannot parse block attributes JSON: ${e}`);
        }

        return undefined;
    }

    parseIcon(icon) {
        try {
            return this.getSvgIcon(icon);
        } catch (e) {
            console.error(`Cannot parse block icon: ${e}`)
        }

        return undefined;
    }
}

defineCustomElement(JmixGrapesJs);

export {JmixGrapesJs};
