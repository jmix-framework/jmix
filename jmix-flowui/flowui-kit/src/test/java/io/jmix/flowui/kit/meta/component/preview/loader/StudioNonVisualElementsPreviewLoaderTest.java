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

package io.jmix.flowui.kit.meta.component.preview.loader;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.meta.component.preview.StandardComponentsLoaderAccess;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioNonVisualElementsPreviewLoaderTest {

    static final Namespace VIEW_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioNonVisualElementsPreviewLoader loader = new StudioNonVisualElementsPreviewLoader();
    final StudioPreviewComponentLoader componentsLoader = StandardComponentsLoaderAccess.loader();

    BaseElement element(String name) {
        return new BaseElement(name, VIEW_NS);
    }

    @Test
    void testSupportsDataBlockElements() {
        for (String name : new String[]{"data", "instance", "collection", "keyValueInstance",
                "keyValueCollection", "loader", "fetchPlan", "query", "properties", "property",
                "condition", "and", "or"}) {
            assertTrue(loader.isSupported(element(name)), name);
        }
    }

    @Test
    void testSupportsFacetsBlockElements() {
        for (String name : new String[]{"facets", "dataLoadCoordinator", "refresh",
                "urlQueryParameters", "pagination", "dataGridFilter", "timer", "settings"}) {
            assertTrue(loader.isSupported(element(name)), name);
        }
    }

    /** Runtime-invisible elements preview as an invisible stand-in, never as a visible box. */
    @Test
    void testBuildsInvisibleStandIn() {
        Component component = loader.load(element("data"), element("view"));

        assertNotNull(component);
        assertFalse(component.isVisible());
    }

    @Test
    void testDoesNotSupportOtherNamespacesOrUnknownTags() {
        assertFalse(loader.isSupported(new BaseElement("data", Namespace.get("http://other"))));
        assertFalse(loader.isSupported(element("vbox")));
        assertFalse(loader.isSupported(element("someUnknownTag")));
    }

    /**
     * These tag names exist both as non-visual children (of {@code urlQueryParameters} /
     * {@code settings}) and as real components. This loader is registered through the SPI, so it is
     * consulted before the standard components loader - claiming them would hijack the component.
     */
    @Test
    void testDoesNotClaimTagsThatAreAlsoRealComponents() {
        for (String name : new String[]{"genericFilter", "propertyFilter", "component"}) {
            assertFalse(loader.isSupported(element(name)), name);
        }
    }

    /** The component loader must keep claiming the colliding names. */
    @Test
    void testCollidingNamesStillClaimedByComponentsLoader() {
        assertTrue(componentsLoader.isSupported(element("genericFilter")));
        assertTrue(componentsLoader.isSupported(element("propertyFilter")));
    }

    /** No tag may be claimed by both loaders, or which one wins would depend on SPI order. */
    @Test
    void testNoOverlapWithComponentsLoader() {
        for (String name : StudioNonVisualElementsPreviewLoader.supportedElements()) {
            assertFalse(componentsLoader.isSupported(element(name)),
                    "claimed by both loaders: " + name);
        }
    }

    /**
     * Tag names that mean an element in one place and a component in another (Studio's
     * {@code FlowComponentLibrary.ELEMENT_AS_COMPONENT_NAMES}). Studio disambiguates them by walking
     * up the parents, which the framework cannot do at claim time: {@code canCreateComponent} builds a
     * detached {@code BaseElement} with no parent, so a loader only ever sees the tag name there.
     * <p>
     * Hence the rule this test pins: this loader may claim an ambiguous name only when *every* meaning
     * is non-visual. {@code instance}/{@code collection} qualify - a data container and a nested
     * container are both invisible. The rest must be left to the components loader, or a nested
     * {@code icon}/{@code header}/{@code select} would preview as an invisible stub.
     */
    @Test
    void testAmbiguousNamesAreOnlyClaimedWhenEveryMeaningIsNonVisual() {
        for (String name : new String[]{"instance", "collection"}) {
            assertTrue(loader.isSupported(element(name)), name);
        }
        for (String name : new String[]{"propertyFilter", "jpqlFilter", "component",
                "header", "select", "icon", "image", "svgIcon", "fontIcon", "content"}) {
            assertFalse(loader.isSupported(element(name)),
                    "ambiguous name with a visual meaning must not be claimed here: " + name);
        }
    }
}
