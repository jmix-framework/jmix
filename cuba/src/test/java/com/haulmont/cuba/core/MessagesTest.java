///*
// * Copyright (c) 2008-2016 Haulmont.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
package com.haulmont.cuba.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.messagespack.MpTestObj;
import com.haulmont.cuba.core.messagespack.nested.MpTestNestedEnum;
import com.haulmont.cuba.core.messagespack.nested.MpTestNestedObj;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestAppender;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.CoreProperties;
import io.jmix.core.LocaleResolver;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class MessagesTest {

    private final TestAppender appender;

    public MessagesTest() {
        appender = new TestAppender();
        appender.start();

        TestSupport.setAuthenticationToSecurityContext();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("com.haulmont.cuba.core.global.impl.CubaMessages");
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
    }

    @Test
    public void test() {
        Messages messages = AppBeans.get(Messages.class);

        String msg = messages.getMessage(MpTestNestedObj.class, "key0");
        assertEquals("Message0", msg);

        msg = messages.getMessage(MpTestObj.class, "key1");
        assertEquals("Message1", msg);

        msg = messages.getMessage(MpTestNestedObj.class, "key2");
        assertEquals("Message2", msg);

        // test cache
        msg = messages.getMessage(MpTestNestedObj.class, "key0");
        assertEquals("Message0", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack.nested", "key1");
        assertEquals("Message1", msg);

        msg = messages.getMessage("test", "key1");
        assertEquals("key1", msg);

        msg = messages.getMessage(MpTestNestedEnum.ONE);
        assertEquals("One", msg);

        msg = messages.getMessage(MpTestNestedObj.InternalEnum.FIRST);
        assertEquals("First", msg);

    }

    @Test
    public void testInclude() {
        Messages messages = AppBeans.get(Messages.class);

        String msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "includedMsg");
        assertEquals("Included Message", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "includedMsgToOverride");
        assertEquals("Overridden Included Message", msg);
    }

    @Test
    public void testMultiInclude() {
        Messages messages = AppBeans.get(Messages.class);

        String msg1 = messages.getMessage("com.haulmont.cuba.core.messagespack.includes", "oneKey");
        assertEquals(msg1, "OK");

        String msg2 = messages.getMessage("com.haulmont.cuba.core.messagespack.includes", "twoKey");
        assertEquals(msg2, "OK");

        String msg3 = messages.getMessage("com.haulmont.cuba.core.messagespack.includes", "threeKey");
        assertEquals(msg3, "overridden");
    }

    @Test
    //todo MG
    @Disabled
    public void testCachingDefaultLoc() {
        Messages messages = prepareCachingTest();

        appender.getMessages().clear();

        String msg = messages.getMessage(MpTestNestedObj.class, "key0");
        assertEquals("Message0", msg);

        assertEquals(2,
                Iterables.size(Iterables.filter(appender.getMessages(), new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String input) {
                        return input != null && input.contains("searchFiles:");
                    }
                }))
        );
        assertEquals(2,
                Iterables.size(Iterables.filter(appender.getMessages(), new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String input) {
                        return input != null && input.contains("searchClasspath:");
                    }
                }))
        );

        appender.getMessages().clear();

        msg = messages.getMessage(MpTestNestedObj.class, "key0");
        assertEquals("Message0", msg);

        assertEquals(0,
                getSearchMessagesCount()
        );
    }

    @Test
    public void testCachingFrenchLoc() {
        Messages messages = prepareCachingTest();

        appender.getMessages().clear();

        String msg = messages.getMessage(MpTestNestedObj.class, "key0", Locale.forLanguageTag("fr"));
        assertEquals("Message0 in French", msg);
        assertEquals(2, getSearchMessagesCount());

        appender.getMessages().clear();

        msg = messages.getMessage(MpTestNestedObj.class, "key0", Locale.forLanguageTag("fr"));
        assertEquals("Message0 in French", msg);
        assertEquals(0, getSearchMessagesCount());
    }

    private Messages prepareCachingTest() {
        Messages messages = AppBeans.get(Messages.class);
        messages.clearCache();
        return messages;
    }

    @Test
    //todo MG
    @Disabled
    public void testCachingDefaultLocSeveralPacks() {
        Messages messages = prepareCachingTest();

        appender.getMessages().clear();

        String msg = messages.getMessage("com.haulmont.cuba.core.messagespack.nested com.haulmont.cuba.core.messagespack", "key0");
        assertEquals("Message0", msg);
        assertEquals(10, getSearchMessagesCount());

        appender.getMessages().clear();

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack.nested com.haulmont.cuba.core.messagespack", "key0");
        assertEquals("Message0", msg);
        assertEquals(0, getSearchMessagesCount());
    }

    @Test
    public void testCachingFrenchLocSeveralPacks() {
        Messages messages = prepareCachingTest();

        appender.getMessages().clear();

        String msg = messages.getMessage("com.haulmont.cuba.core.messagespack.nested com.haulmont.cuba.core.messagespack", "key0",
                Locale.forLanguageTag("fr"));
        assertEquals("Message0 in French", msg);
        assertEquals(12, getSearchMessagesCount());

        appender.getMessages().clear();

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack.nested com.haulmont.cuba.core.messagespack", "key0",
                Locale.forLanguageTag("fr"));
        assertEquals("Message0 in French", msg);
        assertEquals(0, getSearchMessagesCount());
    }

    @Test
    public void testFind() throws Exception {
        Messages messages = AppBeans.get(Messages.class);
        UserSessionSource uss = AppBeans.get(UserSessionSource.class);

        String msg = messages.findMessage("com.haulmont.cuba.core.messagespack.nested", "key0", uss.getLocale());
        assertEquals("Message0", msg);

        msg = messages.findMessage("com.haulmont.cuba.core.messagespack.nested", "non-existing-message", uss.getLocale());
        assertNull(msg);

        msg = messages.findMessage("com.haulmont.cuba.core.messagespack.nested", "key0", null);
        assertEquals("Message0", msg);

        msg = messages.findMessage("com.haulmont.cuba.core.messagespack.nested", "non-existing-message", null);
        assertNull(msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack.nested", "non-existing-message", uss.getLocale());
        assertEquals("non-existing-message", msg);
    }

    @Test
    public void testMainMessagePack() throws Exception {
        Messages messages = AppBeans.get(Messages.class);

        String msg;

        msg = messages.getMainMessage("trueString", Locale.forLanguageTag("en"));
        assertEquals("True", msg);

        msg = messages.getMessage("io.jmix.core.something", "trueString", Locale.forLanguageTag("en"));
        assertEquals("True", msg);

        appender.getMessages().clear();
        msg = messages.getMessage("io.jmix.core.something", "trueString", Locale.forLanguageTag("en"));
        assertEquals("True", msg);
        assertEquals(0, getSearchMessagesCount());
    }

    /**
     * Test hierarchy of country/language/default message packs.
     * <p>
     * messages.properties:<br>
     * commonMsg=Common Message<br>
     * languageMsg=Language Message<br>
     * countryMsg=Country Message<br>
     * <p>
     * messages_fr.properties:<br>
     * languageMsg=Language Message fr<br>
     * countryMsg=Country Message fr<br>
     * <p>
     * messages_fr_CA.properties:<br>
     * countryMsg=Country Message fr CA<br>
     */
    @Test
    public void testLanguageAndCountry() throws Exception {
        Messages messages = AppBeans.get(Messages.class);

        List<Locale> availableLocales = AppBeans.get(CoreProperties.class).getAvailableLocales();
        assertTrue(availableLocales.contains(Locale.forLanguageTag("fr")));
        assertTrue(availableLocales.contains(Locale.forLanguageTag("fr-CA")));

        String msg;

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "commonMsg", Locale.forLanguageTag("fr-CA"));
        assertEquals("Common Message", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "languageMsg", Locale.forLanguageTag("fr-CA"));
        assertEquals("Language Message fr", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "countryMsg", Locale.forLanguageTag("fr-CA"));
        assertEquals("Country Message fr CA", msg);
    }

    @Test
    public void testScriptAndVariant() throws Exception {
        Messages messages = AppBeans.get(Messages.class);

        List<Locale> availableLocales = AppBeans.get(CoreProperties.class).getAvailableLocales();
        assertTrue(availableLocales.contains(LocaleUtils.toLocale("sr")));
        assertTrue(availableLocales.contains(Locale.forLanguageTag("sr-Latn")));
        assertTrue(availableLocales.contains(LocaleUtils.toLocale("ja")));
        assertTrue(availableLocales.contains(LocaleUtils.toLocale("ja_JP_JP")));

        assertEquals(LocaleResolver.resolve("sr-Latn"), Locale.forLanguageTag("sr-Latn"));
        assertEquals(LocaleResolver.resolve("ja_JP_JP"), LocaleUtils.toLocale("ja_JP_JP"));

        String msg;

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "fullMsg", Locale.forLanguageTag("sr-Latn"));
        assertEquals("Full Message sr-Latn", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "languageMsg", LocaleUtils.toLocale("sr"));
        assertEquals("Language Message sr", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "languageMsg", Locale.forLanguageTag("sr-Latn"));
        assertEquals("Language Message sr", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "fullMsg", LocaleUtils.toLocale("ja_JP_JP"));
        assertEquals("Full Message ja_JP_JP", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "languageMsg", LocaleUtils.toLocale("ja"));
        assertEquals("Language Message ja", msg);

        msg = messages.getMessage("com.haulmont.cuba.core.messagespack", "languageMsg", LocaleUtils.toLocale("ja_JP_JP"));
        assertEquals("Language Message ja", msg);
    }

    private int getSearchMessagesCount() {
        return Iterables.size(Iterables.filter(appender.getMessages(), new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null && (input.contains("searchFiles:") || input.contains("searchClasspath:"));
            }
        }));
    }

}
