# Component Patterns in `layout.xsd`

Patterns for adding or extending components in the FlowUI layout XSD, illustrated with definitions already in the file. Use these as templates and decision rules.

Prerequisite: read [reusable-types.md](reusable-types.md) for the catalog of building blocks referenced below.

---

## 1. Decision tree: which base do I extend?

Pick the **most specific** existing base type that already provides the bulk of what your component needs.

```
Are you adding...

  a form field that binds to a data container property?
    └─ extend baseFieldComponent
         (validatable? → validatableBaseFieldComponent
            text-input-shaped? → baseTextFieldComponent
            a value picker with action buttons? → valuePickerComponent
            an entity picker? → entityPickerComponent)

  a custom action (visible in <actions>)?
    └─ extend baseAction (or noShortcutBaseAction inside menus/dropdowns)

  an icon-displaying leaf component?
    └─ extend baseIconComponent

  a tabbed container?
    └─ extend baseTabsComponent

  a dropdown / split-button-style component?
    └─ extend baseDropdownButtonComponent

  a filter primitive?
    └─ extend baseSingleFilterComponent (or one of basePropertyFilter/baseJpqlFilter/baseGroupFilter)

  a thin wrapper around an HTML element?
    └─ extend baseHtmlComponent / baseHtmlContainer / baseClickableHtmlContainer / clickableHtmlContainerWithArea

  none of the above (a generic visible component)?
    └─ extend baseComponent
```

Never extend nothing — every visible component should at least reach `baseComponent` so it inherits `id`, `visible`, `colspan`, `alignSelf`, `justifySelf`, `css`, and the `##other` namespace extension hook.

---

## 2. Example: a new "plain" component extending `baseComponent`

`progressBarComponent` ([layout.xsd](../../../jmix-flowui/flowui/src/main/resources/io/jmix/flowui/view/layout.xsd)) is a minimal pattern — visible widget with its own theme set and a handful of typed primitive attributes:

```xml
<xs:complexType name="progressBarComponent">
    <xs:complexContent>
        <xs:extension base="baseComponent">
            <xs:attribute name="max" type="xs:double"/>
            <xs:attribute name="min" type="xs:double"/>
            <xs:attribute name="value" type="xs:double"/>
            <xs:attribute name="indeterminate" type="xs:boolean"/>
            <xs:attribute name="themeNames" type="progressBarThemeNames"/>

            <xs:attributeGroup ref="hasSize"/>
            <xs:attributeGroup ref="hasClassNames"/>
        </xs:extension>
    </xs:complexContent>
</xs:complexType>
```

Observations:

- Component-specific values that are pure primitives (`min`/`max`/`value`/`indeterminate`) get inline `<xs:attribute>` with a built-in type.
- Sizing and CSS classes come from `hasSize` + `hasClassNames` — never re-declared.
- The theme set is its own simpleType (`progressBarThemeNames`) — see §5 for when to add a new theme type.

To register the new component in the view namespace, also add it to the `standardComponent` group:

```xml
<xs:element name="progressBar" type="progressBarComponent"/>
```

---

## 3. Example: a new form-field component

Fields go through the data-binding/validation chain. `numberFieldComponent` is the canonical pattern:

```xml
<xs:complexType name="numberFieldComponent">
    <xs:complexContent>
        <xs:extension base="baseTextFieldComponent">
            <xs:sequence>
                <xs:element name="validators" minOccurs="0" type="numberFieldValidatorType"/>
            </xs:sequence>

            <xs:attribute name="max" type="xs:double"/>
            <xs:attribute name="min" type="xs:double"/>
            <xs:attribute name="step" type="xs:double"/>
            <xs:attribute name="stepButtonsVisible" type="xs:boolean"/>
            <xs:attribute name="allowedCharPattern" type="resourceString"/>

            <xs:attributeGroup ref="hasRequired"/>
        </xs:extension>
    </xs:complexContent>
</xs:complexType>
```

Key idea: the `<validators>` child uses a **field-specific validator type** (`numberFieldValidatorType`) that already extends `baseValidatorType` and exposes only the rules that make sense for numbers. If a new field type needs validation, create a matching validator (see §6) — do not inline validation elements on the component.

For a selection field (combo, list, etc.) reuse `hasItems` rather than re-declaring `itemsContainer` and `itemsEnum`:

```xml
<xs:complexType name="radioButtonGroupComponent">
    <xs:complexContent>
        <xs:extension base="baseComponent">
            ...
            <xs:attribute name="themeNames" type="radioButtonGroupThemeNames"/>
            <xs:attribute name="datatype" type="fieldDatatypeEnum"/>

            <xs:attributeGroup ref="hasItems"/>
            <xs:attributeGroup ref="hasSize"/>
            <xs:attributeGroup ref="hasLabel"/>
            ...
        </xs:extension>
    </xs:complexContent>
</xs:complexType>
```

---

## 4. Example: extending an existing component without breaking it

Two safe ways to add functionality to an existing component:

**(a) Create a subtype via `xs:extension`.** This is how `treeDataGridComponent` adds hierarchy to `dataGridComponent`:

```xml
<xs:complexType name="treeDataGridComponent">
    <xs:complexContent>
        <xs:extension base="dataGridComponent">
            <xs:attribute name="hierarchyProperty" type="xs:string" use="required"/>
            <xs:attribute name="hierarchyColumn" type="xs:string"/>
            <xs:attribute name="showOrphans" type="xs:boolean"/>
        </xs:extension>
    </xs:complexContent>
</xs:complexType>
```

Then register a separate element name (`<treeDataGrid>`) in `standardComponent`. The existing `dataGrid` element keeps its surface intact.

**(b) Add an attribute directly** to the existing complexType. Acceptable when it's a genuine new capability of the same component. Prefer extending an attributeGroup if the same attribute might appear elsewhere:

- If the new attribute is **specific to this component** → inline `<xs:attribute>`.
- If the new attribute is **shared with ≥2 components** → introduce a `has<X>` attributeGroup and reference it.

Example of the second path: `hasItems` exists exactly because `itemsContainer`/`itemsEnum` are reused across `radioButtonGroup`, `checkboxGroup`, `listBox`, `multiSelectListBox`, `multiSelectComboBox`, `select`, `gridLayout`, `virtualList`, `twinColumn`.

---

## 5. Theme names: when to create a new simpleType

Vaadin theme names are per-component lists of CSS tokens. Conventions:

- **Always** create a `<componentName>ThemeNames` simpleType in the "ThemeNames" block of the XSD when a component supports its own set.
- The shape is `<xs:union>` of empty string + an `xs:string` enumeration so the attribute is optional and validates known tokens:

  ```xml
  <xs:simpleType name="myComponentThemeNames">
      <xs:union>
          <xs:simpleType><xs:restriction base="xs:string"/></xs:simpleType>
          <xs:simpleType>
              <xs:restriction base="xs:string">
                  <xs:enumeration value="small"/>
                  <xs:enumeration value="my-variant"/>
              </xs:restriction>
          </xs:simpleType>
      </xs:union>
  </xs:simpleType>
  ```

- If your set is **identical** to an existing one (e.g. `gridColumnVisibilityThemeNames` ≡ `buttonThemeNames`), declare it as a single-member union to keep the Java-API-mirroring name without value duplication:

  ```xml
  <xs:simpleType name="myComponentThemeNames">
      <xs:union memberTypes="buttonThemeNames"/>
  </xs:simpleType>
  ```

- If your set is a **superset** of an existing one (e.g. `tabSheetThemeNames` adds three values to `tabsThemeNames`), express the relationship explicitly with `xs:union`:

  ```xml
  <xs:simpleType name="myComponentThemeNames">
      <xs:union memberTypes="parentThemeNames">
          <xs:simpleType>
              <xs:restriction base="xs:string">
                  <xs:enumeration value="extra-token-1"/>
                  <xs:enumeration value="extra-token-2"/>
              </xs:restriction>
          </xs:simpleType>
      </xs:union>
  </xs:simpleType>
  ```

---

## 6. Validators: when to create a new validator type

The validator hierarchy is purpose-built — every leaf field has its own `*ValidatorType` that locks down which rules make sense for that input.

| If you need… | Use / extend |
|---|---|
| Just `<notNull>` and `<custom>` | `baseValidatorType` |
| The above + collection rules (`size`, `notEmpty`) | `collectionValidatorType` |
| The above + string rules (`notBlank`, `regexp`, `email`) | `stringValidatorType` |
| The full numeric+string constraint set | `constraintValidatorType` |
| Date-only checks (`future`, `past`, …) | `dateValidatorsType` |
| Only `<custom>` (no built-in rules) | `onlyCustomValidatorType` |

If your field needs a **different mix** of rules, create a new validator complexType that extends the closest existing base and adds only the missing rule elements. Re-use the leaf rule types (`messageValidatorType`, `digitsValidatorType`, `decimalMaxMinValidatorType`, `maxMinValidatorType`, `regexpValidatorType`, `sizeValidatorType`, `dateValidatorType`, `customValidatorType`) — never duplicate their attribute surfaces.

---

## 7. Enum simple types: when to create one vs. reuse

**Reuse first.** Before creating a new `<xs:simpleType>` with an enumeration, check the catalog in [reusable-types.md §3](reusable-types.md#3-shared-simple-types-enums). The XSD already covers:

- Direction & alignment (`orientationType`, `alignEnum`, `justifyContentModeEnum`, …)
- Open modes (`openModeType`)
- Filtering (`propertyFilterOperation`, `groupFilterOperation`, `likeClauseEnum`)
- Icons (`jmixFontIconEnum`)
- Data types (`fieldDatatypeEnum` and date/time subsets)
- Resource strings (`resourceString`) — use this whenever an attribute can be a translation key
- Shortcuts (`shortcutCombination`)

**Create a new enum** when:

- The set of values has **no semantic equivalent** elsewhere in the XSD.
- The values mirror a Java enum that the corresponding component actually accepts.
- The enum is intentionally an *open* enumeration (allow any string + suggest known values): use the `<xs:union>(empty, enumeration)` shape. This is the case for theme names, picker steps, icon enums, shortcut combinations.
- The enum is intentionally a *closed* enumeration (only allowed values are valid): use a plain `<xs:restriction base="xs:string">`.

**Aliasing instead of duplication.** When the Java API exposes two distinct enum names that happen to share their values (e.g. `RangeInput.Orientation` and `Orientation`), keep both XSD type names but declare the alias as a `<xs:union memberTypes="..."/>` — this preserves the documentation value of distinct names without duplicating the enumeration values. The XSD does this for `formLabelsPosition`, `rangeInputOrientationEnum`, `userMenuViewItemOpenModeType`, `detailButtonRendererOpenModeType`, `textAreaThemeNames`, `gridColumnVisibilityThemeNames`.

**Naming.** Prefer the suffix `Enum` for new enumeration simpleTypes (`*Enum`); use `Type` only for non-enum simpleTypes (e.g. `boxSizingType` is named with `Type` because of legacy — new types should prefer `Enum`).

---

## 8. Child slots: prefer named types over inline definitions

If your component has a child slot, give it a **type** rather than an inline anonymous `complexType`. This keeps the XSD scannable and lets other components reuse the slot.

Examples that get this right:

- `<tooltip>` always uses `tooltipElement`.
- `<icon>` slots use `iconComponentElement` (which references `iconComponentGroup`).
- `<prefix>`/`<suffix>` slots use `prefixOrSuffixComponent`.
- Card sub-slots (`title`, `subtitle`, `media`, `header`, `footer`) use `singleLayoutOrComponentType`.

When you add a new slot, look for an existing single-element wrapper type first.

For containers that take multiple children, use `<xs:group ref="layoutOrComponent"/>` (or `singleLayoutOrComponent` for "one optional child") inside the type body — see `scrollerContainer`, `accordionContainer`, `sidePanelLayoutComponent`.

---

## 9. Action types: viewAction / gridAction / pickerAction / genericFilterAction

These four complex types are *intentionally* empty extensions of `baseAction`:

```xml
<xs:complexType name="viewAction">
    <xs:complexContent>
        <xs:extension base="baseAction"/>
    </xs:complexContent>
</xs:complexType>
```

The empty extension makes the action element nameable distinctly in different containers (`<viewActions>`, `<dataGrid><actions>`, `<entityPicker><actions>`, `<genericFilter><actions>`) even though their attribute surface is the same. **Do not collapse them into a single type.** If you add a new place that exposes `<action>`, decide whether one of the four fits, or add a fifth empty extension following the same pattern.

---

## 10. Adding a new attribute group

Introduce a `has<Capability>` attributeGroup when:

1. The same attribute set appears (or will appear) on **two or more** components.
2. The attributes have a coherent semantic name ("focusable attributes", "items source", "click-notifier attributes").

The group lives in the "Interfaces" block of the XSD (between `<!-- Interfaces -->` and `<!-- Utils -->`). Keep the surrounding alphabetical/categorical neighbourhood (identity → text → input → layout/sizing).

Don't create `has<X>` groups for **single-component-only** attributes — keep those inline on the component.

---

## 11. Registering a new top-level component

Two places to update:

1. The complexType in the components section.
2. The `<xs:group name="standardComponent">` `<xs:choice>` at the bottom of the file — add `<xs:element name="..." type="..."/>` to register the element name in the layout namespace.

After both edits, the component is usable inside any layout container.

---

## 12. Don't-do list

- **Don't** re-declare attributes that are already inherited via the extension chain — it currently causes `Duplicate attribute use` warnings on strict XSD validators. (See `textFieldComponent` / `emailFieldComponent` / `imageComponent` re-declaring `hasDataContainer` as a pre-existing example to avoid copying.)
- **Don't** use `<xs:restriction>` without `base="xs:string"` (or another base). xmllint rejects it; Xerces accepts it but emits warnings.
- **Don't** use a single-member `<xs:union>` unless you mean it as an alias. For a plain restricted enum, use `<xs:restriction base="xs:string">`.
- **Don't** wrap every text attribute in `<xs:string>` — prefer `resourceString` so `msg://`-style i18n tokens validate correctly.
- **Don't** add `<xs:any>` inside a component body without a `namespace="##other"` constraint — that would silently allow any element from this namespace and undermine validation. The legitimate use is `<xs:anyAttribute namespace="##other" processContents="lax"/>` (already in `baseComponent`) and `<xs:any namespace="##other">` slots in `conditionType`, `formLayoutComponent`, `formRowType`, `urlQueryParametersType`, `facets`.
- **Don't** duplicate icon-slot choices inline. Use `iconComponentElement` as the type of the slot element, or `<xs:group ref="iconComponentGroup"/>` inside a sequence.
