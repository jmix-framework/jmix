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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProviderResultAbiTest {

    static final String PROVIDER = "io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentProvider";
    static final String CONTEXT = PROVIDER + "$ComponentCreationContext";

    static final String VIEW_XML = """
            <view xmlns="http://jmix.io/schema/flowui/view">
                <layout>
                    <button id="okBtn" width="10em"/>
                </layout>
            </view>""";
    // Unprefixed path steps never match namespaced elements under XPath 1.0 semantics
    // (the default xmlns on <view> does not apply to unprefixed name tests), so match by local-name().
    static final String BUTTON_XPATH = "/*[local-name()='view']/*[local-name()='layout']/*[local-name()='button']";

    Object newContext(Object... args) throws Exception {
        Class<?> contextClass = Class.forName(CONTEXT);
        Class<?>[] types = args.length == 2
                ? new Class<?>[]{String.class, String.class}
                : new Class<?>[]{String.class, String.class, Object.class};
        Constructor<?> constructor = contextClass.getConstructor(types);
        constructor.trySetAccessible();
        return constructor.newInstance(args);
    }

    Method providerMethod(String name) throws Exception {
        Method method = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst().orElseThrow();
        method.trySetAccessible();
        return method;
    }

    @Test
    void testTwoArgContextAndCreateComponentKeepWorking() throws Exception {
        Object component = providerMethod("createComponent").invoke(null, newContext(VIEW_XML, BUTTON_XPATH));
        assertInstanceOf(Component.class, component);
    }

    @Test
    void testThreeArgContextWithNullEnvironmentStillCreatesComponent() throws Exception {
        Object component = providerMethod("createComponent")
                .invoke(null, newContext(VIEW_XML, BUTTON_XPATH, null));
        assertInstanceOf(Component.class, component);
    }

    @Test
    void testCreateComponentIsStaticSingleArgReturningComponent() throws Exception {
        Method method = providerMethod("createComponent");
        assertTrue(Modifier.isStatic(method.getModifiers()));
        assertEquals(Component.class, method.getReturnType());
        assertEquals(1, method.getParameterCount());
        assertEquals(CONTEXT, method.getParameterTypes()[0].getName());
    }

    @Test
    void testProviderHasNoOverloadsOfFrozenMethods() throws Exception {
        long createComponentCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("createComponent")).count();
        long canCreateCount = Arrays.stream(Class.forName(PROVIDER).getDeclaredMethods())
                .filter(method -> method.getName().equals("canCreateComponent")).count();
        assertEquals(1, createComponentCount);
        assertEquals(1, canCreateCount);
    }

    @Test
    public void buildsFullPreviewContent_isPublicStaticNoArgTrue() throws Exception {
        Method m = Class.forName(PROVIDER).getMethod("buildsFullPreviewContent");
        m.trySetAccessible();
        assertTrue(Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()));
        assertEquals(0, m.getParameterCount());
        assertEquals(Boolean.TYPE, m.getReturnType());
        assertEquals(Boolean.TRUE, m.invoke(null));
    }
}
