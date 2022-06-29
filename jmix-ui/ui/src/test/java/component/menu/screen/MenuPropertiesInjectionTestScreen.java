/*
 * Copyright (c) 2020 Haulmont.
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

package component.menu.screen;

import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import test_support.entity.sales.Order;

@UiController("test_MenuPropertiesInjectionTestScreen")
public class MenuPropertiesInjectionTestScreen extends Screen {

    public int testIntProperty;

    public String testStringProperty;

    public Order entityToEdit;

    public void setTestIntProperty(int testIntProperty) {
        this.testIntProperty = testIntProperty;
    }

    public void setTestStringProperty(String testStringProperty) {
        this.testStringProperty = testStringProperty;
    }

    public void setEntityToEdit(Order entityToEdit) {
        this.entityToEdit = entityToEdit;
    }
}
