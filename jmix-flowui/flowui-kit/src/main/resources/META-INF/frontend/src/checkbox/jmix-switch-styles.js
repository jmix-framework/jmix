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

import { css } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export const switchStyles = css`
    
    :host {
        display: inline-block;
    }
    
    :host([hidden]) {
        display: none !important;
    }
    
    :host([disabled]) {
        -webkit-tap-highlight-color: transparent;
    }
    
    .jmix-switch-container {
        display: grid;
        grid-template-columns: auto 1fr;
        align-items: baseline;
    }
    
    [part='switch'],
    ::slotted(input),
    [part='label'] {
        grid-row: 1;
    }
    
    [part='switch'],
    ::slotted(input) {
        grid-column: 1;
    }

    [part='helper-text'],
    [part='error-message'] {
        grid-column: 2;
    }

    :host(:not([has-helper])) [part='helper-text'],
    :host(:not([has-error-message])) [part='error-message'] {
        display: none;
    }

    [part='switch'] {
        display: inline-flex;
        align-items: center;
        align-self: anchor-center;

        width: calc(var(--jmix-switch-size, calc(2.75em / 2)) * 2);
        height: var(--jmix-switch-size, calc(2.75em / 2));
        
        --_input-border-width: var(--vaadin-input-field-border-width, 0);
        --_input-border-color: var(--vaadin-input-field-border-color, transparent);
        box-shadow: inset 0 0 0 var(--_input-border-width, 0) var(--_input-border-color);
    }
    
    [part='switch'] .indicator {
        --_input-border-width: var(--vaadin-input-field-border-width, 0);
        --_input-border-color: var(--vaadin-input-field-border-color, transparent);
        box-shadow: inset 0 0 0 var(--_input-border-width, 0) var(--_input-border-color);
    }

    /* visually hidden */
    ::slotted(input) {
        opacity: 0;
        cursor: inherit;
        margin: 0;
        align-self: stretch;
        -webkit-appearance: none;
        width: initial;
        height: initial;
    }
`;
