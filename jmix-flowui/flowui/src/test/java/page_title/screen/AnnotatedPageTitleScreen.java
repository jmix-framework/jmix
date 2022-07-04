package page_title.screen;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;

@Route("annotated-page-title-screen")
@PageTitle("annotatedPageTitleScreen.annotatedTitle") // or "msg://annotatedPageTitleScreen.annotatedTitle"
@UiController
@UiDescriptor("annotated-page-title-screen.xml")
public class AnnotatedPageTitleScreen extends StandardView {
}
