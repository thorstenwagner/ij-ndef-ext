package de.biomedical_imaging.ij.ndef.ext;


import java.awt.event.MouseListener;

import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class AgglomerateManager_ implements PlugIn {

	public static AgglomerateManager_ instance;
	public boolean aggloHasBlackbackground;
	
	public AgglomerateManager_() {
		// TODO Auto-generated constructor stub
		instance = this;
	}
	
	public void run(String arg) {
		
		if(Shape_Filter.getInstance()==null){
			throw new IllegalStateException("Shape Filter is not runnig!");
		}
		String[] openWindows = new String[WindowManager.getImageCount()];
		
		for(int i = 0; i < WindowManager.getImageCount(); i++){
			openWindows[i] = WindowManager.getImage(WindowManager.getIDList()[i]).getTitle();
		}
		
		GenericDialog gd = new GenericDialog("Register Image to Shape Filter");
		gd.addChoice("Agglomerated Image: ", openWindows, openWindows[0]);
		gd.addChoice("Deagglomerated Image: ", openWindows, openWindows[0]);
		gd.addCheckbox("Agglomerated_Black_Background", false);
		gd.showDialog();
		
		int aggloimageIndex = gd.getNextChoiceIndex();
		ImagePlus aggloImp = WindowManager.getImage(openWindows[aggloimageIndex]).duplicate();
		WindowManager.getImage(openWindows[aggloimageIndex]).close();
		int deaggloimageIndex = gd.getNextChoiceIndex();
		ImagePlus deaggloImp = WindowManager.getImage(openWindows[deaggloimageIndex]);
		aggloHasBlackbackground = gd.getNextBoolean();
		if(aggloImp.getNSlices() != deaggloImp.getNSlices()){
			throw new IllegalArgumentException("Agglomerated and deagglomerated image doesnt have the same number of slices!");
		}
	
		boolean isVisible = (aggloImp.getWindow()!=null);
		if(isVisible){
			aggloImp.getWindow().setVisible(false);
		}
		
		// Need to clean up previous listeners
		// Clean up old listeners
		
		MouseListener[] mls = (MouseListener[])(deaggloImp.getWindow().getComponent(0).getListeners(MouseListener.class));
		for(int i = 0; i < mls.length; i++) {
			if(mls[i] instanceof MouseInAgglomerateListener) {
				deaggloImp.getWindow().getComponent(0).removeMouseListener(mls[i]);
			}
	
		}
	
		deaggloImp.getWindow().getComponent(0).addMouseListener(new MouseInAgglomerateListener(deaggloImp, aggloImp));
	}

}
