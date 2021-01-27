package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_ReportEditLocales.fragment")
@UiDescriptor("locales.xml")
public class LocalesFragment extends ScreenFragment {

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected Messages messages;

    @Install(to = "localeTextField", subject = "contextHelpIconClickHandler")
    protected void localeTextFieldContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage(getClass(), "localeText"))
                .withMessage(messages.getMessage(getClass(), "report.localeTextHelp"))
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth("600px")
                .show();
    }
}
