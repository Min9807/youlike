package com.ll.gramgram.boundedContext.instaMember.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class InstaMemberSnapshot extends InstaMemberBase {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String eventTypeCode;

    private String username;

    @ToString.Exclude
    @ManyToOne
    private InstaMember instaMember;
}