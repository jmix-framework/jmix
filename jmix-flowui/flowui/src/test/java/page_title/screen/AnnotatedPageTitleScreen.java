package page_title.screen;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.screen.StandardScreen;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

@Route("annotated-page-title-screen")
@PageTitle("annotatedPageTitleScreen.annotatedTitle") // or "msg://annotatedPageTitleScreen.annotatedTitle"
@UiController
@UiDescriptor("annotated-page-title-screen.xml")
public class AnnotatedPageTitleScreen extends StandardScreen {
}
