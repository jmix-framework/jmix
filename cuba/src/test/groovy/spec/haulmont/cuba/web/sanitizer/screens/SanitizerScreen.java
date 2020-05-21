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

package spec.haulmont.cuba.web.sanitizer.screens;

import io.jmix.ui.component.MessageDialogFacet;
import io.jmix.ui.component.NotificationFacet;
import io.jmix.ui.component.OptionDialogFacet;
import io.jmix.ui.component.RichTextArea;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;

@UiController
@UiDescriptor("sanitizer-screen.xml")
public class SanitizerScreen extends Screen {

    @Inject
    public TextField<String> textField;

    @Inject
    public RichTextArea richTextArea;

    @Inject
    public MessageDialogFacet messageDialogFacet;

    @Inject
    public OptionDialogFacet optionDialogFacet;

    @Inject
    public NotificationFacet notificationFacet;
}
