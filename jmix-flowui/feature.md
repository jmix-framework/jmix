# View Templates Feature Context

We are developing a new Jmix feature called View Templates.

## Goals

The goal is to save the developer from creating list and detail views for an entity at design time. The views are created by the framework at runtime using the provided templates.

## Code location

- `jmix-flowui/flowui` is the main codebase of the feature
- `sample-app` folder contains a sample project that demonstrates and tests the new feature

## Key decisions

- View templates use `ComponentXmlFactory` to generate XML snippets for data-bound components from entity metadata.
- `ComponentXmlFactory` follows the default component generation strategy for supported single-value properties, but it always emits the default XML component/action set and does not honor application-level `jmix.ui.component.entity-field-*` overrides.
- Embedded attributes are ignored by XML generation, while collection-valued attributes are treated as unsupported.

## Implementation details

- `ComponentXmlFactory#createComponentXml(MetaProperty, @Nullable String)` returns a parseable Flow UI component XML fragment with `id="<propertyName>Field"` and `property="<propertyName>"`; it also adds `dataContainer="<dataContainerId>"` when a data container id is provided.
- Datatype properties map to `textField`, `textArea` for `@Lob String`, `checkbox`, `datePicker`, `timePicker`, `dateTimePicker`, `fileStorageUploadField`, or `fileUploadField` according to the same Java type rules as `DefaultComponentGenerationStrategy`. Enum properties map to `select`.
- Single-value association properties map to `entityPicker` with `entity_lookup` and `entity_clear` actions. Single-value composition properties map to `entityPicker` with `entity_openComposition` and `entity_clear` actions.
- Tests for the XML factory are in `flowui/src/test/groovy/view_template/ComponentXmlFactoryTest.groovy`, with a metadata-only datatype coverage entity in `flowui/src/test/java/test_support/entity/viewtemplate/ComponentXmlDatatypesEntity.java`.
