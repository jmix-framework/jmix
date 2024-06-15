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
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
    }

    .jmix-twin-column-input-container {
        height: 100%;
        overflow-y: auto;
        overflow-x: auto;
        display: grid;
        grid-template-columns: 1fr 0fr 1fr;
        grid-template-rows: 0fr 1fr;
    }

    :host([has-width]) .jmix-twin-column-input-container {
        width: 100%;
    }

    ::slotted([slot="items-label"]) {
        grid-row: 1;
        grid-column: 1;
    }

    ::slotted([slot="selected-items-label"]) {
        grid-row: 1;
        grid-column: 3;
    }

    ::slotted([slot="items"]) {
        grid-row: 2;
        grid-column: 1;
        overflow-y: auto;
    }

    ::slotted([slot="actions"]) {
        grid-row: 2;
        grid-column: 2;
    }

    ::slotted([slot="selected-items"]) {
        grid-row: 2;
        grid-column: 3;
        overflow-y: auto;
    }

    :host(:not([has-label])) [part='label'] {
        display: none;
    }

    :host(:not([has-width])) ::slotted([slot="items"]),
    :host(:not([has-width])) ::slotted([slot="selected-items"]) {
        min-width: var(--jmix-twin-column-column-min-width, 15em);
        width: var(--jmix-twin-column-column-width, 15em);
        max-width: var(--jmix-twin-column-column-max-width, 15em);
    }

    :host(:not([has-height])) ::slotted([slot="items"]),
    :host(:not([has-height])) ::slotted([slot="selected-items"]) {
        min-height: var(--jmix-twin-column-column-min-height, 20em);
        height: var(--jmix-twin-column-column-height, 20em);
        max-height: var(--jmix-twin-column-column-max-height, 20em);
    }

    :host(:not([has-width])) ::slotted([slot="items-label"]),
    :host(:not([has-width])) ::slotted([slot="selected-items-label"]) {
        min-width: var(--jmix-twin-column-column-min-width, 15em);
        width: var(--jmix-twin-column-column-width, 15em);
        max-width: var(--jmix-twin-column-column-max-width, 15em);
    }

    :host(:not([has-height])) ::slotted([slot="actions"]) {
        min-height: var(--jmix-twin-column-column-min-height, 20em);
        height: var(--jmix-twin-column-column-height, 20em);
        max-height: var(--jmix-twin-column-column-max-height, 20em);
    }
`;

registerStyles('jmix-twin-column', [inputFieldShared, jmixTwinColumnStyles], {moduleId: 'jmix-twin-column-styles',});
