package com.dividato.configuracionGeneral.tasks.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clase de utilidad para las tareas programadas (scheduled-context.xml).<br/>
 * Esta clase cuenta con algunos m�todos comunes de todas las tareas,
 * como por ejemplo, la configuraci�n del path de base para todos los archivos de IN/OUT.
 * 
 * @author Victor Kenis
 *
 */
public class Commons {

	private String basePath="";
	
	/**
	 * Constructor.
	 */
	public Commons(){
	}
	/**
	 * Analiza si un path es un directorio accesible 
	 * @param pathStr
	 * @throws ScheduledTaskException
	 */
	public void validate(String pathStr) throws ScheduledTaskException {
		try{
			File path = new File(pathStr);
			if (!path.exists() || !path.isDirectory())
			{
				throw new ScheduledTaskException("file.error.path");
			}
			if(!path.canRead() || !path.canWrite()){
				throw new ScheduledTaskException("file.error.path");
			}
		}catch(SecurityException e){
			throw new ScheduledTaskException("file.error.path");
		}
	}

	
	/**
	 * Retorna el valor en Long. 
	 * Si es requerido y no puede parsear el string
	 * entonces retorna una excepci�n con el mensaje y la data de los par�metros.
	 * Si no es requerido no se lanza ninguna excepci�n. 
	 * @param string valor a parsear
	 * @param requerido si es requerido y no se puede parsear, entonces se lanza una excepci�n
	 * @param message mensaje de la excepci�n.
	 * @param exData datos extras de la excepci�n
	 * @return el valor string como Long o excepci�n
	 * @throws ScheduledTaskException
	 */
	public Long parseLong(String string,boolean requerido,String message,Long ... exData) throws ScheduledTaskException {
		try{
			return new Long(string);
		}catch(Exception e){
			if(requerido){
				throw new ScheduledTaskException(message,exData);
			}
		}
		return null;
	}
	/**
	 * Retorna el valor en Integer. 
	 * Si es requerido y no puede parsear el string
	 * entonces retorna una excepci�n con el mensaje y la data de los par�metros.
	 * Si no es requerido no se lanza ninguna excepci�n. 
	 * @param string valor a parsear
	 * @param requerido si es requerido y no se puede parsear, entonces se lanza una excepci�n
	 * @param message mensaje de la excepci�n.
	 * @param exData datos extras de la excepci�n
	 * @return el valor string como Integer o excepci�n
	 * @throws ScheduledTaskException
	 */
	public Integer parseInteger(String string, boolean requerido, String message,Long ... exData) throws ScheduledTaskException {
		try{
			return new Integer(string);
		}catch(Exception e){
			if(requerido)
				throw new ScheduledTaskException(message,exData);
		}
		return null;
	}
	/**
	 * Retorna el valor en Date. 
	 * Si es requerido y no puede parsear el string
	 * entonces retorna una excepci�n con el mensaje y la data de los par�metros.
	 * Si no es requerido no se lanza ninguna excepci�n.
	 * @param sdf formato esperado 
	 * @param string valor a parsear
	 * @param requerido si es requerido y no se puede parsear, entonces se lanza una excepci�n
	 * @param message mensaje de la excepci�n.
	 * @param exData datos extras de la excepci�n
	 * @return el valor string como Date o excepci�n
	 * @throws ScheduledTaskException
	 */
	public Date parseDate(SimpleDateFormat sdf, String string,boolean requerido, String message,Long ... exData) throws ScheduledTaskException {
		try{
			return sdf.parse(string);
		}catch(Exception e){
			if(requerido)
				throw new ScheduledTaskException(message,exData);
		}
		return null;
	}
	/**
	 * Retorna el valor en Double. 
	 * Si es requerido y no puede parsear el string
	 * entonces retorna una excepci�n con el mensaje y la data de los par�metros.
	 * Si no es requerido no se lanza ninguna excepci�n. 
	 * @param string valor a parsear
	 * @param requerido si es requerido y no se puede parsear, entonces se lanza una excepci�n
	 * @param message mensaje de la excepci�n.
	 * @param exData datos extras de la excepci�n
	 * @return el valor string como Double o excepci�n
	 * @throws ScheduledTaskException
	 */
	public Double parseDouble(String string, boolean requerido, String message,Long ... exData) throws ScheduledTaskException {
		try{
			return new Double(string);
		}catch(Exception e){
			if(requerido)
				throw new ScheduledTaskException(message,exData);
		}
		return null;
	}
	/**
	 * Retorna el mismo string pero con trim.
	 * Si es requerido y es nulo o vac�o lanza una excepci�n.
	 * @param string valor a parsear
	 * @param requerido si es requerido y no se puede parsear, entonces se lanza una excepci�n
	 * @param message mensaje de la excepci�n.
	 * @param exData datos extras de la excepci�n
	 * @return el string nulo o con trim, o excepci�n si es requerido y viene nulo o vac�o.
	 * @throws ScheduledTaskException
	 */
	public String getString(String string, boolean requerido, String message,Long ... exData) throws ScheduledTaskException {
		if(string!=null) string = string.trim();
		if(requerido)
			if(string==null || string.equals(""))
				throw new ScheduledTaskException(message,exData);
		return string;
	}
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	public String getBasePath() {
		return basePath;
	}
	
}
