/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component.loginform;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag("jmix-login-form")
@JsModule("./src/login-form/jmix-login-form.js")
public class EnhancedLoginForm extends LoginForm {

    private static final String REMEMBER_ME_CHANGED_EVENT = "remember-me-changed";
    private static final String LOCALE_CHANGED_EVENT = "locale-selection-changed";

    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String REMEMBER_ME_VISIBILITY_PROPERTY = "rememberMeVisibility";
    private static final String LOCALES_VISIBILITY_PROPERTY = "localesVisibility";

    protected List<Locale> locales;
    protected Locale selectedLocale = null;
    protected boolean rememberMe = false;
    protected Function<Locale, String> localeItemLabelGenerator;

    public EnhancedLoginForm() {
        ComponentUtil.addListener(this, JmixRememberMeChangedEvent.class, this::onRememberMeChangedEvent);
        ComponentUtil.addListener(this, JmixLocaleChangedEvent.class, this::onLocaleChangedEvent);
    }

    /**
     * @return entered username
     */
    @Synchronize(USERNAME_PROPERTY)
    public String getUsername() {
        return getElement().getProperty(USERNAME_PROPERTY);
    }

    /**
     * Sets username to the field.
     *
     * @param username username to set
     */
    public void setUsername(String username) {
        getElement().setProperty(USERNAME_PROPERTY, username);
    }

    /**
     * @return entered password
     */
    @Synchronize(PASSWORD_PROPERTY)
    public String getPassword() {
        return getElement().getProperty(PASSWORD_PROPERTY);
    }

    /**
     * Sets password to the field.
     *
     * @param password password to set
     */
    public void setPassword(String password) {
        getElement().setProperty(PASSWORD_PROPERTY, password);
    }

    /**
     * @return {@code true} if "Remember Me" component is visible
     */
    @Synchronize(REMEMBER_ME_VISIBILITY_PROPERTY)
    public boolean isRememberMeVisible() {
        return getElement().getProperty(REMEMBER_ME_VISIBILITY_PROPERTY, true);
    }

    /**
     * Sets visibility of "Remember Me" component.
     *
     * @param visible whether component should be visible
     */
    public void setRememberMeVisible(boolean visible) {
        getElement().setProperty(REMEMBER_ME_VISIBILITY_PROPERTY, visible);
    }

    /**
     * @return {@code true} if component with locales is visible
     */
    @Synchronize(LOCALES_VISIBILITY_PROPERTY)
    public boolean isLocalesVisible() {
        return getElement().getProperty(LOCALES_VISIBILITY_PROPERTY, true);
    }

    /**
     * Sets visibility of component with locales
     *
     * @param visible whether component should be visible
     */
    public void setLocalesVisible(boolean visible) {
        getElement().setProperty(LOCALES_VISIBILITY_PROPERTY, visible);
    }

    /**
     * Sets available locales to select.
     *
     * @param locales locale items
     */
    public void setLocaleItems(Collection<Locale> locales) {
        this.locales = new ArrayList<>(locales);

        List<LocaleItem> localeItems = locales.stream()
                .map(locale -> new LocaleItem(generateItemLabel(locale), localeToString(locale)))
                .collect(Collectors.toList());

        getElement().setPropertyJson("locales", JsonSerializer.toJson(localeItems));
    }

    /**
     * @return selected locale
     */
    public Locale getSelectedLocale() {
        return selectedLocale;
    }

    /**
     * Selects provided locale if locale options contain it.
     *
     * @param locale locale to select
     */
    public void setSelectedLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }

        if (isLocaleChanged(locale)) {
            getElement().callJsFunction("selectLocale", localeToString(locale));

            handleLocaleChanged(false, locale);
        }
    }

    /**
     * @return {@code true} if "Remember Me" option is checked
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * Sets whether "Remember Me" option should be checked or not.
     *
     * @param rememberMe rememberMe option
     */
    public void setRememberMe(boolean rememberMe) {
        if (isRememberMeChanged(rememberMe)) {
            getElement().callJsFunction("setRememberMe", rememberMe);

            handleRememberMeChanged(false, rememberMe);
        }
    }

    /**
     * @return label generator for the locale items or {@code null} if not set
     */
    public Function<Locale, String> getLocaleItemLabelGenerator() {
        return localeItemLabelGenerator;
    }

    /**
     * Adds listener to handle changes in "Remember Me" option.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    public Registration addRememberMeChangedListener(ComponentEventListener<RememberMeChangedEvent> listener) {
        return ComponentUtil.addListener(this, RememberMeChangedEvent.class, listener);
    }

    /**
     * Adds listener to handle locale selection changes.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    public Registration addLocaleChangedListener(ComponentEventListener<LocaleChangedEvent> listener) {
        return ComponentUtil.addListener(this, LocaleChangedEvent.class, listener);
    }

    /**
     * Sets label generator for the locale items.
     *
     * @param localeItemLabelGenerator item label generator to set
     */
    public void setLocaleItemLabelGenerator(@Nullable Function<Locale, String> localeItemLabelGenerator) {
        this.localeItemLabelGenerator = localeItemLabelGenerator;
    }

    protected void onRememberMeChangedEvent(JmixRememberMeChangedEvent event) {
        handleRememberMeChanged(event.isFromClient(), event.isChecked());
    }

    protected void handleRememberMeChanged(boolean isFromClient, boolean newValue) {
        rememberMe = newValue;

        RememberMeChangedEvent changedEvent =
                new RememberMeChangedEvent(this, isFromClient, rememberMe);

        getEventBus().fireEvent(changedEvent);
    }

    protected void onLocaleChangedEvent(JmixLocaleChangedEvent event) {
        Locale locale = localeFromString(event.getLocaleString());

        handleLocaleChanged(event.isFromClient(), locale);
    }

    protected void handleLocaleChanged(boolean isFromClient, Locale newLocale) {
        Locale oldValue = selectedLocale;
        selectedLocale = newLocale;

        setupLocale(selectedLocale);

        fireLocaleChangedEvent(oldValue, selectedLocale, isFromClient);
    }

    protected void fireLocaleChangedEvent(Locale oldValue, Locale value, Boolean isFromClient) {
        LocaleChangedEvent changedEvent =
                new LocaleChangedEvent(this, isFromClient, oldValue, value);

        getEventBus().fireEvent(changedEvent);
    }

    protected void setupLocale(Locale locale) {
        // used in descendants
    }

    protected String generateItemLabel(Locale locale) {
        if (localeItemLabelGenerator != null) {
            return localeItemLabelGenerator.apply(locale);
        }

        return applyDefaultValueFormat(locale);
    }

    protected String applyDefaultValueFormat(Locale locale) {
        return locale.getDisplayLanguage();
    }

    protected boolean isRememberMeChanged(boolean rememberMe) {
        return this.rememberMe != rememberMe;
    }

    protected boolean isLocaleChanged(Locale locale) {
        return locales.contains(locale) && !locale.equals(selectedLocale);
    }

    protected String localeToString(Locale locale) {
        return locale.toLanguageTag();
    }

    protected Locale localeFromString(String locale) {
        return Locale.forLanguageTag(locale);
    }

    @DomEvent(REMEMBER_ME_CHANGED_EVENT)
    protected static class JmixRememberMeChangedEvent extends ComponentEvent<EnhancedLoginForm> {

        protected final Boolean checked;

        public JmixRememberMeChangedEvent(EnhancedLoginForm source, boolean fromClient,
                                          @EventData("event.detail.checked") Boolean checked) {
            super(source, fromClient);
            this.checked = checked;
        }

        public Boolean isChecked() {
            return checked;
        }
    }

    @DomEvent(LOCALE_CHANGED_EVENT)
    protected static class JmixLocaleChangedEvent extends ComponentEvent<EnhancedLoginForm> {

        protected final String localeString;

        public JmixLocaleChangedEvent(EnhancedLoginForm source, boolean fromClient,
                                      @EventData("event.detail.localeString") String localeString) {
            super(source, fromClient);
            this.localeString = localeString;
        }

        public String getLocaleString() {
            return localeString;
        }
    }

    /**
     * An event that is fired when "Remember Me" becomes checked and unchecked.
     */
    public static class RememberMeChangedEvent extends ComponentEvent<EnhancedLoginForm> {

        protected boolean checked;

        public RememberMeChangedEvent(EnhancedLoginForm source, boolean fromClient, boolean checked) {
            super(source, fromClient);
            this.checked = checked;
        }

        /**
         * @return {@code true} if "Remember Me" option is checked
         */
        public boolean isChecked() {
            return checked;
        }
    }

    /**
     * An event that is fired when the user selects another locale.
     */
    public static class LocaleChangedEvent extends ComponentEvent<EnhancedLoginForm> {

        protected Locale oldValue;
        protected Locale value;

        public LocaleChangedEvent(EnhancedLoginForm source, boolean fromClient, Locale oldValue, Locale value) {
            super(source, fromClient);
            this.oldValue = oldValue;
            this.value = value;
        }

        /**
         * @return previous value
         */
        public Locale getOldValue() {
            return oldValue;
        }

        /**
         * @return current value
         */
        public Locale getValue() {
            return value;
        }
    }
}
