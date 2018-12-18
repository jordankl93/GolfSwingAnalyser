package br.com.ifes.edu.tracking.to;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;

public class Reta {

	private Point pontoA;
	
	private Point pontoB;
	
	private List<Reta> retasParalelas;
	
	public Reta() {
		super();
	}
	
	public Reta(Point pontoA, Point pontoB) {
		super();
		this.pontoA = pontoA;
		this.pontoB = pontoB;
	}
		
	public Reta(Point pontoA, Point pontoB, List<Reta> retasParalelas) {
		super();
		this.pontoA = pontoA;
		this.pontoB = pontoB;
		this.retasParalelas = retasParalelas;
	}

	public Point getPontoA() {
		return pontoA;
	}

	public void setPontoA(Point pontoA) {
		this.pontoA = pontoA;
	}

	public Point getPontoB() {
		return pontoB;
	}

	public void setPontoB(Point pontoB) {
		this.pontoB = pontoB;
	}

	public List<Reta> getRetasParalelas() {
		if(retasParalelas == null)
			retasParalelas = new ArrayList<>();
		return retasParalelas;
	}

	public void setRetasParalelas(List<Reta> retasParalelas) {
		this.retasParalelas = retasParalelas;
	}

	@Override
	public String toString() {
		return "Ponto A: [" + this.pontoA + "]; Ponto B: [" + this.pontoB + "]";
	}

	@Override
	public boolean equals(Object obj) {
		double TOLERANCIA_MAX = 0.5;
		double diferencaCoef = 0;
		
		// Calculo do coeficiente => m0 = y1 - y0 / x1 - x0 
		double coeficienteAng1 = (this.pontoB.y - this.pontoA.y) / (this.pontoB.x - this.pontoA.x);
		double coeficienteAng2;
		Reta reta2 = null;
		
		if(obj instanceof Reta)
			reta2 = (Reta)obj;
		else
			return false;
		
		coeficienteAng2 = (reta2.pontoB.y - reta2.pontoA.y) / (reta2.pontoB.x - reta2.pontoA.x);
		
		diferencaCoef = Math.abs(coeficienteAng2 - coeficienteAng1);
				
		if(diferencaCoef <= TOLERANCIA_MAX)
			// Vai entrar a distancia minima também
			return false;
		else
			return true;
	}

	public int hashCode() {
		return 1;
	}

}
