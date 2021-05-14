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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.LookupPickerField;
import com.haulmont.cuba.gui.components.TokenList;
import com.haulmont.cuba.gui.components.data.value.LegacyCollectionDsValueSource;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.NestedDatasource;
import io.jmix.core.*;
import com.haulmont.cuba.gui.components.CaptionMode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityOptions;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.options.ListEntityOptions;
import io.jmix.ui.component.data.options.MapEntityOptions;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.impl.AbstractField;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Deprecated
public class WebTokenList<V extends Entity> extends AbstractField<CubaTokenList<V>, Collection<V>, Collection<V>>
        implements TokenList<V>, InitializingBean {

    protected OpenType lookupOpenType = OpenType.THIS_TAB;

    private static final Logger log = LoggerFactory.getLogger(WebTokenList.class);

    protected ItemChangeHandler itemChangeHandler;
    protected ItemClickListener itemClickListener;

    protected AfterLookupCloseHandler afterLookupCloseHandler;
    protected AfterLookupSelectionHandler afterLookupSelectionHandler;

    protected Button addButton;
    protected Button clearButton;

    protected LookupPickerField<V> lookupPickerField;
    protected Action lookupAction;
    protected String lookupScreen;
    protected Map<String, Object> lookupScreenParams;

    protected Position position = Position.TOP;
    protected boolean inline;
    protected boolean lookup = false;
    protected boolean clearEnabled = true;
    protected boolean simple = false;
    protected boolean multiselect;

    protected UiComponents uiComponents;
    protected Messages messages;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected WindowConfig windowConfig;
    protected UiScreenProperties screenProperties;
    protected ScreenBuilders screenBuilders;
    protected Icons icons;
    protected EntityStates entityStates;
    protected DataManager dataManager;
    protected FetchPlans fetchPlans;

    protected Function<Object, String> tokenStyleGenerator;

    protected final Consumer<ValueChangeEvent<V>> lookupSelectListener = e -> {
        if (isEditableWithParent()) {
            addValueFromLookupPickerField();
        }
    };

    protected Supplier<Screen> lookupProvider;
    protected Function<? super V, String> optionCaptionProvider;

    protected boolean refreshOptionsOnLookupClose = false;

    public WebTokenList() {
        component = new CubaTokenList<>(this);

        attachValueChangeListener(component);
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setWindowConfig(WindowConfig windowConfig) {
        this.windowConfig = windowConfig;
    }

    @Autowired
    public void setUiScreenProperties(UiScreenProperties screenProperties) {
        this.screenProperties = screenProperties;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    public void setEntityStates(EntityStates entityStates) {
        this.entityStates = entityStates;
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setFetchPlans(FetchPlans fetchPlans) {
        this.fetchPlans = fetchPlans;
    }

    @Override
    public void afterPropertiesSet() {
        createComponents();
    }

    protected void createComponents() {
        addButton = uiComponents.create(Button.class);
        addButton.setCaption(messages.getMessage("actions.Add"));

        clearButton = uiComponents.create(Button.class);
        clearButton.setCaption(messages.getMessage("actions.Clear"));

        createLookupPickerField();

        setMultiSelect(false);
    }

    @SuppressWarnings("unchecked")
    protected void createLookupPickerField() {
        lookupPickerField = uiComponents.create(LookupPickerField.class);
        lookupPickerField.addValueChangeListener(lookupSelectListener);
    }

    @Override
    public void addValidator(Consumer<? super Collection<V>> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<Collection<V>> validator) {
        removeValidator(validator::accept);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public String getOptionsCaptionProperty() {
        return ((LookupPickerField) lookupPickerField).getCaptionProperty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setOptionsCaptionProperty(@Nullable String optionsCaptionProperty) {
        ((LookupPickerField) lookupPickerField).setCaptionProperty(optionsCaptionProperty);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public CaptionMode getOptionsCaptionMode() {
        return ((LookupPickerField) lookupPickerField).getCaptionMode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setOptionsCaptionMode(@Nullable CaptionMode optionsCaptionMode) {
        ((LookupPickerField) lookupPickerField).setCaptionMode(optionsCaptionMode);
    }

    @Override
    public OpenType getLookupOpenMode() {
        return lookupOpenType;
    }

    @Override
    public void setLookupOpenMode(OpenType lookupOpenMode) {
        Preconditions.checkNotNullArgument(lookupOpenMode);

        lookupOpenType = lookupOpenMode;
    }

    protected Screen createLookupScreen(@Nullable Runnable afterLookupSelect) {
        Screen lookupScreen = screenBuilders.lookup(getLookupEntityClass(), getFrame().getFrameOwner())
                .withScreenId(getLookupScreenInternal())
                .withOpenMode(lookupOpenType.getOpenMode())
                .withOptions(new MapScreenOptions(getLookupScreenParamsInternal()))
                .withSelectHandler(selected -> {
                    handleLookupSelection(selected);
                    if (afterLookupSelect != null) {
                        afterLookupSelect.run();
                    }
                })
                .build();

        if (lookupScreen instanceof MultiSelectLookupScreen) {
            ((MultiSelectLookupScreen) lookupScreen).setLookupComponentMultiSelect(isMultiSelect());
        }

        if (lookupOpenType != null) {
            applyOpenTypeParameters(lookupScreen.getWindow(), lookupOpenType);
        }

        return lookupScreen;
    }

    @Deprecated
    protected void applyOpenTypeParameters(Window window, OpenType openType) {
        if (window instanceof DialogWindow) {
            DialogWindow dialogWindow = (DialogWindow) window;

            if (openType.getCloseOnClickOutside() != null) {
                dialogWindow.setCloseOnClickOutside(openType.getCloseOnClickOutside());
            }
            if (openType.getMaximized() != null) {
                dialogWindow.setWindowMode(openType.getMaximized() ? WindowMode.MAXIMIZED : WindowMode.NORMAL);
            }
            if (openType.getModal() != null) {
                dialogWindow.setModal(openType.getModal());
            }
            if (openType.getResizable() != null) {
                dialogWindow.setResizable(openType.getResizable());
            }
            if (openType.getWidth() != null) {
                dialogWindow.setDialogWidth(openType.getWidthString());
            }
            if (openType.getHeight() != null) {
                dialogWindow.setDialogHeight(openType.getHeightString());
            }
        }

        if (openType.getCloseable() != null) {
            window.setCloseable(openType.getCloseable());
        }
    }

    @Override
    public void setValue(@Nullable Collection<V> value) {
        setValue(value, false);
    }

    protected void setValue(@Nullable Collection<V> value, boolean userOriginated) {
        Collection<V> oldValue = getOldValue(value);

        oldValue = new ArrayList<>(oldValue != null
                ? oldValue
                : Collections.emptyList());

        setValueToPresentation(convertToPresentation(value));

        component.refreshTokens(value);
        component.refreshClickListeners(itemClickListener);

        this.internalValue = value;

        fireValueChange(oldValue, value, userOriginated);
    }

    @Nullable
    protected Collection<V> getOldValue(@Nullable Collection<V> newValue) {
        return equalCollections(newValue, internalValue)
                ? component.getValue()
                : internalValue;
    }

    protected void fireValueChange(Collection<V> oldValue, Collection<V> value, boolean userOriginated) {
        if (!equalCollections(oldValue, value)) {
            ValueChangeEvent<Collection<V>> event =
                    new ValueChangeEvent<>(this, oldValue, value, userOriginated);
            publish(ValueChangeEvent.class, event);
        }
    }

    protected boolean equalCollections(@Nullable Collection<V> a, @Nullable Collection<V> b) {
        if (CollectionUtils.isEmpty(a)
                && CollectionUtils.isEmpty(b)) {
            return true;
        }

        if ((CollectionUtils.isEmpty(a) && CollectionUtils.isNotEmpty(b))
                || (CollectionUtils.isNotEmpty(a) && CollectionUtils.isEmpty(b))) {
            return false;
        }

        //noinspection ConstantConditions
        return CollectionUtils.isEqualCollection(a, b);
    }

    @Override
    protected boolean fieldValueEquals(Collection<V> value, Collection<V> oldValue) {
        return equalCollections(value, oldValue);
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        super.setFrame(frame);
        lookupPickerField.setFrame(frame);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || equalCollections(getValue(), getEmptyValue());
    }

    @Override
    public ComboBox.FilterMode getFilterMode() {
        return lookupPickerField.getFilterMode();
    }

    @Override
    public void setFilterMode(ComboBox.FilterMode mode) {
        lookupPickerField.setFilterMode(mode);
    }

    @Override
    public void setLookupFieldOptionsCaptionProvider(@Nullable Function<? super V, String> optionsCaptionProvider) {
        lookupPickerField.setOptionCaptionProvider(optionsCaptionProvider);
    }

    @Override
    public Function<? super V, String> getLookupFieldOptionsCaptionProvider() {
        return lookupPickerField.getOptionCaptionProvider();
    }

    @Override
    public void setRefreshOptionsOnLookupClose(boolean refresh) {
        this.refreshOptionsOnLookupClose = refresh;
    }

    @Override
    public boolean isRefreshOptionsOnLookupClose() {
        return refreshOptionsOnLookupClose;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setOptionsList(List optionsList) {
        setOptions(new ListEntityOptions(optionsList, metadata));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setOptionsMap(Map<String, ?> optionsMap) {
        setOptions(new MapEntityOptions(optionsMap, metadata));
    }

    @Override
    public void setOptions(@Nullable Options<V> options) {
        if (options != null
                && !(options instanceof EntityOptions)) {
            throw new IllegalArgumentException("TokenList supports only EntityOptions");
        }
        lookupPickerField.setOptions(options);
    }

    @Nullable
    @Override
    public Options<V> getOptions() {
        return lookupPickerField.getOptions();
    }

    @Override
    public boolean isClearEnabled() {
        return clearEnabled;
    }

    @Override
    public void setClearEnabled(boolean clearEnabled) {
        if (this.clearEnabled != clearEnabled) {
            clearButton.setVisible(clearEnabled);
            this.clearEnabled = clearEnabled;
            component.refreshComponent();
        }
    }

    @Override
    public boolean isLookup() {
        return lookup;
    }

    @Override
    public void setLookup(boolean lookup) {
        if (this.lookup != lookup) {
            if (lookup) {
                lookupAction = createLookupAction();
                lookupPickerField.addAction(lookupAction);
            } else {
                lookupPickerField.removeAction(lookupAction);
            }
        }
        this.lookup = lookup;
        component.refreshComponent();
    }

    protected Action createLookupAction() {
        return new BaseAction("")
                .withIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP))
                .withHandler(e -> openLookup(null));
    }

    protected void openLookup(@Nullable Runnable afterLookupSelect) {
        Screen lookupScreen;

        if (lookupProvider == null) {
            lookupScreen = createLookupScreen(afterLookupSelect);
        } else {
            lookupScreen = lookupProvider.get();
            if (!(LookupScreen.class.isAssignableFrom(lookupScreen.getClass()))) {
                log.info("Not suitable screen is returned from LookupScreen provider. Default implementation will be used");

                lookupScreen = createLookupScreen(afterLookupSelect);
            }

            //noinspection unchecked
            ((LookupScreen<V>) lookupScreen).setSelectHandler(selected -> {
                handleLookupSelection(selected);
                if (afterLookupSelect != null) {
                    afterLookupSelect.run();
                }
            });
        }

        lookupScreen.show();

        if (afterLookupCloseHandler != null) {
            lookupScreen.addAfterCloseListener(event -> {
                String actionId = ((StandardCloseAction) event.getCloseAction()).getActionId();
                afterLookupCloseHandler.onClose(event.getSource().getWindow(), actionId);
            });
        }
    }

    protected String getLookupScreenInternal() {
        return StringUtils.isNotEmpty(getLookupScreen())
                ? getLookupScreen()
                : windowConfig.getLookupScreen(getLookupEntityClass()).getId();
    }

    protected Class<V> getLookupEntityClass() {
        Class<V> entityClass;

        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource != null) {
            if (valueSource instanceof EntityValueSource) {
                //noinspection unchecked
                entityClass = ((EntityValueSource) valueSource).getMetaPropertyPath().getRangeJavaClass();
            } else {
                entityClass = ((LegacyCollectionDsValueSource<V>) valueSource).getDatasource().getMetaClass().getJavaClass();
            }
        } else if (getOptions() instanceof EntityOptions) {
            entityClass = ((EntityOptions<V>) getOptions()).getEntityMetaClass().getJavaClass();
        } else {
            throw new RuntimeException("Unable to determine entity class to open lookup");
        }

        return entityClass;
    }

    protected Map<String, Object> getLookupScreenParamsInternal() {
        // we create mutable map only for compatibilty with legacy code
        Map<String, Object> params = new HashMap<>();
        params.put("windowOpener", getFrame().getId());
        if (isMultiSelect()) {
            WindowParams.MULTI_SELECT.set(params, true);
            // for backward compatibility
            params.put("multiSelect", "true");
        }
        if (lookupScreenParams != null) {
            params.putAll(lookupScreenParams);
        }
        return params;
    }

    protected void handleLookupSelection(Collection<V> selectedEntities) {
        if (CollectionUtils.isEmpty(selectedEntities)) {
            return;
        }

        handleSelection(selectedEntities);

        if (afterLookupSelectionHandler != null) {
            afterLookupSelectionHandler.onSelect(selectedEntities);
        }
    }

    protected void handleSelection(Collection<V> selected) {
        Collection<V> reloadedSelected = new ArrayList<>();
        //check that selected items are loaded with the correct view and reload selected items if not all the required fields are loaded
        FetchPlan viewForField = screenProperties.isReloadUnfetchedAttributesFromLookupScreens() ?
                getViewForField() :
                null;
        if (viewForField != null) {
            for (V selectedItem : selected) {
                if (!entityStates.isLoadedWithFetchPlan(selectedItem, viewForField)) {
                    //noinspection unchecked
                    reloadedSelected.add((V) dataManager.load(Id.of(selectedItem)).fetchPlan(viewForField).one());
                } else {
                    reloadedSelected.add(selectedItem);
                }
            }
        } else {
            reloadedSelected = selected;
        }
        if (itemChangeHandler != null) {
            reloadedSelected.forEach(itemChangeHandler::addItem);
        } else {
            ValueSource<Collection<V>> valueSource = getValueSource();
            Collection<V> newValue = getValue();
            if (valueSource != null) {
                Collection<V> modelValue = refreshValueIfNeeded();
                for (V newItem : reloadedSelected) {
                    if (!modelValue.contains(newItem)) {
                        modelValue.add(newItem);
                    }
                }

                newValue = modelValue;

                // Options after refresh reloads items without changes made in TokenList,
                // so for values in nested container we need to set master-entity reference
                // due to ContainerValueSource will ignore items if they were in previous value
                updateMasterRefIfOptionsRefreshed(newValue);
            } else {
                if (newValue == null) {
                    newValue = new ArrayList<>();
                }

                // Get rid of duplicates
                Collection<V> itemsToAdd = new ArrayList<>(reloadedSelected);
                itemsToAdd.removeAll(newValue);

                newValue.addAll(itemsToAdd);
            }

            setValue(newValue, true);
        }
    }

    protected boolean isRefreshOptionsEnabled() {
        return getOptions() != null && isRefreshOptionsOnLookupClose();
    }

    protected Collection<V> refreshValueIfNeeded() {
        EntityOptions<V> options = (EntityOptions<V>) getOptions();
        Collection<V> valueSourceValue = getValueSourceValue();

        if (options == null || !isRefreshOptionsEnabled()) {
            return valueSourceValue;
        }

        options.refresh();

        if (getValueSource() instanceof LegacyCollectionDsValueSource) {
            List<V> optionItems = options.getOptions().collect(Collectors.toList());
            List<V> copiedValue = new ArrayList<>(valueSourceValue);

            // replace items with new for legacy datasource
            for (V value : copiedValue) {
                optionItems.stream()
                        .filter(option -> Objects.equals(EntityValues.getId(value), EntityValues.getId(option)))
                        .findFirst()
                        .ifPresent(option -> {
                            valueSourceValue.remove(value);
                            valueSourceValue.add(option);
                        });
            }
        }

        return valueSourceValue;
    }

    /**
     * Sets master-entity reference to the value and remove master-entity reference
     * from options if they are not in nested container.
     *
     * @param value value items
     */
    protected void updateMasterRefIfOptionsRefreshed(Collection<V> value) {
        if (!isRefreshOptionsEnabled()) {
            return;
        }

        if (!(getValueSource() instanceof ContainerValueSource)) {
            return;
        }

        EntityOptions<V> options = (EntityOptions<V>) getOptions();
        if (options == null) {
            return;
        }

        ContainerValueSource valueSource = (ContainerValueSource) getValueSource();
        MetaPropertyPath mpp = valueSource.getMetaPropertyPath();

        MetaProperty inverseProperty = mpp.getMetaProperty().getInverse();
        Object masterEntity = valueSource.getItem();

        if (inverseProperty == null || masterEntity == null) {
            return;
        }

        List<V> optionItems = getOptions().getOptions().collect(Collectors.toList());
        for (V option : optionItems) {
            // skip all options that did not load master-reference
            if (!entityStates.isLoaded(option, inverseProperty.getName())) {
                continue;
            }

            if (value.contains(option)) {
                // reset master-entity reference
                EntityValues.setValue(option, inverseProperty.getName(), masterEntity);
            } else {
                Entity ref = EntityValues.getValue(option, inverseProperty.getName());
                if (ref == null) {
                    continue;
                }

                // remove ref to the master entity if option is not in value
                if (Objects.equals(EntityValues.getId(ref), EntityValues.getId(masterEntity))) {
                    EntityValues.setValue(option, inverseProperty.getName(), null);
                }
            }
        }
    }

    protected Collection<V> getValueSourceValue() {
        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource == null) {
            return Collections.emptyList();
        }

        Collection<V> modelValue;

        if (valueSource instanceof EntityValueSource) {
            Class<?> modelCollectionType = ((EntityValueSource) valueSource)
                    .getMetaPropertyPath().getMetaProperty().getJavaType();

            Collection<V> valueSourceValue = valueSource.getValue() == null
                    ? Collections.emptyList()
                    : valueSource.getValue();

            if (Set.class.isAssignableFrom(modelCollectionType)) {
                modelValue = new LinkedHashSet<>(valueSourceValue);
            } else {
                modelValue = new ArrayList<>(valueSourceValue);
            }
        } else {
            modelValue = new ArrayList<>(valueSource.getValue());
        }

        return modelValue;
    }

    @Nullable
    @Override
    public String getLookupScreen() {
        return lookupScreen;
    }

    @Override
    public void setLookupScreen(@Nullable String lookupScreen) {
        this.lookupScreen = lookupScreen;
    }

    @Override
    public void setLookupScreenParams(@Nullable Map<String, Object> params) {
        this.lookupScreenParams = params;
    }

    @Override
    public Map<String, Object> getLookupScreenParams() {
        return lookupScreenParams;
    }

    @Override
    public boolean isMultiSelect() {
        return multiselect;
    }

    @Override
    public void setMultiSelect(boolean multiselect) {
        this.multiselect = multiselect;
        component.refreshComponent();
    }

    @Nullable
    @Override
    public String getAddButtonCaption() {
        return addButton.getCaption();
    }

    @Override
    public void setAddButtonCaption(@Nullable String caption) {
        addButton.setCaption(caption);
    }

    @Nullable
    @Override
    public String getAddButtonIcon() {
        return addButton.getIcon();
    }

    @Override
    public void setAddButtonIcon(@Nullable String icon) {
        addButton.setIcon(icon);
    }

    @Nullable
    @Override
    public String getClearButtonCaption() {
        return clearButton.getCaption();
    }

    @Override
    public void setClearButtonCaption(@Nullable String caption) {
        clearButton.setCaption(caption);
    }

    @Nullable
    @Override
    public String getClearButtonIcon() {
        return clearButton.getIcon();
    }

    @Override
    public void setClearButtonIcon(@Nullable String icon) {
        clearButton.setIcon(icon);
    }

    @Nullable
    @Override
    public ItemChangeHandler getItemChangeHandler() {
        return itemChangeHandler;
    }

    @Override
    public void setItemChangeHandler(@Nullable ItemChangeHandler handler) {
        this.itemChangeHandler = handler;
    }

    @Nullable
    @Override
    public ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    @Override
    public void setItemClickListener(@Nullable ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.component.refreshClickListeners(itemClickListener);
    }

    @Nullable
    @Override
    public AfterLookupCloseHandler getAfterLookupCloseHandler() {
        return afterLookupCloseHandler;
    }

    @Override
    public void setAfterLookupCloseHandler(@Nullable AfterLookupCloseHandler afterLookupCloseHandler) {
        this.afterLookupCloseHandler = afterLookupCloseHandler;
    }

    @Nullable
    @Override
    public AfterLookupSelectionHandler getAfterLookupSelectionHandler() {
        return afterLookupSelectionHandler;
    }

    @Override
    public void setAfterLookupSelectionHandler(@Nullable AfterLookupSelectionHandler afterLookupSelectionHandler) {
        this.afterLookupSelectionHandler = afterLookupSelectionHandler;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = position;
        component.refreshComponent();
    }

    @Override
    public boolean isInline() {
        return inline;
    }

    @Override
    public void setInline(boolean inline) {
        this.inline = inline;
        component.refreshComponent();
    }

    @Override
    protected void setEditableToComponent(boolean editable) {
        super.setEditableToComponent(editable);

        component.refreshComponent();
    }

    @Override
    public boolean isSimple() {
        return simple;
    }

    @Override
    public void setSimple(boolean simple) {
        this.simple = simple;
        this.addButton.setVisible(simple);
        this.component.refreshComponent();
    }

    @Override
    public void setTokenStyleGenerator(@Nullable Function<Object, String> tokenStyleGenerator) {
        this.tokenStyleGenerator = tokenStyleGenerator;
    }

    @Nullable
    @Override
    public Function<Object, String> getTokenStyleGenerator() {
        return tokenStyleGenerator;
    }

    @Nullable
    @Override
    public String getLookupInputPrompt() {
        return lookupPickerField.getInputPrompt();
    }

    @Override
    public void setLookupInputPrompt(@Nullable String inputPrompt) {
        this.lookupPickerField.setInputPrompt(inputPrompt);
    }

    protected String getInstanceCaption(@Nullable V instance) {
        if (instance == null) {
            return "";
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(instance);
        }

        return metadataTools.getInstanceName(instance);
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        this.optionCaptionProvider = optionCaptionProvider;
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    protected void addValueFromLookupPickerField() {
        V newItem = lookupPickerField.getValue();
        if (newItem == null) {
            return;
        }

        handleSelection(Collections.singleton(newItem));

        lookupPickerField.setValue(null);
        lookupPickerField.focus();
    }

    @Override
    public void focus() {
        if (simple) {
            addButton.focus();
        } else {
            lookupPickerField.focus();
        }
    }

    @Override
    public void setLookupProvider(@Nullable Supplier<Screen> lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    @Nullable
    @Override
    public Supplier<Screen> getLookupProvider() {
        return lookupProvider;
    }

    @Override
    protected Collection<V> convertToModel(Collection<V> componentRawValue) throws ConversionException {
        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource != null) {
            Class<?> modelCollectionType = null;

            if (valueSource instanceof EntityValueSource) {
                MetaPropertyPath mpp = ((EntityValueSource) valueSource).getMetaPropertyPath();
                modelCollectionType = mpp.getMetaProperty().getJavaType();
            } else if (valueSource instanceof LegacyCollectionDsValueSource) {
                CollectionDatasource datasource = ((LegacyCollectionDsValueSource) valueSource).getDatasource();
                if (datasource instanceof NestedDatasource) {
                    MetaProperty property = ((NestedDatasource) datasource).getProperty().getInverse();
                    modelCollectionType = property == null ? null : property.getJavaType();
                }
            }

            if (modelCollectionType != null) {
                if (Set.class.isAssignableFrom(modelCollectionType)) {
                    return new LinkedHashSet<>(componentRawValue);
                }
            }
        }

        return new ArrayList<>(componentRawValue);
    }

    /**
     * If the value for a component is selected from the lookup screen there may be cases when lookup screen contains a list of entities loaded with a
     * view that doesn't contain all attributes, required by the token list.
     * <p>
     * The method evaluates the view that was is for loading entities in the token list
     *
     * @return a view or null if the view cannot be evaluated
     */
    @Nullable
    protected FetchPlan getViewForField() {
        ValueSource valueSource = getValueSource();
        if (valueSource instanceof ContainerValueSource) {
            ContainerValueSource containerValueSource = (ContainerValueSource) valueSource;
            InstanceContainer container = containerValueSource.getContainer();
            FetchPlan view = container.getFetchPlan();
            if (view != null) {
                MetaPropertyPath metaPropertyPath = containerValueSource.getMetaPropertyPath();
                FetchPlan curView = view;
                if (metaPropertyPath != null) {
                    for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
                        FetchPlanProperty viewProperty = curView.getProperty(metaProperty.getName());
                        if (viewProperty != null) {
                            curView = viewProperty.getFetchPlan();
                            if (curView == null) return null;
                        }
                    }
                    MetaProperty inverseMetaProperty = metaPropertyPath.getMetaProperty().getInverse();
                    if (inverseMetaProperty != null && !inverseMetaProperty.getRange().getCardinality().isMany()) {
                        curView = fetchPlans.builder(curView)
                                .add(inverseMetaProperty.getName())
                                .build();
                    }
                }
                if (curView != view) {
                    return curView;
                }
            }
        }
        return null;
    }
}
