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

package io.jmix.auditui.screen.sessions;

import io.jmix.audit.UserSessions;
import io.jmix.audit.entity.UserSession;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Sort;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.impl.EntityValuesComparator;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("userSessions.browse")
@UiDescriptor("user-sessions-browse.xml")
@LookupComponent("sessionsTable")
@Route("sessions")
public class UserSessionsBrowse extends StandardLookup<UserSession> {

    @Autowired
    protected Table<UserSession> sessionsTable;
    @Autowired
    protected CollectionContainer<UserSession> userSessionsDc;
    @Autowired
    protected CollectionLoader<UserSession> userSessionsDl;
    @Autowired
    protected UserSessions userSessions;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    private TextField<String> userName;
    @Autowired
    private DateField<Date> lastRequestDateFrom;
    @Autowired
    private DateField<Date> lastRequestDateTo;
    @Autowired
    private BeanFactory beanFactory;

    @Install(to = "userSessionsDc", target = Target.DATA_CONTAINER, subject = "sorter")
    private void userSessionsDcSorter(final Sort sort) {
        userSessionsDl.setSort(sort);
        userSessionsDl.load();
    }

    @Install(to = "userSessionsDl", target = Target.DATA_LOADER)
    private List<UserSession> userSessionsDlLoadDelegate(final LoadContext<UserSession> loadContext) {
        Stream<UserSession> sessions = userSessions.sessions();
        if (userName.getValue() != null) {
            sessions = sessions.filter(o -> o.getPrincipalName().toLowerCase().contains(userName.getValue()));
        }
        if (lastRequestDateFrom.getValue() != null) {
            sessions = sessions.filter(o -> o.getLastRequest().after(lastRequestDateFrom.getValue()));
        }
        if (lastRequestDateTo.getValue() != null) {
            sessions = sessions.filter(o -> o.getLastRequest().before(lastRequestDateTo.getValue()));
        }

        Comparator<UserSession> comparator = getUserSessionComparator(loadContext);
        if (comparator != null) {
            sessions = sessions.sorted(comparator);
        }

        return sessions.collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Comparator<UserSession> getUserSessionComparator(LoadContext<UserSession> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        if (query == null) {
            throw new RuntimeException("Query in LoadContext is null.");
        }

        Sort sort = query.getSort();
        if (sort == null || sort.getOrders().isEmpty()) {
            return null;
        }

        List<Sort.Order> orders = sort.getOrders();
        MetaClass metaClass = loadContext.getEntityMetaClass();

        Comparator comparator = createComparator(orders.get(0), metaClass);
        for (int i = 1; i < orders.size(); i++) {
            Sort.Order order = orders.get(i);
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(order.getProperty());
            if (propertyPath == null) {
                throw new IllegalArgumentException("Property " + order.getProperty() + " is invalid");
            }

            boolean asc = order.getDirection() == Sort.Direction.ASC;
            EntityValuesComparator<Object> valuesComparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);
            comparator = comparator.thenComparing(e -> EntityValues.getValueEx(e, propertyPath), valuesComparator);
        }
        return comparator;
    }

    protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
        MetaPropertyPath propertyPath = metaClass.getPropertyPath(sortOrder.getProperty());
        if (propertyPath == null) {
            throw new IllegalArgumentException("Property " + sortOrder.getProperty() + " is invalid");
        }

        boolean asc = sortOrder.getDirection() == Sort.Direction.ASC;
        EntityValuesComparator<Object> comparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);
        return Comparator.comparing(e -> EntityValues.getValueEx(e, propertyPath), comparator);
    }

    @Subscribe("sessionsTable.expire")
    public void onSessionsTableExpire(Action.ActionPerformedEvent event) {
        UserSession session = sessionsTable.getSingleSelected();
        if (session != null) {
            userSessions.invalidate(session);
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.formatMessage(UserSessionsBrowse.class, "sessionInvalidated", session.getSessionId()))
                    .show();
            userSessionsDl.load();
        }
    }

    @Subscribe("clearButton")
    public void onClearButtonClick(Button.ClickEvent event) {
        userName.setValue(null);
        lastRequestDateFrom.setValue(null);
        lastRequestDateTo.setValue(null);

        userSessionsDl.load();
    }

    @Subscribe("sessionsTable.refresh")
    public void onSessionsTableRefresh(Action.ActionPerformedEvent event) {
        userSessionsDl.load();
    }
}
