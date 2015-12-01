package de.biomedical_imaging.ij.ndef.ext;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class ResultsTableToImage_ implements PlugIn {

	@Override
	public void run(String arg) {
		ResultsTable rt = ResultsTable.getResultsTable();
		if(rt == null){
			return;
		}
		
		int nRows = rt.getCounter();
		int nCols = rt.getLastColumn()+1;
		
		FloatProcessor fp = new FloatProcessor(nCols, nRows);
		for(int x = 0; x < nCols; x++){
			for(int y = 0; y < nRows; y++){
				fp.putPixelValue(x, y, rt.getValueAsDouble(x, y));
			}
		}
		
		ImagePlus imp = new ImagePlus("Results Tabel as Image", fp);
		imp.show();
		ij.Prefs.set("ndef.result.rtAsImageID", imp.getID());
	}

}
