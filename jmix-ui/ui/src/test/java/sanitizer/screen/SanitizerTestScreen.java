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

package sanitizer.screen;

import io.jmix.ui.component.MessageDialogFacet;
import io.jmix.ui.component.NotificationFacet;
import io.jmix.ui.component.OptionDialogFacet;
import io.jmix.ui.component.RichTextArea;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("sanitizer-test-screen.xml")
public class SanitizerTestScreen extends Screen {

    @Autowired
    public TextField<String> textField;

    @Autowired
    public RichTextArea richTextArea;

    @Autowired
    public MessageDialogFacet messageDialogFacet;

    @Autowired
    public OptionDialogFacet optionDialogFacet;

    @Autowired
    public NotificationFacet notificationFacet;
}
