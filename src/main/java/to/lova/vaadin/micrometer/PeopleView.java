package to.lova.vaadin.micrometer;

import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

@Route("people")
class PeopleView extends VerticalLayout {

    private static final String INITIAL_HTML = "<span>no observations yet</span>";

    private final MeterRegistry registry;
    private final ValueSignal<String> fetchStats = new ValueSignal<>(INITIAL_HTML);
    private final ValueSignal<String> countStats = new ValueSignal<>(INITIAL_HTML);

    PeopleView(PersonService service, MeterRegistry registry, ObservationRegistry observations) {
        this.registry = registry;

        var slowToggle = new Checkbox("Simulate slow fetches");
        slowToggle.setValue(service.isSlow());
        slowToggle.addValueChangeListener(e -> service.setSlow(e.getValue()));

        var breakBtn = new Button("Trigger error", e -> triggerError(service, observations));
        breakBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        var stats = new HorizontalLayout(
                metric("fetch", fetchStats),
                metric("count", countStats),
                slowToggle,
                breakBtn);
        stats.setAlignItems(Alignment.CENTER);

        var grid = new Grid<>(Person.class, false);
        grid.addColumn(Person::id).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Person::firstName).setHeader("First name");
        grid.addColumn(Person::lastName).setHeader("Last name");
        grid.addColumn(Person::email).setHeader("Email").setFlexGrow(1);
        grid.setItems(
                q -> {
                    var page = service.fetch(q.getOffset(), q.getLimit());
                    fetchStats.set(format("person.fetch"));
                    return page;
                },
                _ -> {
                    var size = service.count();
                    countStats.set(format("person.count"));
                    return size;
                });
        grid.setSizeFull();

        setSizeFull();
        add(new H2("People"), stats, grid);
    }

    private void triggerError(PersonService service, ObservationRegistry observations) {
        try {
            Observation.createNotStarted("vaadin.click", observations)
                    .contextualName("click trigger-error")
                    .lowCardinalityKeyValue("component", "trigger-error")
                    .observe(service::breakSomething);
        } catch (RuntimeException ex) {
            var n = Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private HorizontalLayout metric(String label, ValueSignal<String> signal) {
        var badge = new Badge(label);
        badge.addThemeVariants(BadgeVariant.CONTRAST, BadgeVariant.FILLED);
        var row = new HorizontalLayout(badge, new Html(signal));
        row.setSpacing(false);
        row.addClassNames(Gap.SMALL);
        row.setAlignItems(Alignment.CENTER);
        return row;
    }

    private String format(String name) {
        var timer = registry.find(name).timer();
        if (timer == null) {
            return INITIAL_HTML;
        }
        var html = """
            <span><b>%,d</b> calls
            <span style="color:var(--lumo-tertiary-text-color)">·</span>
            avg <b>%.2f</b> ms</span>
        """;
        return html.formatted(timer.count(), timer.mean(TimeUnit.MILLISECONDS));
    }
}
