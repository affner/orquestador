package mx.com.actinver.auth.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import mx.com.actinver.auth.component.JwtTokenComponent;
import mx.com.actinver.auth.dto.AuthRequestDto;
import mx.com.actinver.auth.dto.AuthResponseDto;
import mx.com.actinver.common.exception.InvalidParamException;

@Service
public class AuthService {

	private static final Logger LOG = LogManager.getLogger(AuthService.class);

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenComponent jwtTokenComponent;

	@Autowired
	private UserDetailsService userDetailsService;

	public AuthResponseDto getToken(AuthRequestDto request) {
		String token = "";

		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			if (auth.isAuthenticated()) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

				token = jwtTokenComponent.generateToken(userDetails);
			}
		} catch (Exception e) {
			LOG.error("Invalid credentials.", e);

			throw new InvalidParamException("Credenciales invalidas.");
		}

		return AuthResponseDto.builder().token(token).build();
	}

}
