/*
 * Copyright 2021 Haulmont.
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

package test_support.ui_controller_dependency_injector;

import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.sys.ControllerDependencyInjector;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Component("ui_TestScreenDependencyInjector")
public class TestScreenDependencyInjector implements ControllerDependencyInjector {

    public static final String STR_VALUE = "strValue";

    @Override
    public void inject(InjectionContext injectionContext) {
        FrameOwner frameOwner = injectionContext.getFrameOwner();
        Class screenClass = frameOwner.getClass();
        List<Field> fields = Arrays.asList(screenClass.getDeclaredFields());

        for (Field field : fields) {
            if (field.isAnnotationPresent(TestScreenParam.class)) {
                field.setAccessible(true);
                try {
                    field.set(frameOwner, STR_VALUE);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("CDI - Unable to assign value to field ");
                }
            }
        }
    }
}
