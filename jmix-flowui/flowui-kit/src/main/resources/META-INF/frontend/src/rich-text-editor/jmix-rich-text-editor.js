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

import './vendor/quill.min.js';
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {timeOut} from '@vaadin/component-base/src/async.js';
import {Debouncer} from '@vaadin/component-base/src/debounce.js';
import {LabelMixin} from "@vaadin/field-base/src/label-mixin.js";
import {FocusMixin} from '@vaadin/a11y-base/src/focus-mixin.js';
import {HelperController} from "@vaadin/field-base/src/helper-controller.js";
import {helper} from '@vaadin/vaadin-lumo-styles/mixins/helper.js';
import {jmixRichTextEditorStyles} from './jmix-rich-text-editor-styles.js';

const Quill = window.Quill;

// Workaround for text disappearing when accepting spellcheck suggestion
// See https://github.com/quilljs/quill/issues/2096#issuecomment-399576957
const Inline = Quill.import('blots/inline');

class CustomColor extends Inline {
    constructor(domNode, value) {
        super(domNode, value);

        // Map <font> properties
        domNode.style.color = domNode.color;

        const span = this.replaceWith(new Inline(Inline.create()));

        span.children.forEach((child) => {
            if (child.attributes) child.attributes.copy(span);
            if (child.unwrap) child.unwrap();
        });

        this.remove();

        return span; // eslint-disable-line no-constructor-return
    }
}

CustomColor.blotName = 'customColor';
CustomColor.tagName = 'FONT';

Quill.register(CustomColor, true);

/**
 * Source of an event.
 *
 * See also https://quilljs.com/docs/api/#events for detailed documentation.
 */
const SOURCE = {
    API: 'api',
    USER: 'user',
    SILENT: 'silent',
};

const STATE = {
    DEFAULT: 0,
    CLICKED: 1,
};

const HANDLERS = [
    'bold',
    'italic',
    'underline',
    'strike',
    'header', /* 1, 2, 3 */
    'script', /* sub, super */
    'list', /* ordered, bullet */
    'align', /* default, center, justify */
    'blockquote',
    'code-block',
];

registerStyles('jmix-rich-text-editor', [jmixRichTextEditorStyles, helper], {moduleId: 'jmix-rich-text-editor-styles'});

class RichTextEditor extends ElementMixin(FocusMixin(LabelMixin(ThemableMixin(PolymerElement)))) {
    static get is() {
        return 'jmix-rich-text-editor';
    }

    static get template() {
        return html`
            <div class="jmix-rich-text-editor-wrapper">
                <div part="label">
                    <slot name="label"></slot>
                    <span part="required-indicator" aria-hidden="true"></span>
                </div>

                <div part="editor">
                    <slot name="editor"></slot>
                </div>

                <div part="helper-text">
                    <slot name="helper"></slot>
                </div>

                <div part="error-message">
                    <slot name="error-message"></slot>
                </div>
            </div>
        `;
    }

    /** @private */
    _layout() {
        const container = document.createElement('div');
        container.className = 'jmix-rich-text-editor-container';
        container.innerHTML = `
                <div class="jmix-rich-text-editor-toolbar">
                    <span class="toolbar-group toolbar-group-emphasis">
                        <!-- Bold -->
                        <button class="toolbar-button toolbar-button-bold ql-bold"
                                i18n="bold">
                        </button>
    
                        <!-- Italic -->
                        <button class="toolbar-button toolbar-button-italic ql-italic"
                                i18n="italic">
                        </button>
    
                        <!-- Underline -->
                        <button class="toolbar-button toolbar-button-underline ql-underline"
                                i18n="underline">
                        </button>
    
                        <!-- Strike -->
                        <button class="toolbar-button toolbar-button-strike ql-strike"
                                i18n="strike">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-heading">
                        <!-- Header 1 -->
                        <button class="toolbar-button toolbar-button-h1 ql-header" value="1" 
                                i18n="h1">
                        </button>
                        
                        <!-- Header 2 -->
                        <button class="toolbar-button toolbar-button-h2 ql-header" value="2"
                                i18n="h2">
                        </button>
                        
                        <!-- Header 3 -->
                        <button class="toolbar-button toolbar-button-h3 ql-header" value="3"
                                i18n="h3">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-glyph-transformation">
                        <!-- Subscript -->
                        <button class="toolbar-button toolbar-button-subscript ql-script" value="sub"
                                i18n="subscript">
                        </button>
                        
                        <!-- Superscript -->
                        <button class="toolbar-button toolbar-button-superscript ql-script" value="super"
                                i18n="superscript">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-list">
                        <!-- Ordered list -->
                        <button class="toolbar-button toolbar-button-list-ordered ql-list" value="ordered"
                                i18n="listOrdered">
                        </button>
                        
                        <!-- Unordered list -->
                        <button class="toolbar-button toolbar-button-list-bullet ql-list" value="bullet"
                                i18n="listBullet">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-alignment">
                        <!-- Align start (left for ltr and right for rtl) -->
                        <button class="toolbar-button toolbar-button-align-start ql-align" value="" 
                                i18n="alignStart">
                        </button>
                        
                        <!-- Align center -->
                        <button class="toolbar-button toolbar-button-align-center ql-align" value="center"
                                i18n="alignCenter">
                        </button>
                        
                        <!-- Align end (right for ltr and left for rtl) -->
                        <button class="toolbar-button toolbar-button-align-end ql-align" value="right"
                                i18n="alignEnd">
                        </button>
                        
                        <!-- Align justify -->
                        <button class="toolbar-button toolbar-button-align-justify ql-align" value="justify"
                                i18n="alignJustify">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-rich-text">
                        <!-- Image -->
                        <button class="toolbar-button toolbar-button-image"
                                i18n="image">
                        </button>
                        
                        <!-- Link -->
                        <button class="toolbar-button toolbar-button-link ql-link"
                                i18n="link">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-block">
                        <!-- Blockquote -->
                        <button class="toolbar-button toolbar-button-blockquote ql-blockquote"
                                i18n="blockquote">
                        </button>
                        
                        <!-- Code block -->
                        <button class="toolbar-button toolbar-button-code-block ql-code-block"
                                i18n="codeBlock">
                        </button>
                    </span>
                    
                    <span class="toolbar-group toolbar-group-format">
                        <!-- Clean -->
                        <button class="toolbar-button toolbar-button-clean ql-clean"
                                i18n="clean">
                        </button>
                    </span>
                    
                    <input type="file"
                        class="jmix-rich-text-editor-file-input"
                        accept="image/png, image/gif, image/jpeg, image/bmp, image/x-icon"/>
                </div>
                
                <div class="jmix-rich-text-editor-content"></div>
        `;

        return container;
    }

    static get properties() {
        return {
            /**
             * Value is a list of the operations which describe change to the document.
             * Each of those operations describe the change at the current index.
             * They can be an `insert`, `delete` or `retain`. The format is as follows:
             *
             * ```js
             *  [
             *    { insert: 'Hello World' },
             *    { insert: '!', attributes: { bold: true }}
             *  ]
             * ```
             *
             * See also https://github.com/quilljs/delta for detailed documentation.
             * @type {string}
             */
            value: {
                type: String,
                notify: true,
                value: '',
                sync: true,
            },

            /**
             * HTML representation of the rich text editor content.
             */
            htmlValue: {
                type: String,
                notify: true,
                readOnly: true,
            },

            /**
             * When true, the user can not modify, nor copy the editor content.
             * @type {boolean}
             */
            disabled: {
                type: Boolean,
                value: false,
                reflectToAttribute: true,
            },

            /**
             * When true, the user can not modify the editor content, but can copy it.
             * @type {boolean}
             */
            readonly: {
                type: Boolean,
                value: false,
                reflectToAttribute: true,
            },

            /**
             * String used for the helper text.
             * @attr {string} helper-text
             */
            helperText: {
                type: String,
                observer: '_helperTextChanged',
            },

            /**
             * An object used to localize this component. The properties are used
             * e.g. as the tooltips for the editor toolbar buttons.
             *
             * @default {English/US}
             */
            i18n: {
                type: Object,
                value: () => {
                    return {
                        bold: 'bold',
                        italic: 'italic',
                        underline: 'underline',
                        strike: 'strike',
                        h1: 'h1',
                        h2: 'h2',
                        h3: 'h3',
                        subscript: 'subscript',
                        superscript: 'superscript',
                        listOrdered: 'list ordered',
                        listBullet: 'list bullet',
                        alignStart: 'align start',
                        alignCenter: 'align center',
                        alignEnd: 'align end',
                        alignJustify: 'align justify',
                        image: 'image',
                        link: 'link',
                        blockquote: 'blockquote',
                        codeBlock: 'code block',
                        clean: 'clean',
                    };
                },
            },

            /** @private */
            _editor: {
                type: Object,
                sync: true,
            },

            /**
             * Stores old value
             * @private
             */
            __oldValue: String,

            /** @private */
            __lastCommittedChange: {
                type: String,
                value: '',
            },
        };
    }

    static get observers() {
        return [
            '_valueChanged(value, _editor)',
            '_disabledChanged(disabled, readonly, _editor)'
        ];
    }

    /** @protected */
    async connectedCallback() {
        super.connectedCallback();

        if (!this.$ && this.updateComplete) {
            await this.updateComplete;
        }

        // Flush pending htmlValue only once the editor is fully initialized
        this.__flushPendingHtmlValue();
    }

    /** @private */
    __flushPendingHtmlValue() {
        if (this.__pendingHtmlValue) {
            this.setHtmlValueInternal(this.__pendingHtmlValue);
        }
    }

    constructor() {
        super();

        this._helperController = new HelperController(this);
        this._helperController.addEventListener('slot-content-changed', (event) => {
            const {hasContent, node} = event.detail;
            this.toggleAttribute('has-helper', hasContent);
        });
    }

    /** @protected */
    ready() {
        super.ready();

        // Default Quill implementation can't interact with shadow dom.
        // Creating all required elements in light dom.
        const layout = this._layout();
        layout.setAttribute('slot', 'editor');
        this.appendChild(layout);

        const editor = this.querySelector('.jmix-rich-text-editor-content');
        const toolbar = this.querySelector('.jmix-rich-text-editor-toolbar');

        const toolbarConfig = this._prepareToolbar(toolbar);

        this._editor = new Quill(editor, {
            modules: {
                toolbar: toolbarConfig,
            }
        });

        const qlEditor = editor.querySelector('.ql-editor');

        qlEditor.setAttribute('role', 'textbox');
        qlEditor.setAttribute('aria-multiline', 'true');

        this._editor.on('text-change', () => {
            const timeout = 200;
            this.__debounceSetValue = Debouncer.debounce(this.__debounceSetValue, timeOut.after(timeout), () => {
                this.value = JSON.stringify(this._editor.getContents().ops);
            });
        });

        qlEditor.addEventListener('focusout', () => {
            if (this._toolbarState !== STATE.DEFAULT) {
                this._cleanToolbarState();
            } else {
                this.__emitChangeEvent();
            }
        });

        qlEditor.addEventListener('focus', () => {
            if (this._toolbarState !== STATE.CLICKED) {
                this._cleanToolbarState();
            }
        });

        this.applyLocalization();
    }

    /** @private */
    _prepareToolbar(toolbar) {
        const imageBtn = toolbar.querySelector('.toolbar-button-image');
        imageBtn.onclick = () => {
            this._onImageClick()
        };

        imageBtn.ontouchend = (event) => {
            this._onImageTouchEnd(event)
        };

        const fileInput = toolbar.querySelector('.jmix-rich-text-editor-file-input');
        fileInput.onchange = (event) => {
            this._uploadImage(event)
        };

        const clean = Quill.imports['modules/toolbar'].DEFAULTS.handlers.clean;
        const self = this;

        /**
         * It's easier to override quill's handlers rather
         * handle both 'click' and 'touch' events
         */
        const toolbarConfig = {
            container: toolbar,
            handlers: {
                clean() {
                    self._markToolbarClicked();
                    clean.call(this);
                },
            },
        };

        HANDLERS.forEach((handler) => {
            toolbarConfig.handlers[handler] = (value) => {
                self._markToolbarClicked();
                self._editor.format(handler, value, SOURCE.USER);
            };
        });

        return toolbarConfig;
    }

    /**
     * Sets content represented by HTML snippet into the editor.
     * The snippet is interpreted by [Quill's Clipboard matchers](https://quilljs.com/docs/modules/clipboard/#matchers),
     * which may not produce the exactly input HTML.
     *
     * **NOTE:** Improper handling of HTML can lead to cross site scripting (XSS) and failure to sanitize
     * properly is both notoriously error-prone and a leading cause of web vulnerabilities.
     * This method is aptly named to ensure the developer has taken the necessary precautions.
     * @param {string} htmlValue
     */
    setHtmlValueInternal(htmlValue) {
        if (!this._editor) {
            // The editor isn't ready yet, store the value for later
            this.__pendingHtmlValue = htmlValue;
            // Clear a possible value to prevent it from clearing the pending htmlValue once the editor property is set
            this.value = '';
            return;
        }

        const whitespaceCharacters = {
            '\t': '__RICH_TEXT_EDITOR_TAB',
            '  ': '__RICH_TEXT_EDITOR_DOUBLE_SPACE',
        };
        // Replace whitespace characters with placeholders before the Delta conversion to prevent Quill from trimming them
        Object.entries(whitespaceCharacters).forEach(([character, replacement]) => {
            htmlValue = htmlValue.replaceAll(/>[^<]*</gu, (match) => match.replaceAll(character, replacement)); // NOSONAR
        });

        const deltaFromHtml = this._editor.clipboard.convert(htmlValue);

        // Restore whitespace characters after the conversion
        Object.entries(whitespaceCharacters).forEach(([character, replacement]) => {
            deltaFromHtml.ops.forEach((op) => {
                if (typeof op.insert === 'string') {
                    op.insert = op.insert.replaceAll(replacement, character);
                }
            });
        });

        this._editor.setContents(deltaFromHtml, SOURCE.API);
    }

    /** @private */
    _valueChanged(value, editor) {
        if (value && this.__pendingHtmlValue) {
            // A non-empty value is set explicitly. Clear pending htmlValue to prevent it from overriding the value.
            this.__pendingHtmlValue = undefined;
        }

        if (editor === undefined) {
            return;
        }

        if (value == null || value === '[{"insert":"\\n"}]') {
            this.value = '';
            return;
        }

        if (value === '') {
            this._clear();
            return;
        }

        let parsedValue;
        try {
            parsedValue = JSON.parse(value);
            if (Array.isArray(parsedValue)) {
                this.__oldValue = value;
            } else {
                throw new Error(`expected JSON string with array of objects, got: ${value}`);
            }
        } catch (err) {
            // Use old value in case new one is not suitable
            this.value = this.__oldValue;
            console.error('Invalid value set to rich-text-editor:', err);
            return;
        }
        const delta = new Quill.imports.delta(parsedValue);
        // Suppress text-change event to prevent infinite loop
        if (JSON.stringify(editor.getContents()) !== JSON.stringify(delta)) {
            editor.setContents(delta, SOURCE.SILENT);
        }
        this.__updateHtmlValue();

        if (this._toolbarState === STATE.CLICKED) {
            this._cleanToolbarState();
            this.__emitChangeEvent();
        } else if (!this._editor.hasFocus()) {
            // Value changed from outside
            this.__lastCommittedChange = this.value;
        }
    }

    /** @private */
    _disabledChanged(disabled, readonly, editor) {
        if (disabled === undefined || readonly === undefined || editor === undefined) {
            return;
        }

        if (disabled || readonly) {
            editor.enable(false);

            if (disabled) {
                this._toggleToolbarDisabled(true);
            }
        } else {
            editor.enable();

            if (this.__oldDisabled) {
                this._toggleToolbarDisabled(false);
            }
        }

        this.__oldDisabled = disabled;
    }

    /**
     * @param {string} helperText
     * @private
     */
    _helperTextChanged(helperText) {
        this._helperController.setHelperText(helperText);
    }

    applyLocalization() {
        const buttons = this._toolbarButtons;
        buttons.forEach((btn) => {
            const i18nKey = btn.getAttribute('i18n');
            const value = this.i18n[i18nKey];

            if (value) {
                btn.setAttribute('title', value);
            } else {
                btn.removeAttribute('title');
            }
        });
    }

    /** @private */
    _toggleToolbarDisabled(disable) {
        const buttons = this._toolbarButtons;
        if (disable) {
            buttons.forEach((btn) => btn.setAttribute('disabled', 'true'));
        } else {
            buttons.forEach((btn) => btn.removeAttribute('disabled'));
        }
    }

    /** @private */
    get _toolbarButtons() {
        return this.querySelectorAll('.jmix-rich-text-editor-toolbar button');
    }

    /** @private */
    __emitChangeEvent() {
        let lastCommittedChange = this.__lastCommittedChange;

        if (this.__debounceSetValue && this.__debounceSetValue.isActive()) {
            lastCommittedChange = this.value;
            this.__debounceSetValue.flush();
        }

        if (lastCommittedChange !== this.value) {
            this.dispatchEvent(new CustomEvent('change', {bubbles: true, cancelable: false}));
            this.__lastCommittedChange = this.value;
        }
    }

    /** @private */
    _clear() {
        this._editor.deleteText(0, this._editor.getLength(), SOURCE.SILENT);
        this.__updateHtmlValue();
    }

    /** @private */
    __updateHtmlValue() {
        const editor = this._editor.container.querySelector('.ql-editor');
        let content = editor.innerHTML;

        // Remove Quill classes, e.g. ql-syntax, except for align
        content = content.replace(/\s*ql-(?!align)[\w-]*\s*/gu, '');
        // Remove meta spans, e.g. cursor which are empty after Quill classes removed
        content = content.replace(/<\/?span[^>]*>/gu, '');

        // Replace Quill align classes with inline styles
        [this.__dir === 'rtl' ? 'left' : 'right', 'center', 'justify'].forEach((align) => {
            content = content.replace(
                new RegExp(` class=[\\\\]?"\\s?ql-align-${align}[\\\\]?"`, 'gu'),
                ` style="text-align: ${align}"`,
            );
        });

        content = content.replace(/ class=""/gu, '');

        this._setHtmlValue(content);
    }

    /** @private */
    _onImageTouchEnd(event) {
        // Cancel the event to avoid the following click event
        event.preventDefault();
        this._onImageClick();
    }

    /** @private */
    _onImageClick() {
        const fileInput = this.querySelector('.jmix-rich-text-editor-file-input');

        fileInput.value = '';
        fileInput.click();
    }

    /** @private */
    _uploadImage(e) {
        const fileInput = e.target;
        // NOTE: copied from https://github.com/quilljs/quill/blob/1.3.7/themes/base.js#L128
        if (fileInput.files != null && fileInput.files[0] != null) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const image = e.target.result;
                const range = this._editor.getSelection(true);
                this._editor.updateContents(
                    new Quill.imports.delta()
                        .retain(range.index)
                        .delete(range.length)
                        .insert({image}),
                    SOURCE.USER,
                );
                this._markToolbarClicked();
                this._editor.setSelection(range.index + 1, SOURCE.SILENT);
                fileInput.value = '';
            };
            reader.readAsDataURL(fileInput.files[0]);
        }
    }

    /** @private */
    _markToolbarClicked() {
        this._toolbarState = STATE.CLICKED;
    }

    /** @private */
    _cleanToolbarState() {
        this._toolbarState = STATE.DEFAULT;
    }
}

defineCustomElement(RichTextEditor);

export {RichTextEditor};