package io.jmix.datatoolsflowui.view.datadiagram;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.jmix.datatools.datamodel.DataModelSupport;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@Route(value = "datatl/data-model-diagram", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelDiagramView")
@ViewDescriptor(path = "data-model-diagram-view.xml")
public class DataModelDiagramView extends StandardView {

    @Autowired
    protected DataModelSupport dataModelSupport;

    @ViewComponent
    private JmixImage<Object> diagramImage;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        generateDiagram();
    }

    public void generateDiagram() {
        // hack to passing filtered entities list to this view that open in a browser tab
        byte[] rawResult = dataModelSupport.filteredModelsCount() == dataModelSupport.getDataModelProvider().getModelsCount()
                ? dataModelSupport.generateDiagram()
                : dataModelSupport.generateFilteredDiagram();

        DownloadHandler downloadHandler = DownloadHandler.fromInputStream(e ->
                new DownloadResponse(
                        new ByteArrayInputStream(rawResult),
                        "data-model-er-diagram.png",
                        null,
                        rawResult.length));

        diagramImage.setSrc(downloadHandler);
    }
}