package com.marketplace.servicos.service;

import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    @Transactional
    public User update(Long id, User userUpdate) {
        User existingUser = findById(id);
        
        existingUser.setNome(userUpdate.getNome());
        existingUser.setTelefone(userUpdate.getTelefone());
        
        if (userUpdate.getSenha() != null && !userUpdate.getSenha().isEmpty()) {
            existingUser.setSenha(passwordEncoder.encode(userUpdate.getSenha()));
        }
        
        return userRepository.save(existingUser);
    }
    
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        user.setAtivo(false);
        userRepository.save(user);
    }
}
