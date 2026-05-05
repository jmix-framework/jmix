/**
 * @license
 * Copyright (c) 2025 GlebFox
 */
import '@vaadin/component-base/src/styles/style-props.js';
import { css } from 'lit';
import { icons } from './jmix-markdown-editor-icons.js';

const markdownEditorBaseStyles = css`
  /*
   * Field layout (label, helper, error grid placement) and the required
   * indicator are handled by the inputFieldShared styles included alongside
   * this sheet. Only component-specific rules appear here.
   */

  .field-wrapper {
    --_field-radius: var(--vaadin-input-field-border-radius, var(--vaadin-radius-m));
    display: flex;
    flex-direction: column;
    border-radius: var(--_field-radius);
    border: var(--vaadin-input-field-border-width, 1px) solid
      var(--vaadin-input-field-border-color, var(--vaadin-border-color));
    background: var(--vaadin-input-field-background, var(--vaadin-background-color));
    overflow: hidden;
  }

  :host([invalid]) .field-wrapper {
    --vaadin-input-field-border-color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
  }

  .field-wrapper:focus-within {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-input-field-border-width, 1px) * -1);
  }

  :host([readonly]) .field-wrapper {
    border-style: dashed;
  }

  :host([readonly]) .field-wrapper:focus-within {
    outline-style: dashed;
    --vaadin-input-field-border-color: transparent;
  }

  :host([disabled]) .field-wrapper {
    --vaadin-input-field-background: var(
      --vaadin-input-field-disabled-background,
      var(--vaadin-background-container-strong)
    );
    --vaadin-input-field-border-color: transparent;
    cursor: var(--vaadin-disabled-cursor);
  }

  .header-row {
    display: flex;
    align-items: center;
    background: var(--vaadin-background-container);
    border-bottom: var(--vaadin-input-field-border-width, 1px) solid var(--vaadin-border-color-secondary);
    min-height: 36px;
    padding: 0 var(--vaadin-padding-xs);
  }

  .tabs {
    display: flex;
    flex-shrink: 0;
  }

  .tab {
    padding: var(--vaadin-padding-xs) var(--vaadin-padding-s);
    cursor: var(--vaadin-clickable-cursor);
    border: none;
    background: none;
    color: var(--vaadin-text-color-secondary);
    border-bottom: 2px solid transparent;
    margin-bottom: -1px;
    font: inherit;
    white-space: nowrap;
    -webkit-tap-highlight-color: transparent;
  }

  .tab:hover:not(:disabled) {
    color: var(--vaadin-text-color);
  }

  .tab:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  .tab:disabled {
    color: var(--vaadin-text-color-disabled);
    cursor: var(--vaadin-disabled-cursor);
  }

  .tab[aria-selected='true'] {
    color: var(--vaadin-text-color);
    border-bottom-color: currentColor;
    font-weight: 500;
  }

  .divider {
    width: 1px;
    height: 20px;
    background: var(--vaadin-border-color-secondary);
    margin: 0 var(--vaadin-padding-xs);
    flex-shrink: 0;
  }

  .toolbar {
    display: flex;
    align-items: center;
    flex: 1;
    gap: 1px;
    overflow: hidden;
    position: relative;
  }

  [part~='toolbar-button'],
  .toolbar-overflow {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    border: none;
    background: none;
    cursor: var(--vaadin-clickable-cursor);
    border-radius: var(--vaadin-radius-s);
    color: var(--vaadin-text-color-secondary);
    font: inherit;
    flex-shrink: 0;
    -webkit-tap-highlight-color: transparent;
  }

  [part~='toolbar-button']:focus-visible,
  .toolbar-overflow:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  [part~='toolbar-button']:hover:not(:disabled),
  .toolbar-overflow:hover:not(:disabled) {
    background: var(--vaadin-background-container-strong);
    color: var(--vaadin-text-color);
  }

  [part~='toolbar-button']:disabled,
  .toolbar-overflow:disabled {
    color: var(--vaadin-text-color-disabled);
    cursor: var(--vaadin-disabled-cursor);
  }

  [part~='toolbar-button'][hidden] {
    display: none;
  }

  [part~='toolbar-button'][aria-pressed='true'] {
    background: var(--vaadin-background-container-strong);
    color: var(--vaadin-text-color);
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

  .markdown-editor-context-menu-item-content-heading::before { mask-image: var(--_markdown-editor-icon-heading); }
  .markdown-editor-context-menu-item-content-bold::before    { mask-image: var(--_markdown-editor-icon-bold); }
  .markdown-editor-context-menu-item-content-italic::before  { mask-image: var(--_markdown-editor-icon-italic); }
  .markdown-editor-context-menu-item-content-quote::before   { mask-image: var(--_markdown-editor-icon-quote); }
  .markdown-editor-context-menu-item-content-code::before    { mask-image: var(--_markdown-editor-icon-code); }
  .markdown-editor-context-menu-item-content-link::before    { mask-image: var(--_markdown-editor-icon-link); }
  .markdown-editor-context-menu-item-content-ul::before      { mask-image: var(--_markdown-editor-icon-ul); }
  .markdown-editor-context-menu-item-content-ol::before      { mask-image: var(--_markdown-editor-icon-ol); }
  .markdown-editor-context-menu-item-content-task::before    { mask-image: var(--_markdown-editor-icon-task); }

  .content-area {
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow-y: auto;
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
    .field-wrapper {
      border: 1px solid CanvasText;
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
