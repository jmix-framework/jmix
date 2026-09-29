/*
 * Copyright 2026 Haulmont.
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

package io.jmix.security.role;

import com.google.common.base.Strings;
import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.BaseRoleModel;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Shows the name and the description of a role in a locale.
 * <p>
 * A name or a description that has the key of a message, as a design-time role gets from a {@code msg://} reference,
 * is shown through that message: in the locale, then in the default locale, and the name or the description itself
 * when neither has a text. Otherwise the text is taken from the localized names or descriptions of the role: the
 * entry of the exact locale, then the entry of its language, and the name or the description itself when neither
 * has a text.
 */
@NullMarked
@Component("sec_RoleLocalizationSupport")
public class RoleLocalizationSupport {

    private static final Logger log = LoggerFactory.getLogger(RoleLocalizationSupport.class);

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    /**
     * @return the name of the role in the current user's locale
     */
    public String getLocalizedName(BaseRole role) {
        return getLocalizedName(role, currentAuthentication.getLocale());
    }

    /**
     * @return the name of the role in the given locale
     */
    public String getLocalizedName(BaseRole role, Locale locale) {
        checkNotNullArgument(role, "role is null");
        checkNotNullArgument(locale, "locale is null");

        return Strings.nullToEmpty(
                localize(role.getNameMessageKey(), role.getLocalizedNames(), role.getName(), locale));
    }

    /**
     * @return the description of the role in the current user's locale, or null if the role has none or it is blank
     */
    @Nullable
    public String getLocalizedDescription(BaseRole role) {
        return getLocalizedDescription(role, currentAuthentication.getLocale());
    }

    /**
     * @return the description of the role in the given locale, or null if the role has none or it is blank
     */
    @Nullable
    public String getLocalizedDescription(BaseRole role, Locale locale) {
        checkNotNullArgument(role, "role is null");
        checkNotNullArgument(locale, "locale is null");

        return nullIfAbsent(localize(role.getDescriptionMessageKey(), role.getLocalizedDescriptions(),
                role.getDescription(), locale));
    }

    /**
     * @return the name of the role model in the current user's locale
     * @see #getLocalizedName(BaseRole)
     */
    public String getLocalizedName(BaseRoleModel roleModel) {
        return getLocalizedName(roleModel, currentAuthentication.getLocale());
    }

    /**
     * @return the name of the role model in the given locale
     * @see #getLocalizedName(BaseRole, Locale)
     */
    public String getLocalizedName(BaseRoleModel roleModel, Locale locale) {
        checkNotNullArgument(roleModel, "roleModel is null");
        checkNotNullArgument(locale, "locale is null");

        return Strings.nullToEmpty(
                localize(roleModel.getNameMessageKey(), roleModel.getLocalizedNames(), roleModel.getName(), locale));
    }

    /**
     * @return the description of the role model in the current user's locale, or null if it has none or it is blank
     * @see #getLocalizedDescription(BaseRole)
     */
    @Nullable
    public String getLocalizedDescription(BaseRoleModel roleModel) {
        return getLocalizedDescription(roleModel, currentAuthentication.getLocale());
    }

    /**
     * @return the description of the role model in the given locale, or null if it has none or it is blank
     * @see #getLocalizedDescription(BaseRole, Locale)
     */
    @Nullable
    public String getLocalizedDescription(BaseRoleModel roleModel, Locale locale) {
        checkNotNullArgument(roleModel, "roleModel is null");
        checkNotNullArgument(locale, "locale is null");

        return nullIfAbsent(localize(roleModel.getDescriptionMessageKey(), roleModel.getLocalizedDescriptions(),
                roleModel.getDescription(), locale));
    }

    @Nullable
    protected String localize(@Nullable String messageKey, @Nullable String localizedValues, @Nullable String text,
                              Locale locale) {
        if (messageKey != null && !messageKey.isEmpty()) {
            // The lookup of a locale never reaches the bundle of the default locale, so that locale is looked up on
            // its own: the stored text may not hold its message yet.
            String message = messages.findMessage(messageKey, locale);
            Locale defaultLocale = messageTools.getDefaultLocale();
            if (isAbsent(message) && !locale.equals(defaultLocale)) {
                message = messages.findMessage(messageKey, defaultLocale);
            }
            return isAbsent(message) ? text : message;
        }

        Map<String, String> values = loadLocalizedValues(localizedValues);
        String localized = values.get(LocaleResolver.localeToString(locale));
        if (isAbsent(localized)) {
            localized = values.get(locale.getLanguage());
        }

        return isAbsent(localized) ? text : localized;
    }

    protected Map<String, String> loadLocalizedValues(@Nullable String localizedValues) {
        try {
            return RoleLocalizedValuesUtils.read(localizedValues);
        } catch (IllegalArgumentException e) {
            // A malformed escape makes the localized values unusable; the default text is shown instead. The provider
            // of a database role reports such values when it builds the role, so this is logged at debug only, with
            // the line breaks of the values escaped to keep the record on one line.
            String values = StringUtils.replaceEach(localizedValues,
                    new String[]{"\r", "\n"}, new String[]{"\\r", "\\n"});
            log.debug("Cannot read the localized values of a role '{}': {}", values, e.getMessage());
            return Map.of();
        }
    }

    protected boolean isAbsent(@Nullable String value) {
        return value == null || value.isBlank();
    }

    /**
     * A design-time role without a description holds the empty string its annotation declares by default, a database
     * role holds null; both mean the role has none.
     */
    @Nullable
    protected String nullIfAbsent(@Nullable String value) {
        return isAbsent(value) ? null : value;
    }
}
