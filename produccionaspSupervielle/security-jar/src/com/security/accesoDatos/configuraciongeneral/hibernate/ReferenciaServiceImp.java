/*
 * Copyright 2011 Dividato. All rights reserved
 *
 * Copyright Version 1.0
 *
 * Create on 12/07/2011
 */
package com.security.accesoDatos.configuraciongeneral.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.security.accesoDatos.configuraciongeneral.interfaz.ReferenciaService;
import com.security.accesoDatos.hibernate.GestorHibernate;
import com.security.accesoDatos.hibernate.HibernateControl;
import com.security.exceptions.BasaException;
import com.security.exceptions.ClasificacionDocumentalClienteAspDistintoDeClienteAspUserException;
import com.security.exceptions.ClienteAspNullException;
import com.security.modelo.administracion.ClienteAsp;
import com.security.modelo.configuraciongeneral.ClasificacionDocumental;
import com.security.modelo.configuraciongeneral.ClienteEmp;
import com.security.modelo.configuraciongeneral.Elemento;
import com.security.modelo.configuraciongeneral.LoteReferencia;
import com.security.modelo.configuraciongeneral.Referencia;
import com.security.recursos.Configuracion;

/**
 * @author Gabriel Mainero
 *
 */
@Component
public class ReferenciaServiceImp extends GestorHibernate<Referencia> implements ReferenciaService{
	private static Logger logger=Logger.getLogger(ReferenciaServiceImp.class);
	
	@Autowired
	public ReferenciaServiceImp(HibernateControl hibernateControl) {
		super(hibernateControl);
	}

	@Override
	public Class<Referencia> getClaseModelo() {
		return Referencia.class;
	}
	

	
	/*
	 * Integer numeroPagina;
			Integer tama�oPagina;
			String fieldOrder;
			String sortOrder;
			Long orden;
	 * (non-Javadoc)
	 * @see com.security.accesoDatos.configuraciongeneral.interfaz.ReferenciaService#listarReferencias(java.util.Date, java.util.Date, java.util.Date, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String, java.lang.Long)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List listarReferencias(Long clienteAspId, Date fecha1, Date fecha2, Date fechaEntre, Date fechaInicio, Date fechaFin, Long numero1, Long numero2,
			Long numeroEntre, String numero1Texto, String numero2Texto, String texto1, String texto2, String descripcion,
			String codigosTipoElemento/*Long tipoElementoId*/, String codigoContenedor, String codigoElemento, Long clienteEmpId, 
			String clasificaciones, Long seleccion, String listaIdElementosLectura,
			Integer numeroPagina, Integer tamanioPagina, String fieldOrder, String sortOrder, Boolean filtrarRefEnOperaciones, Integer cantExportar) {
		Session session = null;
		try{
			
			if(numeroPagina!=null && numeroPagina.longValue()>0 
    				&& tamanioPagina!=null && tamanioPagina.longValue()>0){
    			Integer paginaInicial = (numeroPagina - 1);
    			Integer filaDesde = tamanioPagina * paginaInicial;
    			Integer filaHasta = filaDesde + tamanioPagina;
    		}
			
			Integer top = 1500;
			if(cantExportar!=null)
				top = cantExportar;
//			String order = "";
//			if(sortOrder!=null && !"".equals(sortOrder) && fieldOrder!=null && !"".equals(fieldOrder)){
//				if("1".equals(sortOrder)){
//	    			order = fieldOrder;
//				}else if("2".equals(sortOrder)){
//					order = fieldOrder + " DESC";
//				}
//			}
			//obtenemos una sesi�n
			session = getSession();
			//tipoElemento.descripcion, elemento.codigo, ubicacion = calculada (dejar), referencia.descripcion, elemento.estado
			String consulta = "SELECT TOP "+top+" referencia.id, tipoElementos.descripcion as desc1, elementos.codigo, CAST(clasificacionDocumental.codigo as varchar(10)) + '_' + clasificacionDocumental.nombre as clasificacionDocumental, referencia.descripcion as desc2, elementos.estado " +
			", referencia.fecha1, referencia.fecha2, referencia.texto1, referencia.texto2 " +
			", referencia.numero1, referencia.numero2, referencia.descripcion_rearchivo, ee.codigo as codigoContenedor, ('E:'+epe.codigo+'('+cast(pe.posVertical as varchar(3))+','+CAST(pe.posHorizontal as varchar(3))+')') as posicionE " +
			", ('E:'+epc.codigo+'('+cast(pc.posVertical as varchar(3))+','+CAST(pc.posHorizontal as varchar(3))+')') as posicionC , loteReferencia.codigo as codigoR, referencia.ordenRearchivo, referencia.pathArchivoDigital, referencia.referencia_rearchivo_id" +
			/*", ROW_NUMBER() OVER (ORDER BY " + order + ") AS R"*/ ", pjur.razonSocial as Nombre , referencia.pathLegajo, referencia.fechaHora, "+
			
					  " CASE " + 
					  " WHEN referencia.elemento_id IN (select xo.elemento_id from x_operacion_elemento xo where xo.estado = 'Pendiente' and xo.elemento_id = referencia.elemento_id) "
					+ " THEN 'SI' "  
					+ " ELSE 'NO' "  
					+ " END as enReq, (pfis.nombre +' '+ pfis.apellido) as userAsig, referencia.descripcionTarea, ee.estado as estadoContenedor ,referencia.cImagenes " +
					
			" FROM referencia WITH (NOLOCK) INNER JOIN elementos on referencia.elemento_id = elementos.id " +
			" LEFT JOIN elementos ee on elementos.contenedor_id= ee.id " +
			" LEFT JOIN posiciones pe on elementos.posicion_id = pe.id " + 
			" LEFT JOIN estanterias epe on pe.estante_id = epe.id " +
			" LEFT JOIN posiciones pc on ee.posicion_id = pc.id " + 
			" LEFT JOIN estanterias epc on pc.estante_id = epc.id " +
			" LEFT JOIN tipoElementos on elementos.tipoElemento_id = tipoElementos.id " +
			" LEFT JOIN lotereferencia on referencia.lote_referencia_id = lotereferencia.id " +
			" INNER JOIN clientesEmp cem on lotereferencia.cliente_emp_id = cem.id " +
			" INNER JOIN personas_juridicas pjur on cem.razonSocial_id = pjur.id " +
			" LEFT JOIN users usr on referencia.codigoUsuario = usr.id " +
			" LEFT JOIN personas_fisicas pfis on usr.persona_id = pfis.id " +
			" LEFT JOIN clasificacionDocumental on referencia.clasificacion_documental_id = clasificacionDocumental.id " +
			" WHERE 1=1 ";
			
//			//Este filtro se usa cuando se buscan referencias desde un requerimiento, no se deberia usar desde "Buscar Referencias"
//			if(filtrarRefEnOperaciones!=null && filtrarRefEnOperaciones)
//				consulta+= " AND referencia.elemento_id not in (select xo.elemento_id from x_operacion_elemento xo where xo.estado = 'Pendiente' and xo.elemento_id = referencia.elemento_id) ";
			
			if(clienteAspId!=null)
				consulta += " AND (lotereferencia.cliente_asp_id = "+clienteAspId+") ";
			if(clienteEmpId!=null)
				consulta += " AND (lotereferencia.cliente_emp_id = "+clienteEmpId+") ";
			
			if(fecha1 != null)
				consulta += " AND (referencia.fecha1 = CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fecha1)+"',103)) ";
			if(fecha2 != null)
				consulta += " AND (referencia.fecha2 = CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fecha2)+"',103)) ";
			if(fechaEntre != null)
				consulta += " AND (CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fechaEntre)+ "',103) BETWEEN referencia.fecha1 AND referencia.fecha2) ";
			if(fechaInicio != null)
				consulta += " AND (lotereferencia.fecha_registro >= CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fechaInicio)+"',103)) ";
			if(fechaFin != null)
				consulta += " AND (lotereferencia.fecha_registro <= CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fechaFin)+"',103)) ";
			
			if((numero1 != null) || (numero1Texto != null && !"".equals(numero1Texto))){
				if(numero1Texto != null && !"".equals(numero1Texto)){
					consulta += " AND (CAST(referencia.numero1 AS varchar(30)) LIKE '"+numero1Texto+"') ";
				}else if(numero1 != null){
					consulta += " AND (referencia.numero1 = "+numero1+") ";
				}
			}
			if((numero2 != null) || (numero2Texto != null && !"".equals(numero2Texto))){
				if(numero2Texto != null && !"".equals(numero2Texto)){
					consulta += " AND (CAST(referencia.numero2 AS varchar(30)) LIKE '"+numero2Texto+"') ";
				}else if(numero2 != null){
					consulta += " AND (referencia.numero2 = "+numero2+") ";
				}
			}
			if(numeroEntre != null)
				consulta += " AND ("+numeroEntre+" BETWEEN referencia.numero1 AND referencia.numero2) ";
			
			if(texto1 != null && !"".equals(texto1)){
				if(texto1.contains("%"))
					consulta += " AND (referencia.texto1 LIKE '"+texto1+"') ";
				else
					consulta += " AND contains(referencia.texto1, '"+texto1+"') ";
			}
			if(texto2 != null && !"".equals(texto2)){
				if(texto2.contains("%"))
					consulta += " AND (referencia.texto2 LIKE '"+texto2+"') ";
				else
					consulta += " AND contains(referencia.texto2, '"+texto2+"') ";
			}
				
			if(descripcion != null && !"".equals(descripcion)){
				if(descripcion.contains("%"))
					consulta += " AND (referencia.descripcion LIKE '"+descripcion+"') ";
				else
					consulta += " AND contains(referencia.descripcion, '"+descripcion+"') ";
			}
				//			if(elementoId != null)
//				consulta += " AND (referencia.elemento_id = :pelementoId) ";
			if(listaIdElementosLectura!=null && !listaIdElementosLectura.equals(""))
				consulta += " AND (referencia.elemento_id IN ("+listaIdElementosLectura+")) ";
			if(codigoContenedor != null && !"".equals(codigoContenedor))
				consulta += " AND (ee.codigo = '"+codigoContenedor+"') ";
			if(codigoElemento != null && !"".equals(codigoElemento))
				consulta += " AND (elementos.codigo = '"+codigoElemento+"') ";
			/*if(tipoElementoId != null)
				consulta += " AND (tipoElementos.id = :ptipoElementoId) ";*/
			if(codigosTipoElemento != null && !"".equals(codigosTipoElemento))
				consulta += " AND (tipoElementos.codigo IN("+codigosTipoElemento+")) ";
			if(seleccion != null)
				consulta += " AND (referencia.indice_individual = "+seleccion+") ";
			if(clasificaciones != null && !"".equals(clasificaciones))
				consulta += " AND (referencia.clasificacion_documental_id IN("+clasificaciones+")) ";
			
//			if(numeroPagina!=null && numeroPagina.longValue()>0 
//    				&& tamanioPagina!=null && tamanioPagina.longValue()>0){
//    			Integer paginaInicial = (numeroPagina - 1);
//    			Integer filaDesde = tamanioPagina * paginaInicial;
//    			Integer filaHasta = filaDesde + tamanioPagina;
//    			filaDesde = filaDesde + 1;
//    			consulta = "SELECT * FROM (" + consulta + ") AS RESULT WHERE R BETWEEN "+filaDesde+" and "+ filaHasta;
//    		}
			
			String order = "";
			if(sortOrder!=null && !"".equals(sortOrder) && fieldOrder!=null && !"".equals(fieldOrder)){
				if("1".equals(sortOrder)){
	    			consulta += "ORDER BY "+ fieldOrder ;
				}else if("2".equals(sortOrder)){
					consulta += "ORDER BY "+ fieldOrder + " DESC";
				}
			}
			
			SQLQuery q = session.createSQLQuery(consulta);
//			if(fecha1 != null)
//				q.setDate("pfecha1", fecha1);
//			if(fecha2 != null)
//				q.setDate("pfecha2", fecha2);
//			if(fechaEntre != null)
//				q.setDate("pfecha3", fechaEntre);
//			if((numero1 != null) || (numero1Texto != null && !"".equals(numero1Texto))){
//				if(numero1Texto != null && !"".equals(numero1Texto)){
//					q.setString("pnumero1Texto", numero1Texto);
//				}else if(numero1 != null){
//				q.setLong("pnumero1", numero1);
//				}
//			}
//			if((numero2 != null) || (numero2Texto != null && !"".equals(numero2Texto))){
//				if(numero2Texto != null && !"".equals(numero2Texto)){
//					q.setString("pnumero2Texto", numero2Texto);
//				}else if(numero2 != null){
//				q.setLong("pnumero2", numero2);
//				}
//			}
//			if(numeroEntre!=null)
//				q.setLong("pnumero3", numeroEntre);
//			if(texto1 != null && !"".equals(texto1))	
//				q.setString("ptexto1", texto1);
//			if(texto2 != null && !"".equals(texto2))	
//				q.setString("ptexto2", texto2);
//			if(descripcion != null && !"".equals(descripcion))	
//				q.setString("pdescripcion", descripcion);
//			if(elementoId != null)	
//				q.setLong("pelementoId", elementoId);
//			if(codigoContenedor != null && !"".equals(codigoContenedor))
//				q.setString("pcodigoContenedor", ""+codigoContenedor+"");
//			if(codigoElemento != null && !"".equals(codigoElemento))	
//				q.setString("pcodigoElemento", "%"+codigoElemento+"%");
//			if(tipoElementoId != null)	
//				q.setLong("ptipoElementoId", tipoElementoId);
//			if(seleccion != null)
//				q.setLong("pseleccion", seleccion);
//			if(clasificaciones != null && !"".equals(clasificaciones))
//				q.setString("pclasificaciones", clasificaciones);
//			if(clienteEmpId != null)
//				q.setLong("pclienteEmpId", clienteEmpId);
//			if(clienteAspId != null)
//				q.setLong("pclienteAspId", clienteAspId);
			
			return q.list();
			
		}catch(Exception e){
			logger.error("no se pudo listar",e);
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
	 * Devuelve verdadero si el nodo ingresado como parametro y cualquiera de sus nodos hijos no contienen ninguna referencia.
	 * @return
	 */
	public Boolean verificarNodoYNodosHijosSinReferencias(ClasificacionDocumental nodo, ClienteAsp clienteAsp){
		Boolean result = false;
		
		Session session = null;
		try{
			if(nodo.getClienteAsp()==null || clienteAsp==null){
				throw new ClienteAspNullException();
			}
			if(!nodo.getClienteAsp().getId().equals(clienteAsp.getId())){
				throw new ClasificacionDocumentalClienteAspDistintoDeClienteAspUserException();
			}
			session = getSession();
			Set<ClasificacionDocumental> nodosHijos = nodo.getListaCompletaHijos();
			LinkedList<Long> ids = new LinkedList<Long>();
			ids.add(nodo.getId());
			for(ClasificacionDocumental cd : nodosHijos){
				ids.add(cd.getId());
			}
			
			Criteria crit = session.createCriteria(getClaseModelo());
			crit.setProjection(Projections.rowCount());
			crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			crit.createCriteria("clasificacionDocumental", "cd");
			crit.add(Restrictions.eq("cd.clienteAsp", clienteAsp));
			crit.add(Restrictions.in("cd.id", ids));
			Integer cantidadReferencias = (Integer)crit.uniqueResult();
			result = cantidadReferencias.equals(0);
		} catch (HibernateException hibernateException) {
			logger.error("No se pudo verificar nodo sin referencias ",
					hibernateException);
			result = false;
		}catch(BasaException e){
			logger.error("No se pudo verificar nodo sin referencias ",
					e);
			result = false;
		}finally {
		
			try {
				session.close();
			} catch (Exception e) {
				logger.error("No se pudo cerrar la sesi�n", e);
			}
		}
		
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Integer contarElementoFiltradas(Long clienteAspId, Date fecha1, Date fecha2, Date fechaEntre, Date fechaInicio, Date fechaFin, 
			Long numero1, Long numero2,
			Long numeroEntre, String numero1Texto, String numero2Texto, String texto1, String texto2, String descripcion,
			String codigosTipoElemento/*Long tipoElementoId*/, String codigoContenedor, String codigoElemento, Long clienteEmpId,
			String clasificaciones, Long seleccion, String listaIdElementosLectura){
		Session session = null;
		
        try {
        	//obtenemos una sesi�n
			session = getSession();
			//tipoElemento.descripcion, elemento.codigo, ubicacion = calculada (dejar), referencia.descripcion, elemento.estado
//			String consulta = "SELECT count(referencia.id) " +
//			" FROM referencia WITH (NOLOCK) INNER JOIN elementos on referencia.elemento_id = elementos.id " +
//			" LEFT JOIN elementos ee on elementos.contenedor_id= ee.id " +
//			" LEFT JOIN posiciones pe on elementos.posicion_id = pe.id " + 
//			" LEFT JOIN estanterias epe on pe.estante_id = epe.id " +
//			" LEFT JOIN posiciones pc on ee.posicion_id = pc.id " + 
//			" LEFT JOIN estanterias epc on pc.estante_id = epc.id " +
//			" LEFT JOIN tipoElementos on elementos.tipoElemento_id = tipoElementos.id " +
//			" LEFT JOIN loteReferencia on referencia.lote_referencia_id = loteReferencia.id " +
//			" INNER JOIN clientesEmp cem on lotereferencia.cliente_emp_id = cem.id " +
//			" INNER JOIN personas_juridicas pjur on cem.razonSocial_id = pjur.id " +
//			" LEFT JOIN clasificacionDocumental on referencia.clasificacion_documental_id = clasificacionDocumental.id " +
//			" WHERE referencia.elemento_id not in (select xo.elemento_id from x_operacion_elemento xo where xo.estado = 'Pendiente' and xo.elemento_id = referencia.elemento_id) ";
			
			String consulta = "SELECT count(*) " +
			" FROM referencia WITH (NOLOCK) INNER JOIN elementos on referencia.elemento_id = elementos.id " +
			" LEFT JOIN elementos ee on elementos.contenedor_id= ee.id " +
			" LEFT JOIN posiciones pe on elementos.posicion_id = pe.id " + 
			" LEFT JOIN estanterias epe on pe.estante_id = epe.id " +
			" LEFT JOIN posiciones pc on ee.posicion_id = pc.id " + 
			" LEFT JOIN estanterias epc on pc.estante_id = epc.id " +
			" LEFT JOIN tipoElementos on elementos.tipoElemento_id = tipoElementos.id " +
			" LEFT JOIN lotereferencia on referencia.lote_referencia_id = lotereferencia.id " +
			" INNER JOIN clientesEmp cem on lotereferencia.cliente_emp_id = cem.id " +
			" INNER JOIN personas_juridicas pjur on cem.razonSocial_id = pjur.id " +
			" LEFT JOIN clasificacionDocumental on referencia.clasificacion_documental_id = clasificacionDocumental.id " +
			" WHERE 1=1 ";
			
			if(clienteAspId!=null)
				consulta += " AND (lotereferencia.cliente_asp_id = "+clienteAspId+") ";
			if(clienteEmpId!=null)
				consulta += " AND (lotereferencia.cliente_emp_id = "+clienteEmpId+") ";
			
			if(fecha1 != null)
				consulta += " AND (referencia.fecha1 = CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fecha1)+"',103)) ";
			if(fecha2 != null)
				consulta += " AND (referencia.fecha2 = CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fecha2)+"',103)) ";
			if(fechaEntre != null)
				consulta += " AND (CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fechaEntre)+ "',103) BETWEEN referencia.fecha1 AND referencia.fecha2) ";
			if(fechaInicio != null)
				consulta += " AND (lotereferencia.fecha_registro >= CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fechaInicio)+"',103)) ";
			if(fechaFin != null)
				consulta += " AND (lotereferencia.fecha_registro <= CONVERT(DATETIME, '"+Configuracion.formatoFechaFormularios.format(fechaFin)+"',103)) ";
			
			if((numero1 != null) || (numero1Texto != null && !"".equals(numero1Texto))){
				if(numero1Texto != null && !"".equals(numero1Texto)){
					consulta += " AND (CAST(referencia.numero1 AS varchar(30)) LIKE '"+numero1Texto+"')";
				}else if(numero1 != null){
					consulta += " AND (referencia.numero1 = "+numero1+") ";
				}
			}
			if((numero2 != null) || (numero2Texto != null && !"".equals(numero2Texto))){
				if(numero2Texto != null && !"".equals(numero2Texto)){
					consulta += " AND (CAST(referencia.numero2 AS varchar(30)) LIKE '"+numero2Texto+"')";
				}else if(numero2 != null){
					consulta += " AND (referencia.numero2 = "+numero2+") ";
				}
			}
			if(numeroEntre != null)
				consulta += " AND ("+numeroEntre+" BETWEEN referencia.numero1 AND referencia.numero2) ";
			
			if(texto1 != null && !"".equals(texto1)){
				if(texto1.contains("%"))
					consulta += " AND (referencia.texto1 LIKE '"+texto1+"') ";
				else
					consulta += " AND contains(referencia.texto1, '"+texto1+"') ";
			}
			if(texto2 != null && !"".equals(texto2)){
				if(texto2.contains("%"))
					consulta += " AND (referencia.texto2 LIKE '"+texto2+"') ";
				else
					consulta += " AND contains(referencia.texto2, '"+texto2+"') ";
			}
				
			if(descripcion != null && !"".equals(descripcion)){
				if(descripcion.contains("%"))
					consulta += " AND (referencia.descripcion LIKE '"+descripcion+"') ";
				else
					consulta += " AND contains(referencia.descripcion, '"+descripcion+"') ";
			}
				//			if(elementoId != null)
//				consulta += " AND (referencia.elemento_id = :pelementoId) ";
			if(listaIdElementosLectura!=null && !listaIdElementosLectura.equals(""))
				consulta += " AND (referencia.elemento_id IN ("+listaIdElementosLectura+")) ";
			if(codigoContenedor != null && !"".equals(codigoContenedor))
				consulta += " AND (ee.codigo = '"+codigoContenedor+"') ";
			if(codigoElemento != null && !"".equals(codigoElemento))
				consulta += " AND (elementos.codigo = '"+codigoElemento+"') ";
			/*if(tipoElementoId != null)
				consulta += " AND (tipoElementos.id = :ptipoElementoId) ";*/
			if(codigosTipoElemento != null && !"".equals(codigosTipoElemento))
				consulta += " AND (tipoElementos.codigo IN("+codigosTipoElemento+")) ";
			if(seleccion != null)
				consulta += " AND (referencia.indice_individual = "+seleccion+") ";
			if(clasificaciones != null && !"".equals(clasificaciones))
				consulta += " AND (referencia.clasificacion_documental_id IN("+clasificaciones+")) ";
			
			
			SQLQuery q = session.createSQLQuery(consulta);

//List<Object> result = q.list();
return (Integer) q.list().get(0); 

			//return (Integer)q.uniqueResult();

			
			
			
//			if(salida!=null && salida.size()>0){
//				for(Object ob:salida){
//					return Integer.parseInt(ob.toString());
//				}
//				
//			} 
 
			
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
	public List listarDescripcionesPopup(
			Integer codigoClasificacionDocumental, String codigoClienteEmp,String descripcion,
			ClienteAsp clienteAsp) {
	
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
			String consulta = "SELECT referencia.descripcion as descripcion, count(*) as contador " +
			" FROM referencia " +
			" LEFT JOIN loteReferencia on referencia.lote_referencia_id = loteReferencia.id " +
			" LEFT JOIN clientesEmp on loteReferencia.cliente_emp_id = clientesEmp.id " +
			" LEFT JOIN clasificacionDocumental on referencia.clasificacion_documental_id = clasificacionDocumental.id " +
			" WHERE (loteReferencia.cliente_asp_id = :pClienteAspId) ";
			if(codigoClasificacionDocumental!=null)
				consulta += " AND (clasificacionDocumental.codigo = :pCodigo) ";
			if(codigoClienteEmp!=null){
				consulta += " AND (clientesEmp.codigo = :pClienteEmp) ";		
			}
			if(descripcion!=null){
				consulta += " AND (referencia.descripcion LIKE :pDescripcion) ";				
			}
			consulta+=" GROUP BY referencia.descripcion ORDER BY contador desc";
			SQLQuery q = session.createSQLQuery(consulta)
				.addScalar("descripcion", Hibernate.STRING);
			q.setLong("pClienteAspId",clienteAsp.getId());
			if(codigoClasificacionDocumental!=null)
				q.setInteger("pCodigo",codigoClasificacionDocumental);
			if(codigoClienteEmp!=null){
				q.setString("pClienteEmp", codigoClienteEmp);
			}
			if(descripcion != null){
				q.setString("pDescripcion", "%"+descripcion+"%");
			}
			
        	List<String> result = q.list();
        	List<Map<String,String>> retorno = new ArrayList<Map<String,String>>();
        	for(String res : result){
        		Map<String,String> item = new HashMap<String, String>();
        		item.put("descripcion", res);
        		retorno.add(item);
        	}
        	return retorno;
        } catch (Exception exception) {
        	logger.error("No se pudo listar ", exception);
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
	public Referencia obtenerParaRearchivo(ClienteEmp clienteEmp, ClasificacionDocumental clasificacionDocumental, Long numero1) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			List<Referencia> salida = null;
	        Criteria crit = session.createCriteria(getClaseModelo());
	        crit.add(Restrictions.eq("clasificacionDocumental", clasificacionDocumental));
	        

	        crit.createCriteria("loteReferencia", "lote");
	        crit.add(Restrictions.eq("lote.clienteEmp", clienteEmp));
	        
	        crit.add(Restrictions.isNull("referenciaRearchivo"));
	        
	        crit.add(Restrictions.sqlRestriction(numero1 +" BETWEEN {alias}.numero1 and {alias}.numero2"));
	        
	        crit.addOrder(Order.desc("indiceIndividual"));

	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        salida = crit.list();
	        if(salida!=null && salida.size()>0)
	        	return salida.get(0);
	        else
	        	return null;
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
	public List<Referencia> obtenerByElementoContenedor(Elemento contenedor) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
//			Criteria crit = session.createCriteria(getClaseModelo());
//	        crit.createCriteria("elemento","elemento");
//	        crit.add(Restrictions.or(Restrictions.eq("elemento", contenedor),Restrictions.eq("elemento.contenedor", contenedor)));
//	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
	        String consulta = "SELECT distinct re From Referencia re where re.elemento.id = "+contenedor.getId().longValue()+" "+
	        		" OR re.elemento.contenedor.id = "+contenedor.getId().longValue();
	        
	        List<Referencia> lista = (List<Referencia>)session.createQuery(consulta).list();
	        return lista;
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
	public Long obtenerCantidadByElementoContenedor(String codigoContenedor) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
//			Criteria crit = session.createCriteria(getClaseModelo());
//	        crit.createCriteria("elemento","elemento");
//	        crit.add(Restrictions.or(Restrictions.eq("elemento", contenedor),Restrictions.eq("elemento.contenedor", contenedor)));
//	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
//	        String consulta = "SELECT distinct count(re.id) From Referencia re where re.elemento.id = "+contenedor.getId().longValue()+" "+
//	        		" OR re.elemento.contenedor.id = "+contenedor.getId().longValue();
	        
	        String consulta = "select top 1 codigo from elementos e" + 
	        		" inner join referencia r on e.id = r.elemento_id" + 
	        		" where e.codigo = '"+ codigoContenedor +"'" + 
	        		" or contenedor_id in ( select id from elementos where codigo = '"+ codigoContenedor +"')";
	        	       
	       SQLQuery q = session.createSQLQuery(consulta);
	       
	       List lista = q.list();
	       
	       if(lista!=null && lista.size()>0)
	    	   return 1L;
	       else
	    	   return 0L;
	        
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
	public Referencia obtenerByElemento(Elemento elemento) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			
			String consulta = "Select re From Referencia re where re.elemento.id = "+elemento.getId().longValue()+"";
	        
			Referencia refe = (Referencia) session.createQuery(consulta).uniqueResult();
			
	        return refe;
	        
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
	public List<Referencia> listarByCodigoUsuario(Long idUsuario) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			
			Criteria crit = session.createCriteria(getClaseModelo());
	        
			crit.add(Restrictions.and(Restrictions.isNotNull("codigoUsuario"),
					Restrictions.eq("codigoUsuario", idUsuario)));
	        
	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
	        return (List<Referencia>) crit.list();
	        
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
	public Referencia obtenerByCodigoElemento(String codigoElemento) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			
			Criteria crit = session.createCriteria(getClaseModelo());
	        
			crit.add(Restrictions.isNotNull("loteReferencia"));
			crit.createCriteria("elemento","ele");
	        crit.add(Restrictions.eq("ele.codigo", codigoElemento));
	        
	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
	        return (Referencia) crit.uniqueResult();
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
	public Referencia obtenerByIDElemento(Elemento elemento) {
		Session session = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			
			Criteria crit = session.createCriteria(getClaseModelo());
	        
			crit.add(Restrictions.isNotNull("loteReferencia"));
	        crit.add(Property.forName("elemento").eq(elemento));
	        
	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
	        return (Referencia) crit.uniqueResult();
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
	public List<Referencia> getListaByRangoElemento(Long idElementoDesde, Long idElementoHasta) {
		Session session = null;
		List<Referencia> listaReferencias = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			
			Criteria crit = session.createCriteria(getClaseModelo());
	        crit.createCriteria("elemento","ele");
	        
	        if(idElementoDesde!=null)
	        	crit.add(Restrictions.ge("ele.id", idElementoDesde));
	        if(idElementoHasta!=null)
	        	crit.add(Restrictions.le("ele.id", idElementoHasta));
	        
	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
	        listaReferencias = crit.list();
	        
	        return listaReferencias;
	        
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
	public List<Referencia> getListaReferenciasByListaIDsElementos(List<Long> listaIDsElementos) {
		Session session = null;
		List<Referencia> listaReferencias = null;
	    try {
	    	//obtenemos una sesi�n
			session = getSession();
			
			Criteria crit = session.createCriteria(getClaseModelo());
	        crit.createCriteria("elemento","ele");
	        
	        crit.add(Restrictions.in("ele.id", listaIDsElementos));
	        
	        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	        
	        listaReferencias = crit.list();
	        
	        return listaReferencias;
	        
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
	public List<Referencia> listarReferenciaPorLote(LoteReferencia loteReferencia, ClienteAsp cliente){
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
//        	Criteria crit = session.createCriteria(getClaseModelo());
//        	crit.createCriteria("loteReferencia", "loteReferencia");
//        	crit.createCriteria("elemento", "elemento");
        	if(loteReferencia!=null && cliente != null){
//        		crit.add(Restrictions.eq("loteReferencia", loteReferencia));
//        		crit.add(Restrictions.eq("loteReferencia.clienteAsp", cliente));
//        		
//        		crit.addOrder(Order.asc("elemento.codigo"));
//            	crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			
        		String consulta = "SELECT DISTINCT r FROM Referencia r where r.loteReferencia.id = "+ loteReferencia.getId().longValue() +
        						  " and r.loteReferencia.clienteAsp.id = "+cliente.getId().longValue() + " order by r.ordenRearchivo asc" ;

        		Query query = session.createQuery(consulta);
        		
            	return (List<Referencia>)query.list();
        	}
        	else
        		return new ArrayList<Referencia>();
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
	public List<Map<String, Object>> listarPorClasificacionDocumental(
			ClasificacionDocumental clasificacion,
			List<Long> tiposElemento) {
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
			
			String consulta = "select tipoElementos.descripcion as descTipoElemento,"
				+ " elementos.codigo as codigoElemento,"
				+ " fecha1, fecha2, numero1, numero2, texto1, texto2"
				+ " from referencia as referencia"
				+ " inner join elementos on referencia.elemento_id = elementos.id"
				+ " inner join tipoElementos on elementos.tipoElemento_id = tipoElementos.id"
				+ " where clasificacion_documental_id = :clasificacionDocumental"
				+ " and tipoElementos.id in (:tiposElemento)"
				+ " order by tipoElementos.descripcion, elementos.codigo";
			
			SQLQuery q = session.createSQLQuery(consulta)
			.addScalar("descTipoElemento", Hibernate.STRING)
			.addScalar("codigoElemento", Hibernate.STRING)
			.addScalar("fecha1", Hibernate.DATE)
			.addScalar("fecha2", Hibernate.DATE)
			.addScalar("numero1", Hibernate.STRING)
			.addScalar("numero2", Hibernate.STRING)
			.addScalar("texto1", Hibernate.STRING)
			.addScalar("texto2", Hibernate.STRING);
					
			q.setLong("clasificacionDocumental", clasificacion.getId())
			.setParameterList("tiposElemento", tiposElemento);
			
			q.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			
			List l = q.list();
			return l;
        	
        } catch (HibernateException hibernateException) {
        	logger.error("No se pudo listar ", hibernateException);
        	return new ArrayList<Map<String,Object>>();
        }finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}
	
	@Override
	public List<Map<String, Object>> listarPorClasificacionDocumental(Collection<Long> idsClasificaciones, List<Long> tiposElemento) {
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
			
			String consulta = "select clasificacion_documental_id as idClasificacion,"
				+ " tipoElementos.descripcion as descTipoElemento,"
				+ " elementos.codigo as codigoElemento,"
				+ " fecha1, fecha2, numero1, numero2, texto1, texto2"
				+ " from referencia as referencia"
				+ " inner join elementos on referencia.elemento_id = elementos.id"
				+ " inner join tipoElementos on elementos.tipoElemento_id = tipoElementos.id"
				+ " where clasificacion_documental_id in (:clasificacionesDocumentales)"
				+ " and tipoElementos.id in (:tiposElemento)"
				+ " order by tipoElementos.descripcion, elementos.codigo";
			
			SQLQuery q = session.createSQLQuery(consulta)
			.addScalar("idClasificacion",Hibernate.LONG)
			.addScalar("descTipoElemento", Hibernate.STRING)
			.addScalar("codigoElemento", Hibernate.STRING)
			.addScalar("fecha1", Hibernate.DATE)
			.addScalar("fecha2", Hibernate.DATE)
			.addScalar("numero1", Hibernate.STRING)
			.addScalar("numero2", Hibernate.STRING)
			.addScalar("texto1", Hibernate.STRING)
			.addScalar("texto2", Hibernate.STRING);
					
			q.setParameterList("clasificacionesDocumentales", idsClasificaciones)
			.setParameterList("tiposElemento", tiposElemento);
			
			q.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			
			List l = q.list();
			return l;
        	
        } catch (HibernateException hibernateException) {
        	logger.error("No se pudo listar ", hibernateException);
        	return new ArrayList<Map<String,Object>>();
        }finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
	}
	
	@Override
	public List<Referencia> listarReferenciasPorIds(List<Long> idReferencias){
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
        	Criteria crit = session.createCriteria(getClaseModelo());
        	
        	crit.add(Restrictions.in("id", idReferencias));

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
	
	@Override
	public List<Referencia> traerReferenciasPorIdsPorSQL(String idReferencias){
		Session session=null;
		
		try{
			session = getSession();
           	
        	String consulta = "select distinct r.* " +
				" from referencia r " +
				" where  r.id IN ("+idReferencias+")"; 
        	
        		
        	SQLQuery q = session.createSQLQuery(consulta).addEntity(Referencia.class);			
			
			return (List<Referencia>)q.list();
			
		}
		catch(HibernateException hibernateException){
			logger.error("No se pudo listar",hibernateException);
			return null;
		}finally{
			try{
				session.close();
			}catch(Exception e){
				logger.error("No se pudo cerrar la sesi�n",e);
			}
		}
		
	}
	
	@Override
	public List<Object> traerObjectsPorIdsPorSQL(String idReferencias){
		Session session=null;
		
		try{
			session = getSession();
           	
        	String consulta = "select distinct r.*, cl.nombre, e.codigo, con.codigo as codigo2, loteReferencia.codigo as codigo3" +
				" from referencia r " +
				" left join clasificacionDocumental cl on cl.id = r.clasificacion_documental_id " +
                " left join elementos e on r.elemento_id = e.id " + 
                " left join elementos con on e.contenedor_id = con.id " +
                " LEFT JOIN loteReferencia on r.lote_referencia_id = loteReferencia.id " +
				" where  r.id IN ("+idReferencias+")"; 
        	
        		
        	SQLQuery q = session.createSQLQuery(consulta);		
			
			return (List<Object>)q.list();
			
		}
		catch(HibernateException hibernateException){
			logger.error("No se pudo listar",hibernateException);
			return null;
		}finally{
			try{
				session.close();
			}catch(Exception e){
				logger.error("No se pudo cerrar la sesi�n",e);
			}
		}
	}
	
	@Override
	public List traerReferenciasPorLote(Long id){
		Session session=null;
		
		try{
			session = getSession();
           	
        	String consulta = "select * from referencia where lote_referencia_id = "+id+""; 
        	
        		
        	SQLQuery q = session.createSQLQuery(consulta).addEntity(Referencia.class);		
			
			return q.list();
			
		}
		catch(HibernateException hibernateException){
			logger.error("No se pudo listar",hibernateException);
			return null;
		}finally{
			try{
				session.close();
			}catch(Exception e){
				logger.error("No se pudo cerrar la sesi�n",e);
			}
		}
	}
	
	@Override
	public Integer cantReferenciasExistenPorElementos(String idReferencias, Long idClienteAsp){
		
		Session session=null;
		try{
			session = getSession();
           	
        	String consulta = "select distinct count (r.id) " +
				" from referencia r " +
				" left join lotereferencia lote on lote.id = r.lote_referencia_id " +
				" where r.elemento_id IN ("+idReferencias+")" +
				" and r.lote_referencia_id is not NULL " +
				" and lote.cliente_asp_id = "+idClienteAsp+" ";
        	
        		
        	SQLQuery q = session.createSQLQuery(consulta);		
			
        	List salida = q.list();
			if(salida!=null && salida.size()>0){
				for(Object ob:salida){
					return Integer.parseInt(ob.toString());
				}
			}
				
        	return new Integer(0);
		}
		catch(HibernateException hibernateException){
			logger.error("No se pudo listar",hibernateException);
			return null;
		}finally{
			try{
				session.close();
			}catch(Exception e){
				logger.error("No se pudo cerrar la sesi�n",e);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Integer contarLotesEstadoFiltrados(Long clienteAspId, Date fechaDesde, Date fechaHasta, Long codigoLoteDesde, Long codigoLoteHasta,
			String codigoContDesde, String codigoContHasta, Boolean cerrado, String clienteEmpId){
		Session session = null;
		
        try {
        	//obtenemos una sesi�n
			session = getSession();
			//tipoElemento.descripcion, elemento.codigo, ubicacion = calculada (dejar), referencia.descripcion, elemento.estado
			String consulta = "Select COUNT(lotes.lote_referencia_id) "+
				" from (SELECT distinct referencia.lote_referencia_id, max(elemCont.codigo_cont) as codCont, max(elemCont.cerrado) as estCerrado, max(lote.fecha_registro) as fecha, max(lote.codigo) as codigoLote "+
				" FROM referencia inner join (select e.id , e.codigo, e.clienteAsp_id, e.clienteEmp_id, ee.codigo as codigo_cont, ee.cerrado "+
				" from elementos e "+
				" inner join elementos ee on e.contenedor_id = ee.id "+
				" UNION "+
				" select e.id, e.codigo, e.clienteAsp_id, e.clienteEmp_id, e.codigo as codigo_cont, e.cerrado "+
				" from elementos e, tipoElementos te "+
				" where e.tipoElemento_id = te.id "+
				" and te.contenedor = 1) as elemCont on referencia.elemento_id = elemCont.id "+
				" left join lotereferencia lote on  lote.id = referencia.lote_referencia_id "+
				" where 1 = 1 ";
				
			
			consulta += " AND (elemCont.cerrado == :ptexto2) ";
			
			if(clienteAspId!=null)
				consulta += " AND (loteReferencia.cliente_asp_id = :pclienteAspId) ";
			if(clienteEmpId!=null && !"".equals(clienteEmpId))
				consulta += " AND (loteReferencia.cliente_emp_id = :pclienteEmpId) ";
			if(fechaDesde != null)
				consulta += " AND (loteReferencia.fecha_registro = :pfecha1) ";
			if(fechaHasta != null)
				consulta += " AND (loteReferencia.fecha_registro = :pfecha2) ";
			
			if(codigoLoteDesde != null)
				consulta += " AND (loteReferencia.codigo >= :pcodigoDesde) ";
			if(codigoLoteHasta != null)
				consulta += " AND (loteReferencia.codigo <= :pcodigoHasta) ";
			
			
			if(codigoContDesde != null && !"".equals(codigoContDesde))
				consulta += " AND (ee.codigo >= :ptexto2) ";
			if(codigoContHasta != null && !"".equals(codigoContHasta))
				consulta += " AND (ee.codigo <= :ptexto1) ";
				
			consulta+= " group by referencia.lote_referencia_id) as lotes ";
			
			SQLQuery q = session.createSQLQuery(consulta);
			
			q.setBoolean("pcerrado", cerrado);
			
			if(fechaDesde != null)
				q.setDate("pfecha1", fechaDesde);
			if(fechaHasta != null)
				q.setDate("pfecha2", fechaHasta);
			if(codigoLoteDesde != null){
				q.setLong("pnumero1", codigoLoteDesde);
			}
			if(codigoLoteHasta != null){
				q.setLong("pnumero2", codigoLoteHasta);
			}
			if(codigoContDesde != null && !"".equals(codigoContDesde))	
				q.setString("ptexto1", codigoContDesde);
			if(codigoContHasta != null && !"".equals(codigoContHasta))	
				q.setString("ptexto2", codigoContHasta);
			
			
			
			if(clienteEmpId!=null & !"".equals(clienteEmpId))
				q.setString("pclienteEmpId", clienteEmpId);
			if(clienteAspId!=null)
				q.setLong("pclienteAspId", clienteAspId);
			
			List salida = q.list();
			if(salida!=null && salida.size()>0){
				for(Object ob:salida){
					return Integer.parseInt(ob.toString());
				}
			}
				
        	return new Integer(0);
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
}
