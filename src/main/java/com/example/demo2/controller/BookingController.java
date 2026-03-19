package com.example.demo2.controller;

import com.example.demo2.model.Booking;
import com.example.demo2.model.Member;
import com.example.demo2.model.Lesson;
import com.example.demo2.repository.BookingRepository;
import com.example.demo2.repository.MemberRepository;
import com.example.demo2.repository.LessonRepository;
import com.example.demo2.service.FitnessService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final LessonRepository lessonRepository;
    private final FitnessService fitnessService;

    public BookingController(BookingRepository bookingRepository,
                             MemberRepository memberRepository,
                             LessonRepository lessonRepository,
                             FitnessService fitnessService) {
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
        this.lessonRepository = lessonRepository;
        this.fitnessService = fitnessService;
    }

    @GetMapping
    public List<Booking> getAll() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Booking create(@RequestBody BookingRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Booking booking = new Booking(member, lesson, LocalDateTime.now(), "ACTIVE");
        return bookingRepository.save(booking);
    }

    @PutMapping("/{id}")
    public Booking update(@PathVariable Long id, @RequestBody BookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        booking.setMember(member);
        booking.setLesson(lesson);
        booking.setBookingTime(request.getBookingTime());
        booking.setStatus(request.getStatus());
        return bookingRepository.save(booking);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookingRepository.deleteById(id);
    }

    // Новые бизнес-операции
    @PostMapping("/book")
    public Booking bookLesson(@RequestParam Long memberId, @RequestParam Long lessonId) {
        return fitnessService.bookLesson(memberId, lessonId);
    }

    @PostMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return fitnessService.cancelBooking(id);
    }

    @GetMapping("/member/{memberId}/active")
    public List<Booking> getMemberActiveBookings(@PathVariable Long memberId) {
        return fitnessService.getMemberActiveBookings(memberId);
    }

    static class BookingRequest {
        private Long memberId;
        private Long lessonId;
        private LocalDateTime bookingTime;
        private String status;

        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
        public LocalDateTime getBookingTime() { return bookingTime; }
        public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}