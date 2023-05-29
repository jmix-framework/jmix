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
 *
 */

package com.haulmont.cuba.gui.components.filter;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.FilterConditionUtils;
import com.haulmont.cuba.gui.components.filter.condition.GroupCondition;
import com.haulmont.cuba.gui.components.filter.dateinterval.DateIntervalValue;
import com.haulmont.cuba.security.entity.FilterEntity;
import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.datastruct.Node;
import com.haulmont.cuba.core.global.filter.Op;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppliedFilter {

    private FilterEntity filterEntity;

    protected Messages messages = AppBeans.get(Messages.class);
    protected ConditionsTree conditions;

    public AppliedFilter(FilterEntity filterEntity, ConditionsTree conditions) {
        this.filterEntity = filterEntity;
        this.conditions = conditions.createCopy();
    }

    public String getText() {
        String name = filterEntity.getName();
        if (StringUtils.isBlank(name)) {
            name = messages.getMainMessage(filterEntity.getCode());
        }
        StringBuilder sb = new StringBuilder(name);

        List<Node<AbstractCondition>> visibleRootNodesWithValues = new ArrayList<>();
        for (Node<AbstractCondition> rootNode : conditions.getRootNodes()) {
            AbstractCondition condition = rootNode.getData();
            if (!condition.getHidden() && (condition.isGroup() || condition.getParam() != null && condition.getParam().getValue() != null))
                visibleRootNodesWithValues.add(rootNode);
        }

        Iterator<Node<AbstractCondition>> iterator = visibleRootNodesWithValues.iterator();
        if (iterator.hasNext())
            sb.append(": ");
        while (iterator.hasNext()) {
            Node<AbstractCondition> rootNode = iterator.next();
            recursivelyCreateConditionCaption(rootNode, sb);
            if (iterator.hasNext())
                sb.append(", ");
        }

        return sb.toString();
    }

    protected void recursivelyCreateConditionCaption(Node<AbstractCondition> node, StringBuilder sb) {
        AbstractCondition condition = node.getData();
        if (condition.getHidden()) return;
        if (condition.isGroup()) {
            GroupType groupType = ((GroupCondition) condition).getGroupType();
            sb.append(groupType.getLocCaption())
                    .append("(");

            List<Node<AbstractCondition>> visibleChildNodes = new ArrayList<>();
            for (Node<AbstractCondition> childNode : node.getChildren()) {
                AbstractCondition childCondition = childNode.getData();
                if (!childCondition.getHidden() && (childCondition.isGroup() || childCondition.getParam() != null && childCondition.getParam().getValue() != null))
                    visibleChildNodes.add(childNode);
            }

            Iterator<Node<AbstractCondition>> iterator = visibleChildNodes.iterator();
            while (iterator.hasNext()) {
                Node<AbstractCondition> childNode = iterator.next();
                recursivelyCreateConditionCaption(childNode, sb);
                if (iterator.hasNext())
                    sb.append(", ");
            }
            sb.append(")");
        } else {
            Param param = condition.getParam();
            sb.append(condition.getLocCaption()).append(" ");
            if (condition.getOperator() == Op.NOT_EMPTY) {
                if (BooleanUtils.isTrue((Boolean) param.getValue())) {
                    sb.append(messages.getMessage(Op.class, "Op.NOT_EMPTY"));
                } else {
                    sb.append(messages.getMessage(Op.class, "Op.EMPTY"));
                }
            } else {
                sb.append(condition.getOperationCaption())
                        .append(" ")
                        .append(formatParamValue(param));
            }
        }
    }

    protected String formatParamValue(Param param) {
        Object value = param.getValue();
        if (value == null)
            return "";

        if (param.isDateInterval()) {
            DateIntervalValue dateIntervalValue = AppBeans.getPrototype(DateIntervalValue.NAME, value);
            return dateIntervalValue.getLocalizedValue();
        }

        if (value instanceof Entity)
            return AppBeans.get(MetadataTools.class).getInstanceName((Entity) value);

        if (value instanceof Enum)
            return messages.getMessage((Enum) value);

        if (value instanceof ArrayList){
            ArrayList<String> names = new ArrayList<>();
            ArrayList list = ((ArrayList) value);
            for (Object obj : list) {
                if (obj instanceof Entity)
                    names.add(AppBeans.get(MetadataTools.class).getInstanceName(((Entity) obj)));
                else {
                    names.add(FilterConditionUtils.formatParamValue(param, obj));
                }
            }
            return names.toString();
        }

        return FilterConditionUtils.formatParamValue(param, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppliedFilter that = (AppliedFilter) o;

        if (!filterEntity.equals(that.filterEntity)) return false;
        if (!conditions.equals(that.conditions)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filterEntity.hashCode();
        result = 31 * result + conditions.hashCode();
        return result;
    }

    public FilterEntity getFilterEntity() {
        return filterEntity;
    }

    public ConditionsTree getConditions() {
        return conditions;
    }
}
