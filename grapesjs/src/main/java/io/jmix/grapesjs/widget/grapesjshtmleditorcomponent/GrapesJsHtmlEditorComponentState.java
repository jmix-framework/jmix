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

package io.jmix.grapesjs.widget.grapesjshtmleditorcomponent;

import com.vaadin.shared.ui.JavaScriptComponentState;
import io.jmix.grapesjs.component.GjsBlock;
import io.jmix.grapesjs.component.GjsPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GrapesJsHtmlEditorComponentState extends JavaScriptComponentState {

    public List<String> disabledBlocks = new ArrayList<>();

    public String html;

    public Collection<GjsPlugin> plugins;

    public Collection<GjsBlock> blocks;

    public Boolean inlineCss = false;

    public String command;

}