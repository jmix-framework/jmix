/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.gui.dynamicattributes;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.filter.ConditionParamBuilder;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.FilterParser;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.CustomCondition;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.cuba.security.global.UserSession;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.common.datastruct.Node;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.FilterImplementation;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import io.jmix.ui.sys.ValuePathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Collections;
import java.util.Objects;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;


/**
 * Extended PickerField.LookupAction. This action requires "join" and "where" clauses. When the lookup screen is
 * opened these clauses are used for creating dynamic filter condition in the Filter component. So the data in the
 * lookup screen is filtered.
 */
public class FilteringLookupAction extends PickerField.LookupAction {

    private ExtendedEntities extendedEntities;
    private String joinClause;
    private String whereClause;

    protected Messages messages = AppBeans.get(Messages.class);

    public FilteringLookupAction(PickerField pickerField, String joinClause, String whereClause) {
        super(pickerField);

        checkNotNullArgument(pickerField.getMetaClass(), "MetaClass for PickerField is not set");

        this.joinClause = joinClause;
        this.whereClause = whereClause;

        extendedEntities = AppBeans.get(ExtendedEntities.class);
    }

    @Override
    protected void afterLookupWindowOpened(Window lookupWindow) {
        boolean found = ComponentsHelper.walkComponents(lookupWindow, screenComponent -> {
            if (!(screenComponent instanceof Filter)) {
                return false;
            } else {
                MetaClass actualMetaClass = ((FilterImplementation) screenComponent).getEntityMetaClass();
                MetaClass propertyMetaClass = extendedEntities.getEffectiveMetaClass(pickerField.getMetaClass());
                if (Objects.equals(actualMetaClass, propertyMetaClass)) {
                    applyFilter(((Filter) screenComponent));
                    return true;
                }
                return false;
            }
        });
        if (!found) {
            Notifications notifications = ComponentsHelper.getScreenContext(pickerField).getNotifications();

            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMainMessage("dynamicAttributes.entity.filter.filterNotFound"))
                    .show();
        }
        AbstractWindow controller = (AbstractWindow) (lookupWindow).getFrameOwner();
        ((DsContextImplementation) controller.getDsContext()).resumeSuspended();
    }

    protected void applyFilter(Filter filterComponent) {
        Metadata metadata = AppBeans.get(Metadata.class);
        FilterEntity filterEntity = metadata.create(FilterEntity.class);
        filterEntity.setComponentId(getFilterComponentPath(filterComponent));
        filterEntity.setName(messages.getMainMessage("dynamicAttributes.entity.filter"));
        filterEntity.setXml(createFilterXml(filterComponent));

        UserSession userSession = AppBeans.get(UserSessionSource.class).getUserSession();
        // TODO: gg, no user?
//        filterEntity.setUser(userSession.getCurrentOrSubstitutedUser());

        filterComponent.setFilterEntity(filterEntity);
        filterComponent.apply(true);
    }

    protected String createFilterXml(Filter filterComponent) {
        ConditionsTree tree = new ConditionsTree();
        CustomCondition condition = createCustomCondition(filterComponent);
        tree.setRootNodes(Collections.singletonList(new Node<>(condition)));
        return AppBeans.get(FilterParser.class).getXml(tree, Param.ValueProperty.VALUE);
    }

    protected CustomCondition createCustomCondition(Filter filterComponent) {
        CustomCondition condition = new CustomCondition(createConditionXmlElement(),
                null,
                getFilterComponentName(filterComponent),
                ((FilterImplementation) filterComponent).getEntityMetaClass());

        condition.setUnary(true);
        condition.setHidden(true);

        condition.setWhere(whereClause.replaceAll("\\?", ":" + condition.getParamName()));
        condition.setJoin(joinClause);

        ConditionParamBuilder paramBuilder = AppBeans.get(ConditionParamBuilder.class);
        Param param = Param.Builder.getInstance().setName(paramBuilder.createParamName(condition))
                .setJavaClass(Boolean.class)
                .setEntityWhere("")
                .setEntityView("")
                .setMetaClass(((FilterImplementation) filterComponent).getEntityMetaClass())
                .setInExpr(true)
                .setRequired(true)
                .build();
        param.setValue(true);
        condition.setParam(param);

        return condition;
    }

    protected Element createConditionXmlElement() {
        Element conditionElement = DocumentHelper.createDocument().addElement("c");
        conditionElement.addAttribute("name", RandomStringUtils.randomAlphabetic(10));
        conditionElement.addAttribute("width", "1");
        conditionElement.addAttribute("type", "CUSTOM");
        conditionElement.addAttribute("locCaption", messages.getMainMessage("dynamicAttributes.filter.conditionName"));
        return conditionElement;
    }

    protected String getFilterComponentName(Filter filterComponent) {
        String filterComponentPath = getFilterComponentPath(filterComponent);
        String[] strings = ValuePathHelper.parse(filterComponentPath);
        return ValuePathHelper.pathSuffix(strings);
    }

    public static String getFilterComponentPath(Filter filter) {
        StringBuilder sb = new StringBuilder(filter.getId() != null ? filter.getId() : "filterWithoutId");
        Frame frame = filter.getFrame();
        while (frame != null) {
            sb.insert(0, ".");
            String s = frame.getId() != null ? frame.getId() : "frameWithoutId";
            if (s.contains(".")) {
                s = "[" + s + "]";
            }
            sb.insert(0, s);
            if (frame instanceof Window) {
                break;
            }
            frame = frame.getFrame();
        }
        return sb.toString();
    }
}