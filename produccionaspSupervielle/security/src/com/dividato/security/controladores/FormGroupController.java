package com.dividato.security.controladores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.dividato.security.validadores.GroupValidator;
import com.security.accesoDatos.interfaz.AuthorityService;
import com.security.accesoDatos.interfaz.GroupService;
import com.security.modelo.administracion.ClienteAsp;
import com.security.modelo.seguridad.Authority;
import com.security.modelo.seguridad.Group;
import com.security.modelo.seguridad.User;
import com.security.utils.ScreenMessage;
import com.security.utils.ScreenMessageImp;


/**
 * Controlador que se utiliza para los servicios asociados al formulario 
 * de Group.
 * Observar la anotaci�n @Controller (que hereda de @Component) de SPRING.
 * Esta anotaci�n le indica a SPRING que esta clase es de tipo controlador.
 * A continuaci�n est� la anotaci�n @RequestMapping que indica cuales son
 * las URL que mapea este controlador.
 * @author Gabriel Mainero
 * @modif Ezequiel Beccaria (05/05/2011)
 *
 */
@Controller
@RequestMapping(
		value=
			{
				"/precargaFormularioGroup.html",
				"/guardarActualizarGroup.html"
			}
		)
public class FormGroupController {
	private ListaGroupController listaGroupController;
	private GroupService groupService;
	private GroupValidator validator;
	private AuthorityService authorityService;
	
	/**
	 * Setea el servicio de Group.
	 * Observar la anotaci�n @Autowired, que le indica a Spring que
	 * debe ejecutar este m�todo e inyectarle un objeto GroupService.
	 * La clase GroupImp implementa Group y est� anotada
	 * con @Component, entonces Spring sabe que puede instanciar esta clase
	 * y pas�rsela a este m�todo.
	 * @param personaService
	 */
	@Autowired
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}
	@Autowired
	public void setListaGroupController(ListaGroupController listaGroupController) {
		this.listaGroupController = listaGroupController;
	}
	@Autowired
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	@Autowired
	public void setValidator(GroupValidator validator) {
		this.validator = validator;
	}
	@InitBinder
	public void initDataBinder(WebDataBinder binder) {
		validator.initDataBinder(binder);
	}
	/**
	 * Observar la anotaci�n @RequestMapping de SPRING.
	 * Mapea la URL /precargaFormularioGroup.html para que ejecute
	 * este m�todo cuando venga GET.
	 * Todos los par�metros son inyectados por SPRING cuando ejecuta el m�todo.
	 * 
	 * Se encarga de cargar en request y session los datos necesarios
	 * para mostrar el formulario de Group, ya sea nuevo, consulta o modificaci�n.
	 * 
	 * @param accion par�metro que se recibe del request 
	 * (Observar la anotaci�n @RequestParam)
	 * @param idGroup par�metro que se recibe del request
	 * (Observar la anotaci�n @RequestParam)
	 * @param session es la misma sesi�n que usabamos con los Servlets.
	 * @param atributos son los atributos del request
	 * @return "formularioGroup" la vista que debe mostrarse (ver dispatcher-servlet.xml/viewResolver)
	 */
	@RequestMapping(
			value="/precargaFormularioGroup.html",
			method = RequestMethod.GET
		)
	public String precargaFormulario(
			@RequestParam(value="accion",required=false) String accion,
			@RequestParam(value="idGroup",required=false) Long groupId,
			Map<String,Object> atributos) {
		if(accion==null) accion="NUEVO"; //acci�n por defecto: nuevo
		Collection<Authority> privilegiosIzq=authorityService.listAuthorityExceptAuthority("ROLE_ASP_ADMIN");
		Collection<Authority> privilegiosDer=new ArrayList<Authority>();		
		if(!accion.equals("NUEVO")){
			Group groupFormulario;
			groupFormulario=groupService.obtenerPorId(groupId);
			atributos.put("groupFormulario", groupFormulario);			
			privilegiosDer.addAll(groupFormulario.getAuthorities());
			privilegiosIzq.removeAll(privilegiosDer);			
		}
		atributos.put("accion", accion);		
		atributos.put("privilegiosIzq",privilegiosIzq);
		atributos.put("privilegiosDer",privilegiosDer);
		atributos.put("id",groupId);
		
		atributos.put("errores", false);
		atributos.put("hayAvisos", false);
		atributos.put("hayAvisosNeg", false);
		atributos.remove("result");
		atributos.remove("avisos");
		
		return "formularioGroup";
	}
	/**
	 * Observar la anotaci�n @RequestMapping de SPRING.
	 * Todos los par�metros son inyectados por SPRING cuando ejecuta el m�todo.
	 * 
	 * Se encarga de guardar en base de datos Group.
	 * 
	 * @param Group group a guardar.
	 * Observar la anotaci�n @ModelAttribute que le indica a SPRING
	 * que debe intentar llenar el objeto Group con los par�metros del 
	 * request.
	 * @param session es la misma sesi�n que usabamos con los Servlets.
	 * @param atributos son los atributos del request
	 * @return "formularioGroup" la vista que debe mostrarse (ver dispatcher-servlet.xml/viewResolver)
	 */
	@RequestMapping(
			value="/guardarActualizarGroup.html",
			method= RequestMethod.POST
		)
	public String guardarGroup(
			@RequestParam(value="accion",required=false) String accion,
			@RequestParam(value="privilegiosGrupoSeleccionados",required=false) String privilegiosGrupoSeleccionados,
			@ModelAttribute("groupForm") Group group,
			BindingResult result,
			HttpSession session,
			Map<String,Object> atributos){
		if(accion==null || accion.equals("") || accion.equals("NUEVO"))
			accion="NUEVO";
		else{
			accion="MODIFICACION";
		}
		if(!result.hasErrors()){
			group.setCliente(obtenerClienteAspUser());
			group.setAccion(accion);
			group.setGroupPrivileges(privilegiosGrupoSeleccionados);
			validator.validate(group,result);
		}
		
		Group groupFormulario;
		boolean hayAvisos = false;
		boolean hayAvisosNeg = false;		
		boolean commit = false;		
		List<ScreenMessage> avisos = new ArrayList<ScreenMessage>();
		
		if(!result.hasErrors()){
			String[] Authorities = group.getGroupPrivileges().split(",");
			Set<Authority> authoritiesSet = new TreeSet<Authority>(authorityService.listarPorAuthority(Authorities));
			
			if(accion.equals("NUEVO"))
				groupFormulario = crearGroup(group.getCliente(), group.getGroupName(), authoritiesSet, false, false);
			else{
				groupFormulario=groupService.obtenerPorId(group.getId());
				setearDatos(groupFormulario, group);
			}	
						
			// To Do guardar y actualizar group con group members
			if(accion.equals("NUEVO"))
				commit = groupService.save(groupFormulario);
			else
				commit = groupService.update(groupFormulario);
			
			//seteo los menjes
			ScreenMessage mensaje;
			if(commit){
				mensaje = new ScreenMessageImp("notif.group.grupoGuardado", null);
				hayAvisos = true;
			}else{
				mensaje = new ScreenMessageImp("error.commitDataBase", null);
				hayAvisosNeg = true;
			}
			avisos.add(mensaje);
		}
		//Si existen errores
		if(result.hasErrors()){
			atributos.put("groupFormulario", group);
			atributos.put("accion", accion);
			atributos.put("errores", true);
			atributos.put("hayAvisos", false);
			atributos.put("hayAvisosNeg", false);
			atributos.put("result", result);
			return precargaFormulario(accion, group.getId(), atributos);
		}else{
			atributos.put("errores", false);
			atributos.remove("result");		
		}			
		
		atributos.put("hayAvisosNeg", hayAvisosNeg);
		atributos.put("hayAvisos", hayAvisos);
		atributos.put("avisos", avisos);
		
		//hacemos el redirect
		return listaGroupController.mostrarGroup(session, atributos);
	}
	
	public Group crearGroup(ClienteAsp cliente, String groupName, Set<Authority> authorities, Boolean admin, Boolean nombreFijo){
		Group group = new Group();
		group.setGroupName(groupName);
		group.setAuthorities(authorities);
		group.setCliente(cliente);
		group.setAdmin(admin);
		group.setNombreFijo(nombreFijo);
		return group;
	}
	
	public void setearDatos(Group grupo, Group datos){
		//empezamos a setear los datos nuevos,
		grupo.setCliente(datos.getCliente());
		grupo.setGroupName(datos.getGroupName());
		String[] Authorities = datos.getGroupPrivileges().split(",");
		Set<Authority> authoritiesSet = new TreeSet<Authority>(authorityService.listarPorAuthority(Authorities));
		grupo.setAuthorities(authoritiesSet);
	}
	
	private ClienteAsp obtenerClienteAspUser(){
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getCliente();
	}
}
