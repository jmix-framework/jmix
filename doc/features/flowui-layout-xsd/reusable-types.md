# Reusable Types in `layout.xsd`

Catalog of definitions that should be **referenced**, not duplicated, when adding or extending components in the FlowUI layout XSD. Grouped by intent.

XSD file: `jmix-flowui/flowui/src/main/resources/io/jmix/flowui/view/layout.xsd`.

---

## 1. Attribute groups (capability mix-ins)

Attribute groups model individual capabilities ("has X"). Compose them on a complex type to add the capability without re-declaring the attributes.

### Identity & lifecycle
| Group | Attribute(s) | Use on |
|---|---|---|
| `hasId` | `id` (optional) | Anything that may be referenced by id but is optional. |
| `requiresId` | `id` (required) | Items where id is mandatory (action items, menu items, tabs, form rows). |
| `hasEnabled` | `enabled` | Anything that can be disabled. |

### Text / labelling
| Group | Attribute(s) | Use on |
|---|---|---|
| `hasText` | `text`, `whiteSpace` | Components whose primary text is a label/content (buttons, HTML containers). |
| `hasLabel` | `label` | Form fields (the field caption shown to the side or above). |
| `hasTitle` | `title` | Browser-tooltip-style title attribute. |
| `hasPlaceholder` | `placeholder` | Editable fields with placeholder hint. |
| `hasHelperText` | `helperText` | Form fields with helper text under the input. |
| `hasAriaLabel` | `ariaLabel`, `ariaLabelledBy` | Anything that needs explicit accessibility labelling. |

### Icons & shortcuts
| Group | Attribute(s) | Use on |
|---|---|---|
| `hasIcon` | `icon` (typed `jmixFontIconEnum`) | Components/actions that accept a built-in icon by name. |
| `hasShortcutCombination` | `shortcutCombination` | Actions/buttons that support a keyboard shortcut. |
| `hasFocusableAttributes` | `tabIndex`, `focusShortcut` | Anything focusable. |
| `hasClickNotifierAttributes` | `clickShortcut` | Anything click-notifying. |

### Data binding & items
| Group | Attribute(s) | Use on |
|---|---|---|
| `hasDataContainer` | `dataContainer`, `property` | Fields/containers bound to a data container property. |
| `hasItems` | `itemsContainer`, `itemsEnum` | Selection components whose option list comes from a collection container or an enum class. |
| `hasValueAndElement` | `readOnly` | Editable components with read-only mode. |
| `hasValueChangeMode` | `valueChangeTimeout`, `valueChangeMode` | Fields that need value-change-mode tuning (EAGER/LAZY/...). |
| `hasValidation` | `errorMessage` | Components that can show a validation error. |
| `hasSecurityConstraint` | `constraintEntityOp` | Actions/components needing entity-op security gating. |

### Input behaviour
| Group | Attribute(s) | Use on |
|---|---|---|
| `hasRequired` | `required`, `requiredMessage` | Fields that may be marked required. |
| `hasTrimming` | `trimEnabled` | Text fields with optional whitespace trimming. |
| `hasAutocomplete` | `autocomplete` | Input fields exposing HTML autocomplete hint. |
| `hasAutocapitalize` | `autocapitalize` | Input fields exposing HTML autocapitalize. |
| `hasAutocorrect` | `autocorrect` | Input fields with autocorrect toggle. |

### Layout / styling
| Group | Attribute(s) | Use on |
|---|---|---|
| `hasSize` | `width`, `height`, `min/maxWidth`, `min/maxHeight` | Anything sizeable. |
| `hasClassNames` | `classNames` | Anything stylable via CSS class names. |
| `hasOverlayWidth` | `overlayWidth` | Pickers / dropdowns that open an overlay. |
| `hasThemable` | `spacing`, `margin`, `padding`, `boxSizing` | Layouts. |
| `hasFlexible` | `alignItems`, `justifyContent`, plus `hasSize`/`hasEnabled`/`hasClassNames` | Flex layouts (already composes the layout-relevant size/enable/classes). |

### Misc
| Group | Use on |
|---|---|
| `columnsLoadType` | `includeAll`, `exclude` for the `columns` element of `dataGrid`. |

---

## 2. Base complex types (inheritance chains)

Use `xs:extension base="..."` to inherit a chain of attributes rather than re-declaring them. The main chains:

### Plain components

```
baseComponent
├── (most components extend this directly)
```

`baseComponent` provides: `hasId`, `visible`, `colspan`, `alignSelf`, `justifySelf`, `css`, plus `<xs:anyAttribute namespace="##other">` for XSD-extension across schemas. **Every visible component should ultimately extend `baseComponent`.**

### Form fields

```
baseComponent
└── baseFieldComponent              (+ size, label, enabled, classes, helperText, dataContainer, valueAndElement)
    └── validatableBaseFieldComponent  (+ <validators> element, hasValidation)
        ├── baseTextFieldComponent      (+ tooltip/prefix/suffix slots, value, autofocus, autoselect, themeNames=textFieldThemeNames, clearButtonVisible,
        │                                  hasTitle/Placeholder/Autocorrect/Autocomplete/Autocapitalize/ValueChangeMode/Focusable/AriaLabel)
        │   ├── textFieldComponent
        │   ├── emailFieldComponent
        │   ├── passwordFieldComponent
        │   ├── numberFieldComponent
        │   ├── integerFieldComponent
        │   └── bigDecimalFieldComponent
        ├── valuePickerComponent      (+ actions element, formatter, prefix/suffix, autofocus, allowCustomValue, title/focusable/required/placeholder/ariaLabel)
        │   └── entityPickerComponent  (+ metaClass)
        ├── multiSelectComboBoxComponent
        ├── timePickerComponent / datePickerComponent / dateTimePickerComponent
        ├── baseSingleFilterComponent (filter base)
        │   ├── baseJpqlFilterComponent
        │   │   └── jpqlFilterComponent
        │   └── basePropertyFilterComponent
        │       └── propertyFilterComponent
        └── (others)
```

`baseComboBoxPickerComponent` is a parallel branch (extends `baseComponent` directly, not the field chain) used by `entityComboBoxComponent`.

### HTML containers

```
baseComponent
└── baseHtmlComponent              (+ size, title, classes)
    └── baseHtmlContainer           (+ singleLayoutOrComponent group, themeNames, text, enabled, dataContainer)
        ├── baseClickableHtmlContainer (+ clickShortcut)
        │   ├── unorderedListHtmlContainer / orderedListHtmlContainer
        │   ├── imageHtmlComponent
        │   ├── htmlObjectComponent
        │   ├── sectionHtmlComponent
        │   └── (h1..h6, div, span, p, pre, header, footer, aside, article, ... via the standardComponent group)
        ├── htmlContainerWithAria
        │   └── clickableHtmlContainerWithArea
        │       └── nativeButtonComponent
        └── labelComponent / fieldSetHtmlComponent
```

Pick the right entry point: clickable HTML element → `baseClickableHtmlContainer`; with explicit aria → `htmlContainerWithAria`/`clickableHtmlContainerWithArea`; plain leaf (hr, etc.) → `baseHtmlComponent`.

### Actions

```
baseAction                  (+ properties/shortcutCombination/icon sub-elements; type/visible/text/description/actionVariant/hasIcon/hasShortcutCombination/requiresId/hasEnabled)
├── viewAction
├── gridAction
├── pickerAction
├── genericFilterAction
└── securityConstraintAction  (+ hasSecurityConstraint)

noShortcutBaseAction        (same surface MINUS shortcutCombination element/group — for items in menus/dropdowns where shortcuts aren't applicable)
├── dropdownButtonAction
└── userMenuAction
```

The empty-extension actions (`viewAction`, `gridAction`, `pickerAction`, `genericFilterAction`) exist on purpose: they preserve a distinct element name in different containers, even though attribute surface is identical. **Do not "consolidate" them.**

### Icons & icon slots

```
baseIconComponent (+ tooltip slot, color, size, hasClassNames, hasClickNotifierAttributes)
├── iconComponent       (Vaadin icon — required icon enum attribute)
├── svgIconComponent    (symbol + resource)
└── fontIconComponent   (iconClassNames / fontFamily / charCode / ligature)
```

For elements that accept an icon as a *child* (e.g. `<icon>`/`<svgIcon>`/`<fontIcon>`/`<image>` inside a button), use **`iconComponentElement`** as the type and let `iconComponentGroup` produce the choice. Do not redefine the choice inline.

### Tabs

```
baseTabsComponent (+ tab children, hasClassNames, hasSize)
├── tabsContainer    (+ orientation, themeNames=tabsThemeNames)
└── tabSheetComponent (+ prefix/suffix, lazyTabComponent children, themeNames=tabSheetThemeNames)
```

### Dropdown / combo buttons

```
baseDropdownButtonComponent (+ items element with actionItem/componentItem/textItem/separator, icon, openOnHover, themeNames=dropdownButtonThemeNames; hasSize/Text/Icon/Focusable/Title/Classes/Enabled)
├── dropdownButtonComponent (+ dropdownIndicatorVisible)
└── comboButtonComponent    (+ dropdownIcon slot + attr, action, hasShortcutCombination)
```

### Filters

```
baseSingleFilterComponent (extends baseComponent; layoutOrComponent child + tooltip + validators; parameterName, label/validation/required/valueAndElement/focusable/ariaLabel; labelVisible, themeNames, defaultValue)
├── baseJpqlFilterComponent     (+ <condition> element, parameterClass required, hasInExpression)
│   └── jpqlFilterComponent
└── basePropertyFilterComponent (+ property required, operation required, operationsList, operationEditable, operationTextVisible)
    └── propertyFilterComponent
```

```
baseGroupFilterComponent (+ responsiveSteps + nested group/property/jpql filters; operation required; summaryText; operationTextVisible)
└── groupFilterComponent (+ dataLoader required, autoApply)
```

### Login

```
baseLoginComponent (+ form/errorMessage/additionalInformation sub-elements; forgotPasswordButtonVisible; hasEnabled/Classes)
├── loginFormComponent  (+ <form type=jmixLoginFormType>, rememberMeVisible, localesVisible)
└── loginOverlayComponent (+ header/customFormArea/footer, opened)
```

`loginFormType` → `jmixLoginFormType` adds `rememberMe`; both are content types for `<form>` slots.

### Upload

```
baseFieldComponent
└── baseUploadFieldComponent (+ tooltip & uploadIcon slots; many text-resource attrs for i18n; maxFileSize, acceptedFileTypes, uploadIcon, dropAllowed; hasRequired/Validation)
    ├── fileUploadFieldComponent       (+ fileName)
    └── fileStorageUploadFieldComponent (+ fileStoragePutMode, fileStorageName)
```

`uploadComponent` (the standalone uploader) extends `baseComponent` directly — different model from the upload *field*.

### Items query (combo box data sources)

```
baseComboBoxItemsQueryType (+ <query> element; searchStringFormat, escapeValueForLike)
├── comboBoxItemsQueryType                 (alias — no additions)
├── entityComboBoxItemsQueryType           (+ fetchPlan element/attr, class required)
└── multiSelectComboBoxItemsQueryType      (+ fetchPlan element/attr, class)
```

### Validators

```
baseValidatorType (notNull + custom)
├── collectionValidatorType  (+ size, notEmpty)
│   └── stringValidatorType  (+ notBlank, regexp, email)
│       └── constraintValidatorType (+ digits, decimalMin/Max, doubleMin/Max, sign predicates, max/min)
├── dateValidatorsType        (future, futureOrPresent, past, pastOrPresent)
├── bigDecimalFieldValidatorType
├── numberFieldValidatorType
├── integerFieldValidatorType
├── onlyCustomValidatorType  (only allows <custom>)
└── checkboxValidatorType    (only allows <custom>)
```

Each leaf validator element body type (`messageValidatorType`, `customValidatorType`, `dateValidatorType`, `sizeValidatorType`, `regexpValidatorType`, `digitsValidatorType`, `decimalMaxMinValidatorType`, `maxMinValidatorType`) is its own complexType — reuse them when adding validation rules.

### Formatters

```
formatterType (choice of: collection | custom | date | number)
├── customFormatterType        (bean required)
├── formattableFormatterType   (format)
│   └── dateFormatterType      (+ type, useUserTimezone)
```

### Grid renderers

`<xs:group name="renderers">` makes the choice of `numberRenderer | localDateRenderer | localDateTimeRenderer | detailLinkRenderer | detailButtonRenderer`. Refer to it via `<xs:group ref="renderers"/>` inside a column-like type.

```
formattableRendererType        (format required, nullRepresentation)
numberRendererType             (format, numberFormat, nullRepresentation)
detailRendererType             (viewId/viewClass; text/css/classNames)
├── detailLinkRendererType     (+ target)
└── detailButtonRendererType   (+ icon slot, openMode, themeNames=buttonThemeNames, hasIcon)
```

### Generic component / fragment

```
genericComponent      (+ <properties type=genericComponentProperties>, class required)
fragmentComponent     (same shape, + hasClassNames)
fragmentRendererElement (+ <properties>, class required) — used inline inside list/grid components
```

`genericComponentProperties` / `genericComponentProperty` are reusable for *any* "property bag" element with `name`/`value`/`type` triples (the value-type enum is `genericComponentPropertyValueType`).

---

## 3. Shared simple types (enums)

Reuse these instead of defining a fresh `xs:restriction base="xs:string"`.

### Layout / direction / alignment

| Type | Values |
|---|---|
| `orientationType` | `HORIZONTAL`, `VERTICAL` |
| `scrollDirection` | `HORIZONTAL`, `VERTICAL`, `BOTH`, `NONE` |
| `alignEnum` | `START`, `END`, `CENTER`, `STRETCH`, `BASELINE`, `AUTO` |
| `justifyContentModeEnum` | `START`, `END`, `CENTER`, `BETWEEN`, `AROUND`, `EVENLY` |
| `contentAlignmentEnum` | `START`, `END`, `CENTER`, `STRETCH`, `SPACE_BETWEEN`, `SPACE_AROUND` |
| `flexDirectionEnum` | `ROW`, `ROW_REVERSE`, `COLUMN`, `COLUMN_REVERSE` |
| `flexWrapEnum` | `NOWRAP`, `WRAP`, `WRAP_REVERSE` |
| `tooltipPosition` | 12 positions around the element |
| `sidePanelPositionType` | `TOP`, `RIGHT`, `BOTTOM`, `LEFT`, `INLINE_START`, `INLINE_END` |
| `boxSizingType` | `CONTENT_BOX`, `BORDER_BOX`, `UNDEFINED` |

> `rangeInputOrientationEnum` is intentionally kept as a named alias of `orientationType` (it mirrors a Java type). The XSD declares it as `<xs:union memberTypes="orientationType"/>`. Do not redefine the same values inline.

### Form labels

| Type | Values | Notes |
|---|---|---|
| `labelPositionType` | `ASIDE`, `TOP` | Used on `propertyFilter` / `jpqlFilter`. |
| `formLabelsPosition` | `ASIDE`, `TOP` | Used on `formLayout` and `responsiveStep`. Alias of `labelPositionType` via union. |

### Sizing

| Type | What it represents |
|---|---|
| `componentSize` | Empty string OR `100%`. Underlying base for `width`/`height`/`min*`/`max*`. |

### Resource strings (i18n)

`resourceString` accepts an arbitrary string OR an `msg://` token. **All user-visible text attributes** (`label`, `text`, `title`, `placeholder`, `helperText`, etc.) should use `resourceString` rather than `xs:string`.

### Time / date / shortcuts

| Type | What it represents |
|---|---|
| `timePickerStep` | Common step shortcuts: `900s`, `15m`, `20m`, `30m`, `2h`, ..., `12h`. Allows any string too. |
| `shortcutCombination` | Predefined tokens like `${SAVE_SHORTCUT}`, `${GRID_CREATE_SHORTCUT}`, plus any string. |

### Action / theming

| Type | Values |
|---|---|
| `actionVariant` | `DEFAULT`, `PRIMARY`, `DANGER`, `SUCCESS` |
| `actionTypeEnum` | Built-in action type names (`entity_clear`, `list_create`, `detail_save`, ...). |
| `openModeType` | `NAVIGATION`, `DIALOG`. Aliases: `userMenuViewItemOpenModeType`, `detailButtonRendererOpenModeType` (declared as unions over `openModeType`). |

### Icons

| Type | What it represents |
|---|---|
| `jmixFontIconEnum` | Union of `jmixSpecificIconEnum` (Jmix action icons) and `vaadinIconEnum` (the full Vaadin icon set). Use this everywhere an icon string attribute is exposed. |

### Field datatypes

| Type | Values |
|---|---|
| `fieldDatatypeEnum` | All scalar Jmix datatype tokens (`string`, `int`, `long`, `uuid`, `date`, `localDate`, `decimal`, `fileRef`, etc.). |
| `dateDatatypeEnum`, `timeDatatypeEnum`, `dateTimeDatatypeEnum` | Restricted subsets for the corresponding pickers. |

### Filters

| Type | Values |
|---|---|
| `groupFilterOperation` | `AND`, `OR` |
| `propertyFilterOperation` | Single operation token (`EQUAL`, `CONTAINS`, ...). |
| `propertyFilterOperationsList` | Empty OR `propertyFilterOperation` (intended as a whitespace-separated list of operations for an editable property filter). |
| `likeClauseEnum` | `NONE`, `CASE_SENSITIVE`, `CASE_INSENSITIVE` |
| `menuFilterFieldFilterMode` | `CASE_SENSITIVE`, `CASE_INSENSITIVE` |

### Grid

| Type | Values |
|---|---|
| `gridDropMode` | `BETWEEN`, `ON_TOP`, `ON_TOP_OR_BETWEEN`, `ON_GRID` |
| `gridSelectionMode` | `SINGLE`, `MULTI`, `NONE` |
| `gridColumnRendering` | `EAGER`, `LAZY` |
| `gridMultiSortPriority` | `APPEND`, `PREPEND` |
| `gridNestedNullBehaviorEnum` | `THROW`, `ALLOW_NULLS` |
| `gridColumnTextAlignEnum` | `START`, `CENTER`, `END` |
| `aggregationType` | `SUM`, `COUNT`, `AVG`, `MIN`, `MAX` |
| `aggregationPosition` | `TOP`, `BOTTOM` |
| `itemsQueryFetchPlanFetchMode` | `AUTO`, `UNDEFINED`, `JOIN`, `BATCH` |

### Specific component enums

| Type | Notes |
|---|---|
| `valueChangeModeEnum` | `EAGER`, `LAZY`, `TIMEOUT`, `ON_BLUR`, `ON_CHANGE` |
| `whiteSpaceEnum` | CSS `white-space` values |
| `anchorTarget` | `DEFAULT`, `SELF`, `BLANK`, `PARENT`, `TOP` |
| `autocompleteEnum` | The full HTML autocomplete token set |
| `autocapitalizeEnum` | `NONE`, `SENTENCES`, `WORDS`, `CHARACTERS` |
| `numberingType` | `NUMBER`, `UPPERCASE_LETTER`, ... — for ordered lists |
| `inputTypeEnum` | HTML `<input type>` values |
| `importanceTypeEnum` | `AUTO`, `HIGH`, `LOW` |
| `sandboxTypeEnum` | HTML iframe sandbox tokens |
| `formatterDateType` | `DATE`, `DATETIME` |
| `genericComponentPropertyValueType` | Empty / `CONTAINER_REF` / `LOADER_REF` / `ICON` |
| `onViewEventEnum` | `Init`, `BeforeShow`, `Ready` |
| `multiSelectComboBoxAutoExpandModeEnum` | `VERTICAL`, `HORIZONTAL`, `BOTH`, `NONE` |
| `markdownEditorMode` | `EDIT`, `PREVIEW` |
| `codeEditorMode`, `codeEditorTheme` | Long enums of supported ACE modes/themes |
| `fileStoragePutMode` | `MANUAL`, `IMMEDIATE` |
| `rangeInputOrientationEnum` | Alias of `orientationType` — see above |
| `userMenuViewItemOpenModeType`, `detailButtonRendererOpenModeType` | Aliases of `openModeType` — see above |

### Theme names

Each Vaadin-themable component has its own `*ThemeNames` simpleType. They all follow the same shape — `<xs:union>` of an empty string and an enumeration of allowed CSS theme tokens — so the attribute is optional and accepts only known values.

| Type | Component(s) |
|---|---|
| `buttonThemeNames` | `button`, `drawerToggle` |
| `gridColumnVisibilityThemeNames` | `gridColumnVisibility` (alias of `buttonThemeNames` via union) |
| `dropdownButtonThemeNames` | `dropdownButton`, `comboButton` |
| `comboBoxThemeNames` | combo-box-like fields |
| `selectThemeNames` | `select` |
| `textFieldThemeNames` | `textField`, `emailField`, `passwordField`, `numberField`, ... |
| `textAreaThemeNames` | `textArea` (alias of `textFieldThemeNames`) |
| `timePickerThemeNames` / `datePickerThemeNames` / `dateTimePickerThemeNames` | corresponding pickers |
| `progressBarThemeNames` | `progressBar` |
| `radioButtonGroupThemeNames` / `checkboxGroupThemeNames` | group fields |
| `markdownEditorThemeNames` / `richTextEditorThemeNames` | editors |
| `badgeThemeNames` | spans/badges (and currently inherited by `baseHtmlContainer.themeNames`) |
| `detailsThemeNames` | `details`, `genericFilter` (uses the same theme set) |
| `spacingThemeNames` | layout containers (via `componentLayout.themeNames`) |
| `tabThemeNames` / `tabsThemeNames` / `tabSheetThemeNames` | tab components |
| `cardThemeNames` | `card` |
| `splitThemeNames` | `split` |
| `imageThemeNames` | `image`, `imageIcon` |
| `avatarThemeNames` | `avatar` |
| `gridThemeNames` | `dataGrid`, `treeDataGrid` |
| `twinColumnThemeNames` | `twinColumn` |
| `userMenuThemeNames`, `userMenuItemThemeNames` | `userMenu`, user-menu items |
| `listMenuThemeNames`, `menuFilterFieldThemeNames` | side menu, menu filter field |

---

## 4. Shared element groups (`xs:group`)

Element groups bundle a `<choice>` of allowed children. Reference them via `<xs:group ref="..."/>` instead of inlining.

| Group | Produces |
|---|---|
| `iconComponentGroup` | Optional choice of `<icon>` / `<svgIcon>` / `<fontIcon>` / `<image>`. Used inside any component that has a child icon slot. |
| `htmlHeaders` | Choice of `<h1>` ... `<h6>`. |
| `htmlLists` | Choice of `<listItem>` / `<orderedList>` / `<unorderedList>`. |
| `renderers` | Column-cell renderer choice (`numberRenderer`, `localDateRenderer`, `localDateTimeRenderer`, `detailLinkRenderer`, `detailButtonRenderer`). |
| `standardComponent` | The full registry of HTML, layout, and component elements that can appear inside a layout. Add new top-level components here. |
| `layoutOrComponent` | Unbounded `<choice>` of `standardComponent` + `<xs:any namespace="##other">`. Use as the content of containers that accept multiple children. |
| `singleLayoutOrComponent` | Same as above but at most one child. Use for "slot" elements (e.g. `prefix`, `suffix`, `startSlot`, `media`). |

The corresponding *type* `singleLayoutOrComponentType` wraps `singleLayoutOrComponent` for use as an element's `type` attribute (e.g. card slots).

---

## 5. Common sub-element types

These small complex types are reused as the type of named child elements. Use them as-is when adding child slots:

| Type | Used as type of element(s) |
|---|---|
| `tooltipElement` | `<tooltip>` everywhere |
| `prefixOrSuffixComponent` | `<prefix>` / `<suffix>` slots in fields |
| `iconComponentElement` | `<icon>` slot inside buttons, items, etc. (see `iconComponentGroup`) |
| `actionProperties` / `actionProperty` | `<properties>` / `<property>` inside `baseAction` |
| `actionShortcutCombination` | `<shortcutCombination>` inside `baseAction` |
| `contextMenuType` / `contextMenuItemType` | `<contextMenu>` slot in grids |
| `dialogMode` | `dialogMode` settings element on screens |
| `responsiveStepsType` | `<responsiveSteps>` in `formLayout` and `genericFilter` |
| `formRowType` / `formItemType` | Children of `formLayout` |
| `gridAggregationType` | `<aggregation>` inside `dataGridColumnComponent` |
| `dataGridColumnComponent` / `dataGridEditorActionsColumn` / `gridEditorButtonType` | Inside `<columns>` of a data grid |
| `urlQueryParametersType` and its `*UrlQueryParametersType` variants | URL-query-parameter facet definitions |
| `dataLoadCoordinatorType`, `dataLoadCoordinatorTriggerType`, `onViewEventType`, `onContainerItemChangedType`, `onComponentValueChangedType` | Inside `<facets>` |
| `timerType`, `settingsFacetType`, `settingsComponentType` | Other facets |
| `filterConditions` / `filterConfigurations` / `filterConfiguration` | Inside `<genericFilter>` |
| `gridColumnVisibilityMenuItemType` | `<menuItem>` inside `gridColumnVisibility` |
| `userMenuBaseItemType` and descendants | Items inside `<userMenu>` |
| `dropdownActionItemType` / `dropdownComponentItemType` / `dropdownTextItemType` | Items inside `baseDropdownButtonComponent` |
| `conditionType` | `<condition>` inside JPQL filter (allows `##other` namespace content) |
| `viewActions`, `facets` | Top-level view sections |
| `rootLayout`, `baseLayout`, `componentLayout`, `hboxComponent`, `flexLayoutComponent` | Layout containers |

---

## 6. Type-naming conventions in this XSD

When you do need to introduce something new, follow these conventions so it fits with the rest of the file:

| Kind | Convention | Example |
|---|---|---|
| Component complex type | `<componentName>Component` | `progressBarComponent` |
| Container complex type | `<containerName>Container` | `scrollerContainer` |
| Layout complex type | `<layoutName>Layout` or `*Component` | `flexLayoutComponent` |
| Action complex type | `<actionName>Action` or `*Type` | `dropdownButtonAction` |
| Capability mix-in | `has<Capability>` attributeGroup | `hasHelperText` |
| Required-version capability | `requires<Capability>` | `requiresId` |
| Element-body type (no top-level element) | `<purpose>Type` | `tooltipPosition`, `loginHeaderType` |
| Enum simple type | `<purpose>Enum` (preferred) or `<purpose>Type` (legacy) | `valueChangeModeEnum`, `boxSizingType` |
| Theme-name enum | `<componentName>ThemeNames` | `comboBoxThemeNames` |
| Validator type | `<form>ValidatorType` | `stringValidatorType` |
| Renderer type | `<purpose>RendererType` | `numberRendererType` |
