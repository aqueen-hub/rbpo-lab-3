package com.example.demo2.controller;

import com.example.demo2.model.Member;
import com.example.demo2.model.Subscription;
import com.example.demo2.repository.MemberRepository;
import com.example.demo2.repository.SubscriptionRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    public SubscriptionController(SubscriptionRepository subscriptionRepository,
                                  MemberRepository memberRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public List<Subscription> getAll() {
        return subscriptionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Subscription getById(@PathVariable Long id) {
        return subscriptionRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Subscription create(@RequestBody SubscriptionRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Subscription subscription = new Subscription(member, request.getStartDate(),
                request.getEndDate(), request.getType());
        return subscriptionRepository.save(subscription);
    }

    @PutMapping("/{id}")
    public Subscription update(@PathVariable Long id, @RequestBody SubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        subscription.setMember(member);
        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(request.getEndDate());
        subscription.setType(request.getType());
        return subscriptionRepository.save(subscription);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        subscriptionRepository.deleteById(id);
    }

    // Внутренний класс DTO
    static class SubscriptionRequest {
        private Long memberId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String type;

        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}