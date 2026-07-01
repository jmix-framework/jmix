/*
 * Copyright 2024 Haulmont.
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
import {field} from '@vaadin/field-base/src/styles/field-base-styles.js';
import {addGlobalStyles} from '@vaadin/component-base/src/styles/add-global-styles.js';
import {css} from 'lit';

const jmixRichTextEditor = css`
    :host {
        display: flex;
        box-sizing: content-box;

        font-family: inherit;
        font-size: inherit;
        line-height: inherit;
        -webkit-text-size-adjust: 100%;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;

        padding: var(--vaadin-gap-xs) 0;

        min-width: 32em;
        min-height: 18em;
    }

    :host:before {
        content: none;
    }

    :host([hidden]) {
        display: none !important;
    }

    :host([disabled]) {
        pointer-events: none;
        opacity: 0.5;
        -webkit-user-select: none;
        -moz-user-select: none;
        user-select: none;
    }

    .jmix-rich-text-editor-wrapper {
        display: flex;
        flex-direction: column;
        min-height: 100%;
        max-height: 100%;
        flex: auto;
    }

    [part="editor"] {
        display: flex;
        flex-direction: column;
        flex: auto;
        height: 100%;
        overflow-y: auto;
    }
`;

addGlobalStyles(
    'jmix-rich-text-editor',
    css`
        /*
        Quill core styles.
        CSS selectors removed: margin & padding reset, check list, indentation, video, colors, ordered & unordered list, h1-6, anchor
        */

        jmix-rich-text-editor .ql-clipboard {
            left: -100000px;
            height: 1px;
            overflow-y: hidden;
            position: absolute;
            top: 50%;
        }

        jmix-rich-text-editor .ql-clipboard p {
            margin: 0;
            padding: 0;
        }

        jmix-rich-text-editor .ql-editor {
            box-sizing: border-box;
            line-height: 1.42;
            height: 100%;
            outline: none;
            overflow-y: auto;
            padding: 0.75em 1em;
            -moz-tab-size: 4;
            tab-size: 4;
            text-align: left;
            white-space: pre-wrap;
            word-wrap: break-word;
            flex: 1;
        }

        jmix-rich-text-editor .ql-editor > * {
            cursor: text;
        }

        jmix-rich-text-editor .ql-align-left {
            text-align: left;
        }

        jmix-rich-text-editor .ql-direction-rtl {
            direction: rtl;
            text-align: inherit;
        }

        jmix-rich-text-editor .ql-align-center {
            text-align: center;
        }

        jmix-rich-text-editor .ql-align-justify {
            text-align: justify;
        }

        jmix-rich-text-editor .ql-align-right {
            text-align: right;
        }


        /* Base */

        jmix-rich-text-editor .jmix-rich-text-editor-container {
            display: flex;
            flex-direction: column;
            min-height: inherit;
            max-height: inherit;
            flex: auto;
            overflow: hidden;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-container {
            border: 1px solid var(--vaadin-input-field-border-color, var(--vaadin-border-color));
            border-radius: var(--vaadin-radius-m);
        }

        jmix-rich-text-editor .jmix-rich-text-editor-container:has([class*='jmix-rich-text-editor-content']:focus-within),
        jmix-rich-text-editor .jmix-rich-text-editor-container:has([class*='toolbar-button']:active) {
            outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
            outline-offset: calc(var(--vaadin-focus-ring-width, 1px) * -1);
        }


        jmix-rich-text-editor[readonly] .jmix-rich-text-editor-container {
            border-style: dashed;
        }

        jmix-rich-text-editor[readonly] .jmix-rich-text-editor-container:has([class*='jmix-rich-text-editor-content']:focus-within) {
            outline-style: dashed;
            --vaadin-input-field-border-color: transparent;
        }

        jmix-rich-text-editor[disabled] .jmix-rich-text-editor-container:has([class*='jmix-rich-text-editor-content']:focus-within) {
            --vaadin-input-field-border-color: transparent;
        }

        vaadin-form-layout jmix-rich-text-editor {
            /* Workaround for odd margin inside vaadin-form-layout */
            align-self: flex-start;
        }


        /* Toolbar */

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar {
            display: flex;
            flex-wrap: wrap;
            flex-shrink: 0;
            
            gap: var(--vaadin-gap-s);
            padding: calc(var(--vaadin-gap-s) - 1px) var(--vaadin-gap-xs);

            border-bottom: 1px solid var(--vaadin-border-color-secondary);
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-group'] {
            display: flex;
            gap: 1px;
            align-items: center;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button'] {
            font: inherit;
            line-height: 1;
            text-transform: none;
            background: transparent;
            position: relative;
            
            border: 1px solid transparent;
            border-radius: var(--vaadin-radius-m);
            color: var(--vaadin-text-color-secondary);
            cursor: var(--vaadin-clickable-cursor);

            transition: color 80ms, background-color 80ms, scale 0.18s;
            outline-offset: calc(var(--vaadin-focus-ring-width) * -1);

            padding: var(--vaadin-padding-s);
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button']::before {
            display: block;
            font-family: var(--jmix-rte-icons-font-family, var(--jmix-font-icon-font-family));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button']:hover {
            outline: none;
            background-color: var(--vaadin-background-container);
            color: var(--vaadin-text-color);
            box-shadow: none;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button']:focus {
            outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
        }

        @media (forced-colors: active) {
            jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button']:focus,
            jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button']:hover {
                outline: 1px solid !important;
            }

            jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button'].ql-active {
                outline: 2px solid;
                outline-offset: -1px;
            }
        }

        @media (hover: none) {
            jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button']:hover {
                background-color: transparent;
            }
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button'].ql-active {
            background-color: var(--vaadin-background-container-strong);
            color: var(--vaadin-text-color);
            border-color: var(--vaadin-border-color-secondary);
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-bold']::before {
            content: var(--jmix-rte-icons-bold, var(--jmix-font-icon-bold));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-italic']::before {
            content: var(--jmix-rte-icons-italic, var(--jmix-font-icon-italic));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-underline']::before {
            content: var(--jmix-rte-icons-underline, var(--jmix-font-icon-underline));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-strike']::before {
            content: var(--jmix-rte-icons-strikethrough, var(--jmix-font-icon-strikethrough));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-h1']::before {
            content: 'H1';
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-h2']::before {
            content: 'H2';
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-h3']::before {
            content: 'H3';
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-h1']::before,
        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-h2']::before,
        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-h3']::before {
            letter-spacing: -0.05em;
            font-weight: 700;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-subscript']::before {
            content: var(--jmix-rte-icons-subscript, var(--jmix-font-icon-subscript));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-superscript']::before {
            content: var(--jmix-rte-icons-superscript, var(--jmix-font-icon-superscript));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-blockquote']::before {
            content: var(--jmix-rte-icons-quote-right, var(--jmix-font-icon-quote-right));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-code-block']::before {
            content: var(--jmix-rte-icons-angle-left, var(--jmix-font-icon-angle-left)) var(--jmix-rte-icons-angle-right, var(--jmix-font-icon-angle-right));
            letter-spacing: -0.4em;
            margin-left: -0.2em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-list-ordered']::before {
            content: var(--jmix-rte-icons-list-ordered, var(--jmix-font-icon-list-ol));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-list-bullet']::before {
            content: var(--jmix-rte-icons-list-bullet, var(--jmix-font-icon-list-ul));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-align-start']::before {
            content: var(--jmix-rte-icons-align-start, var(--jmix-font-icon-align-left));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-align-center']::before {
            content: var(--jmix-rte-icons-align-center, var(--jmix-font-icon-align-center));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-align-end']::before {
            content: var(--jmix-rte-icons-align-end, var(--jmix-font-icon-align-right));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-align-justify']::before {
            content: var(--jmix-rte-icons-align-justify, var(--jmix-font-icon-align-justify));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-image']::before {
            content: var(--jmix-rte-icons-image, var(--jmix-font-icon-picture));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-link']::before {
            content: var(--jmix-rte-icons-link, var(--jmix-font-icon-link));
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar [class*='toolbar-button-clean']::before {
            content: var(--jmix-rte-icons-clean, var(--jmix-font-icon-eraser));
        }

        /* State */

        jmix-rich-text-editor[readonly] .jmix-rich-text-editor-toolbar {
            display: none;
        }

        jmix-rich-text-editor[disabled] [class*='toolbar-button'] {
            background-color: transparent;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-toolbar input[type='file'] {
            display: none;
        }


        /* Content */

        jmix-rich-text-editor .jmix-rich-text-editor-content {
            box-sizing: border-box;
            position: relative;
            flex: auto;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content > .ql-editor {
            padding: 0 var(--vaadin-gap-m);
            line-height: inherit;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content :where(h1, h2, h3, h4, h5, h6) {
            margin-top: 1.25em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content h1 {
            margin-bottom: 0.75em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content :where(h2, h3, h4) {
            margin-bottom: 0.5em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content h5 {
            margin-bottom: 0.25em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content blockquote {
            padding-left: 1em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content pre {
            white-space: pre-wrap;
            margin-bottom: 0.3125em;
            margin-top: 0.3125em;
            padding: 0.3125em 0.625em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content code {
            font-size: 85%;
            padding: 0.125em 0.25em;
        }

        jmix-rich-text-editor .jmix-rich-text-editor-content img {
            max-width: 100%;
        }

        /* RTL specific styles */

        jmix-rich-text-editor[dir='rtl'] .ql-editor {
            direction: rtl;
            text-align: right;
        }

        jmix-rich-text-editor[dir='rtl'] .jmix-rich-text-editor-toolbar [class*='toolbar-button-align-start'] {
            rotate: 180deg;
        }

        jmix-rich-text-editor[dir='rtl'] .jmix-rich-text-editor-toolbar [class*='toolbar-button-align-end'] {
            rotate: 180deg;
        }
    `,
);

export const jmixRichTextEditorStyles = [field, jmixRichTextEditor];