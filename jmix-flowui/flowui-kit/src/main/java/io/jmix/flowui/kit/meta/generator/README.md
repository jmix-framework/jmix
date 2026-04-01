# Studio Meta Generator

This package contains internal tooling for generating Studio meta-annotations from Flow UI XSD schemas.

The generator is intended for framework developers who add or update XSD elements and then need a starting point
for `@StudioComponent`, `@StudioElement`, `@StudioAction`, `@StudioFacet` or `@StudioDataComponent`
descriptions.

## Main Classes

- `StudioMetaDescriptionGenerator` parses XSD files, resolves referenced types and attributes, infers a meta kind
  and renders Java source with `properties`, `propertyGroups`, TODO comments and child element hints.
- `StudioPropertyGroupsMatcher` scans existing `@StudioPropertyGroup` declarations and adds only exact
  `propertyGroups` matches.
- `StudioXsdElementCandidate` describes a concrete XSD element match together with its context.
- `StudioMetaGenerationResult` contains the generated source, output path and collected TODOs.

## Programmatic Usage

Typical flow:

1. Resolve the workspace root with `StudioMetaDescriptionGenerator.detectWorkspaceRoot(...)`.
2. Create `StudioMetaDescriptionGenerator`.
3. Find matching XSD elements with `findElementCandidates(...)`.
4. Pick the exact `StudioXsdElementCandidate`.
5. Compute the default output path with `getDefaultOutputPath(...)` or provide your own path.
6. Call `generate(...)` to preview the source or `write(...)` to create or update the Java file.

Example:

```java
Path workspaceRoot = StudioMetaDescriptionGenerator.detectWorkspaceRoot(Path.of("").toAbsolutePath());
StudioMetaDescriptionGenerator generator = new StudioMetaDescriptionGenerator(workspaceRoot);

Path schemaPath = workspaceRoot.resolve("jmix/jmix-flowui/flowui/src/main/resources/io/jmix/flowui/view/layout.xsd");
StudioXsdElementCandidate candidate = generator.findElementCandidates(schemaPath, "button").get(0);

Path outputPath = generator.getDefaultOutputPath(candidate);
StudioMetaGenerationResult result = generator.write(candidate, StudioMetaKind.AUTO, outputPath);
```

## CLI

`StudioMetaDescriptionGenerator.Cli` is a thin command-line wrapper over the API.
It is not packaged as a separate distribution and is usually started from IDE run configuration or from a custom
Java launch with the `flowui-kit` runtime classpath.

Example:

```bash
java <runtime-classpath> io.jmix.flowui.kit.meta.generator.StudioMetaDescriptionGenerator\$Cli \
  --root /path/to/jmix-all \
  --xsd /path/to/layout.xsd \
  --element button \
  --output /path/to/StudioButtonGenerated.java \
  --kind auto
```

Arguments:

- `--root` points to the workspace root.
- `--xsd` points to the source schema.
- `--element` is the target XSD element name.
- `--output` is the file to create or update.
- `--kind` overrides automatic kind detection. Supported values: `auto`, `component`, `element`, `action`,
  `facet`, `data-component`.
- `--list-schemas` prints discovered schemas under the workspace root. In this mode only `--root` is required.

If several XSD elements share the same name, the CLI asks to choose the exact candidate by number.

## Generated Output

The generator intentionally keeps the output conservative.

- Existing inline `properties` are always generated.
- `propertyGroups` are added only for exact matches against existing groups.
- Uncertain values are left as TODO comments instead of guessing.
- Child element declarations are reported as TODO comments when nested meta descriptions may also be needed.

Generated source is a starting point for manual review, not a final replacement for hand-written meta descriptions.

## Tests

Integration-style tests for this package are located in the `flowui` module:

- `jmix-flowui/flowui/src/test/groovy/io/jmix/flowui/kit/meta/generator`

They validate generation against existing framework meta descriptions and dedicated test fixtures.
