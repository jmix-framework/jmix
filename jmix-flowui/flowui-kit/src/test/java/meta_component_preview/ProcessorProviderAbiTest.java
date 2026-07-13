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

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ABI safety net for the additive {@code addPreviewChild}/{@code removePreviewChild} statics on the
 * frozen {@code StudioPreviewComponentProvider}: the original hint-less overloads keep their exact
 * signature, and the new slotHint overloads are additive (distinct arity), discovered and invoked
 * via reflection just like Studio does.
 */
class ProcessorProviderAbiTest {

    static final String PROVIDER = "io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentProvider";

    Method providerMethod(String name) throws Exception {
        return providerMethod(name, method -> true);
    }

    Method providerMethod(String name, int parameterCount) throws Exception {
        return providerMethod(name, method -> method.getParameterCount() == parameterCount);
    }

    private Method providerMethod(String name, java.util.function.Predicate<Method> filter) throws Exception {
        Method method = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .filter(filter)
                .findFirst().orElseThrow();
        method.trySetAccessible();
        return method;
    }

    @Test
    void testProviderHasExactlyOneHintLessAndOneSlotHintOverloadOfEach() throws Exception {
        long addCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("addPreviewChild")).count();
        long removeCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("removePreviewChild")).count();
        // Additive: the original 3-arg add / 2-arg remove statics (task A2) plus the new 4-arg add /
        // 3-arg remove slotHint statics (task A3) - never a 3rd overload of either.
        assertEquals(2, addCount);
        assertEquals(2, removeCount);
    }

    @Test
    void testOriginalHintLessOverloadsKeepTheirExactSignature() throws Exception {
        assertEquals(3, providerMethod("addPreviewChild", 3).getParameterCount());
        assertEquals(2, providerMethod("removePreviewChild", 2).getParameterCount());
    }

    @Test
    void testFrozenStaticsUntouchedByProcessorAddition() throws Exception {
        long createComponentCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("createComponent")).count();
        long canCreateCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("canCreateComponent")).count();
        long createComponentResultCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("createComponentResult")).count();
        assertEquals(1, createComponentCount);
        assertEquals(1, canCreateCount);
        assertEquals(1, createComponentResultCount);
    }

    private long countDeclaredMethods(String name) {
        return Arrays.stream(providerDeclaredMethods()).filter(method -> method.getName().equals(name)).count();
    }

    private Method[] providerDeclaredMethods() {
        try {
            return Class.forName(PROVIDER).getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Task B1: pins the keyed-family statics (actions/tabs/columns) to exactly one overload each
     * and re-confirms the pre-existing statics are untouched by their addition - the same
     * additive-only guarantee {@link #testProviderHasExactlyOneHintLessAndOneSlotHintOverloadOfEach()}
     * pins for the slotHint statics.
     */
    @Test
    void testKeyedPreviewStaticsAreExactlyOneOverloadEachAndExistingStaticsUntouched() {
        assertEquals(1, countDeclaredMethods("addPreviewAction"));
        assertEquals(1, countDeclaredMethods("removePreviewAction"));
        assertEquals(1, countDeclaredMethods("addPreviewTab"));
        assertEquals(1, countDeclaredMethods("removePreviewTab"));
        assertEquals(1, countDeclaredMethods("addPreviewColumn"));
        assertEquals(1, countDeclaredMethods("removePreviewColumn"));

        assertEquals(2, countDeclaredMethods("addPreviewChild"));
        assertEquals(2, countDeclaredMethods("removePreviewChild"));
        assertEquals(1, countDeclaredMethods("createComponent"));
        assertEquals(1, countDeclaredMethods("canCreateComponent"));
        assertEquals(1, countDeclaredMethods("createComponentResult"));
    }

    /**
     * Regression for the discoverability defect: Studio finds these statics via
     * {@code FlowReflectionUtils.findMethod(class, name, paramTypes)}, which (like
     * {@link Class#getMethods()}) only matches {@code public} methods. A mutation static declared
     * without {@code public} is invisible to that lookup, so the whole component-processor
     * delegation silently never fires and Studio falls back to reflection. Every NEW mutation
     * static must be both public and, since the declaring class itself stays package-private,
     * still invocable via {@code setAccessible} - exactly how Studio calls it once found.
     */
    @Test
    void testAllNewMutationStaticsArePublicAndInvocableForStudioDiscovery() throws Exception {
        String[] mutationStaticNames = {
                "addPreviewChild", "removePreviewChild",
                "addPreviewAction", "removePreviewAction",
                "addPreviewTab", "removePreviewTab",
                "addPreviewColumn", "removePreviewColumn"
        };

        for (String name : mutationStaticNames) {
            for (Method method : providerDeclaredMethods()) {
                if (!method.getName().equals(name)) {
                    continue;
                }
                assertTrue(java.lang.reflect.Modifier.isPublic(method.getModifiers()),
                        () -> "Expected " + method + " to be public so Studio's public-only "
                                + "FlowReflectionUtils.findMethod lookup can discover it");
                assertTrue(method.trySetAccessible(),
                        () -> method + " must remain invocable via setAccessible "
                                + "even though its declaring class is package-private");
            }
        }

        // The declaring class itself stays package-private - only the members change.
        assertFalse(java.lang.reflect.Modifier.isPublic(Class.forName(PROVIDER).getModifiers()));
    }

    @Test
    void testAddPreviewActionAttachesToHasActionsAndRemovePreviewActionDetaches() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();
        Action action = new BaseAction<>("save");

        Object addHandled = providerMethod("addPreviewAction", 3).invoke(null, grid, action, -1);

        assertEquals(Boolean.TRUE, addHandled);
        assertEquals(action, grid.getAction("save"));

        Object removeHandled = providerMethod("removePreviewAction", 2).invoke(null, grid, action);

        assertEquals(Boolean.TRUE, removeHandled);
        assertNull(grid.getAction("save"));
    }

    @Test
    void testAddPreviewActionReturnsFalseWhenActionArgIsNotAKitAction() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();

        Object handled = providerMethod("addPreviewAction", 3).invoke(null, grid, "not-an-action", -1);

        assertFalse((Boolean) handled);
    }

    @Test
    void testAddPreviewTabAttachesTabAndContentAndRemovePreviewTabDetaches() throws Exception {
        TabSheet tabSheet = new TabSheet();
        Tab tab = new Tab("First");
        Div content = new Div();

        Object addHandled = providerMethod("addPreviewTab", 4).invoke(null, tabSheet, tab, content, -1);

        assertEquals(Boolean.TRUE, addHandled);
        assertEquals(content, tabSheet.getComponent(tab));

        Object removeHandled = providerMethod("removePreviewTab", 2).invoke(null, tabSheet, tab);

        assertEquals(Boolean.TRUE, removeHandled);
        assertEquals(0, tabSheet.getTabCount());
    }

    @Test
    void testAddPreviewColumnCreatesByKeyAndRemovePreviewColumnRemovesByKey() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();

        Object addHandled = providerMethod("addPreviewColumn", 3).invoke(null, grid, "name", -1);

        assertEquals(Boolean.TRUE, addHandled);
        assertTrue(grid.getColumnByKey("name") != null);

        Object removeHandled = providerMethod("removePreviewColumn", 2).invoke(null, grid, "name");

        assertEquals(Boolean.TRUE, removeHandled);
        assertNull(grid.getColumnByKey("name"));
    }

    /**
     * The load-time/interactive no-conflict case: {@code StudioGridPreviewLoader} already built the
     * column at load time (bind-by-key); the interactive palette-drop path must reuse it rather than
     * duplicate it.
     */
    @Test
    void testAddPreviewColumnReusesLoadTimeColumnInsteadOfDuplicating() throws Exception {
        JmixGrid<Object> grid = new JmixGrid<>();
        Grid.Column<Object> loadTimeColumn = grid.addColumn(item -> "").setKey("email");

        Object addHandled = providerMethod("addPreviewColumn", 3).invoke(null, grid, "email", -1);

        assertEquals(Boolean.TRUE, addHandled);
        assertEquals(1, grid.getColumns().size());
        assertEquals(loadTimeColumn, grid.getColumnByKey("email"));
    }

    @Test
    void testAddPreviewChildAttachesDivToVerticalLayout() throws Exception {
        VerticalLayout parent = new VerticalLayout();
        Div child = new Div();

        Object handled = providerMethod("addPreviewChild", 3).invoke(null, parent, child, -1);

        assertEquals(Boolean.TRUE, handled);
        assertTrue(parent.getChildren().anyMatch(component -> component == child));
    }

    @Test
    void testRemovePreviewChildDetachesDivFromVerticalLayout() throws Exception {
        VerticalLayout parent = new VerticalLayout();
        Div child = new Div();
        parent.add(child);

        Object handled = providerMethod("removePreviewChild", 2).invoke(null, parent, child);

        assertEquals(Boolean.TRUE, handled);
        assertTrue(parent.getChildren().noneMatch(component -> component == child));
    }

    @Test
    void testAddAndRemovePreviewChildReturnFalseForUnsupportedParent() throws Exception {
        Component unsupportedParent = new Text("leaf");
        Component child = new Div();

        Object addHandled = providerMethod("addPreviewChild", 3).invoke(null, unsupportedParent, child, -1);
        Object removeHandled = providerMethod("removePreviewChild", 2).invoke(null, unsupportedParent, child);

        assertFalse((Boolean) addHandled);
        assertFalse((Boolean) removeHandled);
    }

    @Test
    void testSlotHintAddPreviewChildAttachesPrefixToTextField() throws Exception {
        TextField parent = new TextField();
        Div prefix = new Div();

        Object handled = providerMethod("addPreviewChild", 4).invoke(null, parent, prefix, -1, "prefix");

        assertEquals(Boolean.TRUE, handled);
        assertEquals(prefix, parent.getPrefixComponent());
    }

    @Test
    void testSlotHintRemovePreviewChildDetachesSuffixFromTextField() throws Exception {
        TextField parent = new TextField();
        Div suffix = new Div();
        parent.setSuffixComponent(suffix);

        Object handled = providerMethod("removePreviewChild", 3).invoke(null, parent, suffix, "suffix");

        assertEquals(Boolean.TRUE, handled);
        assertNull(parent.getSuffixComponent());
    }

    @Test
    void testSlotHintAddPreviewChildAttachesToAppLayoutNavbarDrawerAndContent() throws Exception {
        AppLayout appLayout = new AppLayout();
        Div navbarItem = new Div();
        Div content = new Div();

        Object navbarHandled = providerMethod("addPreviewChild", 4)
                .invoke(null, appLayout, navbarItem, -1, "navbar");
        Object contentHandled = providerMethod("addPreviewChild", 4)
                .invoke(null, appLayout, content, -1, "content");

        assertEquals(Boolean.TRUE, navbarHandled);
        assertEquals(Boolean.TRUE, contentHandled);
        assertTrue(appLayout.getChildren().anyMatch(component -> component == navbarItem));
        assertEquals(content, appLayout.getContent());
    }

    @Test
    void testSlotHintAddPreviewChildReturnsFalseForUnrecognizedParentSlotHintCombo() throws Exception {
        TextField parent = new TextField();
        Div child = new Div();

        // A bare HasPrefix without the matching hint can't be disambiguated by the slotHint statics
        // either - "navbar" only means something for AppLayout.
        Object handled = providerMethod("addPreviewChild", 4).invoke(null, parent, child, -1, "navbar");

        assertFalse((Boolean) handled);
    }
}
