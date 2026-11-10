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

package export_dialog.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * Shows products in a data grid with two export actions: one as configured by the properties, and one that lists
 * its dialog actions in XML.
 */
@Route("export-dialog-actions-test-view")
@ViewController("ExportDialogActionsTestView")
@ViewDescriptor("export-dialog-actions-test-view.xml")
public class ExportDialogActionsTestView extends StandardView {
}
