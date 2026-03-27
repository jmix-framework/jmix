/*
 * Copyright 2026 Haulmont.
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

import '@vaadin/component-base/src/styles/style-props.js';
import {css} from 'lit';

export const jmixValuePickerStyles = css`

    [part="input-field"] {
        padding: 0;
    }

    :host([has-actions]) [part="input-field"] {
        padding-right: var(--vaadin-padding-xs);
    }

    [part='input-field'] ::slotted(:is(input, textarea)) {
        padding: var(--vaadin-input-field-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    }

    :host([focused]) [part='input-field'] ::slotted(:is(input, textarea)) {
        padding-inline-end: 0;
    }

    [part="action-part"] ::slotted(*) {
        display: flex;  
        gap: var(--vaadin-gap-xs);
    }

    :host([readonly]) [part="action-part"] {
        display: none;
    }
`;