package de.biomedical_imaging.ij.ndef.ext;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import ij.ImagePlus;
import ij.WindowManager;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.PolygonFiller;

public class BlobRemover_ implements PlugInFilter {
	int tMean,tMedian;
	ImagePlus imp;
	ImagePlus greyscaleImage;
	ArrayList<Integer> rowsToDelte;	
	@Override
	public int setup(String arg, ImagePlus imp) {
		if(arg!="final"){
			//Init
			this.imp = imp;
			rowsToDelte = new ArrayList<Integer>();	
			
			//Show GUI
			String[] openWindows = new String[WindowManager.getImageCount()];
			
			for(int i = 0; i < WindowManager.getImageCount(); i++){
				openWindows[i] = WindowManager.getImage(WindowManager.getIDList()[i]).getTitle();
			}
			
			GenericDialog gd = new GenericDialog("Greyscale Blob Remover");
			gd.addChoice("Greyscale Image", openWindows, openWindows[0]);
			gd.addSlider("Mean", 0, 255, 0);
			gd.addSlider("Median", 0, 255, 0);
			gd.showDialog();
			
			//Get Values
			
			int greyscaleIndex = gd.getNextChoiceIndex();
			greyscaleImage = WindowManager.getImage(openWindows[greyscaleIndex]);
			tMean = (int)gd.getNextNumber();
			tMedian = (int)gd.getNextNumber();
			//shapeFilterBlobs= Shape_Filter.getInstance().getAllBlobs();
		}else{
		//	ResultsTable rt = ResultsTable.getResultsTable();
		//	for (Integer i : rowsToDelte) {
		//		rt.deleteRow(i);	
		//	}
		}
		return DOES_8G + DOES_STACKS + FINAL_PROCESSING;
	}

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		/*
		ResultsTable rt = ResultsTable.getResultsTable();
		if(rt==null){
			IJ.error("No Results Table");
		}
		int cMean = rt.getColumnIndex("Grey-Mean");
		int cMedian = rt.getColumnIndex("Grey-Median");
		
		for(int i = 0; i < rt.getCounter(); i++){
			int frame = Integer.parseInt(rt.getStringValue(0, i));
			if(ip.getSliceNumber() != frame){
				continue;
			}
			double vMean = Double.parseDouble(rt.getStringValue(cMean, i));
			double vMedian = Double.parseDouble(rt.getStringValue(cMedian, i));
				
			if(vMean < tMean || vMedian < tMedian){
				int bloblabel = Integer.parseInt(rt.getStringValue(1, i));	
				Blob b = shapeFilterBlobs[frame-1].getBlobByLabel(bloblabel);
				ip.setColor(Color.black);
				PolygonRoi proi = new PolygonRoi(b.getOuterContour(), PolygonRoi.POLYGON);
				Rectangle r = proi.getBounds();
				PolygonFiller pf = new PolygonFiller();
				pf.setPolygon(proi.getXCoordinates(), proi.getYCoordinates(), proi.getNCoordinates());
				ip.setRoi(r);
				ImageProcessor objectMask = pf.getMask(r.width, r.height);
				ip.fill(objectMask);
			
				ip.drawPolygon(b.getOuterContour());
				rowsToDelte.add(i);
			}			
		}
		
		rt.show("Results");	
		*/
		ManyBlobs mb = new ManyBlobs(new ImagePlus("help", ip));
		mb.setBackground(0);
		mb.findConnectedComponents();
		for(int i = 0; i < mb.size(); i++){
			double[] meanAndMeadian = getMeanAndMedian(mb.get(i), greyscaleImage.getStack().getProcessor(ip.getSliceNumber()));
			
			if(meanAndMeadian[0] < tMean || meanAndMeadian[1] < tMedian){
				ip.setColor(Color.black);
				PolygonRoi proi = new PolygonRoi(mb.get(i).getOuterContour(), PolygonRoi.POLYGON);
				Rectangle r = proi.getBounds();
				PolygonFiller pf = new PolygonFiller();
				pf.setPolygon(proi.getXCoordinates(), proi.getYCoordinates(), proi.getNCoordinates());
				ip.setRoi(r);
				ImageProcessor objectMask = pf.getMask(r.width, r.height);
				ip.fill(objectMask);
			
				ip.drawPolygon(mb.get(i).getOuterContour());
			//	rowsToDelte.add(i);
			}			
		}
	}
	
	private double[] getMeanAndMedian(Blob b, ImageProcessor greyscale){
		greyscale.setRoi(b.getOuterContour());
		int flags = ImageStatistics.MEDIAN+ImageStatistics.MEAN + ImageStatistics.SKEWNESS+ ImageStatistics.KURTOSIS +  ImageStatistics.STD_DEV;
		
		ImageStatistics is = ImageStatistics.getStatistics(greyscale, flags, null);
		double[] result = {is.mean,is.median};
		return result;

	}

}
