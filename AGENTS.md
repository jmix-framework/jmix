# Jmix Coding Guidelines

## Project Structure & Module Organization
- Multi-module Gradle workspace for Jmix development.
- Standard Gradle layouts (`src/main`, `src/test`) per module.
- BOM for shared dependencies and Jmix modules in `jmix-bom`.
- Gradle plugin used by applications in `jmix-gradle-plugin`.
- Gradle plugin to build Jmix itself in `jmix-build`.

## Build and Test Commands
- `./gradlew build` — build all modules and run checks.
- `./gradlew test` — run tests across modules.
- `./gradlew :<module-name>:test` — run tests of a single module. Take `<module-name>` from `settings.gradle`, for example `data` for `jmix-data/data` module. It means that you must execute `./gradlew :data:test` instead of `./gradlew :jmix-data:data:test`.  
- Tests of REST API are located in `jmix-rest/sample-rest` module and executed with `./gradlew :sample-rest:test`.

## Agent Notes
- Use `jetbrains` MCP to check file problems with `get_file_problems("path/to/file.ext", onlyErrors=false)`.
- Add imports instead of using fully qualified class names.
- Do not add comments or Javadocs to the files that you didn't change.
- Use current year in copyright headers for new files, e.g. `Copyright 2026 Haulmont`.

## Coding Style (Java)
- Use IntelliJ default formatting. Max line length 120 (soft), method ~50 lines, class ~500 lines.
- Declaration order: constants, fields, constructors, methods, inner interfaces, inner classes. Static members first.
- Add comments only for non-trivial logic; proper English, start with a capital and end with a period.
- Use blank lines between logical blocks; keep blocks compact.
- `final` only where required, not everywhere.
- Streams: each method after `stream()` on a new line unless it fits in one line.

## Naming Conventions
- Classes: CamelCase even for abbreviations (`RdbmsStore`, `IdProxy`).
- Internal helper methods: suffix `Internal`; avoid `_` prefixes.
- Beans: explicit `@Component("module_BeanName")`. Do not use `NAME` constants. Prefer `Support` over `Helper`/`Utils`.
- Logger constant name: `log`.
- Accepted abbreviations for local vars: `em` (EntityManager), `tx` (Transaction) , `lc` (LoadContext), `kc` (KeyCombination).
- Liquibase: SQL keywords lowercase, identifiers `UPPERCASE_WITH_UNDERSCORES`.
- View routes: `kebab-case` with module short-id prefix (e.g. `sec/resource-role-models`).

## Nullability
- Only return/accept `null` when annotated `@Nullable`. Otherwise non-null is implied.
- Prefer package-level `@NonNullApi` (except entity packages).
- Use `org.springframework.lang.Nullable` when `spring-core` is available, else `jakarta.annotation.Nullable`.
- For public API, validate non-null params with:
  - `io.jmix.core.common.util.Preconditions#checkNotNullArgument()` (if `jmix-core` is available),
  - `com.google.common.base.Preconditions` (if `guava` is available),
  - `java.util.Objects#requireNonNull()` (fallback).
- Never return `null` collections; return empty collections.

## Common Practices
- Keep code as simple as possible (KISS/YAGNI).
- Pay attention to IDE warnings; suppress narrowly when unavoidable.
- Prefer method references over lambdas when possible; avoid unnecessary lambdas when simple `for` loop is possible.
- Dependency injection: field injection is preferred.

## UI Practices
- Use `em`/`rem` (avoid `px`); `spacing` is default; `vbox` needs `padding="false"` to avoid extra space.
- `TextArea` height: `9.5em`.
- Avoid expanding `detailActions`. Grids typically do not need expand; set `min-height` (e.g. `20em`).
- Set `emptySelectionAllowed="true"` for `Select`/`ComboBox` when value must be clearable.
- Use RTL-safe spacing (`margin-inline-*`), and add `aria-label` where needed.
- `@Route` path: addon prefix then screen name (e.g. `sec/resourcerolemodels/:code`).
- `@Subscribe`, `@Install`, `@Supply`, and view parameter methods must be `public`. Other APIs should be `protected`/`private`.
- View controller field order: constants, view components, data containers/loaders, beans, then own fields; no blank lines inside a block.
- Component init: constructor handles inputs only, move setup into overridable `protected` init method. Separate create vs init for child components.
- Prevent unsaved changes: when navigation close action is used, also call `BeforeLeaveEvent#postpone`.
- UI components should support light/dark themes via CSS variables, RTL, forced colors, and reduced motion.
- Icons: use `JmixFontIcon` and `Icons` bean where available; new icons require `JmixFontIcon` entry and CSS mapping.

### View XML Descriptor Naming
- Data containers: `camelCase` ending with `Dc` (e.g. `usersDc`).
- Data loaders: `camelCase` ending with `Dl` (e.g. `usersDl`).
- Query params: `camelCase` (prefixes separated by `_`), e.g. `:current_user_username`.
- Component ids: `camelCase`, include purpose; data-bound ids include entity/attribute + type (e.g. `usersDataGrid`, `usernameField`).
- Action ids end with `Action`; include type value if defined (e.g. `entityLookupAction`).
- Exceptions: `detailActions`, `form`; picker actions may omit `Action`.
- Message keys:
  - View title: `<viewClassName>.title`
  - Component attribute: `<componentId>.<attribute>`
  - Action attribute: `<actionId>.<attribute>` or `action.<actionType>.<attribute>`
  - Menu: `menu.<menuId>.<attribute>`; menu item: `<viewClassName>.<attribute>` or `menu.<menuItemId>.<attribute>`

## Testing
- Use JUnit 5 or Spock. Prefer integration tests.
- Tests focus on behavior via public APIs.
- Test packages: top-level by functionality (e.g. `data_manager`), with subpackages if needed.
- Test class names end with `*Test`; helper classes start with `Test*`.
- Test infrastructure under `test_support`, entities in `test_support.entity` (or its subpackages).
- Resources mirror test package paths under `resources`.
- Slow tests: tag `@Tag("slowTests")` (JUnit) or `@IgnoreIf({env["includeSlowTests"] != 'true'})` (Spock). Run with `-PincludeSlowTests=true`.
