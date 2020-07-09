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

package io.jmix.uidata;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.uidata.entity.UiTablePresentation;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.PresentationsChangeListener;
import io.jmix.ui.presentation.model.TablePresentation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;

public class TablePresentationsImpl implements TablePresentations {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected CurrentAuthentication authentication;
    @Autowired
    protected EntityStates entityStates;

    protected String name;
    protected Map<Object, TablePresentation> presentations;
    protected TablePresentation current;
    protected TablePresentation def;

    protected Set<TablePresentation> needToUpdate = new HashSet<>();
    protected Set<TablePresentation> needToRemove = new HashSet<>();

    protected List<PresentationsChangeListener> listeners;


    public TablePresentationsImpl(Component c) {
        name = ComponentsHelper.getComponentPath(c);
    }

    @Override
    public void add(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        checkLoad();
        presentations.put(EntityValues.<UUID>getId(p), p);
        if (entityStates.isNew(p)) {
            needToUpdate.add(p);

            if (BooleanUtils.isTrue(p.getDefault())) {
                def = p;
            }
        }
        firePresentationsSetChanged();
    }

    @Override
    public TablePresentation getCurrent() {
        checkLoad();
        return current;
    }

    @Override
    public void setCurrent(@Nullable TablePresentation p) {
        checkLoad();
        if (p == null) {
            Object old = current;
            current = null;
            fireCurrentPresentationChanged(old);
        } else if (presentations.containsKey(EntityValues.<UUID>getId(p))) {
            Object old = current;
            current = p;
            fireCurrentPresentationChanged(old);
        } else {
            throw new IllegalStateException(String.format("Invalid presentation: %s", EntityValues.<UUID>getId(p)));
        }
    }

    @Override
    public String getSettingsString(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        p = getPresentation(EntityValues.<UUID>getId(p));

        if (p == null) {
            return null;
        }

        return StringUtils.isBlank(p.getSettings()) ? null : p.getSettings();
    }

    @Override
    public void setSettings(TablePresentation p, @Nullable String settings) {
        Preconditions.checkNotNullArgument(p);

        p = getPresentation(EntityValues.<UUID>getId(p));
        if (p != null) {
            p.setSettings(settings);
            modify(p);
        }
    }

    @Override
    public TablePresentation getPresentation(Object id) {
        checkLoad();
        return presentations.get(id);
    }

    @Override
    public String getCaption(Object id) {
        TablePresentation p = getPresentation(id);
        if (p != null) {
            return p.getName();
        }
        return null;
    }

    @Override
    public Collection<Object> getPresentationIds() {
        checkLoad();
        return Collections.unmodifiableCollection(presentations.keySet());
    }

    @Override
    public void setDefault(@Nullable TablePresentation p) {
        checkLoad();
        if (p == null) {
            Object old = def;
            def = null;
            fireDefaultPresentationChanged(old);
        } else if (presentations.containsKey(EntityValues.<UUID>getId(p))) {
            Object old = def;
            if (def != null) {
                def.setDefault(false);
            }
            p.setDefault(true);
            def = p;
            fireDefaultPresentationChanged(old);
        } else {
            throw new IllegalStateException(String.format("Invalid presentation: %s", EntityValues.<UUID>getId(p)));
        }
    }

    @Override
    public TablePresentation getDefault() {
        return def;
    }

    @Override
    public void remove(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        checkLoad();
        if (presentations.remove(EntityValues.<UUID>getId(p)) != null) {
            if (entityStates.isNew(p)) {
                needToUpdate.remove(p);
            } else {
                needToUpdate.remove(p);
                needToRemove.add(p);
            }

            if (p.equals(def)) {
                def = null;
            }

            if (p.equals(current)) {
                current = null;
            }

            firePresentationsSetChanged();
        }
    }

    @Override
    public void modify(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        checkLoad();
        if (presentations.containsKey(EntityValues.<UUID>getId(p))) {
            needToUpdate.add(p);
            if (BooleanUtils.isTrue(p.getDefault())) {
                setDefault(p);
            } else if (def != null && EntityValues.<UUID>getId(def).equals(EntityValues.<UUID>getId(p))) {
                setDefault(null);
            }
        } else {
            throw new IllegalStateException(String.format("Invalid presentation: %s", EntityValues.<UUID>getId(p)));
        }
    }

    @Override
    public boolean isAutoSave(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        p = getPresentation(EntityValues.<UUID>getId(p));
        return p != null && BooleanUtils.isTrue(p.getAutoSave());
    }

    @Override
    public boolean isGlobal(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        p = getPresentation(EntityValues.<UUID>getId(p));
        return p != null && !entityStates.isNew(p) && p.getUserLogin() == null;
    }

    @Override
    public void commit() {
        if (!needToUpdate.isEmpty() || !needToRemove.isEmpty()) {
            SaveContext ctx = new SaveContext().saving(needToUpdate).removing(needToRemove);
            Set<Entity> commitResult = dataManager.save(ctx);
            commited(commitResult);

            clearCommitList();

            firePresentationsSetChanged();
        }
    }

    public void commited(Set<Entity> entities) {
        for (Entity entity : entities) {
            if (entity.equals(def))
                setDefault((TablePresentation) entity);
            else if (entity.equals(current))
                current = (TablePresentation) entity;

            if (presentations.containsKey(EntityValues.getId(entity))) {
                presentations.put(EntityValues.getId(entity), (TablePresentation) entity);
            }
        }
    }

    @Override
    public void addListener(PresentationsChangeListener listener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(PresentationsChangeListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listeners = null;
            }
        }
    }

    @Override
    public TablePresentation getPresentationByName(String name) {
        for (TablePresentation p : presentations.values()) {
            if (name.equalsIgnoreCase(p.getName())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public TablePresentation create() {
        return metadata.create(UiTablePresentation.class);
    }

    protected void fireCurrentPresentationChanged(Object oldPresentationId) {
        if (listeners != null) {
            for (final PresentationsChangeListener listener : listeners) {
                listener.currentPresentationChanged(this, oldPresentationId);
            }
        }
    }

    protected void firePresentationsSetChanged() {
        if (listeners != null) {
            for (final PresentationsChangeListener listener : listeners) {
                listener.presentationsSetChanged(this);
            }
        }
    }

    protected void fireDefaultPresentationChanged(Object oldPresentationId) {
        if (listeners != null) {
            for (final PresentationsChangeListener listener : listeners) {
                listener.defaultPresentationChanged(this, oldPresentationId);
            }
        }
    }

    protected void checkLoad() {
        if (presentations == null) {
            LoadContext<UiTablePresentation> ctx
                    = new LoadContext<>(UiTablePresentation.class);

            ctx.setFetchPlan(fetchPlanRepository.getFetchPlan(
                    UiTablePresentation.class, "app"));

//             todo user substitution
            BaseUser user = authentication.getUser();

            ctx.setQueryString("select p from ui_TablePresentation p " +
                    "where p.componentId = :component and (p.userLogin is null or p.userLogin = :userLogin)")
                    .setParameter("component", name)
                    .setParameter("userLogin", user.getUsername());

            final List<UiTablePresentation> list = dataManager.loadList(ctx);

            presentations = new LinkedHashMap<>(list.size());
            for (final TablePresentation p : list) {
                presentations.put(EntityValues.<UUID>getId(p), p);
            }
        }
    }

    protected void clearCommitList() {
        needToUpdate.clear();
        needToRemove.clear();
    }
}
