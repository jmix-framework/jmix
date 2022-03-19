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

package com.haulmont.cuba.gui.actions.list;

import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.filter.FilterHelper;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Screens;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.ValuePathHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An action that adds a record to the set.
 */
@StudioAction(
        target = "com.haulmont.cuba.gui.components.ListComponent",
        description = "Adds the selected entity to a set")
@ActionType(AddToSetAction.ID)
public class AddToSetAction extends SecuredListAction {

    public static final String ID = "addToSet";

    protected Filter filter;

    protected FilterHelper filterHelper;

    protected UserSessionSource userSessionSource;

    public AddToSetAction() {
        this(ID);
    }

    public AddToSetAction(String id) {
        super(id);
    }

    @Autowired
    public void setFilterHelper(FilterHelper filterHelper) {
        this.filterHelper = filterHelper;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.PLUS_CIRCLE);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(AddToSetAction.class, "actions.AddToSet");
    }

    @Autowired
    public void setUserSessionSource(UserSessionSource userSessionSource) {
        this.userSessionSource = userSessionSource;
    }

    @Override
    public void actionPerform(Component component) {
        MetaClass entityMetaClass;
        if (target.getItems() instanceof EntityDataUnit) {
            entityMetaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        } else {
            throw new UnsupportedOperationException("Unsupported data unit " + target.getItems());
        }

        String query;
        if (filter.getDatasource() != null) {
            query = filter.getDatasource().getQuery();
        } else {
            query = filter.getDataLoader().getQuery();
        }

        String[] strings = ValuePathHelper.parse(CubaComponentsHelper.getFilterComponentPath(filter));
        String componentId = ValuePathHelper.pathSuffix(strings);
        Set ownerSelection = target.getSelected();

        Map<String, Object> params = new HashMap<>();
        params.put("entityType", entityMetaClass.getName());
        params.put("items", ownerSelection);
        params.put("componentPath", CubaComponentsHelper.getFilterComponentPath(filter));
        params.put("componentId", componentId);
        params.put("foldersPane", filterHelper.getFoldersPane());
        params.put("entityClass", entityMetaClass.getJavaClass().getName());
        params.put("query", query);
        params.put("username", userSessionSource.getUserSession().getUser().getUsername());

        Screens screens = ComponentsHelper.getScreenContext(filter).getScreens();
        screens.create("saveSetInFolder", OpenMode.DIALOG, new MapScreenOptions(params))
                .show();
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && (filter != null || searchFilter());
    }

    protected boolean searchFilter() {
        if (target == null) {
            return false;
        }

        return ComponentsHelper.walkComponents(target.getFrame(), component -> {
            if (component instanceof Filter
                    && Objects.equals(((Filter) component).getApplyTo(), target)) {
                setFilter((Filter) component);
                return true;
            }
            return false;
        });
    }

    @Override
    protected boolean isPermitted() {
        return super.isPermitted()
                && filterHelper.isTableActionsEnabled()
                && filterHelper.mainScreenHasFoldersPane(target.getFrame());
    }
}
