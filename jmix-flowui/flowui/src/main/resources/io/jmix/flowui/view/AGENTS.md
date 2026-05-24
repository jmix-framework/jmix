# layout.xsd — Agent Guidelines

When modifying `layout.xsd` in this directory, check whether the change should also be reflected in `doc/features/flowui-layout-xsd/` (paths relative to the repository root). Update the docs when the change introduces or alters something that agents use as a reference when working on the XSD.

## What to update and where

| Change in `layout.xsd` | File to update |
|---|---|
| New or renamed `attributeGroup` (`has<Capability>`, `requires<Capability>`) | `reusable-types.md` §1 — add a row to the appropriate table |
| New or changed base `complexType` (inheritance chain) | `reusable-types.md` §2 — extend the relevant ASCII tree and description |
| New `simpleType` enum, alias, or theme-name type | `reusable-types.md` §3 — add a row to the appropriate table |
| New `xs:group` (element group) | `reusable-types.md` §4 — add a row to the group table |
| New reusable sub-element `complexType` | `reusable-types.md` §5 — add a row to the sub-element table |
| New type-naming convention or deviation from it | `reusable-types.md` §6 |
| New component-construction pattern or "don't do" rule | `component-patterns.md` — add or update the relevant section |
| New decision rule for base-type selection | `component-patterns.md` §1 decision tree |

## When NOT to update docs

- Pure bug fixes (e.g. correcting a typo in an attribute value, fixing a duplicate attribute warning) that do not change the public XSD surface.
- Changes to component-specific attributes that are already covered by an existing pattern — no new building block is introduced.
- Cosmetic reformatting of the XSD with no semantic change.

## Doc accuracy rule

The docs describe the XSD as the source of truth. If a doc section is already wrong (e.g. it references a type that was renamed), fix the stale entry as part of the same change.
