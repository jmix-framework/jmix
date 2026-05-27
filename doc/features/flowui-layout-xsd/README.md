# FlowUI Layout XSD — Reference for Agents

Reference material for agents working on `jmix-flowui/flowui/src/main/resources/io/jmix/flowui/view/layout.xsd` — the XSD that defines the `http://jmix.io/schema/flowui/layout` namespace used in Jmix view XML.

The goal: when adding a new component, extending an existing one, or defining a new attribute, **reuse existing building blocks first**. New `simpleType`/`complexType`/`attributeGroup` definitions should be introduced only when nothing existing fits.

Read the docs in this order:

1. [reusable-types.md](reusable-types.md) — catalog of the reusable definitions in the XSD: attribute groups (capability mix-ins), base complex types (inheritance chains), shared enums, shared element groups, validators, formatters, renderers.
2. [component-patterns.md](component-patterns.md) — how to apply the catalog when adding a component, extending an existing one, or deciding whether to introduce a new type.
3. [known-issues.md](known-issues.md) — pre-existing strict-XSD-validator complaints that were intentionally left unfixed, with context for future cleanup passes.

Both reference docs use real definitions from `layout.xsd` as the source of truth — when in doubt, the XSD wins.
