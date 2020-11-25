package com.dividato.configuracionGeneral.utils;

import java.util.List;

public class TablaPaginada<T> {
	int tama�oPagina;
	int numeroPagina;
	int totalPaginas;
	List<T> registros;
	
	public TablaPaginada() {
		super();
	}
	
	public TablaPaginada(int tama�oPagina, int numeroPagina, int totalPaginas,
			List<T> registros) {
		super();
		this.tama�oPagina = tama�oPagina;
		this.numeroPagina = numeroPagina;
		this.totalPaginas = totalPaginas;
		this.registros = registros;
	}


	public int getTama�oPagina() {
		return tama�oPagina;
	}
	public void setTama�oPagina(int tama�oPagina) {
		this.tama�oPagina = tama�oPagina;
	}
	public int getNumeroPagina() {
		return numeroPagina;
	}
	public void setNumeroPagina(int numeroPagina) {
		this.numeroPagina = numeroPagina;
	}
	public int getTotalPaginas() {
		return totalPaginas;
	}
	public void setTotalPaginas(int totalPaginas) {
		this.totalPaginas = totalPaginas;
	}
	public List<T> getRegistros() {
		return registros;
	}
	public void setRegistros(List<T> registros) {
		this.registros = registros;
	}
	
}
