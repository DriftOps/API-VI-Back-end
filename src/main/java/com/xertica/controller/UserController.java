package com.xertica.controller;

import com.xertica.model.User;
import com.xertica.service.UserService;
import com.xertica.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // GET /usuarios/{id}/mensagem
    @GetMapping("/{id}/mensagem")
    public ResponseEntity<String> getMensagemPersonalizada(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    String mensagem = userService.gerarMensagemPersonalizada(user);
                    return ResponseEntity.ok(mensagem);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
