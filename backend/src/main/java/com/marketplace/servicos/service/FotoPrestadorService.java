package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.FotoPrestadorDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.FotoPrestador;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.repository.FotoPrestadorRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FotoPrestadorService {

    private static final int MAX_FOTOS = 10;
    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> TIPOS_ACEITOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final FotoPrestadorRepository fotoRepository;
    private final PrestadorRepository prestadorRepository;

    @Value("${upload.dir:/uploads}")
    private String uploadDir;

    public FotoPrestadorService(FotoPrestadorRepository fotoRepository, PrestadorRepository prestadorRepository) {
        this.fotoRepository = fotoRepository;
        this.prestadorRepository = prestadorRepository;
    }

    @Transactional
    public FotoPrestadorDTO upload(MultipartFile file, Long usuarioId) {
        if (file.isEmpty()) throw new BusinessException("Arquivo vazio");
        if (!TIPOS_ACEITOS.contains(file.getContentType()))
            throw new BusinessException("Tipo de arquivo não suportado. Use JPEG, PNG ou WEBP");
        if (file.getSize() > MAX_BYTES)
            throw new BusinessException("Arquivo muito grande. Tamanho máximo: 5 MB");

        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado"));

        if (fotoRepository.countByPrestadorId(prestador.getId()) >= MAX_FOTOS)
            throw new BusinessException("Limite de " + MAX_FOTOS + " fotos atingido");

        String ext = extensao(file.getContentType());
        String nomeArquivo = UUID.randomUUID() + ext;
        Path diretorio = Paths.get(uploadDir, "prestadores", prestador.getId().toString());

        try {
            Files.createDirectories(diretorio);
            Files.copy(file.getInputStream(), diretorio.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar arquivo: " + e.getMessage());
        }

        String urlRelativa = "/uploads/prestadores/" + prestador.getId() + "/" + nomeArquivo;

        FotoPrestador foto = new FotoPrestador();
        foto.setPrestador(prestador);
        foto.setUrl(urlRelativa);
        foto = fotoRepository.save(foto);

        return toDTO(foto);
    }

    @Transactional
    public void deletar(Long fotoId, Long usuarioId) {
        FotoPrestador foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada"));

        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado"));

        if (!foto.getPrestador().getId().equals(prestador.getId()))
            throw new BusinessException("Você não tem permissão para excluir esta foto");

        Path arquivo = Paths.get(uploadDir + foto.getUrl().replace("/uploads", ""));
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            // arquivo já ausente do disco — remove o registro mesmo assim
        }

        fotoRepository.delete(foto);
    }

    @Transactional(readOnly = true)
    public List<FotoPrestadorDTO> listar(Long prestadorId) {
        if (!prestadorRepository.existsById(prestadorId))
            throw new ResourceNotFoundException("Prestador não encontrado");
        return fotoRepository.findByPrestadorIdOrderByDataUploadAsc(prestadorId)
                .stream().map(this::toDTO).toList();
    }

    private FotoPrestadorDTO toDTO(FotoPrestador foto) {
        return new FotoPrestadorDTO(foto.getId(), foto.getUrl(), foto.getDataUpload());
    }

    private String extensao(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
