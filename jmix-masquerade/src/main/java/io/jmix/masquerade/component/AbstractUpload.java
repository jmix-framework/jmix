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

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import java.io.File;

import static com.codeborne.selenide.Selectors.shadowCss;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.*;

/**
 * Abstract class for upload-like web-element wrappers.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractUpload<T> extends AbstractComponent<T> {

    protected static final String UPLOAD_INPUT_SELECTOR = "input#fileInput";

    protected AbstractUpload(By by) {
        super(by);
    }

    /**
     * @return {@link SelenideElement} of an input web-element
     */
    protected SelenideElement getInputDelegate() {
        return $(shadowCss(UPLOAD_INPUT_SELECTOR, getUploadButtonCssSelector()))
                .shouldBe(EXIST);
    }

    /**
     * @return CSS selector of the upload button
     */
    protected abstract String getUploadButtonCssSelector();

    /**
     * Sets the file value to the upload field.
     *
     * @param file file or files to upload
     */
    public void setValue(File... file) {
        getInputDelegate()
                .shouldBe(ENABLED)
                .shouldNotBe(READONLY)
                .uploadFile(file);
    }
}
