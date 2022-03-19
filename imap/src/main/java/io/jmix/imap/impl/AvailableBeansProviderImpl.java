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


package io.jmix.imap.impl;

import io.jmix.imap.AvailableBeansProvider;
import io.jmix.imap.ImapEventsGenerator;
import io.jmix.imap.events.BaseImapEvent;
import io.jmix.imap.sync.events.ImapStandardEventsGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component("imap_AvailableBeansProvider")
public class AvailableBeansProviderImpl implements AvailableBeansProvider {

    private final static Logger log = LoggerFactory.getLogger(AvailableBeansProviderImpl.class);

    protected final Map<Class<? extends BaseImapEvent>, EventHandlerBeanMetadata> beanMetas = new HashMap<>();

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Map<String, List<String>> getEventHandlers(Class<? extends BaseImapEvent> eventClass) {
        log.debug("Get available beans to handle {}", eventClass);
        
        beanMetas.putIfAbsent(eventClass, new EventHandlerBeanMetadata(eventClass));

        Map<String, List<String>> availableHandlers = beanMetas.get(eventClass).getAvailableBeans();
        log.debug("{} can be handled by {}", eventClass, availableHandlers);
        return availableHandlers;
    }

    @Override
    public Map<String, String> getEventsGenerators() {
        Map<String, ImapEventsGenerator> eventsGenerators = applicationContext.getBeansOfType(ImapEventsGenerator.class);
        return eventsGenerators.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getClass().getName()))
                .filter(e -> !e.getValue().equals(ImapStandardEventsGenerator.class.getName()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }


    protected class EventHandlerBeanMetadata {

        protected Class<? extends BaseImapEvent> eventClass;

        public EventHandlerBeanMetadata(Class<? extends BaseImapEvent> eventClass) {
            this.eventClass = eventClass;
        }

        protected Map<String, List<String>> getAvailableBeans() {
            Map<String, List<String>> result = new TreeMap<>();
            String[] beanNames = applicationContext.getBeanNamesForAnnotation(Component.class);
            for (String name : beanNames) {
                if (applicationContext.isSingleton(name) && !name.startsWith("org.springframework.")) {
                    List<String> availableMethods = getAvailableMethods(name);
                    if (CollectionUtils.isNotEmpty(availableMethods)) {
                        result.put(name, availableMethods);
                    }
                }
            }

            return result;
        }
        protected List<String> getAvailableMethods(String beanName) {
            List<String> methods = new ArrayList<>();
            try {
                Object bean = applicationContext.getBean(beanName);

                @SuppressWarnings("unchecked")
                List<Class<?>> classes = ClassUtils.getAllInterfaces(bean.getClass());

                for (Class aClass : classes) {
                    if (aClass.getName().startsWith("org.springframework.")) {
                        continue;
                    }

                    for (Method method : aClass.getMethods()) {
                        if (isMethodAvailable(method)) {
                            methods.add(method.getName());
                        }
                    }
                }

                if (CollectionUtils.isEmpty(methods)) {
                    for (Method method : bean.getClass().getMethods()) {
                        if (!method.getDeclaringClass().equals(Object.class) && isMethodAvailable(method)) {
                            methods.add(method.getName());
                        }
                    }
                }

            } catch (Throwable t) {
                log.debug(t.getMessage());
            }
            return methods;
        }

        protected boolean isMethodAvailable(Method method) {
            return method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].isAssignableFrom(eventClass)
                    && BaseImapEvent.class.isAssignableFrom(method.getParameterTypes()[0])
                    && method.getAnnotation(EventListener.class) == null;
        }
    }
}
