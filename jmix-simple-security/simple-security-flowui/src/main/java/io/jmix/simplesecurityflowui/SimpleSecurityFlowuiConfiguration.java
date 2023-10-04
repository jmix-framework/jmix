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

package io.jmix.simplesecurityflowui;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.Messages;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.simplesecurity.SimpleSecurityConfiguration;
import io.jmix.simplesecurityflowui.authentication.LoginViewSupport;
import io.jmix.simplesecurityflowui.constraint.SecurityConstraintRegistration;
import io.jmix.simplesecurityflowui.constraint.SpecificConstraintImpl;
import io.jmix.simplesecurityflowui.constraint.UiMenuConstraint;
import io.jmix.simplesecurityflowui.constraint.UiShowViewConstraint;
import io.jmix.simplesecurityflowui.exception.AccessDeniedExceptionHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Collections;

@Configuration
@JmixModule(dependsOn = {CoreConfiguration.class, SimpleSecurityConfiguration.class})
public class SimpleSecurityFlowuiConfiguration {

    @Bean("simsec_LoginViewSupport")
    public LoginViewSupport loginViewSupport() {
        return new LoginViewSupport();
    }

    @Bean("simsec_UiMenuConstraint")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public UiMenuConstraint uiMenuConstraint(ViewRegistry viewRegistry, AccessAnnotationChecker accessAnnotationChecker) {
        return new UiMenuConstraint(viewRegistry, accessAnnotationChecker);
    }

    @Bean("simsec_UiShowViewConstraint")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public UiShowViewConstraint uiShowViewConstraint(ViewRegistry viewRegistry, AccessAnnotationChecker accessAnnotationChecker) {
        return new UiShowViewConstraint(viewRegistry, accessAnnotationChecker);
    }

//    @Bean("simsec_SpecificConstraint")
//    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//    public SpecificConstraintImpl specificConstraint(CurrentAuthentication currentAuthentication) {
//        return new SpecificConstraintImpl(currentAuthentication);
//    }

    @Bean("simsec_SecurityConstraintRegistration")
    public SecurityConstraintRegistration securityConstraintRegistration(AccessConstraintsRegistry accessConstraintsRegistry,
                                                                         BeanFactory beanFactory) {
        return new SecurityConstraintRegistration(accessConstraintsRegistry, beanFactory);
    }

    @Bean("simsec_AccessDeniedExceptionHandler")
    public AccessDeniedExceptionHandler accessDeniedExceptionHandler(Notifications notifications, Messages messages) {
        return new AccessDeniedExceptionHandler(notifications, messages);
    }


//    @Bean("simsec_ViewControllersConfiguration")
//    public ViewControllersConfiguration views(ApplicationContext applicationContext,
//                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
//        ViewControllersConfiguration viewControllers
//                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
//        viewControllers.setBasePackages(Collections.singletonList("io.jmix.simplesecurityflowui.view"));
//        return viewControllers;
//    }

    @Bean("simsec_ActionsConfiguration")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.simplesecurityflowui.action"));
        return actionsConfiguration;
    }
}
