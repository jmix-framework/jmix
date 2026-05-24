# layout.xsd — Agent Guidelines

`doc/features/flowui-layout-xsd/` (paths relative to the repository root) is the agent-facing reference for `layout.xsd`. Use it both as input *before* editing and as output *after* editing.

## Before editing `layout.xsd` — read the docs first

When asked to add a new component, extend an existing one, introduce a new attribute, or otherwise modify `layout.xsd`, read the reference docs **before writing any XSD**. They tell you which reusable building block to apply instead of inventing a new one.

| If the task is to… | Read first |
|---|---|
| Add a new component | `component-patterns.md` §1 (decision tree for picking a base type) + §2/§3 (worked examples) + §11 (registering in `standardComponent`) |
| Add a new attribute to an existing component | `reusable-types.md` §1 (attribute groups) — check whether the attribute already exists as part of a `has<X>` group before declaring it inline |
| Add a new enum/simpleType | `reusable-types.md` §3 + `component-patterns.md` §7 (decision rule: reuse vs. create vs. alias via `xs:union`) |
| Add a new theme-names type | `component-patterns.md` §5 (shape for new theme types, when to alias an existing one) |
| Add validation rules to a field | `reusable-types.md` §2 (validator hierarchy) + `component-patterns.md` §6 |
| Add an icon slot | `reusable-types.md` §5 — use `iconComponentElement` / `iconComponentGroup`, don't redefine inline |
| Add a child container slot | `reusable-types.md` §4 (`layoutOrComponent` / `singleLayoutOrComponent` / `singleLayoutOrComponentType`) |
| Add an action type | `reusable-types.md` §2 (action types) + `component-patterns.md` §9 |
| Anything else touching the XSD | At minimum skim `component-patterns.md` §12 (don't-do list) and `known-issues.md` |

If a reusable definition fits — **use it** (`<xs:extension base="…">`, `<xs:attributeGroup ref="…">`, `<xs:group ref="…">`, `type="…"`). Only introduce a new `simpleType` / `complexType` / `attributeGroup` when no existing one matches; in that case follow the naming conventions in `reusable-types.md` §6.

## After editing `layout.xsd` — update the docs

When the change introduces or alters something that agents use as a reference, update the docs in the same change.

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
