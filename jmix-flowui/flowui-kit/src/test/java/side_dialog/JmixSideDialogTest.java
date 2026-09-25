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

package side_dialog;

import io.jmix.flowui.kit.component.sidedialog.JmixSideDialog;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JmixSideDialogTest {

    @Test
    void getAriaRole_byDefault_returnsDialog() {
        JmixSideDialog sideDialog = new JmixSideDialog();

        assertEquals(Optional.of("dialog"), sideDialog.getAriaRole());
    }

    @Test
    void setAriaRole_setsRoleOnDialogElement() {
        JmixSideDialog sideDialog = new JmixSideDialog();

        sideDialog.setAriaRole("alertdialog");

        assertEquals("alertdialog", sideDialog.getElement().getAttribute("role"));
    }

    @Test
    void setRole_setsAriaRole() {
        JmixSideDialog sideDialog = new JmixSideDialog();

        sideDialog.setRole("alertdialog");

        assertEquals(Optional.of("alertdialog"), sideDialog.getAriaRole());
    }
}
