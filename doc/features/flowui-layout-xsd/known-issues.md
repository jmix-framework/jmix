# Known Issues in `layout.xsd`

Pre-existing XSD-compliance issues that were intentionally left unfixed. Listed here so future agents can pick them up without re-analysing the whole file.

All issues are visible only under a strict XSD 1.0 validator (e.g. `xmllint --schema`). IntelliJ IDEA and the Xerces parser used at Jmix runtime accept the current XSD, so view-XML validation works in IDEs and at boot.

---

## 1. `xs:all` + `xs:extension` in the validator type hierarchy

### Where

`baseValidatorType` uses `<xs:all>` as its content model. Several types inherit from it via `<xs:extension base="baseValidatorType">`:

- `collectionValidatorType`
- `dateValidatorsType`
- `stringValidatorType` (chain root for the constraint hierarchy)
- `bigDecimalFieldValidatorType`
- `numberFieldValidatorType`
- `integerFieldValidatorType`
- `constraintValidatorType` (extends `stringValidatorType`)

### Why xmllint complains

XSD 1.0 forbids placing an `<xs:all>` model group inside any other model group. When a complex type with `<xs:all>` is extended, the resulting effective content is conceptually `<xs:sequence><xs:all>…</xs:all> + …</xs:sequence>`, which violates the rule. XSD 1.1 dropped this restriction; Xerces in lax mode does too. xmllint follows XSD 1.0 strictly and refuses to compile the schema.

### Why it is not fixed

The intent behind `<xs:all>` here is correct: validator child elements (`notNull`, `custom`, `notBlank`, `regexp`, etc.) should be writable in any order inside `<validators>`. Switching to `<xs:sequence minOccurs="0">` would compile under strict 1.0 but would impose a fixed order on existing view-XML — a behavioural change we want to avoid.

### What it costs

Strict XSD 1.0 tools can't validate any view-XML that contains a `<validators>` element. IDEs (IntelliJ) and the Jmix runtime are unaffected.

### Possible future directions

- Wait for full XSD 1.1 adoption across tooling and keep the current shape.
- Drop the `xs:extension` chain among validators: each leaf validator (`stringValidatorType`, `numberFieldValidatorType`, …) would inline its own `<xs:all>` listing all allowed rule elements. Removes the conflict but explodes duplication — every leaf would re-declare `notNull` + `custom`. Probably worth doing only with code-gen.
- Accept the strict-validator cost and migrate to `<xs:sequence minOccurs="0">` after an audit confirming no view-XML relies on ordering.

---

## 2. `imageComponent` redeclares `themeNames` and `hasDataContainer`

### Where

```xml
<xs:complexType name="imageComponent">
    <xs:complexContent>
        <xs:extension base="imageHtmlComponent">
            <xs:attributeGroup ref="hasDataContainer"/>
            <xs:attribute name="themeNames" type="imageThemeNames"/>
        </xs:extension>
    </xs:complexContent>
</xs:complexType>
```

The inheritance chain:

```
baseHtmlComponent
└── baseHtmlContainer            (declares themeNames="badgeThemeNames" + hasDataContainer)
    └── baseClickableHtmlContainer
        └── imageHtmlComponent
            └── imageComponent   (redeclares themeNames + hasDataContainer)
```

So `imageComponent` triggers three duplicate-attribute errors: `property` and `dataContainer` (from the duplicate `hasDataContainer` group reference) and `themeNames` (from re-declaring the attribute with a different type).

### Why it is not fixed

Two distinct sub-issues are entangled here:

1. **`hasDataContainer` duplicate.** Easy to remove on its own — `imageComponent` inherits the group from `baseHtmlContainer`.
2. **`themeNames` type narrowing from `badgeThemeNames` to `imageThemeNames`.** This is a deliberate attempt to narrow the type of an inherited attribute. XSD 1.0 allows attribute-type narrowing only via `xs:restriction`, not `xs:extension`. The current code uses extension, so the parser sees two attributes with the same name and different types — a hard conflict.

   Untangling this would require either:
   - Removing the `themeNames="badgeThemeNames"` default from `baseHtmlContainer` (a behaviour change — every plain HTML container like `div`, `span`, `h1`…`h6`, `p`, `pre` would lose its `themeNames` attribute), or
   - Rewriting `imageComponent` via `xs:restriction` (much more verbose — restriction requires re-stating every inherited attribute), or
   - Generalising the `themeNames` attribute on `baseHtmlContainer` to `xs:string` so subtypes can re-declare it freely (loses validation of allowed badge values for the containers that should still use them).

   None of these are zero-risk; the call deferred until we are ready for a deeper refactor of HTML-container theming.

### What it costs

Same as #1 — strict validators reject the schema; IDEs and Jmix runtime accept it.

### Possible future directions

- Remove `themeNames="badgeThemeNames"` from `baseHtmlContainer` (preferred, but a behaviour change) and then drop the explicit redeclaration in `imageComponent`. The `unorderedListHtmlContainer` had a similar redundant redeclaration already removed in the earlier refactor.
- If the goal is to also kill the `hasDataContainer` duplicate without touching `themeNames`, that single fix can be done in isolation — but the `themeNames` conflict will remain visible to strict validators, so the value of the partial fix is limited.

---

## Quick reference

| Issue | Affected types | Error class | Behaviour change to fix? |
|---|---|---|---|
| 1 | validator type hierarchy | `xs:all` cannot be inside `xs:sequence` from extension | Yes — forces element ordering |
| 2 | `imageComponent` | duplicate `themeNames` / `dataContainer` / `property` | Yes — removes `themeNames` from plain HTML containers, OR requires `xs:restriction` rewrite |
