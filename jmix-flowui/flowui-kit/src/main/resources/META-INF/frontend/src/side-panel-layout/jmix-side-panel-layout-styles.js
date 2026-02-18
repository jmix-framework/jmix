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

import { css } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export const sidePanelLayoutStyles = css`
    :host {
        display: block;
        box-sizing: border-box;

        --_transition-duration: var(--jmix-side-panel-layout-transition-duration, 200ms);

        --_horizontal-size: var(--jmix-side-panel-layout-horizontal-size, auto);
        --_horizontal-max-size: var(--jmix-side-panel-layout-horizontal-max-size, 50%);
        --_horizontal-min-size: var(--jmix-side-panel-layout-horizontal-min-size, 14em);

        --_vertical-size: var(--jmix-side-panel-layout-vertical-size, auto);
        --_vertical-max-size: var(--jmix-side-panel-layout-vertical-max-size, 50%);
        --_vertical-min-size: var(--jmix-side-panel-layout-vertical-min-size, 10em);
    }

    [part='layout'] {
        display: flex;
        height: 100%;
        position: relative;
        overflow: hidden;
    }

    :host,
    [part='content'] {
        display: flex;
        flex-direction: column;
    }

    [part='content'] {
        flex: 1 1 100%;
        height: 100%;
        box-sizing: border-box;
        transition:
            max-height var(--_transition-duration),
            max-width var(--_transition-duration);
    }

    [part='modalityCurtain'] {
        position: absolute;
        height: 100%;
        width: 100%;
        touch-action: manipulation;
    }

    [part='modalityCurtain'][hidden] {
        display: none;
    }

    [part='sidePanel'] {
        display: flex;
        flex-direction: column;
        height: 100%;
        width: 100%;
        outline: none;
        position: absolute;
        box-sizing: border-box;
        transition:
            transform var(--_transition-duration),
            visibility 0s linear var(--_transition-duration);
        visibility: hidden;
    }

    [part='sidePanelContent'] {
        box-sizing: border-box;
        display: flex;
        height: 100%;
        width: 100%;
        min-height: 0;
        flex: 1 1 auto;
        flex-direction: column;
        align-items: flex-start;
        overflow: hidden;
    }

    :host([side-panel-opened]) [part='sidePanel'] {
        visibility: visible;
        touch-action: manipulation;
        transition:
            transform var(--_transition-duration),
            visibility 0s;
    }

    :host([side-panel-opened][modal]) [part='modalityCurtain'] {
        animation: var(--_transition-duration) jmix-modality-curtain-dimmed-enter both;
        will-change: opacity;
    }

    :host(:not([side-panel-opened])[modal]) [part='modalityCurtain']:not([hidden]) {
        animation: var(--_transition-duration) jmix-modality-curtain-dimmed-out both;
        will-change: opacity;
    }

    /* SidePanel position */

    :host([side-panel-position='']) [part='sidePanel'],
    :host([side-panel-position='left']) [part='sidePanel'],
    :host([side-panel-position='right']) [part='sidePanel'],
    :host([side-panel-position='inline-start']) [part='sidePanel'],
    :host([side-panel-position='inline-end']) [part='sidePanel'] {
        width: var(--_horizontal-size);
        max-width: var(--_horizontal-max-size);
        min-width: var(--_horizontal-min-size);
    }

    :host([side-panel-position='bottom']) [part='sidePanel'],
    :host([side-panel-position='top']) [part='sidePanel'] {
        height: var(--_vertical-size);
        max-height: var(--_vertical-max-size);
        min-height: var(--_vertical-min-size);
    }

    :host([side-panel-position='top']) ::slotted([slot='sidePanelContentSlot']),
    :host([side-panel-position='bottom']) ::slotted([slot='sidePanelContentSlot']) {
        flex: 1 1 auto;
        min-height: 0;
    }

    :host([side-panel-position='left']) [part='sidePanel'] {
        transform: translateX(-100%);
        left: 0;
    }

    :host([side-panel-position='']) [part='sidePanel'],
    :host([side-panel-position='right']) [part='sidePanel'] {
        transform: translateX(100%);
        right: 0;
    }

    :host([side-panel-position='top']) [part='sidePanel'] {
        transform: translateY(-100%);
        top: 0;
    }

    :host([side-panel-position='bottom']) [part='sidePanel'] {
        transform: translateY(100%);
        bottom: 0;
    }

    :host([side-panel-position='inline-start']) [part='sidePanel'] {
        transform: translateX(-100%);
        inset-inline-start: 0;
    }

    :host([side-panel-position='inline-start'][dir='rtl']) [part='sidePanel'] {
        transform: translateX(100%);
    }

    :host([side-panel-position='inline-end']) [part='sidePanel'] {
        transform: translateX(100%);
        inset-inline-end: 0;
    }

    :host([side-panel-position='inline-end'][dir='rtl']) [part='sidePanel'] {
        transform: translateX(-100%);
    }

    /* Animation */

    :host([side-panel-opened][side-panel-position='']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-position='right']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-position='left']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-position='inline-start']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-position='inline-end']) [part='sidePanel'] {
        transform: translateX(0%);
    }

    :host([side-panel-opened][side-panel-position='top']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-position='bottom']) [part='sidePanel'] {
        width: 100%;
        transform: translateY(0%);
    }

    :host([side-panel-position='left'][side-panel-mode='push']) [part='content'] {
        /*
         * Since we change content max-width, the content shrinks itself to start.
         * When LEFT mode is used, we need to align content to end for smooth animation.
         */
        margin-left: auto;
    }

    :host([side-panel-position='inline-start'][side-panel-mode='push']) [part='content'] {
        margin-inline-start: auto;
    }

    :host([side-panel-position='inline-end'][side-panel-mode='push']) [part='content'] {
        margin-inline-end: auto;
    }

    :host([side-panel-position='top'][side-panel-mode='push']) [part='content'] {
        /*
         * Since we change content max-height, the content shrinks itself to top.
         * When TOP mode is used, we need to align content to bottom for smooth animation.
         */
        align-self: end;
    }

    @keyframes jmix-modality-curtain-dimmed-enter {
        0% {
            opacity: 0;
        }
    }

    @keyframes jmix-modality-curtain-dimmed-out {
        100% {
            opacity: 0;
        }
    }

    @media (prefers-reduced-motion: reduce) {

        :host {
            --_transition-duration: none !important;
        }
    }
`;
