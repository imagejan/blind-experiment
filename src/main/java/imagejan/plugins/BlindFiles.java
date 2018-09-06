package imagejan.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

import net.imagej.ImageJ;

/**
 * Blind Files Plugin Command
 * <p>
 * This plugin renames a list of files by random names (blinding),
 * keeping track of the name changes, thus allowing to revert
 * the file name changes (unblinding) as well as resolve a blinded
 * list of results.
 *
 * @author Jan Eglinger
 * @author Andrew Valente
 */

@Plugin(type = Command.class, menuPath = "Analyze>Blind Experiment>Blind Files")
public class BlindFiles implements Command {
	
	@Parameter(label="Choose a directory to blind.", style="directory")
	private File folderPath;
	
	@Parameter(label="File extension: ")
	private String fileExtension = "tif";
	
	@Parameter()
	private PrefService prefs;
	
	@Parameter()
	private LogService log;
	

	@Override
	public void run() {
		
		// Blind the files
		boolean success = blindFiles(folderPath, fileExtension);
		
		// Record if successful
		if (success) {
			log.info("Files were successfully blinded.");
		}
		else {
			log.error("Could not write properties file.");
		}
	}
	
	/**
	 * Blind all files in a directory with a given extension.
	 * @param directory The folder of files which are to be blinded.
	 * @param extension The file extension of the files to be blinded.
	 * @return A boolean indicating if blinding was successful.
	 */
	public boolean blindFiles(File directory, String extension) {
		File propFile = new File(directory, "blind_experiment.props");
		File[] list = directory.listFiles();
		Properties prop = new Properties();
		prop.setProperty("file extension", extension);
		
		for (File file : list) {
			if (file.getName().endsWith("." + extension)) {
				String id = UUID.randomUUID().toString();
				if (file.renameTo(new File(directory, id + "." + extension))) {
					prop.setProperty(id, file.getName());
				}
			}
		}
		
		// Try and save the properties file, abort if unsuccessful
		try {
			prop.store(new FileOutputStream(propFile), "Blinding information for the files in this folder. DO NOT DELETE THIS FILE");
			prefs.put("blindPropsPath", propFile.getCanonicalPath());
			return true;
		}
		catch (Exception e) {
			// TODO: Roll back file name changes if propFile is not saved.
			return false;
		}
		
	}
	
	/**
	 * A main method for testing purposes...
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String... args) throws Exception {
        // Create the ImageJ application context with all available services
    	// Context context = new Context();
    	final ImageJ ij = new ImageJ();
    	ij.launch(args);
    }
}
