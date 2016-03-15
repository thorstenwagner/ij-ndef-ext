package de.biomedical_imaging.ij.ndef.ext;

import java.util.ArrayList;

import de.biomedical_imaging.ij.ellipsesplit.EllipseSplit_;
import de.biomedical_imaging.ij.ellipsesplit.ManyEllipses;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class EllipseContoursDataAsImage_ implements PlugIn{

	@Override
	public void run(String arg) {
		
		if(EllipseSplit_.getInstance()==null){
			IJ.error("Ellipse Filter did not run!");
			return;
		}
		ArrayList<ManyEllipses> allEllipses = EllipseSplit_.getInstance().getAllEllipses();
		
		int columns = 0;
		int rows = 0;
		for(int i = 0; i < allEllipses.size();i++){
			if(allEllipses.get(i).size()>columns){
				columns = allEllipses.get(i).size();
			}
			for(int j = 0; j < allEllipses.get(i).size(); j++){
				if(allEllipses.get(i).get(j).getPolygon().npoints > rows){
					rows = allEllipses.get(i).get(j).getPolygon().npoints + 1;
				}
			}
		}
		
		ImageStack stk = new ImageStack(columns,rows);
		for(int i = 0; i < allEllipses.size();i++){
			FloatProcessor xCoord = new FloatProcessor(columns,rows);
			xCoord.set(-2);
			FloatProcessor yCoord = new FloatProcessor(columns,rows);
			yCoord.set(-2);
			for(int j = 0; j < allEllipses.get(i).size(); j++){
				xCoord.putPixelValue(j, 0, allEllipses.get(i).get(j).getLabel());
				yCoord.putPixelValue(j, 0, allEllipses.get(i).get(j).getLabel());
				for(int k = 0 ; k < allEllipses.get(i).get(j).getPolygon().npoints; k++){
					xCoord.putPixelValue(j, k+1, allEllipses.get(i).get(j).getPolygon().xpoints[k]);
					yCoord.putPixelValue(j, k+1, allEllipses.get(i).get(j).getPolygon().ypoints[k]);
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
