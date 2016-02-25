package core;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class StatistiquesGui extends JFrame{
	private PoleEmploi poleEmploi;
	
	private JTextField individusField, tauxChomageField, 
	                   emploisPercusField, emploisNonPourvusField, emploisEnvoyesField,
	                   individusQualif1Field, individusQualif2Field, individusQualif3Field,
	                   revenuMinMoyenField, tempsLibreMinMoyenField, revenuMoyenMoyenField, tempsLibreMoyenMoyenField;
	
	private XYSeriesCollection tauxDataset = new XYSeriesCollection();
	private XYSeriesCollection revenuDataset = new XYSeriesCollection();
	
	private XYSeries tauxChomageSeries = new XYSeries("Taux de Chômage");
	private XYSeries proportionNiv1Series = new XYSeries("proportionNiv1");
	private XYSeries proportionNiv2Series = new XYSeries("proportionNiv2");
	private XYSeries proportionNiv3Series = new XYSeries("proportionNiv3");
	
	private XYSeries revenuMinMoyenSeries = new XYSeries("Revenu Min Moyen");
	
	StatistiquesGui(PoleEmploi p){
		super(p.getLocalName());
		
		poleEmploi = p;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(12, 2));
		
		// Statistiques Labels
		panel.add(new JLabel("Individus dans le système:"));
		individusField = new JTextField(10);
		panel.add(individusField);
		
		panel.add(new JLabel("Taux de chômage dans le système:"));
		tauxChomageField = new JTextField(10);
		panel.add(tauxChomageField);
		
		panel.add(new JLabel("Nombre d'emplois perçus par Pôle Emploi en tout:"));
		emploisPercusField = new JTextField(10);
		panel.add(emploisPercusField);
		
		panel.add(new JLabel("NombreEmploisNonPourvus dans le système:"));
		emploisNonPourvusField = new JTextField(10);
		panel.add(emploisNonPourvusField);
		
		panel.add(new JLabel("NombreEmploisEnvoyes dans le système:"));
		emploisEnvoyesField = new JTextField(10);
		panel.add(emploisEnvoyesField);
		
		panel.add(new JLabel("Individus de niveau 1:"));
		individusQualif1Field = new JTextField(10);
		panel.add(individusQualif1Field);
		
		panel.add(new JLabel("Individus de niveau 2:"));
		individusQualif2Field = new JTextField(10);
		panel.add(individusQualif2Field);
		
		panel.add(new JLabel("Individus de niveau 3:"));
		individusQualif3Field = new JTextField(10);
		panel.add(individusQualif3Field);
		
		panel.add(new JLabel("Revenu Min Moyen:"));
		revenuMinMoyenField = new JTextField(10);
		panel.add(revenuMinMoyenField);
		
		panel.add(new JLabel("Temps Libre Min Moyen:"));
		tempsLibreMinMoyenField = new JTextField(10);
		panel.add(tempsLibreMinMoyenField);
		
		panel.add(new JLabel("Revenu Moyen Moyen:"));
		revenuMoyenMoyenField = new JTextField(10);
		panel.add(revenuMoyenMoyenField);
		
		panel.add(new JLabel("Temps libre Moyen Moyen:"));
		tempsLibreMoyenMoyenField = new JTextField(10);
		panel.add(tempsLibreMoyenMoyenField);
		
		getContentPane().add(panel, BorderLayout.CENTER);	
		
		// Graphe TauxChomage + proportionNiveauIndividu
		tauxDataset.addSeries(tauxChomageSeries);
		tauxDataset.addSeries(proportionNiv1Series);
		tauxDataset.addSeries(proportionNiv2Series);
		tauxDataset.addSeries(proportionNiv3Series);
		JFreeChart tauxLineChart = ChartFactory.createXYLineChart(
				"Taux de chômage dans le système",
				"Mois","Taux",
				tauxDataset,
				PlotOrientation.VERTICAL,
				true,true,false);
		ChartPanel tauxChartPanel = new ChartPanel( tauxLineChart );
		tauxChartPanel.setPreferredSize( new java.awt.Dimension( 1120 , 350 ) );
		XYPlot plot = tauxLineChart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for ( int i = 0; i < tauxDataset.getSeriesCount(); i++ ){
			renderer.setSeriesStroke( i , new BasicStroke( 1.5f ) );
		}
		plot.setRenderer( renderer ); 
		
		getContentPane().add(tauxChartPanel, BorderLayout.SOUTH);
		
		// Graphe Revenu Min Moyen
		revenuDataset.addSeries(revenuMinMoyenSeries);
		JFreeChart revenuLineChart = ChartFactory.createXYLineChart(
				"Revenu Min Moyen dans le système",
				"Mois","Taux",
				revenuDataset,
				PlotOrientation.VERTICAL,
				true,true,false);
		ChartPanel revenuChartPanel = new ChartPanel( revenuLineChart );
		revenuChartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		
		getContentPane().add(revenuChartPanel, BorderLayout.EAST);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
	
	public void updateData(int toursOut, int toursOutLim,
			               int individus, double tauxChomage, int employes, 
			               int nombreEmploisNonPourvus, int nombreEmploisEnvoyes,
			               int individusQualif1, int individusQualif2, int individusQualif3,
			               int revenuMinMoyen, int tempsLibreMinMoyen, 
			               int revenuMoyenMoyen, int tempsLibreMoyenMoyen) {
		
		individusField.setText(""+individus);
		tauxChomageField.setText(""+tauxChomage);
		emploisPercusField.setText(employes+nombreEmploisNonPourvus+"");
		emploisNonPourvusField.setText(""+nombreEmploisNonPourvus);
		emploisEnvoyesField.setText(""+nombreEmploisEnvoyes);
		individusQualif1Field.setText(""+individusQualif1);
		individusQualif2Field.setText(""+individusQualif2);
		individusQualif3Field.setText(""+individusQualif3);
		revenuMinMoyenField.setText(""+revenuMinMoyen);
		tempsLibreMinMoyenField.setText(""+tempsLibreMinMoyen);
		revenuMoyenMoyenField.setText(""+revenuMoyenMoyen);
		tempsLibreMoyenMoyenField.setText(""+tempsLibreMoyenMoyen);
		
		if ( toursOut >= (toursOutLim - 1) ) {
			for ( int i = 0; i < tauxDataset.getSeriesCount(); i++ ){
				tauxDataset.getSeries(i).clear();
			}
			revenuMinMoyenSeries.clear();
		}
		else {
			tauxChomageSeries.add(toursOut, tauxChomage);
			proportionNiv1Series.add(toursOut, (double) individusQualif1/individus);
			proportionNiv2Series.add(toursOut, (double) individusQualif2/individus);
			proportionNiv3Series.add(toursOut, (double) individusQualif3/individus);
			
			revenuMinMoyenSeries.add(toursOut, revenuMinMoyen);
		}
	}
}
