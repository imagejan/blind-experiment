package imagejan.plugins;

/*
 *  Licensed under the terms of the GPL (http://www.gnu.org/licenses/gpl.html)
 */

import fiji.util.gui.GenericDialogPlus; // requires sc.fiji:fiji-lib
import org.apache.commons.io.FilenameUtils; // requires commons-io:commons-io

import ij.IJ;

import ij.measure.ResultsTable;

import ij.plugin.PlugIn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.Properties;
import java.util.UUID;

/**
 * This plugin renames a list of files by random names (blinding),
 * keeping track of the name changes, thus allowing to revert
 * the file name changes (unblinding) as well as resolve a blinded
 * list of results.
 *
 * @author Jan Eglinger
 */
public class Blind_Experiment implements PlugIn {
	/**
	 * This method gets called by ImageJ / Fiji.
	 *
	 * @param arg can be specified in plugins.config
	 *   - "blind_files"
	 *   - "unblind_files"
	 *   - "unblind_results"
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		//IJ.log(arg);
		if (arg.equals("blind_files")) {
			GenericDialogPlus gd = new GenericDialogPlus("Mask filenames for blinded analysis");
			gd.addDirectoryField("Folder (files will be renamed)", "");
			gd.addStringField("File extension", "tif"); // everything except "props", "xml" ?
			gd.showDialog();
			if (gd.wasCanceled()) return;
			String dirpath = gd.getNextString();
			String ext = gd.getNextString();
			File dir = new File(dirpath);
			File propFile = new File(dir, "blind_experiment.props"); // check if exists already => abort
			File[] list = dir.listFiles();
			Properties prop = new Properties();
			prop.setProperty("file extension", ext);
			for (File file : list) {
				if (file.getName().endsWith("." + ext)) {
					String id = UUID.randomUUID().toString();
					if (file.renameTo(new File(dirpath, id + "." + ext))) {
						prop.setProperty(id, file.getName());
						//IJ.log(id + ":" + prop.getProperty(id));
					}
				}
			}
			try {
				prop.store(new FileOutputStream(propFile), "Blinding information for the files in this folder. DO NOT DELETE THIS FILE");
			} catch (Exception e) {
				IJ.error("Could not write properties file.");
				// rollback ?
			}
		}
		else if (arg.equals("unblind_files")) {
			String propFilePath = IJ.getFilePath("Please choose saved name association file");
			if (propFilePath == null) return;
			File propFile = new File(propFilePath);
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(propFile));
			} catch (Exception e) {
				return;
			}
			String ext = prop.getProperty("file extension");
			File dir = propFile.getParentFile();
			File [] list = dir.listFiles();
			boolean success = true;
			for (File file : list) {
				IJ.log(file.getPath());
				String origName = prop.getProperty(FilenameUtils.removeExtension(FilenameUtils.getName(file.getPath())));
				IJ.log(origName);
				if (origName != null) {
					if (!file.renameTo(new File(dir, FilenameUtils.removeExtension(origName) + "." + ext))) {
						success = false;
					}
				}
			}
			if (success) {
				// delete prop file
				// propFile.delete();
			}
		}
		else if (arg.equals("unblind_results")) {
			String propFilePath = IJ.getFilePath("Please choose saved name association file");
			if (propFilePath == null) return;
			File propFile = new File(propFilePath);
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(propFile));
			} catch (Exception e) {
				return;
			}
			ResultsTable rt = ResultsTable.getResultsTable();
			if (rt == null) {
				IJ.error("There's no results table open.");
				return;
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
		}
		else {
			IJ.error("Please run this plugin with an argument (specified in plugins.config).");
		}
	}
}
