/*
 * Copyright 2025 Haulmont.
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
import {ActiveMixin} from '@vaadin/a11y-base/src/active-mixin.js';
import {DelegateFocusMixin} from '@vaadin/a11y-base/src/delegate-focus-mixin.js';
import {CheckedMixin} from '@vaadin/field-base/src/checked-mixin.js';
import {FieldMixin} from '@vaadin/field-base/src/field-mixin.js';
import {InputController} from '@vaadin/field-base/src/input-controller.js';
import {LabelledInputController} from '@vaadin/field-base/src/labelled-input-controller.js';

export const SwitchMixin = (superclass) =>
    class SwitchMixinClass extends FieldMixin(CheckedMixin(DelegateFocusMixin(ActiveMixin(superclass)))) {

        static get properties() {
            return {
                /**
                 * The name of the switch.
                 *
                 * @type {string}
                 */
                name: {
                    type: String,
                    value: '',
                },

                /**
                 * When true, the user cannot modify the value of the switch.
                 * The difference between `disabled` and `readonly` is that the
                 * read-only switch remains focusable, is announced by screen
                 * readers and its value can be submitted as part of the form.
                 */
                readonly: {
                    type: Boolean,
                    value: false,
                    reflectToAttribute: true,
                },

                /**
                 * Indicates whether the element can be focused and where it participates in sequential keyboard navigation.
                 *
                 * @override
                 * @protected
                 */
                tabindex: {
                    type: Number,
                    value: 0,
                    reflectToAttribute: true,
                },
            }
        }

        static get observers() {
            return ['__readonlyChanged(readonly, inputElement)'];
        }

        /** @override */
        static get delegateAttrs() {
            return [...super.delegateAttrs, 'name', 'invalid', 'required'];
        }

        constructor() {
            super();

            this._setType('checkbox');

            this._boundOnInputClick = this._onInputClick.bind(this);

            // Set the string "on" as the default value for the switch following the HTML specification:
            // https://html.spec.whatwg.org/multipage/input.html#dom-input-value-default-on
            this.value = 'on';
        }

        /** @protected */
        ready() {
            super.ready();

            this.addController(
                new InputController(this, (input) => {
                    this._setInputElement(input);
                    this._setFocusElement(input);
                    this.stateTarget = input;
                    this.ariaTarget = input;
                }),
            );
            this.addController(new LabelledInputController(this.inputElement, this._labelController));

            this._createMethodObserver('_checkedChanged(checked)');
        }

        /**
         * Override method inherited from `ActiveMixin` to prevent setting `active`
         * attribute when readonly, or when clicking a link placed inside the label,
         * or when clicking slotted helper or error message element.
         *
         * @param {Event} event
         * @return {boolean}
         * @protected
         * @override
         */
        _shouldSetActive(event) {
            if (
                this.readonly ||
                event.target.localName === 'a' ||
                event.target === this._helperNode ||
                event.target === this._errorNode
            ) {
                return false;
            }

            return super._shouldSetActive(event);
        }

        /**
         * Override method inherited from `InputMixin`.
         * @param {!HTMLElement} input
         * @protected
         * @override
         */
        _addInputListeners(input) {
            super._addInputListeners(input);

            input.addEventListener('click', this._boundOnInputClick);
        }

        /**
         * Override method inherited from `InputMixin`.
         * @param {!HTMLElement} input
         * @protected
         * @override
         */
        _removeInputListeners(input) {
            super._removeInputListeners(input);

            input.removeEventListener('click', this._boundOnInputClick);
        }

        /** @private */
        _onInputClick(event) {
            // Prevent native switch checked change
            if (this.readonly) {
                event.preventDefault();
            }
        }

        /** @private */
        __readonlyChanged(readonly, inputElement) {
            if (!inputElement) {
                return;
            }

            // Use aria-readonly since native switch doesn't support readonly
            if (readonly) {
                inputElement.setAttribute('aria-readonly', 'true');
            } else {
                inputElement.removeAttribute('aria-readonly');
            }
        }

        /**
         * @override
         * @return {boolean}
         */
        checkValidity() {
            return !this.required || !!this.checked;
        }

        /**
         * Override method inherited from `FocusMixin` to validate on blur.
         * @param {boolean} focused
         * @protected
         */
        _setFocused(focused) {
            super._setFocused(focused);

            // Do not validate when focusout is caused by document
            // losing focus, which happens on browser tab switch.
            if (!focused && document.hasFocus()) {
                this._requestValidation();
            }
        }

        /** @private */
        _checkedChanged(checked) {
            if (checked || this.__oldChecked) {
                this._requestValidation();
            }

            this.__oldChecked = checked;
        }

        /**
         * Override an observer from `FieldMixin`
         * to validate when required is removed.
         *
         * @protected
         * @override
         */
        _requiredChanged(required) {
            super._requiredChanged(required);

            if (required === false) {
                this._requestValidation();
            }
        }

        /** @private */
        _onRequiredIndicatorClick() {
            this._labelNode.click();
        }
    };
