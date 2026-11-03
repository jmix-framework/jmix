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
import {jmixValuePickerActionsShared} from "../../styles/value-picker-actions-shared-styles";

const jmixMultiSelectComboBoxPicker = css`
    [part="input-field"] {
        padding-block: 0;
        padding-inline-end: 0;
    }

    :host([has-actions]) [part="input-field"] {
        padding-inline-end: var(--vaadin-padding-xs);
    }

    /* The input padding sets the field height, so keep it when Vaadin removes it from a read-only or disabled field. */
    [part='input-field'] ::slotted(:is(input, textarea)),
    :host(:is([readonly], [disabled])) [part='input-field'] ::slotted(input) {
        padding: var(--vaadin-input-field-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
        padding-inline: 0;
    }

    /* The input is taller than the chips, so center the overflow chip next to it. */
    ::slotted([slot='overflow']) {
        align-self: center;
    }
`;

export const jmixMultiSelectComboBoxPickerStyles = [jmixValuePickerActionsShared, jmixMultiSelectComboBoxPicker];