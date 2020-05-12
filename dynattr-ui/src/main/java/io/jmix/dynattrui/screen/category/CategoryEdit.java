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

package io.jmix.dynattrui.screen.category;

import io.jmix.core.CoreProperties;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.LoadContext;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.SaveContext;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.impl.model.Category;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.dynattrui.screen.categoryattr.CategoryAttrsFragment;
import io.jmix.dynattrui.screen.localization.AttributeLocalizationFragment;
import io.jmix.dynattrui.screen.location.AttributeLocationFragment;
import io.jmix.ui.Fragments;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Fragment;
import io.jmix.ui.components.HasValue;
import io.jmix.ui.components.LookupField;
import io.jmix.ui.components.TabSheet;
import io.jmix.ui.components.VBoxLayout;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@UiController("sys$Category.edit")
@UiDescriptor("category-edit.xml")
@EditedEntityContainer("categoryDc")
public class CategoryEdit extends StandardEditor<Category> {

    protected static final String ATTRIBUTES_LOCATION_TAB = "attributesLocationTab";

    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected MessageTools messageTools;
    @Inject
    protected DataManager dataManager;
    @Inject
    protected FetchPlanRepository fetchPlanRepository;
    @Inject
    protected Fragments fragments;
    @Inject
    protected CoreProperties coreProperties;

    @Inject
    protected LookupField<MetaClass> entityTypeField;
    @Inject
    protected VBoxLayout categoryAttrsBox;
    @Inject
    protected TabSheet tabSheet;

    @Inject
    protected InstanceContainer<Category> categoryDc;
    @Inject
    protected CollectionContainer<CategoryAttribute> categoryAttributesDc;

    protected AttributeLocalizationFragment localizationFragment;
    protected AttributeLocationFragment attributeLocationFragment;

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
            LoadContext<Category> lc = new LoadContext<>(Category.class)
                    .setFetchPlan(fetchPlanRepository.getFetchPlan(Category.class, "category.defaultEdit"));
            Category category = getEditedEntity();
            lc.setQueryString("select c from sys$Category c where c.entityType = :entityType and not c.id = :id")
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
        //final ExtendedEntities extendedEntities = metadata.getExtendedEntities();

        Map<String, MetaClass> options = new TreeMap<>();//the map sorts meta classes by the string key
        for (MetaClass metaClass : metadataTools.getAllPersistentMetaClasses()) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && !HasUuid.class.isAssignableFrom(metaClass.getJavaClass())) {
                continue;
            }
            options.put(messageTools.getDetailedEntityCaption(metaClass), metaClass);
        }
        entityTypeField.setOptionsMap(options);

        if (getEditedEntity().getEntityType() != null) {
            entityTypeField.setValue(categoryDc.getEntityMetaClass());
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            TabSheet.Tab localizationTab = tabSheet.getTab("localizationTab");
            localizationTab.setVisible(true);

            VBoxLayout localizationTabComponent = (VBoxLayout) tabSheet.getTabComponent("localizationTab");
            localizationFragment = fragments.create(this, AttributeLocalizationFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getLocaleNames());

            Fragment fragment = localizationFragment.getFragment();
            fragment.setWidth(Component.FULL_SIZE);
            localizationTabComponent.add(fragment);
            localizationTabComponent.expand(fragment);
        }
    }

    protected void initAttributeLocationTab() {
        VBoxLayout attributesLocationTabComponent = (VBoxLayout) tabSheet.getTabComponent(ATTRIBUTES_LOCATION_TAB);
        attributeLocationFragment = fragments.create(this, AttributeLocationFragment.class);

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
