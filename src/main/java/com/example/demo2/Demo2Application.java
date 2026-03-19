package com.example.demo2;

import com.example.demo2.model.*;
import com.example.demo2.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class Demo2Application {

	public static void main(String[] args) {
		SpringApplication.run(Demo2Application.class, args);
	}

	@Bean
	public CommandLineRunner initData(MemberRepository memberRepository,
									  TrainerRepository trainerRepository,
									  SubscriptionRepository subscriptionRepository,
									  LessonRepository lessonRepository,
									  BookingRepository bookingRepository) {
		return args -> {
			// Проверим, есть ли уже данные
			if (memberRepository.count() == 0) {
				// Создаём участников
				Member member1 = new Member("Иван Петров", "ivan@example.com", "+71234567890", LocalDate.now());
				Member member2 = new Member("Мария Сидорова", "maria@example.com", "+79876543210", LocalDate.now().minusDays(5));
				memberRepository.save(member1);
				memberRepository.save(member2);

				// Создаём тренеров
				Trainer trainer1 = new Trainer("Анна Смирнова", "yoga", "+71112223344");
				Trainer trainer2 = new Trainer("Пётр Иванов", "fitness", "+72223334455");
				trainerRepository.save(trainer1);
				trainerRepository.save(trainer2);

				// Подписки
				Subscription sub1 = new Subscription(member1, LocalDate.now(), LocalDate.now().plusMonths(1), "monthly");
				Subscription sub2 = new Subscription(member2, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), "monthly");
				subscriptionRepository.save(sub1);
				subscriptionRepository.save(sub2);

				// Занятия
				Lesson lesson1 = new Lesson("Yoga Morning", trainer1,
						LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
						LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), 10);
				Lesson lesson2 = new Lesson("Fitness Express", trainer2,
						LocalDateTime.now().plusDays(1).withHour(18).withMinute(0),
						LocalDateTime.now().plusDays(1).withHour(19).withMinute(0), 15);
				lessonRepository.save(lesson1);
				lessonRepository.save(lesson2);

				// Брони
				Booking booking1 = new Booking(member1, lesson1, LocalDateTime.now(), "ACTIVE");
				Booking booking2 = new Booking(member2, lesson2, LocalDateTime.now(), "ACTIVE");
				bookingRepository.save(booking1);
				bookingRepository.save(booking2);

				System.out.println("Тестовые данные добавлены");
			}
		};
	}
}