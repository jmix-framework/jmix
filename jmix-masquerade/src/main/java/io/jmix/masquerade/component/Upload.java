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

package io.jmix.masquerade.component;

import org.openqa.selenium.By;

import static io.jmix.masquerade.Masquerade.UI_TEST_ID;

/**
 * Web-element wrapper for upload component.
 */
public class Upload extends AbstractUpload<Upload> {

    public Upload(By by) {
        super(by);
    }

    @Override
    protected String getUploadButtonCssSelector() {
        return "vaadin-upload[%s='%s']".formatted(
                UI_TEST_ID,
                getDelegate().getDomAttribute(UI_TEST_ID));
    }
}
