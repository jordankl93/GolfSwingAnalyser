package br.com.ifes.edu.tracking.to;

import org.opencv.core.Point;

public class ResultadoDeteccao {
	
	private Point pontaMao;
	
	private Point pontoTaco;
	
	private Point pontaMaoAnterior;
	
	private Point pontaTacoAnterior;
	
	public ResultadoDeteccao() {
		
	}

	public ResultadoDeteccao(Point pontaMao, Point pontoTaco) {
		super();
		this.pontaMao = pontaMao;
		this.pontoTaco = pontoTaco;
	}
	
	public ResultadoDeteccao(Point pontaMao, Point pontoTaco, Point pontaMaoAnterior, Point pontaTacoAnterior) {
		super();
		this.pontaMao = pontaMao;
		this.pontoTaco = pontoTaco;
		this.pontaMaoAnterior = pontaMaoAnterior;
		this.pontaTacoAnterior = pontaTacoAnterior;
	}

	public Point getPontaMao() {
		return pontaMao;
	}

	public void setPontaMao(Point pontaMao) {
		this.pontaMao = pontaMao;
	}

	public Point getPontoTaco() {
		return pontoTaco;
	}

	public void setPontoTaco(Point pontoTaco) {
		this.pontoTaco = pontoTaco;
	}

	public Point getPontaMaoAnterior() {
		return pontaMaoAnterior;
	}

	public void setPontaMaoAnterior(Point pontaMaoAnterior) {
		this.pontaMaoAnterior = pontaMaoAnterior;
	}

	public Point getPontaTacoAnterior() {
		return pontaTacoAnterior;
	}

	public void setPontaTacoAnterior(Point pontaTacoAnterior) {
		this.pontaTacoAnterior = pontaTacoAnterior;
	}
	

}
