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

package io.jmix.aitools.tool;

import io.jmix.aitools.tool.impl.AiToolDescriptorProviderImpl;
import io.jmix.aitools.tool.impl.AiToolRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiToolRegistryTest {

    private AiToolRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new AiToolRegistryImpl();
        ReflectionTestUtils.setField(registry, "aiToolDescriptorProvider", new AiToolDescriptorProviderImpl());
    }

    @Test
    @DisplayName("Collects @Tool methods from multiple beans and exposes them via getAll/findByName")
    void testCollectsToolsFromMultipleBeans() {
        SimpleToolA toolA = new SimpleToolA();
        SimpleToolB toolB = new SimpleToolB();
        buildRegistry(List.of(toolA, toolB));

        List<ResolvedAiTool> all = registry.getAll();
        assertEquals(2, all.size());

        Optional<ResolvedAiTool> a = registry.findByName("alpha");
        assertTrue(a.isPresent());
        assertSame(toolA, a.get().getSource());
        assertEquals("Alpha description", a.get().getDescription());

        Optional<ResolvedAiTool> b = registry.findByName("beta");
        assertTrue(b.isPresent());
        assertSame(toolB, b.get().getSource());

        assertEquals(2, registry.getAllCallbacks().size());
    }

    @Test
    @DisplayName("Collects multiple @Tool methods declared on the same bean")
    void testCollectsMultipleToolMethodsFromOneBean() {
        MultiMethodTool tool = new MultiMethodTool();
        buildRegistry(List.of(tool));

        assertEquals(2, registry.getAll().size());
        assertTrue(registry.findByName("first").isPresent());
        assertTrue(registry.findByName("second").isPresent());
    }

    @Test
    @DisplayName("findByName returns empty for unknown tool name")
    void testFindByNameMissing() {
        buildRegistry(List.of(new SimpleToolA()));
        assertTrue(registry.findByName("does-not-exist").isEmpty());
    }

    @Test
    @DisplayName("Deduplicates the same bean appearing twice in the autowired list")
    void testDeduplicatesSameBean() {
        SimpleToolA tool = new SimpleToolA();
        buildRegistry(List.of(tool, tool));

        assertEquals(1, registry.getAll().size());
        assertSame(tool, registry.findByName("alpha").orElseThrow().getSource());
    }

    @Test
    @DisplayName("Two distinct beans of the same tool class are both kept (identity-based dedup)")
    void testTwoBeansOfSameClassKept() {
        // Two distinct bean instances of the same class - neither should be dropped by dedup.
        // Their @Tool names collide intentionally, so the registry must report this as a
        // duplicate-name error rather than silently dropping the second bean.
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildRegistry(List.of(new SimpleToolA(), new SimpleToolA())));
        assertTrue(ex.getMessage().contains("alpha"));
        assertTrue(ex.getMessage().contains("SimpleToolA"),
                "Error message should reference both conflicting beans of the same class");
    }

    @Test
    @DisplayName("findByMarker returns only tools whose source implements the marker")
    void testFindByMarker() {
        MarkerATool a = new MarkerATool();
        MarkerBTool b = new MarkerBTool();
        buildRegistry(List.of(a, b));

        List<ResolvedAiTool> markerA = registry.findByMarker(MarkerA.class);
        assertEquals(1, markerA.size());
        assertSame(a, markerA.get(0).getSource());

        List<ResolvedAiTool> markerB = registry.findByMarker(MarkerB.class);
        assertEquals(1, markerB.size());
        assertSame(b, markerB.get(0).getSource());
    }

    @Test
    @DisplayName("Bean implementing multiple markers is returned by each of them")
    void testBeanImplementingMultipleMarkers() {
        MultiMarkerTool tool = new MultiMarkerTool();
        buildRegistry(List.of(tool));

        assertEquals(1, registry.findByMarker(MarkerA.class).size());
        assertEquals(1, registry.findByMarker(MarkerB.class).size());
        assertSame(tool, registry.findByMarker(MarkerA.class).get(0).getSource());
        assertSame(tool, registry.findByMarker(MarkerB.class).get(0).getSource());
    }

    @Test
    @DisplayName("findByMarker returns empty list when no tool implements the marker")
    void testFindByMarkerEmpty() {
        buildRegistry(List.of(new SimpleToolA()));
        assertTrue(registry.findByMarker(MarkerA.class).isEmpty());
    }

    @Test
    @DisplayName("@ToolOverride replaces original tool method, keeping the original tool name")
    void testOverrideReplacesOriginal() {
        SimpleToolA original = new SimpleToolA();
        OverridingTool override = new OverridingTool();
        buildRegistry(List.of(original, override));

        // Only the overridden 'alpha' remains in the registry, registered under the original name.
        assertEquals(1, registry.getAll().size());
        ResolvedAiTool alpha = registry.findByName("alpha").orElseThrow();
        assertSame(override, alpha.getSource());
        assertEquals("Overridden alpha description", alpha.getDescription());

        // The override method's own @Tool name is irrelevant - the tool is exposed as 'alpha'.
        assertTrue(registry.findByName("irrelevant-own-name").isEmpty());
    }

    @Test
    @DisplayName("Override inherits the original tool's marker membership")
    void testOverrideInheritsOriginalMarkers() {
        // Original bean implements MarkerA, override bean implements only base JmixAiTool.
        // The override must still appear in findByMarker(MarkerA.class).
        MarkerAOriginalTool original = new MarkerAOriginalTool();
        PlainOverrideForMarkerA override = new PlainOverrideForMarkerA();
        buildRegistry(List.of(original, override));

        List<ResolvedAiTool> markerA = registry.findByMarker(MarkerA.class);
        assertEquals(1, markerA.size());
        ResolvedAiTool resolved = markerA.get(0);
        assertSame(override, resolved.getSource(), "override must replace the implementation");
        assertEquals("marker-a-tool", resolved.getName(), "tool name must stay the original");
        assertTrue(resolved.getMarkers().contains(MarkerA.class),
                "marker membership must be inherited from the original");
    }

    @Test
    @DisplayName("Override adds its own markers to the union without losing the original's")
    void testOverrideUnionsMarkers() {
        // Original implements MarkerA, override implements MarkerB. Result must have both.
        MarkerAOriginalTool original = new MarkerAOriginalTool();
        MarkerBOverrideForMarkerA override = new MarkerBOverrideForMarkerA();
        buildRegistry(List.of(original, override));

        ResolvedAiTool resolved = registry.findByName("marker-a-tool").orElseThrow();
        Set<Class<? extends JmixAiTool>> markers = resolved.getMarkers();
        assertTrue(markers.contains(MarkerA.class), "marker from original must be present");
        assertTrue(markers.contains(MarkerB.class), "marker from override must be present");

        assertEquals(1, registry.findByMarker(MarkerA.class).size());
        assertEquals(1, registry.findByMarker(MarkerB.class).size());
    }

    @Test
    @DisplayName("@Tool methods inherited from a superclass are picked up")
    void testCollectsInheritedToolMethods() {
        InheritingTool tool = new InheritingTool();
        buildRegistry(List.of(tool));

        // 'base-method' is declared on AbstractBaseTool, 'derived-method' is declared on InheritingTool.
        assertEquals(2, registry.getAll().size());
        assertTrue(registry.findByName("base-method").isPresent());
        assertTrue(registry.findByName("derived-method").isPresent());
        assertSame(tool, registry.findByName("base-method").orElseThrow().getSource());
    }

    @Test
    @DisplayName("Subclass overriding a @Tool method without re-annotating still exposes the tool")
    void testInheritedToolAnnotationFoundOnSubclassOverride() {
        SubclassRedefiningBaseMethod tool = new SubclassRedefiningBaseMethod();
        buildRegistry(List.of(tool));

        // Subclass redefines base-method without re-applying @Tool. The annotation must still
        // be discovered via the superclass declaration.
        assertEquals(1, registry.getAll().size());
        ResolvedAiTool resolved = registry.findByName("base-method").orElseThrow();
        assertSame(tool, resolved.getSource());
    }

    @Test
    @DisplayName("Tool name conflict without @ToolOverride fails on refresh")
    void testNameConflictWithoutOverrideFails() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildRegistry(List.of(new SimpleToolA(), new ToolWithAlphaName())));
        assertTrue(ex.getMessage().contains("alpha"));
    }

    @Test
    @DisplayName("Two beans declaring @ToolOverride for the same name fail on refresh")
    void testTwoOverridesForSameNameFails() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildRegistry(List.of(new SimpleToolA(), new OverridingTool(), new SecondOverridingTool())));
        assertTrue(ex.getMessage().contains("alpha"));
    }

    @Test
    @DisplayName("Two dangling @ToolOverride sharing the fallback @Tool name fail with both targets listed")
    void testTwoDanglingOverridesSharingFallbackNameFails() {
        // Both methods are dangling (their @ToolOverride targets don't exist) and happen to share
        // their own @Tool name, so the fallback would place both in the same bucket. The error
        // message must name each method's actual @ToolOverride value - using the bucket key would
        // mislead the user, because neither method actually wrote @ToolOverride("foo").
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildRegistry(List.of(new DanglingOverrideFooToMissingX(), new DanglingOverrideFooToMissingY())));
        assertTrue(ex.getMessage().contains("foo"),
                "Error must mention the colliding @Tool name");
        assertTrue(ex.getMessage().contains("missing-X"),
                "Error must mention the first method's @ToolOverride target");
        assertTrue(ex.getMessage().contains("missing-Y"),
                "Error must mention the second method's @ToolOverride target");
    }

    @Test
    @DisplayName("@ToolOverride with non-existent target registers the method as a new tool")
    void testOverrideMissingTargetRegistersAsNewTool() {
        DanglingOverrideTool tool = new DanglingOverrideTool();
        buildRegistry(List.of(tool));

        assertEquals(1, registry.getAll().size());
        ResolvedAiTool resolved = registry.getAll().get(0);
        // Falls back to the method's own @Tool name when the override target does not exist.
        assertEquals("dangling", resolved.getName());
        assertTrue(registry.findByName("does-not-exist").isEmpty());
    }

    @Test
    @DisplayName("Dangling @ToolOverride whose own @Tool name collides with a regular tool fails on refresh")
    void testDanglingOverrideCollidingWithRegularFails() {
        // The override targets a missing tool, so the registry would normally fall back to the
        // override's own @Tool name. But that name is already taken by SimpleToolA - falling back
        // would silently replace it. The registry must fail loudly instead.
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildRegistry(List.of(new SimpleToolA(), new DanglingOverrideCollidingWithAlpha())));
        assertTrue(ex.getMessage().contains("alpha"),
                "Error must mention the colliding @Tool name");
        assertTrue(ex.getMessage().contains("does-not-exist"),
                "Error must mention the missing @ToolOverride target");
    }

    @Test
    @DisplayName("refresh() is idempotent - names, descriptions, markers, sources and methods stay equal")
    void testRefreshIdempotent() {
        // Two tools - one plain JmixAiTool, one with a marker - to cover both name/description
        // and marker-set comparisons.
        SimpleToolA toolA = new SimpleToolA();
        MarkerATool toolB = new MarkerATool();
        buildRegistry(List.of(toolA, toolB));
        List<ResolvedAiTool> first = registry.getAll();
        assertEquals(2, first.size());

        // Snapshot every observable attribute of each ResolvedAiTool produced by the first refresh.
        List<String> firstNames = first.stream().map(ResolvedAiTool::getName).toList();
        List<String> firstDescriptions = first.stream().map(ResolvedAiTool::getDescription).toList();
        List<Set<Class<? extends JmixAiTool>>> firstMarkers = first.stream().map(ResolvedAiTool::getMarkers).toList();
        List<JmixAiTool> firstSources = first.stream().map(ResolvedAiTool::getSource).toList();
        List<Method> firstMethods = first.stream().map(ResolvedAiTool::getMethod).toList();

        // Refresh again - a fresh ResolvedAiTools snapshot is published, but its contents must
        // still describe the same set of tools in the same order.
        ReflectionTestUtils.invokeGetterMethod(registry, "refresh");
        List<ResolvedAiTool> second = registry.getAll();

        assertEquals(2, second.size());
        assertEquals(firstNames, second.stream().map(ResolvedAiTool::getName).toList(),
                "Names must stay equal across refreshes");
        assertEquals(firstDescriptions, second.stream().map(ResolvedAiTool::getDescription).toList(),
                "Descriptions must stay equal across refreshes");
        assertEquals(firstMarkers, second.stream().map(ResolvedAiTool::getMarkers).toList(),
                "Marker sets must stay equal across refreshes");
        // Source beans are not rebuilt, so identity is preserved across refreshes.
        List<JmixAiTool> secondSources = second.stream().map(ResolvedAiTool::getSource).toList();
        for (int i = 0; i < firstSources.size(); i++) {
            assertSame(firstSources.get(i), secondSources.get(i),
                    "Source bean at index " + i + " must remain the same instance");
        }
        // Method handles compare equal by declaring class + name + params, even when
        // getDeclaredMethods() returns fresh instances per call.
        assertEquals(firstMethods, second.stream().map(ResolvedAiTool::getMethod).toList(),
                "Reflected Method handles must stay equal");
    }

    private void buildRegistry(List<JmixAiTool> tools) {
        ReflectionTestUtils.setField(registry, "aiTools", tools);
        ReflectionTestUtils.invokeGetterMethod(registry, "refresh");
    }

    interface MarkerA extends JmixAiTool {
    }

    interface MarkerB extends JmixAiTool {
    }

    static class SimpleToolA implements JmixAiTool {
        @Tool(name = "alpha", description = "Alpha description")
        public String alpha() {
            return "a";
        }
    }

    static class SimpleToolB implements JmixAiTool {
        @Tool(name = "beta", description = "Beta description")
        public String beta() {
            return "b";
        }
    }

    static class MultiMethodTool implements JmixAiTool {
        @Tool(name = "first", description = "First")
        public String first() {
            return "1";
        }

        @Tool(name = "second", description = "Second")
        public String second() {
            return "2";
        }
    }

    static class MarkerATool implements MarkerA {
        @Tool(name = "from-a", description = "from-a")
        public String fromA() {
            return "a";
        }
    }

    static class MarkerBTool implements MarkerB {
        @Tool(name = "from-b", description = "from-b")
        public String fromB() {
            return "b";
        }
    }

    static class MultiMarkerTool implements MarkerA, MarkerB {
        @Tool(name = "from-both", description = "from-both")
        public String fromBoth() {
            return "ab";
        }
    }

    static class OverridingTool implements JmixAiTool {
        @Tool(name = "irrelevant-own-name", description = "Overridden alpha description")
        @ToolOverride("alpha")
        public String overriddenAlpha() {
            return "overridden";
        }
    }

    static class SecondOverridingTool implements JmixAiTool {
        @Tool(name = "another-irrelevant-name", description = "Another override of alpha")
        @ToolOverride("alpha")
        public String anotherOverride() {
            return "overridden-2";
        }
    }

    static class ToolWithAlphaName implements JmixAiTool {
        @Tool(name = "alpha", description = "Duplicate alpha without override")
        public String alpha() {
            return "dup";
        }
    }

    static class DanglingOverrideTool implements JmixAiTool {
        @Tool(name = "dangling", description = "Override targeting a missing tool")
        @ToolOverride("does-not-exist")
        public String dangling() {
            return "x";
        }
    }

    static class DanglingOverrideCollidingWithAlpha implements JmixAiTool {
        @Tool(name = "alpha", description = "Dangling override colliding on its own @Tool.name")
        @ToolOverride("does-not-exist")
        public String collides() {
            return "x";
        }
    }

    static class DanglingOverrideFooToMissingX implements JmixAiTool {
        @Tool(name = "foo", description = "Dangling override with @Tool.name=foo targeting missing-X")
        @ToolOverride("missing-X")
        public String x() {
            return "x";
        }
    }

    static class DanglingOverrideFooToMissingY implements JmixAiTool {
        @Tool(name = "foo", description = "Dangling override with @Tool.name=foo targeting missing-Y")
        @ToolOverride("missing-Y")
        public String y() {
            return "y";
        }
    }

    static class MarkerAOriginalTool implements MarkerA {
        @Tool(name = "marker-a-tool", description = "Original")
        public String original() {
            return "orig";
        }
    }

    static class PlainOverrideForMarkerA implements JmixAiTool {
        @Tool(name = "ignored", description = "Plain override")
        @ToolOverride("marker-a-tool")
        public String overridden() {
            return "ovr";
        }
    }

    static class MarkerBOverrideForMarkerA implements MarkerB {
        @Tool(name = "ignored-too", description = "Override that adds MarkerB")
        @ToolOverride("marker-a-tool")
        public String overridden() {
            return "ovr-b";
        }
    }

    static abstract class AbstractBaseTool implements JmixAiTool {
        @Tool(name = "base-method", description = "Inherited base tool")
        public String baseMethod() {
            return "base";
        }
    }

    static class InheritingTool extends AbstractBaseTool {
        @Tool(name = "derived-method", description = "Derived tool")
        public String derivedMethod() {
            return "derived";
        }
    }

    static class SubclassRedefiningBaseMethod extends AbstractBaseTool {
        @Override
        public String baseMethod() {
            // Re-declared without re-applying @Tool; annotation must still be discovered via superclass.
            return "redefined";
        }
    }
}
