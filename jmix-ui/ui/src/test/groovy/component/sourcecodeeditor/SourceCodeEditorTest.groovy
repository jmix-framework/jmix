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

package component.sourcecodeeditor

import component.sourcecodeeditor.screen.SourceCodeEditorScreenTest
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.impl.SourceCodeEditorImpl
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import io.jmix.ui.widget.addon.aceeditor.Suggester
import io.jmix.ui.widget.addon.aceeditor.Suggestion
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class SourceCodeEditorTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.sourcecodeeditor.screen"])
    }

    def "SourceCodeEditor applySuggestion"() {
        showTestMainScreen()

        def screen = (SourceCodeEditorScreenTest) screens.create(SourceCodeEditorScreenTest)
        screen.sourceCodeEditor.setValue("ABCDEFG")

        Suggester suggester = ((SourceCodeEditorImpl) screen.sourceCodeEditor).suggestionExtension.suggester

        expect:
        text == suggester.applySuggestion(createSuggestion(start, end), "ABCDEFG", cursor)

        where:
        start | end | cursor || text
         1    |  3  | 5      || "A___DEFG"        //  1 = B          ;  3 = D                        ; 5 = F
        -1    | -1  | 5      || "ABCDE___FG"      // -1 = cursor     ; -1 = cursor                   ; 5 = F
         3    | -1  | 5      || "ABC___FG"        //  3 = D          ; -1 = cursor                   ; 5 = F
        -1    |  6  | 5      || "ABCDE___G"       // -1 = cursor     ;  6 = G                        ; 5 = F
         0    |  6  | 5      || "___G"            //  0 = A          ;  6 = G                        ; 5 = F
         1    |  7  | 5      || "A___"            //  1 = B          ;  7 = greater than text length ; 5 = F
    }

    protected Suggestion createSuggestion(int start, int end) {
        return new Suggestion("___", null, "___", start, end)
    }
}
