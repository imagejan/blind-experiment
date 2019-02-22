package sc.fiji.blind_experiment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

/**
 * Blind Files Plugin Command
 * <p>
 * This plugin unblinds a folder of files that have been previously blinded.
 *
 * @author Jan Eglinger
 * @author Andrew Valente
 */

@Plugin(type = Command.class, menuPath = "Analyze>Blind Experiment>Unblind Files")
public class UnblindFiles implements Command {
	
	@Parameter()
	PrefService prefs;
	
	@Parameter()
	LogService log;
	
	@Override
	public void run() {
		// Unblind the files using the saved props file
		File propsFile = new File(prefs.get("blindPropsPath"));
		boolean success = unblindFiles(propsFile);
		
		// Record if successful
		if (success) {
			log.info("Files were successfully unblinded.");
		}
		else {
			log.error("Could not unblind the files.");
		}
	}
	
	/**
	 * Unblind previously blinded files using the generated props file.
	 * @param propertiesFile The props file generated from blinding the folder of files.
	 * @return A boolean indicating if the operation was successful.
	 */
	public boolean unblindFiles(File propertiesFile) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propertiesFile));
		} catch (Exception e) {
			return false;
		}
		String ext = prop.getProperty("file extension");
		File dir = propertiesFile.getParentFile();
		File [] list = dir.listFiles();
		for (File file : list) {
			String origName = prop.getProperty(FilenameUtils.removeExtension(FilenameUtils.getName(file.getPath())));
			if (origName != null) {
				if (!file.renameTo(new File(dir, FilenameUtils.removeExtension(origName) + "." + ext))) {
					return false;
				}
			}
		}
		return true;
	}
}
