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

package localization;

import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import test_support.FullCalendarFlowuiTestConfiguration;
import tools.jackson.databind.JsonNode;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the locale that the calendar sends to the client.
 */
@UiTest
@SpringBootTest(classes = {FullCalendarFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class FullCalendarLocaleTest {

    @Autowired
    UiComponents uiComponents;
    @Autowired
    AuthenticationManager authenticationManager;

    @Nullable
    Authentication previousAuthentication;

    @AfterEach
    void restoreAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(previousAuthentication);
    }

    @Test
    void create_userLocaleWithCountry_sendsLanguageTag() {
        assertEquals("ru-RU", createCalendarAndGetLocale(Locale.of("ru", "RU")));
    }

    @Test
    void create_userLocaleWithScript_sendsLanguageTag() {
        Locale locale = new Locale.Builder().setLanguage("sr").setScript("Cyrl").setRegion("RS").build();

        assertEquals("sr-Cyrl-RS", createCalendarAndGetLocale(locale));
    }

    @Test
    void create_userLocaleWithExtension_sendsLanguageTagWithoutExtension() {
        assertEquals("th-TH", createCalendarAndGetLocale(Locale.forLanguageTag("th-TH-u-nu-thai")));
    }

    @Test
    void create_userLocaleWithIllFormedVariant_sendsLanguageTagWithoutPrivateUse() {
        assertEquals("ja-JP", createCalendarAndGetLocale(Locale.of("ja", "JP", "JP")));
    }

    String createCalendarAndGetLocale(Locale userLocale) {
        authenticateWithLocale(userLocale);

        FullCalendar calendar = uiComponents.create(FullCalendar.class);
        JsonNode i18n = (JsonNode) calendar.getElement().getPropertyRaw("i18n");

        return i18n.get("locale").asString();
    }

    void authenticateWithLocale(Locale locale) {
        previousAuthentication = SecurityContextHolder.getContext().getAuthentication();

        SystemAuthenticationToken authentication = (SystemAuthenticationToken)
                authenticationManager.authenticate(new SystemAuthenticationToken(null));
        authentication.setDetails(ClientDetails.builder().locale(locale).build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
