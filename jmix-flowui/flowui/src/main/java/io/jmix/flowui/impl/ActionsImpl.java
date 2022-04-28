package io.jmix.flowui.impl;

import io.jmix.core.ClassManager;
import io.jmix.flowui.Actions;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.sys.ActionDefinition;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("flowui_Actions")
public class ActionsImpl implements Actions, ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(ActionsImpl.class);

    protected List<ActionsConfiguration> configurations = Collections.emptyList();
    protected ClassManager classManager;
    protected ApplicationContext applicationContext;

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

    @Override
    public Action create(String actionTypeId) {
        Class<? extends Action> actionClass = classes.get(actionTypeId);
        if (actionClass == null) {
            throw new IllegalArgumentException("Unable to find action type: " + actionTypeId);
        }

        return createAction(actionClass);
    }

    @Override
    public Action create(String actionTypeId, String id) {
        Class<? extends Action> actionClass = classes.get(actionTypeId);
        if (actionClass == null) {
            throw new IllegalArgumentException("Unable to find action type: " + actionTypeId);
        }

        return createAction(actionClass, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Action> T create(Class<T> actionTypeClass) {
        Class<? extends Action> actionClass = resolveActionClass(actionTypeClass);

        return (T) createAction(actionClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Action> T create(Class<T> actionTypeClass, String id) {
        Class<? extends Action> actionClass = resolveActionClass(actionTypeClass);

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
}
