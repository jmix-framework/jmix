/**
 * @license
 * Copyright (c) 2025 GlebFox
 */
import '@vaadin/context-menu';
import '@vaadin/markdown';
import { html, LitElement, nothing } from 'lit';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DelegateStateMixin } from '@vaadin/component-base/src/delegate-state-mixin.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { I18nMixin } from '@vaadin/component-base/src/i18n-mixin.js';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';
import { TooltipController } from '@vaadin/component-base/src/tooltip-controller.js';
import { DelegateFocusMixin } from '@vaadin/a11y-base/src/delegate-focus-mixin.js';
import { FieldMixin } from '@vaadin/field-base/src/field-mixin.js';
import { LabelledInputController } from '@vaadin/field-base/src/labelled-input-controller.js';
import { TextAreaController } from '@vaadin/field-base/src/text-area-controller.js';
import { inputFieldShared } from '@vaadin/field-base/src/styles/input-field-shared-styles.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { jmixMarkdownEditorBaseStyles } from './styles/jmix-markdown-editor-base-styles.js';

const DEFAULT_I18N = Object.freeze({
  tabs: {
    edit: 'Edit',
    preview: 'Preview',
  },
  toolbar: {
    accessibleLabel: 'Formatting',
    heading: 'Heading',
    bold: 'Bold',
    italic: 'Italic',
    quote: 'Quote',
    code: 'Code',
    link: 'Link',
    unorderedList: 'Unordered list',
    orderedList: 'Ordered list',
    taskList: 'Task list',
    overflow: 'More formatting options',
  },
});

/**
 * `<markdown-editor>` is a web component for editing and previewing Markdown content.
 *
 * ```html
 * <markdown-editor label="Article body"></markdown-editor>
 * ```
 *
 * ### Styling
 *
 * The following shadow DOM parts are available for styling:
 *
 * Part name              | Description
 * -----------------------|------------------------------------------
 * `label`                | The label element
 * `required-indicator`   | The required state indicator element
 * `input-field`          | The element that wraps all editor content
 * `header`               | The header row containing tabs and toolbar
 * `tabs`                 | The tab list container
 * `tab`                  | A tab button (also `tab-edit` or `tab-preview`)
 * `divider`              | The vertical divider between tabs and toolbar
 * `toolbar`              | The formatting toolbar
 * `toolbar-button`       | A toolbar icon button (also `toolbar-button-<key>`)
 * `toolbar-overflow`     | The overflow `···` button
 * `content-area`         | The scrollable content container
 * `editor`               | The wrapper around the slotted textarea
 * `preview`              | The wrapper around the rendered markdown
 * `helper-text`          | The helper text element wrapper
 * `error-message`        | The error message element
 *
 * The following state attributes are available for styling:
 *
 * Attribute           | Description
 * --------------------|-------------------------------------------
 * `disabled`          | Set when the element is disabled
 * `focused`           | Set when the textarea has focus
 * `focus-ring`        | Set when the textarea was focused via keyboard
 * `readonly`          | Set when the element is read-only
 * `has-label`         | Set when the element has a label
 * `has-helper`        | Set when the element has helper text or slot
 * `has-error-message` | Set when the element has an error message
 * `has-tooltip`       | Set when the element has a slotted tooltip
 * `invalid`           | Set when the element is invalid
 * `required`          | Set when the element is required
 *
 * @fires {CustomEvent} invalid-changed - Fired when the `invalid` property changes.
 * @fires {CustomEvent} value-changed - Fired when the `value` property changes.
 * @fires {CustomEvent} validated - Fired whenever the field is validated.
 * @fires {CustomEvent} mode-changed - Fired when the editor mode changes between Edit and Preview.
 *
 * @customElement
 * @extends HTMLElement
 * @mixes DelegateFocusMixin
 * @mixes DelegateStateMixin
 * @mixes ElementMixin
 * @mixes FieldMixin
 * @mixes I18nMixin
 * @mixes ThemableMixin
 */
export class JmixMarkdownEditor extends I18nMixin(
  DEFAULT_I18N,
  DelegateStateMixin(FieldMixin(DelegateFocusMixin(ThemableMixin(ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement))))))),
) {
  static get is() {
    return 'jmix-markdown-editor';
  }

  static get lumoInjector() {
    return { ...super.lumoInjector, includeBaseStyles: true };
  }

  static get properties() {
    return {
      /**
       * The current Markdown value of the editor.
       * @type {string}
       */
      value: {
        type: String,
        value: '',
        notify: true,
      },

      /**
       * The active tab. Either `'edit'` or `'preview'`.
       * @type {string}
       */
      mode: {
        type: String,
        value: 'edit',
        reflectToAttribute: true,
      },

      /**
       * The name of the editor submitted with form data.
       * @type {string}
       */
      name: {
        type: String,
        reflectToAttribute: true,
      },

      /**
       * Set to true to disable the editor.
       * @type {boolean}
       */
      disabled: {
        type: Boolean,
        value: false,
        observer: '_disabledChanged',
        reflectToAttribute: true,
        sync: true,
      },

      /**
       * Set to true to make the editor read-only.
       * @type {boolean}
       */
      readonly: {
        type: Boolean,
        value: false,
        reflectToAttribute: true,
      },

      /**
       * A hint to the user of what can be entered in the editor.
       * @type {string}
       */
      placeholder: {
        type: String,
        reflectToAttribute: true,
      },
    };
  }

  static get styles() {
    return [inputFieldShared, jmixMarkdownEditorBaseStyles];
  }

  static get delegateAttrs() {
    return ['required', 'readonly', 'invalid', 'name', 'placeholder', 'disabled'];
  }

  static get delegateProps() {
    // 'value' is intentionally excluded. DelegatePropsMixin would assign
    // ta.value on every property change, resetting the browser undo stack even
    // for formatting actions that use execCommand. We sync manually in updated()
    // with an equality guard.
    return [];
  }

  constructor() {
    super();
    this._hiddenFromIndex = Infinity;
    this._toolbarFocusKey = this._toolbarItems[0].key;
    this._activeFormats = new Set();
    this._savedSelection = null;
    this._pendingSelection = null;
    this._skipValueSync = false;
    this._boundUpdateActiveFormats = this._updateActiveFormats.bind(this);
  }

  /** @protected */
  render() {
    const items = this._toolbarItems;
    const hasOverflow = this._hiddenFromIndex < items.length;

    return html`
      <div part="label" @click="${this._onLabelClick}">
        <slot name="label"></slot>
        <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
      </div>

      <div class="field-wrapper" part="input-field">
        ${this.readonly ? nothing : html`
          <div class="header-row" part="header">
            <div class="tabs" part="tabs" role="tablist" @keydown="${this._onTabsKeydown}">
              <button
                role="tab"
                class="tab"
                part="tab tab-edit"
                aria-selected="${this.mode === 'edit'}"
                tabindex="${this.disabled ? '-1' : this.mode === 'edit' ? '0' : '-1'}"
                ?disabled=${this.disabled}
                @click=${() => this._onEditTabClick()}
                type="button"
              >${this.__effectiveI18n.tabs.edit}</button>
              <button
                role="tab"
                class="tab"
                part="tab tab-preview"
                aria-selected="${this.mode === 'preview'}"
                tabindex="${this.disabled ? '-1' : this.mode === 'preview' ? '0' : '-1'}"
                ?disabled=${this.disabled}
                @click=${() => this._onPreviewTabClick()}
                type="button"
              >${this.__effectiveI18n.tabs.preview}</button>
            </div>

            ${this.mode === 'edit' ? html`
              <div class="divider" part="divider"></div>
              <div class="toolbar" part="toolbar" role="toolbar" aria-label="${this.__effectiveI18n.toolbar.accessibleLabel}" @keydown="${this._onToolbarKeydown}">
                ${items.map((item, idx) => this._renderToolbarButton(item, idx))}
                ${hasOverflow ? html`
                  <button
                    class="toolbar-overflow"
                    part="toolbar-overflow"
                    title="${this.__effectiveI18n.toolbar.overflow}"
                    tabindex="${this.disabled ? '-1' : this._toolbarFocusKey === 'overflow' ? '0' : '-1'}"
                    ?disabled=${this.disabled}
                    @focus=${() => this._onToolbarButtonFocus('overflow')}
                    type="button"
                  >···</button>
                ` : nothing}
              </div>
            ` : nothing}
          </div>
        `}

        <div
          class="content-area"
          part="content-area"
          tabindex="${this._contentAreaTabIndex}"
          @blur="${this._onContentBlur}"
          @focus="${this._onContentFocus}"
          @mousedown="${this._onContentAreaMouseDown}"
        >
          <div class="editor-area" part="editor" @click="${this._onFieldClick}">
            <slot name="textarea"></slot>
          </div>
          <div class="preview-area" part="preview">
            <slot name="preview"></slot>
          </div>
        </div>
      </div>

      ${this.disabled || this.readonly ? nothing : html`
        <vaadin-context-menu
          open-on="click"
          position="bottom-end"
          @item-selected="${this._onOverflowItemSelected}"
          @closed="${this._onOverflowClosed}"
        ></vaadin-context-menu>
      `}

      <div part="helper-text">
        <slot name="helper"></slot>
      </div>

      <div part="error-message">
        <slot name="error-message"></slot>
      </div>

      <slot name="tooltip"></slot>
    `;
  }

  /** @protected */
  focus(options) {
    const contentFocusTarget = this._contentFocusTarget;
    if (!this.disabled && (this.readonly || this.mode === 'preview') && contentFocusTarget) {
      contentFocusTarget.focus(options);
      if (!(options && options.focusVisible === false)) {
        this.setAttribute('focus-ring', '');
      }
      return;
    }

    super.focus(options);
  }

  /** @protected */
  blur() {
    const contentFocusTarget = this._contentFocusTarget;
    if (contentFocusTarget && this.shadowRoot.activeElement === contentFocusTarget) {
      contentFocusTarget.blur();
      return;
    }

    super.blur();
  }

  /** @private */
  _onLabelClick(e) {
    if (!this.disabled && (this.readonly || this.mode === 'preview')) {
      e.preventDefault();
      this.focus();
    }
  }

  /** @private */
  _onContentAreaMouseDown(e) {
    if (this.disabled) {
      e.preventDefault();
    }
  }

  /** @private */
  _onFieldClick() {
    if (this.mode !== 'preview' && !this.readonly && !this.disabled) {
      this._textareaEl?.focus();
    }
  }

  /** @protected */
  ready() {
    super.ready();

    this._resizeObserver = new ResizeObserver(() => this._measureToolbar());

    this._tooltipController = new TooltipController(this);
    this._tooltipController.setPosition('top');
    this.addController(this._tooltipController);

    // Create the textarea in light DOM. TextAreaController appends a
    // <textarea slot="textarea"> to the host and assigns it a unique id so
    // that LabelledInputController can wire label.for, enabling native
    // label-click-to-focus across the shadow boundary.
    this._textAreaController = new TextAreaController(this, (ta) => {
      this._textareaEl = ta;
      this.ariaTarget = ta;
      this.stateTarget = ta;
      this._setFocusElement(ta);
      ta.addEventListener('input', (e) => this._onInput(e));
      ta.addEventListener('keydown', (e) => this._onKeydown(e));
      ta.addEventListener('keyup', this._boundUpdateActiveFormats);
      ta.addEventListener('mouseup', this._boundUpdateActiveFormats);
      ta.addEventListener('select', this._boundUpdateActiveFormats);
      ta.addEventListener('focus', () => {
        if (this._pendingSelection) {
          const { start, end } = this._pendingSelection;
          this._pendingSelection = null;
          requestAnimationFrame(() => this._textareaEl.setSelectionRange(start, end));
        }
      });
      this.addController(new LabelledInputController(ta, this._labelController));
      this._tooltipController.setAriaTarget(ta);
    });
    this.addController(this._textAreaController);

    // Create the preview element in light DOM so it is projected into the
    // shadow DOM via <slot name="preview">.
    this._markdownEl = document.createElement('vaadin-markdown');
    this._markdownEl.setAttribute('slot', 'preview');
    this._markdownEl.content = this.value || '';
    this.appendChild(this._markdownEl);
  }

  /** @protected */
  connectedCallback() {
    super.connectedCallback();

    if (this._resizeObserver && this._observedToolbar) {
      this._resizeObserver.observe(this._observedToolbar);
      this._measureToolbar();
    }
  }

  /** @protected */
  disconnectedCallback() {
    super.disconnectedCallback();
    this._resizeObserver?.disconnect();
  }

  /** @protected */
  updated(changed) {
    super.updated(changed);

    // Wire the context-menu to the overflow button whenever the toolbar re-renders,
    // and keep its items in sync with the current hidden-from index.
    const contextMenu = this.shadowRoot.querySelector('vaadin-context-menu');
    if (contextMenu) {
      const overflowBtn = this.shadowRoot.querySelector('.toolbar-overflow');
      if (overflowBtn) {
        if (contextMenu.listenOn !== overflowBtn) {
          contextMenu.listenOn = overflowBtn;
        }
        contextMenu.items = this._toolbarItems.slice(this._hiddenFromIndex).map((item) => ({
          component: this._buildOverflowItem(item),
          action: item.action,
        }));
      } else {
        contextMenu.listenOn = contextMenu;
        contextMenu.items = [];
      }
      if (changed.has('disabled') && this.disabled) {
        contextMenu.close();
      }
    }

    // The toolbar is conditionally rendered (write mode only). Re-attach the
    // ResizeObserver whenever it re-enters the DOM after a preview→write switch.
    const toolbar = this.shadowRoot.querySelector('.toolbar');
    if (toolbar !== this._observedToolbar) {
      if (this._observedToolbar) {
        this._resizeObserver?.unobserve(this._observedToolbar);
      }
      this._observedToolbar = toolbar;
      if (toolbar) {
        this._resizeObserver?.observe(toolbar);
        this._measureToolbar();
      }
    }

    // When switching back to write mode, move the saved cursor position to
    // _pendingSelection. The textarea's focus listener applies it the next
    // time the textarea receives focus, keeping cursor restore decoupled from
    // focus management so keyboard-driven tab switching can keep focus on the
    // mode tab rather than jumping to the textarea.
    if (changed.has('mode') && this.mode === 'edit' && this._savedSelection) {
      this._pendingSelection = this._savedSelection;
      this._savedSelection = null;
    }

    // Fire mode-changed after the first render (changed.get returns the previous
    // value; undefined means this is the initial property assignment, not a change).
    if (changed.has('mode') && changed.get('mode') !== undefined) {
      this.dispatchEvent(new CustomEvent('mode-changed', { bubbles: true, composed: true }));
    }

    // Sync value to the textarea. An equality guard prevents same-value
    // reassignment, which resets the native undo stack in most browsers.
    // _skipValueSync is set before execCommand calls to skip this sync
    // entirely — execCommand already updated ta.value via the native API.
    if (changed.has('value') && this._textareaEl) {
      if (this._skipValueSync) {
        this._skipValueSync = false;
      } else if (this._textareaEl.value !== (this.value ?? '')) {
        this._textareaEl.value = this.value ?? '';
      }
    }

    // Sync the light-DOM markdown preview content.
    if (changed.has('value') && this._markdownEl) {
      this._markdownEl.content = this.value;
    }
  }

  /** @private */
  get _isMac() {
    return /Mac/.test(navigator.platform);
  }

  /** @private */
  get _contentAreaTabIndex() {
    if (this.disabled) {
      return '-1';
    }
    return this.readonly || this.mode === 'preview' ? String(this.focusElement?.tabIndex ?? 0) : '-1';
  }

  /** @private */
  get _contentFocusTarget() {
    return this.shadowRoot.querySelector('.content-area');
  }

  /** @private */
  _onContentFocus(e) {
    e.stopPropagation();
    this.dispatchEvent(new Event('focus'));
  }

  /** @private */
  _onContentBlur(e) {
    e.stopPropagation();
    this.dispatchEvent(new Event('blur'));
  }

  /** @private */
  _isContentFocusEvent(e) {
    return !this.disabled && e.composedPath().includes(this._contentFocusTarget);
  }

  /** @protected */
  _shouldSetFocus(e) {
    return super._shouldSetFocus(e) || this._isContentFocusEvent(e);
  }

  /** @protected */
  _shouldRemoveFocus(e) {
    return super._shouldRemoveFocus(e) || this._isContentFocusEvent(e);
  }

  /** @private */
  get _toolbarItems() {
    const t = this.__effectiveI18n.toolbar;
    const mod = this._isMac ? '⌘' : 'Ctrl+';
    return [
      { key: 'heading', title: t.heading, action: () => this._togglePrefixFormat({ prefix: '## ' }) },
      { key: 'bold', title: t.bold, shortcut: `${mod}B`, action: () => this._toggleFormat({ prefix: '**', suffix: '**' }) },
      { key: 'italic', title: t.italic, shortcut: `${mod}I`, action: () => this._toggleFormat({ prefix: '_', suffix: '_' }) },
      { key: 'quote', title: t.quote, action: () => this._togglePrefixFormat({ prefix: '> ' }) },
      { key: 'code', title: t.code, shortcut: `${mod}E`, action: () => this._toggleFormat({ prefix: '`', suffix: '`' }) },
      { key: 'link', title: t.link, shortcut: `${mod}K`, action: () => this._toggleLinkFormat() },
      { key: 'ul', title: t.unorderedList, action: () => this._toggleUlFormat() },
      { key: 'ol', title: t.orderedList, action: () => this._toggleOlFormat() },
      { key: 'task', title: t.taskList, action: () => this._togglePrefixFormat({ prefix: '- [ ] ' }) },
    ];
  }

  /** @private */
  _measureToolbar() {
    const toolbar = this.shadowRoot.querySelector('.toolbar');
    if (!toolbar) return;

    const btns = Array.from(toolbar.querySelectorAll('[part~="toolbar-button"]'));
    if (!btns.length) return;

    const toolbarWidth = toolbar.offsetWidth;

    // Hidden buttons (display:none) return offsetWidth 0 so cannot be measured.
    // Find the first visible button; fall back to the overflow button when all
    // toolbar buttons are currently hidden.
    const visibleBtn =
      btns.find((btn) => !btn.hidden) ||
      toolbar.querySelector('.toolbar-overflow');
    if (!visibleBtn) return;
    const btnWidth = visibleBtn.offsetWidth;
    if (!btnWidth) return;

    // Read the column gap from the toolbar so the calculation respects theme
    // or consumer overrides to the gap value.
    const gap = parseInt(getComputedStyle(toolbar).columnGap, 10) || 0;

    const N = btns.length;

    // Pass 1: total width of all N buttons with N-1 gaps between them.
    const totalWidth = N * btnWidth + (N - 1) * gap;

    let firstHidden = Infinity;

    if (totalWidth > toolbarWidth) {
      // Pass 2: find the split. Reserve the overflow button width plus one gap.
      const available = toolbarWidth - btnWidth - gap;
      let used = 0;
      for (let i = 0; i < N; i++) {
        used += btnWidth + (i > 0 ? gap : 0);
        if (used > available) {
          firstHidden = i;
          break;
        }
      }
    }

    if (this._hiddenFromIndex !== firstHidden) {
      this._hiddenFromIndex = firstHidden;

      // If the focused key is now hidden or the overflow button has disappeared,
      // move focus tracking to a visible toolbar control.
      const focusIdx = this._toolbarItems.findIndex((i) => i.key === this._toolbarFocusKey);
      const overflowVisible = firstHidden < this._toolbarItems.length;
      const overflowGone = this._toolbarFocusKey === 'overflow' && !overflowVisible;
      const regularButtonHidden =
        this._toolbarFocusKey !== 'overflow' && (focusIdx === -1 || focusIdx >= this._hiddenFromIndex);
      if (overflowGone || regularButtonHidden) {
        this._toolbarFocusKey =
          firstHidden === 0
            ? 'overflow'
            : this._toolbarItems[Math.max(0, firstHidden === Infinity ? this._toolbarItems.length - 1 : firstHidden - 1)].key;
      }

      this.requestUpdate();
    }
  }

  /** @private */
  _onToolbarButtonFocus(key) {
    if (this._toolbarFocusKey !== key) {
      this._toolbarFocusKey = key;
      this.requestUpdate();
    }
  }

  /** @private */
  _onOverflowItemSelected(e) {
    if (this.disabled) return;

    e.detail.value.action?.();
  }

  /** @private */
  _buildOverflowItem(item) {
    const el = document.createElement('vaadin-context-menu-item');
    const content = document.createElement('div');
    content.className = `markdown-editor-context-menu-item-content markdown-editor-context-menu-item-content-${item.key}`;
    content.textContent = item.title;
    el.appendChild(content);
    return el;
  }

  /** @private */
  _onOverflowClosed() {
    if (this.disabled) return;

    // Return focus to the overflow button unless an action already moved
    // focus to the textarea.
    requestAnimationFrame(() => {
      if (document.activeElement !== this._textareaEl) {
        this.shadowRoot.querySelector('.toolbar-overflow')?.focus();
      }
    });
  }

  /** @private */
  _onEditTabClick() {
    if (this.disabled) return;

    this.mode = 'edit';
    this.updateComplete.then(() => this._textareaEl?.focus());
  }

  /** @private */
  _onPreviewTabClick() {
    if (this.disabled) return;

    this._saveSelection();
    this.mode = 'preview';
  }

  /** @private */
  _saveSelection() {
    if (this._textareaEl) {
      this._savedSelection = { start: this._textareaEl.selectionStart, end: this._textareaEl.selectionEnd };
    }
  }

  /** @private */
  _onTabsKeydown(e) {
    if (this.disabled) return;

    if (e.key !== 'ArrowLeft' && e.key !== 'ArrowRight') return;
    e.preventDefault();
    const nextMode = this.mode === 'edit' ? 'preview' : 'edit';
    if (nextMode === 'preview') {
      this._saveSelection();
    }
    this.mode = nextMode;
    this.updateComplete.then(() => {
      const idx = this.mode === 'edit' ? 0 : 1;
      this.shadowRoot.querySelectorAll('.tab')[idx]?.focus();
    });
  }

  /** @private */
  _onToolbarKeydown(e) {
    if (this.disabled) return;

    if (e.key === 'ArrowRight') {
      e.preventDefault();
      this._moveFocusInToolbar(1);
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      this._moveFocusInToolbar(-1);
    } else if (e.key === 'Escape') {
      e.preventDefault();
      this._textareaEl?.focus();
    }
  }

  /** @private */
  _moveFocusInToolbar(step) {
    const visibleKeys = this._toolbarItems.slice(0, this._hiddenFromIndex).map((i) => i.key);
    if (this._hiddenFromIndex < this._toolbarItems.length) {
      visibleKeys.push('overflow');
    }
    if (!visibleKeys.length) return;

    let idx = visibleKeys.indexOf(this._toolbarFocusKey);
    if (idx === -1) idx = 0;
    idx = (visibleKeys.length + idx + step) % visibleKeys.length;
    this._toolbarFocusKey = visibleKeys[idx];

    this.requestUpdate();
    this.updateComplete.then(() => {
      const toolbar = this.shadowRoot.querySelector('.toolbar');
      const btn =
        this._toolbarFocusKey === 'overflow'
          ? toolbar?.querySelector('.toolbar-overflow')
          : toolbar?.querySelector(`[part~="toolbar-button-${this._toolbarFocusKey}"]`);
      btn?.focus();
    });
  }

  /** @private */
  _isWrapActive(val, start, end, prefix, suffix) {
    const pLen = prefix.length;
    const sLen = suffix.length;
    const sel = val.slice(start, end);
    const contextWrapped = start >= pLen && val.slice(start - pLen, start) === prefix && val.slice(end, end + sLen) === suffix;
    const selfWrapped = sel.startsWith(prefix) && sel.endsWith(suffix) && sel.length > pLen + sLen;
    if (contextWrapped || selfWrapped) {
      return true;
    }

    if (prefix !== suffix) {
      return false;
    }

    return Boolean(this._findWrapRange(val, start, end, prefix));
  }

  /** @private */
  _findWrapRange(val, start, end, delimiter) {
    const lineStart = val.lastIndexOf('\n', start - 1) + 1;
    const nextNewline = val.indexOf('\n', end);
    const lineEnd = nextNewline === -1 ? val.length : nextNewline;
    const line = val.slice(lineStart, lineEnd);
    const positions = [];
    let pos = line.indexOf(delimiter);
    while (pos !== -1) {
      positions.push(lineStart + pos);
      pos = line.indexOf(delimiter, pos + delimiter.length);
    }

    for (let i = 0; i + 1 < positions.length; i += 2) {
      const openStart = positions[i];
      const contentStart = openStart + delimiter.length;
      const contentEnd = positions[i + 1];
      const closeEnd = contentEnd + delimiter.length;
      const cursorInside = start === end && start >= contentStart && start <= contentEnd;
      const selectionInside = start < end && start >= contentStart && end <= contentEnd;
      const selectionIsWhole = start === openStart && end === closeEnd;

      if (cursorInside || selectionInside || selectionIsWhole) {
        return { openStart, contentStart, contentEnd, closeEnd };
      }
    }

    return null;
  }

  /** @private */
  _isPrefixActive(val, start, prefix) {
    const lineStart = val.lastIndexOf('\n', start - 1) + 1;
    return val.slice(lineStart, lineStart + prefix.length) === prefix;
  }

  /** @private */
  _updateActiveFormats() {
    const ta = this._textareaEl;
    if (!ta) {
      if (this._activeFormats.size > 0) {
        this._activeFormats = new Set();
        this.requestUpdate();
      }
      return;
    }

    const { value: val, selectionStart: start, selectionEnd: end } = ta;
    const active = new Set();

    if (this._isWrapActive(val, start, end, '**', '**')) active.add('bold');
    if (this._isWrapActive(val, start, end, '_', '_')) active.add('italic');
    if (this._isWrapActive(val, start, end, '`', '`')) active.add('code');

    if (this._findLinkAtRange(val, start, end)) {
      active.add('link');
    }

    // Prefix formats: check most specific first to avoid ul matching inside task.
    if (this._isPrefixActive(val, start, '- [ ] ')) {
      active.add('task');
    } else if (this._isPrefixActive(val, start, '- ')) {
      active.add('ul');
    }
    if (this._isPrefixActive(val, start, '## ')) active.add('heading');
    if (this._isPrefixActive(val, start, '> ')) active.add('quote');

    const lineStart = val.lastIndexOf('\n', start - 1) + 1;
    if (/^\d+\. /.test(val.slice(lineStart))) active.add('ol');

    const changed = active.size !== this._activeFormats.size || [...active].some((k) => !this._activeFormats.has(k));
    if (changed) {
      this._activeFormats = active;
      this.requestUpdate();
    }
  }

  /** @private */
  _toggleFormat({ prefix, suffix = '' }) {
    const ta = this._textareaEl;
    if (!ta) return;

    const { value: val, selectionStart: start, selectionEnd: end } = ta;
    const pLen = prefix.length;
    const sLen = suffix.length;

    if (!this._isWrapActive(val, start, end, prefix, suffix)) {
      this._applyFormat({ prefix, suffix });
      return;
    }

    if (prefix === suffix) {
      const range = this._findWrapRange(val, start, end, prefix);
      if (range) {
        const inner = val.slice(range.contentStart, range.contentEnd);
        this._replaceRange(ta, range.openStart, range.closeEnd, inner);
        requestAnimationFrame(() => {
          if (start === end) {
            const cursor = Math.max(range.openStart, Math.min(start - pLen, range.openStart + inner.length));
            ta.setSelectionRange(cursor, cursor);
          } else if (start === range.openStart && end === range.closeEnd) {
            ta.setSelectionRange(range.openStart, range.openStart + inner.length);
          } else {
            ta.setSelectionRange(start - pLen, end - pLen);
          }
          this._updateActiveFormats();
        });
        return;
      }
    }

    // Remove wrap markers.
    if (start === end) {
      const cursor = start - pLen;
      this._replaceRange(ta, start - pLen, end + sLen, '');
      requestAnimationFrame(() => { ta.setSelectionRange(cursor, cursor); this._updateActiveFormats(); });
    } else {
      const sel = val.slice(start, end);
      const contextWrapped = start >= pLen && val.slice(start - pLen, start) === prefix && val.slice(end, end + sLen) === suffix;
      if (contextWrapped) {
        this._replaceRange(ta, start - pLen, end + sLen, sel);
        requestAnimationFrame(() => { ta.setSelectionRange(start - pLen, end - pLen); this._updateActiveFormats(); });
      } else {
        // Selection itself contains the markers.
        const inner = sel.slice(pLen, sel.length - sLen);
        this._replaceRange(ta, start, end, inner);
        requestAnimationFrame(() => { ta.setSelectionRange(start, start + inner.length); this._updateActiveFormats(); });
      }
    }
  }

  /** @private */
  _getLineRange(ta) {
    const val = ta.value;
    const { selectionStart, selectionEnd } = ta;
    const rangeStart = val.lastIndexOf('\n', selectionStart - 1) + 1;
    // If the selection ends exactly at the start of a new line (the preceding
    // newline was included in the selection), exclude that trailing line.
    const effectiveEnd =
      selectionEnd > selectionStart && val[selectionEnd - 1] === '\n'
        ? selectionEnd - 1
        : selectionEnd;
    const nextNewline = val.indexOf('\n', effectiveEnd);
    const rangeEnd = nextNewline === -1 ? val.length : nextNewline;
    return { rangeStart, rangeEnd };
  }

  /** @private */
  _adjustPosition(origPos, rangeStart, origLines, newLines) {
    // Map an absolute textarea position through a multi-line transformation.
    // Positions before the range are unchanged; positions within a line are
    // shifted by that line's length delta (clamped to the new line bounds);
    // positions after the range are shifted by the total delta.
    if (origPos < rangeStart) return origPos;
    let origLineStart = 0;
    let newLineStart = 0;
    let totalDelta = 0;
    for (let i = 0; i < origLines.length; i++) {
      const origLineEnd = origLineStart + origLines[i].length;
      if (origPos - rangeStart <= origLineEnd) {
        const offsetInLine = origPos - rangeStart - origLineStart;
        const newOffset = Math.max(
          0,
          Math.min(offsetInLine + newLines[i].length - origLines[i].length, newLines[i].length),
        );
        return rangeStart + newLineStart + newOffset;
      }
      totalDelta += newLines[i].length - origLines[i].length;
      origLineStart = origLineEnd + 1;
      newLineStart += newLines[i].length + 1;
    }
    return origPos + totalDelta;
  }

  /** @private */
  _applyLineTransform(transformFn) {
    const ta = this._textareaEl;
    if (!ta) return;
    const { value: val, selectionStart, selectionEnd } = ta;
    const { rangeStart, rangeEnd } = this._getLineRange(ta);
    const origLines = val.slice(rangeStart, rangeEnd).split('\n');
    const newLines = transformFn(origLines);
    this._replaceRange(ta, rangeStart, rangeEnd, newLines.join('\n'));
    const newStart = this._adjustPosition(selectionStart, rangeStart, origLines, newLines);
    const newEnd = this._adjustPosition(selectionEnd, rangeStart, origLines, newLines);
    requestAnimationFrame(() => { ta.setSelectionRange(newStart, newEnd); this._updateActiveFormats(); });
  }

  /** @private */
  _togglePrefixFormat({ prefix }) {
    this._applyLineTransform((origLines) => {
      const allActive = origLines.every((line) => line.startsWith(prefix));
      return allActive
        ? origLines.map((line) => line.slice(prefix.length))
        : origLines.map((line) => (line.startsWith(prefix) ? line : prefix + line));
    });
  }

  /** @private */
  _toggleUlFormat() {
    this._applyLineTransform((origLines) => {
      // All lines are plain ul (not task list) → remove '- ' from all.
      const allPlainUl = origLines.every(
        (line) => line.startsWith('- ') && !/^- \[[ x]\] /.test(line),
      );
      if (allPlainUl) return origLines.map((line) => line.slice(2));
      return origLines.map((line) => {
        const olMatch = line.match(/^(\d+\. )/);
        if (olMatch) return '- ' + line.slice(olMatch[1].length); // ol → ul
        if (line.startsWith('- ')) return line;                   // already ul (or task list)
        return '- ' + line;                                        // plain → ul
      });
    });
  }

  /** @private */
  _toggleOlFormat() {
    this._applyLineTransform((origLines) => {
      // All lines are ordered list → remove 'N. ' from all.
      const allOl = origLines.every((line) => /^\d+\. /.test(line));
      if (allOl) return origLines.map((line) => line.replace(/^\d+\. /, ''));
      let counter = 1;
      return origLines.map((line) => {
        const olMatch = line.match(/^(\d+\. )/);
        if (olMatch) return `${counter++}. ` + line.slice(olMatch[1].length); // renumber ol
        if (/^- \[[ x]\] /.test(line)) return line;                           // task list: skip
        if (line.startsWith('- ')) return `${counter++}. ` + line.slice(2);   // ul → ol
        return `${counter++}. ` + line;                                         // plain → ol
      });
    });
  }

  /** @private */
  _applyFormat({ prefix, suffix = '' }) {
    const ta = this._textareaEl;
    if (!ta) return;

    const start = ta.selectionStart;
    const end = ta.selectionEnd;
    const selected = ta.value.slice(start, end);

    const replacement = selected ? prefix + selected + suffix : prefix + suffix;
    const cursorStart = start + prefix.length;
    const cursorEnd = selected ? end + prefix.length : cursorStart;

    this._replaceRange(ta, start, end, replacement);
    requestAnimationFrame(() => ta.setSelectionRange(cursorStart, cursorEnd));
  }

  /** @private */
  _toggleLinkFormat() {
    const ta = this._textareaEl;
    if (!ta) return;

    const { value: val, selectionStart: start, selectionEnd: end } = ta;
    const link = this._findLinkAtRange(val, start, end);

    if (link) {
      this._replaceRange(ta, link.start, link.end, link.label);
      const selectionStart = link.start;
      const selectionEnd = link.start + link.label.length;
      requestAnimationFrame(() => {
        if (start === end) {
          const offsetInLabel = Math.max(0, Math.min(start - link.labelStart, link.label.length));
          const cursor = link.start + offsetInLabel;
          ta.setSelectionRange(cursor, cursor);
        } else {
          ta.setSelectionRange(selectionStart, selectionEnd);
        }
        this._updateActiveFormats();
      });
      return;
    }

    const SufStr = '](url)';

    if (start === end) {
      // Apply: [](url), cursor inside [].
      this._replaceRange(ta, start, end, '[](url)');
      requestAnimationFrame(() => { ta.setSelectionRange(start + 1, start + 1); this._updateActiveFormats(); });
      return;
    }

    const sel = val.slice(start, end);
    const contextWrapped = start >= 1 && val.slice(start - 1, start) === '[' && val.slice(end, end + SufStr.length) === SufStr;
    const linkMatch = sel.match(/^\[([^\]]+)\]\([^)]*\)$/);

    if (contextWrapped) {
      // [selected](url) where selection is `selected` → selected.
      this._replaceRange(ta, start - 1, end + SufStr.length, sel);
      requestAnimationFrame(() => { ta.setSelectionRange(start - 1, start - 1 + sel.length); this._updateActiveFormats(); });
    } else if (linkMatch) {
      // Selection is `[text](url)` → text.
      const inner = linkMatch[1];
      this._replaceRange(ta, start, end, inner);
      requestAnimationFrame(() => { ta.setSelectionRange(start, start + inner.length); this._updateActiveFormats(); });
    } else {
      // Apply: [selected](url), cursor on url.
      const urlStart = start + sel.length + 3;
      this._replaceRange(ta, start, end, '[' + sel + '](url)');
      requestAnimationFrame(() => { ta.setSelectionRange(urlStart, urlStart + 3); this._updateActiveFormats(); });
    }
  }

  /** @private */
  _findLinkAtRange(val, start, end) {
    const lineStart = val.lastIndexOf('\n', start - 1) + 1;
    const nextNewline = val.indexOf('\n', end);
    const lineEnd = nextNewline === -1 ? val.length : nextNewline;
    const line = val.slice(lineStart, lineEnd);
    const linkRegex = /\[([^\]]*)\]\(([^)]*)\)/g;
    let match;

    while ((match = linkRegex.exec(line))) {
      const linkStart = lineStart + match.index;
      const labelStart = linkStart + 1;
      const label = match[1];
      const labelEnd = labelStart + label.length;
      const urlStart = labelEnd + 2;
      const urlEnd = urlStart + match[2].length;
      const linkEnd = urlEnd + 1;

      const cursorInside = start === end && start >= linkStart && start <= linkEnd;
      const selectionInside = start < end && start >= linkStart && end <= linkEnd;
      if (cursorInside || selectionInside) {
        return { start: linkStart, end: linkEnd, labelStart, labelEnd, urlStart, urlEnd, label };
      }
    }

    return null;
  }

  /** @private */
  _updateValue(newValue) {
    // Setting the property is sufficient: notify: true on the value property
    // causes PolylitMixin to dispatch value-changed automatically after update.
    this.value = newValue;
  }

  /** @private */
  _replaceRange(ta, rangeStart, rangeEnd, replacement) {
    const nextValue = ta.value.slice(0, rangeStart) + replacement + ta.value.slice(rangeEnd);
    ta.focus();
    ta.setSelectionRange(rangeStart, rangeEnd);
    if (nextValue === ta.value) {
      this._skipValueSync = false;
      return;
    }

    this._skipValueSync = true;
    if (!document.execCommand('insertText', false, replacement)) {
      // execCommand is unavailable (e.g. in test environments). Fall back to
      // direct assignment, which works but resets the native undo stack.
      this._skipValueSync = false;
      ta.value = nextValue;
      this._updateValue(ta.value);
    }
    // On success, execCommand fires 'input' synchronously, which calls
    // _onInput → _updateValue → this.value = ta.value. The subsequent
    // updated() call sees _skipValueSync and skips writing back to ta.value,
    // preserving the native undo stack entry created by execCommand.
  }

  /** @private */
  _onInput(e) {
    this._updateValue(e.target.value);
  }

  /** @private */
  _onKeydown(e) {
    if (this.disabled || this.readonly) return;

    if (!e.metaKey && !e.ctrlKey) return;

    const shortcuts = {
      b: () => this._toggleFormat({ prefix: '**', suffix: '**' }),
      i: () => this._toggleFormat({ prefix: '_', suffix: '_' }),
      e: () => this._toggleFormat({ prefix: '`', suffix: '`' }),
      k: () => this._toggleLinkFormat(),
    };

    const action = shortcuts[e.key.toLowerCase()];
    if (action) {
      e.preventDefault();
      action();
    }
  }

  /** @private */
  _renderToolbarButton(item, idx) {
    const hidden = idx >= this._hiddenFromIndex;
    return html`
      <button
        part="toolbar-button toolbar-button-${item.key}"
        title="${item.title}${item.shortcut ? ' (' + item.shortcut + ')' : ''}"
        ?hidden="${hidden}"
        tabindex="${this.disabled ? '-1' : item.key === this._toolbarFocusKey ? '0' : '-1'}"
        aria-pressed="${this._activeFormats.has(item.key)}"
        ?disabled=${this.disabled}
        @focus=${() => this._onToolbarButtonFocus(item.key)}
        @click=${(e) => { e.stopPropagation(); item.action(); }}
        type="button"
      ></button>
    `;
  }
}

defineCustomElement(JmixMarkdownEditor);
