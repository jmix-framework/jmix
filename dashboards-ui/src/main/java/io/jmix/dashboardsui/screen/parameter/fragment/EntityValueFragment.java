/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.screen.parameter.fragment;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dashboards.model.parameter.type.EntityParameterValue;
import io.jmix.dashboards.model.parameter.type.ParameterValue;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UiController("dshbrd_EntityValue.fragment")
@UiDescriptor("entity-value-fragment.xml")
public class EntityValueFragment extends ScreenFragment implements ValueFragment {

    @Autowired
    protected ComboBox<MetaClass> metaClassLookup;

    @Autowired
    protected ComboBox<Object> entitiesLookup;

    @Autowired
    protected ComboBox<String> viewLookup;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @Subscribe
    public void onInit(InitEvent event) {
        MapScreenOptions options = (MapScreenOptions) event.getOptions();
        Map<String, Object> params = options.getParams();

        init(params);
    }

    public void init(Map<String, Object> params) {
        loadAllPersistentClasses();
        selectIfExist((EntityParameterValue) params.get(VALUE));
        metaClassLookup.addValueChangeListener(e -> metaClassValueChanged(e.getValue()));
    }

    @Override
    public ParameterValue getValue() {
        EntityParameterValue value = new EntityParameterValue();
        MetaClass metaClass = metaClassLookup.getValue();

        if (metaClass != null) {
            value.setMetaClassName(metaClass.getName());

            Object entity = entitiesLookup.getValue();
            value.setEntityId(entity == null ? null : EntityValues.getId(entity).toString());

            String fetchPlan = viewLookup.getValue();
            value.setFetchPlan(fetchPlan);
        }

        return value;
    }

    protected void loadAllPersistentClasses() {
        List<MetaClass> metaClasses = new ArrayList<>(metadataTools.getAllJpaEntityMetaClasses());
        metaClassLookup.setOptionsList(metaClasses);
    }

    public void selectIfExist(EntityParameterValue value) {
        if (value != null && isNotBlank(value.getMetaClassName())) {
            String metaClassName = value.getMetaClassName();

            Optional<MetaClass> classOpt = metaClassLookup.getOptions().getOptions().collect(Collectors.toList())
                    .stream()
                    .filter(clazz -> metaClassName.equals(clazz.getName()))
                    .findFirst();

            if (classOpt.isPresent()) {
                MetaClass metaClass = classOpt.get();
                metaClassLookup.setValue(metaClass);
                loadEntities(metaClass);
                loadFetchPlans(metaClass);

                String entityId = value.getEntityId();
                if (isNotBlank(entityId)) {
                    entitiesLookup.getOptions().getOptions().collect(Collectors.toList())
                            .stream()
                            .filter(entity -> entityId.equals(EntityValues.getId(entity).toString()))
                            .findFirst()
                            .ifPresent(entity -> entitiesLookup.setValue(entity));
                }

                String fetchPlan = value.getFetchPlan();
                if (isNotBlank(fetchPlan) && viewLookup.getOptions().getOptions().anyMatch(e -> e.equals(fetchPlan))) {
                    viewLookup.setValue(fetchPlan);
                }
            }
        }
    }

    protected void metaClassValueChanged(MetaClass metaClass) {
        if (metaClass == null) {
            entitiesLookup.setValue(null);
            entitiesLookup.setOptionsList(Collections.emptyList());
            viewLookup.setValue(null);
            viewLookup.setOptionsList(Collections.emptyList());
        } else {
            loadEntities(metaClass);
            loadFetchPlans(metaClass);
        }
    }

    protected void loadEntities(MetaClass metaClass) {
        LoadContext loadContext = new LoadContext(metaClass)
                .setQuery(new LoadContext.Query(format("select e from %s e", metaClass.getName())));
        List entities = dataManager.loadList(loadContext);
        entitiesLookup.setOptionsList(entities);
    }

    protected void loadFetchPlans(MetaClass metaClass) {
        List<String> fetchPlans = new ArrayList<>(fetchPlanRepository.getFetchPlanNames(metaClass));
        fetchPlans.add(0, FetchPlan.LOCAL);
        fetchPlans.add(1, FetchPlan.INSTANCE_NAME);
        fetchPlans.add(2, FetchPlan.BASE);
        viewLookup.setOptionsList(fetchPlans);
    }
}
