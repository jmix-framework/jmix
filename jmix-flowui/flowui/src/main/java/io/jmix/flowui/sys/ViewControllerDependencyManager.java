package io.jmix.flowui.sys;

import io.jmix.flowui.view.View;
import io.jmix.flowui.sys.ControllerDependencyInjector.InjectionContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component("flowui_ViewControllerDependencyManager")
public class ViewControllerDependencyManager {

    protected ViewControllerDependencyInjector controllerDependencyInjector;
    protected List<ControllerDependencyInjector> dependencyInjectors;

    public ViewControllerDependencyManager(ViewControllerDependencyInjector controllerDependencyInjector,
                                           @Nullable List<ControllerDependencyInjector> dependencyInjectors) {
        this.controllerDependencyInjector = controllerDependencyInjector;
        this.dependencyInjectors = dependencyInjectors;
    }

    public void inject(View controller) {
        controllerDependencyInjector.inject(controller);

        if (CollectionUtils.isNotEmpty(dependencyInjectors)) {
            InjectionContext injectionContext = new InjectionContext(controller);
            for (ControllerDependencyInjector injector : dependencyInjectors) {
                injector.inject(injectionContext);
            }
        }
    }
}
