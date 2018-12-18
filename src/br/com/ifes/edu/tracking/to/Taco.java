package br.com.ifes.edu.tracking.to;

import org.opencv.core.Point;

public class Taco {
	
	public Taco(Point mao, Point ponta) {
		super();
		this.mao = mao;
		this.ponta = ponta;
	}
	
	public Point getMao() {
		return mao;
	}

	public Point getPonta() {
		return ponta;
	}
	
	public double getAngularCoefficient() {
		return (mao.y - ponta.y) / (mao.x - ponta.x);
	}
	
	public boolean insideCircle(Point center, double radius) {
		if(distance(ponta, center) < radius) {
			if(distance(mao, center) < radius) {
				return true;
			}
		}
		return false;
	}
	
	public double size() {
		return distance(mao, ponta);
	}
	
	public void swap() {
		Point aux = mao;
		mao = ponta;
		ponta = aux;
	}
	
	private double distance(Point a, Point b){
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
	
	Point mao;
	Point ponta;
}
