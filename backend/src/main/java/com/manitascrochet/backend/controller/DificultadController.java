package com.manitascrochet.backend.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.model.Dificultad;

@RestController
@RequestMapping("/api/dificultades")
public class DificultadController {


    // GET /api/dificultades
    @GetMapping
    public List<Dificultad> obtenerTodas() {
       return Arrays.asList(Dificultad.values());
    }

}