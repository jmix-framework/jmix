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
 import '@vaadin/vaadin-lumo-styles/color.js';
 import '@vaadin/vaadin-lumo-styles/sizing.js';
 import '@vaadin/vaadin-lumo-styles/style.js';
 import '@vaadin/vaadin-lumo-styles/typography.js';
 import {inputFieldShared} from '@vaadin/vaadin-lumo-styles/mixins/input-field-shared.js';
 import {css, registerStyles} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const jmixTwinColumnStyles = css`
    .jmix-twin-column-container {
      display: grid;
      width: 100%;
      height: 100%;
      grid-template-columns: 1fr 0fr 1fr;
    }

    [part="label"] {
      grid-row: 1;
      grid-column: span 3;
    }

    ::slotted([slot="options-label"]) {
      grid-row: 2;
      grid-column: 1;
    }

    ::slotted([slot="selected-label"]) {
      grid-row: 2;
      grid-column: 3;
    }

    ::slotted([slot="options"]) {
      grid-row: 3;
      grid-column: 1;
      overflow-y: auto;
      min-height: var(--jmix-twin-column-column-min-height, 14.6em);
      min-width: var(--jmix-twin-column-column-min-width, 12em);
    }

    ::slotted([slot="actions"]) {
      grid-row: 3;
      grid-column: 2;
    }

    ::slotted([slot="selected"]) {
      grid-row: 3;
      grid-column: 3;
      overflow-y: auto;
      overflow-y: auto;
      min-height: var(--jmix-twin-column-column-min-height, 14.6em);
      min-width: var(--jmix-twin-column-column-min-width, 12em);
    }

    [part="helper-text"] {
      grid-row: 4;
    }

    [part="error-message"] {
      grid-row: 5;
    }
`;

registerStyles('jmix-twin-column', [inputFieldShared, jmixTwinColumnStyles], {moduleId: 'jmix-twin-column-styles',});
