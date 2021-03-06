package com.dividato.configuracionGeneral.controladores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.dividato.configuracionGeneral.validadores.PosicionBusquedaValidator;
import com.dividato.configuracionGeneral.validadores.PosicionLibreBusquedaValidator;
import com.security.accesoDatos.configuraciongeneral.interfaz.DepositoService;
import com.security.accesoDatos.configuraciongeneral.interfaz.ElementoService;
import com.security.accesoDatos.configuraciongeneral.interfaz.EmpresaService;
import com.security.accesoDatos.configuraciongeneral.interfaz.LecturaDetalleService;
import com.security.accesoDatos.configuraciongeneral.interfaz.LecturaService;
import com.security.accesoDatos.configuraciongeneral.interfaz.PosicionService;
import com.security.accesoDatos.configuraciongeneral.interfaz.SeccionService;
import com.security.accesoDatos.configuraciongeneral.interfaz.SucursalService;
import com.security.accesoDatos.jerarquias.interfaz.JerarquiaService;
import com.security.modelo.administracion.ClienteAsp;
import com.security.modelo.configuraciongeneral.Elemento;
import com.security.modelo.configuraciongeneral.Empresa;
import com.security.modelo.configuraciongeneral.Lectura;
import com.security.modelo.configuraciongeneral.LecturaDetalle;
import com.security.modelo.configuraciongeneral.Posicion;
import com.security.modelo.configuraciongeneral.TipoElemento;
import com.security.modelo.general.PersonaFisica;
import com.security.modelo.jerarquias.Jerarquia;
import com.security.modelo.seguridad.User;
import com.security.utils.CampoDisplayTag;
import com.security.utils.Constantes;
import com.security.utils.ScreenMessage;
import com.security.utils.ScreenMessageImp;
/**
 * Controlador que se utiliza para los servicios asociados a la
 * lista de PosicionLibre.
 * Observar la anotaci�n @Controller (que hereda de @Component) de SPRING.
 * Esta anotaci�n le indica a SPRING que esta clase es de tipo controlador.
 * A continuaci�n est� la anotaci�n @RequestMapping que indica cuales son
 * las URL que mapea este controlador.
 * @author Victor Kenis
 *
 */
@Controller
@RequestMapping(
		value=
			{	
				"/iniciarPosicionLibre.html",
				"/mostrarPosicionLibre.html",
				"/filtrarPosicionLibre.html",
				"/asignarPosicionLibre.html",
				"/guardarPosicionLibre.html",
				"/cancelarPosicionLibre.html",
				"/importarLecturaPosicionLibre.html"
			}
		)
public class ListaPosicionLibreController {
	
	private PosicionService posicionService;
	private PosicionLibreBusquedaValidator validator;
	private LecturaService lecturaService;
	private LecturaDetalleService lecturaDetalleService;
	private ElementoService elementoService;
		
	@Autowired
	public void setElementoService(ElementoService elementoService) {
		this.elementoService = elementoService;
	}
	@Autowired
	public void setPosicionService(PosicionService posicionService) {
		this.posicionService = posicionService;
	}
	@Autowired
	public void setValidator(PosicionLibreBusquedaValidator validator) {
		this.validator = validator;
	}
	@InitBinder
	public void initDataBinder(WebDataBinder binder) {
		validator.initDataBinder(binder);
	}
	
	/**
	 * Setea el servicio de Depositos.
	 * Observar la anotaci�n @Autowired, que le indica a Spring que
	 * debe ejecutar este m�todo e inyectarle un objeto PosicionService.
	 * La clase DepositoServiceImp implementa Deposito y est� anotada
	 * con @Component, entonces Spring sabe que puede instanciar esta clase
	 * y pas�rsela a este m�todo.
	 * @param depositoService
	 */
	
	@Autowired
	public void setLecturaDetalleService(LecturaDetalleService lecturaDetalleService) {
		this.lecturaDetalleService = lecturaDetalleService;
	}
	
	@Autowired
	public void setLecturaService(LecturaService lecturaService) {
		this.lecturaService = lecturaService;
	}
			
	@RequestMapping(
			value="/iniciarPosicionLibre.html",
			method = RequestMethod.GET
		)
	public String iniciarPosicionLibre(HttpSession session,
			Map<String,Object> atributos){
		
		session.removeAttribute("tipoSession");
		session.removeAttribute("listaElementosSession");
		session.removeAttribute("posicionesALiberarSession");
		session.removeAttribute("posicionsSession");
		session.removeAttribute("posicionesAsignadasSession");
		session.removeAttribute("lectura");
		session.removeAttribute("posicionLibreBusqueda");
		
		return "redirect:mostrarPosicionLibre.html";
	}
	
	/**
	 * Observar la anotaci�n @RequestMapping de SPRING.
	 * Todos los par�metros son inyectados por SPRING cuando ejecuta el m�todo.
	 * 
	 * Se encarga de buscar la lista de PosicionLibre y subirla a request.
	 * 
	 * @param atributos son los atributos del request
	 * @return "consultaPosicionLibre" la vista que debe mostrarse (ver dispatcher-servlet.xml/viewResolver)
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(
			value="/mostrarPosicionLibre.html",
			method = RequestMethod.GET
		)
	public String mostrarPosicionLibre(HttpSession session,
			Map<String,Object> atributos,
			@RequestParam(value="val", required=false) String valSeccion,
			@RequestParam(value="val", required=false) String valDeposito,
			@RequestParam(value="accion", required=false) String accion,
			@RequestParam(value="val", required=false) String valLectura,
			@RequestParam(value="codigoLectura", required=false) String codigoLectura,
			@RequestParam(value="importar", required=false) String importar,
			@RequestParam(value="depositoCodigo", required=false) String depositoCodigo){

		TipoElemento tipo = (TipoElemento) session.getAttribute("tipoSession");
		List<LecturaDetalle> listaElementos = (List<LecturaDetalle>) session.getAttribute("listaElementosSession");
		List<Posicion> posicionesALiberar = (List<Posicion>) session.getAttribute("posicionesALiberarSession");
		List<PosicionLibre> posicionesAsignadas = (List<PosicionLibre>) session.getAttribute("posicionesAsignadasSession");
		List<Posicion> posicions = (List<Posicion>) session.getAttribute("posicionsSession");
		if(posicions==null)
			posicions = new ArrayList<Posicion>();
		
		List<PosicionLibre> listaPosicionesLibres = new ArrayList<PosicionLibre>();
		Posicion posicion = (Posicion) session.getAttribute("posicionLibreBusqueda");		
//		if(posicion != null && posicion.getCodigoDeposito() != null && !"".equals(posicion.getCodigoDeposito())
//				&&  posicion.getCodigoSeccion() != null && !"".equals(posicion.getCodigoSeccion())){
		//Este anterior if estaba en la busqueda de posicion.. ver si sirve o no
		
//		}else if(posicion==null){
//			posicion=new Posicion();
//			posicion.setCodigoDesdeEstante("0000");
//			posicion.setCodigoHastaEstante("9999");
//			session.setAttribute("posicionBusqueda", posicion);		
		
		if("CANCELAR".equalsIgnoreCase(accion))
		{
			posicionesAsignadas = null;
			posicionesALiberar = null;
			accion = "ASIGNAR";
			atributos.put("accion", accion);
		}
		
		//Si no hay posiciones asignadas hay que buscar en base de datos
		if(posicionesAsignadas==null){
		
			posicions = null;
			if(posicion!= null){
				posicion.setEstado("DISPONIBLE");
				//posicions =(List<Posicion>) posicionService.listarPosicionFiltradas(posicion, obtenerClienteAspUser());
				posicions = posicionService.traerPosicionesLibresPorSQL(posicion, obtenerClienteAspUser(),tipo);

				session.setAttribute("posicionLibreBusqueda", posicion);
			}
			//Si se encontraton posiciones
			if(posicions!=null)
			{
				for(Object ob:posicions){
					Object[] lista = (Object[]) ob;
					PosicionLibre posicionLibre = new PosicionLibre((Posicion)lista[0],(String)lista[1]);
					listaPosicionesLibres.add(posicionLibre);
				}
				
				
				//Si existe una lectura con elementos validos cargada
				if(listaElementos != null && listaElementos.size()>0){
				//Si hay un tipo seteado
					if(tipo!=null)
					{
						atributos.put("accion", "ASIGNAR");
					}
				}
			}
		}else
		{
			listaPosicionesLibres = posicionesAsignadas;
			atributos.put("accion", "GUARDAR");
		}

		if(session.getAttribute("lectura")!=null)
		{
			Lectura lectura = (Lectura)session.getAttribute("lectura");
			atributos.put("codigoLectura", lectura.getCodigo());
			atributos.put("descripcion", lectura.getDescripcion());
		}
		atributos.put("listaElementos", listaElementos);
		session.setAttribute("listaElementosSession", listaElementos);
		atributos.put("posicionesLibres", listaPosicionesLibres);
		session.setAttribute("posicionesAsignadasSession", posicionesAsignadas);
		session.setAttribute("posicionesLibresSession", listaPosicionesLibres);
		session.setAttribute("posicionsSession", listaPosicionesLibres);
		atributos.put("clienteId", obtenerClienteAspUser().getId());
		atributos.put("codigoEmpresa", obtenerEmpresaDefault().getCodigo());
	
		//si no hay errores
		if(atributos.get("errores") == null)
			atributos.put("errores", false);

		//definirPopupLecturas(atributos, valLectura);

		//hacemos el forward
		return "consultaPosicionLibre";
	}
	
	@RequestMapping(
			value="/filtrarPosicionLibre.html",
			method = RequestMethod.POST
		)
	public String filtrarPosicionLibre(@ModelAttribute("posicionLibreBusqueda") Posicion posicion, 
			BindingResult result,
			HttpSession session,
			Map<String,Object> atributos){
		//buscamos en la base de datos y lo subimos a request.
		this.validator.validate(posicion, result);
		if(!result.hasErrors()){
			session.setAttribute("posicionLibreBusqueda", posicion);
			atributos.put("errores", false);
			atributos.remove("result");
		}else{
			atributos.put("errores", true);
			atributos.put("result", result);			
		}
		return mostrarPosicionLibre(session,atributos, null, null, null, null, null, null, null);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(
			value="/asignarPosicionLibre.html",
			method = RequestMethod.GET
		)
	public String asignarPosicionLibre(HttpSession session, Map<String,Object> atributos){
		
		ScreenMessage mensaje = null;
		List<ScreenMessage> avisos = new ArrayList<ScreenMessage>();

		Boolean hayAvisosNeg = false;
		if(atributos.get("errores") == null){
			atributos.put("errores", false);
		}
		
		TipoElemento tipo = (TipoElemento) session.getAttribute("tipoSession");
		List<LecturaDetalle> listaElementos = (List<LecturaDetalle>) session.getAttribute("listaElementosSession");
		List<Posicion> posicionesALiberar = (List<Posicion>) session.getAttribute("posicionesALiberarSession");
		List<PosicionLibre> posicionesAsignadas = (List<PosicionLibre>) session.getAttribute("posicionesAsignadasSession");
		List<Posicion> posicions = (List<Posicion>) session.getAttribute("posicionsSession");
		List<PosicionLibre> listaPosicionesLibres = (List<PosicionLibre>) session.getAttribute("posicionesLibresSession");
		
		if(listaPosicionesLibres!=null && listaPosicionesLibres.size()>0 ){
			
			if(listaElementos != null && listaElementos.size() > 0){
				if(listaPosicionesLibres.size()>= listaElementos.size()){

					posicionesAsignadas = new ArrayList<PosicionLibre>();
					posicionesALiberar = new ArrayList<Posicion>();
					
					for(int i = 0; i<listaElementos.size();i++)
					{
						for(int f = 0; f<listaPosicionesLibres.size();f++)
						{
							if(listaElementos.get(i).getElemento().getDepositoActual()!= null)
							{
								if(listaElementos.get(i).getElemento().getDepositoActual().getId().longValue() !=
									listaPosicionesLibres.get(f).getPos().getEstante().getGrupo().getSeccion().getDeposito().getId().longValue())
								{
									continue;
								}
							}
							
							Boolean mismoTipo = elementoService.verificarTipoElementoValidoParaMismoModulo(tipo, listaPosicionesLibres.get(f).getPos().getModulo(), obtenerClienteAsp());
							Boolean mismoTipoModulosAdy = elementoService.verificarTipoElementoValidoParaGrupoDeModulos(tipo,listaPosicionesLibres.get(f).getPos().getModulo(), obtenerClienteAsp());
							//Pregunto si son del mismo tipo la lecutra y la posicion
							if((mismoTipo!=null && mismoTipo) && (mismoTipoModulosAdy!=null && mismoTipoModulosAdy)){
								
								listaPosicionesLibres.get(f).getPos().setEstado(Constantes.POSICION_ESTADO_OCUPADA);
								listaPosicionesLibres.get(f).getPos().setElementoAsignado(listaElementos.get(i).getElemento());
								posicionesAsignadas.add(listaPosicionesLibres.get(f));
								if(listaElementos.get(i).getElemento().getPosicion() != null)
								{
									listaElementos.get(i).getElemento().getPosicion().setEstado(Constantes.POSICION_ESTADO_DISPONIBLE);
									posicionesALiberar.add(listaElementos.get(i).getElemento().getPosicion());
								}
								listaElementos.get(i).getElemento().setPosicionFutura(listaPosicionesLibres.get(f).getPos());
								listaPosicionesLibres.remove(f);
								break;
							}
							
						}//fin ciclo posiciones
						
					}//fin ciclo elementos
					
					session.setAttribute("posicionesALiberarSession", posicionesALiberar);
					session.setAttribute("posicionesAsignadasSession", posicionesAsignadas);
					session.setAttribute("listaElementosSession", listaElementos);
					
				}
				else
				{
					mensaje = new ScreenMessageImp("formularioPosicionLibre.errorMenorCantidadPosiciones", null);
					hayAvisosNeg = true;
					avisos.add(mensaje);
				}
			}
			else
			{
				mensaje = new ScreenMessageImp("formularioPosicionLibre.errorNoHayElementos", null);
				hayAvisosNeg = true;
				avisos.add(mensaje);
			}
		}
		else
		{
			mensaje = new ScreenMessageImp("formularioPosicionLibre.errorNoHayPosicionesLibres", null);
			hayAvisosNeg = true;
			avisos.add(mensaje);
		}
		
		atributos.put("hayAvisosNeg", hayAvisosNeg);
		atributos.put("avisos", avisos);
		atributos.put("posicionesLibres", posicions);
		session.setAttribute("posicionsSession", posicions);
		
		//hacemos el forward
		return mostrarPosicionLibre(session,atributos, null, null, null, null, null, null, null);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(
			value="/guardarPosicionLibre.html", 
			method = RequestMethod.GET
		)
	public String guardarPosicionLibre(HttpSession session, Map<String,Object> atributos){
		

		List<LecturaDetalle> listaElementos = (List<LecturaDetalle>) session.getAttribute("listaElementosSession");
		List<Posicion> posicionesALiberar = (List<Posicion>) session.getAttribute("posicionesALiberarSession");
		List<PosicionLibre> posicionesAsignadas = (List<PosicionLibre>) session.getAttribute("posicionesAsignadasSession");
		List<Posicion> posicions = (List<Posicion>) session.getAttribute("posicionsSession");
		
		Boolean commit;
		List<Elemento> elementos = new ArrayList<Elemento>();
		
		for(int i = 0; i<listaElementos.size();i++)
		{
			elementos.add(listaElementos.get(i).getElemento());
		}
		
		List<Posicion> listaPosicionesAsignadas = new ArrayList<Posicion>(); 
		for(PosicionLibre pos:posicionesAsignadas){
			Posicion posicionLibre = new Posicion();
			posicionLibre = pos.getPos();
			listaPosicionesAsignadas.add(posicionLibre);
		}
	
		commit = elementoService.guardarAsignacionPosicionesLibres(posicionesALiberar, listaPosicionesAsignadas, elementos, obtenerClienteAsp(), obtenerUser(), (Lectura)session.getAttribute("lectura"));
		//commit = elementoService.actualizarElementoList(elementos);
		//commit2 = posicionService.actualizarPosicionList(posicionesAsignadas);
//		if(posicionesALiberar.size()>0)
//		{
//			commit3 = posicionService.actualizarPosicionList(posicionesALiberar);
//		}
		
		if(commit != null && !commit){
			List<ScreenMessage> avisos = new ArrayList<ScreenMessage>();
			ScreenMessage mensaje = new ScreenMessageImp("error.commitDataBase", null);
			avisos.add(mensaje); //agrego el mensaje a la coleccion
			atributos.put("posicionesLibres", posicions);
			session.setAttribute("posicionsSession", posicions);
			atributos.put("listaElementos", listaElementos);
			atributos.put("accion", "GUARDAR");
			atributos.put("errores", false);
			atributos.put("hayAvisos", false);
			atributos.put("hayAvisosNeg", true);
			atributos.put("avisos", avisos);
			return mostrarPosicionLibre(session,atributos, null, null, null, null, null, null, null);
		}
		else
		{
			List<ScreenMessage> avisos = new ArrayList<ScreenMessage>();
			ScreenMessage mensajeEstanteReg = new ScreenMessageImp("formularioPosicionLibre.notificacion.posicionLibreRegistrada", null);
			avisos.add(mensajeEstanteReg); //agrego el mensaje a la coleccion
			atributos.put("errores", false);
			atributos.remove("result");
			atributos.put("hayAvisos", true);
			atributos.put("hayAvisosNeg", false);
			atributos.put("avisos", avisos);
		}
		
		session.removeAttribute("tipoSession");
		session.removeAttribute("listaElementosSession");
		session.removeAttribute("posicionesALiberarSession");
		session.removeAttribute("posicionesAsignadasSession");
		session.removeAttribute("posicionsSession");
		session.removeAttribute("lectura");
		
		atributos.remove("accion");
		atributos.remove("posicions");
		atributos.remove("listaElementos");
		session.removeAttribute("posicionLibreBusqueda");
		return mostrarPosicionLibre(session,atributos, null, null, null, null, null, null, null);
		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(
			value="/importarLecturaPosicionLibre.html",
			method = RequestMethod.GET
		)
	public String importarLecturaPosicionLibre(HttpSession session,
			Map<String,Object> atributos,
			@RequestParam(value="codigoLectura", required=false) String codigoLectura){
		
		TipoElemento tipo = (TipoElemento) session.getAttribute("tipoSession");
		List<LecturaDetalle> listaElementos = (List<LecturaDetalle>) session.getAttribute("listaElementosSession");
		List<Posicion> posicions = (List<Posicion>) session.getAttribute("posicionsSession");
		
		ScreenMessage mensaje = null;
		List<ScreenMessage> avisos = new ArrayList<ScreenMessage>();
		Boolean hayAvisos = false;
		Boolean hayAvisosNeg = false;
		Boolean banderaModulos = false, banderaInexistentes = false;
		
		//Si se importa una lectura
	 	if (codigoLectura != null) {
				Lectura lectura = lecturaService.obtenerPorCodigo(
						Long.valueOf(codigoLectura), null, obtenerEmpresaDefault(),obtenerClienteAsp());
				if(lectura==null)
				{
					session.removeAttribute("lectura");
					listaElementos = null;
					hayAvisosNeg = true;
					mensaje = new ScreenMessageImp("formularioPosicionLibre.notificacion.lecturaIvalida", null);
					avisos.add(mensaje);
					atributos.put("hayAvisosNeg", hayAvisosNeg);
					atributos.put("avisos", avisos);
					return mostrarPosicionLibre(session, atributos, null, null, null, null, null, null, null);
				}
				session.setAttribute("lectura", lectura);
				listaElementos = lecturaDetalleService.listarLecturaDetallePorLectura(lectura,obtenerClienteAsp());
				//Si la lectura contiene al menos un elemento
				if (listaElementos != null && listaElementos.size() > 0) {
					
					tipo = null;
					int cantidadTipo = 0;
					//Recorro la lista de elementos
					for (int i = 0; i < listaElementos.size(); i++) {
						//Pregunto si el codigo de barras pertenece a un modulo
						if (listaElementos.get(i).getCodigoBarras().startsWith("99")) {
							//Si es asi se remueve de la lista
							listaElementos.remove(i);
							i = i - 1;
							banderaModulos = true;
						} 
						else
						{
							//Pregunto si la lectura pertenece a un elemento existente
							if (listaElementos.get(i).getElemento() == null) {
							//Si se es asi se remueve
							listaElementos.remove(i);
							i = i - 1;
							banderaInexistentes = true;
							}
							//Si llega aqui es que es un elemento existente
							else
							{
								cantidadTipo = 1;
								//La primera vez que entra aca la variable tipo estara en nula
								if(tipo != null)
								{
									//Si se entro aqui es el segundo o mas elemento existente por lo que se compara el tipoElemento
									//con el tipoElemento del elemento existente anterior
									//Todos deben tener el mismo tipo
									if(!tipo.equals(listaElementos.get(i).getElemento().getTipoElemento()))
									{
										//Si es diferente se avisa que la lectura contiene elementos de tipo diferente y sale
										cantidadTipo = 2;
										hayAvisosNeg = true;
										mensaje = new ScreenMessageImp("formularioPosicionLibre.notificacion.filtradoElementoDiferentesTipo", null);
										avisos.add(mensaje);
										atributos.put("hayAvisosNeg", hayAvisosNeg);
										atributos.put("avisos", avisos);
										listaElementos = null;
										//hacemos el forward
										return mostrarPosicionLibre(session, atributos, null, null, null, null, null, null, null);
									}
								}
								//Se setea el tipo de elemento del ultimo elemento leido en la lectura
								tipo = listaElementos.get(i).getElemento().getTipoElemento();
							}
						}

					}
					if(banderaModulos)
					{
						//Se avisa que uno o mas modulos fueron descartados de la lectura
						mensaje = new ScreenMessageImp("formularioPosicionLibre.notificacion.filtradoElementoModulo", null);
						avisos.add(mensaje);
						hayAvisos = true;
						
					}
					if(banderaInexistentes)
					{
						//Se avisa que uno o mas elementos inexistentes fueron descartados de la lectura
						mensaje = new ScreenMessageImp("formularioPosicionLibre.notificacion.filtradoElementoInexistente", null);
						avisos.add(mensaje);
						hayAvisos = true;
					}
					//Se pregunta si quedaron elementos existentes en la lectura para asignarles posiciones libres
					if (listaElementos.size() > 0) {
						//Se muesta la lista de elementos
						session.setAttribute("listaElementosSession", listaElementos);
						atributos.put("listaElementos", listaElementos);
						session.setAttribute("tipoSession", tipo);
					} else {
						//Se avisa que la lectura no contiene elmentos validos para asignarles posicion libre
						mensaje = new ScreenMessageImp("formularioPosicionLibre.notificacion.lecturaSinElementos", null);
						avisos.add(mensaje);
						hayAvisosNeg = true;
					}

				}
			}//salida de la importacion de lectura
	
		atributos.put("codigoLectura", codigoLectura);
		atributos.put("hayAvisos", hayAvisos);
		atributos.put("avisos", avisos);
		if(posicions!=null)
		{
			atributos.put("posicionesLibres", posicions);
			session.setAttribute("posicionsSession", posicions);
		}
		//hacemos el forward
		return mostrarPosicionLibre(session, atributos, null, null, null, null, null, null, null);
		
	}
	
	/////////////////////METODOS DE SOPORTE/////////////////////////////
	
	public class PosicionLibre{
		
		private Posicion pos;
		private String valoracion;
		
		public PosicionLibre(Posicion posi, String val){
			pos = posi;
			valoracion = val;
		}
			
		public Posicion getPos() {
			return pos;
		}
		public void setPos(Posicion posicion) {
			this.pos = posicion;
		}
		public String getValoracion() {
			return valoracion;
		}
		public void setValoracion(String valoracion) {
			this.valoracion = valoracion;
		}

	}
	private ClienteAsp obtenerClienteAspUser(){
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getCliente();
	}
	
	private User obtenerUser(){
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	private ClienteAsp obtenerClienteAsp(){
		return obtenerUser().getCliente();
	}
	private Empresa obtenerEmpresaDefault(){
		return ((PersonaFisica)obtenerClienteAsp().getContacto()).getEmpresaDefecto();
	}
	
}