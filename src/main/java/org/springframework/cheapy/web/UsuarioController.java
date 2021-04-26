
package org.springframework.cheapy.web;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.cheapy.model.Client;
import org.springframework.cheapy.model.Municipio;
import org.springframework.cheapy.model.Usuario;
import org.springframework.cheapy.service.UsuarioService;
import org.springframework.cheapy.utils.MD5;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuarioController {

	private static final String		VIEWS_USUARIO_CREATE_OR_UPDATE_FORM	= "usuarios/createOrUpdateUsuarioForm";

	private final UsuarioService	usuarioService;


	public UsuarioController(final UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/usuarios/show")
	public String processShowForm(final Map<String, Object> model) {
		Usuario usuario = this.usuarioService.getCurrentUsuario();
		model.put("usuario", usuario);
		return "usuarios/usuariosShow";
	}
	
	@GetMapping("/usuarios/favoritos/{page}")
	public String processFindForm(@PathVariable("page") final int page, final Map<String, Object> model) {
		List<Client> client = this.usuarioService.getCurrentUsuario().getFavoritos();
		List<Client> res = new ArrayList<>();
		
		for(int i=page*5; i<page*5+5; i++) {
			if(client.size()<=i) {
				break;
			}
			res.add(client.get(i));
		}
		
		Boolean next = true;
		if(page*5+5>client.size()) {
			next = false;
		}
		
		model.put("municipios", Municipio.values());
		model.put("clientLs", res);
		model.put("nextPage", next);
		model.put("localDateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		return "usuarios/favoritos";

	}

	@GetMapping(value = "/usuarios/edit")
	public String updateUsuario(final ModelMap model, final HttpServletRequest request) {
		Map<Object, String> municipios = new HashMap<Object, String>();

		Municipio[] a = Municipio.values();
		int cont = 0;
		for (Municipio i : Municipio.values()) {
			municipios.put(a[cont], i.toString());
			cont++;
		}
		Usuario usuario = this.usuarioService.getCurrentUsuario();
		model.addAttribute("usuario", usuario);
		model.put("municipios", municipios);
		model.addAttribute("municipio", usuario.getMunicipio());
		return UsuarioController.VIEWS_USUARIO_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping(value = "/usuarios/edit")
	public String updateUsuario(@Valid final Usuario usuarioEdit, final BindingResult result, final ModelMap model, final HttpServletRequest request) {

		Usuario usuario = this.usuarioService.getCurrentUsuario();

		if (usuarioEdit.getUsuar().getPassword().equals("")) {
			result.rejectValue("usuar.password", "", "La contraseña no puede estar vacía");

		}

		if (result.hasErrors()) {
			model.addAttribute("usuario", usuarioEdit);
			Map<Object, String> municipios = new HashMap<Object, String>();
			Municipio[] a = Municipio.values();
			int cont = 0;
			for (Municipio i : Municipio.values()) {
				municipios.put(a[cont], i.toString());
				cont++;
			}
			model.put("municipios", municipios);
			return UsuarioController.VIEWS_USUARIO_CREATE_OR_UPDATE_FORM;
		}

		BeanUtils.copyProperties(usuario, usuarioEdit, "nombre", "apellidos", "municipio", "direccion", "email", "usuar");
		usuarioEdit.getUsuar().setUsername(usuario.getUsuar().getUsername());
		usuarioEdit.getUsuar().setEnabled(true);
		this.usuarioService.saveUsuario(usuarioEdit);
		return "redirect:/usuarios/show";
	}

	@GetMapping(value = "/usuarios/disable")
	public String disableUsuario(final ModelMap model) {

		Usuario usuario = this.usuarioService.getCurrentUsuario();
		model.put("usuario", usuario);
		return "usuarios/usuariosDisable";
	}

	@PostMapping(value = "/usuarios/disable")
	public String disableUsuarioForm(final ModelMap model, final HttpServletRequest request) {

		Usuario usuario = this.usuarioService.getCurrentUsuario();
		usuario.getUsuar().setEnabled(false);
		this.usuarioService.saveUsuario(usuario);

		try {

			request.logout();

		} catch (ServletException e) {

			e.printStackTrace();

		}

		return "redirect:/login";

	}

	@GetMapping(value = "/usuarios/delete")
	public String deleteUsuario(final ModelMap model) {
		Usuario usuario = this.usuarioService.getCurrentUsuario();
		model.put("usuario", usuario);
		return "usuarios/usuariosDelete";
	}

	@PostMapping(value = "/usuarios/delete")
	public String deleteUsuarioForm(final ModelMap model, final HttpServletRequest request) {
		Usuario usuario = this.usuarioService.getCurrentUsuario();
		this.usuarioService.deleteUsuario(usuario);

		try {
			request.logout();
		} catch (ServletException e) {
			e.printStackTrace();
		}

		return "redirect:/login";
	}
	
	@GetMapping(value = "/usuarios/edit/password")
	public String updatePassUsuario(final ModelMap model, final HttpServletRequest request) {
	
		Usuario usuario = this.usuarioService.getCurrentUsuario();
		usuario.getUsuar().setPassword("");
		model.addAttribute("usuario", usuario);
		return "usuarios/password";
	}
	
	@PostMapping(value = "/usuarios/edit/password")
	public String updatePassUsuario(@Valid final Usuario usuarioEdit, final BindingResult result, final ModelMap model, final HttpServletRequest request) {

		Usuario usuario = this.usuarioService.getCurrentUsuario();

		if (usuarioEdit.getUsuar().getPassword().equals("")) {
			result.rejectValue("usuar.password", "", "La contraseña no puede estar vacía");
		}

		if (result.hasErrors()) {
			return "usuarios/password";
		}
		BeanUtils.copyProperties(usuario, usuarioEdit, "nombre", "apellidos", "municipio", "direccion", "email", "usuar");
		usuarioEdit.getUsuar().setUsername(usuario.getUsuar().getUsername());
		usuarioEdit.getUsuar().setPassword(MD5.md5(usuarioEdit.getUsuar().getPassword()));
		usuarioEdit.getUsuar().setEnabled(true);
		this.usuarioService.saveUsuario(usuarioEdit);
		return "redirect:/usuarios/show";
}
}
