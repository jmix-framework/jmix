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

export const sidePanelLayoutStyles = css`
    :host {
        display: block;
        box-sizing: border-box;

        --jmix-side-panel-vertical-size: auto;
        --jmix-side-panel-vertical-max-size: 14em;
        --jmix-side-panel-vertical-min-size: 10em;

        --jmix-side-panel-horizontal-size: auto;
        --jmix-side-panel-horizontal-max-size: 26em;
        --jmix-side-panel-horizontal-min-size: 14em;

        --jmix-side-panel-transition: 200ms;
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
            max-height var(--jmix-side-panel-transition),
            max-width var(--jmix-side-panel-transition);
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
            transform var(--jmix-side-panel-transition),
            visibility 0s linear var(--jmix-side-panel-transition);
        visibility: hidden;
    }

    [part='sidePanelContent'] {
        box-sizing: border-box;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        width: 100%;
    }

    :host([side-panel-opened]) [part='sidePanel'] {
        visibility: visible;
        touch-action: manipulation;
        visibility: visible;
        touch-action: manipulation;
        transition:
            transform var(--jmix-side-panel-transition),
            visibility 0s;
    }

    :host([side-panel-opened][modal]) [part='modalityCurtain'] {
        animation: var(--jmix-side-panel-transition) jmix-modality-curtain-dimmed-enter both;
        will-change: opacity;
    }

    :host(:not([side-panel-opened])[modal]) [part='modalityCurtain']:not([hidden]) {
        animation: var(--jmix-side-panel-transition) jmix-modality-curtain-dimmed-out both;
        will-change: opacity;
    }

    /* SidePanel placement */

    :host([side-panel-placement='']) [part='sidePanel'],
    :host([side-panel-placement='left']) [part='sidePanel'],
    :host([side-panel-placement='right']) [part='sidePanel'],
    :host([side-panel-placement='inline-start']) [part='sidePanel'],
    :host([side-panel-placement='inline-end']) [part='sidePanel'] {
        width: var(--jmix-side-panel-horizontal-size);
        max-width: var(--jmix-side-panel-horizontal-max-size);
        min-width: var(--jmix-side-panel-horizontal-min-size);
    }

    :host([side-panel-placement='bottom']) [part='sidePanel'],
    :host([side-panel-placement='top']) [part='sidePanel'] {
        height: var(--jmix-side-panel-vertical-size);
        max-height: var(--jmix-side-panel-vertical-max-size);
        min-height: var(--jmix-side-panel-vertical-min-size);
    }

    :host([side-panel-placement='top']) ::slotted([slot='sidePanelContentSlot']),
    :host([side-panel-placement='bottom']) ::slotted([slot='sidePanelContentSlot']) {
        max-height: var(--jmix-side-panel-vertical-max-size);
    }

    :host([side-panel-placement='left']) [part='sidePanel'] {
        transform: translateX(-100%);
        left: 0;
    }

    :host([side-panel-placement='']) [part='sidePanel'],
    :host([side-panel-placement='right']) [part='sidePanel'] {
        transform: translateX(100%);
        right: 0;
    }

    :host([side-panel-placement='top']) [part='sidePanel'] {
        transform: translateY(-100%);
        top: 0;
    }

    :host([side-panel-placement='bottom']) [part='sidePanel'] {
        transform: translateY(100%);
        bottom: 0;
    }

    :host([side-panel-placement='inline-start']) [part='sidePanel'] {
        transform: translateX(-100%);
        inset-inline-start: 0;
    }

    :host([side-panel-placement='inline-start'][dir='rtl']) [part='sidePanel'] {
        transform: translateX(100%);
    }

    :host([side-panel-placement='inline-end']) [part='sidePanel'] {
        transform: translateX(100%);
        inset-inline-end: 0;
    }

    :host([side-panel-placement='inline-end'][dir='rtl']) [part='sidePanel'] {
        transform: translateX(-100%);
    }

    /* Animation */

    :host([side-panel-opened][side-panel-placement='']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-placement='right']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-placement='left']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-placement='inline-start']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-placement='inline-end']) [part='sidePanel'] {
        transform: translateX(0%);
    }

    :host([side-panel-opened][side-panel-placement='top']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-placement='bottom']) [part='sidePanel'] {
        width: 100%;
        transform: translateY(0%);
    }

    :host([side-panel-placement='left'][side-panel-mode='push']) [part='content'] {
        /*
         * Since we change content max-width, the content shrinks itself to start.
         * When LEFT mode is used, we need to align content to end for smooth animation.
         */
        margin-left: auto;
    }

    :host([side-panel-placement='inline-start'][side-panel-mode='push']) [part='content'] {
        margin-inline-start: auto;
    }

    :host([side-panel-placement='inline-end'][side-panel-mode='push']) [part='content'] {
        margin-inline-end: auto;
    }

    :host([side-panel-placement='top'][side-panel-mode='push']) [part='content'] {
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
            --jmix-side-panel-transition: none !important;
        }
    }
`;
