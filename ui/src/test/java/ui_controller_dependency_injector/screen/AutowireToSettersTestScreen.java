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

package ui_controller_dependency_injector.screen;

import io.jmix.core.Messages;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.formatter.NumberFormatter;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("autowire-to-setters-test-screen.xml")
public class AutowireToSettersTestScreen extends Screen {

    public BeanFactory beanFactory;
    public Messages messages;
    public ScreenBuilders screenBuilders;
    public Screens screens;
    public Button button;
    public ObjectProvider<NumberFormatter> numberFormatterProvider;

    @Autowired
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setScreens(Screens screens) {
        this.screens = screens;
    }

    @Autowired
    public void setButton(Button button) {
        this.button = button;
    }

    @Autowired
    public void setNumberFormatterProvider(ObjectProvider<NumberFormatter> numberFormatterProvider) {
        this.numberFormatterProvider = numberFormatterProvider;
    }
}
