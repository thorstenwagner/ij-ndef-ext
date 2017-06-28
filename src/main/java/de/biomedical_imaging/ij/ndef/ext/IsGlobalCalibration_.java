package de.biomedical_imaging.ij.ndef.ext;

import ij.IJ;
import ij.plugin.PlugIn;

public class IsGlobalCalibration_ implements PlugIn {

	@Override
	public void run(String arg) {
		if(IJ.getImage().getGlobalCalibration()==null){
			ij.Prefs.set("ndef.cal.global", false);
		}else{
			ij.Prefs.set("ndef.cal.global", true);
		}
		
	}
}
