package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AuthResponseDTO;
import com.marketplace.servicos.dto.LoginDTO;
import com.marketplace.servicos.dto.RegisterDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.model.VerificationToken;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.repository.VerificationTokenRepository;
import com.marketplace.servicos.security.JwtTokenProvider;
import com.marketplace.servicos.utils.DocumentUtils;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    public AuthService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository,
                      EmailService emailService, PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
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
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (!user.getAtivo()) {
                // Reativação de conta: atualiza dados e envia email de verificação
                user.setNome(registerDTO.getNome());
                user.setLogin(registerDTO.getEmail());
                user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
                user.setTipo(registerDTO.getTipo());
                user.setTelefone(registerDTO.getTelefone());                
                user.setCpf(DocumentUtils.somenteNumeroCpf(registerDTO.getCpf()));
                user.setRg(registerDTO.getRg());
                user.setOrgaoExpedidor(registerDTO.getOrgaoExpedidor());
                user.setDataExpedicaoRg(registerDTO.getDataExpedicaoRg());            
            } else {
                 // Caso raro onde existsByEmailAndAtivoTrue falhou ou concorrência
                 throw new BusinessException("Usuário já existe.");
            }
        } else {
            // Novo cadastro
            user = new User();
            user.setDataCadastro(LocalDateTime.now().withNano(0));
            user.setNome(registerDTO.getNome());
            user.setEmail(registerDTO.getEmail());
            user.setLogin(registerDTO.getEmail());
            user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
            user.setTipo(registerDTO.getTipo());
            user.setTelefone(registerDTO.getTelefone());
            user.setCpf(DocumentUtils.somenteNumeroCpf(registerDTO.getCpf()));
            user.setRg(registerDTO.getRg());
            user.setOrgaoExpedidor(registerDTO.getOrgaoExpedidor());
            user.setDataExpedicaoRg(registerDTO.getDataExpedicaoRg());       
            user.setCep(registerDTO.getCep());
            user.setEndereco(registerDTO.getEndereco());
            user.setCidade(registerDTO.getCidade());
            user.setEstado(registerDTO.getEstado());    
            user.setNumero(registerDTO.getNumero());
            user.setComplemento(registerDTO.getComplemento());       
            user.setAtivo(false); // Inativo até confirmar email
        }

        user = userRepository.save(user);

        // Gerar token de verificação
        String tokenUuid = UUID.randomUUID().toString();
        System.out.println("=== DEBUG REGISTRO ===");
        System.out.println("Token gerado: " + tokenUuid);
        VerificationToken verificationToken = new VerificationToken(tokenUuid, user);
        verificationTokenRepository.save(verificationToken);
        System.out.println("Token salvo no banco");
        System.out.println("======================");

        // Enviar email
        emailService.sendVerificationEmail(user.getEmail(), tokenUuid);

        // Retorna null no token para indicar que precisa verificar email
        return new AuthResponseDTO(null, user.getId(), user.getNome(), user.getEmail(), user.getTipo());
    }
    
    @Transactional
    public void verifyAccount(String token) {
        System.out.println("=== DEBUG VERIFICAÇÃO ===");
        System.out.println("Token recebido: " + token);
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        System.out.println("Token encontrado no banco: " + (verificationToken != null ? "SIM" : "NÃO"));
        
        if (verificationToken == null) {
            throw new BusinessException("Token de verificação inválido.");
        }
        
        System.out.println("Data de expiração: " + verificationToken.getExpiryDate());
        System.out.println("Data atual: " + LocalDateTime.now());
        
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
             throw new BusinessException("O link de verificação expirou. Solicite um novo.");
        }
        
        User user = verificationToken.getUser();
        user.setAtivo(true);
        userRepository.save(user);
        
        verificationTokenRepository.delete(verificationToken);
        System.out.println("Conta verificada com sucesso!");
        System.out.println("=========================");
    }
    
    public AuthResponseDTO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha())
        );
        
        User user = (User) authentication.getPrincipal();
        
        // Verificação extra de segurança, embora o AuthenticationManager já deva checar isEnabled()
        if (!user.getAtivo()) {
            throw new BusinessException("Sua conta ainda não foi ativada. Verifique seu email.");
        }

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