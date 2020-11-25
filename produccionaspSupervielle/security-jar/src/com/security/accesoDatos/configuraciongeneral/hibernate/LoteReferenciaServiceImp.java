/*
 * Copyright 2011 Dividato. All rights reserved
 *
 * Copyright Version 1.0
 *
 * Create on 08/06/2011
 */
package com.security.accesoDatos.configuraciongeneral.hibernate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.security.accesoDatos.configuraciongeneral.interfaz.LoteReferenciaService;
import com.security.accesoDatos.hibernate.GestorHibernate;
import com.security.accesoDatos.hibernate.HibernateControl;
import com.security.modelo.administracion.ClienteAsp;
import com.security.modelo.configuraciongeneral.Elemento;
import com.security.modelo.configuraciongeneral.ElementoHistorico;
import com.security.modelo.configuraciongeneral.Empleado;
import com.security.modelo.configuraciongeneral.LoteReferencia;
import com.security.modelo.configuraciongeneral.Referencia;
import com.security.modelo.configuraciongeneral.ReferenciaHistorico;
import com.security.modelo.seguridad.User;
import com.security.utils.Constantes;
import com.security.utils.DateUtil;

/**
 * 
 * @author FedeMz
 *
 */
@Component
public class LoteReferenciaServiceImp extends GestorHibernate<LoteReferencia> implements LoteReferenciaService {
	private static Logger logger=Logger.getLogger(LoteReferenciaServiceImp.class);
	
	@Autowired
	public LoteReferenciaServiceImp(HibernateControl hibernateControl) {
		super(hibernateControl);
	}

	@Override
	public Class<LoteReferencia> getClaseModelo() {
		return LoteReferencia.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LoteReferencia> obtenerPor(ClienteAsp cliente,
			String codigoEmpresa, String codigoSucursal, String codigoCliente,
			Long codigoDesde, Long codigoHasta, Date fechaDesde, Date fechaHasta) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
	        Criteria crit = session.createCriteria(getClaseModelo());
	        crit.add(Restrictions.eq("clienteAsp", cliente));
	        
        	if(codigoEmpresa!=null && !codigoEmpresa.isEmpty()){
        		crit.createCriteria("empresa", "emp");
        		crit.add(Restrictions.eq("emp.codigo", codigoEmpresa));
        	}
        	if(codigoSucursal!=null && !codigoSucursal.isEmpty()){
        		crit.createCriteria("sucursal", "suc");
        		crit.add(Restrictions.eq("suc.codigo", codigoSucursal));
        	}
        	if(codigoCliente!=null && !codigoCliente.isEmpty()){
        		crit.createCriteria("clienteEmp", "cli");
        		crit.add(Restrictions.eq("cli.codigo", codigoCliente));
        	}
        	if(codigoDesde!=null)
        		crit.add(Restrictions.ge("codigo", codigoDesde));
        		//crit.add(Restrictions.ge("id", codigoDesde));
        	if(codigoHasta!=null && codigoHasta.intValue()!=0)
        		crit.add(Restrictions.le("codigo", codigoHasta));
        		//crit.add(Restrictions.le("id", codigoHasta));
        	if(fechaDesde!=null){
        		crit.add(Restrictions.ge("fechaRegistro", DateUtil.getDateFrom(fechaDesde)));
        	}
        	if(fechaHasta!=null){
        		crit.add(Restrictions.le("fechaRegistro", DateUtil.getDateTo(fechaHasta)));
        	}

	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        return crit.list();
	    } catch (HibernateException e1) {
	    	logger.error("no se pudo listar todos ", e1);
	        return null;
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
	public List<LoteReferencia> listarLoteReferenciaFiltradas(ClienteAsp cliente,
			String codigoEmpresa, String codigoSucursal, String codigoCliente, Empleado personal,
			Long codigoDesde, Long codigoHasta, Date fechaDesde, Date fechaHasta, 
			String fieldOrder, String sortOrder,Integer numeroPagina,Integer tama�oPagina) {
		
		Session session = null;
        try {
        	List<Long> ids = obtenerIDsLoteReferenciaFiltradas(cliente, codigoEmpresa,codigoSucursal,codigoCliente,
        			personal,codigoDesde,codigoHasta,fechaDesde,fechaHasta,fieldOrder,sortOrder,numeroPagina,tama�oPagina);
        	
        	//obtenemos una sesi�n
			session = getSession();
			//Si es null retornamos en cero, se puso dentro de session debido a que debe cerrar el finally
			if(ids==null || ids.size()==0)
        		return new ArrayList<LoteReferencia>();
			
        	Criteria crit = session.createCriteria(getClaseModelo());
        	crit.add(Restrictions.in("id", ids));
      
//        	//Ordenamos
        	if(sortOrder!=null && sortOrder.length()>0 &&
            	fieldOrder!=null && fieldOrder.length()>0){
            		
        			String fieldOrdenar = "";
            		String fieldOrdenar2 = "";
            		
            		if("formularioLoteReferencia.lista.codigo".equals(fieldOrder))
            		{
            			//fieldOrdenar = "id";
            			fieldOrdenar = "codigo";
            		}
            		if("formularioLoteReferencia.lista.fecha".equals(fieldOrder)){
    					fieldOrdenar = "fechaRegistro";
    					fieldOrdenar2 = "codigo";
    				}
            		
            		if("1".equals(sortOrder)){
            			if(!"".equals(fieldOrdenar))
        					crit.addOrder(Order.desc(fieldOrdenar));
            			if(!"".equals(fieldOrdenar2))
        					crit.addOrder(Order.desc(fieldOrdenar2));
        			}else if("2".equals(sortOrder)){
        				if(!"".equals(fieldOrdenar))
        					crit.addOrder(Order.asc(fieldOrdenar));
            			if(!"".equals(fieldOrdenar2))
        					crit.addOrder(Order.asc(fieldOrdenar2));
        			}
            	
            	}else{
            		crit.addOrder(Order.desc("codigo"));
            	}
        		
        	crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            return crit.list();
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
	
	@SuppressWarnings("unchecked")
	private List<Long> obtenerIDsLoteReferenciaFiltradas(ClienteAsp cliente,
			String codigoEmpresa, String codigoSucursal, String codigoCliente,Empleado personal,
			Long codigoDesde, Long codigoHasta, Date fechaDesde, Date fechaHasta, 
			String fieldOrder, String sortOrder,Integer numeroPagina,Integer tama�oPagina) {
		
		Session session = null;
		
        try {
        	//obtenemos una sesi�n
			session = getSession();
        	Criteria crit = session.createCriteria(getClaseModelo());
        	crit.setProjection(Projections.id());
	       
	        
	        crit.add(Restrictions.eq("clienteAsp", cliente));
	        
        	if(codigoEmpresa!=null && !codigoEmpresa.isEmpty()){
        		crit.createCriteria("empresa", "emp");
        		crit.add(Restrictions.eq("emp.codigo", codigoEmpresa));
        	}
        	if(codigoSucursal!=null && !codigoSucursal.isEmpty()){
        		crit.createCriteria("sucursal", "suc");
        		crit.add(Restrictions.eq("suc.codigo", codigoSucursal));
        	}
        	if(codigoCliente!=null && !codigoCliente.isEmpty()){
        		crit.createCriteria("clienteEmp", "cli");
        		crit.add(Restrictions.eq("cli.codigo", codigoCliente));
        	}
        	if(personal!=null){
        		crit.add(Restrictions.sqlRestriction(" {alias}.codigo IN (Select idLoteReferencia from referencias_historico where usuario_id = "+personal.getId()+")"));
        	}
        	if(codigoDesde!=null)
        		crit.add(Restrictions.ge("codigo", codigoDesde));
        		//crit.add(Restrictions.ge("id", codigoDesde));
        	if(codigoHasta!=null && codigoHasta.intValue()!=0)
        		crit.add(Restrictions.le("codigo", codigoHasta));
        		//crit.add(Restrictions.le("id", codigoHasta));
        	if(fechaDesde!=null){
        		crit.add(Restrictions.ge("fechaRegistro", DateUtil.getDateFrom(fechaDesde)));
        	}
        	if(fechaHasta!=null){
        		crit.add(Restrictions.le("fechaRegistro", DateUtil.getDateTo(fechaHasta)));
        	}
        	
        	crit.add(Restrictions.or(Restrictions.ne("habilitado", false), Restrictions.isNull("habilitado")));
        	
//        	//Ordenamos
        	if(sortOrder!=null && sortOrder.length()>0 &&
            	fieldOrder!=null && fieldOrder.length()>0){
            		
        			String fieldOrdenar = "";
            		String fieldOrdenar2 = "";
            		
            		if("formularioLoteReferencia.lista.codigo".equals(fieldOrder))
            		{
            			//fieldOrdenar = "id";
            			fieldOrdenar = "codigo";
            		}
            		if("formularioLoteReferencia.lista.fecha".equals(fieldOrder)){
    					fieldOrdenar = "fechaRegistro";
    					fieldOrdenar2 = "codigo";
    				}
            		
            		if("1".equals(sortOrder)){
            			if(!"".equals(fieldOrdenar))
        					crit.addOrder(Order.desc(fieldOrdenar));
            			if(!"".equals(fieldOrdenar2))
        					crit.addOrder(Order.desc(fieldOrdenar2));
        			}else if("2".equals(sortOrder)){
        				if(!"".equals(fieldOrdenar))
        					crit.addOrder(Order.asc(fieldOrdenar));
            			if(!"".equals(fieldOrdenar2))
        					crit.addOrder(Order.asc(fieldOrdenar2));
        			}
            	
            	}else{
            		crit.addOrder(Order.desc("codigo"));
            	}
            	

        	//Paginamos
        	if(numeroPagina!=null && numeroPagina.longValue()>0 
    				&& tama�oPagina!=null && tama�oPagina.longValue()>0){
    			Integer paginaInicial = (numeroPagina - 1);
    			Integer filaDesde = tama�oPagina * paginaInicial;
    			crit.setFirstResult(filaDesde);
    			
    			crit.setMaxResults(tama�oPagina);
    		}
        	
        	return crit.list();
        	
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
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LoteReferencia> obtenerLoteReferenciaSQL(ClienteAsp cliente,
			String codigoEmpresa, String codigoSucursal, String codigoCliente,Empleado personal,
			Long codigoDesde, Long codigoHasta, Date fechaDesde, Date fechaHasta, 
			String fieldOrder, String sortOrder,Integer numeroPagina,Integer tama�oPagina) {
		
		Session session = null;
		String order = "";
		
        try {
        	//obtenemos una sesi�n
			session = getSession();
			
//        	//Ordenamos
        	if(sortOrder!=null && sortOrder.length()>0 &&
            	fieldOrder!=null && fieldOrder.length()>0){
            		
        			String fieldOrdenar = "";
            		String fieldOrdenar2 = "";

            		if("formularioLoteReferencia.lista.codigo".equals(fieldOrder))
            		{
            			//fieldOrdenar = "id";
            			fieldOrdenar = "this_.codigo ";
            		}
            		if("formularioLoteReferencia.lista.fecha".equals(fieldOrder)){
    					fieldOrdenar = "this_.fecha_registro ";
    					fieldOrdenar2 = ",this_.codigo ";
    				}
            		
            		if("1".equals(sortOrder)){
            			if(!"".equals(fieldOrdenar))
        					fieldOrdenar+= "desc";
            			if(!"".equals(fieldOrdenar2))
        					fieldOrdenar2+= "desc";
        			}else if("2".equals(sortOrder)){
        				if(!"".equals(fieldOrdenar))
        					fieldOrdenar+= "asc";
            			if(!"".equals(fieldOrdenar2))
            				fieldOrdenar2+= "asc";
        			}
            		
            		order = fieldOrdenar + fieldOrdenar2;
            	
            	}
        	else
            	{
            		order = "this_.codigo desc";
            	}
        	
			String consulta = "select distinct this_.id as id,this_.codigo as codigo,this_.fecha_registro as fecha_registro,"
					+ "pjur.razonSocial as clienteEmpStr, emp1_.descripcion as empresaStr, suc2_.descripcion as sucursalStr, "
					+ "(select distinct count(rf.id) from basa.dbo.referencia rf where rf.lote_referencia_id = this_.id) as cantidadRef,"
					+ "(pfis.nombre +' '+ pfis.apellido) as usuarioCarga, DENSE_RANK() OVER (ORDER BY "+order+") AS R "
					+ "from basa.dbo.lotereferencia this_ "
	    			+ "inner join basa.dbo.clientesEmp cli3_ on this_.cliente_emp_id=cli3_.id "
	    			+ "inner join basa.dbo.empresas emp1_ on this_.empresa_id=emp1_.id "
	    			+ "inner join basa.dbo.sucursales suc2_ on this_.sucursal_id=suc2_.id "
	    			+ "left join basa.dbo.referencias_historico rh on this_.codigo = rh.idLoteReferencia "
	    			+ "LEFT JOIN basa.dbo.users usr on rh.usuario_id = usr.id "
	    			+ "LEFT JOIN basa.dbo.personas_fisicas pfis on usr.persona_id = pfis.id "
	    			+ "INNER JOIN personas_juridicas pjur on cli3_.razonSocial_id = pjur.id " 
	    			+ " where 1=1 "
	    			+ "and (this_.habilitado<>0 or this_.habilitado is null) "
	    			+ "and this_.cliente_asp_id= "+cliente.getId().longValue()+" ";
	    	
					if(codigoEmpresa!=null && !codigoEmpresa.isEmpty())
						consulta+= "and emp1_.codigo="+codigoEmpresa+" ";
					
					if(codigoSucursal!=null && !codigoSucursal.isEmpty())
						consulta+= "and suc2_.codigo="+codigoSucursal+" ";
					
					if(codigoCliente!=null && !codigoCliente.isEmpty())
						consulta+= "and cli3_.codigo="+codigoCliente+" ";
	    			
	    			if(personal!=null)
	    				consulta+= "and rh.usuario_id = :personalId ";
	    				
	    			if(codigoDesde!=null)
	    				consulta+= "and this_.codigo>=:codigoDesde ";
	            		
	            	if(codigoHasta!=null && codigoHasta.intValue()!=0)
	            		consulta+= "and this_.codigo<=:codigoHasta ";
	            		
	            	if(fechaDesde!=null)
	            		consulta+= "and this_.fecha_registro>=:fechaDesde ";
	            		
	            	if(fechaHasta!=null)
	            		consulta+= "and this_.fecha_registro<=:fechaHasta ";
	
        	
	    	if(numeroPagina!=null && numeroPagina.longValue()>0 
    				&& tama�oPagina!=null && tama�oPagina.longValue()>0){
    			Integer paginaInicial = (numeroPagina - 1);
    			Integer filaDesde = tama�oPagina * paginaInicial;
    			Integer filaHasta = filaDesde + tama�oPagina;
    			filaDesde = filaDesde + 1;
    			consulta = "SELECT * FROM (" + consulta + ") AS RESULT WHERE R BETWEEN "+filaDesde+" and "+ filaHasta + " order by R";
    		}
	            	
	    	Query q = session.createSQLQuery(consulta)
					.setResultTransformer(
							new AliasToEntityMapResultTransformer());
        	
        	if(personal!=null)
        		q.setLong("personalId", personal.getId());
        	
        	if(codigoDesde!=null)
				q.setLong("codigoDesde", codigoDesde);
        		
        	if(codigoHasta!=null && codigoHasta.intValue()!=0)
        		q.setLong("codigoHasta", codigoHasta);
      	
        	if(fechaDesde!=null)
        		q.setDate("fechaDesde", fechaDesde);
        		
        	if(fechaHasta!=null)
        		q.setDate("fechaHasta", fechaHasta);
        	      	
        	return convertLotesReferenciaList(q.list());
        	
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
	
	private List<LoteReferencia> convertLotesReferenciaList(
			List<Map<String, Object>> list) {
		List<LoteReferencia> returnList = new ArrayList<LoteReferencia>();
		for (Map<String, Object> map : list) {
			LoteReferencia consortiumProduction = (LoteReferencia) getObjectFromMap(
					map, new LoteReferencia());
			returnList.add(consortiumProduction);
		}
		return returnList;
	}
	
	
	@Override
	public Integer contarObtenerPor(ClienteAsp cliente,
			String codigoEmpresa, String codigoSucursal, String codigoCliente,Empleado personal,
			Long codigoDesde, Long codigoHasta, Date fechaDesde, Date fechaHasta) {
		
		Session session = null;
		Integer result = null;
		
	    try {
	    	//obtenemos una sesi�n
	    	session = getSession();
	        Criteria crit = session.createCriteria(getClaseModelo());
	        crit.setProjection(Projections.rowCount());
	       
	        
	        crit.add(Restrictions.eq("clienteAsp", cliente));
	        
        	if(codigoEmpresa!=null && !codigoEmpresa.isEmpty()){
        		crit.createCriteria("empresa", "emp");
        		crit.add(Restrictions.eq("emp.codigo", codigoEmpresa));
        	}
        	if(codigoSucursal!=null && !codigoSucursal.isEmpty()){
        		crit.createCriteria("sucursal", "suc");
        		crit.add(Restrictions.eq("suc.codigo", codigoSucursal));
        	}
        	if(codigoCliente!=null && !codigoCliente.isEmpty()){
        		crit.createCriteria("clienteEmp", "cli");
        		crit.add(Restrictions.eq("cli.codigo", codigoCliente));
        	}
        	if(personal!=null){
        		crit.add(Restrictions.sqlRestriction(" {alias}.codigo IN (Select idLoteReferencia from referencias_historico where usuario_id = "+personal.getId()+")"));
        	}
        	if(codigoDesde!=null)
        		crit.add(Restrictions.ge("codigo", codigoDesde));
        		//crit.add(Restrictions.ge("id", codigoDesde));
        	if(codigoHasta!=null && codigoHasta.intValue()!=0)
        		crit.add(Restrictions.le("codigo", codigoHasta));
        		//crit.add(Restrictions.le("id", codigoHasta));
        	if(fechaDesde!=null){
        		crit.add(Restrictions.ge("fechaRegistro", DateUtil.getDateFrom(fechaDesde)));
        	}
        	if(fechaHasta!=null){
        		crit.add(Restrictions.le("fechaRegistro", DateUtil.getDateTo(fechaHasta)));
        	}
        	
        	crit.add(Restrictions.or(Restrictions.ne("habilitado", false), Restrictions.isNull("habilitado")));

	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        result = ((Integer)crit.list().get(0));
	        return result;
	    } catch (HibernateException e1) {
	    	logger.error("no se pudo listar todos ", e1);
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
	public Integer contarLotesSql(ClienteAsp cliente,
			String codigoEmpresa, String codigoSucursal, String codigoCliente,Empleado personal,
			Long codigoDesde, Long codigoHasta, Date fechaDesde, Date fechaHasta) {
		
		Session session = null;
		Integer result = null;
		
	    try {
	    	//obtenemos una sesi�n
	    	session = getSession();
	    	
	    	String consulta = "select count(distinct this_.id) from basa.dbo.lotereferencia this_ "
	    			+ "inner join basa.dbo.clientesEmp cli3_ on this_.cliente_emp_id=cli3_.id "
	    			+ "inner join basa.dbo.empresas emp1_ on this_.empresa_id=emp1_.id "
	    			+ "inner join basa.dbo.sucursales suc2_ on this_.sucursal_id=suc2_.id "
	    			+ "left join basa.dbo.referencias_historico rh on this_.codigo = rh.idLoteReferencia "
	    			+ " where 1=1 "
	    			+ "and (this_.habilitado<>0 or this_.habilitado is null) "
	    			+ "and this_.cliente_asp_id= "+cliente.getId().longValue()+" ";
	    	
	    			if(codigoEmpresa!=null && !codigoEmpresa.isEmpty())
	    				consulta+= "and emp1_.codigo="+codigoEmpresa+" ";
	    			
	    			if(codigoSucursal!=null && !codigoSucursal.isEmpty())
	    				consulta+= "and suc2_.codigo="+codigoSucursal+" ";
	    			
	    			if(codigoCliente!=null && !codigoCliente.isEmpty())
	    				consulta+= "and cli3_.codigo="+codigoCliente+" ";
	    			
	    			if(personal!=null)
	    				consulta+= "and rh.usuario_id = :personalId ";
	    				
	    			if(codigoDesde!=null)
	    				consulta+= "and this_.codigo>=:codigoDesde ";
	            		
	            	if(codigoHasta!=null && codigoHasta.intValue()!=0)
	            		consulta+= "and this_.codigo<=:codigoHasta ";
	            		
	            	if(fechaDesde!=null)
	            		consulta+= "and this_.fecha_registro>=:fechaDesde ";
	            		
	            	if(fechaHasta!=null)
	            		consulta+= "and this_.fecha_registro<=:fechaHasta ";
	            		
	    			
	            	SQLQuery q = session.createSQLQuery(consulta);
	            	
	            	if(personal!=null)
	            		q.setLong("personalId", personal.getId());
	            	
	            	if(codigoDesde!=null)
	    				q.setLong("codigoDesde", codigoDesde);
	            		
	            	if(codigoHasta!=null && codigoHasta.intValue()!=0)
	            		q.setLong("codigoHasta", codigoHasta);
	            	
	            	if(fechaDesde!=null)
	            		q.setDate("fechaDesde", fechaDesde);
	            		
	            	if(fechaHasta!=null)
	            		q.setDate("fechaHasta", fechaHasta);

	            	return (Integer) q.list().get(0); 
	    
        
	    } catch (HibernateException e1) {
	    	logger.error("no se pudo listar todos ", e1);
	        return null;
	    }finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}
	
	
	/**
	 * Guarda o actualiza un LoteReferencia en la base de datos y todas sus relaciones
	 * @param objeto
	 */
	@SuppressWarnings("unchecked")
	public synchronized void guardarActualizar(LoteReferencia loteReferencia){
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			
			//referencias a mantener
			Collection<Referencia> referencias=new ArrayList<Referencia>(loteReferencia.getReferencias());
			
			if(loteReferencia.getId()==null || loteReferencia.getId().equals(0L)){
				Long id = null;
				loteReferencia.setId(id);
				//Agregado para el codigo///////////////
				loteReferencia.setCodigo(traerUltCodigoPorClienteAsp(obtenerClienteAspUser())+1L);
				////////////////////////////////////////
				loteReferencia.getReferencias().clear();
				session.save(loteReferencia);
			}else{
				//actualizamos las todas las referencias
				for(Referencia ref : referencias)
				{
					Elemento contenedor = ref.getContenedor();
					if(contenedor!=null){
						contenedor = (Elemento)session.get(Elemento.class, contenedor.getId()); //refrescamos de la base
					}
					ref.setElemento((Elemento)session.get(Elemento.class, ref.getElemento().getId()));//refrescamos de la base
					ref.getElemento().setClienteEmp(loteReferencia.getClienteEmp());
					ref.setLoteReferencia(loteReferencia);
					if(contenedor!=null && ref.getElemento()!=contenedor){
						contenedor.setClienteEmp(loteReferencia.getClienteEmp());
						session.update(contenedor);
						//Lineas agregadas para el historico
						registrarHistoricoElementos("MS004ELE", contenedor, session);
						////////////////////////////////////
						ref.getElemento().setContenedor(contenedor);
					}
					session.update(ref.getElemento());
					//Lineas agregadas para el historico
					registrarHistoricoElementos("MS006ELE", ref.getElemento(), session);
					////////////////////////////////////
					if(ref.getId()!=null && !ref.getId().equals(0L)){
						session.update(ref);
						//Linea agregada para el historico de referencias
						registrarHistoricoReferencias("MS006REF", ref, session,loteReferencia);
						/////////////////////////////////////////////////
					}
				}
				//borramos las referencias eliminadas
				loteReferencia = (LoteReferencia) session.get(LoteReferencia.class, loteReferencia.getId());
				Collection<Referencia> remove = CollectionUtils.subtract(loteReferencia.getReferencias(),referencias);
				for(Referencia ref:remove){
					if(ref.getId()!=null){
						ref.getElemento().setContenedor(null);
						session.update(ref.getElemento());
						//Lineas agregadas para el historico de elementos
						registrarHistoricoElementos("MS005ELE", ref.getElemento(), session);
						////////////////////////////////////
						session.delete(ref);
						//Linea agregada para el historico de referencias
						registrarHistoricoReferencias("MS005REF", ref, session,loteReferencia);
						/////////////////////////////////////////////////
						loteReferencia.getReferencias().remove(ref);
					}
				}
				
				//referencias agregadas
				referencias = CollectionUtils.subtract(referencias,loteReferencia.getReferencias());
			}
			
			//guardamos las nuevas referencias
			for(Referencia ref : referencias)
			{
				Elemento contenedor = ref.getContenedor();
				if(contenedor!=null){
					contenedor = (Elemento)session.get(Elemento.class, contenedor.getId()); //refrescamos de la base
				}
				ref.setElemento((Elemento)session.get(Elemento.class, ref.getElemento().getId()));//refrescamos de la base
				ref.getElemento().setClienteEmp(loteReferencia.getClienteEmp());
				ref.setLoteReferencia(loteReferencia);
				if(contenedor!=null && ref.getElemento()!=contenedor){
					contenedor.setClienteEmp(loteReferencia.getClienteEmp());
					session.update(contenedor);
					//Lineas agregadas para el historico
					registrarHistoricoElementos("MS004ELE", contenedor, session);
					////////////////////////////////////
					ref.getElemento().setContenedor(contenedor);
				}
				session.update(ref.getElemento());
				//Lineas agregadas para el historico
				registrarHistoricoElementos("MS007ELE", ref.getElemento(), session);
				////////////////////////////////////
				session.save(ref);
				//Linea agregada para el historico de referencias
				registrarHistoricoReferencias("MS004REF", ref, session,loteReferencia);
				/////////////////////////////////////////////////
				loteReferencia.getReferencias().add(ref);
			}
			session.update(loteReferencia);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
		} 
		catch (RuntimeException e) {
			//si ocurre alg�n error intentamos hacer rollback
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();
		        } catch (HibernateException e1) {
		        	logger.error("no se pudo hacer rollback", e1);
		        }
		        logger.error("no se pudo guardar", e);
			}
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
	public synchronized boolean guardarActualizarLoteYModificadas(LoteReferencia loteReferencia, List<Referencia> modificadas, List<Referencia> eliminadas){
		Session session = null;
		Transaction tx = null;
		Boolean nuevo = false;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			
			//Actualizamos el historico de referencias con las modificadas
			if(modificadas!=null && modificadas.size()>0){
				for(Referencia refModi:modificadas){
					//Linea agregada para el historico de referencias
					registrarHistoricoReferencias("MS006REF", refModi, session,loteReferencia);
					/////////////////////////////////////////////////
				}
			}
				
			//Actualizamos el historico de referencias con las eliminadas
			if(eliminadas!=null && eliminadas.size()>0){
				for(Referencia refEli:eliminadas){
					//Linea agregada para el historico de referencias
					registrarHistoricoReferencias("MS005REF", refEli, session,loteReferencia);
					/////////////////////////////////////////////////
				}
			}
			
			//referencias a mantener
			Collection<Referencia> referencias=new ArrayList<Referencia>(loteReferencia.getReferencias());
			
			if(loteReferencia.getId()==null || loteReferencia.getId().equals(0L)){
				nuevo = true;
				Long id = null;
				loteReferencia.setId(id);
				//Agregado para el codigo///////////////
				loteReferencia.setCodigo(traerUltCodigoPorClienteAsp(loteReferencia.getClienteAsp())+1L);
				////////////////////////////////////////
				loteReferencia.getReferencias().clear();
				session.save(loteReferencia);
			}else{
				//actualizamos las todas las referencias
				for(Referencia ref : referencias)
				{
					Elemento contenedor = ref.getContenedor();
					if(contenedor!=null){
						contenedor = (Elemento)session.get(Elemento.class, contenedor.getId()); //refrescamos de la base
					}
					ref.setElemento((Elemento)session.get(Elemento.class, ref.getElemento().getId()));//refrescamos de la base
					ref.getElemento().setClienteEmp(loteReferencia.getClienteEmp());
					ref.setLoteReferencia(loteReferencia);
					if(contenedor!=null && ref.getElemento()!=contenedor){
						contenedor.setClienteEmp(loteReferencia.getClienteEmp());
						session.update(contenedor);
						//Lineas agregadas para el historico
						registrarHistoricoElementos("MS004ELE", contenedor, session);
						////////////////////////////////////
						ref.getElemento().setContenedor(contenedor);
					}
					session.update(ref.getElemento());
					//Lineas agregadas para el historico
					registrarHistoricoElementos("MS006ELE", ref.getElemento(), session);
					////////////////////////////////////
					if(ref.getId()!=null && !ref.getId().equals(0L)){
						session.update(ref);
					}
				}
				//borramos las referencias eliminadas
				loteReferencia = (LoteReferencia) session.get(LoteReferencia.class, loteReferencia.getId());
				Collection<Referencia> remove = CollectionUtils.subtract(loteReferencia.getReferencias(),referencias);
				for(Referencia ref:remove){
					if(ref.getId()!=null){
						ref.getElemento().setContenedor(null);
						session.update(ref.getElemento());
						//Lineas agregadas para el historico de elementos
						registrarHistoricoElementos("MS005ELE", ref.getElemento(), session);
						////////////////////////////////////
						session.delete(ref);
						loteReferencia.getReferencias().remove(ref);
					}
				}
				
				//referencias agregadas
				referencias = CollectionUtils.subtract(referencias,loteReferencia.getReferencias());
			}
			
			//guardamos las nuevas referencias
			for(Referencia ref : referencias)
			{
				Elemento contenedor = ref.getContenedor();
				if(contenedor!=null){
					contenedor = (Elemento)session.get(Elemento.class, contenedor.getId()); //refrescamos de la base
				}
				ref.setElemento((Elemento)session.get(Elemento.class, ref.getElemento().getId()));//refrescamos de la base
				ref.getElemento().setClienteEmp(loteReferencia.getClienteEmp());
			//	ref.getElemento().setEstado(Constantes.ELEMENTO_ESTADO_EN_GUARDA);
				ref.setLoteReferencia(loteReferencia);
				if(contenedor!=null && ref.getElemento()!=contenedor){
					contenedor.setClienteEmp(loteReferencia.getClienteEmp());
					session.update(contenedor);
					//Lineas agregadas para el historico
					registrarHistoricoElementos("MS004ELE", contenedor, session, loteReferencia.getUsuario());
					////////////////////////////////////
					ref.getElemento().setContenedor(contenedor);
				}
				session.update(ref.getElemento());
				//Lineas agregadas para el historico
				registrarHistoricoElementos("MS007ELE", ref.getElemento(), session, loteReferencia.getUsuario());
				////////////////////////////////////
				session.save(ref);
				//Linea agregada para el historico de referencias
				registrarHistoricoReferencias("MS004REF", ref, session,loteReferencia);
				/////////////////////////////////////////////////
				loteReferencia.getReferencias().add(ref);
			}
			session.update(loteReferencia);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "No fue posible guardar");
			if(nuevo!=null && nuevo){
				loteReferencia.setId(0L);
				loteReferencia.setCodigo(0L);
			}
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
	public synchronized boolean guardarLoteYActualizarReferencias(LoteReferencia loteReferencia){
		Session session = null;
		Transaction tx = null;
		Boolean nuevo = false;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			
//			//Actualizamos el historico de referencias con las modificadas
//			if(modificadas!=null && modificadas.size()>0){
//				for(Referencia refModi:modificadas){
//					//Linea agregada para el historico de referencias
//					registrarHistoricoReferencias("MS006REF", refModi, session,loteReferencia);
//					/////////////////////////////////////////////////
//				}
//			}
					
			//referencias a mantener
			Collection<Referencia> referencias=new ArrayList<Referencia>(loteReferencia.getReferencias());
			
			if(loteReferencia.getId()==null || loteReferencia.getId().equals(0L)){
				nuevo = true;
				Long id = null;
				loteReferencia.setId(id);
				//Agregado para el codigo///////////////
				loteReferencia.setCodigo(traerUltCodigoPorClienteAsp(obtenerClienteAspUser())+1L);
				////////////////////////////////////////
				loteReferencia.getReferencias().clear();
				session.save(loteReferencia);
			
				//actualizamos todas las referencias
				for(Referencia ref : referencias)
				{
					Elemento contenedor = ref.getContenedor();
					if(contenedor!=null){
						contenedor = (Elemento)session.get(Elemento.class, contenedor.getId()); //refrescamos de la base
					}
					ref.setElemento((Elemento)session.get(Elemento.class, ref.getElemento().getId()));//refrescamos de la base
					ref.getElemento().setClienteEmp(loteReferencia.getClienteEmp());
					ref.setLoteReferencia(loteReferencia);
					if(contenedor!=null && ref.getElemento()!=contenedor){
						contenedor.setClienteEmp(loteReferencia.getClienteEmp());
						session.update(contenedor);
						//Lineas agregadas para el historico
						registrarHistoricoElementos("MS004ELE", contenedor, session);
						////////////////////////////////////
						ref.getElemento().setContenedor(contenedor);
					}
					session.update(ref.getElemento());
					//Lineas agregadas para el historico
					registrarHistoricoElementos("MS006ELE", ref.getElemento(), session);
					////////////////////////////////////
					if(ref.getId()!=null && !ref.getId().equals(0L)){
						session.update(ref);
						//Linea agregada para el historico de referencias
						registrarHistoricoReferencias("MS006REF", ref, session,loteReferencia);
						/////////////////////////////////////////////////
					}
				}
								
			}
			
			session.update(loteReferencia);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "No fue posible guardar");
			if(nuevo!=null && nuevo){
				loteReferencia.setId(0L);
				loteReferencia.setCodigo(0L);
			}
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
	public void eliminar(long idLoteReferencia){
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos una nueva transacci�n
			tx = session.getTransaction();
			
			tx.begin();
			LoteReferencia loteReferencia=(LoteReferencia) session.get(LoteReferencia.class, idLoteReferencia);
			
			for(Referencia ref:loteReferencia.getReferencias())
			{
				ref.getElemento().setContenedor(null);
				session.update(ref.getElemento());
				//Lineas agregadas para el historico
				registrarHistoricoElementos("MS005ELE", ref.getElemento(), session);
				////////////////////////////////////
				session.delete(ref);
				//Linea agregada para el historico de referencias
				registrarHistoricoReferencias("MS003REF", ref, session,loteReferencia);
				/////////////////////////////////////////////////
				
			}
			
			session.delete(loteReferencia);
			//hacemos commit a los cambios para que se refresque la base de datos.
			tx.commit();
		} 
		catch (RuntimeException e) {
			//si ocurre alg�n error, se hace rollback a los cambios.
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();
		        } catch (HibernateException e1) {
		        	logger.error("no se pudo hacer rollback", e1);
		        }
		        logger.error("no se pudo guardar", e);
			}
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
		
	}
	
	@Override
	public int eliminarLoteReferencia(long idLoteReferencia){
		Session session = null;
		Transaction tx = null;
		int cantidadReferenciasEliminadas = 0;
		
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos una nueva transacci�n
			tx = session.getTransaction();
			//traemos el lote de la session
			LoteReferencia loteReferencia=(LoteReferencia) session.get(LoteReferencia.class, idLoteReferencia);
			
			if(loteReferencia.getReferencias().size()>0){
				
				Iterator<Referencia> it = loteReferencia.getReferencias().iterator();
				while(it.hasNext()){
					
					Referencia ref = (Referencia)it.next();
					
					try
					{	
						
						tx.begin();
						session.clear();
						
						ref.getElemento().setContenedor(null);
						session.refresh(ref);
						session.update(ref.getElemento());
						//Lineas agregadas para el historico
						registrarHistoricoElementos("MS005ELE", ref.getElemento(), session);
						////////////////////////////////////
						session.delete(ref);
						//Linea agregada para el historico de referencias
						registrarHistoricoReferencias("MS003REF", ref, session,loteReferencia);
						/////////////////////////////////////////////////
						session.refresh(ref);
						tx.commit();
						it.remove();
						cantidadReferenciasEliminadas++;
					}
					catch(RuntimeException e)
					{
						if (tx != null && tx.isActive()) 
						{
							try 
							{
								tx.rollback();
					        } catch (HibernateException e1) {
					        	logger.error("no se pudo hacer rollback", e1);
					        }
					        logger.error("no se pudo guardar", e);
						}
					}
					
				}
				
				//Si la cantidad de referencias ants del proceso es igual a la actual es que no se borro ninguna
				if(cantidadReferenciasEliminadas==0){
					return 0;
				}
			}
			
			if(loteReferencia.getReferencias().size()==0){
				try
				{
					tx.begin();
					loteReferencia.setHabilitado(false);
					loteReferencia.setReferencias(null);
					//session.delete(loteReferencia);
					session.clear();
					session.update(loteReferencia);
					tx.commit();
					return 2;
				}
				catch(RuntimeException e)
				{
					if (tx != null && tx.isActive()) 
					{
						try 
						{
							tx.rollback();
				        } catch (HibernateException e1) {
				        	logger.error("no se pudo hacer rollback", e1);
				        }
				        logger.error("no se pudo guardar", e);
					}
				}
			}
			else if(loteReferencia.getReferencias().size()>0){
				return 1;
			}
			
		} 
		catch (NumberFormatException e) {
			//si ocurre alg�n error, se hace rollback a los cambios.
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();
		        } catch (HibernateException e1) {
		        	logger.error("no se pudo hacer rollback", e1);
		        }
		        logger.error("no se pudo eliminar", e);
		        return 0;
			}
			
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
		return 0;
	}
	
	//Metodo agregado para el codigo
	@SuppressWarnings("unchecked")
	@Override
	public Long traerUltCodigoPorClienteAsp(ClienteAsp cliente){
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
        	Criteria crit = session.createCriteria(getClaseModelo());
        	if(cliente!=null){
	        	crit.add(Restrictions.eq("clienteAsp", cliente));	        	
	        	crit.setProjection(Projections.max("codigo"));
        	}
        	crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        	List<Long> result = crit.list();
        	if(result.size() == 1){
        		Long rta = result.get(0);
        		if(rta == null)
        			return (long) 0;
        		else
        			return rta; 
        	}
            return null;
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
	
	private User obtenerUser(){
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}
	
	private ClienteAsp obtenerClienteAspUser(){
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getCliente();
	}
	
	private void registrarHistoricoReferencias(String mensaje, Referencia referencia,Session session,LoteReferencia lote){
		ReferenciaHistorico referenciaHis = new ReferenciaHistorico();
		referenciaHis.setIdReferencia(referencia.getId());
		//referenciaHis.setIdLoteReferencia(lote.getId());
		referenciaHis.setIdLoteReferencia(lote.getCodigo());
		if(referencia.getElemento()!=null)
			referenciaHis.setCodigoElemento(referencia.getElemento().getCodigo());
		if(referencia.getElemento().getContenedor()!=null)
			referenciaHis.setCodigoContenedor(referencia.getElemento().getContenedor().getCodigo());
		referenciaHis.setAccion(mensaje);
		referenciaHis.setFechaHora(new Date());
		if(lote.getUsuario()!=null)
			referenciaHis.setUsuario(lote.getUsuario());
		else
			referenciaHis.setUsuario(obtenerUser());
		if(lote.getClienteEmp()!=null)
			referenciaHis.setClienteAsp(lote.getClienteAsp());
		else
			referenciaHis.setClienteAsp(obtenerClienteAspUser());
		referenciaHis.setCodigoCliente(lote.getClienteEmp().getCodigo());
		referenciaHis.setNombreCliente(lote.getClienteEmp().getRazonSocialONombreYApellido());
		session.save(referenciaHis);
	}
	
	private void registrarHistoricoElementos(String mensaje, Elemento elemento,Session session){
		registrarHistoricoElementos(mensaje, elemento, session, null);
	}
	
	private void registrarHistoricoElementos(String mensaje, Elemento elemento,Session session,User usuario){
		ElementoHistorico elementoHis = new ElementoHistorico();
		elementoHis.setCodigoElemento(elemento.getCodigo());
		elementoHis.setAccion(mensaje);
		elementoHis.setFechaHora(new Date());
		if(usuario!=null)
			elementoHis.setUsuario(usuario);
		else
			elementoHis.setUsuario(obtenerUser());
		elementoHis.setClienteAsp(elemento.getClienteAsp());
		if(elemento.getClienteEmp()!=null){
			elementoHis.setCodigoCliente(elemento.getClienteEmp().getCodigo());
			elementoHis.setNombreCliente(elemento.getClienteEmp().getRazonSocialONombreYApellido());
		}
		elementoHis.setCodigoTipoElemento(elemento.getTipoElemento().getCodigo());
		elementoHis.setNombreTipoElemento(elemento.getTipoElemento().getDescripcion());
		session.save(elementoHis);
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
	
	@Override
	public LoteReferencia getByID(Long id) {
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
			
			String consulta = " SELECT DISTINCT lr FROM LoteReferencia lr WHERE lr.id = "+ id.longValue()+"";
			
			LoteReferencia loteReferencia = (LoteReferencia)session.createQuery(consulta).uniqueResult();
			Hibernate.initialize(loteReferencia.getEmpresa());
			Hibernate.initialize(loteReferencia.getSucursal());
			Hibernate.initialize(loteReferencia.getClienteEmp());
			
			return loteReferencia;
			
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
	
	private Object getObjectFromMap(Map<String, Object> map, Object object) {
		Field[] fields = object.getClass().getDeclaredFields();

		for (Field field : fields) {
			Annotation[] annotations = field.getDeclaredAnnotations();

			for (Annotation annotation : annotations) {
				if (annotation instanceof javax.persistence.Column) {
					javax.persistence.Column column = (javax.persistence.Column) annotation;
					try {
						setReflectedValue(field.getName(), object,
								map.get(column.name()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return object;
	}

	@SuppressWarnings("rawtypes")
	protected void setReflectedValue(String field, Object objectFrom, Object value)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		if (value == null)
			return;
		Class[] param = new Class[1];
		param[0] = value.getClass();
		if(param[0] == java.sql.Timestamp.class || param[0] == java.sql.Date.class){
			param[0] = java.util.Date.class;
		}

		Method method = objectFrom.getClass().getDeclaredMethod(
				getSetFromField(field), param);
		method.invoke(objectFrom, value);
	}

	private String getSetFromField(String field) {
		char first = field.charAt(0);
		String cutted = field.substring(1, field.length());
		return "set" + Character.toUpperCase(first) + cutted;
	}
}
