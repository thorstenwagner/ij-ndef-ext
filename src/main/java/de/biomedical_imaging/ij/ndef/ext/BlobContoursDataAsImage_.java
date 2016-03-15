package de.biomedical_imaging.ij.ndef.ext;

import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.blob.ManyBlobs;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class BlobContoursDataAsImage_ implements PlugIn{

	@Override
	public void run(String arg) {

		if(Shape_Filter.getInstance()==null){
			IJ.error("Shape filter did not run!");
			return;
		}
		ManyBlobs[] allBlobs = Shape_Filter.getInstance().getAllBlobs();
		
		int columns = 0;
		int rows = 0;
		for(int i = 0; i < allBlobs.length;i++){
			if(allBlobs[i].size()>columns){
				columns = allBlobs[i].size();
			}
			for(int j = 0; j < allBlobs[i].size(); j++){
				if(allBlobs[i].get(j).getOuterContour().npoints > rows){
					rows = allBlobs[i].get(j).getOuterContour().npoints + 1;
				}
			}
		}

		ImageStack stk = new ImageStack(columns,rows);
		for(int i = 0; i < allBlobs.length;i++){
			FloatProcessor xCoord = new FloatProcessor(columns,rows);
			xCoord.set(-2);
			FloatProcessor yCoord = new FloatProcessor(columns,rows);
			yCoord.set(-2);
			for(int j = 0; j < allBlobs[i].size(); j++){
				xCoord.putPixelValue(j, 0, allBlobs[i].get(j).getLabel());
				yCoord.putPixelValue(j, 0, allBlobs[i].get(j).getLabel());
				for(int k = 0 ; k < allBlobs[i].get(j).getOuterContour().npoints; k++){
					xCoord.putPixelValue(j, k+1, allBlobs[i].get(j).getOuterContour().xpoints[k]);
					yCoord.putPixelValue(j, k+1, allBlobs[i].get(j).getOuterContour().ypoints[k]);
				}
			}
			stk.addSlice(xCoord);
			stk.addSlice(yCoord);
		}

		ImagePlus imp = new ImagePlus("Contour Coordinates as Image", stk);
		imp.show();
	
		ij.Prefs.set("ndef.result.contourImgID", imp.getID());
		
	}

}
