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
    
    :host {
        --_switch-size: var(--jmix-switch-size, 1lh);
        --_switch-indicator-offset: calc(var(--vaadin-padding-xs) / 2);
        --_switch-indicator-size: var(--jmix-switch-indicator-size, calc(var(--_switch-size) - var(--_switch-indicator-offset) * 2));
    }

    [part='switch'] {
        align-self: center;
        /* to remove inheritance from checkable styles */
        justify-content: unset;
        width: calc(var(--_switch-size) * 2);
        height: var(--_switch-size);

        border-radius: var(--jmix-switch-border-radius, calc(var(--_switch-size) / 2));
    }
    
    /* to remove default checkbox checkmark inherited from checkable styles */
    [part='switch']::after {
        content: none;
    }
    
    [part='switch'] .indicator {
        width: var(--_switch-indicator-size);
        height: var(--_switch-indicator-size);
        border-radius: 50%;

        box-sizing: border-box;
        
        margin-inline-start: var(--_switch-indicator-offset);
        transform: none;
        
        opacity: 1;
        transition: margin-inline-start 0.2s ease;
    }

    :host([checked]) [part='switch'] .indicator {
        margin-inline-start: calc(100% - var(--_switch-indicator-size) - var(--_switch-indicator-offset));
        background-color: var(--switch-checked-indicator-background-color, var(--_color));
    }

    :host([readonly]) {
        --vaadin-switch-background: transparent;
        --vaadin-switch-border-color: var(--vaadin-border-color);
        --vaadin-switch-marker-color: var(--vaadin-text-color);
        --_border-style: dashed;
    }
`;

export const jmixSwitchStyles = [field, checkable('switch'), jmixSwitch];
