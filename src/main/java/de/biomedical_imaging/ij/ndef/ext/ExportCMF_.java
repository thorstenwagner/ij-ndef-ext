package de.biomedical_imaging.ij.ndef.ext;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class ExportCMF_ implements PlugIn{
	ResultsTable rt;
	
	public void exportCMF(){
		ResultsTable rt = ResultsTable.getResultsTable();
		String[] rtHeadings = rt.getHeadings();
		String[] headings = new String[rtHeadings.length-1];
		int k = 0;
		for(int i = 1; i < rtHeadings.length;i++){
			headings[k] = rtHeadings[i];
			k++;
		}
		
		GenericDialog gd = new GenericDialog("Export CMF");
		gd.addChoice("Data_column", headings, headings[0]);
		gd.showDialog();
		double[] data = null;
		if (!gd.wasCanceled()) {
			int column = rt.getColumnIndex(gd.getNextChoice());
			data = rt.getColumnAsDoubles(column);
		}
		else{
			return;
		}
		
		JFileChooser fileChooser = new JFileChooser();
		String path = "";
		if (fileChooser.showSaveDialog(IJ.getInstance()) == JFileChooser.APPROVE_OPTION) {
		  path = fileChooser.getSelectedFile().getAbsolutePath();
		 
		}
		
		EmpiricalDistribution distribution = null;
		boolean redo=false;
		int BIN_COUNT = (int) Math.ceil(Math.log(data.length)/Math.log(2)+1) ;
		do {
			 //Sturges formula * 2
			redo=false;
			
			distribution = new EmpiricalDistribution(BIN_COUNT);
			distribution.load(data);
			
			
			PrintWriter out = null;
			try {
				out = new PrintWriter(path);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String LINE_SEP = "\n";
			if(IJ.isWindows()){
				LINE_SEP = "\r\n";
			}
			
			String cmfstr = "size \t q0";
			out.println(cmfstr);
			java.util.Arrays.sort(data);
			for(int i = 0; i < data.length; i++){
				try{
					out.println(data[i] + "\t" + distribution.cumulativeProbability(data[i]));
				}
				catch(NotStrictlyPositiveException e){
					BIN_COUNT--;
					redo = true;
					break;
				}
				
			}
			out.close();
		}while(redo);
		
	}

	@Override
	public void run(String arg) {
		Menu Results = ResultsTable.getResultsWindow().getMenuBar().getMenu(3);
		if(Results.getItem(Results.getItemCount()-1).getLabel().equals("Export CMF")==false){
			MenuItem exportCMF = new MenuItem("Export CMF");
			exportCMF.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					exportCMF();
					
				}
			});
			Results.add(exportCMF);
		}
	}
}
