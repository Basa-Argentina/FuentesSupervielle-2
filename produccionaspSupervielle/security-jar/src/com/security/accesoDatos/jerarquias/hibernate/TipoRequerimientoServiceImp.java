/*
 * Copyright 2011 Dividato. All rights reserved
 *
 * Copyright Version 1.0
 *
 * Create on 11/07/2011
 */
package com.security.accesoDatos.jerarquias.hibernate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.security.accesoDatos.hibernate.GestorHibernate;
import com.security.accesoDatos.hibernate.HibernateControl;
import com.security.accesoDatos.jerarquias.interfaz.TipoRequerimientoService;
import com.security.modelo.administracion.ClienteAsp;
import com.security.modelo.jerarquias.TipoRequerimiento;

/**
 * @author Ezequiel Beccaria
 *
 */
@Component
public class TipoRequerimientoServiceImp extends GestorHibernate<TipoRequerimiento> implements TipoRequerimientoService{
	private static Logger logger=Logger.getLogger(TipoRequerimientoServiceImp.class);
	
	@Autowired
	public TipoRequerimientoServiceImp(HibernateControl hibernateControl) {
		super(hibernateControl);
	}

	

	@Override
	public Class<TipoRequerimiento> getClaseModelo() {
		return TipoRequerimiento.class;
	}

	@Override
	public boolean delete(TipoRequerimiento objeto) {
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			//guardamos el objeto
			session.delete(objeto);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "No fue posible Eliminar");
			return false;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TipoRequerimiento> listarTipoRequerimiento(String codigo, String descripcion, Integer plazo, ClienteAsp clienteAsp) {
		Session session = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			Criteria c = session.createCriteria(getClaseModelo());
			//filtro por codigo
			if(codigo != null && !"".equals(codigo))
				c.add(Restrictions.ilike("codigo", codigo+"%"));
			//filtro por descripcion
			if(descripcion != null && !"".equals(descripcion))
				c.add(Restrictions.ilike("descripcion", descripcion+"%"));
			//filtro por plazo
			if(plazo != null)
				c.add(Restrictions.eq("plazo", plazo));
			//filtro por cliente
			if(clienteAsp != null)
				c.add(Restrictions.eq("clienteAsp", clienteAsp));
			//Seteo propiedades de la consulta
			c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			return c.list();
		} catch (HibernateException e) {
			logger.error("no se pudo obtener lista",e);
			return null;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}

	@Override
	public boolean save(TipoRequerimiento objeto) {
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			//guardamos el objeto
			session.save(objeto);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "No fue posible Guardar");
			return false;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}

	@Override
	public boolean update(TipoRequerimiento objeto) {
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			//guardamos el objeto
			session.update(objeto);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "No fue posible Actualizar");
			return false;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean seRepiteCodigoTipoRequerimiento(TipoRequerimiento tipo, ClienteAsp clienteAsp) {
		Session session = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			Criteria c = session.createCriteria(getClaseModelo());			
			//filtro por codigo
			if(tipo.getCodigo() != null && !"".equals(tipo.getCodigo()))
				c.add(Restrictions.ilike("codigo", tipo.getCodigo()+"%"));
			//filtro por cliente
			if(clienteAsp != null)
				c.add(Restrictions.eq("clienteAsp", clienteAsp));
			//filtro para la modificacion
			if(!"NUEVO".equals(tipo.getAccion()))
				c.add(Restrictions.ne("id", tipo.getId()));
			//Seteo propiedades de la consulta
			c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List l = c.list();
			if(l.isEmpty())
				return false;
			else
				return true;
		} catch (HibernateException e) {
			logger.error("no se pudo obtener lista",e);
			return true;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<TipoRequerimiento> listarTipoRequerimientoPopup(String val,	ClienteAsp clienteAsp) {
		Session session = null;
		if(clienteAsp == null)
			return null;
        try {
        	String valores[] = null;
        	if(val!=null)
        		valores = val.split(" ");
        	//obtenemos una sesi�n
			session = getSession();
        	Criteria c = session.createCriteria(getClaseModelo());
        	//filtro value
        	if(valores!=null){
        		for(String filtro : valores){
		        	c.add(Restrictions.or(
		        			Restrictions.ilike("codigo", filtro+"%"), 
		        			Restrictions.ilike("descripcion", filtro+"%")));
        		}
        	}
        	//filtro clienteAsp
        	c.add(Restrictions.eq("clienteAsp", clienteAsp));
        	c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        	return c.list();
        } catch (HibernateException hibernateException) {
        	logger.error("No se pudo listar ", hibernateException);
	        return null;
        }finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}

	@Override
	public TipoRequerimiento obtenerPorCodigo(String codigo, ClienteAsp clienteAsp) {
		Session session = null;
		if(clienteAsp == null)
			return null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			
//			Criteria c = session.createCriteria(getClaseModelo());			
//			//filtro por codigo
//			if(codigo != null && !"".equals(codigo))
//				c.add(Restrictions.eq("codigo", codigo));
//			else
//				return null;
//			//filtro por cliente
//			c.add(Restrictions.eq("clienteAsp", clienteAsp));
//			//Seteo propiedades de la consulta
//			c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			
			String consulta = "SELECT DISTINCT tr FROM TipoRequerimiento tr WHERE 1 = 1 ";
							if(codigo != null && !"".equals(codigo)) {
								consulta += "AND tr.codigo = '" + codigo + "' ";
							}
							else {
								return null;
							}
							consulta += "AND tr.clienteAsp.id = " + clienteAsp.getId().longValue() + " ";
							
			
			TipoRequerimiento tipoReq = (TipoRequerimiento) session.createQuery(consulta).uniqueResult();
			
			return tipoReq;
			
		} catch (HibernateException e) {
			logger.error("no se pudo obtener lista",e);
			return null;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}
	
	private void rollback(Transaction tx, Exception e, String mensaje){
		//si ocurre alg�n error intentamos hacer rollback
		if (tx != null && tx.isActive()) {
			try {
				tx.rollback();
	        } catch (HibernateException e1) {
	        	logger.error("no se pudo hacer rollback "+getClaseModelo().getName(), e1);
	        }
	        logger.error(mensaje+" "+getClaseModelo().getName(), e);
		}
	}
}
