# Markdown Editor — Manual Test Checklist

## 1. Height behaviour

### 1.1 Auto height (no explicit height set)

- [ ] Component grows vertically as text is typed in the Edit tab.
- [ ] Switching to Preview renders the full markdown without clipping; component height matches rendered content.
- [ ] Switching back to Edit returns to the textarea height matching its content.
- [ ] Empty editor shows a reasonable minimum height (≈ 6 lines of text).

### 1.2 Fixed height (explicit height configured)

- [ ] Component height stays exactly at the set value regardless of content length.
- [ ] Edit tab: textarea scrolls vertically when content exceeds the available area; no content is clipped by the border.
- [ ] Preview tab: rendered markdown scrolls vertically when it exceeds the available area.
- [ ] Scrollbar appears inside the component border, not overlapping it.
- [ ] Switching between Edit and Preview tabs does not change the component's outer height.

---

## 2. Disabled state

- [ ] Tab buttons (Edit / Preview) are visually dimmed and cannot be clicked.
- [ ] Toolbar buttons are visually dimmed and cannot be clicked.
- [ ] Textarea text cannot be edited.
- [ ] Textarea text cannot be selected with the mouse.
- [ ] `Tab` navigation skips the disabled editor entirely, including the scrollable content area.
- [ ] No hover highlight appears on tabs or toolbar buttons when the cursor moves over them.
- [ ] The disabled cursor is shown when hovering over the component.

---

## 3. Read-only state

- [ ] Neither tabs nor toolbar are rendered — only the preview content is shown.
- [ ] The field wrapper has a dashed border.
- [ ] The value is fully readable and the preview scrolls when content exceeds the set height.
- [ ] `Tab` navigation can focus the scrollable preview content area so the value can be read and scrolled with the keyboard.
- [ ] No interaction is possible.

---

## 4. Toolbar — formatting actions

Perform each action with the editor in Edit mode.

### Wrap formats (bold, italic, code, link)

| Button | No selection                                  | With selection                         | Toggle off (cursor inside)             |
|--------|-----------------------------------------------|----------------------------------------|----------------------------------------|
| Bold   | Inserts `**\|**`, cursor between markers      | Wraps selection as `**sel**`           | Removes markers, restores selection    |
| Italic | Inserts `_\|_`                                | Wraps as `_sel_`                       | Removes markers                        |
| Code   | Inserts `` `\|` ``                            | Wraps as `` `sel` ``                   | Removes markers                        |
| Link   | Inserts `[](url)`, cursor inside `[]`         | Wraps as `[sel](url)`, selects `url`   | Removes `[](url)` construct            |

- [ ] Bold: `⌘B` / `Ctrl+B` keyboard shortcut works.
- [ ] Italic: `⌘I` / `Ctrl+I` keyboard shortcut works.
- [ ] Code: `⌘E` / `Ctrl+E` keyboard shortcut works.
- [ ] Link: `⌘K` / `Ctrl+K` keyboard shortcut works.
- [ ] `⌘Z` / `Ctrl+Z` undoes a toolbar or shortcut formatting action.
- [ ] `⌘⇧Z` / `Ctrl+Y` redoes a previously undone formatting action.
- [ ] Undo/redo of formatting actions interleaves correctly with undoing plain typing.

### Line formats (heading, quote, unordered list, ordered list, task list)

| Button         | Apply                           | Remove                            |
|----------------|---------------------------------|-----------------------------------|
| Heading        | Inserts `## ` at line start     | Removes `## ` from line start     |
| Quote          | Inserts `> ` at line start      | Removes `> ` from line start      |
| Unordered list | Inserts `- ` at line start      | Removes `- ` from line start      |
| Ordered list   | Inserts `1. ` at line start     | Removes `1. ` from line start     |
| Task list      | Inserts `- [ ] ` at line start  | Removes `- [ ] ` from line start  |

- [ ] Line formats always insert/remove at the **line start**, not at the cursor position.
- [ ] Cursor / selection offset is preserved correctly after apply and remove.
- [ ] With multiple lines selected, the format is applied to / removed from **all** selected lines in one undo step.
- [ ] With multiple lines selected and all already formatted, clicking the button removes the format from all.
- [ ] Clicking UL on ordered-list lines converts them to `- ` items (ul↔ol conversion).
- [ ] Clicking OL on unordered-list lines converts them to sequentially numbered `1.` `2.` … items.
- [ ] ul↔ol conversion works correctly across a multi-line selection.
- [ ] Task-list lines (`- [ ] `) are not silently converted when clicking UL or OL.

### Toggle state (aria-pressed)

- [ ] Active wrap format (cursor is inside markers) shows the toolbar button as pressed.
- [ ] Active line format (current line has the prefix) shows the toolbar button as pressed.
- [ ] Task list prefix `- [ ] ` does not incorrectly activate the unordered list button.
- [ ] Pressed state updates when moving the cursor with arrow keys or clicking.

---

## 5. Toolbar overflow menu

- [ ] At full width all 9 buttons are visible and no overflow `···` button appears.
- [ ] When the component is rendered at a width that does not fit all buttons, `···` appears immediately on load — no interaction required.
- [ ] Resize the browser narrower: buttons that no longer fit disappear and `···` appears without flickering.
- [ ] Resize the browser wider: once all buttons fit again, `···` disappears without flickering.
- [ ] Resize the browser so narrowly that only `···` remains visible, then resize wider: toolbar buttons return from overflow as soon as they fit.
- [ ] At an extremely narrow width where only `···` is visible, keyboard `Tab` focus lands on `···`, not on the textarea or a hidden toolbar button.
- [ ] Clicking `···` opens the overflow menu listing the hidden buttons with their labels and icons.
- [ ] Each overflow menu item shows an icon matching the corresponding toolbar button.
- [ ] No empty checkmark space appears to the left of overflow menu items.
- [ ] Clicking an overflow item applies the formatting, closes the menu, and moves focus to the textarea.
- [ ] Pressing `Escape` inside the overflow menu closes it and returns focus to `···`.
- [ ] Clicking outside the component closes the overflow menu.

---

## 6. Keyboard navigation

- [ ] `Tab` moves focus in order: tabs → toolbar → textarea (Edit mode) or tabs (Preview mode).
- [ ] `Shift+Tab` moves focus in reverse order.
- [ ] Inside the tab bar, `←` / `→` switches between Edit and Preview; focus stays on the active tab — it does not jump to the textarea.
- [ ] Switching from Preview to Edit via `←` / `→` restores cursor position when the textarea is subsequently focused.
- [ ] Clicking the Edit tab switches to Edit mode and focuses the textarea with cursor position restored.
- [ ] Inside the toolbar, `←` / `→` moves between toolbar buttons; wraps at ends.
- [ ] Clicking a toolbar button with the mouse updates the roving focus so that a subsequent Tab → Shift+Tab returns to that button.
- [ ] `Escape` inside the toolbar moves focus to the textarea.
- [ ] When the browser is resized so the overflow button disappears, `Tab` into the toolbar lands on a visible button, not a ghost focus position.
- [ ] The scrollable content area is skipped by `Tab` in editable Edit mode, but remains focusable in Preview and read-only modes.
- [ ] Inside the overflow menu, `↑` / `↓` move between items; `Escape` or `Tab` closes the menu and returns focus to `···`.

---

## 7. i18n

### Default labels

- [ ] Edit tab is labelled **Edit**, Preview tab is labelled **Preview**.
- [ ] Toolbar button tooltips match the defaults: Heading, Bold, Italic, Quote, Code, Link, Unordered list, Ordered list, Task list.
- [ ] On macOS, shortcut hints show `⌘B`, `⌘I`, `⌘E`, `⌘K`.
- [ ] On other OS, shortcut hints show `Ctrl+B`, `Ctrl+I`, `Ctrl+E`, `Ctrl+K`.

### Custom i18n (configure overrides via `setI18n()`)

- [ ] Overridden tab labels are shown instead of the defaults.
- [ ] Overridden toolbar tooltips are shown instead of the defaults.
- [ ] Keys not included in the override fall back to their defaults (no blank labels).

---

## 8. Placeholder

- [ ] Placeholder text is shown in the Edit tab when the value is empty.
- [ ] Placeholder disappears as soon as the user starts typing.
- [ ] Placeholder is styled in the secondary text colour (not the same as value text).
- [ ] No placeholder is shown in the Preview tab.
- [ ] Clearing the value restores the placeholder.

---

## 9. Field features

- [ ] Label is shown above the field; clicking it focuses the textarea (Edit mode).
- [ ] Required indicator (`*`) click also focuses the textarea.
- [ ] Helper text is shown below the field.
- [ ] Required indicator (`*`) is visible when `setRequiredIndicatorVisible(true)`.
- [ ] Binder validation: submitting with an empty value shows the required error message below the field.
- [ ] Binder validation: a value that fails a custom validator shows that validator's error message.
- [ ] Valid submission completes without validation errors.
- [ ] `value-changed` event fires according to the _value change mode_.
- [ ] Switching to Preview and back to Edit preserves the cursor position and selection.
- [ ] Clicking into the textarea adds `focused` attribute on the host element; clicking outside removes it.
- [ ] Tabbing into the textarea adds both `focused` and `focus-ring` on the host; tabbing away removes both.
- [ ] Clicking a toolbar button removes `focused` from the host (textarea lost focus); the field border remains highlighted via `:focus-within`.

---

## 10. Mode change event and Java API

- [ ] Switching to the Preview tab fires `ModeChangedEvent` with mode `PREVIEW` and `fromClient=true`.
- [ ] Switching back to the Edit tab fires `ModeChangedEvent` with mode `EDIT` and `fromClient=true`.
- [ ] Calling `setMode(Mode.PREVIEW)` server-side fires `ModeChangedEvent` with mode `PREVIEW` and `fromClient=false`.
- [ ] Calling `setMode(Mode.EDIT)` server-side fires `ModeChangedEvent` with mode `EDIT` and `fromClient=false`.
- [ ] `getMode()` returns `EDIT` on initial load before any interaction.

---

## 11. Theming

Test each theme mode on a page that also contains at least one standard Vaadin text field for visual comparison.

### 11.1 No theme

- [ ] The component renders with a visible border, label area, header row, and content area.
- [ ] No layout is broken: the header row, toolbar, tabs, and content area are all visible and correctly positioned.
- [ ] The invalid state shows a visible error border and error message below the field.
- [ ] All theme variants work correctly: 
  - [ ] `toolbar-align-start`
  - [ ] `toolbar-align-center`
  - [ ] `toolbar-align-end`
  - [ ] `helper-above-field`

### 11.2 Lumo theme

- [ ] The component font family, font size, and line height match those of other Lumo fields on the same page.
- [ ] Text appears sharp and smooth, consistent with other Lumo fields on the same page.
- [ ] The invalid state border color matches the error color used by other Lumo fields on the same page.
- [ ] No structural styles are missing: the field border, header row, tabs, toolbar buttons, and content area all appear correctly.
- [ ] All theme variants work correctly: 
  - [ ] `toolbar-align-start`
  - [ ] `toolbar-align-center`
  - [ ] `toolbar-align-end`
  - [ ] `helper-above-field`

### 11.3 Aura theme

- [ ] Toolbar buttons animate smoothly when hovered (color and background-color transition).
- [ ] Toolbar buttons show a subtle scale-down when clicked and spring back when released.
- [ ] The same transitions and press effect apply to the overflow `···` button.
- [ ] Toolbar button icons appear slightly smaller than their button bounds, consistent with other Aura toolbar components on the same page.
- [ ] The invalid state border color matches the error color used by other Aura fields on the same page.
- [ ] No structural styles are missing: the field border, header row, tabs, toolbar buttons, and content area all appear correctly.
- [ ] All theme variants work correctly:
  - [ ] `toolbar-align-start`
  - [ ] `toolbar-align-center`
  - [ ] `toolbar-align-end`
  - [ ] `helper-above-field`

---

## 12. Declarative XML attributes

Load a view descriptor that configures the editor declaratively, then verify both the server-side component API and
client-side behaviour.

- [ ] Java API has corresponding XML attributes.
- [ ] XML attributes are applied to the component.

---

## 13. Jmix data binding

- [ ] The initial entity property value is shown in Edit and Preview modes.
- [ ] Changing the entity property programmatically updates the editor value without user interaction.
- [ ] Typing in the editor updates the bound entity property according to the configured value change mode.
- [ ] Setting the bound entity property to `null` clears the editor and leaves it in the empty state without exceptions.
- [ ] Setting the editor value to `null` writes `null` back to the bound entity property without exceptions.
- [ ] Required state and required message are resolved through the Jmix field delegate when validation is executed.
- [ ] Custom Jmix validators registered on the component run against the bound value and report errors through the field.
- [ ] The component can be used inside `formLayout dataContainer="..."` with only `property="..."` configured.
- [ ] Unsaved-change tracking detects edits made through the bound editor in a detail view.
- [ ] Data binding works after switching between Edit and Preview and after toggling read-only mode.

---

## 14. Studio support

- [ ] MarkdownEditor is available in the Studio component palette under Components.
- [ ] Dragging the component to a view creates a `<markdownEditor>` element with a valid `id`.
- [ ] Studio offers conversion from `textField`, `textArea`, `codeEditor`, and `richTextEditor` to `markdownEditor`.
- [ ] The Properties panel lists all supported XML attributes.
- [ ] Property categories are correct: data binding properties under Data Binding, validation properties under Validation,
  size properties under Size, and theme/style properties under Look and Feel.
- [ ] `mode` is shown as an enum with `EDIT` and `PREVIEW` options and default value `EDIT`.
- [ ] `themeNames` is shown as a values-list with the four MarkdownEditor theme names.
- [ ] `dataContainer` suggestions include compatible instance containers from the current view.
- [ ] `property` suggestions are filtered by the selected `dataContainer` and include compatible `String` properties.
- [ ] Selecting `dataContainer` and `property` in Studio writes valid XML and the view loads successfully.
- [ ] Changing `dataContainer` refreshes the `property` suggestions and does not leave stale invalid bindings unnoticed.
- [ ] Studio allows adding a `<tooltip>` child and string `<validators>` child where they are valid.
- [ ] XML validation and completion accept all supported MarkdownEditor attributes and reject unsupported attributes.
