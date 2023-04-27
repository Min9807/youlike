package com.ll.gramgram.boundedContext.instaMember.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@Getter
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@ToString(callSuper = true)
public abstract class InstaMemberBase {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    String gender;

    long likesCountByGenderWomanAndAttractiveTypeCode1;
    long likesCountByGenderWomanAndAttractiveTypeCode2;
    long likesCountByGenderWomanAndAttractiveTypeCode3;
    long likesCountByGenderManAndAttractiveTypeCode1;
    long likesCountByGenderManAndAttractiveTypeCode2;
    long likesCountByGenderManAndAttractiveTypeCode3;

    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;

    public Long getLikesCountByGenderWoman() {
        return likesCountByGenderWomanAndAttractiveTypeCode1 + likesCountByGenderWomanAndAttractiveTypeCode2 + likesCountByGenderWomanAndAttractiveTypeCode3;
    }

    public Long getLikesCountByGenderMan() {
        return likesCountByGenderManAndAttractiveTypeCode1 + likesCountByGenderManAndAttractiveTypeCode2 + likesCountByGenderManAndAttractiveTypeCode3;
    }

    public Long getLikesCountByAttractionTypeCode1() {
        return likesCountByGenderWomanAndAttractiveTypeCode1 + likesCountByGenderManAndAttractiveTypeCode1;
    }

    public Long getLikesCountByAttractionTypeCode2() {
        return likesCountByGenderWomanAndAttractiveTypeCode2 + likesCountByGenderManAndAttractiveTypeCode2;
    }

    public Long getLikesCountByAttractionTypeCode3() {
        return likesCountByGenderWomanAndAttractiveTypeCode3 + likesCountByGenderManAndAttractiveTypeCode3;
    }

    public Long getLikes() {
        return getLikesCountByGenderWoman() + getLikesCountByGenderMan();
    }
}
