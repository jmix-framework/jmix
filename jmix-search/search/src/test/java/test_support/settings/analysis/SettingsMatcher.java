/*
 * Copyright 2021 Haulmont.
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

package test_support.settings.analysis;

import org.elasticsearch.common.settings.Settings;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SettingsMatcher extends TypeSafeMatcher<Settings> {

    private final Settings expectedSettings;

    private SettingsMatcher(Settings expectedSettings) {
        this.expectedSettings = expectedSettings;
    }

    @Override
    protected boolean matchesSafely(Settings settings) {
        return expectedSettings.equals(settings);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expectedSettings.toString());
    }

    public static Matcher<Settings> configureWith(Settings expectedSettings) {
        return new SettingsMatcher(expectedSettings);
    }
}
