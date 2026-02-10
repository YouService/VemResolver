package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AlterarSenhaDTO;
import com.marketplace.servicos.dto.UpdatePerfilDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User atualizarPerfil(Long userId, UpdatePerfilDTO updatePerfilDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!user.getEmail().equals(updatePerfilDTO.getEmail()) &&
            userRepository.existsByEmail(updatePerfilDTO.getEmail())) {
            throw new BusinessException("Email já está em uso por outro usuário");
        }

        user.setNome(updatePerfilDTO.getNome());
        user.setEmail(updatePerfilDTO.getEmail());
        user.setTelefone(updatePerfilDTO.getTelefone());
        user.setCep(updatePerfilDTO.getCep());
        user.setEndereco(updatePerfilDTO.getEndereco());
        user.setCidade(updatePerfilDTO.getCidade());
        user.setEstado(updatePerfilDTO.getEstado());
        user.setRg(updatePerfilDTO.getRg());
        user.setOrgaoExpedidor(updatePerfilDTO.getOrgaoExpedidor());
        user.setDataExpedicaoRg(updatePerfilDTO.getDataExpedicaoRg());

        return userRepository.save(user);
    }

    @Transactional
    public void alterarSenha(Long userId, AlterarSenhaDTO alterarSenhaDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!passwordEncoder.matches(alterarSenhaDTO.getSenhaAtual(), user.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }

        if (!alterarSenhaDTO.getNovaSenha().equals(alterarSenhaDTO.getConfirmarSenha())) {
            throw new BusinessException("Nova senha e confirmação não conferem");
        }

        if (alterarSenhaDTO.getSenhaAtual().equals(alterarSenhaDTO.getNovaSenha())) {
            throw new BusinessException("Nova senha deve ser diferente da atual");
        }

        user.setSenha(passwordEncoder.encode(alterarSenhaDTO.getNovaSenha()));
        userRepository.save(user);
    }

    public User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }
}
