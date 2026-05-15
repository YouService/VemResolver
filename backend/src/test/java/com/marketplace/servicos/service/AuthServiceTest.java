package com.marketplace.servicos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.marketplace.servicos.dto.AuthResponseDTO;
import com.marketplace.servicos.dto.LoginDTO;
import com.marketplace.servicos.dto.RegisterDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.model.VerificationToken;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.repository.VerificationTokenRepository;
import com.marketplace.servicos.model.RefreshToken;
import com.marketplace.servicos.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_NewUser_SavesUserAndSendsEmail() {
        RegisterDTO dto = new RegisterDTO();
        dto.setNome("Fulano");
        dto.setEmail("fulano@example.com");
        dto.setCpf("111.111.111-11");
        String rawPassword = "Password123";
        String encoded = Base64.getEncoder().encodeToString(rawPassword.getBytes());
        dto.setSenha(encoded);
        dto.setTipo(TipoUsuario.CLIENTE);

        when(userRepository.existsByCpfAndAtivoTrue(any())).thenReturn(false);
        when(userRepository.existsByEmailAndAtivoTrue(dto.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResponseDTO response = authService.register(dto);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq(dto.getEmail()), anyString());
    }

    @Test
    void register_ExistingActiveEmail_Throws() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("active@example.com");
        dto.setCpf("222.222.222-22");
        dto.setSenha(Base64.getEncoder().encodeToString("Xx123456".getBytes()));

        when(userRepository.existsByCpfAndAtivoTrue(any())).thenReturn(false);
        when(userRepository.existsByEmailAndAtivoTrue(dto.getEmail())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(dto));
    }

    @Test
    void verifyAccount_ValidToken_ActivatesUser() {
        User user = new User();
        user.setId(2L);
        user.setNome("Teste");
        user.setEmail("t@example.com");
        user.setAtivo(false);

        VerificationToken token = new VerificationToken("tok", user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(verificationTokenRepository.findByToken("tok")).thenReturn(token);

        authService.verifyAccount("tok");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getAtivo());
        verify(verificationTokenRepository).delete(token);
    }

    @Test
    void verifyAccount_InvalidToken_Throws() {
        when(verificationTokenRepository.findByToken("bad")).thenReturn(null);
        assertThrows(BusinessException.class, () -> authService.verifyAccount("bad"));
    }

    @Test
    void login_InactiveUser_Throws() {
        User user = new User();
        user.setAtivo(false);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        LoginDTO dto = new LoginDTO("u@x.com", "pwd");

        assertThrows(BusinessException.class, () -> authService.login(dto));
    }

    @Test
    void login_ActiveUser_ReturnsToken() {
        User user = new User();
        user.setId(5L);
        user.setNome("Ok");
        user.setEmail("ok@example.com");
        user.setAtivo(true);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-token");
        when(refreshTokenService.generateRefreshToken(user)).thenReturn(rt);

        LoginDTO dto = new LoginDTO("ok@example.com", "pwd");

        AuthResponseDTO resp = authService.login(dto);

        assertNotNull(resp);
        assertEquals("jwt-token", resp.getToken());
        assertEquals(5L, resp.getUserId());
    }

    @Test
    void deletarConta_UserExists_SetsInactive() {
        User user = new User();
        user.setId(10L);
        user.setAtivo(true);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        authService.deletarConta(10L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().getAtivo());
    }

}
