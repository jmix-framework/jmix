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

export const jmixValuePickerButtonStyles = css`
    :host {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        white-space: nowrap;
        -webkit-tap-highlight-color: transparent;
        -webkit-user-select: none;
        user-select: none;
        cursor: var(--vaadin-clickable-cursor);
        box-sizing: border-box;
        flex-shrink: 0;
        height: var(--vaadin-button-height, fit-content);
        margin: var(--vaadin-button-margin, 0);
        padding: 1px;
        font-family: var(--vaadin-button-font-family, inherit);
        font-size: var(--vaadin-button-font-size, inherit);
        line-height: var(--vaadin-button-line-height, inherit);
        font-weight: var(--vaadin-button-font-weight, 500);
        color: var(--vaadin-button-text-color, var(--vaadin-text-color));
        background: var(--vaadin-button-background, var(--vaadin-background-container));
        background-origin: border-box;
        border: var(--vaadin-button-border-width, 1px) solid
        var(--vaadin-button-border-color, var(--vaadin-border-color-secondary));
        border-radius: var(--vaadin-button-border-radius, var(--vaadin-radius-m));
        touch-action: manipulation;
    }

    :host([hidden]) {
        display: none !important;
    }

    :host(:is([focus-ring], :focus-visible)) {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
        outline-offset: 1px;
    }

    :host([disabled]) {
        pointer-events: var(--_vaadin-button-disabled-pointer-events, none);
        cursor: var(--vaadin-disabled-cursor);
        opacity: 0.5;
    }

    [part='icon'] {
        flex: none;
    }

    @media (forced-colors: active) {
        :host {
            --vaadin-button-border-width: 1px;
            --vaadin-button-background: ButtonFace;
            --vaadin-button-text-color: ButtonText;
        }

        ::slotted(*) {
            forced-color-adjust: auto;
        }

        :host([disabled]) {
            --vaadin-button-background: transparent !important;
            --vaadin-button-border-color: GrayText !important;
            --vaadin-button-text-color: GrayText !important;
            opacity: 1;
        }
    }
`;