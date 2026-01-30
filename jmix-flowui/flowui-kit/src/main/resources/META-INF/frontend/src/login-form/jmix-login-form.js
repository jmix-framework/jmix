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

import { css, html, render } from 'lit';
import { ifDefined } from 'lit/directives/if-defined.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { LoginForm } from '@vaadin/login/src/vaadin-login-form.js';

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
// CAUTION: copied from @vaadin/login [last update Vaadin 25.0.0]
class JmixLoginForm extends LoginForm {
    static get is() {
        return 'jmix-login-form';
    }

    static get styles() {
        return css`
            vaadin-login-form-wrapper > form > * {
                width: 100%;
            }
        `;
    }

    static get properties() {
        return {
            username: {
                type: String,
                notify: true,
            },
            password: {
                type: String,
                notify: true,
            },
            rememberMeVisibility: {
                type: Boolean,
                value: true,
                notify: true,
            },
            localesVisibility: {
                type: Boolean,
                notify: true,
            },
            locales: {
                type: Object,
                value: []
            },
        }
    }

    /**
     * @protected
     */
    get _additionalFieldsBox() {
      return this.querySelector('#additionalFields');
    }

    /**
     * @protected
     */
    get _rememberMeCheckbox() {
      return this.querySelector('#rememberMeCheckbox');
    }

    /**
     * @protected
     */
    get _localesSelect() {
      return this.querySelector('#localesSelect');
    }

    /**
     * @protected
     */
    updated(props) {
        super.updated(props);

        if (props.has('username')) {
            const oldUsername = props.get('username');
            this._onUsernameChanged(oldUsername, this.username);
        }
        if (props.has('password')) {
            const oldPassword = props.get('password');
            this._onPasswordChanged(oldPassword, this.password);
        }
        if (props.has('locales')) {
            this._onLocalesChanged(this.locales);
        }
        if (props.has('rememberMeVisibility') || props.has('localesVisibility')) {
            this._onVisibilityPropertiesChanged(this.rememberMeVisibility, this.localesVisibility);
        }
        if (props.has('i18n')) {
            this._onI18nChanged(this.i18n);
        }
    }

    /**
     * CAUTION: copied from @vaadin/login/src/vaadin-login-form-mixin.js [last update Vaadin 25.0.0]
     * @override
     */
    __renderSlottedForm() {
      render(
        html`
          <form method="POST" action="${ifDefined(this.action)}" @formdata="${this._onFormData}" slot="form">
            <input id="csrf" type="hidden" />
            <vaadin-text-field
              name="username"
              .label="${this.__effectiveI18n.form.username}"
              .errorMessage="${this.__effectiveI18n.errorMessage.username}"
              id="vaadinLoginUsername"
              required
              @keydown="${this._handleInputKeydown}"
              autocapitalize="none"
              autocorrect="off"
              spellcheck="false"
              autocomplete="username"
              manual-validation
            >
              <input type="text" slot="input" @keyup="${this._handleInputKeyup}" />
            </vaadin-text-field>

            <vaadin-password-field
              name="password"
              .label="${this.__effectiveI18n.form.password}"
              .errorMessage="${this.__effectiveI18n.errorMessage.password}"
              id="vaadinLoginPassword"
              required
              @keydown="${this._handleInputKeydown}"
              spellcheck="false"
              autocomplete="current-password"
              manual-validation
            >
              <input type="password" slot="input" @keyup="${this._handleInputKeyup}" />
            </vaadin-password-field>

            ${this._renderAdditionalFields()}
          </form>

          <vaadin-button slot="submit" theme="primary submit" @click="${this.submit}" .disabled="${this.disabled}">
            ${this.__effectiveI18n.form.submit}
          </vaadin-button>

          <vaadin-button
            slot="forgot-password"
            theme="tertiary small"
            @click="${this._onForgotPasswordClick}"
            ?hidden="${this.noForgotPassword}"
          >
            ${this.__effectiveI18n.form.forgotPassword}
          </vaadin-button>
        `,
        this,
        { host: this },
      );
    }

    _renderAdditionalFields() {
        return html`
            <div id="additionalFields" class="jmix-login-form-additional-fields-container">
                <vaadin-checkbox
                    id="rememberMeCheckbox"
                    class="jmix-login-form-remember-me"
                    @checked-changed="${this._onRememberMeValueChange}"
                ></vaadin-checkbox>
                <vaadin-select
                    id="localesSelect"
                    class="jmix-login-form-locales-select"
                    @value-changed="${this._localeValueChanged}"
                ></vaadin-select>
            </div>
        `;
    }

    /**
     * Selects locale in the locales component.
     * Server callable function.
     *
     * @param {string} localeString the locale to select
     * @public
     */
    selectLocale(localeString) {
        if (!this._localesSelect) {
            return;
        }

        const currentValue = this._localesSelect.value;

        if (localeString && currentValue !== localeString) {
            this._localesSelect.jmixUserOriginated = false;
            this._localesSelect.value = localeString;
        }
    }

    /**
     * Sets the rememberMeCheckbox value.
     * Server callable function.
     *
     * @param {boolean} rememberMe whether checkbox should be active or inactive
     * @public
     */
    setRememberMe(rememberMe) {
        if (!this.rememberMeCheckbox) {
            return;
        }

        if (this.rememberMeCheckbox.checked !== rememberMe) {
            this.rememberMeCheckbox.jmixUserOriginated = false;
            this.rememberMeCheckbox.checked = rememberMe;
        }
    }

    /**
     * Observer for 'username' property change.
     *
     * @param {string} oldUsername previous username
     * @param {string} username new username
     * @protected
     */
    _onUsernameChanged(oldUsername, username) {
        this._userNameField.value = username;
    }

    /**
     * Observer for 'password' property change.
     *
     * @param {string} oldPassword previous password
     * @param {string} password new password
     * @protected
     */
    _onPasswordChanged(oldPassword, password) {
        this._passwordField.value = password;
    }

    /**
     * Observer for 'locales' property change.
     *
     * @param {string} items new items
     * @protected
     */
    _onLocalesChanged(items) {
        if (this._localesSelect) {
            this._localesSelect.items = items;
        }
    }

    /**
     * Observer for 'rememberMeVisibility' and 'localesVisibility' properties change.
     *
     * @param {boolean} localesVisibility new value for locales visibility
     * @param {boolean} rememberMeVisibility new value for rememberMe visibility
     * @protected
     */
    _onVisibilityPropertiesChanged(rememberMeVisibility, localesVisibility) {
        this._additionalFieldsBox.hidden = !rememberMeVisibility && !localesVisibility;
        this._rememberMeCheckbox.hidden = !rememberMeVisibility;
        this._localesSelect.hidden = !localesVisibility;
    }

    /**
     * Observer for 'i18n' property change.
     *
     * @param {object} i18n object contains localized labels
     * @protected
     */
    _onI18nChanged(i18n) {
        this._rememberMeCheckbox.label = this.i18n.form.rememberMe;
    }

    _localeValueChanged(e) {
        const localeString = e.detail.value;
        // The first event after initialization is fired for empty value.
        if (!localeString) {
            return;
        }

        if (this._localesSelect.jmixUserOriginated) {
            const customEvent = new CustomEvent('locale-selection-changed', {detail: {localeString: localeString}});
            this.dispatchEvent(customEvent);
        }
        this._localesSelect.jmixUserOriginated = true;
    }

    _onRememberMeValueChange(e) {
        if (this._rememberMeCheckbox.jmixUserOriginated) {
            const customEvent = new CustomEvent('remember-me-changed', {detail: {checked: e.detail.value}});
            this.dispatchEvent(customEvent);
        }
        this._rememberMeCheckbox.jmixUserOriginated = true;
    }
}

defineCustomElement(JmixLoginForm);

export {JmixLoginForm};
