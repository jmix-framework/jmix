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

import 'ace-builds/src-noconflict/ace.js';
import 'ace-builds/esm-resolver.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {ResizeMixin} from '@vaadin/component-base/src/resize-mixin.js';
import {InputFieldMixin} from '@vaadin/field-base/src/input-field-mixin.js';
import {TooltipController} from '@vaadin/component-base/src/tooltip-controller.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {inputFieldShared} from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import {registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {jmixCodeEditorStyles} from './jmix-code-editor-styles';

registerStyles('jmix-code-editor', [inputFieldShared, jmixCodeEditorStyles], {moduleId: 'jmix-code-editor-styles'});

class JmixCodeEditor extends ResizeMixin(InputFieldMixin(ThemableMixin(ElementMixin(PolymerElement)))) {

    static get is() {
        return 'jmix-code-editor';
    }

    static get template() {
        return html`
            <div class="jmix-code-editor-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true"></span>
                </div>

                <div part="input-field"
                     readonly="[[readonly]]"
                     disabled="[[disabled]]"
                     invalid="[[invalid]]"></div>

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
        return {
            theme: {
                type: String,
                value: 'textmate',
                observer: '_onThemeChange'
            },

            mode: {
                type: String,
                value: 'plain_text',
                observer: '_onModeChange'
            },

            highlightActiveLine: {
                type: Boolean,
                value: true,
                observer: '_onHighlightActiveLineChange'
            },

            showGutter: {
                type: Boolean,
                value: true,
                observer: '_onShowGutterChange'
            },

            showLineNumbers: {
                type: Boolean,
                value: true,
                observer: '_onShowLineNumbersChange'
            },

            showPrintMargin: {
                type: Boolean,
                value: true,
                observer: '_onShowPrintMarginChange'
            },

            printMarginColumn: {
                type: Number,
                value: 80,
                observer: '_onPrintMarginColumnChange'
            },

            value: {
                type: String,
                observer: '_onValueChange'
            },

            fontSize: {
                type: String,
                value: '1rem',
                observer: '_onFontSizeChange'
            },

            /** @private */
            _editor: {
                type: Object,
            }
        }
    }

    static get observers() {
        return ['_disabledChanged(disabled, readonly, _editor)'];
    }

    /** @protected */
    ready() {
        super.ready();

        const editor = this.shadowRoot.querySelector('[part="input-field"]');

        this._editor = ace.edit(editor, {
            theme: "ace/theme/" + this.theme,
            mode: "ace/mode/" + this.mode,
            highlightActiveLine: this.highlightActiveLine,
            showGutter: this.showGutter,
            showLineNumbers: this.showLineNumbers,
            showPrintMargin: this.showPrintMargin,
            printMarginColumn: this.printMarginColumn,
            value: this.value,
            fontSize: this.fontSize,
            useWorker: false
        });


        this._tooltipController = new TooltipController(this);
        this._tooltipController.setPosition('top');
        this.addController(this._tooltipController);

        this._editor.on('blur', () => {
            const customEvent = new CustomEvent('value-changed', {detail: {value: this._editor.getValue()}});
            this.dispatchEvent(customEvent);
        });
    }

    /** @protected */
    _disabledChanged(disabled, readonly, editor) {
        if (disabled === undefined || readonly === undefined || editor === undefined) {
            return;
        }

        if (disabled || readonly) {
            editor.setReadOnly(true);
        } else {
            editor.setReadOnly(false);
        }

        this._editor.setReadOnly(readonly);
        this._editor.setHighlightActiveLine(!readonly);
        this._editor.setHighlightGutterLine(!readonly);
        this._editor.renderer.$cursorLayer.element.style.opacity = readonly
            ? 0
            : 1;
    }

    /**
     * @protected
     * @override
     */
    _onResize() {
        this._editor.resize();
    }

    /**
     * @protected
     */
    _onModeChange(mode) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.session.setMode("ace/mode/" + mode);
    }

    /**
     * @protected
     */
    _onThemeChange(theme) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setTheme("ace/theme/" + theme);
    }

    /**
     * @protected
     */
    _onHighlightActiveLineChange(showActiveLine) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setHighlightActiveLine(showActiveLine);
    }

    /**
     * @protected
     */
    _onShowGutterChange(showGutter) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setOption("showGutter", showGutter);
    }

    /**
     * @protected
     */
    _onShowLineNumbersChange(showLineNumbers) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setOption("showLineNumbers", showLineNumbers);
    }

    /**
     * @protected
     */
    _onShowPrintMarginChange(showPrintMargin) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setShowPrintMargin(showPrintMargin);
    }

    /**
     * @protected
     */
    _onValueChange(value) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setValue(value);
    }

    /**
     * @protected
     */
    _onFontSizeChange(fontSize) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setFontSize(fontSize);
    }

    /**
     * @protected
     */
    _onPrintMarginColumnChange(printMarginColumn) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setPrintMarginColumn(printMarginColumn);
    }

    get clearElement() {
        return null;
    }
}

customElements.define(JmixCodeEditor.is, JmixCodeEditor);

export {JmixCodeEditor};