package io.jmix.flowui;

import com.vaadin.flow.component.notification.Notification;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.flowui.component")
@ConstructorBinding
public class FlowUiComponentProperties {

    String gridCreateShortcut;
    //String gridAddShortcut;
    String gridRemoveShortcut;
    String gridEditShortcut;
    //String gridViewShortcut;

    String defaultNotificationPosition;
    int defaultNotificationDuration;

//    String pickerShortcutModifiers;
    String pickerLookupShortcut;
    String pickerOpenShortcut;
    String pickerClearShortcut;

    public FlowUiComponentProperties(
            @DefaultValue("CONTROL-BACKSLASH") String gridCreateShortcut,
            /*String gridAddShortcut,*/
            @DefaultValue("CONTROL-DELETE") String gridRemoveShortcut,
            @DefaultValue("ENTER") String gridEditShortcut,
            /*String gridViewShortcut,*/
            @DefaultValue("MIDDLE") String defaultNotificationPosition,
            @DefaultValue("3000") int defaultNotificationDuration,
            /*@DefaultValue("CONTROL-ALT") String pickerShortcutModifiers,*/
            // TODO: gg, Think another shortcuts. These clash with browser
            @DefaultValue("CONTROL-ALT-L") String pickerLookupShortcut,
            @DefaultValue("CONTROL-ALT-O") String pickerOpenShortcut,
            @DefaultValue("CONTROL-ALT-C") String pickerClearShortcut) {
        this.gridCreateShortcut = gridCreateShortcut;
        /*this.gridAddShortcut = gridAddShortcut;*/
        this.gridRemoveShortcut = gridRemoveShortcut;
        this.gridEditShortcut = gridEditShortcut;
        /*this.gridViewShortcut = gridViewShortcut;*/
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

/*    public String getGridAddShortcut() {
        return gridAddShortcut;
    }*/

    public String getGridRemoveShortcut() {
        return gridRemoveShortcut;
    }

    public String getGridEditShortcut() {
        return gridEditShortcut;
    }

/*    public String getGridViewShortcut() {
        return gridViewShortcut;
    }*/

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
