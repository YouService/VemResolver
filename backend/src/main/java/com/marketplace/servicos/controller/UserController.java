package com.marketplace.servicos.controller;

import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody User userUpdate) {
        String email = userDetails.getUsername();
        User currentUser = userService.findByEmail(email);
        User updatedUser = userService.update(currentUser.getId(), userUpdate);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userService.findByEmail(email);
        userService.delete(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
