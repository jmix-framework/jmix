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

import '@vaadin/component-base/src/styles/style-props.js';
import { css } from 'lit';
import { checkable } from '@vaadin/field-base/src/styles/checkable-base-styles.js';
import { field } from '@vaadin/field-base/src/styles/field-base-styles.js';

const jmixSwitch = css`

    [part='switch'] {
        width: calc(var(--jmix-switch-size, 1lh) * 2);
        height: var(--jmix-switch-size, 1lh);
    }
    
    [part='switch'] .indicator {
        --_input-border-width: var(--vaadin-input-field-border-width, 0);
        --_input-border-color: var(--vaadin-input-field-border-color, transparent);
        box-shadow: inset 0 0 0 var(--_input-border-width, 0) var(--_input-border-color);
    }

    :host([readonly]) {
        --vaadin-checkbox-background: transparent;
        --vaadin-checkbox-border-color: var(--vaadin-border-color);
        --vaadin-checkbox-marker-color: var(--vaadin-text-color);
        --_border-style: dashed;
    }
`;

export const jmixSwitchStyles = [field, checkable('switch'), jmixSwitch];