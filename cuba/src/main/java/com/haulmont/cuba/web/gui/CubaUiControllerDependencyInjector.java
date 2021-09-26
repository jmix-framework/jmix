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

package com.haulmont.cuba.web.gui;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.security.global.UserSession;
import io.jmix.core.DevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.MessageBundle;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.UiControllerDependencyInjector;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.sys.UiControllerReflectionInspector.InjectElement;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

public class CubaUiControllerDependencyInjector extends UiControllerDependencyInjector {

    public CubaUiControllerDependencyInjector(ApplicationContext applicationContext,
                                              UiControllerReflectionInspector reflectionInspector) {
        super(applicationContext, reflectionInspector);
    }

    @Nullable
    @Override
    protected Object getInjectedInstance(Class<?> type, String name, InjectElement injectElement, FrameOwner frameOwner,
                                         ScreenOptions options) {
        AnnotatedElement element = injectElement.getElement();

        if (UserSession.class.isAssignableFrom(type)) {
            UserSessionSource userSessionSource = applicationContext.getBean(UserSessionSource.class);
            return userSessionSource.getUserSession();
        } else if (Config.class.isAssignableFrom(type)) {
            Configuration configuration = (Configuration) applicationContext.getBean(Configuration.NAME);
            //noinspection unchecked
            return configuration.getConfig((Class<? extends Config>) type);
        } else if (Datasource.class.isAssignableFrom(type)) {
            checkLegacyFrame("Datasource can be injected only into LegacyFrame inheritors", frameOwner);
            // Injecting a datasource
            return ((LegacyFrame) frameOwner).getDsContext().get(name);
        } else if (DsContext.class.isAssignableFrom(type)) {
            checkLegacyFrame("DsContext can be injected only into LegacyFrame inheritors", frameOwner);
            // Injecting the DsContext
            return ((LegacyFrame) frameOwner).getDsContext();
        } else if (DataSupplier.class.isAssignableFrom(type)) {
            checkLegacyFrame("DataSupplier can be injected only into LegacyFrame inheritors", frameOwner);
            // Injecting the DataSupplier
            return ((LegacyFrame) frameOwner).getDsContext().getDataSupplier();
        } else if (WindowManager.class.isAssignableFrom(type)) {
            return UiControllerUtils.getScreenContext(frameOwner).getScreens();
        } else if (Logger.class == type && element instanceof Field) {
            // injecting logger
            return LoggerFactory.getLogger(((Field) element).getDeclaringClass());
        }

        return super.getInjectedInstance(type, name, injectElement, frameOwner, options);
    }

    protected void checkLegacyFrame(String message, FrameOwner frameOwner) {
        if (!(frameOwner instanceof LegacyFrame)) {
            throw new DevelopmentException(message);
        }
    }

    @Override
    protected MessageBundle createMessageBundle(AnnotatedElement element, FrameOwner frameOwner, Frame frame) {
        MessageBundle messageBundle = super.createMessageBundle(element, frameOwner, frame);

        if (frame instanceof Component.HasXmlDescriptor) {
            Element xmlDescriptor = ((Component.HasXmlDescriptor) frame).getXmlDescriptor();
            if (xmlDescriptor != null) {
                String messagePack = xmlDescriptor.attributeValue("messagesPack");
                if (messagePack != null) {
                    messageBundle.setMessageGroup(messagePack);
                }
            }
        }

        return messageBundle;
    }
}
