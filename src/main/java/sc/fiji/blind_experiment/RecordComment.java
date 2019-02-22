package sc.fiji.blind_experiment;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Blind Files Plugin Command
 * <p>
 * This plugin adds a user defined comment to a ResultsTable.
 *
 * @author Andrew Valente
 */

@Plugin(type = Command.class, menuPath = "Analyze>Blind Experiment>Record Comment")
public class RecordComment implements Command {
	
	@Parameter(label="Comment: ")
	String comment = "";
	
	@Parameter()
	ImagePlus imp;
	
	@Parameter()
	ResultsTable rt;
	
	@Override
	public void run() {
		rt.incrementCounter();
		rt.addLabel(imp.getTitle());
		rt.addValue("Comment", comment);
		rt.show("Results");
	}

}
