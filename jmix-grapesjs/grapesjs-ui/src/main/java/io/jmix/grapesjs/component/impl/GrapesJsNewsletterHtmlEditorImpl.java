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

package io.jmix.grapesjs.component.impl;


import io.jmix.grapesjs.component.GrapesJsNewsletterHtmlEditor;

public class GrapesJsNewsletterHtmlEditorImpl extends GrapesJsHtmlEditorImpl implements GrapesJsNewsletterHtmlEditor {

    protected Boolean inlineCss = false;

    public Boolean getInlineCss() {
        return inlineCss;
    }

    public void setInlineCss(Boolean inlineCss) {
        this.inlineCss = inlineCss;
        component.setInlineCss(inlineCss);
    }
}
