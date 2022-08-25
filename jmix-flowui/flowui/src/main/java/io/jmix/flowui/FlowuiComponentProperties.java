package io.jmix.flowui;

import com.vaadin.flow.component.notification.Notification;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.flowui.component")
@ConstructorBinding
public class FlowuiComponentProperties {

    String gridCreateShortcut;
    // TODO: gg, implement
    String gridAddShortcut;
    String gridRemoveShortcut;
    String gridEditShortcut;
    String gridReadShortcut;

    String defaultNotificationPosition;
    int defaultNotificationDuration;

//    String pickerShortcutModifiers;
    String pickerLookupShortcut;
    String pickerOpenShortcut;
    String pickerClearShortcut;

    public FlowuiComponentProperties(
            @DefaultValue("CONTROL-BACKSLASH") String gridCreateShortcut,
            @DefaultValue("CONTROL-ALT-BACKSLASH") String gridAddShortcut,
            @DefaultValue("CONTROL-DELETE") String gridRemoveShortcut,
            @DefaultValue("ENTER") String gridEditShortcut,
            @DefaultValue("ENTER") String gridReadShortcut,
            @DefaultValue("MIDDLE") String defaultNotificationPosition,
            @DefaultValue("3000") int defaultNotificationDuration,
            /*@DefaultValue("CONTROL-ALT") String pickerShortcutModifiers,*/
            // TODO: gg, Think another shortcuts. These clash with web browser
            @DefaultValue("CONTROL-ALT-L") String pickerLookupShortcut,
            @DefaultValue("CONTROL-ALT-O") String pickerOpenShortcut,
            @DefaultValue("CONTROL-ALT-C") String pickerClearShortcut) {
        this.gridCreateShortcut = gridCreateShortcut;
        this.gridAddShortcut = gridAddShortcut;
        this.gridRemoveShortcut = gridRemoveShortcut;
        this.gridEditShortcut = gridEditShortcut;
        this.gridReadShortcut = gridReadShortcut;
        this.defaultNotificationPosition = defaultNotificationPosition;
        this.defaultNotificationDuration = defaultNotificationDuration;

//        this.pickerShortcutModifiers = pickerShortcutModifiers;
        this.pickerLookupShortcut = pickerLookupShortcut;
        this.pickerOpenShortcut = pickerOpenShortcut;
        this.pickerClearShortcut = pickerClearShortcut;
    }

    public String getGridCreateShortcut() {
        return gridCreateShortcut;
    }

    public String getGridAddShortcut() {
        return gridAddShortcut;
    }

    public String getGridRemoveShortcut() {
        return gridRemoveShortcut;
    }

    public String getGridEditShortcut() {
        return gridEditShortcut;
    }

    public String getGridReadShortcut() {
        return gridReadShortcut;
    }

    public Notification.Position getDefaultNotificationPosition() {
        return Notification.Position.valueOf(defaultNotificationPosition);
    }

    public int getDefaultNotificationDuration() {
        return defaultNotificationDuration;
    }

    /*public String getPickerShortcutModifiers() {
        return pickerShortcutModifiers;
    }*/

    public String getPickerLookupShortcut() {
        return pickerLookupShortcut;
    }

    public String getPickerOpenShortcut() {
        return pickerOpenShortcut;
    }

    public String getPickerClearShortcut() {
        return pickerClearShortcut;
    }
}
