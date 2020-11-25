/*
 * Copyright 2011 Dividato. All rights reserved
 *
 * Copyright Version 1.0
 *
 * Create on 21/06/2011
 */
package com.security.accesoDatos.configuraciongeneral.interfaz;

import java.util.List;

import com.security.accesoDatos.interfaz.GeneralServiceInterface;
import com.security.modelo.configuraciongeneral.TipoVariacion;

/**
 * @author Ezequiel Beccaria
 *
 */
public interface TipoVariacionService extends GeneralServiceInterface<TipoVariacion>{
	public boolean save(TipoVariacion variacion);
	public boolean update(TipoVariacion variacion);
	public boolean delete(TipoVariacion variacion);
	public List<TipoVariacion> listarVariaciones(String descripcion);
}
