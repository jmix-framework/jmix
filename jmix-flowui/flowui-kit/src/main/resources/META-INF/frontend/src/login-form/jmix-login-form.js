/*
 * Copyright 2022 Haulmont.
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

import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/password-field/src/vaadin-password-field.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import '@vaadin/select/src/vaadin-select.js';
import '@vaadin/login/src/vaadin-login-form-wrapper.js';

import {html} from '@polymer/polymer/polymer-element.js';
import {LoginForm} from '@vaadin/login/src/vaadin-login-form.js';

/**
 * ### Styling
 *
 * The following custom properties are available for styling:
 *
 * Custom property                          | Description                               | Default
 * -----------------------------------------|-------------------------------------------|---------
 * `--jmix-login-form-locales-select-width` | Default width of the locales select field | `8em`
 *
 * The following attributes are available:
 *
 * Attribute                 | Description
 * --------------------------|-------------
 * `username`                | The username that is entered or provided from server-side.
 * `password`                | The password that is entered or provided from server-side.
 * `rememberMeVisibility`    | Defines the visibility of "Remember Me" component.
 * `localesVisibility`       | Defines the visibility of locales selection component.
 * `locales`                 | List of available locales to select.
 * `i18n`                    | CAUTION! Copied from LoginMixin. Additionally, defines "rememberMe" property.
 *
 * @fires {CustomEvent} remember-me-changed - Fired when "rememberMeCheckbox" is checked or unchecked.
 * @fires {CustomEvent} locale-selection-changed - Fired when selection in "localesSelect" is changed
 */
// CAUTION: copied from @vaadin/login [last update Vaadin 24.0.3]
class JmixLoginForm extends LoginForm {
    static get template() {
        return html`
            <style>
                vaadin-login-form-wrapper > form > * {
                    width: 100%;
                }
            </style>
            <vaadin-login-form-wrapper theme$="[[_theme]]" error="[[error]]" i18n="[[i18n]]">
                <form method="POST" action$="[[action]]" slot="form">
                    <input id="csrf" type="hidden"/>
                    <vaadin-text-field
                            name="username"
                            label="[[i18n.form.username]]"
                            id="vaadinLoginUsername"
                            required
                            on-keydown="_handleInputKeydown"
                            autocapitalize="none"
                            autocorrect="off"
                            spellcheck="false"
                            autocomplete="username"
                            value="[[username]]"
                    >
                        <input type="text" slot="input" on-keyup="_handleInputKeyup"/>
                    </vaadin-text-field>

                    <vaadin-password-field
                            name="password"
                            label="[[i18n.form.password]]"
                            id="vaadinLoginPassword"
                            required
                            on-keydown="_handleInputKeydown"
                            spellcheck="false"
                            autocomplete="current-password"
                            value="[[password]]"
                    >
                        <input type="password" slot="input" on-keyup="_handleInputKeyup"/>
                    </vaadin-password-field>

                    <div id="additionalFields" class="jmix-login-form-additional-fields-container">
                        <vaadin-checkbox id="rememberMeCheckbox"
                                         label="[[i18n.form.rememberMe]]"
                                         class="jmix-login-form-remember-me"></vaadin-checkbox>
                        <vaadin-select id="localesSelect"
                                       class="jmix-login-form-locales-select">
                        </vaadin-select>
                    </div>

                    <vaadin-button theme="primary contained submit" on-click="submit" disabled$="[[disabled]]">
                        [[i18n.form.submit]]
                    </vaadin-button>
                </form>

                <vaadin-button
                        slot="forgot-password"
                        theme="tertiary small"
                        on-click="_onForgotPasswordClick"
                        hidden$="[[noForgotPassword]]"
                >
                    [[i18n.form.forgotPassword]]
                </vaadin-button>
            </vaadin-login-form-wrapper>
        `;
    }

    static get is() {
        return 'jmix-login-form';
    }

    static get properties() {
        return {
            username: {
                type: String,
                value: null,
                notify: true
            },
            password: {
                type: String,
                value: null,
                notify: true
            },
            rememberMeVisibility: {
                type: Boolean,
                value: true
            },
            localesVisibility: {
                type: Boolean,
                value: true,
            },
            locales: {
                type: Object,
                value: []
            },
            /* CAUTION! Copied from LoginMixin */
            i18n: {
                type: Object,
                value: function () {
                    return {
                        form: {
                            title: 'Log in',
                            username: 'Username',
                            password: 'Password',
                            submit: 'Log in',
                            forgotPassword: 'Forgot password',
                            rememberMe: "Remember me"
                        },
                        errorMessage: {
                            title: 'Incorrect username or password',
                            message: 'Check that you have entered the correct username and password and try again.'
                        }
                    };
                },
                notify: true
            },
        }
    }

    static get observers() {
        return [
            '_onVisibilityPropertiesChanged(rememberMeVisibility, localesVisibility)',
            `_onLocalesPropertyChanged(locales)`
        ]
    }

    ready() {
        super.ready();
        this.$.localesSelect.addEventListener('value-changed', (e) => this._localeValueChanged(e));
        this.$.rememberMeCheckbox.addEventListener('checked-changed', (e) => this._onRememberMeValueChange(e));

        this.$.localesSelect.jmixUserOriginated = true
        this.$.rememberMeCheckbox.jmixUserOriginated = true
    }

    _onVisibilityPropertiesChanged(rememberMeVisibility, localesVisibility) {
        this.$.additionalFields.hidden = !rememberMeVisibility && !localesVisibility;
        this.$.rememberMeCheckbox.hidden = !rememberMeVisibility;
        this.$.localesSelect.hidden = !localesVisibility;
    }

    _onLocalesPropertyChanged(items) {
        this.$.localesSelect.items = items;
    }

    selectLocale(localeString) {
        const currentValue = this.$.localesSelect.value;

        if (localeString && currentValue !== localeString) {
            this.$.localesSelect.jmixUserOriginated = false;
            this.$.localesSelect.value = localeString;
        }
    }

    setRememberMe(rememberMe) {
        if (this.$.rememberMeCheckbox.checked !== rememberMe) {
            this.$.rememberMeCheckbox.jmixUserOriginated = false;
            this.$.rememberMeCheckbox.checked = rememberMe;
        }
    }

    _onRememberMeValueChange(e) {
        if (this.$.rememberMeCheckbox.jmixUserOriginated) {
            const customEvent = new CustomEvent('remember-me-changed', {detail: {checked: e.detail.value}});
            this.dispatchEvent(customEvent);
        }
        this.$.rememberMeCheckbox.jmixUserOriginated = true;
    }

    _localeValueChanged(e) {
        const localeString = e.detail.value;

        if (this.$.localesSelect.jmixUserOriginated) {
            const customEvent = new CustomEvent('locale-selection-changed', {detail: {localeString: localeString}});
            this.dispatchEvent(customEvent);
        }
        this.$.localesSelect.jmixUserOriginated = true;
    }
}

customElements.define(JmixLoginForm.is, JmixLoginForm);

export {JmixLoginForm};
