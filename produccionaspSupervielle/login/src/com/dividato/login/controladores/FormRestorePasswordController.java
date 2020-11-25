package com.dividato.login.controladores;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dividato.login.validadores.RestorePasswordValidator;
import com.security.accesoDatos.interfaz.UserService;
import com.security.modelo.seguridad.User;
import com.security.recursos.RecursosPassword;
import com.security.servicios.MailManager;
import com.security.utils.ScreenMessage;
import com.security.utils.ScreenMessageImp;


/**
 * Controlador que se utiliza para los servicios asociados al formulario 
 * de RestorePassword.
 * Observar la anotaci�n @Controller (que hereda de @Component) de SPRING.
 * Esta anotaci�n le indica a SPRING que esta clase es de tipo controlador.
 * A continuaci�n est� la anotaci�n @RequestMapping que indica cuales son
 * las URL que mapea este controlador.
 * @author Gabriel Mainero
 *
 */
@Controller
@RequestMapping(
		value=
			{
				"/precargaFormularioRestorePassword.html",
				"/enviarRestorePassword.html"
			}
		)
public class FormRestorePasswordController {
	private static Logger logger = Logger.getLogger(FormRestorePasswordController.class);
	private UserService userService;
	private RestorePasswordValidator validator;
	private MailManager mailManager;
	
	/**
	 * Setea el servicio de RestorePassword.
	 * Observar la anotaci�n @Autowired, que le indica a Spring que
	 * debe ejecutar este m�todo e inyectarle un objeto RestorePasswordService.
	 * La clase RestorePasswordImp implementa RestorePassword y est� anotada
	 * con @Component, entonces Spring sabe que puede instanciar esta clase
	 * y pas�rsela a este m�todo.
	 * @param personaService
	 */
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	@Autowired
	public void setValidator(RestorePasswordValidator validator) {
		this.validator = validator;
	}
	@Autowired
	public void setMailManager(MailManager mailManager){
		this.mailManager=mailManager;
	}
	@InitBinder
	public void initDataBinder(WebDataBinder binder) {
		validator.initDataBinder(binder);
	}
	/**
	 * Observar la anotaci�n @RequestMapping de SPRING.
	 * Mapea la URL /precargaFormularioRestorePassword.html para que ejecute
	 * este m�todo cuando venga GET.
	 * Todos los par�metros son inyectados por SPRING cuando ejecuta el m�todo.
	 * 
	 * Se encarga de cargar en request y session los datos necesarios
	 * para mostrar el formulario de RestorePassword, ya sea nuevo, consulta o modificaci�n.
	 * 
	 * @param atributos son los atributos del request
	 * @return "formularioRestorePassword" la vista que debe mostrarse (ver dispatcher-servlet.xml/viewResolver)
	 */
	@RequestMapping(
			value="/precargaFormularioRestorePassword.html",
			method = RequestMethod.GET
		)
	public String precargaFormularioRestorePassword(
			Map<String,Object> atributos) {
		User restorePasswordFormulario = new User();
		atributos.put("restorePasswordFormulario", restorePasswordFormulario);
		return "formularioRestorePassword";
	}
	/**
	 * Observar la anotaci�n @RequestMapping de SPRING.
	 * Todos los par�metros son inyectados por SPRING cuando ejecuta el m�todo.
	 * 
	 * Se encarga de guardar en base de datos RestorePassword.
	 * 
	 * @param RestorePassword insCost a guardar.
	 * Observar la anotaci�n @ModelAttribute que le indica a SPRING
	 * que debe intentar llenar el objeto RestorePassword con los par�metros del 
	 * request.
	 * @param session es la misma sesi�n que usabamos con los Servlets.
	 * @param atributos son los atributos del request
	 * @return "formularioRestorePassword" la vista que debe mostrarse (ver dispatcher-servlet.xml/viewResolver)
	 */
	@RequestMapping(
			value="/enviarRestorePassword.html",
			method= RequestMethod.POST
		)
	public String enviarRestorePassword(
			@ModelAttribute("restorePasswordForm") User restorePassword,
			BindingResult result,
			Map<String,Object> atributos){
		
		if(!result.hasErrors()){
			validator.validate(restorePassword,result);
		}
		if(result.hasErrors()){
			atributos.put("restorePasswordFormulario", restorePassword);
			atributos.put("errores", true);	
			atributos.put("result", result);
			return precargaFormularioRestorePassword(atributos);
		}
		final List<User> userList = userService.listarPorEMail(restorePassword.getPersona().getMail());		
		if(userList!=null){
			final String sistema = "Sistema " + userList.get(0).getCliente().getPersona().getRazonSocial();
			String pass = RecursosPassword.generarPassword();
			final String pass2 = pass;
			new Thread(){
				public void run(){
					enviarMail(userList,pass2,sistema);
				}
			}.start();
			try {
				pass = RecursosPassword.encriptar(pass);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			//modificaci�n Ezequiel Beccaria		
			for(User user:userList){
				user.setPassword(pass);	
				userService.actualizar(user);
			}
		}
		//Genero el aviso
		List<ScreenMessage> avisos = new ArrayList<ScreenMessage>();
		ScreenMessage mensaje = new ScreenMessageImp("formularioRestorePassword.confirmacion", null);
		avisos.add(mensaje); //agrego el mensaje a la coleccion
		
		atributos.put("errores", false);
		atributos.remove("result");
		atributos.put("hayAvisos", true);
		atributos.put("avisos", avisos);
		//hacemos el redirect
		return "index";
	}
	
//	private void enviarMail(User user, String pass){
//		try {
//			mailManager.enviar(user.geteMail(), "PAYMOD AG - Recuperar Usuario", "Usuario: " + user.getUsername() + " Contrase�a: " + pass);
//		} catch (MessagingException e) {
//			logger.error(e);
//		} catch (IllegalStateException e){
//			logger.error(e);
//		} 
//	}
	
	private void enviarMail(List<User> userList, String pass,String sistema){
		try {
			StringBuilder str = new StringBuilder();
			for(User user:userList){
				str.append("Usuario: " + user.getUsername() + " \n Nueva Contrase�a: " + pass + "\n");				
			}
			mailManager.enviar(userList.get(0).getPersona().getMail(), sistema + " - Recuperar contrase�a", str.toString(),sistema);
		} catch (MessagingException e) {
			logger.error(e);
		} catch (IllegalStateException e){
			logger.error(e);
		}
	}
}
