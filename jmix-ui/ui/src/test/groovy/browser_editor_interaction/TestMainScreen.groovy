/*
 * Copyright 2020 Haulmont.
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

package browser_editor_interaction

import io.jmix.ui.UiComponents
import io.jmix.ui.component.AppWorkArea
import io.jmix.ui.component.Window
import io.jmix.ui.screen.Screen
import io.jmix.ui.screen.UiController
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.Nullable

@UiController("main")
class TestMainScreen extends Screen implements Window.HasWorkArea {

    @Autowired
    private UiComponents uiComponents

    private AppWorkArea workArea

    TestMainScreen() {
        addInitListener {
            workArea = uiComponents.create(AppWorkArea.class)
            getWindow().add(workArea)
        }
    }

    @Nullable
    @Override
    AppWorkArea getWorkArea() {
        return workArea
    }
}
