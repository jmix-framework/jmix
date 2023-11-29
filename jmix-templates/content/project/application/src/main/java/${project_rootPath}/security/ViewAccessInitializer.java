package ${project_rootPackage}.security;

import io.jmix.flowui.FlowuiViewIndex;
import io.jmix.simplesecurityflowui.access.ViewAccessManager;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Class is responsible for granting access to views from included add-ons.
 */
@Component
public class ViewAccessInitializer {

    private final ViewAccessManager viewAccessManager;

    public ViewAccessInitializer(ViewAccessManager viewAccessManager) {
        this.viewAccessManager = viewAccessManager;
    }

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        viewAccessManager.grantAccess("USER", FlowuiViewIndex.editFilterGroup.viewIds());
    }
}
