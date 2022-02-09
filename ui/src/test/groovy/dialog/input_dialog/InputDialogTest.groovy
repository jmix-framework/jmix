/*
 * Copyright (c) 2020 Haulmont.
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

package dialog.input_dialog

import com.google.common.base.Strings
import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.impl.*
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.DialogAction
import io.jmix.ui.app.inputdialog.DialogActions
import io.jmix.ui.app.inputdialog.InputDialog
import io.jmix.ui.app.inputdialog.InputParameter
import io.jmix.ui.component.*
import io.jmix.ui.component.inputdialog.InputDialogAction
import io.jmix.ui.screen.FrameOwner
import io.jmix.ui.screen.Screen
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.model_objects.GoodInfoObject
import test_support.entity.sales.Status

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class InputDialogTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['io.jmix.ui.app.inputdialog'])
    }

    def "input parameter ids should be different"() {

        def mainScreen = showTestMainScreen()

        when: "the same id is used"
        vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.parameter("same"),
                        InputParameter.parameter("same"),
                        InputParameter.parameter("not the same"))
                .show()
        then:
        thrown(IllegalArgumentException)

        when: "different ids are used"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.parameter("not the same 1"),
                        InputParameter.parameter("not the same 2"),
                        InputParameter.parameter("not the same 3"))
                .build()
        then:
        dialog.show()
    }

    def "input parameter types are presented"() {

        def mainScreen = showTestMainScreen()

        when: "all types are used"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.parameter("default"),
                        InputParameter.stringParameter("string"),
                        InputParameter.intParameter("int"),
                        InputParameter.doubleParameter("double"),
                        InputParameter.longParameter("long"),
                        InputParameter.bigDecimalParameter("bigDecimal"),
                        InputParameter.booleanParameter("boolean"),
                        InputParameter.entityParameter("entity", GoodInfoObject),
                        InputParameter.timeParameter("time"),
                        InputParameter.dateParameter("date"),
                        InputParameter.dateTimeParameter("dateTime"),
                        InputParameter.fileParameter("fileRef"),
                        InputParameter.byteArrayParameter("byteArray"),
                        InputParameter.localTimeParameter("localTime"),
                        InputParameter.localDateParameter("localDate"),
                        InputParameter.localDateTimeParameter("localDateTime"),
                        InputParameter.offsetTimeParameter("offsetTime"),
                        InputParameter.offsetDateTimeParameter("offsetDateTime"),
                )
                .show()
        then:
        def form = (Form) dialog.getWindow().getComponentNN("form")

        def defaultField = (TextField) form.getComponentNN("default")
        defaultField.getDatatype().getClass() == StringDatatype

        def stringField = (TextField) form.getComponentNN("string")
        stringField.getDatatype().getClass() == StringDatatype

        def intField = (TextField) form.getComponentNN("int")
        intField.getDatatype().getClass() == IntegerDatatype

        def doubleField = (TextField) form.getComponentNN("double")
        doubleField.getDatatype().getClass() == DoubleDatatype

        def bigDecimalField = (TextField) form.getComponentNN("bigDecimal")
        bigDecimalField.getDatatype().getClass() == BigDecimalDatatype

        (CheckBox) form.getComponentNN("boolean")
        (EntityPicker) form.getComponentNN("entity")

        def timeField = (TimeField) form.getComponentNN("time")
        timeField.getDatatype().getClass() == TimeDatatype

        def dateField = (DateField) form.getComponentNN("date")
        dateField.getDatatype().getClass() == DateDatatype

        def dateTimeField = (DateField) form.getComponentNN("dateTime")
        dateTimeField.getDatatype().getClass() == DateTimeDatatype

        (FileStorageUploadField) form.getComponentNN("fileRef")
        (FileUploadField) form.getComponentNN("byteArray")
        (TimeField) form.getComponentNN("localTime")
        (DateField) form.getComponentNN("localDate")
        (DateField) form.getComponentNN("localDateTime")
        (TimeField) form.getComponentNN("offsetTime")
        (DateField) form.getComponentNN("offsetDateTime")
    }

    def "default actions are created"() {

        def mainScreen = showTestMainScreen()

        when: "YES NO CANCEL are created"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.parameter("default"),
                        InputParameter.stringParameter("string"))
                .withActions(DialogActions.YES_NO_CANCEL)
                .show()
        then:
        def actionsLayout = (HBoxLayout) dialog.getWindow().getComponentNN("actionsLayout")
        actionsLayout.ownComponents.size() == 4

        // YES action
        def yesBtn = (Button) actionsLayout.getComponent(1) // because 0 - spacer
        def yesAction = yesBtn.getAction() as DialogAction
        yesAction.getType() == DialogAction.Type.YES

        // NO action
        def noBtn = (Button) actionsLayout.getComponent(2)
        def noAction = (DialogAction) noBtn.getAction()
        noAction.getType() == DialogAction.Type.NO

        // CANCEL action
        def cancelBtn = (Button) actionsLayout.getComponent(3)
        def cancelAction = (DialogAction) cancelBtn.getAction()
        cancelAction.getType() == DialogAction.Type.CANCEL
    }

    def "default actions with result handler"() {

        given:

        def mainScreen = showTestMainScreen()

        def goodInfoObject = metadata.create(GoodInfoObject)
        def defaultString = "default value"

        when: "dialog uses result handler"
        def dialog = showDialogWithResultHandler(mainScreen, defaultString, goodInfoObject)
        def yesBtn = getButtonFromDialog(dialog, 1) // because 0 - spacer
        yesBtn.getAction().actionPerform(yesBtn)

        then:
        !screens.getOpenedScreens().getActiveScreens().contains(dialog)

        when:
        dialog = showDialogWithResultHandler(mainScreen, defaultString, goodInfoObject)
        def noBtn = getButtonFromDialog(dialog, 2)
        noBtn.getAction().actionPerform(noBtn)

        then:
        !screens.getOpenedScreens().getActiveScreens().contains(dialog)
    }

    protected InputDialog showDialogWithResultHandler(Screen mainScreen, defaultString, goodInfoObject) {
        vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.parameter("string").withDefaultValue(defaultString),
                        InputParameter.entityParameter("entity", GoodInfoObject).withDefaultValue(goodInfoObject))
                .withActions(DialogActions.YES_NO, { result ->
                    switch (result.getCloseActionType()) {
                        case InputDialog.InputDialogResult.ActionType.YES:
                            assert result.getValue("string") == defaultString
                            assert result.getValue("entity") == goodInfoObject
                            assert result.getCloseAction() == InputDialog.INPUT_DIALOG_YES_ACTION
                            break
                        case InputDialog.InputDialogResult.ActionType.NO:
                            assert result.getCloseAction() == InputDialog.INPUT_DIALOG_NO_ACTION
                            break
                    }
                })
                .show()
    }

    def "custom input dialog actions"() {

        given:

        def mainScreen = showTestMainScreen()
        def dateValue = new Date()
        def stringValue = "Default value"

        when: "created custom action"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.parameter("string").withDefaultValue(stringValue),
                        InputParameter.dateParameter("date").withDefaultValue(dateValue))
                .withActions(
                        InputDialogAction.action("ok").withHandler({
                            InputDialogAction.InputDialogActionPerformed event ->
                                InputDialog dialog = event.getInputDialog()

                                assert dialog.getValue("string") == stringValue
                                assert dialog.getValue("date") == dateValue
                        }),
                        InputDialogAction.action("cancel").withHandler({
                            InputDialogAction.InputDialogActionPerformed event ->
                                event.getInputDialog().close(InputDialog.INPUT_DIALOG_CANCEL_ACTION)
                        }))
                .show()

        then:
        def actionsLayout = (HBoxLayout) dialog.getWindow().getComponentNN("actionsLayout")
        actionsLayout.getComponents().size() == 3

        def okBtn = (Button) actionsLayout.getComponent(1) // because 0 - spacer
        okBtn.getAction().actionPerform(okBtn)

        def cancelBtn = (Button) actionsLayout.getComponent(2)
        cancelBtn.getAction().actionPerform(cancelBtn)

        !screens.getOpenedScreens().getActiveScreens().contains(dialog)
    }

    def "open with close listener"() {

        def mainScreen = showTestMainScreen()

        when: "check closing with OK action"
        def dialog = (InputDialog) createDialogWithCloseListener(mainScreen)

        then:
        def okBtn = getButtonFromDialog(dialog, 1) // because 0 - spacer
        okBtn.getAction().actionPerform(okBtn)

        !screens.getOpenedScreens().getActiveScreens().contains(dialog)

        when: "check closing with CANCEL action"
        def cancelDialog = (InputDialog) createDialogWithCloseListener(mainScreen)

        then:
        def cancelBtn = getButtonFromDialog(cancelDialog, 2)
        cancelBtn.getAction().actionPerform(cancelBtn)

        !screens.getOpenedScreens().getActiveScreens().contains(dialog)
    }

    protected InputDialog createDialogWithCloseListener(Screen mainScreen) {

        def bigDecimalValue = 1234

        return vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.bigDecimalParameter("bigDecimal").withDefaultValue(bigDecimalValue),
                        InputParameter.booleanParameter("boolean").withDefaultValue(true))
                .withCloseListener({ event ->
                    if (event.getCloseAction() == InputDialog.INPUT_DIALOG_OK_ACTION) {
                        assert event.getValue("bigDecimal") == bigDecimalValue
                        assert event.getValue("boolean") == true
                    } else {
                        assert event.getCloseAction() == InputDialog.INPUT_DIALOG_CANCEL_ACTION
                    }
                })
                .show()
    }

    def "input parameter with custom field"() {

        given:

        def mainScreen = showTestMainScreen()
        def customValue = "default value"
        def dateTimeValue = new Date()

        when: "get value from custom field"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.dateTimeParameter("dateTime").withDefaultValue(dateTimeValue),
                        new InputParameter("custom")
                                .withField({
                                    TextField field = uiComponents.create(TextField)
                                    field.setValue(customValue)
                                    return field
                                }))
                .withCloseListener({ event ->
                    if (event.getCloseAction() == InputDialog.INPUT_DIALOG_OK_ACTION) {
                        assert event.getValue("dateTime") == dateTimeValue
                        assert event.getValue("custom") == customValue
                    }
                })
                .show()
        then:
        def okBtn = getButtonFromDialog(dialog, 1) // because 0 - spacer
        okBtn.getAction().actionPerform(okBtn)
    }

    def "field validation"() {
        def mainScreen = showTestMainScreen()
        def datatypeRegistry = applicationContext.getBean(DatatypeRegistry)

        when: "custom field has incorrect value"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.dateParameter("date"),
                        new InputParameter("custom")
                                .withField({
                                    TextField field = uiComponents.create(TextField)
                                    field.setValue("sda")
                                    field.setDatatype(datatypeRegistry.get(Integer))
                                    return field
                                }))
                .show()
        then:
        def okBtn = getButtonFromDialog(dialog, 1) // because 0 - spacer
        okBtn.getAction().actionPerform(okBtn)

        // shouldn't be closed
        screens.getOpenedScreens().getActiveScreens().contains(dialog)

        when: "field is required"
        InputDialog reqDialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(InputParameter.intParameter("int").withRequired(true))
                .show()

        then:
        def reqOkBtn = getButtonFromDialog(reqDialog, 1) // because 0 - spacer
        reqOkBtn.getAction().actionPerform(reqOkBtn)

        // shouldn't be closed
        screens.getOpenedScreens().getActiveScreens().contains(reqDialog)
    }

    def "validator and default DialogActions"() {

        def mainScreen = showTestMainScreen()

        when: "create dialog with default actions and custom validator"
        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.stringParameter("phoneField"),
                        InputParameter.stringParameter("addressField"))
                .withValidator({ context ->
                    def phone = (String) context.getValue("phoneField")
                    def address = (String) context.getValue("addressField")
                    if (Strings.isNullOrEmpty(phone) && Strings.isNullOrEmpty(address)) {
                        return ValidationErrors.of("Phone or Address should be filled")
                    }
                    return ValidationErrors.none()
                })
                .show()
        then:
        def okBtn = getButtonFromDialog(dialog, 1)
        okBtn.getAction().actionPerform(okBtn)

        // shouldn't be closed
        screens.getOpenedScreens().getActiveScreens().contains(dialog)
    }

    def "validator and validationRequired in InputDialogAction"() {

        def mainScreen = showTestMainScreen()

        when: "validator and validationRequired in InputDialogAction"
        def targetValue = 100100
        def date = new Date(targetValue)

        InputDialog dialogA = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(
                        InputParameter.stringParameter("descriptionField"),
                        InputParameter.dateParameter("expirationDate").withDefaultValue(date))
                .withActions(
                        InputDialogAction.action("okAction")
                                .withHandler({ event ->
                                    event.getInputDialog().close(FrameOwner.WINDOW_CLOSE_ACTION)
                                }),
                        InputDialogAction.action("cancelAction")
                                .withValidationRequired(false)
                                .withHandler({ event ->
                                    event.getInputDialog().close(FrameOwner.WINDOW_CLOSE_ACTION)
                                }))
                .withValidator({ values ->
                    def expDate = (Date) values.getValue("expirationDate")
                    if (expDate.getTime() < targetValue + 1) {
                        return ValidationErrors.of("Wrong expiration date")
                    }
                    return ValidationErrors.none()
                })
                .show()

        then:
        def okBtnA = getButtonFromDialog(dialogA, 1)
        okBtnA.getAction().actionPerform(okBtnA)
        // shouldn't be closed
        screens.getOpenedScreens().getActiveScreens().contains(dialogA)

        def cancelBtnA = getButtonFromDialog(dialogA, 2)
        cancelBtnA.getAction().actionPerform(cancelBtnA)

        // must be closed
        !screens.getOpenedScreens().getActiveScreens().contains(dialogA)
    }

    def "enum input parameter"() {

        def mainScreen = showTestMainScreen()

        when: "validator and validationRequired in InputDialogAction"

        InputDialog dialog = vaadinUi.dialogs.createInputDialog(mainScreen)
                .withParameters(InputParameter.enumParameter("enumField", Status)
                        .withDefaultValue(Status.OK))
                .withActions(DialogActions.OK_CANCEL, { result ->
                    switch (result.closeActionType) {
                        case InputDialog.InputDialogResult.ActionType.OK:
                            Status status = result.getValue("enumField")
                            assert status == Status.OK
                            break
                    }
                })
                .show()

        then:
        def okBtnA = getButtonFromDialog(dialog, 1)
        okBtnA.getAction().actionPerform(okBtnA)

        // must be closed
        !screens.getOpenedScreens().getActiveScreens().contains(dialog)
    }

    protected static Button getButtonFromDialog(InputDialog dialog, int index) {
        def actionsLayout = (OrderedContainer) dialog.getWindow().getComponentNN("actionsLayout")
        return (Button) actionsLayout.getComponent(index) // 0 - spacer
    }
}
