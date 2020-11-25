package com.security.accesoDatos.hibernate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.security.accesoDatos.interfaz.GroupService;
import com.security.modelo.administracion.ClienteAsp;
import com.security.modelo.seguridad.Group;

/**
 * 
 * @author Gabriel Mainero
 * @modificado Ezequiel Beccaria: Se agrego un nuevo metodo ("listarPorGroupNameEq(String groupName)")
 * @modificado Todos las consultas deben filtrarse por cliente (obligatoriamente)
 * 				Si el cliente=null -> objetos pertenecientes al usuarios asp_admin
 * 
 */
@Component
public class GroupServiceImp extends GestorHibernate<Group> implements GroupService {
	private static Logger logger=Logger.getLogger(GroupServiceImp.class);

	@Autowired
	public GroupServiceImp(HibernateControl hibernateControl) {
		super(hibernateControl);
	}

	@Override
	public Class<Group> getClaseModelo() {
		return Group.class;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> listarTodosGroupFiltrados(Group group, ClienteAsp cliente)
    {
		Session session = null;
        try {
        	//obtenemos una sesi�n
			session = getSession();
        	Criteria crit = session.createCriteria(getClaseModelo());
        	//filtro por group
        	if(group!=null && group.getGroupName()!= null && !"".equals(group.getGroupName()))
        		crit.add(Restrictions.ilike("groupName", group.getGroupName() + "%"));	
        	//filtro por cliente
        	if(cliente != null)
        		crit.add(Restrictions.eq("cliente", cliente));
        	else 
        		crit.add(Restrictions.isNull("cliente"));
        	//criterios de ordenaci�n
        	crit.addOrder(Order.asc("groupName"));
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
	
	/**
	 * Guarda un nuevo grupo en la base de datos y sus asignaciones de privilegios 
	 * @param objeto
	 */
	public Boolean save(Group group){
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			//guardamos el objeto
			session.save(group);//			
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "no se pudo guardar");	
			return false;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
    }
	
	/**
	 * Actualiza un nuevo grupo en la base de datos y sus asignaciones de privilegios 
	 * @param objeto
	 */
	public Boolean update(Group group){
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos la transacci�n
			tx = session.getTransaction();
			tx.begin();
			//actualizamod el objeto
			session.update(group);
			//hacemos commit a la transacci�n para que 
			//se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "no se pudo actualizar");	
			return false;
		}finally{
        	try{
        		session.close();
        	}catch(Exception e){
        		logger.error("No se pudo cerrar la sesi�n", e);
        	}
        }
    }
	
	/**
	 * Eliminar un grupos y sus privilegios
	 * @param clase
	 * @param id
	 */
	public Boolean delete(Group group){
		Session session = null;
		Transaction tx = null;
		try {
			//obtenemos una sesi�n
			session = getSession();
			//creamos una nueva transacci�n
			tx = session.getTransaction();
			tx.begin();
			//Eliminamos el objeto
			session.delete(group);
			//hacemos commit a los cambios para que se refresque la base de datos.
			tx.commit();
			return true;
		} 
		catch (RuntimeException e) {
			rollback(tx, e, "No se pudo eliminar");
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
	public List<Group> listarPorId(Long[] ids, ClienteAsp cliente) {
		Session session = null; 
		try {
			//obtenemos una sesi�n
			session = getSession();
			Criteria crit = session.createCriteria(getClaseModelo());
			//Filtro por groupNames
			crit.add(Restrictions.in("id", ids));
			//Filtro por cliente
        	if(cliente != null)
        		crit.add(Restrictions.eq("cliente", cliente));
        	else 
        		crit.add(Restrictions.isNull("cliente"));
			return crit.list();
		} catch (HibernateException e) {
			logger.error("no se pudo obtener el usuario por nombre",e);
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
