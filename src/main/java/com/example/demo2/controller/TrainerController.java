package com.example.demo2.controller;

import com.example.demo2.model.Trainer;
import com.example.demo2.repository.TrainerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerRepository trainerRepository;

    public TrainerController(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    public List<Trainer> getAll() {
        return trainerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Trainer getById(@PathVariable Long id) {
        return trainerRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Trainer create(@RequestBody Trainer trainer) {
        return trainerRepository.save(trainer);
    }

    @PutMapping("/{id}")
    public Trainer update(@PathVariable Long id, @RequestBody Trainer trainer) {
        trainer.setId(id);
        return trainerRepository.save(trainer);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        trainerRepository.deleteById(id);
    }
}