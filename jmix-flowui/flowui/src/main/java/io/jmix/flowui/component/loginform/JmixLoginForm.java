/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.loginform;

import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Locale;

public class JmixLoginForm extends EnhancedLoginForm implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
    }

    protected void autowireDependencies() {
        messageTools = applicationContext.getBean(MessageTools.class);
    }

    @Override
    protected void setupLocale(Locale locale) {
        VaadinSession.getCurrent().setLocale(locale);
    }

    @Override
    protected String localeToString(Locale locale) {
        return LocaleResolver.localeToString(locale);
    }

    @Override
    protected Locale localeFromString(String locale) {
        return LocaleResolver.resolve(locale);
    }

    @Override
    protected String applyDefaultValueFormat(Locale locale) {
        return messageTools.getLocaleDisplayName(locale);
    }
}
