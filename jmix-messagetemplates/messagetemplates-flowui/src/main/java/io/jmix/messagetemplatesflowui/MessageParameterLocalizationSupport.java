/*
 * Copyright 2025 Haulmont.
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

package io.jmix.messagetemplatesflowui;

import com.google.common.base.Strings;
import io.jmix.core.CoreProperties;
import io.jmix.core.LocaleResolver;
import io.jmix.core.Metadata;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplatesflowui.view.messagetemplateparameter.model.MessageTemplateParameterLocalization;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Support class for processing {@link MessageTemplateParameterLocalization}.
 */
@Component("msgtmp_MessageParameterLocalizationSupport")
public class MessageParameterLocalizationSupport {

    protected CurrentAuthentication currentAuthentication;
    protected Metadata metadata;
    protected CoreProperties coreProperties;

    public MessageParameterLocalizationSupport(CurrentAuthentication currentAuthentication,
                                               Metadata metadata,
                                               CoreProperties coreProperties) {
        this.currentAuthentication = currentAuthentication;
        this.metadata = metadata;
        this.coreProperties = coreProperties;
    }

    /**
     * Gets localized name for the passed {@link MessageTemplateParameter parameter} if one is defined,
     * otherwise returns the default name of the parameter.
     *
     * @param parameter parameter for getting localized name
     * @return localized name for the passed {@link MessageTemplateParameter parameter} if one is defined,
     * otherwise returns the default name of the parameter
     */
    public String getLocalizedName(MessageTemplateParameter parameter) {
        String localization = parameter.getLocalization();
        String defaultName = parameter.getName();

        if (Strings.isNullOrEmpty(localization)) {
            return defaultName;
        }

        String currentLocale = LocaleResolver.localeToString(currentAuthentication.getLocale());

        return convertLocalizationsToLocalizationEntities(localization).stream()
                .filter(localizedName -> localizedName.getLocale().equals(currentLocale))
                .findAny()
                .map(MessageTemplateParameterLocalization::getName)
                .orElse(defaultName);
    }

    /**
     * Converts the passed localizations string into a special {@link MessageTemplateParameterLocalization DTO entities}.
     * <p>
     * Example of a passed string:
     * <pre>{@code
     *      en=User
     *      de=Benutzer
     * }</pre>
     *
     * @param localization localization string to convert
     * @return special {@link MessageTemplateParameterLocalization DTO entity} to represent the locale
     */
    public List<MessageTemplateParameterLocalization> convertLocalizationsToLocalizationEntities(String localization) {
        return Arrays.stream(localization.split("\n"))
                .map(this::convertStringToLocalizationEntityMapper)
                .filter(this::isAvailableLocale)
                .toList();
    }

    /**
     * Converts the passed special {@link MessageTemplateParameterLocalization DTO entity}
     * to a localization string.
     *
     * @param localization special {@link MessageTemplateParameterLocalization DTO entity} to convert
     * @return localization string
     */
    public String convertLocalizationEntityToStringMapper(MessageTemplateParameterLocalization localization) {
        return "%s=%s".formatted(localization.getLocale(), localization.getName());
    }

    /**
     * Converts the passed localization string into a special {@link MessageTemplateParameterLocalization DTO entity}.
     * <p>
     * Example of a passed string: {@code en=User}.
     *
     * @param localization localization string to convert
     * @return special {@link MessageTemplateParameterLocalization DTO entity} to represent the locale
     */
    public MessageTemplateParameterLocalization convertStringToLocalizationEntityMapper(String localization) {
        String[] localizationValues = localization.split("=");

        MessageTemplateParameterLocalization entity = metadata.create(MessageTemplateParameterLocalization.class);

        if (localizationValues.length != 2) {

            // unsupportable value, will be filtered later
            entity.setLocale("");
            return entity;
        }

        entity.setLocale(localizationValues[0]);
        entity.setName(localizationValues[1]);

        return entity;
    }

    /**
     * @param selectedLocales locales that have already been selected
     * @return list of available locales as a string filtered by the passed already selected locales
     */
    public List<String> getUnselectedLocales(List<MessageTemplateParameterLocalization> selectedLocales) {
        return coreProperties.getAvailableLocales().stream()
                .map(LocaleResolver::localeToString)
                .filter(locale -> selectedLocalesFilter(selectedLocales, locale))
                .toList();
    }

    /**
     * @return count of available locales
     */
    public int getAvailableLocalesCount() {
        return coreProperties.getAvailableLocales().size();
    }

    protected boolean selectedLocalesFilter(List<MessageTemplateParameterLocalization> existedLocales, String locale) {
        return existedLocales.stream()
                .map(MessageTemplateParameterLocalization::getLocale)
                .noneMatch(locale::equals);
    }

    protected boolean isAvailableLocale(MessageTemplateParameterLocalization entity) {
        return coreProperties.getAvailableLocales().stream()
                .map(LocaleResolver::localeToString)
                .anyMatch(entity.getLocale()::equals);
    }
}
