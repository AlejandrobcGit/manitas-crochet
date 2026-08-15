package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.dto.ComentarioDto;
import com.manitascrochet.backend.dto.ComentarioResponseDto;
import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ComentarioNoEncontradoException;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.ComentarioRepository;
import com.manitascrochet.backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ValoracionService servicioValoracion;

    public void eliminarComentario(String comentarioId, String userId) {

        Comentario comentario = comentarioRepository.findByIdAndUsuarioId(comentarioId, userId)
                .orElseThrow(() -> new ComentarioNoEncontradoException());

        comentarioRepository.delete(comentario);
    }

    public List<ComentarioResponseDto> obtenerComentariosFigura(String figuraId) {
        return comentarioRepository.findByFiguraIdOrderByFechaCreacionDesc(figuraId).stream()
                .map(this::convertirAResponseDto)
                .collect(Collectors.toList());
    }

    public Comentario guardarComentario(
            ComentarioDto comentarioDto,
            String userId) {

        Optional<Comentario> comentarioExistente = comentarioRepository.findByUsuarioIdAndFiguraId(
                userId,
                comentarioDto.getFiguraId());

        if (comentarioExistente.isPresent()) {

            Comentario existente = comentarioExistente.get();

            existente.setComentario(comentarioDto.getComentario());
            existente.setFechaModificacion(LocalDateTime.now());

            return comentarioRepository.save(existente);
        }

        LocalDateTime ahora = LocalDateTime.now();
        Comentario comentario = new Comentario();
        comentario.setUsuarioId(userId);
        comentario.setFiguraId(comentarioDto.getFiguraId());
        comentario.setComentario(comentarioDto.getComentario());
        comentario.setFechaCreacion(ahora);
        comentario.setFechaModificacion(ahora);

        return comentarioRepository.save(comentario);
    }



    public Comentario obtenerComentarioUsuarioFigura(
            String figuraId,
            String userId) {

        return comentarioRepository.findByUsuarioIdAndFiguraId(userId,figuraId)
                .orElse(null);
    }

    public ComentarioResponseDto convertirAResponseDto(Comentario comentario) {

        String nombreUsuario = usuarioRepository.findById(comentario.getUsuarioId())
                .map(Usuario::getUsername)
                .orElse(comentario.getUsuarioId());

        ValoracionDto valoracion = servicioValoracion.obtenerValoracionUsuario(comentario.getUsuarioId(), comentario.getFiguraId());

        return new ComentarioResponseDto(
                nombreUsuario,
                comentario.getFiguraId(),
                valoracion.getPuntuacion(),
                comentario.getComentario(),
                comentario.getFechaCreacion(),
                comentario.getFechaModificacion()
        );
    }


    public void eliminarComentariosPorFigura(String figuraId) {
        comentarioRepository.deleteByFiguraId(figuraId);
    }
}
