import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.gephi.project.api.Workspace;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.Container.Factory;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;

import com.itextpdf.text.PageSize;


public class demo {
	public static void main(String[] args) {
		//Init a project - and therefore a workspace
		ProjectController pc = (ProjectController) Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		
		//Generate a new random graph into a container
		Container container = ((Factory) Lookup.getDefault().lookup(Container.Factory.class)).newContainer();
		RandomGraph randomGraph = new RandomGraph();
		randomGraph.setNumberOfNodes(100);
		randomGraph.setWiringProbability(0.01);
		randomGraph.generate(container.getLoader());

		//Append container to graph structure
		ImportController importController = (ImportController) Lookup.getDefault().lookup(ImportController.class);
		importController.process(container, new DefaultProcessor(), workspace);
		
		//Create graph model
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
				
		// Create a table so can record which nodes have adopted
        Table nodeTable = graphModel.getNodeTable();
        nodeTable.addColumn("adopted", Boolean.class);
        
        //Get a UndirectedGraph now 
		UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
		
		double p = 0.1; // p is fixed external influence
		
		/*Preview is the last step before export and allows display customization and aesthetics refinements. 
			PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();//Get a graph model - it exists because we have a workspace
			PreviewProperties prop = model.getProperties();*/
				
		// Randomly choose proportion of nodes to adopt based on p
		for (Node n : undirectedGraph.getNodes()) {
			if (Math.random() < p) { 
				n.setAttribute("adopted", true);
				System.out.println(n+" has adopted");
				//prop.putValue(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(Color.cyan));
			} else {
				n.setAttribute("adopted", false);
			}
		}
		
		//Export full graph to gephi format
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("demo.gexf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
	
		//Simple PDF export
		ExportController ec1 = Lookup.getDefault().lookup(ExportController.class);
		try {
		   ec1.exportFile(new File("demo.pdf"));
		} catch (IOException ex) {
		   ex.printStackTrace();
		   return;
		}

		//PDF Exporter config and export to Byte array
		PDFExporter pdfExporter = (PDFExporter) ec1.getExporter("pdf");
		pdfExporter.setPageSize(PageSize.A0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ec1.exportStream(baos, pdfExporter);
	}
}