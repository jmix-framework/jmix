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

import { inputFieldShared } from '@vaadin/vaadin-lumo-styles/mixins/input-field-shared.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const uploadField = css`
  :host(:hover:not([readonly]):not([focused])) [part='input-field']::after {
    opacity: 0;
  }
  
  ::slotted(:not([slot$='fix'])) {
    padding: 0;
    --_lumo-text-field-overflow-mask-image: 0;
    -webkit-mask-image: 0;
    mask-image: 0;
  }
`;

registerStyles('jmix-upload-field', [inputFieldShared, uploadField], {
    moduleId: 'lumo-jmix-upload-field-styles'
});
