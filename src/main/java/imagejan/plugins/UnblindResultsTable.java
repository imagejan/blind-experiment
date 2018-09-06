package imagejan.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

import ij.IJ;
import ij.measure.ResultsTable;

/**
 * Blind Files Plugin Command
 * <p>
 * This plugin unblinds a ResultsTable containing masked filenames.
 *
 * @author Jan Eglinger
 * @author Andrew Valente
 */

@Plugin(type = Command.class, menuPath = "Analyze>Blind Experiment>Unblind Results Table")
public class UnblindResultsTable implements Command {
	
	@Parameter()
	ResultsTable rt;
	
	@Parameter()
	PrefService prefs;
	
	@Parameter()
	LogService log;
	
	@Override
	public void run() {
		// Unblind the ResultsTable
		File propsFile = new File(prefs.get("blindPropsPath"));
		boolean success = unblindResultsTable(propsFile, rt);
		
		// Record if successful
		if (success) {
			log.info("ResultsTable was successfully unblinded.");
		}
		else {
			log.error("Could not unblind the ResultsTable.");
		}
	}
	
	/**
	 * Unblind the labels of a ResultsTable.
	 * @param propertiesFile The saved props file containing the blinded file information.
	 * @param rt The ResultsTable with labels to unblind.
	 * @return A boolean indication of if the operation was successful.
	 */
	public boolean unblindResultsTable(File propertiesFile, ResultsTable rt){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propertiesFile));
		} catch (Exception e) {
			return false;
		}
		for (int i=0; i<rt.getCounter(); i++) {
			String label = rt.getLabel(i);
			if (label != null) {
				String newLabel = prop.getProperty(FilenameUtils.removeExtension(label));
				if (newLabel != null) {
					rt.setLabel(newLabel, i);
				}
			}
		}
		rt.show("Results");
		return true;
	}
}
