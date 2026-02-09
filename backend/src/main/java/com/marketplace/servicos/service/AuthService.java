package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AuthResponseDTO;
import com.marketplace.servicos.dto.LoginDTO;
import com.marketplace.servicos.dto.RegisterDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }
    
    @Transactional
    public AuthResponseDTO register(RegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        
        User user = new User();
        user.setNome(registerDTO.getNome());
        user.setLogin(generateLogin(registerDTO.getEmail()));
        user.setEmail(registerDTO.getEmail());
        user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
        user.setTipo(registerDTO.getTipo());
        user.setTelefone(registerDTO.getTelefone());
        user.setCpf(registerDTO.getCpf());
        user.setRg(registerDTO.getRg());
        user.setDataExpedicao(registerDTO.getDataExpedicao());
        
        user = userRepository.save(user);
        
        String token = jwtTokenProvider.generateToken(user);
        
        return new AuthResponseDTO(token, user.getId(), user.getNome(), user.getEmail(), user.getTipo());
    }
    
    private String generateLogin(String email) {
        String login = email.split("@")[0];
        String baseLogin = login;
        int counter = 1;
        
        while (userRepository.existsByLogin(login)) {
            login = baseLogin + counter;
            counter++;
        }
        
        return login;
    }
    
    public AuthResponseDTO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha())
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        
        return new AuthResponseDTO(token, user.getId(), user.getNome(), user.getEmail(), user.getTipo());
    }
}
