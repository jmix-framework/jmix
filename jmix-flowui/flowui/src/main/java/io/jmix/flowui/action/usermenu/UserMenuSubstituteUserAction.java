package io.jmix.flowui.action.usermenu;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.core.usersubstitution.event.UserSubstitutionsChangedEvent;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.usermenu.UserMenu;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.WindowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.function.Consumer;

@ActionType(UserMenuSubstituteUserAction.ID)
public class UserMenuSubstituteUserAction extends UserMenuAction<UserMenuSubstituteUserAction, UserMenu>
        implements ApplicationListener<UserSubstitutionsChangedEvent>, ViewOpeningAction {

    private static final Logger log = LoggerFactory.getLogger(UserMenuSubstituteUserAction.class);

    public static final String ID = "userMenu_substituteUser";
    public static final String DEFAULT_VIEW = "substituteUserView";

    protected ApplicationContext applicationContext;
    protected DialogWindows dialogWindows;
    protected UserSubstitutionManager substitutionManager;
    protected CurrentUserSubstitution currentUserSubstitution;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();

    protected boolean hasSubstitutedUsers;

    protected Registration attachRegistration;
    protected Registration detachRegistration;

    public UserMenuSubstituteUserAction() {
        this(ID);
    }

    public UserMenuSubstituteUserAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.EXCHANGE);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.userMenu.SubstituteUser");
    }

    @Autowired
    public void setCurrentUserSubstitution(CurrentUserSubstitution currentUserSubstitution) {
        this.currentUserSubstitution = currentUserSubstitution;
    }

    @Autowired
    public void setSubstitutionManager(UserSubstitutionManager substitutionManager) {
        this.substitutionManager = substitutionManager;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        // User substitute view opens in a dialog window only
        return OpenMode.DIALOG;
    }

    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        log.warn("{} doesn't support setting {}", ID, OpenMode.class.getSimpleName());
    }

    @Nullable
    @Override
    public String getViewId() {
        return viewInitializer.getViewId();
    }

    @Override
    public void setViewId(@Nullable String viewId) {
        viewInitializer.setViewId(viewId);
    }

    @Nullable
    @Override
    public Class<? extends View> getViewClass() {
        return viewInitializer.getViewClass();
    }

    @Override
    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        viewInitializer.setViewClass(viewClass);
    }

    @Nullable
    @Override
    public RouteParametersProvider getRouteParametersProvider() {
        // User substitute view opens in a dialog window only
        return null;
    }

    @Override
    public void setRouteParametersProvider(@Nullable RouteParametersProvider routeParameters) {
        log.warn("{} doesn't support setting {}", ID, RouteParametersProvider.class.getSimpleName());
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        // User substitute view opens in a dialog window only
        return null;
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider queryParameters) {
        log.warn("{} doesn't support setting {}", ID, QueryParametersProvider.class.getSimpleName());
    }

    @Nullable
    @Override
    public <V extends View<?>> Consumer<DialogWindow.AfterCloseEvent<V>> getAfterCloseHandler() {
        return viewInitializer.getAfterCloseHandler();
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Nullable
    @Override
    public <V extends View<?>> Consumer<V> getViewConfigurer() {
        return viewInitializer.getViewConfigurer();
    }

    @Override
    public <V extends View<?>> void setViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        viewInitializer.setViewConfigurer(viewConfigurer);
    }

    @Override
    protected void setTargetInternal(@Nullable UserMenu target) {
        super.setTargetInternal(target);

        unbindListeners();

        if (target != null) {
            bindListeners(target);
        }
    }

    protected void unbindListeners() {
        if (attachRegistration != null) {
            attachRegistration.remove();
            attachRegistration = null;
        }

        if (detachRegistration != null) {
            detachRegistration.remove();
            detachRegistration = null;
        }
    }

    protected void bindListeners(UserMenu target) {
        attachRegistration = target.addAttachListener(this::onTargetAttach);
        detachRegistration = target.addDetachListener(this::onTargetDetach);
    }

    protected void onTargetAttach(AttachEvent attachEvent) {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            context.addApplicationListener(this);
        }
    }

    protected void onTargetDetach(DetachEvent detachEvent) {
        if (applicationContext instanceof ConfigurableApplicationContext context) {
            context.removeApplicationListener(this);
        }
    }

    @Override
    public void refreshState() {
        hasSubstitutedUsers = hasSubstitutedUsers();
        super.refreshState();
    }

    protected boolean hasSubstitutedUsers() {
        return target != null
                && menuItem != null
                && !substitutionManager.getCurrentSubstitutedUsers().isEmpty();
    }

    @Override
    protected void setVisibleInternal(boolean visible) {
        super.setVisibleInternal(visible && hasSubstitutedUsers);
    }

    @Override
    public void execute() {
        checkTarget();

        View<?> origin = UiComponentUtils.getView(target);
        WindowBuilder<View<?>> builder = dialogWindows.view(origin, DEFAULT_VIEW);

        builder = viewInitializer.initWindowBuilder(builder);
        builder.open();
    }

    @Override
    public void onApplicationEvent(UserSubstitutionsChangedEvent event) {
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (Objects.equals(authenticatedUser.getUsername(), event.getSource())) {
            refreshState();
        }
    }
}
