package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.CategoriaDTO;
import com.marketplace.servicos.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias de serviços")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoriaController {
    
    private final CategoriaService categoriaService;
    
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    
    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public ResponseEntity<Page<CategoriaDTO>> listarTodas(Pageable pageable) {
        return ResponseEntity.ok(categoriaService.listarTodas(pageable));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }
    
    @PostMapping
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar nova categoria")
    public ResponseEntity<CategoriaDTO> criar(@Valid @RequestBody CategoriaDTO dto) {
        CategoriaDTO criada = categoriaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
    
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<CategoriaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar categoria")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
