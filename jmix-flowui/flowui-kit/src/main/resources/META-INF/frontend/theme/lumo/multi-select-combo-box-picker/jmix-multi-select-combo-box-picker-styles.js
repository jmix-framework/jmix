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

import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';


const multiSelectComboBoxPicker = css`
  [part="action-part"] ::slotted(*) {
    gap: var(--lumo-space-xs);
    margin-left: var(--lumo-space-xs);
  }
  
  :host([has-actions]) [part="input-field"] {
    padding-right: var(--lumo-space-xs);
  }
`;

registerStyles('jmix-multi-select-combo-box-picker', multiSelectComboBoxPicker,{
    moduleId: 'lumo-multi-select-combo-box-picker-styles'
});