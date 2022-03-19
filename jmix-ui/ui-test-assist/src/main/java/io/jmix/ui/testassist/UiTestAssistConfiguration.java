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

package io.jmix.ui.testassist;

import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.spring.VaadinConfiguration;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.ui.AppUI;
import io.jmix.ui.testassist.ui.TestAppUI;
import io.jmix.ui.testassist.ui.TestVaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Locale;

@Configuration
@Import({VaadinConfiguration.class})
public class UiTestAssistConfiguration {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    protected VaadinSession vaadinSession;

    @EventListener
    public void init(ContextRefreshedEvent event) {
        // saving session to avoid it be GC'ed
        VaadinSession.setCurrent(vaadinSession = createTestVaadinSession());

        formatStringsRegistry.setFormatStrings(Locale.ENGLISH, new FormatStrings(
                '.', ',',
                "#,##0", "#,##0.###", "#,##0.##",
                "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM/yyyy HH:mm Z", "HH:mm", "HH:mm Z",
                "True", "False"));
    }

    @Bean("ui_testAppUi")
    @Primary
    public AppUI testAppUI() {
        return new TestAppUI();
    }

    protected VaadinSession createTestVaadinSession() {
        return new TestVaadinSession(new WebBrowser(), Locale.ENGLISH);
    }
}
