package br.com.ifes.edu.tracking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import br.com.ifes.edu.tracking.to.ResultadoDeteccao;
import br.com.ifes.edu.tracking.to.Reta;
import br.com.ifes.edu.tracking.to.Taco;


public class ClubExtraction {
	// O que é isto?
	public static final int SENSITIVITY_VALUE = 15;
	public static final double PI = 3.14;
	public static final double DISTANCIA_MAX = 240.0;
	public static final double TOLERANCIA_MAX = 1.5;
	
	// Por que estes valores???
	private int erodeSize = 1, dilateSize = 3;
	
	private Mat difference1, difference2;
	private Mat threshold1, threshold2;
	private Mat mask;

	public ClubExtraction() {
		difference1 = new Mat();
		difference2 = new Mat();
		threshold1 = new Mat();
		threshold2 = new Mat();
	}
	
	public void morphOps(Mat thresh) {
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodeSize, erodeSize));
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateSize, dilateSize));
		
		Imgproc.dilate(thresh, thresh, dilateElement);
		Imgproc.erode(thresh, thresh, erodeElement);

		Imgproc.dilate(thresh, thresh, dilateElement);
		Imgproc.erode(thresh, thresh, erodeElement);		
	}
	
	public Taco trackMotion(Mat frame1, Mat frame2, Mat frame3, Mat imageToDraw, Taco taco0, Taco resultadoAnterior) {
		difference1 = new Mat();
		difference2 = new Mat();
		threshold1  = new Mat();
		threshold2  = new Mat();
		mask = new Mat();
		
		Mat frame1gray = new Mat();
		Mat frame2gray = new Mat();
		Mat frame3gray = new Mat();
		
		
		Imgproc.cvtColor(frame1, frame1gray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(frame2, frame2gray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(frame3, frame3gray, Imgproc.COLOR_BGR2GRAY);
		
		
		/* Calcula a diferença absoluta por elemento entre duas matrizes para os frames anterior e atual */
		Core.absdiff(frame2gray, frame1gray, difference1);
		
		Imgproc.threshold(difference1, threshold1, SENSITIVITY_VALUE, 255, Imgproc.THRESH_BINARY);
		
		//morphOps(threshold1);		
		Imgproc.morphologyEx(threshold1, threshold1, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
		Imgproc.morphologyEx(threshold1, threshold1, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
		
		// Mostra imagem na tela
		//GolfTrackerRunner.updateImage(threshold1);
		
		
		
		/*Para frames atual e posterior */		
		Core.absdiff(frame2gray, frame3gray, difference2);
		
		Imgproc.threshold(difference2, threshold2, SENSITIVITY_VALUE, 255, Imgproc.THRESH_BINARY);
				 
		//morphOps(threshold2);		
		Imgproc.morphologyEx(threshold2, threshold2, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
		Imgproc.morphologyEx(threshold2, threshold2, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
		
		// Mostra imagem na tela
		//GolfTrackerRunner.updateImage(threshold2);		
		
		
		
		/* a) e b) Operação Logica comparando as duas máscaras obtidas */
		Core.bitwise_and(threshold1, threshold2, frame2gray, mask);
		
		//GolfTrackerRunner.updateImage(frame2gray);
		
		/* c) Aplicando o método Canny*/
		
		Imgproc.blur(frame2gray, frame2gray, new Size(3, 3));
		
		// detect the edges
		Mat edges = new Mat();
		int lowThreshold = 10;
		int ratio = 3;
		Imgproc.Canny(frame2gray, edges, lowThreshold, lowThreshold * ratio);
		
		//GolfTrackerRunner.updateImage(edges);
		
		/* d) Detectando os Segmentos*/		
		
		return detectaPontaDoTaco(edges, frame2, imageToDraw, taco0, resultadoAnterior);
		
	}
	
	private Taco detectaPontaDoTaco(Mat arestas, Mat frame, Mat imageToDraw, Taco taco0, Taco anterior) {
				
		
//		rho = 1  # distance resolution in pixels of the Hough grid
//		theta = np.pi / 180  # angular resolution in radians of the Hough grid
//		threshold = 15  # minimum number of votes (intersections in Hough grid cell)
//		min_line_length = 50  # minimum number of pixels making up a line
//		max_line_gap = 20  # maximum gap in pixels between connectable line segments
//		line_image = np.copy(img) * 0  # creating a blank to draw lines on
				
		/* DEFININDO AS VARIAVEIS */
		double tamanhoDoTaco;
		Double tamanhoMinReta;
		List<Taco> possiveis = new ArrayList<Taco>();
		
		// Draw the lines
		double[] linha = null;
		
		
		/* PEGANDO TODAS AS RETAS ENCONTRADAS NO FRAME ATUAL */
		Mat linesP = new Mat();
		
		/* Calculando tamanho do taco pela distancia Euclidiana*/
		tamanhoDoTaco = taco0.size();
		
		tamanhoMinReta = tamanhoDoTaco / 2;
		Imgproc.HoughLinesP(arestas, linesP, 1, Math.PI / 180, tamanhoMinReta.intValue(), tamanhoMinReta, 10);
		
		for (int x = 0; x < linesP.rows(); x++) {
			linha = linesP.get(x, 0);
			Taco t = new Taco(new Point(linha[0], linha[1]), new Point(linha[2], linha[3]));
			possiveis.add(t);
			Imgproc.line(imageToDraw, new Point(linha[0], linha[1]), new Point(linha[2], linha[3]), new Scalar(0, 255, 255), 2);
		}
		
		Imgproc.circle(imageToDraw, taco0.getMao(), (int)(tamanhoDoTaco * 2.5), new Scalar(0, 255, 0), 3);
		List<Taco> insideCircle = new ArrayList<Taco>();
		for( int i = 0; i < possiveis.size(); i++ ) {
			if( possiveis.get(i).insideCircle(taco0.getMao(), tamanhoDoTaco * 2.5) ) {
				System.out.println(possiveis.get(i).getAngularCoefficient());
				insideCircle.add(possiveis.get(i));
				Imgproc.line(imageToDraw, possiveis.get(i).getMao(), possiveis.get(i).getPonta(), new Scalar(0, 0, 255), 2);
			}
		}
		
		List<Taco> paralelas = new ArrayList<Taco>();
		for( int i = 0; i < insideCircle.size(); i++) {
			double ca1 = insideCircle.get(i).getAngularCoefficient();
			for( int j = 0; j < insideCircle.size(); j++ ) {
				if( i != j ) {
					double ca2 = insideCircle.get(j).getAngularCoefficient();
					if( Math.abs(ca1 - ca2) < 0.5) {
						Taco mergedTaco = merge(insideCircle.get(i), insideCircle.get(j));
						paralelas.add(mergedTaco);
						
						Imgproc.line(imageToDraw, mergedTaco.getMao(), mergedTaco.getPonta(), new Scalar(0, 255, 0), 2);
					}
				}
			}
		}
		
		return maisPerto(paralelas, anterior);
		//return mesmaDirecao(paralelas, anterior);
		
	}
	
	private Taco maisPerto(List<Taco> candidatos, Taco anterior) {
		Taco escolhido = null;
		double menorDist = 1000000;
		for( int i = 0; i < candidatos.size(); i++) {
			double dist1 = euclideanDistance(anterior.getMao(), candidatos.get(i).getMao());
			double dist2 = euclideanDistance(anterior.getMao(), candidatos.get(i).getPonta());
			
			if( dist1 < dist2) {
				if(dist1 < menorDist) {
					escolhido = candidatos.get(i);
					menorDist = dist1;
				}
			}
			else {
				candidatos.get(i).swap();
				if(dist2 < menorDist) {
					escolhido = candidatos.get(i);
					menorDist = dist2;
				}
			}
		}
		return escolhido;
	}
	
	private Taco mesmaDirecao(List<Taco> candidatos, Taco anterior) {
		double ac = anterior.getAngularCoefficient();
		double pac = - 1 / ac;
		Taco escolhido = null;
		double menorDiff = 1000000;
		Point ponta = anterior.getPonta();
		for( int i = 0; i < candidatos.size(); i++) {
			
			double ac1 = (candidatos.get(i).getPonta().y - ponta.y) / (candidatos.get(i).getPonta().x - ponta.x); 
			double diff1 = Math.abs(ac1 - pac);
			double ac2 = (candidatos.get(i).getMao().y - ponta.y) / (candidatos.get(i).getMao().x - ponta.x);
			double diff2 = Math.abs(ac2 - pac);
			
			if( diff1 < menorDiff) {
				escolhido = candidatos.get(i);
				menorDiff = diff1;
			}
			if( diff2 < menorDiff) {
				candidatos.get(i).swap();
				escolhido = candidatos.get(i);
				menorDiff = diff2;
			}
		}
		return escolhido;
		
	}
	
	private Taco merge(Taco t1, Taco t2) {
		List<Taco> tacos = new ArrayList<Taco>();
		tacos.add(t1);
		tacos.add(t2);
		tacos.add(new Taco(t1.getMao(), t2.getMao()));
		tacos.add(new Taco(t1.getMao(), t2.getPonta()));
		tacos.add(new Taco(t1.getPonta(), t2.getMao()));
		tacos.add(new Taco(t1.getPonta(), t2.getPonta()));
		double maxSize = 0;
		Taco m = t1;
		for(int i = 0; i < tacos.size(); i++) {
			if(tacos.get(i).size() > maxSize) {
				m = tacos.get(i);
				maxSize = tacos.get(i).size();
			}
		}
		return m;
	}
	
	private double menorDistancia(Point ponta, Point a, Point b) {
		double distanciaA, distanciaB;
		
		distanciaA = euclideanDistance(ponta, a);
		distanciaB = euclideanDistance(ponta, b);
		
		if(distanciaA <= distanciaB)
			return distanciaA;
		else
			return distanciaB;
		
	}
	
	public static double euclideanDistance(Point a, Point b){
	    double distance = 0.0;
	    
	    try{
	        if(a != null && b != null){
	            double xDiff = a.x - b.x;
	            double yDiff = a.y - b.y;
	            distance = Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff, 2));
	        }
	    }catch(Exception e){
	        System.err.println("Something went wrong in euclideanDistance function "+e.getMessage());
	    }
	    return Math.abs(distance);
	}
}
