package de.biomedical_imaging.ij.ndef.ext;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

import java.awt.Color;
import java.awt.MenuItem;
import java.awt.Polygon;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;

import de.biomedical_imaging.ij.watershedellipse.Ellipse;
import de.biomedical_imaging.ij.watershedellipse.ImageResultsTableSelector;
import de.biomedical_imaging.ij.watershedellipse.ManyEllipses;
import de.biomedical_imaging.ij.watershedellipse.WatershedEllipse_;

public class ShowEllipsesAsOverlay_ implements PlugIn{
	ImagePlus binaryImg;
	ImagePlus targetImg;
	
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		if(WatershedEllipse_.getInstance()==null){
			throw new IllegalStateException("Ellipse fitting is not runnig!");
		}
		

		String[] openWindows = new String[WindowManager.getImageCount()];
		
		for(int i = 0; i < WindowManager.getImageCount(); i++){
			openWindows[i] = WindowManager.getImage(WindowManager.getIDList()[i]).getTitle();
		}
		
		GenericDialog gd = new GenericDialog("Show as overlay");
		gd.addChoice("Binary_image: ", openWindows, openWindows[0]);
		gd.addChoice("Target_image: ", openWindows, openWindows[1]);
		gd.showDialog();
		
		int binaryIndex = gd.getNextChoiceIndex();
		binaryImg = WindowManager.getImage(openWindows[binaryIndex]);
		
		int targetIndex = gd.getNextChoiceIndex();
		targetImg = WindowManager.getImage(openWindows[targetIndex]);
		WatershedEllipse_.getInstance().getResultsTableSelectionDrawer().setTargetImage(targetImg.getID()); 
		WatershedEllipse_.getInstance().getImageResultsTableSelector().setTargetImage(targetImg.getID()); 
		//targetImg.getWindow().getComponent(0).addKeyListener(new RestorOverlayListener(this));
		ImageCanvas ic = (ImageCanvas)targetImg.getWindow().getComponent(0); 
		ic.disablePopupMenu(true);
		ic.addMouseListener(new EllipseImagePopupListener(ic,targetImg,this));
		showOverlay();
	}
	
	public void showOverlay(){
		Overlay ov = targetImg.getOverlay();
		if(ov == null){
			ov = new Overlay();
			targetImg.setOverlay(ov);
		}
		ov.clear();
		
		ArrayList<ManyEllipses> allEllipses = WatershedEllipse_.getInstance().getAllEllipses();

		for(int i = 0; i < allEllipses.size(); i++){

			for (int j = 0; j < allEllipses.get(i).size(); j++) {
				Ellipse ell = allEllipses.get(i).get(j);
				
				Polygon p = new Polygon(ell.getPolygon().xpoints.clone(),ell.getPolygon().ypoints.clone(), ell.getPolygon().npoints);
				PolygonRoi pr = new PolygonRoi(p, Roi.TRACED_ROI);
				pr.setPosition(i+1);
				pr.setStrokeColor(Color.red);
				pr.setStrokeWidth(2);
				ov.add(pr);
			}
		}
		targetImg.repaintWindow();
	}

}

class EllipseImagePopupListener implements MouseListener {

	ImageCanvas ic;
	ShowEllipsesAsOverlay_ plugin;
	ImagePlus targetImp;
	boolean multipleEllipseSelection;
	ArrayList<Ellipse> selectedEllipses;
	
	public EllipseImagePopupListener(ImageCanvas ic,ImagePlus targetImp,ShowEllipsesAsOverlay_ plugin) {
		this.ic = ic;
		this.plugin = plugin;
		this.targetImp = targetImp;
		multipleEllipseSelection = false;
		selectedEllipses = new ArrayList<Ellipse>();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopupMenu(e.getX(), e.getY());
		}
		
	}
	
	public void showPopupMenu(int x, int y){
		PopupMenu popup = new PopupMenu("Particle Sizer Menu");
		
		MenuItem removeParticleItem = new MenuItem("Remove particle");
		removeParticleItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(multipleEllipseSelection && ImageResultsTableSelector.isEllipseSelected == false){
					
					ResultsTable rt = ResultsTable.getResultsTable();
					//Generate set of labels
					HashSet<Integer> labelSet = new HashSet<Integer>();
					for (Ellipse el : selectedEllipses) {
						labelSet.add(el.getLabel());
					}

					for(int j = 0; j < rt.getCounter(); j++){
						int frame = Integer.parseInt(rt.getStringValue(0, j));
						if(frame!=targetImp.getCurrentSlice()){
							continue;
						}
						int label = Integer.parseInt(rt.getStringValue(1, j));
						if(labelSet.contains(label)){
							ResultsTable.getResultsTable().deleteRow(j);
							j--;
						}

					}
					
					WatershedEllipse_.getInstance().getAllEllipses().get(targetImp.getCurrentSlice()-1).removeAll(selectedEllipses);
					Prefs.set("ndef.NumberOfParticles", ResultsTable.getResultsTable().getCounter());
					IJ.runMacro("updateResults();");
					IJ.run("Select None");
					multipleEllipseSelection =false;
					plugin.showOverlay();
				}
				else{
				
					int start = IJ.getTextPanel().getSelectionStart();
					int end = IJ.getTextPanel().getSelectionEnd();
					if(start!=-1){
	
						if(start == end && ImageResultsTableSelector.isEllipseSelected){
							
							int frame = Integer.parseInt(ResultsTable.getResultsTable().getStringValue(0, start));
							int label = Integer.parseInt(ResultsTable.getResultsTable().getStringValue(1, start));
							
							Ellipse ell = WatershedEllipse_.getInstance().getEllipseByFrameAndLabel(frame-1, label);
							WatershedEllipse_.getInstance().getAllEllipses().get(targetImp.getCurrentSlice()-1).remove(ell);
	
							ResultsTable.getResultsTable().deleteRow(start);
				
							Prefs.set("ndef.NumberOfParticles", ResultsTable.getResultsTable().getCounter());
							IJ.runMacro("updateResults();");
							ImageResultsTableSelector.isEllipseSelected = false;
							plugin.showOverlay();
						}
					}
				}
				
			}
		});
		
		removeParticleItem.setEnabled(ImageResultsTableSelector.isEllipseSelected || multipleEllipseSelection);
		
		MenuItem restorSelectionItem = new MenuItem("Restore particle selection");
		
		restorSelectionItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plugin.showOverlay();
				
			}
		});
		
		popup.add(removeParticleItem); 
		popup.add(restorSelectionItem); 
		
		
		ic.add(popup);

		popup.show(ic, x, y);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopupMenu(e.getX(), e.getY());
		}
		if(targetImp.getRoi()!=null){
			if(targetImp.getRoi().getType() == 0){
				Overlay ov = targetImp.getOverlay();
				if(ov==null){
					ov = new Overlay();
					targetImp.setOverlay(ov);
				}else{
					ov.clear();
				}
				Roi r = targetImp.getRoi();
				ManyEllipses el = WatershedEllipse_.getInstance().getAllEllipses().get(targetImp.getCurrentSlice()-1);
				
				for (Ellipse ellipse : el) {
					if(r.contains((int)ellipse.getX(), (int)ellipse.getY())){
						selectedEllipses.add(ellipse);
						multipleEllipseSelection = true;
						PolygonRoi pr = (PolygonRoi) ellipse.getRoi();
						pr.setStrokeWidth(2);
						pr.setPosition(targetImp.getCurrentSlice());
						ov.add(pr);
						targetImp.repaintWindow();
					}
				}
			}else{
				multipleEllipseSelection = false;
				selectedEllipses = new ArrayList<Ellipse>();
			}
		}
		else{
			multipleEllipseSelection = false;
			selectedEllipses = new ArrayList<Ellipse>();
		}
	}
	
}
