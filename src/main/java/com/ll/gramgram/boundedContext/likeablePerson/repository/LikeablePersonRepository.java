package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeablePersonRepository extends JpaRepository<LikeablePerson, Long> {
    Optional<LikeablePerson> findById(Long id);
    Optional<LikeablePerson> findByFromInstaMemberAndToInstaMember(InstaMember fromInstaMember, InstaMember toInstaMember);
    List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId);
}