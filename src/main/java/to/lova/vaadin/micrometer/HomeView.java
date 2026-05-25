package to.lova.vaadin.micrometer;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
class HomeView extends VerticalLayout {

    HomeView() {
        add(new H1("Hello, Vaadin!"));
    }
}
