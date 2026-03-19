package com.example.demo2.controller;

import com.example.demo2.model.Lesson;
import com.example.demo2.model.Trainer;
import com.example.demo2.repository.LessonRepository;
import com.example.demo2.repository.TrainerRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonRepository lessonRepository;
    private final TrainerRepository trainerRepository;

    public LessonController(LessonRepository lessonRepository, TrainerRepository trainerRepository) {
        this.lessonRepository = lessonRepository;
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    public List<Lesson> getAll() {
        return lessonRepository.findAll();
    }

    @GetMapping("/{id}")
    public Lesson getById(@PathVariable Long id) {
        return lessonRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Lesson create(@RequestBody LessonRequest request) {
        Trainer trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        Lesson lesson = new Lesson(request.getTitle(), trainer,
                request.getStartTime(), request.getEndTime(), request.getCapacity());
        return lessonRepository.save(lesson);
    }

    @PutMapping("/{id}")
    public Lesson update(@PathVariable Long id, @RequestBody LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Trainer trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        lesson.setTitle(request.getTitle());
        lesson.setTrainer(trainer);
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
        lesson.setCapacity(request.getCapacity());
        return lessonRepository.save(lesson);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        lessonRepository.deleteById(id);
    }

    static class LessonRequest {
        private String title;
        private Long trainerId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int capacity;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getTrainerId() { return trainerId; }
        public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
    }
}