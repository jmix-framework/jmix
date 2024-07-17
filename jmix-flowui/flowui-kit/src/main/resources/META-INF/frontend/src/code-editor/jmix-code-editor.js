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
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
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
                observer: '_onThemeChange',
                notify: true
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

            highlightGutterLine: {
                type: Boolean,
                value: true,
                observer: '_onHighlightGutterLineChange'
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

            textWrap: {
                type: Boolean,
                value: false,
                observer: '_onTextWrapChange'
            },

            useSoftTabs: {
                type: Boolean,
                value: true,
                observer: '_onUseSoftTabsChange'
            },

            /** @private */
            _editor: {
                type: Object
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

        if (this.theme === undefined) {
            this.initApplicationThemeObserver();
        }

        this._editor = ace.edit(editor, {
            theme: "ace/theme/" + this.theme,
            mode: "ace/mode/" + this.mode,
            highlightActiveLine: this.highlightActiveLine,
            highlightGutterLine: this.highlightGutterLine,
            showGutter: this.showGutter,
            showLineNumbers: this.showLineNumbers,
            showPrintMargin: this.showPrintMargin,
            printMarginColumn: this.printMarginColumn,
            value: this.value,
            fontSize: this.fontSize,
            wrap: this.textWrap,
            useSoftTabs: this.useSoftTabs,
            useWorker: false
        });

        this._tooltipController = new TooltipController(this);
        this._tooltipController.setPosition('top');
        this._tooltipController.setAriaTarget(this._editor);
        this.addController(this._tooltipController);

        this._editor.on('blur', () => {
            const customEvent = new CustomEvent('value-changed', {detail: {value: this._editor.getValue()}});
            this.dispatchEvent(customEvent);

            this._setFocused(false);
        });

        this.addEventListener('focus', (e) => {
            this._setFocused(true);
        });

        this._setFocusElement(this._editor.textInput.getElement());
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

    /**
     * @protected
     */
    _applyTheme() {
        const currentTheme = this._getCurrentApplicationTheme();

        if (currentTheme === "dark") {
            this.theme = "nord_dark";
        } else if (currentTheme === "") {
            this.theme = "textmate";
        }
    }

    /**
     * @protected
     */
    _getCurrentApplicationTheme() {
        return document.documentElement.getAttribute("theme");
    }

    /**
     * @protected
     */
    _disabledChanged(disabled, readonly, editor) {
        if (disabled === undefined || readonly === undefined || editor === undefined) {
            return;
        }

        editor.setReadOnly(disabled || readonly);
        if (!disabled && !readonly) {
            editor.textInput.getElement().removeAttribute('disabled')
        }

        this._editor.setHighlightActiveLine(this.highlightActiveLine && !readonly);
        this._editor.setHighlightGutterLine(this.highlightGutterLine && !readonly);
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
    _onHighlightGutterLineChange(showGutterLine) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setHighlightGutterLine(showGutterLine);
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

        this._editor.session.setValue(value);
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
     * @private
     */
    _onTextWrapChange(textWrap) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.session.setUseWrapMode(textWrap);
    }

    /**
     * @private
     */
    _onUseSoftTabsChange(useSoftTabs) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.session.setUseSoftTabs(useSoftTabs);
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

defineCustomElement(JmixCodeEditor);

export {JmixCodeEditor};