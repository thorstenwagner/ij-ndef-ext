package de.biomedical_imaging.ij.ndef.ext;

import java.awt.Window;
import java.awt.event.MouseListener;
import de.biomedical_imaging.ij.ellipsesplit.EllipseSplit_;
import de.biomedical_imaging.ij.ellipsesplit.ImageResultsTableSelector;
import de.biomedical_imaging.ij.ellipsesplit.ResultsTableSelectionDrawer;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class RegisterImageToEllipseFitting_ implements PlugIn{

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		if(EllipseSplit_.getInstance()==null){
			throw new IllegalStateException("Shape Filter is not runnig!");
		}
		String[] openWindows = new String[WindowManager.getImageCount()];
		for(int i = 0; i < WindowManager.getImageCount(); i++){
			openWindows[i] = WindowManager.getImage(WindowManager.getIDList()[i]).getTitle();
		}
		
		GenericDialog gd = new GenericDialog("Register Image to Shape Filter");
		gd.addChoice("Image: ", openWindows, openWindows[0]);
		gd.showDialog();
		
		int cIndex = gd.getNextChoiceIndex();
		String title = openWindows[cIndex];
		Window window = WindowManager.getWindow(title);
		ImagePlus image = WindowManager.getImage(title);
		boolean windowIsVisible = (window!=null);
		if(windowIsVisible){
			
			//Remove all shape fitler Listeners
			MouseListener[] listeners = window.getComponent(0).getMouseListeners();
			for (MouseListener mouseListener : listeners) {
				if(mouseListener instanceof ImageResultsTableSelector){
					window.getComponent(0).removeMouseListener(mouseListener);
				}
			}
			listeners=IJ.getTextPanel().getMouseListeners();
			for (MouseListener mouseListener : listeners) {
				if(mouseListener instanceof ResultsTableSelectionDrawer){
					IJ.getTextPanel().removeMouseListener(mouseListener);
				}
			}
			window.getComponent(0).addMouseListener(new ImageResultsTableSelector(image));
			IJ.getTextPanel().addMouseListener(new ResultsTableSelectionDrawer(image));
		}
		
		
		
	}

}
