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

import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const loginForm = css`
  .jmix-login-form-additional-fields-container {
    display: flex;
    gap: 0.5em;
    justify-content: end;
    padding-top: 1em;
  }
  .jmix-login-form-remember-me {
    align-self: center;
  }
  .jmix-login-form-locales-select {
    width: var(--material-jmix-login-form-locales-select-width, 8em);
  }
  
  vaadin-select.jmix-login-form-locales-select vaadin-select-value-button {
    width: 0;
  }
`;

registerStyles('jmix-login-form', [loginForm], {
    moduleId: 'lumo-jmix-login-form-styles'
});