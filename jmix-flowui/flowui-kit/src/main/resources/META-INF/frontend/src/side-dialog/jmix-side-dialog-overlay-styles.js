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

        --jmix-side-dialog-transition-duration: 200ms;
        --jmix-side-dialog-horizontal-size: 20em;
        --jmix-side-dialog-vertical-size: 18em;
    }

    :host [part='overlay'] {
        border-radius: 0;
    }

    /* RIGHT - default */

    :host([drawer-placement='']),
    :host([drawer-placement='right']) {
        align-items: flex-end;
        margin-right: 0;
    }

    :host([drawer-placement=''][dir='rtl']),
    :host([drawer-placement='right'][dir='rtl']) {
        align-items: flex-start;
        margin-left: 0;
    }

    :host([opening][drawer-placement='']) [part='overlay'],
    :host([opening][drawer-placement='right']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-right-opening var(--jmix-side-dialog-transition-duration) ease-in-out both;
    }

    :host([closing][drawer-placement='']) [part='overlay'],
    :host([closing][drawer-placement='right']) [part='overlay'] {
        transform: translateX(0);

        animation: jmix-side-dialog-right-closing var(--jmix-side-dialog-transition-duration) ease-in-out both;
    }

    /* LEFT */

    :host([drawer-placement="left"]) {
        align-items: flex-start;
        margin-left: 0;
    }

    :host([drawer-placement='left'][dir='rtl']) {
        align-items: flex-end;
        margin-right: 0;
    }

    :host([opening][drawer-placement='left']) [part='overlay'] {
        transform: translateX(-100%);

        animation: jmix-side-dialog-left-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='left']) [part='overlay'] {
        transform: translateX(0);

        animation: jmix-side-dialog-left-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    /* TOP */

    :host([drawer-placement="top"]:not([fullscreen])) {
        margin-top: 0;
        align-self: flex-start;
    }

    :host([opening][drawer-placement='top']) [part='overlay'] {
        transform: translateY(-100%);

        animation: jmix-side-dialog-top-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='top']) [part='overlay'] {
        transform: translateY(0);

        animation: jmix-side-dialog-top-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    /* Bottom */

    :host([drawer-placement="bottom"]:not([fullscreen])) {
        margin-bottom: 0;
        align-self: flex-end;
    }

    :host([opening][drawer-placement='bottom']) [part='overlay'] {
        transform: translateY(100%);

        animation: jmix-side-dialog-bottom-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='bottom']) [part='overlay'] {
        transform: translateY(0);

        animation: jmix-side-dialog-bottom-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    /* Inline start */

    :host([drawer-placement="inline-start"]) {
        align-items: start;
        margin-inline-start: 0;
    }

    :host([opening][drawer-placement='inline-start']) [part='overlay'] {
        transform: translateX(-100%);

        animation: jmix-side-dialog-left-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([opening][drawer-placement='inline-start'][dir='rtl']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-inline-start-rtl-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='inline-start']) [part='overlay'] {
        transform: translateX(0%);

        animation: jmix-side-dialog-left-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='inline-start'][dir='rtl']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-inline-start-rtl-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    /* Inline end */

    :host([drawer-placement="inline-end"]) {
        align-items: end;
        margin-inline-end: 0;
    }

    :host([opening][drawer-placement='inline-end']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-right-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([opening][drawer-placement='inline-end'][dir='rtl']) [part='overlay'] {
        transform: translateX(-100%);

        animation: jmix-side-dialog-inline-end-rtl-opening var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='inline-end']) [part='overlay'] {
        transform: translateX(0%);

        animation: jmix-side-dialog-right-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([closing][drawer-placement='inline-end'][dir='rtl']) [part='overlay'] {
        transform: translateX(100%);

        animation: jmix-side-dialog-inline-end-rtl-closing var(--jmix-side-dialog-transition-duration) ease-in-out forwards;
    }

    :host([drawer-placement="left"]) [part='overlay'],
    :host([drawer-placement="right"]) [part='overlay'],
    :host([drawer-placement="inline-start"]) [part='overlay'],
    :host([drawer-placement="inline-end"]) [part='overlay'] {
        height: 100%;
        width: var(--jmix-side-dialog-horizontal-size);
    }

    :host([drawer-placement="top"]) [part='overlay'],
    :host([drawer-placement="bottom"]) [part='overlay'] {
        height: var(--jmix-side-dialog-vertical-size);
        width: 100%;
    }

    :host([fullscreen]) [part='overlay'] {
        width: 100%;
        height: 100%;
    }

    /* Right */

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

    /* Left */

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

    /* Inline start RTL */

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

    /* Inline end RTL */

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

    /* Top */

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

    /* Bottom */

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