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

package io.jmix.ui.app.filter.condition;

import com.google.common.collect.Lists;
import io.jmix.core.LoadContext;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.filter.FilterConditionsBuilder;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.StandardOutcome;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes"})
@UiController("ui_AddConditionScreen")
@UiDescriptor("add-condition-screen.xml")
@LookupComponent("filterConditionsTree")
public class AddConditionScreen extends StandardLookup<FilterCondition> {

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected FilterConditionsBuilder builder;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected FilterSupport filterSupport;
    @Autowired
    protected FilterComponents filterComponents;

    @Autowired
    protected Tree<FilterCondition> filterConditionsTree;
    @Autowired
    protected PopupButton createPopupButton;
    @Autowired
    protected TextField<String> conditionCaptionFilterField;

    protected List<FilterCondition> conditions = new ArrayList<>();
    protected Filter.Configuration currentFilterConfiguration;

    public List<FilterCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<FilterCondition> conditions) {
        this.conditions = conditions;
    }

    public Filter.Configuration getCurrentFilterConfiguration() {
        return currentFilterConfiguration;
    }

    public void setCurrentFilterConfiguration(Filter.Configuration currentFilterConfiguration) {
        this.currentFilterConfiguration = currentFilterConfiguration;
    }

    @Install(to = "filterConditionsDl", target = Target.DATA_LOADER)
    protected List<FilterCondition> filterConditionsDlLoadDelegate(LoadContext<FilterCondition> loadContext) {
        String searchString = conditionCaptionFilterField.getValue();
        return conditions.stream()
                .filter(condition -> StringUtils.isEmpty(searchString) || condition.getCaption().contains(searchString))
                .collect(Collectors.toList());
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        String propertiesCaption =
                messages.getMessage(FilterAddConditionAction.class, "addConditionAction.properties");

        HeaderFilterCondition propertiesHeader = getHeaderFilterConditionByCaption(propertiesCaption);
        if (propertiesHeader != null) {
            filterConditionsTree.expand(propertiesHeader);
        }

        initCreatePopupButton();
    }

    protected void initCreatePopupButton() {
        for (Class<? extends FilterCondition> modelClass : filterComponents.getRegisteredModelClasses()) {
            try {
                String editScreenId = filterComponents.getEditScreenId(modelClass);
                Action popupAction = createPopupAction(editScreenId, modelClass);
                createPopupButton.addAction(popupAction);
            } catch (IllegalArgumentException e) {
                return;
            }
        }
    }

    protected Action createPopupAction(String editScreenId,
                                       Class<? extends FilterCondition> modelClass) {
        MetaClass metaClass = metadata.getClass(modelClass);

        return new BaseAction("filter_create_" + editScreenId)
                .withCaption(messages.formatMessage(AddConditionScreen.class,
                        "addConditionScreen.createPopupAction",
                        messageTools.getEntityCaption(metaClass)))
                .withHandler(actionPerformedEvent -> {
                    Screen editScreen = screenBuilders.editor(modelClass, getWindow().getFrameOwner())
                            .withScreenId(editScreenId)
                            .newEntity()
                            .build();

                    if (editScreen instanceof LogicalFilterConditionEdit) {
                        ((LogicalFilterConditionEdit<?>) editScreen).setConfiguration(getCurrentFilterConfiguration());
                    }

                    editScreen.addAfterCloseListener(afterCloseEvent -> {
                        if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                            FilterCondition filterCondition =
                                    (FilterCondition) ((FilterConditionEdit) afterCloseEvent.getScreen())
                                            .getInstanceContainer()
                                            .getItem();
                            select(Lists.newArrayList(filterCondition));
                        }
                    });

                    editScreen.show();
                });
    }

    @Nullable
    protected HeaderFilterCondition getHeaderFilterConditionByCaption(String caption) {
        return conditions.stream()
                .filter(condition -> condition instanceof HeaderFilterCondition
                        && Objects.equals(condition.getCaption(), caption))
                .map(condition -> (HeaderFilterCondition) condition)
                .findFirst()
                .orElse(null);
    }
}
