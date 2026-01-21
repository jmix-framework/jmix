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

export const jmixSideDialogOverlayStyles = css`
    :host {
        top: 0;
        bottom: 0;
        right: 0;
        left: 0;

        --_transition-duration: var(--jmix-side-dialog-transition-duration, 200ms);

        --_default-horizontal-size: auto;
        --_default-horizontal-max-size: 50%;
        --_default-horizontal-min-size: 16em;

        --_default-vertical-size: auto;
        --_default-vertical-max-size: 50%;
        --_default-vertical-min-size: 16em;

        --_horizontal-size: var(--jmix-side-dialog-horizontal-size, var(--_default-horizontal-size));
        --_horizontal-max-size: var(--jmix-side-dialog-horizontal-max-size, var(--_default-horizontal-max-size));
        --_horizontal-min-size: var(--jmix-side-dialog-horizontal-min-size, var(--_default-horizontal-min-size));

        --_vertical-size: var(--jmix-side-dialog-vertical-size, var(--_default-vertical-size));
        --_vertical-max-size: var(--jmix-side-dialog-vertical-max-size, var(--_default-vertical-max-size));
        --_vertical-min-size: var(--jmix-side-dialog-vertical-min-size, var(--_default-vertical-min-size));
    }

    :host [part='overlay'] {
        border-radius: 0;
    }

    /* Right - default */

    :host([side-dialog-placement='']),
    :host([side-dialog-placement='right']) {
        align-items: flex-end;
        margin-right: 0;
    }

    :host([side-dialog-placement=''][dir='rtl']),
    :host([side-dialog-placement='right'][dir='rtl']) {
        align-items: flex-start;
        margin-left: 0;
    }

    :host([opening][side-dialog-placement='']) [part='overlay'],
    :host([opening][side-dialog-placement='right']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-right-opening var(--_transition-duration) ease-in-out both;
    }

    :host([closing][side-dialog-placement='']) [part='overlay'],
    :host([closing][side-dialog-placement='right']) [part='overlay'] {
        transform: translateX(0);

        animation: jmix-side-dialog-right-closing var(--_transition-duration) ease-in-out both;
    }

    /* Left */

    :host([side-dialog-placement="left"]) {
        align-items: flex-start;
        margin-left: 0;
    }

    :host([side-dialog-placement='left'][dir='rtl']) {
        align-items: flex-end;
        margin-right: 0;
    }

    :host([opening][side-dialog-placement='left']) [part='overlay'] {
        transform: translateX(-100%);

        animation: jmix-side-dialog-left-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='left']) [part='overlay'] {
        transform: translateX(0);

        animation: jmix-side-dialog-left-closing var(--_transition-duration) ease-in-out forwards;
    }

    /* Top */

    :host([side-dialog-placement="top"]:not([fullscreen])) {
        margin-top: 0;
        align-self: flex-start;
        max-height: 100vh;
    }

    :host([opening][side-dialog-placement='top']) [part='overlay'] {
        transform: translateY(-100%);

        animation: jmix-side-dialog-top-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='top']) [part='overlay'] {
        transform: translateY(0);

        animation: jmix-side-dialog-top-closing var(--_transition-duration) ease-in-out forwards;
    }

    /* Bottom */

    :host([side-dialog-placement="bottom"]:not([fullscreen])) {
        margin-bottom: 0;
        align-self: flex-end;
        max-height: 100vh;
    }

    :host([opening][side-dialog-placement='bottom']) [part='overlay'] {
        transform: translateY(100%);

        animation: jmix-side-dialog-bottom-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='bottom']) [part='overlay'] {
        transform: translateY(0);

        animation: jmix-side-dialog-bottom-closing var(--_transition-duration) ease-in-out forwards;
    }

    /* Inline start */

    :host([side-dialog-placement="inline-start"]) {
        align-items: start;
        margin-inline-start: 0;
    }

    :host([opening][side-dialog-placement='inline-start']) [part='overlay'] {
        transform: translateX(-100%);

        animation: jmix-side-dialog-left-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([opening][side-dialog-placement='inline-start'][dir='rtl']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-inline-start-rtl-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='inline-start']) [part='overlay'] {
        transform: translateX(0%);

        animation: jmix-side-dialog-left-closing var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='inline-start'][dir='rtl']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-inline-start-rtl-closing var(--_transition-duration) ease-in-out forwards;
    }

    /* Inline end */

    :host([side-dialog-placement="inline-end"]) {
        align-items: end;
        margin-inline-end: 0;
    }

    :host([opening][side-dialog-placement='inline-end']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-right-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([opening][side-dialog-placement='inline-end'][dir='rtl']) [part='overlay'] {
        transform: translateX(-100%);

        animation: jmix-side-dialog-inline-end-rtl-opening var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='inline-end']) [part='overlay'] {
        transform: translateX(0%);

        animation: jmix-side-dialog-right-closing var(--_transition-duration) ease-in-out forwards;
    }

    :host([closing][side-dialog-placement='inline-end'][dir='rtl']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-inline-end-rtl-closing var(--_transition-duration) ease-in-out forwards;
    }

    :host([side-dialog-placement="left"]) [part='overlay'],
    :host([side-dialog-placement="right"]) [part='overlay'],
    :host([side-dialog-placement="inline-start"]) [part='overlay'],
    :host([side-dialog-placement="inline-end"]) [part='overlay'] {
        height: 100%;
        max-width: var(--_horizontal-max-size);
        min-width: var(--_horizontal-min-size);
        width: var(--_horizontal-size);
    }

    :host([side-dialog-placement="top"]) [part='overlay'],
    :host([side-dialog-placement="bottom"]) [part='overlay'] {
        height: var(--_vertical-size);
        max-height: var(--_vertical-max-size);
        min-height: var(--_vertical-min-size);
        width: 100%;
    }

    :host([fullscreen]) [part='overlay'] {
        width: 100%;
        height: 100%;
        max-height: 100%;
        max-width: 100%;
    }

    /* Right animation */

    @keyframes jmix-side-dialog-right-opening {
        0% {
            transform: translateX(100%);
        }
        100% {
            transform: translateX(0);
        }
    }

    @keyframes jmix-side-dialog-right-closing {
        0% {
            transform: translateX(0);
        }
        100% {
            transform: translateX(100%);
        }
    }

    /* Left animation */

    @keyframes jmix-side-dialog-left-opening {
        0% {
            transform: translateX(-100%);
        }
        100% {
            transform: translateX(0);
        }
    }

    @keyframes jmix-side-dialog-left-closing {
        0% {
            transform: translateX(0);
        }
        100% {
            transform: translateX(-100%);
        }
    }

    /* Inline start RTL animation */

    @keyframes jmix-side-dialog-inline-start-rtl-opening {
        0% {
            transform: translateX(100%);
        }
         100% {
            transform: translateX(0);
        }
    }

    @keyframes jmix-side-dialog-inline-start-rtl-closing {
        0% {
            transform: translateX(0%);
        }
        100% {
            transform: translateX(100%);
        }
    }

    /* Inline end RTL animation */

    @keyframes jmix-side-dialog-inline-end-rtl-opening {
        0% {
            transform: translateX(-100%);
        }
        100% {
            transform: translateX(0);
        }
    }

    @keyframes jmix-side-dialog-inline-end-rtl-closing {
         0% {
            transform: translateX(0%);
        }
         100% {
            transform: translateX(-100%);
        }
    }

    /* Top animation */

    @keyframes jmix-side-dialog-top-opening {
        0% {
            transform: translateY(-100%);
        }
        100% {
            transform: translateY(0);
        }
    }

    @keyframes jmix-side-dialog-top-closing {
        0% {
            transform: translateY(0);
        }
        100% {
            transform: translateY(-100%);
        }
    }

    /* Bottom animation */

    @keyframes jmix-side-dialog-bottom-opening {
        0% {
            transform: translateY(100%);
        }
        100% {
            transform: translateY(0);
        }
    }

    @keyframes jmix-side-dialog-bottom-closing {
        0% {
            transform: translateY(0);
        }
        100% {
            transform: translateY(100%);
        }
    }
`;