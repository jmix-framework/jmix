package io.jmix.flowui.component;

import javax.annotation.Nullable;
import java.time.ZoneId;

public interface HasZoneId {

    @Nullable
    ZoneId getZoneId();

    void setZoneId(@Nullable ZoneId zoneId);
}
