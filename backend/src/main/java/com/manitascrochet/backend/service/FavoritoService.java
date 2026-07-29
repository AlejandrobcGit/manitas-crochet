package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.model.Favorito;
import com.manitascrochet.backend.repository.FavoritoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;

    public boolean cambiarFavorito(String usuario, String figuraId) {

        Favorito favorito = favoritoRepository.findByUsuarioIdAndFiguraIdAndActivoTrue(
                usuario,
                figuraId);

        if (favorito == null) {

            Favorito favoritoNuevo = new Favorito();

            favoritoNuevo.setId(null);
            favoritoNuevo.setUsuarioId(usuario);
            favoritoNuevo.setFiguraId(figuraId);
            favoritoNuevo.setFechaAlta(LocalDateTime.now());
            favoritoNuevo.setFechaBaja(null);
            favoritoNuevo.setActivo(true);

            favoritoRepository.save(favoritoNuevo);

            return true;

        } else {

            favorito.setFechaBaja(LocalDateTime.now());
            favorito.setActivo(false);

            favoritoRepository.save(favorito);

            return false;
        }
    }

    public List<String> obtenerFavoritosActivos(String usuario) {

        return favoritoRepository
                .findByUsuarioIdAndActivoTrue(usuario)
                .stream()
                .map(Favorito::getFiguraId)
                .toList();
    }
}
