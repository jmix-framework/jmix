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

package io.jmix.flowui.impl;

import com.google.common.base.Strings;
import io.jmix.core.ClassManager;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.Actions;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.sys.ActionDefinition;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ActionsConfigurationSorter;
import io.jmix.flowui.sys.BeanUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link Actions} interface that manages the creation and registration
 * of action types annotated with {@link ActionType}. It initializes and resolves action classes,
 * making them available for use within the application context.
 */
@Component("flowui_Actions")
public class ActionsImpl implements Actions, ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(ActionsImpl.class);

    protected List<ActionsConfiguration> configurations = Collections.emptyList();
    protected ClassManager classManager;
    protected ApplicationContext applicationContext;
    protected ActionsConfigurationSorter actionsConfigurationSorter;
    protected AnnotationScanMetadataReaderFactory metadataReaderFactory;

    protected Map<String, Class<? extends Action>> classes = new HashMap<>();

    @Autowired(required = false)
    public void setConfigurations(List<ActionsConfiguration> configurations) {
        this.configurations = configurations;
    }

    @Autowired
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setActionsConfigurationSorter(ActionsConfigurationSorter actionsConfigurationSorter) {
        this.actionsConfigurationSorter = actionsConfigurationSorter;
    }

    @Autowired
    public void setMetadataReaderFactory(AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        this.metadataReaderFactory = metadataReaderFactory;
    }

    @PostConstruct
    protected void postConstruct() {
        //sort ActionsConfiguration list in the same order as Jmix modules. In this case actions overridden
        //in add-ons or application will replace original action definitions
        this.configurations = actionsConfigurationSorter.sort(this.configurations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Action> T create(String actionTypeId) {
        Class<? extends Action> actionClass = classes.get(actionTypeId);
        if (actionClass == null) {
            throw new IllegalArgumentException("Unable to find action type: " + actionTypeId);
        }

        return (T) createAction(actionClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Action> T create(String actionTypeId, String id) {
        Class<? extends Action> actionClass = classes.get(actionTypeId);
        if (actionClass == null) {
            throw new IllegalArgumentException("Unable to find action type: " + actionTypeId);
        }

        return (T) createAction(actionClass, id);
    }

    protected Action createAction(Class<? extends Action> actionClass) {
        Constructor<? extends Action> constructor;
        try {
            constructor = actionClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Unable to get constructor for '%s' action", actionClass), e);
        }

        try {
            Action instance = constructor.newInstance();
            BeanUtil.autowireContext(applicationContext, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Error creating the '%s' action instance", actionClass), e);
        }
    }

    protected Action createAction(Class<? extends Action> actionClass, String id) {
        Constructor<? extends Action> constructor;
        try {
            constructor = actionClass.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Unable to get constructor for '%s' action", actionClass), e);
        }

        try {
            Action instance = constructor.newInstance(id);
            BeanUtil.autowireContext(applicationContext, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Error creating the '%s' action instance", actionClass), e);
        }
    }

    protected Class<? extends Action> resolveActionClass(Class<? extends Action> actionClass) {
        ActionType annotation = actionClass.getAnnotation(ActionType.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("No @%s annotation for class %s", ActionType.class.getSimpleName(), actionClass));
        }

        Class<? extends Action> resolvedClass = classes.get(annotation.value());
        if (resolvedClass == null) {
            throw new IllegalStateException(String.format("Unable to resolve Action with @%s %s",
                    ActionType.class.getSimpleName(), actionClass));
        }

        if (!actionClass.isAssignableFrom(resolvedClass)) {
            throw new IllegalStateException(String.format("%s %s is not assignable from %s",
                    ActionType.class.getSimpleName(), actionClass, resolvedClass));
        }

        return resolvedClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
        // here we receive events for all child contexts
        if (event.getApplicationContext() == this.applicationContext) {
            long startTime = System.currentTimeMillis();

            Map<String, String> squashedMap = new HashMap<>();

            for (ActionsConfiguration configuration : configurations) {
                for (ActionDefinition actionDefinition : configuration.getActions()) {
                    squashedMap.put(actionDefinition.getId(), actionDefinition.getActionClass());
                }
            }

            classes.clear();

            for (Map.Entry<String, String> entry : squashedMap.entrySet()) {
                Class clazz = classManager.loadClass(entry.getValue());
                classes.put(entry.getKey(), clazz);
            }

            log.debug("Actions initialized in {} ms", System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Reloads an action class for hot-deploy.
     *
     * @param className action class name
     */
    public void loadActionClass(String className) {
        MetadataReader metadataReader = null;
        try {
            metadataReader = metadataReaderFactory.getMetadataReader(className);
        } catch (IOException e) {
            log.debug("Unable to get MetadataReader for action class: {}", className);
        }

        if (metadataReader == null || !isCandidateAction(metadataReader)) {
            return;
        }

        Map<String, Object> actionTypeAnnotation =
                metadataReader.getAnnotationMetadata().getAnnotationAttributes(ActionType.class.getName());

        String actionTypeId = null;
        if (actionTypeAnnotation != null) {
            actionTypeId = (String) actionTypeAnnotation.get(ActionType.VALUE_ATTRIBUTE);
        }

        if (Strings.isNullOrEmpty(actionTypeId)) {
            actionTypeId = metadataReader.getClassMetadata().getClassName();
        }

        ActionDefinition actionDefinition = new ActionDefinition(actionTypeId, className);
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);

        actionsConfiguration.setExplicitDefinitions(Collections.singletonList(actionDefinition));
        configurations.add(actionsConfiguration);

        //noinspection unchecked
        Class<? extends Action> actionClass = ((Class<? extends Action>) classManager.loadClass(className));
        classes.put(actionTypeId, actionClass);

        log.debug("Reloaded action class: {}", actionClass.getName());
    }

    protected boolean isCandidateAction(MetadataReader metadataReader) {
        return metadataReader.getClassMetadata().isConcrete()
                && metadataReader.getAnnotationMetadata().hasAnnotation(ActionType.class.getName());
    }
}
