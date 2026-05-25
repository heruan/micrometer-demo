package to.lova.vaadin.micrometer;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

@Component
class TracingVaadinInitListener implements VaadinServiceInitListener {

    private final ObservationRegistry registry;

    TracingVaadinInitListener(ObservationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInit -> uiInit.getUI().addBeforeEnterListener(this::observeNavigation));
    }

    private void observeNavigation(BeforeEnterEvent event) {
        var path = event.getLocation().getPath();
        Observation.createNotStarted("vaadin.navigate", registry)
                .contextualName("navigate " + path)
                .lowCardinalityKeyValue("route", path.isEmpty() ? "/" : path)
                .lowCardinalityKeyValue("ui.id", String.valueOf(event.getUI().getUIId()))
                .observe(() -> {});
    }
}
