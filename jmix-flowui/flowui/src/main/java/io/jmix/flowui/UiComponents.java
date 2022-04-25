package io.jmix.flowui;

import com.vaadin.flow.component.Component;
import org.springframework.core.ParameterizedTypeReference;

public interface UiComponents {

    <T extends Component> T create(Class<T> type);

    <T extends Component> T create(ParameterizedTypeReference<T> type);
}
