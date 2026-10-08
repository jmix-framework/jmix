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

import 'ace-builds/src-min-noconflict/ace.js';
import './jmix-esm-resolver.js';
import {keyWordCompleter, snippetCompleter, textCompleter} from 'ace-builds/src-noconflict/ext-language_tools.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ResizeMixin} from '@vaadin/component-base/src/resize-mixin.js';
import {InputFieldMixin} from '@vaadin/field-base/src/input-field-mixin.js';
import {TooltipController} from '@vaadin/component-base/src/tooltip-controller.js';
import {html, LitElement} from 'lit';
import {inputFieldShared} from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {jmixCodeEditorStyles} from './styles/jmix-code-editor-base-styles';

/*
 * Ace appends the suggestions popup to the document body, so the popup is painted in the regular
 * stacking context. Vaadin shows overlays as popovers, i.e. in the browser top layer, which is painted
 * above the regular content regardless of the `z-index`, therefore the popup is promoted to the top
 * layer as well. See `JmixCodeEditor#_configureSuggestionsPopup()`.
 */
ace.require('ace/lib/dom').importCssString(`
    .ace_editor.ace_autocomplete[popover] {
        /* The user agent stylesheet centers a popover using 'inset: 0' and 'margin: auto', which
           overrides the position assigned to the popup by Ace. */
        inset: auto;
        margin: 0;
        /* A modal overlay disables pointer events on the document body. */
        pointer-events: auto;
    }

    /* Ace measures the popup right after making it visible and before the popup is promoted to the
       top layer, so the popup must keep its box while it is not shown as a popover. Ace hides the
       popup by an inline 'display: none' style, which still wins over this rule. */
    .ace_editor.ace_autocomplete[popover]:not(:popover-open) {
        display: block;
    }
`, 'jmix-code-editor-autocomplete.css');

class JmixCodeEditor extends ResizeMixin(InputFieldMixin(ThemableMixin(ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement)))))) {

    static get is() {
        return 'jmix-code-editor';
    }

    static get styles() {
        return [inputFieldShared].concat(jmixCodeEditorStyles);
    }

    render() {
        return html`
            <div class="jmix-code-editor-container">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true"></span>
                </div>

                <div part="input-field"
                     .readonly="${this.readonly}"
                     .disabled="${this.disabled}"
                     .invalid="${this.invalid}">
                    <div class="jmix-code-editor-canvas"></div>
                </div>

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

            tabSize: {
                type: Number,
                value: 4,
                observer: '_onTabSizeChange'
            },

            placeholder: {
                type: String,
                value: '',
                observer: '_onPlaceholderChange'
            },

            defaultSuggestionsEnabled: {
                type: Boolean,
                value: false,
                observer: '_onDefaultSuggestionsEnabledChange'
            },

            liveSuggestionsEnabled: {
                type: Boolean,
                value: false,
                observer: '_onLiveSuggestionsEnabledChange'
            },

            /**
             * Regular expression pattern for suggesting from server.
             */
            suggestOn: String,

            /**
             * Regular expression dependent on #suggestOn property.
             */
            suggestOnRegExp: {
                type: Object,
                computed: '_computeSuggestOnRegExp(suggestOn)'
            },

            /**
             * Flag for tracking the presence of a suggester on the server side.
             * Required to avoid unnecessary client-server interaction.
             */
            serverSuggesterSet: {
                type: Boolean,
                value: false,
                observer: '_onServerSuggesterSetChange'
            },

            /** @private */
            _editor: Object
        }
    }

    static get observers() {
        return ['_disabledChanged(disabled, readonly, _editor)'];
    }

    /** @protected */
    connectedCallback() {
        super.connectedCallback();

        if (this._editor !== undefined && this.theme === undefined) {
            this.initApplicationThemeObserver();
            this._applyTheme();
        }
    }

    /** @protected */
    disconnectedCallback() {
        super.disconnectedCallback();

        this._disconnectApplicationThemeObserver();
    }

    /** @protected */
    ready() {
        super.ready();

        const editor = this.shadowRoot.querySelector('.jmix-code-editor-canvas');

        this._editor = ace.edit(editor, {
            theme: "ace/theme/" + (this.theme ?? this._getApplicationEditorTheme()),
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
            tabSize: this.tabSize,
            placeholder: this.placeholder,
            enableBasicAutocompletion: true,
            enableLiveAutocompletion: this.liveSuggestionsEnabled,
            useWorker: false
        });

        if (this.theme === undefined) {
            this.initApplicationThemeObserver();
        }

        this._tooltipController = new TooltipController(this);
        this._tooltipController.setPosition('top');
        this._tooltipController.setAriaTarget(this._editor);
        this.addController(this._tooltipController);

        this._editor.on('blur', () => {
            this.value = this._editor.getValue();
            const customEvent = new CustomEvent('value-changed', {detail: {value: this._editor.getValue()}});
            this.dispatchEvent(customEvent);

            this._setFocused(false);
        });

        this.addEventListener('focus', (e) => {
            this._setFocused(true);
        });

        this._setFocusElement(this._editor.textInput.getElement());

        this.initSuggestionListeners();
        this.updateSuggestions();
        this.initSuggestionsPopup();
    }

    initApplicationThemeObserver() {
        // 'ready()' is reached from within 'super.connectedCallback()', so both may request the
        // observer during the same attach: the previous one must be released, not just replaced.
        this._disconnectApplicationThemeObserver();

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
     * Initializes the required listeners and values for suggestion mechanism to work.
     */
    initSuggestionListeners() {
        // initial value
        this._cursorPosition = 0;

        this._editor.session.selection.on('changeCursor', (e) => {
            this._internalValue = this._editor.getValue();
            const cursor = this._editor.selection.getCursor();

            this._cursorPosition = this._calculateAbsoluteCursorPosition(this._internalValue, cursor);

            // processing the flag set in the `change` handler
            if (this._autocompleteRequested) {
                this._autocompleteRequested = false;
                this._editor.execCommand('startAutocomplete');
            }
        });

        this._editor.session.on('change', (e) => {
            if (this.suggestOnRegExp
                && this._editor.getValue()
                // +1 because the cursor change happens later than the value change
                && this.suggestOnRegExp.test(this._editor.getValue().substring(0, this._cursorPosition + 1))) {

                // set a flag to invoke autocompletion during a subsequent `changeCursor` (due to current user input)
                this._autocompleteRequested = true;
            }
        });
    }

    /**
     * Updates the list of auto-completions depending on the current state of the editor.
     */
    updateSuggestions() {
        const suggesters = this.defaultSuggestionsEnabled
            ? [textCompleter, keyWordCompleter, snippetCompleter]
            : [];

        if (this.serverSuggesterSet) {
            suggesters.push(this._getServerSuggester());
        }

        this._editor.completers = suggesters;
    }

    /**
     * @returns suggester object that requests autocompletes from the server-side
     * @private
     */
    _getServerSuggester() {
        return {
            $server: this.$server,
            getValue: () => this._internalValue,
            getCursorPosition: () => this._cursorPosition,
            getCompletions: function (editor, session, pos, prefix, callback) {
                this.$server.getSuggestions(this.getValue() ?? '', this.getCursorPosition(), prefix)
                    .then(suggestions => {
                        suggestions = suggestions.map(this.suggestionMapper);
                        callback(null, suggestions);
                    });
            },
            suggestionMapper: function (suggestion) {
                return {
                    caption: suggestion.displayText,
                    value: suggestion.suggestionText,
                    meta: suggestion.descriptionText
                }
            }
        };
    }

    /**
     * Subscribes to the creation of the popup in which the suggestions are shown. Ace creates the
     * popup lazily, on the first suggestions request.
     */
    initSuggestionsPopup() {
        const {Autocomplete} = ace.require('ace/autocomplete');
        const completer = Autocomplete.for(this._editor);

        const createPopup = completer.$init.bind(completer);
        completer.$init = () => this._configureSuggestionsPopup(createPopup());
    }

    /**
     * Promotes the suggestions popup to the browser top layer for the time it is shown. Otherwise,
     * the popup of an editor placed in a Vaadin overlay, e.g. in a dialog window, is completely
     * covered by that overlay.
     *
     * @private
     */
    _configureSuggestionsPopup(popup) {
        const element = popup.container;
        element.popover = 'manual';

        // The popup is added to the top layer anew every time it is shown, so that it is placed
        // above the overlays opened in the meantime.
        popup.on('show', () => {
            if (!element.matches(':popover-open')) {
                element.showPopover();
            }
        });

        popup.on('hide', () => {
            if (element.matches(':popover-open')) {
                element.hidePopover();
            }
        });

        return popup;
    }

    /**
     * @protected
     */
    _applyTheme() {
        if (this._editor === undefined) {
            return;
        }

        // The application theme is applied to the editor directly instead of being written to the
        // 'theme' property: otherwise an implicitly applied theme is indistinguishable from a theme
        // set explicitly, and the latter gets silently overwritten.
        this._editor.setTheme("ace/theme/" + this._getApplicationEditorTheme());
    }

    /**
     * @protected
     */
    _disconnectApplicationThemeObserver() {
        if (this._applicationThemeObserver !== undefined) {
            this._applicationThemeObserver.disconnect();
            this._applicationThemeObserver = undefined;
        }
    }

    /**
     * @return the editor theme matching the current application theme
     * @protected
     */
    _getApplicationEditorTheme() {
        return this._getCurrentApplicationTheme() === 'dark' ? 'nord_dark' : 'textmate';
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

        // An explicitly set theme wins over the application theme.
        this._disconnectApplicationThemeObserver();

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
     * @private
     */
    _onTabSizeChange(tabSize) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.session.setTabSize(tabSize);
    }

    /**
     * @private
     */
    _onPlaceholderChange(placeholder) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setOption('placeholder', placeholder);
    }

    /**
     * @private
     */
    _onDefaultSuggestionsEnabledChange() {
        if (this._editor === undefined) {
            return;
        }

        this.updateSuggestions();
    }

    /**
     * @private
     */
    _onLiveSuggestionsEnabledChange(liveSuggestionsEnabled) {
        if (this._editor === undefined) {
            return;
        }

        this._editor.setOption('enableLiveAutocompletion', liveSuggestionsEnabled);
    }

    /**
     * @private
     */
    _computeSuggestOnRegExp(suggestOn) {
        return new RegExp(suggestOn + '$')
    }

    /**
     * @private
     */
    _onServerSuggesterSetChange(serverSuggesterSet) {
        if (this._editor === undefined) {
            return;
        }

        this.updateSuggestions();
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

    /**
     * Calculates the absolute value of the cursor position, taking into account all entered rows and columns.
     *
     * @return absolute cursor position index
     * @private
     */
    _calculateAbsoluteCursorPosition(value, cursor) {
        const lines = value.split('\n', -1);

        let currentLine = 0;
        let pos = 0;

        while (currentLine < cursor.row) {
            pos += lines[currentLine].length + 1;
            ++currentLine;
        }

        pos += cursor.column;
        return pos;
    }

    get clearElement() {
        return null;
    }
}

defineCustomElement(JmixCodeEditor);
