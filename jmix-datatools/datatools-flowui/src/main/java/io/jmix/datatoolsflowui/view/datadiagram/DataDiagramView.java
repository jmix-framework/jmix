package io.jmix.datatoolsflowui.view.datadiagram;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.jmix.datatools.datamodel.DataModelManager;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@Route(value = "datatl/data-diagram", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataDiagramView")
@ViewDescriptor(path = "data-diagram-view.xml")
public class DataDiagramView extends StandardView {
    @Autowired
    protected DataModelManager dataModelManager;

    @ViewComponent
    private JmixImage<Object> diagramImage;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        generateDiagram();
    }

    public void generateDiagram() {
        // hack to passing filtered entities list to this view that open in a neighboring tab
        byte[] rawResult = dataModelManager.filteredModelsCount() == dataModelManager.getDataModelHolder().modelsCount()
                ? dataModelManager.generateDiagram()
                : dataModelManager.generateFilteredDiagram();

        DownloadHandler downloadHandler = DownloadHandler.fromInputStream(e ->
                new DownloadResponse(
                        new ByteArrayInputStream(rawResult),
                        "data-model-er-diagram.png",
                        null,
                        rawResult.length));

        diagramImage.setSrc(downloadHandler);
    }
}