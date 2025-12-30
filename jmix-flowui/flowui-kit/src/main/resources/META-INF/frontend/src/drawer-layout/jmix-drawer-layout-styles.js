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

export const drawerLayoutStyles = css`
    :host {
        display: block;
        box-sizing: border-box;

        --jmix-drawer-layout-drawer-height: 12em;
        --jmix-drawer-layout-drawer-width: 20em;
        --jmix-drawer-layout-transition: 200ms;
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
            max-height var(--jmix-drawer-layout-transition),
            max-width var(--jmix-drawer-layout-transition);
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

    [part='drawer'] {
        display: flex;
        flex-direction: column;
        height: 100%;
        width: 100%;
        outline: none;
        position: absolute;
        box-sizing: border-box;
        transition:
              transform var(--jmix-drawer-layout-transition),
              visibility var(--jmix-drawer-layout-transition);
    }

    [part='drawer'][hidden] {
        visibility: hidden;
    }

    [part='drawerContent'] {
        box-sizing: border-box;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        width: 100%;
    }

    :host([drawer-opened]) [part='drawer'] {
        visibility: visible;
        touch-action: manipulation;
    }

    :host([drawer-opened][fullscreen]) [part='drawer'] {
        visibility: hidden;
    }

    :host([drawer-opened][modal][theme~='dimmed-curtain']) [part='modalityCurtain'] {
        animation: var(--jmix-drawer-layout-transition) jmix-modality-curtain-dimmed-enter both;
        will-change: opacity;
    }

    :host(:not([drawer-opened])[modal][theme~='dimmed-curtain']) [part='modalityCurtain']:not([hidden]) {
        animation: var(--jmix-drawer-layout-transition) jmix-modality-curtain-dimmed-out both;
        will-change: opacity;
    }

    /* Drawer placement */

    :host([drawer-placement='left']) [part='drawer'] {
        transform: translateX(-100%);
        left: 0;
        width: var(--jmix-drawer-layout-drawer-width);
    }

    :host([drawer-placement='']) [part='drawer'],
    :host([drawer-placement='right']) [part='drawer'] {
        transform: translateX(100%);
        right: 0;
        width: var(--jmix-drawer-layout-drawer-width);
    }

    :host([drawer-placement='top']) [part='drawer'] {
        transform: translateY(-100%);
        top: 0;
        height: var(--jmix-drawer-layout-drawer-height);
    }

    :host([drawer-placement='bottom']) [part='drawer'] {
        transform: translateY(100%);
        bottom: 0;
        height: var(--jmix-drawer-layout-drawer-height);
    }

    :host([drawer-placement='inline-start']) [part='drawer'] {
        transform: translateX(-100%);
        inset-inline-start: 0;
        width: var(--jmix-drawer-layout-drawer-width);
    }

    :host([drawer-placement='inline-start'][dir='rtl']) [part='drawer'] {
        transform: translateX(100%);
    }

    :host([drawer-placement='inline-end']) [part='drawer'] {
        transform: translateX(100%);
        inset-inline-end: 0;
        width: var(--jmix-drawer-layout-drawer-width);
    }

    :host([drawer-placement='inline-end'][dir='rtl']) [part='drawer'] {
        transform: translateX(-100%);
    }

    /* Animation */

    :host([drawer-opened][drawer-placement='left']) [part='drawer'] {
        transform: translateX(0%);
    }

    :host([drawer-placement='left'][drawer-mode='push']) [part='content'] {
        /*
         * Since we change content max-width, the content shrinks itself to start.
         * When LEFT mode is used, we need to align content to end for smooth animation.
         */
        margin-left: auto;
    }

    :host([drawer-opened][drawer-placement='left'][drawer-mode='push']) [part='content'] {
        max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
    }

    :host([drawer-opened][drawer-placement='inline-start']) [part='drawer'] {
        transform: translateX(0%);
    }

    :host([drawer-placement='inline-start'][drawer-mode='push']) [part='content'] {
        margin-inline-start: auto;
    }

    :host([drawer-opened][drawer-placement='inline-start'][drawer-mode='push']) [part='content'] {
        max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
    }

    :host([drawer-opened][drawer-placement='inline-end']) [part='drawer'] {
        transform: translateX(0%);
    }

    :host([drawer-placement='inline-end'][drawer-mode='push']) [part='content'] {
        margin-inline-end: auto;
    }

    :host([drawer-opened][drawer-placement='inline-end'][drawer-mode='push']) [part='content'] {
        max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
    }

    :host([drawer-opened][drawer-placement='']) [part='drawer'],
    :host([drawer-opened][drawer-placement='right']) [part='drawer'] {
        transform: translateX(0%);
    }

    :host([drawer-placement='right'][drawer-mode='push']) [part='content'],
    :host([drawer-placement='left'][drawer-mode='push']) [part='content'],
    :host([drawer-placement='inline-start'][drawer-mode='push']) [part='content'],
    :host([drawer-placement='inline-end'][drawer-mode='push']) [part='content'] {
        max-width: 100%;
    }

    :host([drawer-opened][drawer-placement='right'][drawer-mode='push']) [part='content'] {
        max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
    }

    :host([drawer-placement='top'][drawer-mode='push']) [part='content'],
    :host([drawer-placement='bottom'][drawer-mode='push']) [part='content'] {
        max-height: 100%;
    }

    :host([drawer-placement='top'][drawer-mode='push']) [part='content'] {
        /*
         * Since we change content max-height, the content shrinks itself to top.
         * When TOP mode is used, we need to align content to bottom for smooth animation.
         */
        align-self: end;
    }

    :host([drawer-opened][drawer-placement='top'][drawer-mode='push']) [part='content'] {
        max-height: calc(100% - var(--jmix-drawer-layout-drawer-height));
    }

    :host([drawer-opened][drawer-placement='bottom'][drawer-mode='push']) [part='content'] {
        max-height: calc(100% - var(--jmix-drawer-layout-drawer-height));
    }

    :host([drawer-opened][drawer-placement='top']) [part='drawer'] {
        height: var(--jmix-drawer-layout-drawer-height);
        width: 100%;
        transform: translateY(0%);
    }

    :host([drawer-opened][drawer-placement='bottom']) [part='drawer'] {
        height: var(--jmix-drawer-layout-drawer-height);
        width: 100%;
        transform: translateY(0%);
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

    @keyframes jmix-modality-curtain-blur-enter {
        0% {
            backdrop-filter: blur(0);
        }
        100% {
            backdrop-filter: blur(1px);
        }
    }

    @keyframes jmix-modality-curtain-blur-out {
        0% {
            backdrop-filter: blur(1px);
        }
        100% {
            backdrop-filter: blur(0);
        }
    }
`;
