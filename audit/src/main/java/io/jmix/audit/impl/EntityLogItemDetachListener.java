/*
 * Copyright 2019 Haulmont.
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

package io.jmix.audit.impl;

import com.google.common.collect.Ordering;
import io.jmix.audit.entity.EntityLogAttr;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.core.EntityStates;
import io.jmix.data.listener.BeforeDetachEntityListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.StringReader;
import java.util.*;

import static io.jmix.audit.entity.EntityLogAttr.*;

@Component("audit_EntityLogItemDetachListener")
public class EntityLogItemDetachListener implements BeforeDetachEntityListener<EntityLogItem> {

    private final Logger log = LoggerFactory.getLogger(EntityLogItemDetachListener.class);


    @Autowired
    protected EntityStates entityStates;

    protected final String[] skipNames = new String[]{VALUE_ID_SUFFIX,
            MP_SUFFIX, OLD_VALUE_SUFFIX, OLD_VALUE_ID_SUFFIX};

    @Override
    public void onBeforeDetach(EntityLogItem item) {
        if (item.getAttributes() != null)
            return;

        fillAttributesFromChangesField(item);
    }

    protected void fillAttributesFromChangesField(EntityLogItem item) {
        log.trace("fillAttributesFromChangesField for {}", item);
        List<EntityLogAttr> attributes = new ArrayList<>();

        if (!entityStates.isLoaded(item, "changes")) {
            item.setAttributes(new LinkedHashSet<>(attributes));
            return;
        }

        StringReader reader = new StringReader(item.getChanges());
        Properties properties = new Properties();
        try {
            properties.load(reader);
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if (StringUtils.endsWithAny(name, skipNames))
                    continue;

                EntityLogAttr attr = new EntityLogAttr();
                attr.setId(UUID.randomUUID());
                attr.setLogItem(item);
                attr.setName(name);
                attr.setValue(properties.getProperty(name));
                attr.setValueId(properties.getProperty(name + VALUE_ID_SUFFIX));
                attr.setOldValue(properties.getProperty(name + OLD_VALUE_SUFFIX));
                attr.setOldValueId(properties.getProperty(name + OLD_VALUE_ID_SUFFIX));
                attr.setMessagesPack(properties.getProperty(name + MP_SUFFIX));

                attributes.add(attr);
            }
        } catch (Exception e) {
            log.error("Unable to fill EntityLog attributes for {}", item, e);
        }

        attributes.sort((o1, o2) -> Ordering.natural().compare(o1.getName(), o2.getName()));

        item.setAttributes(new LinkedHashSet<>(attributes));
    }
}
