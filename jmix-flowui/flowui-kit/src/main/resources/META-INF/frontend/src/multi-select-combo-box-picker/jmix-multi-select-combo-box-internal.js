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

import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { ComboBoxDataProviderMixin } from '@vaadin/combo-box/src/vaadin-combo-box-data-provider-mixin.js';
import { ComboBoxMixin } from '@vaadin/combo-box/src/vaadin-combo-box-mixin.js';
import { ComboBoxPlaceholder } from '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

// CAUTION: copied from @vaadin/login [last update Vaadin 24.0.3]
class JmixMultiSelectComboBoxInternal extends ComboBoxDataProviderMixin(ComboBoxMixin(ThemableMixin(PolymerElement))) {
    static get is() {
        return 'jmix-multi-select-combo-box-internal';
    }

    static get template() {
        return html`
      <style>
        :host([opened]) {
          pointer-events: auto;
        }
      </style>

      <slot></slot>

      <vaadin-multi-select-combo-box-overlay
        id="overlay"
        opened="[[_overlayOpened]]"
        loading$="[[loading]]"
        theme$="[[_theme]]"
        position-target="[[_target]]"
        no-vertical-overlap
        restore-focus-node="[[inputElement]]"
      ></vaadin-multi-select-combo-box-overlay>
    `;
    }

    static get properties() {
        return {
            /**
             * A subset of items, filtered based on the user input.
             */
            filteredItems: {
                type: Array,
                notify: true,
            },

            /**
             * When set to `true`, "loading" attribute is set
             * on the host and the overlay element.
             * @type {boolean}
             */
            loading: {
                type: Boolean,
                notify: true,
            },

            /**
             * Total number of items.
             * @type {number | undefined}
             */
            size: {
                type: Number,
                notify: true,
            },

            /**
             * Selected items to render in the dropdown
             * when the component is read-only.
             */
            selectedItems: {
                type: Array,
                value: () => [],
            },

            /**
             * Last input value entered by the user before value is updated.
             * Used to store `filter` property value before clearing it.
             */
            lastFilter: {
                type: String,
                notify: true,
            },

            _target: {
                type: Object,
            },
        };
    }

    /**
     * Reference to the clear button element.
     * @protected
     * @return {!HTMLElement}
     */
    get clearElement() {
        return this.querySelector('[part="clear-button"]');
    }

    /**
     * Tag name prefix used by scroller and items.
     * @protected
     * @return {string}
     */
    get _tagNamePrefix() {
        return 'vaadin-multi-select-combo-box';
    }

    /**
     * Override method inherited from the combo-box
     * to allow opening dropdown when readonly.
     * @override
     */
    open() {
        if (!this.disabled && !(this.readonly && this.selectedItems.length === 0)) {
            this.opened = true;
        }
    }

    /** @protected */
    ready() {
        super.ready();

        this._target = this;
        this._toggleElement = this.querySelector('.toggle-button');
        this._actionsBox = this.querySelector('[part="action-part"]');
    }

    /**
     * Override combo-box method to set correct owner for using by item renderers.
     * This needs to be done before the scroller gets added to the DOM to ensure
     * Lit directive works in case when combo-box is opened using attribute.
     *
     * @protected
     * @override
     */
    _initScroller() {
        const comboBox = this.getRootNode().host;

        super._initScroller(comboBox);
    }

    /**
     * Override method from `InputMixin`.
     *
     * @protected
     * @override
     */
    clear() {
        super.clear();

        if (this.inputElement) {
            this.inputElement.value = '';
        }
    }

    /**
     * Override Enter handler to keep overlay open
     * when item is selected or unselected.
     * @param {!Event} event
     * @protected
     * @override
     */
    _onEnter(event) {
        this.__enterPressed = true;

        super._onEnter(event);
    }

    /**
     * @protected
     * @override
     */
    _closeOrCommit() {
        if (this.readonly) {
            this.close();
            return;
        }

        if (this.__enterPressed) {
            this.__enterPressed = null;

            // Keep selected item focused after committing on Enter.
            const focusedItem = this.filteredItems[this._focusedIndex];
            this._commitValue();
            this._focusedIndex = this.filteredItems.indexOf(focusedItem);

            return;
        }

        super._closeOrCommit();
    }

    /**
     * @protected
     * @override
     */
    _commitValue() {
        // Store filter value for checking if user input is matching
        // an item which is already selected, to not un-select it.
        this.lastFilter = this.filter;

        super._commitValue();
    }

    /**
     * Override method inherited from the combo-box
     * to not update focused item when readonly.
     * @protected
     * @override
     */
    _onArrowDown() {
        if (!this.readonly) {
            super._onArrowDown();
        } else if (!this.opened) {
            this.open();
        }
    }

    /**
     * Override method inherited from the combo-box
     * to not update focused item when readonly.
     * @protected
     * @override
     */
    _onArrowUp() {
        if (!this.readonly) {
            super._onArrowUp();
        } else if (!this.opened) {
            this.open();
        }
    }

    /**
     * Override method inherited from the combo-box
     * to close dropdown on blur when readonly.
     * @param {FocusEvent} event
     * @protected
     * @override
     */
    _onFocusout(event) {
        // Disable combo-box logic that updates selectedItem
        // based on the overlay focused index on input blur
        this._ignoreCommitValue = true;

        super._onFocusout(event);

        if (this.readonly && !this._closeOnBlurIsPrevented) {
            this.close();
        }
    }

    /**
     * Override method inherited from the combo-box
     * to not commit an already selected item again
     * on blur, which would result in un-selecting.
     * @protected
     * @override
     */
    _detectAndDispatchChange() {
        if (this._ignoreCommitValue) {
            this._ignoreCommitValue = false;

            // Reset internal combo-box state
            this.selectedItem = null;
            this._inputElementValue = '';
            return;
        }

        super._detectAndDispatchChange();
    }

    /**
     * @param {CustomEvent} event
     * @protected
     * @override
     */
    _overlaySelectedItemChanged(event) {
        event.stopPropagation();

        // Do not un-select on click when readonly
        if (this.readonly) {
            return;
        }

        if (event.detail.item instanceof ComboBoxPlaceholder) {
            return;
        }

        if (this.opened) {
            this.dispatchEvent(
                new CustomEvent('combo-box-item-selected', {
                    detail: {
                        item: event.detail.item,
                    },
                }),
            );
        }
    }

    /**
     * Override method inherited from the combo-box
     * to not request data provider when read-only.
     *
     * @param {number}
     * @return {boolean}
     * @protected
     * @override
     */
    _shouldLoadPage(page) {
        if (this.readonly) {
            return false;
        }

        return super._shouldLoadPage(page);
    }

    /**
     * Override method inherited from the combo-box
     * to not clear the data provider cache when read-only.
     *
     * @protected
     * @override
     */
    clearCache() {
        if (this.readonly) {
            return;
        }

        super.clearCache();
    }

    /**
     * Jmix API
     *
     * @param {Event} event
     * @protected
     * @override
     */
    _onHostClick(event) {
        const path = event.composedPath();

        // Do not open dropdown when clicking on the picker actions
        if (!path.includes(this._actionsBox)) {
            super._onHostClick(event);
        }
    }
}

customElements.define(JmixMultiSelectComboBoxInternal.is, JmixMultiSelectComboBoxInternal);