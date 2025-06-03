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

package io.jmix.masquerade.component;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.jmix.masquerade.condition.*;
import io.jmix.masquerade.sys.Composite;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static org.openqa.selenium.By.xpath;

/**
 * Web-element wrapper for notifications. Supports closing, {@link NotificationTheme},
 * {@link NotificationPosition}, {@link NotificationTitle}, {@link NotificationTitleContains},
 * {@link NotificationMessage} and {@link NotificationMessageContains} condition checking.
 */
public class Notification extends Composite<Notification> {

    /**
     * Closes this notification by clicking close button.
     */
    public void close() {
        By buttonPath = xpath("./div[contains(@class, 'component-content')]" +
                "/vaadin-button[@class='close-button']");

        getDelegate().find(buttonPath)
                .shouldBe(VISIBLE)
                .click();
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof NotificationTheme notificationTheme) {
            shouldHave(domAttribute("theme", notificationTheme.getValue().getThemeName()));
            return CheckResult.accepted();
        } else if (condition instanceof NotificationPosition notificationPosition) {
            shouldHave(domAttribute("slot", notificationPosition.getValue().getSlotName()));
            return CheckResult.accepted();
        } else if (condition instanceof NotificationTitle notificationTitle) {
            getTitleElement().shouldHave(exactText(notificationTitle.getValue()));
            return CheckResult.accepted();
        } else if (condition instanceof NotificationTitleContains notificationTitleContains) {
            getTitleElement().shouldHave(text(notificationTitleContains.getValue()));
            return CheckResult.accepted();
        } else if (condition instanceof NotificationMessage notificationMessage) {
            getMessageElement().shouldHave(exactText(notificationMessage.getValue()));
            return CheckResult.accepted();
        } else if (condition instanceof NotificationMessageContains notificationMessageContains) {
            getMessageElement().shouldHave(text(notificationMessageContains.getValue()));
            return CheckResult.accepted();
        }

        return super.check(condition);
    }

    protected SelenideElement getTitleElement() {
        SelenideElement notificationContent = getDelegate()
                .find(TagNames.DIV);
        String titleCandidate = notificationContent.getOwnText();

        if (!Strings.isNullOrEmpty(titleCandidate)) {
            return notificationContent;
        }

        By titleElementSelector =
                xpath(".//div[contains(@class,'jmix-text-layout')]" +
                        "/h4[@class='title']");

        return getDelegate().find(titleElementSelector);
    }

    protected SelenideElement getMessageElement() {
        By titleElementSelector =
                xpath(".//div[contains(@class,'jmix-text-layout')]" +
                        "/p[@class='message']");

        return getDelegate().find(titleElementSelector);
    }

    /**
     * Theme enumeration of the notification. The values are mapped with {@code theme} DOM attribute values.
     */
    public enum Theme {

        PRIMARY("primary"),
        SUCCESS("success"),
        WARNING("warning"),
        ERROR("error"),
        CONTRAST("contrast");

        private final String themeName;

        Theme(String themeName) {
            this.themeName = themeName;
        }

        public String getThemeName() {
            return themeName;
        }
    }

    /**
     * Theme position of the notification. The values are mapped with {@code slot} DOM attribute values
     */
    public enum Position {

        TOP_STRETCH("top-stretch"),
        TOP_START("top-start"),
        TOP_CENTER("top-center"),
        TOP_END("top-end"),
        MIDDLE("middle"),
        BOTTOM_START("bottom-start"),
        BOTTOM_CENTER("bottom-center"),
        BOTTOM_END("bottom-end"),
        BOTTOM_STRETCH("bottom-stretch");

        private final String slotName;

        Position(String slotName) {
            this.slotName = slotName;
        }

        public String getSlotName() {
            return slotName;
        }
    }
}
