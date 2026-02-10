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

import java.util.Optional;

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
        if (userRepository.existsByEmailAndAtivoTrue(registerDTO.getEmail())) {
            throw new BusinessException("Este email já está cadastrado em nosso sistema. Se você esqueceu sua senha, use a opção de recuperação de senha.");
        }

        Optional<User> existingUser = userRepository.findByEmail(registerDTO.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!user.getAtivo()) {
                user.setNome(registerDTO.getNome());
                user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
                user.setTipo(registerDTO.getTipo());
                user.setTelefone(registerDTO.getTelefone());
                user.setCpf(registerDTO.getCpf());
                user.setRg(registerDTO.getRg());
                user.setOrgaoExpedidor(registerDTO.getOrgaoExpedidor());
                user.setDataExpedicaoRg(registerDTO.getDataExpedicaoRg());
                user.setAtivo(true);

                String token = jwtTokenProvider.generateToken(user);
                return new AuthResponseDTO(token, user.getId(), user.getNome(), user.getEmail(), user.getTipo());
            }
        }

        User user = new User();
        user.setNome(registerDTO.getNome());
        user.setEmail(registerDTO.getEmail());
        user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
        user.setTipo(registerDTO.getTipo());
        user.setTelefone(registerDTO.getTelefone());
        user.setCpf(registerDTO.getCpf());
        user.setRg(registerDTO.getRg());
        user.setOrgaoExpedidor(registerDTO.getOrgaoExpedidor());
        user.setDataExpedicaoRg(registerDTO.getDataExpedicaoRg());

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);

        return new AuthResponseDTO(token, user.getId(), user.getNome(), user.getEmail(), user.getTipo());
    }
    
    public AuthResponseDTO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha())
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        
        return new AuthResponseDTO(token, user.getId(), user.getNome(), user.getEmail(), user.getTipo());
    }
    
    @Transactional
    public void deletarConta(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        
        user.setAtivo(false);
        userRepository.save(user);
    }
}
