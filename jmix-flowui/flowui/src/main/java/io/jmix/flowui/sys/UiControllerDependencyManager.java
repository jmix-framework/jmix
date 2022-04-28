package io.jmix.flowui.sys;

import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.sys.ControllerDependencyInjector.InjectionContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component("flowui_UiControllerDependencyManager")
public class UiControllerDependencyManager {

    protected UiControllerDependencyInjector controllerDependencyInjector;
    protected List<ControllerDependencyInjector> dependencyInjectors;

    public UiControllerDependencyManager(UiControllerDependencyInjector controllerDependencyInjector,
                                         @Nullable List<ControllerDependencyInjector> dependencyInjectors) {
        this.controllerDependencyInjector = controllerDependencyInjector;
        this.dependencyInjectors = dependencyInjectors;
    }

    public void inject(Screen controller) {
        controllerDependencyInjector.inject(controller);

        if (CollectionUtils.isNotEmpty(dependencyInjectors)) {
            InjectionContext injectionContext = new InjectionContext(controller);
            for (ControllerDependencyInjector injector : dependencyInjectors) {
                injector.inject(injectionContext);
            }
        }
    }
}
