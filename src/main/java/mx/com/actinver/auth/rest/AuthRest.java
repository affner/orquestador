package mx.com.actinver.auth.rest;

import javax.validation.Valid;

import mx.com.actinver.auth.dto.AuthResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mx.com.actinver.auth.dto.AuthRequestDto;
import mx.com.actinver.auth.service.AuthService;

@RestController
@RequestMapping("oauth")
@Api(tags = { "Auth" })
public class AuthRest {

	@Autowired
	private AuthService authService;

	@ApiOperation(value = "Autenticar", notes = "Permite la autenticacion y obtener un token de autorizacion.")
	@PostMapping("token")
	public ResponseEntity<AuthResponseDto> getToken(@Valid @RequestBody AuthRequestDto request) {
		return ResponseEntity.ok(authService.getToken(request));
	}

}
