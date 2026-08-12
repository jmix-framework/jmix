/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package meta_component_preview;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ABI safety net for the surface Studio reaches by reflection. That surface is exactly one public
 * static - {@code perform(String, Map)} - so a new operation or capability costs a map key, never a
 * new method. This pins that shape, and pins every operation's behavior invoked the way Studio
 * invokes it. The pre-protocol {@code createComponent}/{@code canCreateComponent} statics stay
 * pinned too: released Studio versions still call them directly.
 */
class ProcessorProviderAbiTest {

    static final String PROVIDER = "io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentProvider";

    static final String VIEW_NS = "http://jmix.io/schema/flowui/view";

    static final String VIEW_XML = """
            <view xmlns="http://jmix.io/schema/flowui/view">
                <layout>
                    <button id="okBtn" width="10em"/>
                </layout>
            </view>""";

    // Unprefixed path steps never match namespaced elements under XPath 1.0, so match by local-name().
    static final String BUTTON_XPATH =
            "/*[local-name()='view']/*[local-name()='layout']/*[local-name()='button']";

    private Method providerMethod(String name) throws Exception {
        Method method = Arrays.stream(providerDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst().orElseThrow();
        method.trySetAccessible();
        return method;
    }

    private Method[] providerDeclaredMethods() {
        try {
            return Class.forName(PROVIDER).getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private long countDeclaredMethods(String name) {
        return Arrays.stream(providerDeclaredMethods()).filter(method -> method.getName().equals(name)).count();
    }

    /**
     * Invokes the entry point the way Studio does: resolve by name and exact parameter types,
     * {@code trySetAccessible}, invoke.
     */
    private Object perform(String operation, Map<String, Object> params) throws Exception {
        Method method = Class.forName(PROVIDER).getMethod("perform", String.class, Map.class);
        method.trySetAccessible();
        return method.invoke(null, operation, params);
    }

    /**
     * A designer change as Studio reports it: the mutated container in {@code target}, the live
     * object (if Studio holds one) in {@code child}, the child element's XML identity in
     * {@code tag}/{@code attributes}. Studio treats {@code null} as "run my own fallback".
     */
    private Object added(Component target, Map<String, Object> params) throws Exception {
        Map<String, Object> call = params == null ? new HashMap<>() : new HashMap<>(params);
        call.put("target", target);
        return perform("componentAdded", call);
    }

    private boolean removed(Component target, Map<String, Object> params) throws Exception {
        Map<String, Object> call = params == null ? new HashMap<>() : new HashMap<>(params);
        call.put("target", target);
        return perform("componentRemoved", call) == Boolean.TRUE;
    }

    private static Map<String, Object> params(Object... keyValuePairs) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            params.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return params;
    }

    @Test
    void testProtocolSurfaceIsExactlyOnePublicStatic() {
        assertEquals(1, countDeclaredMethods("perform"));

        // A per-operation static coming back is exactly what this design removes.
        assertTrue(Arrays.stream(providerDeclaredMethods())
                        .filter(method -> Modifier.isPublic(method.getModifiers()))
                        .allMatch(method -> method.getName().equals("perform")),
                "perform must stay the only public static");
    }

    /**
     * Studio resolves these by name AND exact parameter types
     * ({@code FlowReflectionUtils.findMethod}), so a type drift silently disables the delegation
     * while a count-based pin stays green.
     */
    @Test
    void testEntryPointAndLegacyStaticsKeepExactSignatures() throws Exception {
        Class<?> context = Class.forName(PROVIDER + "$ComponentCreationContext");

        assertParameterTypes(providerMethod("perform"), String.class, Map.class);
        assertEquals(Object.class, providerMethod("perform").getReturnType());

        // Released Studio versions still call these two directly.
        assertParameterTypes(providerMethod("createComponent"), context);
        assertParameterTypes(providerMethod("canCreateComponent"), String.class, String.class);
        assertEquals(Component.class, providerMethod("createComponent").getReturnType());
        assertTrue(Modifier.isStatic(providerMethod("createComponent").getModifiers()));
        assertTrue(Modifier.isStatic(providerMethod("canCreateComponent").getModifiers()));
    }

    @Test
    void testCreationOperationsGoThroughTheEntryPoint() throws Exception {
        assertEquals(Boolean.TRUE,
                perform("canCreateComponent", params("tag", "button", "namespace", VIEW_NS)));
        assertEquals(Boolean.FALSE,
                perform("canCreateComponent", params("tag", "noSuchElement", "namespace", VIEW_NS)));

        Object component = perform("createComponent", params(
                "viewXml", VIEW_XML, "componentPath", BUTTON_XPATH, "environment", null));
        assertInstanceOf(Component.class, component);
    }

    @Test
    void testEntryPointReturnsNullForUnknownOperationAndMissingTarget() throws Exception {
        assertNull(perform("noSuchOperation", params("child", new Div())));
        assertNull(perform(null, params()));
        // A mutation without its target can't be dispatched.
        assertNull(perform("componentAdded", params("child", new Div())));
        // The removed per-operation vocabulary must not silently keep working.
        assertNull(perform("addChild", params("target", new VerticalLayout(), "child", new Div())));
    }

    private static void assertParameterTypes(Method method, Class<?>... expected) {
        assertArrayEquals(expected, method.getParameterTypes(),
                method.getName() + " parameter types drifted from the frozen ABI Studio resolves by");
    }

    @Test
    void testFrozenCreationStaticsUntouched() {
        assertEquals(1, countDeclaredMethods("createComponent"));
        assertEquals(1, countDeclaredMethods("canCreateComponent"));
    }

    @Test
    void testLegacyTwoArgContextStillCreatesComponent() throws Exception {
        assertInstanceOf(Component.class,
                providerMethod("createComponent").invoke(null, newContext(VIEW_XML, BUTTON_XPATH)));
    }

    @Test
    void testThreeArgContextWithNullEnvironmentStillCreatesComponent() throws Exception {
        assertInstanceOf(Component.class,
                providerMethod("createComponent").invoke(null, newContext(VIEW_XML, BUTTON_XPATH, null)));
    }

    private Object newContext(Object... args) throws Exception {
        Class<?>[] types = args.length == 2
                ? new Class<?>[]{String.class, String.class}
                : new Class<?>[]{String.class, String.class, Object.class};
        Constructor<?> constructor = Class.forName(PROVIDER + "$ComponentCreationContext").getConstructor(types);
        constructor.trySetAccessible();
        return constructor.newInstance(args);
    }

    /**
     * Studio finds these via {@code FlowReflectionUtils.findMethod}, which (like
     * {@link Class#getMethods()}) only matches {@code public} methods, and invokes them through
     * {@code setAccessible} because the declaring class stays package-private.
     */
    @Test
    void testEntryPointIsPublicStaticAndInvocable() throws Exception {
        Method method = providerMethod("perform");
        assertTrue(Modifier.isPublic(method.getModifiers()), "perform must be public for Studio's lookup");
        assertTrue(Modifier.isStatic(method.getModifiers()), "perform must be static");
        assertTrue(method.trySetAccessible(), "perform must stay invocable via setAccessible");

        assertFalse(Modifier.isPublic(Class.forName(PROVIDER).getModifiers()));
    }

    @Test
    void testCapabilitiesOperationReportsFullContent() throws Exception {
        Map<?, ?> capabilities = (Map<?, ?>) perform("capabilities", null);

        assertEquals(Boolean.TRUE, capabilities.get("fullContent"));
    }

    @Test
    void testMissingOrWrongTypedArgumentYieldsNullSoStudioFallsBack() throws Exception {
        VerticalLayout parent = new VerticalLayout();

        // no child and no column identity: nothing to classify
        assertNull(added(parent, params()));
        assertNull(added(parent, null));
        // a wrongly-typed child counts as absent
        assertNull(added(parent, params("child", "not-a-component")));
    }

    @Test
    void testPlainChildAttachesAndDetaches() throws Exception {
        VerticalLayout parent = new VerticalLayout();
        Div child = new Div();

        assertEquals(Boolean.TRUE, added(parent, params("child", child, "index", -1)));
        assertTrue(parent.getChildren().anyMatch(component -> component == child));

        assertTrue(removed(parent, params("child", child)));
        assertTrue(parent.getChildren().noneMatch(component -> component == child));
    }

    @Test
    void testAbsentIndexAppends() throws Exception {
        VerticalLayout parent = new VerticalLayout();
        Div first = new Div();
        Div second = new Div();

        assertEquals(Boolean.TRUE, added(parent, params("child", first)));
        assertEquals(Boolean.TRUE, added(parent, params("child", second)));

        assertEquals(second, parent.getComponentAt(1));
    }

    @Test
    void testUnsupportedParentYieldsNull() throws Exception {
        Component unsupportedParent = new Text("leaf");
        Component child = new Div();

        assertNull(added(unsupportedParent, params("child", child)));
        assertFalse(removed(unsupportedParent, params("child", child)));
    }

    /**
     * Studio names no slot: the framework derives it from the child element's tag.
     */
    @Test
    void testPrefixAndSuffixTagsClassifyAsSlots() throws Exception {
        TextField withPrefix = new TextField();
        Div prefix = new Div();

        assertEquals(Boolean.TRUE,
                added(withPrefix, params("child", prefix, "index", -1, "tag", "prefix")));
        assertEquals(prefix, withPrefix.getPrefixComponent());

        TextField withSuffix = new TextField();
        Div suffix = new Div();
        withSuffix.setSuffixComponent(suffix);

        assertTrue(removed(withSuffix, params("child", suffix, "tag", "suffix")));
        assertNull(withSuffix.getSuffixComponent());
    }

    @Test
    void testAppLayoutPartTagsClassifyAsSlots() throws Exception {
        AppLayout appLayout = new AppLayout();
        Div navbarItem = new Div();
        Div drawerItem = new Div();
        Div content = new Div();

        assertEquals(Boolean.TRUE, added(appLayout, params("child", navbarItem, "tag", "navigationBar")));
        assertEquals(Boolean.TRUE, added(appLayout, params("child", drawerItem, "tag", "drawerLayout")));
        assertEquals(Boolean.TRUE, added(appLayout, params("child", content, "tag", "initialLayout")));

        assertTrue(appLayout.getChildren().anyMatch(component -> component == navbarItem));
        assertTrue(appLayout.getChildren().anyMatch(component -> component == drawerItem));
        assertEquals(content, appLayout.getContent());

        // layout and workArea (the tabbedmode name) are the other two names of the content slot
        Div layout = new Div();
        assertEquals(Boolean.TRUE, added(appLayout, params("child", layout, "tag", "layout")));
        assertEquals(layout, appLayout.getContent());

        Div workArea = new Div();
        assertEquals(Boolean.TRUE, added(appLayout, params("child", workArea, "tag", "workArea")));
        assertEquals(workArea, appLayout.getContent());

        // a remove without a tag on an AppLayout keeps the pre-classifier "content" semantics
        assertTrue(removed(appLayout, params("child", workArea)));
        assertTrue(appLayout.getChildren().noneMatch(component -> component == workArea));
    }

    /**
     * A freshly dropped component has no XML element yet, so an add may arrive without a tag.
     * It must NOT default to the content slot — that would replace the AppLayout's work area —
     * and an AppLayout is no plain-children container, so the framework declines entirely.
     */
    @Test
    void testTaglessAddOnAppLayoutIsDeclined() throws Exception {
        AppLayout appLayout = new AppLayout();
        Div workArea = new Div();
        appLayout.setContent(workArea);

        assertNull(added(appLayout, params("child", new Div())));
        assertEquals(workArea, appLayout.getContent());
    }

    /**
     * The touchOptimized navbar needs a flag this SPI's slot processors don't model, so the
     * framework declines and Studio's own reflective path attaches it.
     */
    @Test
    void testTouchOptimizedNavigationBarIsDeclined() throws Exception {
        AppLayout appLayout = new AppLayout();

        assertNull(added(appLayout, params(
                "child", new Div(), "tag", "navigationBar", "attributes", Map.of("touchOptimized", "true"))));
    }

    @Test
    void testActionChildClassifiesByType() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();
        Action action = new BaseAction<>("save");

        assertEquals(Boolean.TRUE, added(grid, params("child", action, "index", -1)));
        assertEquals(action, grid.getAction("save"));

        assertTrue(removed(grid, params("child", action)));
        assertNull(grid.getAction("save"));
    }

    @Test
    void testTabClassifiesByContentPresence() throws Exception {
        TabSheet tabSheet = new TabSheet();
        Tab tab = new Tab("First");
        Div content = new Div();

        assertEquals(Boolean.TRUE,
                added(tabSheet, params("child", tab, "content", content, "index", -1)));
        assertEquals(content, tabSheet.getComponent(tab));

        assertTrue(removed(tabSheet, params("child", tab, "tag", "tab")));
        assertEquals(0, tabSheet.getTabCount());
    }

    /**
     * A column has no live child: it is classified by its XML identity alone, and the created
     * column is returned so Studio binds to it directly.
     */
    @Test
    void testColumnClassifiesByAttributesAndReturnsTheCreatedColumn() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();

        Object column = added(grid, params(
                "tag", "column", "attributes", Map.of("property", "name"), "index", -1));
        assertInstanceOf(Grid.Column.class, column);
        assertEquals(grid.getColumnByKey("name"), column);

        assertTrue(removed(grid, params("tag", "column", "attributes", Map.of("property", "name"))));
        assertNull(grid.getColumnByKey("name"));
    }

    @Test
    void testBareEditorActionsColumnDefaultsItsKeyToTheTagName() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();

        Object column = added(grid, params("tag", "editorActionsColumn"));
        assertInstanceOf(Grid.Column.class, column);
        assertNotNull(grid.getColumnByKey("editorActionsColumn"));
    }

    /**
     * The load-time/interactive no-conflict case: {@code StudioGridPreviewLoader} already built the
     * column at load time (bind-by-key); the interactive palette-drop path must reuse it rather than
     * duplicate it.
     */
    @Test
    void testAddColumnReusesLoadTimeColumnInsteadOfDuplicating() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();
        Grid.Column<Object> loadTimeColumn = grid.addColumn(item -> "").setKey("email");

        Object column = added(grid, params("tag", "column", "attributes", Map.of("key", "email"), "index", -1));

        assertEquals(1, grid.getColumns().size());
        assertEquals(loadTimeColumn, column);
    }
}
