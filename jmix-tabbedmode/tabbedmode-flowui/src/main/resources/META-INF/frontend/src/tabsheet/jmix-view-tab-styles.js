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

export const jmixViewTabStyles = css`
    :host {
        box-sizing: border-box;
        padding: 0.5rem 0.75rem;
        font-family: var(--lumo-font-family);
        font-size: var(--lumo-font-size-m);
        line-height: var(--lumo-line-height-xs);
        font-weight: 500;
        opacity: 1;
        color: var(--lumo-secondary-text-color);
        transition: 0.15s color,
        0.2s transform;
        flex-shrink: 0;
        display: flex;
        gap: var(--lumo-space-s);
        align-items: center;
        position: relative;
        cursor: var(--lumo-clickable-cursor);
        transform-origin: 50% 100%;
        outline: none;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        overflow: hidden;
        min-width: var(--lumo-size-m);
        
        -webkit-user-select: none;
        user-select: none;
        
        --_focus-ring-color: var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
        --_focus-ring-width: var(--vaadin-focus-ring-width, 2px);
        
        --_selection-color: var(--vaadin-selection-color, var(--lumo-primary-color));
        --_selection-color-text: var(--vaadin-selection-color-text, var(--lumo-primary-text-color));
    }

    :host(:not([orientation='vertical'])) {
        text-align: center;
    }

    :host([orientation='vertical']) {
        transform-origin: 0% 50%;
        padding: 0.25rem 1rem;
        min-height: var(--lumo-size-m);
        min-width: 0;
    }

    @media (forced-colors: active) {
        :host([focused]) {
            outline: 1px solid;
            outline-offset: -1px;
        }

        :host([orientation='vertical'][selected]) {
            border-bottom: none;
            border-left: 2px solid;
        }
    }

    :host(:hover),
    :host([focus-ring]) {
        color: var(--lumo-body-text-color);
    }

    :host([selected]) {
        color: var(--_selection-color-text);
        transition: 0.6s color;
    }

    :host([active]:not([selected])) {
        color: var(--_selection-color-text);
        transition-duration: 0.1s;
    }

    :host::before,
    :host::after {
        content: '';
        position: absolute;
        display: var(--_lumo-tab-marker-display, block);
        bottom: 0;
        left: 50%;
        width: 100%;
        height: 2px;
        background-color: var(--lumo-contrast-60pct);
        border-radius: var(--lumo-border-radius-s) var(--lumo-border-radius-s) 0 0;
        transform: translateX(-50%) scale(0);
        transform-origin: 50% 100%;
        transition: 0.14s transform cubic-bezier(0.12, 0.32, 0.54, 1);
        will-change: transform;
    }

    :host([orientation='vertical'])::before,
    :host([orientation='vertical'])::after {
        left: 0;
        bottom: 50%;
        transform: translateY(50%) scale(0);
        width: 2px;
        height: var(--lumo-size-xs);
        border-radius: 0 var(--lumo-border-radius-s) var(--lumo-border-radius-s) 0;
        transform-origin: 100% 50%;
    }

    :host::after {
        box-shadow: 0 0 0 4px var(--_selection-color);
        opacity: 0.15;
        transition: 0.15s 0.02s transform,
        0.8s 0.17s opacity;
    }

    :host([selected])::before,
    :host([selected])::after {
        background-color: var(--_selection-color);
        transform: translateX(-50%) scale(1);
        transition-timing-function: cubic-bezier(0.12, 0.32, 0.54, 1.5);
    }

    :host([orientation='vertical'][selected])::before,
    :host([orientation='vertical'][selected])::after {
        transform: translateY(50%) scale(1);
    }

    :host([selected]:not([active]))::after {
        opacity: 0;
    }

    :host(:not([orientation='vertical'])) ::slotted(a[href]) {
        justify-content: center;
    }

    :host ::slotted(a) {
        display: flex;
        width: 100%;
        align-items: center;
        height: 100%;
        margin: -0.5rem -0.75rem;
        padding: 0.5rem 0.75rem;
        outline: none;

        /*
            Override the CSS inherited from \`lumo-color\` and \`lumo-typography\`.
            Note: \`!important\` is needed because of the \`:slotted\` specificity.
          */
        text-decoration: none !important;
        color: inherit !important;
    }

    :host ::slotted(vaadin-icon) {
        margin: 0 4px;
        width: var(--lumo-icon-size-m);
        height: var(--lumo-icon-size-m);
    }

    /* Vaadin icons are based on a 16x16 grid (unlike Lumo and Material icons with 24x24), so they look too big by default */

    :host ::slotted(vaadin-icon[icon^='vaadin:']) {
        padding: 0.25rem;
        box-sizing: border-box !important;
    }

    :host(:not([dir='rtl'])) ::slotted(vaadin-icon:first-child) {
        margin-left: 0;
    }

    :host(:not([dir='rtl'])) ::slotted(vaadin-icon:last-child) {
        margin-right: 0;
    }

    :host([theme~='icon-on-top']) {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: space-around;
        text-align: center;
        padding-bottom: 0.5rem;
        padding-top: 0.25rem;
    }

    :host([theme~='icon-on-top']) ::slotted(a) {
        flex-direction: column;
        align-items: center;
        margin-top: -0.25rem;
        padding-top: 0.25rem;
    }

    :host([theme~='icon-on-top']) ::slotted(vaadin-icon) {
        margin: 0;
    }

    /* Disabled */

    :host([disabled]) {
        pointer-events: none;
        opacity: 1;
        color: var(--lumo-disabled-text-color);
    }

    /* Focus-ring */

    :host([focus-ring]) {
        box-shadow: inset 0 0 0 var(--_focus-ring-width) var(--_focus-ring-color);
        border-radius: var(--lumo-border-radius-m);
    }

    /* RTL specific styles */

    :host([dir='rtl'])::before,
    :host([dir='rtl'])::after {
        left: auto;
        right: 50%;
        transform: translateX(50%) scale(0);
    }

    :host([dir='rtl'][selected]:not([orientation='vertical']))::before,
    :host([dir='rtl'][selected]:not([orientation='vertical']))::after {
        transform: translateX(50%) scale(1);
    }

    :host([dir='rtl']) ::slotted(vaadin-icon:first-child) {
        margin-right: 0;
    }

    :host([dir='rtl']) ::slotted(vaadin-icon:last-child) {
        margin-left: 0;
    }

    :host([orientation='vertical'][dir='rtl']) {
        transform-origin: 100% 50%;
    }

    :host([dir='rtl'][orientation='vertical'])::before,
    :host([dir='rtl'][orientation='vertical'])::after {
        left: auto;
        right: 0;
        border-radius: var(--lumo-border-radius-s) 0 0 var(--lumo-border-radius-s);
        transform-origin: 0 50%;
    }
`;