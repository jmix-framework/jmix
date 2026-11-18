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

import {css} from 'lit';

export const stepStyles = css`
    :host {
        --_indicator-size: var(--jmix-stepper-indicator-size, 1.75rem);
        --_accent-color: var(--jmix-stepper-accent-color,
                var(--_jmix-stepper-accent-color-default, var(--vaadin-text-color)));
        --_accent-contrast-color: var(--jmix-stepper-accent-contrast-color,
                var(--_jmix-stepper-accent-contrast-color-default, var(--vaadin-background-color)));
        --_completed-success-color: var(--jmix-stepper-completed-success-color,
                var(--_jmix-stepper-completed-success-color-default, light-dark(#2e7d32, #66bb6a)));
        --_connector-color: var(--jmix-stepper-connector-color, var(--vaadin-border-color-secondary));

        display: flex;
        align-items: center;
        flex: 1 0 auto;
        min-width: 0;
        box-sizing: border-box;
    }

    :host([hidden]) {
        display: none !important;
    }

    :host([last]) {
        flex-grow: 0;
    }

    :host([orientation='vertical']) {
        flex-direction: column;
        align-items: stretch;
        flex: none;
    }

    [part='button'] {
        display: flex;
        align-items: center;
        gap: var(--vaadin-gap-s, 0.5rem);
        min-width: 0;
        padding: var(--vaadin-padding-xs, 0.25rem);
        border-radius: var(--vaadin-radius-m, 0.5rem);
        color: var(--vaadin-text-color);
        cursor: default;
        outline: none;
    }

    :host([state='completed']) [part='button'],
    :host([state='visited']) [part='button'] {
        cursor: var(--vaadin-clickable-cursor, pointer);
    }

    :host([state='unavailable']) [part='button'] {
        color: var(--vaadin-text-color-disabled);
    }

    [part='button']:focus-visible {
        outline: var(--vaadin-focus-ring-width, 2px) solid var(--vaadin-focus-ring-color, currentColor);
        outline-offset: 1px;
    }

    [part='indicator'] {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        flex: none;
        box-sizing: border-box;
        width: var(--_indicator-size);
        height: var(--_indicator-size);
        border: 1px solid currentColor;
        border-radius: 50%;
        font-size: 0.875em;
        line-height: 1;
    }

    :host([state='current']) [part='indicator'],
    :host([state='completed']) [part='indicator'] {
        border-color: transparent;
        background: var(--_accent-color);
        color: var(--_accent-contrast-color);
    }

    :host([theme~='completed-success'][state='completed']) [part='indicator'] {
        background: var(--_completed-success-color);
    }

    [part='checkmark']::before {
        content: '';
        display: block;
        width: 1em;
        height: 1em;
        background: currentColor;
        mask: var(--_vaadin-icon-checkmark) 50% / 100% no-repeat;
    }

    ::slotted([slot='icon']) {
        --vaadin-icon-size: 1em;
        width: 1em;
        height: 1em;
    }

    [part='text'] {
        display: flex;
        flex-direction: column;
        min-width: 0;
    }

    [part='label'],
    [part='description'] {
        white-space: nowrap;
    }

    :host([state='current']) [part='label'] {
        font-weight: 600;
    }

    [part='description'] {
        font-size: 0.875em;
        color: var(--vaadin-text-color-secondary);
    }

    :host([collapsed]:not([state='current'])) [part='text'] {
        display: none;
    }

    :host([collapsed][state='current']) {
        flex-shrink: 1;
    }

    :host([collapsed][state='current']) [part='label'],
    :host([collapsed][state='current']) [part='description'] {
        overflow: hidden;
        text-overflow: ellipsis;
    }

    [part='connector'] {
        flex: 1 1 auto;
        min-width: 1rem;
        height: 1px;
        margin-inline: var(--vaadin-gap-s, 0.5rem);
        background: var(--_connector-color);
    }

    :host([theme~='completed-success'][state='completed']) [part='connector'] {
        background: var(--_completed-success-color);
    }

    :host([last]) [part='connector'] {
        display: none;
    }

    :host([orientation='vertical']) [part='connector'] {
        flex: none;
        width: 1px;
        min-width: 0;
        height: 1rem;
        margin-block: var(--vaadin-gap-xs, 0.25rem);
        margin-inline: calc(var(--vaadin-padding-xs, 0.25rem) + var(--_indicator-size) / 2) 0;
    }

    :host([theme~='small']) {
        --_indicator-size: var(--jmix-stepper-indicator-size, 1.375rem);
        font-size: 0.875em;
    }

    @media (forced-colors: active) {
        [part='indicator'] {
            border-color: CanvasText;
        }

        :host([state='current']) [part='indicator'],
        :host([state='completed']) [part='indicator'] {
            background: Highlight;
            color: HighlightText;
        }

        [part='checkmark']::before {
            forced-color-adjust: none;
            background: HighlightText;
        }

        [part='connector'] {
            background: CanvasText;
        }
    }
`;
