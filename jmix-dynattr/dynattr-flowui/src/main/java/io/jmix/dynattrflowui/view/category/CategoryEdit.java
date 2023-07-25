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

package io.jmix.dynattrflowui.view.category;

import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttrsFragment;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationFragment;
import io.jmix.dynattrflowui.view.location.AttributeLocationFragment;
import io.jmix.ui.Fragments;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@UiController("dynat_Category.edit")
@UiDescriptor("category-edit.xml")
@EditedEntityContainer("categoryDc")
public class CategoryEdit extends StandardEditor<Category> {

    protected static final String ATTRIBUTES_LOCATION_TAB = "attributesLocationTab";

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected ComboBox<MetaClass> entityTypeField;
    @Autowired
    protected VBoxLayout categoryAttrsBox;
    @Autowired
    protected TabSheet tabSheet;

    @Autowired
    protected InstanceContainer<Category> categoryDc;
    @Autowired
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;
    @Autowired
    private Metadata metadata;

    protected AttributeLocalizationFragment localizationFragment;
    protected AttributeLocationFragment attributeLocationFragment;
    @Autowired
    private AccessManager accessManager;

    @Subscribe
    protected void onInit(InitEvent event) {
        CategoryAttrsFragment categoryAttrsFragment = fragments.create(this, CategoryAttrsFragment.class);
        Fragment fragment = categoryAttrsFragment.getFragment();
        categoryAttrsBox.add(fragment);
        categoryAttrsBox.expand(fragment);
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initEntityTypeField();
        initLocalizationTab();
        initAttributeLocationTab();
        setupFieldsLock();
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            entityTypeField.setEnabled(false);
        }
    }

    @Subscribe("entityTypeField")
    protected void onEntityTypeFieldValueChange(HasValue.ValueChangeEvent<MetaClass> event) {
        if (event.getValue() != null) {
            getEditedEntity().setEntityType(event.getValue().getName());
        }
    }

    @Subscribe("isDefaultField")
    protected void onIsDefaultFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (Boolean.TRUE.equals(event.getValue())) {
            FetchPlan fetchPlan = fetchPlans.builder(Category.class)
                    .add("isDefault")
                    .build();
            LoadContext<Category> lc = new LoadContext(metadata.getClass(Category.class))
                    .setFetchPlan(fetchPlan);
            Category category = getEditedEntity();
            lc.setQueryString("select c from dynat_Category c where c.entityType = :entityType and not c.id = :id")
                    .setParameter("entityType", category.getEntityType())
                    .setParameter("id", category.getId());
            List<Category> result = dataManager.loadList(lc);
            for (Category cat : result) {
                cat.setIsDefault(false);
            }

            SaveContext saveContext = new SaveContext().saving(result);
            dataManager.save(saveContext);
        }
    }

    @Subscribe("tabSheet")
    protected void onTabSheetSelectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        String tabName = event.getSelectedTab().getName();
        if (ATTRIBUTES_LOCATION_TAB.equals(tabName)) {
            attributeLocationFragment.setCategoryAttributes(new ArrayList<>(categoryAttributesDc.getItems()));
        }
    }

    protected void initEntityTypeField() {
        Map<String, MetaClass> options = new TreeMap<>();//the map sorts meta classes by the string key
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
                continue;
            }
            options.put(messageTools.getDetailedEntityCaption(metaClass), metaClass);
        }
        entityTypeField.setOptionsMap(options);

        if (getEditedEntity().getEntityType() != null) {
            entityTypeField.setValue(extendedEntities.getEffectiveMetaClass(getEditedEntity().getEntityType()));
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            TabSheet.Tab localizationTab = tabSheet.getTab("localizationTab");
            localizationTab.setVisible(true);

            CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
            accessManager.applyRegisteredConstraints(crudEntityContext);

            VBoxLayout localizationTabComponent = (VBoxLayout) tabSheet.getTabComponent("localizationTab");
            localizationFragment = fragments.create(this, AttributeLocalizationFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getLocaleNames());
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

            Fragment fragment = localizationFragment.getFragment();
            fragment.setWidth(Component.FULL_SIZE);
            localizationTabComponent.add(fragment);
            localizationTabComponent.expand(fragment);
        }
    }

    protected void initAttributeLocationTab() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);

        VBoxLayout attributesLocationTabComponent = (VBoxLayout) tabSheet.getTabComponent(ATTRIBUTES_LOCATION_TAB);
        attributeLocationFragment = fragments.create(this, AttributeLocationFragment.class);
        attributeLocationFragment.setEnabled(crudEntityContext.isUpdatePermitted());
        Fragment fragment = attributeLocationFragment.getFragment();
        fragment.setWidth(Component.FULL_SIZE);
        attributesLocationTabComponent.add(fragment);
        attributesLocationTabComponent.expand(fragment);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreCommitEvent event) {
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
        }
    }
}
