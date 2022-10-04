/*
 * Copyright 2022 Haulmont.
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

import { css, registerStyles } from "@vaadin/vaadin-themable-mixin";

const pickerButton = css`
  :host {
    /* Sizing */
    --lumo-button-size: var(--lumo-size-s);
    min-width: var(--lumo-button-size);
    height: var(--lumo-button-size);
    box-sizing: border-box;
    
    /* Style */
    font-family: var(--lumo-font-family);
    font-size: var(--lumo-font-size-m);
    font-weight: 500;
    color: var(--_lumo-button-color, var(--lumo-primary-text-color));
    background-color: var(--_lumo-button-background-color, var(--lumo-contrast-5pct));
    border-radius: var(--lumo-border-radius-m);
    cursor: var(--lumo-clickable-cursor);
    -webkit-tap-highlight-color: transparent;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
  
  /* Set only for the internal parts so we don’t affect the host vertical alignment */
  [part='icon'] {
    line-height: var(--lumo-line-height-xs);
  }
  
  /* For interaction states */
  :host::before,
  :host::after {
    content: '';
    /* We rely on the host always being relative */
    position: absolute;
    z-index: 1;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background-color: currentColor;
    border-radius: inherit;
    opacity: 0;
    transition: opacity 0.2s;
    pointer-events: none;
  }
  
  /* Hover */
  
  @media (any-hover: hover) {
    :host(:hover)::before {
      opacity: 0.02;
    }
  }
  
  /* Active */
  
  :host::after {
    transition: opacity 1.4s, transform 0.1s;
    filter: blur(8px);
  }
  
  :host([active])::before {
    opacity: 0.05;
    transition-duration: 0s;
  }
  
  :host([active])::after {
    opacity: 0.1;
    transition-duration: 0s, 0s;
    transform: scale(0);
  }
  
  /* Keyboard focus */
  
  :host([focus-ring]) {
    box-shadow: 0 0 0 2px var(--lumo-primary-color-50pct);
  }
  
  /* Disabled state. Keep selectors after other color variants. */
  
  :host([disabled]) {
    pointer-events: none;
    color: var(--lumo-disabled-text-color);
  }
  
  /* Icons */
  
  [part] ::slotted(vaadin-icon),
  [part] ::slotted(iron-icon) {
    display: inline-block;
    width: var(--lumo-icon-size-m);
    height: var(--lumo-icon-size-m);
  }
  
  /* Vaadin icons are based on a 16x16 grid (unlike Lumo and Material icons with 24x24), so they look too big by default */
  [part] ::slotted(vaadin-icon[icon^='vaadin:']),
  [part] ::slotted(iron-icon[icon^='vaadin:']) {
    padding: 0.25em;
    box-sizing: border-box !important;
  }
`;

registerStyles('jmix-value-picker-button', pickerButton, {
  moduleId: 'lumo-value-picker-button-styles'
});