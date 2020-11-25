var menuAbierto=false;

$(document).ready(function() {
	//tabla elemento
	$("#elemento tbody td:nth-child(10)").click(function(){
		var input = $(this).find('input');

		if(input.length > 0) {
			$('#codigoDesde').val(input.val());
	        $('#codigoHasta').val(input.val());
	      }else{
	    	  $('#codigoDesde').val($(this).text());
		      $('#codigoHasta').val($(this).text());
	      }
			
	});
	$("#elemento tbody tr").mouseover(function(){
		if(!menuAbierto)
			$(this).addClass('tr_mouseover');
			
	});
	$("#elemento tbody tr").mouseout(function(){
		if(!menuAbierto)
			$(this).removeClass('tr_mouseover');
	});
	
	//Checkbox
	$("input[name=checktodos]").change(function(){
		$('input[type=checkbox]').each( function() {			
			if($("input[name=checktodos]:checked").length == 1){
				this.checked = true;
			} else {
				this.checked = false;
			}
		});
	});
	
	//Busqueda
	$('#busquedaDiv').attr({'style':'display:block'});
	$('#busquedaImg').click(function() {
		$('#busquedaDiv').slideToggle('slideUp');
		$('#busquedaImgSrcDown').slideToggle('slideUp');
		$('#busquedaImgSrc').slideToggle('slideUp');
	});
	$('#busquedaDiv > table').addClass('seccion');
	
	//Tooltips
	$("img[title]").tooltip();
	
	$('#guardar').click(function(){
		$('#hdn_guardar').val(true);	
		guardar(); 
	});
	$('#guardarYcontinuar').click(function(){
		$('#hdn_guardar').val(false);
		guardar(); 
	});
	
	
	$('#cancelar').click(function(){
		cancelar(); 
	});
	
   
	//Popups Busqueda
    $('#botonPopupEmpresa').click(function(){
  	  abrirPopupEmpresa();
    });
    
    $('#botonPopupSucursales').click(function(){
  	  abrirPopupSucursal();
    });
    
    $('#botonPopupDeposito').click(function(){
  	  abrirPopupDeposito();
    });
    
    $('#botonPopupCliente').click(function(){
  	  abrirPopupCliente()();
    });
    
    $('#botonPopupContenedor').click(function(){
  	 abrirPopupContenedor(); 
    });
    
    $('#botonPopupLectura').click(function(){
  	 abrirPopupLectura(); 
    });
  
          
  	//Fin menu
    	//binding OnBlur() para los campos con popup	
  	$("#codigoEmpresa").blur(function(){
  		getAjax('empresasServlet','codigo','codigoEmpresa',getEmpresaCallBack);
  		getAjaxConParent('lecturasPorEmpresaServlet','codigo','codigoLectura','codigoEmpresa',getLecturaCallBack);
  	});
  	$("#codigoSucursal").blur(function(){
  		getAjax('sucursalesServlet','codigo','codigoSucursal',getSucursalCallBack);
  	});
  	$("#codigoDeposito").blur(function(){
  		getAjax('depositosServlet','codigo','codigoDeposito',getDepositoCallBack);
  	});
  	$("#codigoCliente").blur(function(){
  		getAjax('clientesServlet','codigo','codigoCliente',getClienteCallBack);
  	});
  	$("#codigoContenedor").blur(function(){
  		getAjax('elementosServlet','codigo','codigoContenedor',getElementoCallBack);
  	});

  	$("#codigoLectura").blur(function(){
  		getAjaxConParent('lecturasPorEmpresaServlet','codigo','codigoLectura','codigoEmpresa',getLecturaCallBack);
  	});
  	if($("#codigoDeposito").val() != null){
  		getAjax('depositosServlet','codigo','codigoDeposito',getDepositoCallBack);
  	}
  	if($("#codigoSucursal").val() != null){
  		getAjax('sucursalesServlet','codigo','codigoSucursal',getSucursalCallBack);
  	}
  	if($("#codigoEmpresa").val() != null){
  		getAjax('empresasServlet','codigo','codigoEmpresa',getEmpresaCallBack);
  	}
  	if($("#codigoCliente").val() != null){
  		getAjax('clientesServlet','codigo','codigoCliente',getClienteCallBack);
  	}
  	if($("#codigoContenedor").val() != null){
  		getAjax('elementosServlet','codigo','codigoContenedor',getElementoCallBack);
  	}
  	if($("#codigoLectura").val() != null){
  		getAjaxConParent('lecturasPorEmpresaServlet','codigo','codigoLectura','codigoEmpresa',getLecturaCallBack);
  	}
  	
  	$('#pop').hide();

    	
});

function guardar(){
	var allVals = [];
    $("input[class='selectableCheckbox']:checked").each(function() {
    	allVals.push($(this).val());
    });
    
    if(allVals.length == 0)
    {
    	jAlert('Debe seleccionar al menos un elemento.','Selecci�n de Elementos');
		return;
    }else{
		document.forms[0].submit();
	}
    
//	if($("#codigoContenedor").val() == null || $("#codigoContenedor").val()==""){
//		jAlert('Seleccione un contenedor','Selecci�n de Elementos');
//		return;
//	}else{
//		document.forms[0].submit();
//	}
}
function cancelar(){
	document.location="iniciarElemento.html";
}
function volver(){
	document.location="menu.html";
}
function abrirPopupEmpresa(){
	
	var url = "popUpSeleccionEmpresa.html";
	
	jQuery.ajax({
        url: url,
        success: function(data){
           $(this).html(data);
           $(".displayTagDiv").displayTagAjax();
           popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer');
        },
        data: ({"time":new Date().getTime()}),
        context: $(".selectorDiv")
    });
}

function abrirPopupSucursal(){
	if($("#codigoEmpresa").val()==null || $("#codigoEmpresa").val()==""){
		jAlert($('#errorSeleccioneEmpresa').val());
		return;
	}
	var url = "popUpSeleccionSucursal.html?codigo="+$("#codigoEmpresa").val();
	jQuery.ajax({
        url: url,
        success: function(data){
           $(this).html(data);
           $(".displayTagDiv").displayTagAjax();
           popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer');
        },
        data: ({"time":new Date().getTime()}),
        context: $(".selectorDiv")
    });
}

function abrirPopupDeposito(){
	if($("#codigoEmpresa").val()==null || $("#codigoEmpresa").val()==""){
		jAlert($('#errorSeleccioneEmpresa').val());
		return;
	}else if($("#codigoSucursal").val()==null || $("#codigoSucursal").val()==""){
		jAlert($('#errorSeleccioneSucursal'));
		return;
	}
	var url = "popUpSeleccionDepositoPorSucursal.html?codigoSucursal="+$("#codigoSucursal").val();
	jQuery.ajax({
        url: url,
        success: function(data){
           $(this).html(data);
           $(".displayTagDiv").displayTagAjax();
           popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer');
        },
        data: ({"time":new Date().getTime()}),
        context: $(".selectorDiv")
    });
}

function abrirPopupCliente(){
	if($("#codigoEmpresa").val()==null || $("#codigoEmpresa").val()==""){
		jAlert($('#errorSeleccioneEmpresa').val());
		return;
	}
	var url = "popUpSeleccionCliente.html?codigo="+$("#codigoEmpresa").val();
	jQuery.ajax({
        url: url,
        success: function(data){
           $(this).html(data);
           $(".displayTagDiv").displayTagAjax();
           popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer');
        },
        data: ({"time":new Date().getTime()}),
        context: $(".selectorDiv")
    });
}

function abrirPopupContenedor(){
	
	//var url = "popUpSeleccionContenedor.html?codigo="+$('#codigoCliente').val();
	var url = "popUpSeleccionContenedor.html";
//	$.post(url, {codigo:+$('#codigoCliente').val()}, function(data){
//												        $('.selectorDiv').html(data);
//												        $(".displayTagDiv").displayTagAjax();
//												        popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer');
//												        $('#pop').hide();
//     												});
	
	jQuery.ajax({
        url: url,
        success: function(data){
           $(this).html(data);
           $(".displayTagDiv").displayTagAjax();
           popupOffDiv($('#pop'),'darkLayer');
           popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer'); 
        },
        type: "GET",
        timeout:120000,
        data: ({"time":new Date().getTime()})+"&codigo="+$('#codigoCliente').val(),
        context: $(".selectorDiv"),
        beforeSend:function(){
        	popupOnDiv($('#pop'),'darkLayer');
        },
        error:function(){
        	jAlert("Ha ocurrido un error...");
        	popupOffDiv($('#pop'),'darkLayer');
        }
    });
}

function abrirPopupLectura(){
	var url = "popUpSeleccionLectura.html?codigo="+$("#codigoEmpresa").val();
	jQuery.ajax({
        url: url,
        success: function(data){
           $(this).html(data);
           $(".displayTagDiv").displayTagAjax();
           popupOnDiv($(this).find('.darkMiddleClass'),'darkLayer');
        },
        data: ({"time":new Date().getTime()}),
        context: $(".selectorDiv")
    });
}


//ajax
function getAjax(url, varName, elementName, callBack){
	var input = document.getElementById(elementName);
	if(input == null)
		return;
	var url = url+'?'+varName+'='+input.value+'&clienteId='+$("#clienteId").val();	
	var request = new HttpRequest(url, callBack);
	request.send();	
}
//ajax
function getAjaxConParent(url, varName, elementName, parentId,callBack){

		var input = document.getElementById(elementName);
		if(input == null)
			return;
		var url = url+'?'+varName+'='+input.value+'&clienteId='+$("#clienteId").val()+'&empresaId='+$("#"+parentId+"").val();	
		var request = new HttpRequest(url, callBack);
		request.send();
	
}
//ajax
function getDepositoCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != "")
		$("#codigoDepositoLabel").html(sResponseText);	
	else{
		$("#idCliente").val("");
		$("#codigoDepositoLabel").html("");
	}
}
//ajax
function getSucursalCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != "")
		$("#codigoSucursalLabel").html(sResponseText);
	else{
		$("#idCliente").val("");
		$("#codigoSucursalLabel").html("");
		$("#codigoDepositoLabel").html("");
		$("#codigoDeposito").val("");
	}
}

//ajax
function getEmpresaCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != "")
		$("#codigoEmpresaLabel").html(sResponseText);
	else{
		$("#idCliente").val("");
		$("#codigoEmpresaLabel").html("");
		$("#codigoDepositoLabel").html("");
		$("#codigoSucursalLabel").html("");
		$("#codigoDeposito").val("");
		$("#codigoSucursal").val("");
	}
}

//ajax
function getClienteCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != "")
		$("#codigoClienteLabel").html(sResponseText);
	else{
		$("#idCliente").val("");
		$("#codigoClienteLabel").html("");
	}
}

//ajax
function getElementoCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != "")
		$("#codigoContenedorLabel").html(sResponseText);
	else{
		$("#idCliente").val("");
		$("#codigoContenedorLabel").html("");
	}
}

//ajax
function getLecturaCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != "")
		$("#codigoLecturaLabel").html(sResponseText);
	else{
		$("#codigoLecturaLabel").html("");
		$("#codigoLectura").val("");
	}
}

function getValidadorElemntoCallBack(sResponseText){	
	if (sResponseText != 'null' && sResponseText != ""){
		jConfirm('El elemento contiene referencias, desea eliminarlo?', 'Confirmar Eliminar',function(r) {
		    if(r){
		    	document.location="eliminarElemento.html?id="+sResponseText;
		    }
		});
	
	}
}

