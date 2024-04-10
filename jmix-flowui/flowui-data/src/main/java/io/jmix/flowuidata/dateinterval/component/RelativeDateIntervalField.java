/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowuidata.dateinterval.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowuidata.dateinterval.RelativeDateTimeMomentProvider;
import io.jmix.flowuidata.dateinterval.model.RelativeDateInterval;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Internal
public class RelativeDateIntervalField extends CustomField<RelativeDateInterval>
        implements SupportsValidation<RelativeDateInterval>, ApplicationContextAware, InitializingBean {

    protected HorizontalLayout root;
    protected JmixSelect<RelativeDateInterval.Operation> relativeDateTimeOperationSelect;
    protected JmixSelect<Enum<?>> relativeDateTimeSelect;

    protected ApplicationContext applicationContext;

    protected UiComponents uiComponents;
    protected Messages messages;
    protected RelativeDateTimeMomentProvider relativeMomentProvider;

    public RelativeDateIntervalField() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
        relativeMomentProvider = applicationContext.getBean(RelativeDateTimeMomentProvider.class);
    }

    protected void initComponent() {
        initRoot();
        initOperationSelect();
        initDateTimeSelect();
        updateInvalidState();
    }

    protected void initRoot() {
        root = uiComponents.create(HorizontalLayout.class);

        root.setPadding(false);
        root.addClassNames(LumoUtility.AlignItems.BASELINE);
        add(root);
    }

    protected void initOperationSelect() {
        //noinspection unchecked
        relativeDateTimeOperationSelect = createSelectComponent("relativeDateTimeOperationSelect");
        relativeDateTimeOperationSelect.setWidth("4em");

        Map<RelativeDateInterval.Operation, String> localizationMap = Arrays
                .stream(RelativeDateInterval.Operation.values())
                .sequential()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item, item.getValue()), Map::putAll);

        ComponentUtils.setItemsMap(relativeDateTimeOperationSelect, localizationMap);

        root.add(relativeDateTimeOperationSelect);
    }

    protected void initDateTimeSelect() {
        //noinspection unchecked
        relativeDateTimeSelect = createSelectComponent("relativeDateTimeSelect");
        // WA: min-width (in a flexbox) defaults not to 0 but to the element's intrinsic width,
        // which in this case is the default width
        relativeDateTimeSelect.setMinWidth("1px");

        root.add(relativeDateTimeSelect);
        root.setFlexGrow(1D, relativeDateTimeSelect);
    }

    @SuppressWarnings("rawtypes")
    protected JmixSelect createSelectComponent(String id) {
        JmixSelect<?> select = uiComponents.create(JmixSelect.class);

        select.setStatusChangeHandler(this::onValidationStatusChange);
        select.addValidationStatusChangeListener(__ -> updateInvalidState());
        select.addClientValidatedEventListener(__ -> updateInvalidState());

        select.setRequiredMessage(
                messages.getMessage(getClass(),
                        "RelativeDateIntervalField.%s.requiredMessage".formatted(id))
        );
        select.setRequired(true);

        select.addThemeVariants(SelectVariant.LUMO_ALIGN_CENTER);
        return select;
    }

    public void setDateTimeSelectItemsMap(Map<Enum<?>, String> localizationMap) {
        ComponentUtils.setItemsMap(relativeDateTimeSelect, localizationMap);
    }

    @Override
    protected RelativeDateInterval generateModelValue() {
        String relativeMoment = relativeDateTimeSelect.getValue() == null
                ? null
                : relativeDateTimeSelect.getValue().name();

        return new RelativeDateInterval(
                relativeDateTimeOperationSelect.getValue(),
                Strings.nullToEmpty(relativeMoment)
        );
    }

    @Override
    protected void setPresentationValue(RelativeDateInterval newPresentationValue) {
        relativeDateTimeOperationSelect.setValue(newPresentationValue.getOperation());
        relativeDateTimeSelect.setValue(
                relativeMomentProvider.getByName(newPresentationValue.getRelativeDateTimeMomentName())
        );
    }

    @Override
    public Registration addValidator(Validator<? super RelativeDateInterval> validator) {
        throw new UnsupportedOperationException("%s has a predefined validators".formatted(getClass().getSimpleName()));
    }

    protected void onValidationStatusChange(SupportsStatusChangeHandler.StatusContext<? extends Component> context) {
        setErrorMessage(context.getDescription());
    }

    protected void updateInvalidState() {
        setInvalid(relativeDateTimeOperationSelect.isInvalid() || relativeDateTimeSelect.isInvalid());
    }

    @Override
    public void executeValidators() throws ValidationException {
        relativeDateTimeOperationSelect.executeValidators();
        relativeDateTimeSelect.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }
}
