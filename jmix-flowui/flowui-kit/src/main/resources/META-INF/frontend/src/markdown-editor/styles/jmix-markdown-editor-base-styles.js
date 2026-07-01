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
import { css } from 'lit';
import { icons } from './jmix-markdown-editor-icons.js';

const markdownEditorBaseStyles = css`
    .jmix-markdown-editor-container {
        --_field-radius: var(--vaadin-input-field-border-radius, var(--vaadin-radius-m));
        --_field-border-color: var(--vaadin-input-field-border-color, var(--vaadin-border-color));
        --_field-border-width: var(--vaadin-input-field-border-width, 1px);
        
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        border-radius: var(--_field-radius);
        border: var(--_field-border-width) solid var(--_field-border-color);
        background: var(--vaadin-input-field-background, var(--vaadin-background-color));
        overflow: hidden;
        
        width: var(--jmix-markdown-editor-default-width, 600px);
        max-width: 100%;
        min-width: 100%;
    }

    :host([invalid]) {
        --vaadin-input-field-border-color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
    }

    .jmix-markdown-editor-container:focus-within {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
        outline-offset: calc(var(--vaadin-input-field-border-width, 1px) * -1);
    }

    :host([readonly]) .jmix-markdown-editor-container {
        border-style: dashed;
    }

    :host([readonly]) .jmix-markdown-editor-container:focus-within {
        outline-style: dashed;
    }

    :host([disabled]) .jmix-markdown-editor-container {
        --vaadin-input-field-background: var(
                --vaadin-input-field-disabled-background,
                var(--vaadin-background-container-strong)
        );
        --vaadin-input-field-border-color: transparent;
        cursor: var(--vaadin-disabled-cursor);
    }

    [part='header'] {
        display: flex;
        align-items: center;
        background: var(--jmix-markdown-editor-header-background, var(--vaadin-background-container));
        border-bottom: var(--vaadin-input-field-border-width, 1px) solid var(--vaadin-border-color-secondary);
        gap: var(--vaadin-gap-xs);
    }

    [part='tabs'] {
        display: flex;
        flex-shrink: 0;
        gap: 1px;
    }

    .tab {
        padding: var(--jmix-markdown-editor-tab-padding, var(--vaadin-padding-m) var(--vaadin-padding-l));
        cursor: var(--vaadin-clickable-cursor);

        border: var(--_field-border-width) solid transparent;
        border-radius: var(--_field-radius);
        border-bottom-left-radius: 0;
        border-bottom-right-radius: 0;
        margin: calc(var(--_field-border-width) * -1);

        background: var(--jmix-markdown-editor-tab-background, transparent);
        color: var(--jmix-markdown-editor-tab-text-color, var(--vaadin-text-color-secondary));

        font: inherit;
        white-space: nowrap;
        -webkit-tap-highlight-color: transparent;
    }

    .tab:hover:not(:disabled) {
        --jmix-markdown-editor-tab-text-color: var(--vaadin-text-color);
    }

    .tab:focus-visible {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
        outline-offset: calc(var(--vaadin-focus-ring-width) * -3);
    }

    .tab:disabled {
        --jmix-markdown-editor-tab-text-color: var(--vaadin-text-color-disabled);
        cursor: var(--vaadin-disabled-cursor);
    }

    .tab[aria-selected='true']:not([disabled]) {
        --jmix-markdown-editor-tab-text-color: var(--vaadin-text-color);
        --jmix-markdown-editor-tab-background: var(--vaadin-input-field-background, var(--vaadin-background-color));
        font-weight: 500;

        border-color: var(--vaadin-border-color-secondary);
        border-bottom-color: transparent;
    }

    .divider {
        width: 1px;
        height: 1lh;
        background: var(--vaadin-border-color-secondary);
        flex-shrink: 0;
    }

    [part='toolbar'] {
        display: flex;
        align-items: center;
        flex: 1;
        gap: 1px;
        overflow: hidden;
        position: relative;

        padding-inline-end: var(--vaadin-padding-xs);
    }

    :host([theme~='toolbar-align-start']) [part='toolbar'] {
        justify-content: flex-start;
    }

    :host([theme~='toolbar-align-center']) [part='toolbar'] {
        justify-content: center;
    }

    :host([theme~='toolbar-align-end']) [part='toolbar'] {
        justify-content: flex-end;
    }

    :host([theme~='toolbar-align-center']) [part='divider'],
    :host([theme~='toolbar-align-end']) [part='divider'] {
        display: none;
    }

    [part~='toolbar-button'],
    .toolbar-overflow {
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        padding: var(--jmix-markdown-editor-toolbar-button-padding, var(--vaadin-padding-xs));
        border: var(--jmix-markdown-editor-toolbar-button-border-width, 1px) solid var(--jmix-markdown-editor-toolbar-button-border-color, transparent);
        background: var(--jmix-markdown-editor-toolbar-button-background, transparent);
        cursor: var(--vaadin-clickable-cursor);
        border-radius: var(--jmix-markdown-editor-toolbar-button-border-radius, var(--vaadin-radius-m));
        color: var(--jmix-markdown-editor-toolbar-button-text-color, var(--vaadin-text-color-secondary));
        font: inherit;
        flex-shrink: 0;
        -webkit-tap-highlight-color: transparent;
    }

    [part~='toolbar-button']:focus-visible,
    .toolbar-overflow:focus-visible {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
        outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
    }

    [part~='toolbar-button']:hover:not(:disabled):not([aria-pressed='true']),
    .toolbar-overflow:hover:not(:disabled):not([aria-pressed='true']) {
        --jmix-markdown-editor-toolbar-button-background: var(--vaadin-background-container-strong);
        --jmix-markdown-editor-toolbar-button-text-color: var(--vaadin-text-color);
    }

    [part~='toolbar-button']:disabled,
    .toolbar-overflow:disabled {
        --jmix-markdown-editor-toolbar-button-text-color: var(--vaadin-text-color-disabled);
        cursor: var(--vaadin-disabled-cursor);
    }

    [part~='toolbar-button'][hidden] {
        display: none;
    }

    [part~='toolbar-button'][aria-pressed='true'] {
        --jmix-markdown-editor-toolbar-button-background: var(--vaadin-background-container-strong);
        --jmix-markdown-editor-toolbar-button-text-color: var(--vaadin-text-color);
        --jmix-markdown-editor-toolbar-button-border-color: var(--vaadin-border-color);
    }

    [part~='toolbar-button']::before {
        content: '';
        display: block;
        height: var(--vaadin-icon-size, 1lh);
        width: var(--vaadin-icon-size, 1lh);
        background: currentColor;
        mask-size: var(--vaadin-icon-visual-size, 100%);
        mask-repeat: no-repeat;
        mask-position: 50%;
    }

    [part~='toolbar-button-heading']::before {
        mask-image: var(--_markdown-editor-icon-heading);
    }

    [part~='toolbar-button-bold']::before {
        mask-image: var(--_markdown-editor-icon-bold);
    }

    [part~='toolbar-button-italic']::before {
        mask-image: var(--_markdown-editor-icon-italic);
    }

    [part~='toolbar-button-quote']::before {
        mask-image: var(--_markdown-editor-icon-quote);
    }

    [part~='toolbar-button-code']::before {
        mask-image: var(--_markdown-editor-icon-code);
    }

    [part~='toolbar-button-link']::before {
        mask-image: var(--_markdown-editor-icon-link);
    }

    [part~='toolbar-button-ul']::before {
        mask-image: var(--_markdown-editor-icon-ul);
    }

    [part~='toolbar-button-ol']::before {
        mask-image: var(--_markdown-editor-icon-ol);
    }

    [part~='toolbar-button-task']::before {
        mask-image: var(--_markdown-editor-icon-task);
    }

    .toolbar-overflow {
        font-weight: bold;
        letter-spacing: 1px;
    }

    vaadin-context-menu-item::part(checkmark) {
        display: none;
    }

    .markdown-editor-context-menu-item-content {
        display: flex;
        align-items: center;
        gap: var(--vaadin-space-s, 0.5em);
    }

    .markdown-editor-context-menu-item-content::before {
        content: '';
        display: block;
        flex-shrink: 0;
        width: var(--vaadin-icon-size, 1lh);
        height: var(--vaadin-icon-size, 1lh);
        background: currentColor;
        mask-size: var(--vaadin-icon-visual-size, 100%);
        mask-repeat: no-repeat;
        mask-position: 50%;
    }

    .markdown-editor-context-menu-item-content-heading::before {
        mask-image: var(--_markdown-editor-icon-heading);
    }

    .markdown-editor-context-menu-item-content-bold::before {
        mask-image: var(--_markdown-editor-icon-bold);
    }

    .markdown-editor-context-menu-item-content-italic::before {
        mask-image: var(--_markdown-editor-icon-italic);
    }

    .markdown-editor-context-menu-item-content-quote::before {
        mask-image: var(--_markdown-editor-icon-quote);
    }

    .markdown-editor-context-menu-item-content-code::before {
        mask-image: var(--_markdown-editor-icon-code);
    }

    .markdown-editor-context-menu-item-content-link::before {
        mask-image: var(--_markdown-editor-icon-link);
    }

    .markdown-editor-context-menu-item-content-ul::before {
        mask-image: var(--_markdown-editor-icon-ul);
    }

    .markdown-editor-context-menu-item-content-ol::before {
        mask-image: var(--_markdown-editor-icon-ol);
    }

    .markdown-editor-context-menu-item-content-task::before {
        mask-image: var(--_markdown-editor-icon-task);
    }

    .content-area {
        position: relative;
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;
        overflow-y: auto;
    }

    .content-area:focus-visible {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    }

    .editor-area {
        display: flex;
        flex-direction: column;
        flex: 1;
    }

    /* Hide the editor area in preview mode and when read-only. */

    :host([mode='preview']) .editor-area,
    :host([readonly]) .editor-area {
        display: none;
    }

    .preview-area {
        display: none;
        padding: var(--vaadin-padding-s) var(--vaadin-padding-m);
        box-sizing: border-box;
    }

    /* Show the preview area in preview mode and when read-only. */

    :host([mode='preview']) .preview-area,
    :host([readonly]) .preview-area {
        display: block;
    }

    ::slotted(textarea) {
        width: 100%;
        min-height: 6em;
        field-sizing: content;
        padding: var(--vaadin-padding-s) var(--vaadin-padding-m);
        font: inherit;
        font-family: monospace;
        color: var(--vaadin-input-field-value-color, var(--vaadin-text-color));
        background: transparent;
        border: none;
        outline: none;
        resize: none;
        box-sizing: border-box;
        caret-color: var(--vaadin-input-field-value-color);
    }

    /* Reset ::placeholder to inherit so :placeholder-shown can override color.
       ::slotted(textarea)::placeholder does not work in Safari. */

    ::slotted(textarea)::placeholder {
        color: inherit;
    }

    ::slotted(textarea:placeholder-shown) {
        color: var(--vaadin-input-field-placeholder-color, var(--vaadin-text-color-secondary));
    }

    :host([disabled]) ::slotted(textarea) {
        color: var(--vaadin-text-color-disabled);
        pointer-events: none;
        -webkit-user-select: none;
        user-select: none;
    }

    @media (forced-colors: active) {
        .jmix-markdown-editor-container {
            border: 1px solid CanvasText;
        }

        .tab[aria-selected='true'] {
            border-bottom-width: 3px;
        }

        [part~='toolbar-button']::before {
            background: CanvasText;
        }

        [part~='toolbar-button'][aria-pressed='true'] {
            background: Highlight;
        }

        [part~='toolbar-button'][aria-pressed='true']:before {
            background: HighlightText;
        }

        ::slotted(textarea) {
            color: FieldText;
            background: Field;
        }

        ::slotted(textarea:placeholder-shown) {
            color: GrayText;
        }
    }
`;

export const jmixMarkdownEditorBaseStyles = [icons, markdownEditorBaseStyles];
