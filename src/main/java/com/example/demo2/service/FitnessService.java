package com.example.demo2.service;

import com.example.demo2.model.*;
import com.example.demo2.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FitnessService {

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LessonRepository lessonRepository;
    private final BookingRepository bookingRepository;

    public FitnessService(MemberRepository memberRepository,
                          TrainerRepository trainerRepository,
                          SubscriptionRepository subscriptionRepository,
                          LessonRepository lessonRepository,
                          BookingRepository bookingRepository) {
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.lessonRepository = lessonRepository;
        this.bookingRepository = bookingRepository;
    }

    // 1. Оформление брони
    @Transactional
    public Booking bookLesson(Long memberId, Long lessonId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Проверка активной подписки
        boolean hasActiveSubscription = subscriptionRepository.findAll().stream()
                .filter(s -> s.getMember().getId().equals(memberId))
                .anyMatch(s -> !s.getEndDate().isBefore(lesson.getStartTime().toLocalDate()));

        if (!hasActiveSubscription) {
            throw new RuntimeException("Member has no active subscription for this lesson date");
        }

        // Проверка свободных мест
        long activeBookingsCount = bookingRepository.findAll().stream()
                .filter(b -> b.getLesson().getId().equals(lessonId) && "ACTIVE".equals(b.getStatus()))
                .count();
        if (activeBookingsCount >= lesson.getCapacity()) {
            throw new RuntimeException("No available seats for this lesson");
        }

        // Проверка, не забронировал ли уже этот участник
        boolean alreadyBooked = bookingRepository.findAll().stream()
                .anyMatch(b -> b.getMember().getId().equals(memberId) &&
                        b.getLesson().getId().equals(lessonId) &&
                        "ACTIVE".equals(b.getStatus()));
        if (alreadyBooked) {
            throw new RuntimeException("Member already booked this lesson");
        }

        Booking booking = new Booking(member, lesson, LocalDateTime.now(), "ACTIVE");
        return bookingRepository.save(booking);
    }

    // 2. Отмена брони
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Проверка, можно ли отменить (до начала занятия > 2 часов)
        LocalDateTime now = LocalDateTime.now();
        if (booking.getLesson().getStartTime().minusHours(2).isBefore(now)) {
            throw new RuntimeException("Cannot cancel booking less than 2 hours before start");
        }

        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    // 3. Продление подписки
    @Transactional
    public Subscription extendSubscription(Long subscriptionId, LocalDate newEndDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (newEndDate.isBefore(subscription.getEndDate())) {
            throw new RuntimeException("New end date must be after current end date");
        }
        subscription.setEndDate(newEndDate);
        return subscriptionRepository.save(subscription);
    }

    // 4. Расписание тренера
    public List<Lesson> getTrainerSchedule(Long trainerId, LocalDateTime from, LocalDateTime to) {
        return lessonRepository.findAll().stream()
                .filter(l -> l.getTrainer().getId().equals(trainerId))
                .filter(l -> l.getStartTime().isAfter(from) && l.getEndTime().isBefore(to))
                .toList();
    }

    // 5. Активные брони участника
    public List<Booking> getMemberActiveBookings(Long memberId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getMember().getId().equals(memberId) && "ACTIVE".equals(b.getStatus()))
                .toList();
    }
}