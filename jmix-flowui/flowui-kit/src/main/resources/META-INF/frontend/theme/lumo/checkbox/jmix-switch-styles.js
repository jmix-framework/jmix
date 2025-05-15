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

import '@vaadin/vaadin-lumo-styles/color.js';
import '@vaadin/vaadin-lumo-styles/font-icons.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/typography.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-switch',
    css`
        :host {
            color: var(--jmix-switch-label-color, var(--lumo-body-text-color));
            font-size: var(--jmix-switch-label-font-size, var(--lumo-font-size-m));
            font-family: var(--lumo-font-family);
            line-height: var(--lumo-line-height-s);
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
            -webkit-tap-highlight-color: transparent;
            -webkit-user-select: none;
            -moz-user-select: none;
            user-select: none;
            cursor: default;
            outline: none;
            
            --_switch-size: var(--jmix-switch-size, calc(var(--lumo-size-l) / 2));
            --_switch-background: var(--jmix-switch-background, var(--lumo-contrast-20pct));
            --_switch-indicator-size: var(--jmix-switch-indicator-size, calc(var(--lumo-size-m) / 2));
            --_focus-ring-color: var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
            --_focus-ring-width: var(--vaadin-focus-ring-width, 2px);
            --_selection-color: var(--vaadin-selection-color, var(--lumo-primary-color));
            --_invalid-background: var(--vaadin-input-field-invalid-background, var(--lumo-error-color-10pct));
            --_disabled-checked-indicator-color: var(--jmix-switch-disabled-checked-indicator-color, var(--lumo-contrast-30pct));
        }

        [part='label'] {
            display: flex;
            position: relative;
            max-width: max-content;
        }

        :host([has-label]) ::slotted(label) {
            padding: var(
                    --jmix-switch-label-padding,
                    var(--lumo-space-xs) var(--lumo-space-s) var(--lumo-space-xs) var(--lumo-space-xs)
            );
        }

        :host([dir='rtl'][has-label]) ::slotted(label) {
            padding: var(--lumo-space-xs) var(--lumo-space-xs) var(--lumo-space-xs) var(--lumo-space-s);
        }

        :host([has-label][required]) ::slotted(label) {
            padding-inline-end: var(--lumo-space-m);
        }

        [part='switch'] {
            box-sizing: border-box;
            width: calc(var(--_switch-size) * 2);
            height: var(--_switch-size);

            margin: var(--lumo-space-xs);

            position: relative;
            border-radius: var(--jmix-switch-border-radius, calc(var(--_switch-size) / 2));
            background: var(--_switch-background);
            transition: transform 0.2s cubic-bezier(0.12, 0.32, 0.54, 2),
            background-color 0.15s;
            cursor: var(--lumo-clickable-cursor);

            /* Default field border color */
            --_input-border-color: var(--vaadin-input-field-border-color, var(--lumo-contrast-50pct));
        }
        
        :host([checked]) {
            --vaadin-input-field-border-color: transparent;
        }
        
        :host([checked]) [part='switch'] {
            background-color: var(--_selection-color);
        }

        /* Checked indicator */

        [part='switch'] .indicator {
            width: var(--_switch-indicator-size);
            height: var(--_switch-indicator-size);
            border-radius: 50%;

            background-color: var(--jmix-switch-indicator-color, var(--lumo-tint-90pct));
            border: none;
            box-sizing: border-box;
            
            margin-inline-start: calc(var(--lumo-space-xs) / 2);
            transform: none;

            opacity: 1;
            transition: margin-inline-start 0.2s ease;

            /* Default field border color */
            --_input-border-color: var(--vaadin-input-field-border-color, var(--lumo-contrast-50pct));
        }

        :host([checked]) [part='switch'] .indicator {
            margin-inline-start: calc(100% - var(--_switch-indicator-size) - calc(var(--lumo-space-xs) / 2));
            background-color: var(--jmix-switch-checked-indicator-color, var(--lumo-primary-contrast-color));
        }

        /* Readonly */
        
        :host([readonly]:not([checked])) {
            color: var(--lumo-secondary-text-color);
        }

        :host([readonly]:not([checked])) [part='switch'] {
            background: transparent;
            box-shadow: none;
            border: var(--vaadin-input-field-readonly-border, 1px dashed var(--lumo-contrast-50pct));
        }

        :host([readonly]:not([checked])) [part='switch'] .indicator {
            background: transparent;
            box-shadow: none;
            border-radius: inherit;
            border: var(--vaadin-input-field-readonly-border, 1px dashed var(--lumo-contrast-50pct));
        }

        /* Focus ring */

        :host([focus-ring]) [part='switch'] {
            box-shadow: 0 0 0 1px var(--lumo-base-color),
            0 0 0 calc(var(--_focus-ring-width) + 1px) var(--_focus-ring-color),
            inset 0 0 0 var(--_input-border-width, 0) var(--_input-border-color);
        }

        :host([focus-ring][readonly]:not([checked])) [part='switch'] {
            box-shadow: 0 0 0 1px var(--lumo-base-color),
            0 0 0 calc(var(--_focus-ring-width) + 1px) var(--_focus-ring-color);
        }

        /* Disabled */

        :host([disabled]) {
            pointer-events: none;
            --vaadin-input-field-border-color: var(--lumo-contrast-20pct);
        }

        :host([disabled]) ::slotted(label) {
            color: inherit;
        }

        :host([disabled]) [part='switch'] {
            background-color: var(--jmix-switch-disabled-background, var(--lumo-contrast-10pct));
        }

        :host([disabled]) [part='switch'] .indicator {
            background-color: var(--_disabled-checked-indicator-color);
        }

        :host([disabled]) [part='label'],
        :host([disabled]) [part='helper-text'] {
            color: var(--lumo-disabled-text-color);
            -webkit-text-fill-color: var(--lumo-disabled-text-color);
        }

        :host([readonly][checked]:not([disabled])) [part='switch'] {
            background-color: var(--jmix-switch-readonly-checked-background, var(--lumo-contrast-70pct));
        }

        /* Used for activation "halo" */
        
        [part='switch']::after {
            position: absolute;
            content: "";
            pointer-events: none;
            color: transparent;
            width: 100%;
            height: 100%;
            line-height: var(--_switch-size);
            border-radius: inherit;
            background-color: inherit;
            transform: scale(1.2);
            opacity: 0;
            transition: transform 0.1s, opacity 0.8s;
        }

        /* Hover */

        :host(:not([checked]):not([disabled]):not([readonly]):not([invalid]):hover) [part='switch'] {
            background: var(--jmix-switch-background-hover, var(--lumo-contrast-30pct));
        }

        /* Disable hover for touch devices */
        
        @media (pointer: coarse) {
            /* prettier-ignore */
            :host(:not([checked]):not([disabled]):not([readonly]):not([invalid]):hover) [part='switch'] {
                background: var(--_switch-background);
            }
        }

        /* Active */

        :host([active]) [part='switch'] {
            transform: none;
        }

        :host([active]:not([checked])) [part='switch']::after {
            transition-duration: 0.01s, 0.01s;
            transform: scale(0);
            opacity: 0.4;
        }

        /* Must be defined in the Lumo theme in order to override the default styles */
        @media (prefers-reduced-motion: reduce) {
            [part='switch']::after {
                content: none;
            }
        }

        @media (forced-colors: active) {
            [part='switch'] {
                outline: 1px solid;
                outline-offset: -1px;
            }

            [part='switch'] .indicator {
                outline: 1px solid;
                outline-offset: -1px;
                border-radius: inherit;
                background-color: buttontext;
            }

            :host([disabled]) [part='switch'],
            :host([disabled]) [part='switch'] .indicator {
                outline-color: GrayText;
            }

            :host(:is([checked])) [part='switch'] {
                background: highlight;
            }

            :host(:is([checked])) [part='switch'] .indicator {
                outline: 1px solid;
                outline-offset: -1px;
                border-radius: inherit;
            }

            :host([focused]) [part='switch'] {
                outline-width: 2px;
            }
        }

        /* Required */
        
        :host([required]) [part='required-indicator'] {
            position: absolute;
            top: var(--lumo-space-xs);
            right: var(--lumo-space-xs);
        }

        :host([required][dir='rtl']) [part='required-indicator'] {
            right: auto;
            left: var(--lumo-space-xs);
        }

        :host([required]) [part='required-indicator']::after {
            content: var(--lumo-required-field-indicator, '\\2022');
            transition: opacity 0.2s;
            color: var(--lumo-required-field-indicator-color, var(--lumo-primary-text-color));
            width: 1em;
            text-align: center;
        }

        :host(:not([has-label])) [part='required-indicator'] {
            display: none;
        }

        /* Invalid */

        :host([invalid]) {
            --vaadin-input-field-border-color: var(--lumo-error-color);
        }

        :host([invalid]) [part='switch'] {
            background: var(--_invalid-background);
            background-image: linear-gradient(var(--_invalid-background) 0%, var(--_invalid-background) 100%);
        }

        :host([invalid]:hover) [part='switch'] {
            background-image: linear-gradient(var(--_invalid-background) 0%, var(--_invalid-background) 100%),
            linear-gradient(var(--_invalid-background) 0%, var(--_invalid-background) 100%);
        }

        :host([invalid][focus-ring]) {
            --_focus-ring-color: var(--lumo-error-color-50pct);
        }

        :host([invalid]) [part='required-indicator']::after {
            color: var(--lumo-required-field-indicator-color, var(--lumo-error-text-color));
        }

        /* Error message */

        [part='error-message'] {
            font-size: var(--vaadin-input-field-error-font-size, var(--lumo-font-size-xs));
            line-height: var(--lumo-line-height-xs);
            font-weight: var(--vaadin-input-field-error-font-weight, 400);
            color: var(--vaadin-input-field-error-color, var(--lumo-error-text-color));
            will-change: max-height;
            transition: 0.4s max-height;
            max-height: 5em;
            padding-inline-start: var(--lumo-space-xs);
        }

        :host([has-error-message]) [part='error-message']::after,
        :host([has-helper]) [part='helper-text']::after {
            content: '';
            display: block;
            height: 0.4em;
        }

        :host(:not([invalid])) [part='error-message'] {
            max-height: 0;
            overflow: hidden;
        }

        /* Helper */

        [part='helper-text'] {
            display: block;
            color: var(--vaadin-input-field-helper-color, var(--lumo-secondary-text-color));
            font-size: var(--vaadin-input-field-helper-font-size, var(--lumo-font-size-xs));
            line-height: var(--lumo-line-height-xs);
            font-weight: var(--vaadin-input-field-helper-font-weight, 400);
            margin-left: calc(var(--lumo-border-radius-m) / 4);
            transition: color 0.2s;
            padding-inline-start: var(--lumo-space-xs);
        }

        :host(:hover:not([readonly])) [part='helper-text'] {
            color: var(--lumo-body-text-color);
        }

        :host([has-error-message]) ::slotted(label),
        :host([has-helper]) ::slotted(label) {
            padding-bottom: 0;
        }
    `,

    // TODO: kd, workaround for https://github.com/vaadin/web-components/issues/2176
    //  rename to 'lumo-switch' after vaadin style order support
    {moduleId: 'jmix-lumo-switch' }
);
