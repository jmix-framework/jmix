# View Templates Feature Context

We are developing a new Jmix feature called View Templates.

## Goals

The feature allows developers to skip design-time creation of standard list and detail views for an entity. The framework generates these views at runtime from templates declared on the entity.

## Code location

- `jmix-flowui/flowui` is the main codebase of the feature.
- `sample-app` can be used for manual verification, but the main automated coverage is in `jmix-flowui/flowui/src/test`.

## Key decisions

- We considered implementing template views with shared generic controllers plus a `_jmix_view_id` query parameter and request-scoped context.
  - We did not keep that approach. The final implementation generates a dedicated controller class for each template view by using ByteBuddy.
  - The reason is integration with normal Jmix and Vaadin class-based view infrastructure. The controller class itself must carry the effective `@ViewController`, `@ViewDescriptor`, and `@Route` metadata so view lookup, route resolution, menu navigation, create/edit navigation, and direct instantiation work without special query-parameter plumbing.
- `TemplateListView` and `TemplateDetailView` remain as generic base classes only. Generated subclasses add the runtime annotations and are what `ViewRegistry` registers.
  - We also kept descriptor generation in memory through `ViewTemplateDescriptorRegistry` instead of creating descriptor files on disk.

## Implementation details

### Public API

- `io.jmix.flowui.view.template.ListViewTemplate`
- `io.jmix.flowui.view.template.DetailViewTemplate`

Both annotations are meta-annotations, so the framework reads them from entity metadata rather than from direct Java reflection on the entity class.

### Annotation attributes

Supported attributes on both annotations:

- `path`: resource path to a Freemarker XML descriptor template.
- `templateParams`: JSON object with additional template parameters.
- `parentMenu`: parent menu id for the generated menu item. If empty, no menu item is created.
- `viewId`: generated view id.
- `viewRoute`: generated route path.
- `viewTitle`: generated view title.

Resolved defaults:

- List view id: `<entityName>.list`
- Detail view id: `<entityName>.detail`
- List view title: `<entityName> list`
- Detail view title: `<entityName>`

Route rules:

- For list views, `viewRoute` is used as-is when non-empty.
- For detail views, `viewRoute` is the base route only. The framework always appends `/:id`.
- If detail `viewRoute` already ends with `/:id`, definition loading fails with `IllegalArgumentException`.
- If `viewRoute` is empty, the framework derives the route from `viewId` by lowercasing it, replacing non-alphanumeric characters with `-`, and trimming leading or trailing separators.

### Template model

When rendering the descriptor template, the framework provides:

- `entityMetaClass`: entity `MetaClass`
- `viewTitle`: resolved title from annotation or default
- `componentXmlFactory`: `io.jmix.flowui.view.template.impl.ComponentXmlFactory`
- `templateHelper`: `io.jmix.flowui.view.template.ViewTemplateHelper`

The framework also parses `templateParams` as a JSON object and merges its entries into the template model.

Built-in templates additionally recognize these top-level `templateParams` entries and pass them to the helper:

- `includeProperties`: JSON array of direct property names to include explicitly
- `excludeProperties`: JSON array of direct property names to exclude explicitly

### Descriptor generation

- Default descriptor templates are:
  - `flowui/src/main/resources/io/jmix/flowui/view/template/list-view.ftl`
  - `flowui/src/main/resources/io/jmix/flowui/view/template/detail-template.ftl`
- The built-in templates delegate property selection to `templateHelper.getProperties(entityMetaClass, includeProperties![], excludeProperties![])`.
- `ViewTemplateDefinitions` renders the descriptor first and stores it in `ViewTemplateDescriptorRegistry`.
- Descriptors are stored under synthetic paths with prefix `view-template:`.
- `ViewXmlLoader` recognizes these synthetic paths and loads the descriptor from the in-memory registry instead of the classpath.

#### Template helper property filtering

- `ViewTemplateHelper` is an experimental public API intended for Freemarker view templates.
- The default implementation is `io.jmix.flowui.view.template.impl.DefaultViewTemplateHelper`.
- `getProperties(MetaClass, List<String>, List<String>)` returns direct single-value properties in metadata order.
- Supported property kinds:
  - datatype properties
  - enum properties
  - single-value associations
  - single-value compositions
- Unsupported property kinds:
  - collection-valued properties
  - embedded properties
- Default exclusions:
  - `@SystemLevel` properties
  - `@Secret` properties
  - properties annotated with `@Id`, `@Version`, `@JmixGeneratedValue`
  - properties annotated with `@CreatedBy`, `@CreatedDate`, `@LastModifiedBy`, `@LastModifiedDate`
  - properties annotated with `@DeletedBy`, `@DeletedDate`
- `includeProperties` can restore excluded direct supported properties.
- `excludeProperties` is applied last and wins over inclusion.
- Property paths are not supported. If `includeProperties` or `excludeProperties` contain a dotted name like
  `customer.name`, template rendering fails with `IllegalArgumentException`.

### Controller generation

- Runtime controller generation uses ByteBuddy (`net.bytebuddy:byte-buddy`).
- The framework keeps generic base classes:
  - `TemplateListView extends StandardListView<Object>`
  - `TemplateDetailView extends StandardDetailView<Object>`
- For each template definition, the framework generates a dedicated subclass in a package derived from the entity class package: `<entity-package>.generated_view`.
- Example: `com.company.foo.entity.Alpha` produces controllers under `com.company.foo.entity.generated_view`, such as `AlphaListView` and `AlphaDetailView`.
- Each generated controller class is annotated at runtime with:
  - `@ViewController(id = ...)`
  - `@ViewDescriptor(path = ...)`
  - `@Route(value = ..., layout = DefaultMainViewParent.class)`

### View registration

- `ViewTemplateDefinitions` loads all template definitions lazily and caches them.
- `ViewRegistry` adds generated views to the standard `ViewInfo` registry during initialization.
- Generated list and detail views are also registered as primary views for their entity in `primaryListViews` and `primaryDetailViews`.

### Menu registration

- `MenuConfig` loads normal XML menu definitions first and then appends template-generated menu items.
- If `parentMenu` is empty, no menu item is created.
- If `parentMenu` points to an existing menu item, the generated item is added under it.
- If `parentMenu` is missing, `MenuConfig` creates a new root menu item with that id and title equal to the same string value.
- The generated child item title is the resolved `viewTitle`.
- Detail menu items include the standard route parameter `id=new`.

### XML component generation

- `ComponentXmlFactory#createComponentXml(MetaProperty, @Nullable String)` returns a parseable Flow UI component XML fragment with `id="<propertyName>Field"` and `property="<propertyName>"`.
- If a data container id is provided, it also adds `dataContainer="<dataContainerId>"`.
- Datatype properties map to the same default single-value component set as `DefaultComponentGenerationStrategy`.
- `@Lob String` maps to `textArea`.
- Enum properties map to `select`.
- Single-value associations map to `entityPicker` with `entity_lookup` and `entity_clear`.
- Single-value compositions map to `entityPicker` with `entity_openComposition` and `entity_clear`.
- Embedded attributes are ignored.
- Collection-valued attributes are treated as unsupported.
- The XML factory always emits the default XML component/action set and does not honor application-level `jmix.ui.component.entity-field-*` overrides.

## Tests

Main coverage added in this session:

- `flowui/src/test/java/view_template/ViewTemplateIntegrationTest.java`
  - verifies generated controller subclasses, view ids, descriptor paths, routes, menu items, navigation, and
    built-in template property filtering
- `flowui/src/test/groovy/view_template/ComponentXmlFactoryTest.groovy`
  - verifies XML generation rules
- `flowui/src/test/groovy/view_template/DefaultViewTemplateHelperTest.groovy`
  - verifies direct-property filtering, default exclusions, include/exclude overrides, metadata order preservation,
    and property-path rejection
