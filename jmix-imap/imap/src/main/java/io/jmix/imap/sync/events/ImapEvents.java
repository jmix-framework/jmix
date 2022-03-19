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

package io.jmix.imap.sync.events;

import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.imap.ImapEventsGenerator;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.entity.*;
import io.jmix.imap.events.BaseImapEvent;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component("imap_Events")
public class ImapEvents {

    private final static Logger log = LoggerFactory.getLogger(ImapEvents.class);

    @Autowired
    protected ApplicationEventPublisher events;

    @Autowired
    protected SystemAuthenticator authentication;

    @Autowired
    protected ImapDataProvider imapDataProvider;

    @Autowired
    protected ApplicationContext applicationContext;

    public void init(ImapMailBox mailBox) {
        getEventsGenerator(mailBox).init(mailBox);
    }

    public void shutdown(ImapMailBox mailBox) {
        getEventsGenerator(mailBox).shutdown(mailBox);
    }

    public void handleNewMessages(ImapFolder folder) {
        fireEvents(folder, getEventsGenerator(folder.getMailBox()).generateForNewMessages(folder));
    }

    public void handleChangedMessages(ImapFolder folder) {
        fireEvents(folder, getEventsGenerator(folder.getMailBox()).generateForChangedMessages(folder));
    }

    public void handleMissedMessages(ImapFolder folder) {
        fireEvents(folder, getEventsGenerator(folder.getMailBox()).generateForMissedMessages(folder));
    }

    protected ImapEventsGenerator getEventsGenerator(ImapMailBox mailBox) {
        String eventsGeneratorClassName = mailBox.getEventsGeneratorClass();
        if (eventsGeneratorClassName != null) {
            Class<?> eventsGeneratorClass = ReflectionHelper.getClass(eventsGeneratorClassName);
            Map<String, ?> beans = applicationContext.getBeansOfType(eventsGeneratorClass);
            if (MapUtils.isEmpty(beans)) {
                return getStandardEventsGenerator();
            }
            Map.Entry<String, ?> bean = beans.entrySet().iterator().next();
            if (!(bean.getValue() instanceof ImapEventsGenerator)) {
                log.warn("Bean {} is not implementation of ImapEventsGenerator interface", bean.getKey());
                return getStandardEventsGenerator();
            }
            return (ImapEventsGenerator) bean.getValue();
        }
        return getStandardEventsGenerator();
    }

    protected ImapEventsGenerator getStandardEventsGenerator() {
        return (ImapEventsGenerator) applicationContext.getBean("imap_StandardEventsGenerator");
    }

    protected void fireEvents(ImapFolder folder, Collection<? extends BaseImapEvent> imapEvents) {
        log.trace("Fire events {} for {}", imapEvents, folder);

        if (CollectionUtils.isEmpty(imapEvents)) {
            return;
        }

        filterEvents(folder, imapEvents);

        log.debug("Filtered events for {}: {}", folder.getId(), imapEvents);
        authentication.begin();
        try {
            ImapFolder freshFolder = imapDataProvider.findFolder(folder);
            imapEvents.forEach(event -> {
                log.trace("firing event {}", event);
                events.publishEvent(event);

                List<ImapEventHandler> eventHandlers = ImapEventType.getByEventType(event.getClass()).stream()
                        .map(freshFolder::getEvent)
                        .filter(Objects::nonNull)
                        .filter(folderEvent -> BooleanUtils.isTrue(folderEvent.getEnabled()))
                        .map(ImapFolderEvent::getEventHandlers)
                        .filter(handlers -> !CollectionUtils.isEmpty(handlers))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                log.trace("firing event {} using handlers {}", event, eventHandlers);
                invokeAttachedHandlers(event, freshFolder, eventHandlers);

                log.trace("finish processing event {}", event);
            });
        } finally {
            authentication.end();
        }
    }

    protected void filterEvents(ImapFolder folder, Collection<? extends BaseImapEvent> imapEvents) {
        for (ImapEventType eventType : ImapEventType.values()) {
            if (!folder.hasEvent(eventType)) {
                imapEvents.removeIf(event -> eventType.getEventClass().equals(event.getClass()));
            }
        }
    }

    protected void invokeAttachedHandlers(BaseImapEvent event, ImapFolder folder, List<ImapEventHandler> handlers) {
        log.trace("{}: invoking handlers {} for event {}", folder.getName(), handlers, event);

        for (ImapEventHandler handler : handlers) {
            Object bean = applicationContext.getBean(handler.getBeanName());
            if (bean == null) {
                log.warn("No bean {} is available, check the folder {} configuration", handler.getBeanName(), folder);
                return;
            }
            Class<? extends BaseImapEvent> eventClass = event.getClass();
            try {
                authentication.begin();
                List<Method> methods = Arrays.stream(bean.getClass().getMethods())
                        .filter(m -> m.getName().equals(handler.getMethodName()))
                        .filter(m -> m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isAssignableFrom(eventClass))
                        .collect(Collectors.toList());
                log.trace("{}: methods to invoke: {}", handler, methods);
                if (CollectionUtils.isEmpty(methods)) {
                    log.warn("No method {} for bean {} is available, check the folder {} configuration",
                            handler.getMethodName(), handler.getBeanName(), folder);
                }

                for (Method method : methods) {
                    method.invoke(bean, event);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Can't invoke bean for imap folder event", e);
            } finally {
                authentication.end();
            }
        }
    }
}
