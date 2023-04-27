package com.ll.gramgram.boundedContext.instaMember.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMemberSnapshot;
import com.ll.gramgram.boundedContext.instaMember.repository.InstaMemberRepository;
import com.ll.gramgram.boundedContext.instaMember.repository.InstaMemberSnapshotRepository;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstaMemberService {
    private final InstaMemberRepository instaMemberRepository;
    private final MemberService memberService;
    private final InstaMemberSnapshotRepository instaMemberSnapshotRepository;

    public Optional<InstaMember> findByUsername(String username) {
        return instaMemberRepository.findByUsername(username);
    }

    @Transactional
    public RsData<InstaMember> connect(Member member, String username, String gender) {
        Optional<InstaMember> opInstaMember = findByUsername(username);

        if (opInstaMember.isPresent() && !opInstaMember.get().getGender().equals("U")) {
            return RsData.of("F-1", "해당 인스타그램 아이디는 이미 다른 사용자와 연결되었습니다.");
        }

        RsData<InstaMember> instaMemberRsData = findByUsernameOrCreate(username, gender);

        memberService.updateInstaMember(member, instaMemberRsData.getData());

        return instaMemberRsData;
    }

    private RsData<InstaMember> create(String username, String gender) {
        InstaMember instaMember = InstaMember
                .builder()
                .username(username)
                .gender(gender)
                .build();

        instaMemberRepository.save(instaMember);

        return RsData.of("S-1", "인스타계정이 등록되었습니다.", instaMember);
    }

    @Transactional
    public RsData<InstaMember> findByUsernameOrCreate(String username) {
        Optional<InstaMember> opInstaMember = findByUsername(username);

        return opInstaMember
                .map(instaMember -> RsData.of("S-2", "인스타계정이 등록되었습니다.", instaMember))
                .orElseGet(() -> create(username, "U"));
    }

    @Transactional
    public RsData<InstaMember> findByUsernameOrCreate(String username, String gender) {
        Optional<InstaMember> opInstaMember = findByUsername(username);

        if (opInstaMember.isPresent()) {
            InstaMember instaMember = opInstaMember.get();
            instaMember.updateGender(gender); // 성별세팅
            instaMemberRepository.save(instaMember);

            return RsData.of("S-2", "인스타계정이 등록되었습니다.", instaMember);
        }

        return create(username, gender);
    }

    private void saveSnapshot(InstaMemberSnapshot snapshot) {
        instaMemberSnapshotRepository.save(snapshot);
    }

    public void whenAfterModifyAttractiveType(LikeablePerson likeablePerson, int oldAttractiveTypeCode) {
        InstaMember fromInstaMember = likeablePerson.getFromInstaMember();
        InstaMember toInstaMember = likeablePerson.getToInstaMember();

        toInstaMember.decreaseLikesCount(fromInstaMember.getGender(), oldAttractiveTypeCode);
        toInstaMember.increaseLikesCount(fromInstaMember.getGender(), likeablePerson.getAttractiveTypeCode());

        InstaMemberSnapshot snapshot = toInstaMember.snapshot("ModifyAttractiveType");

        saveSnapshot(snapshot);
    }

    public void whenAfterLike(LikeablePerson likeablePerson) {
        InstaMember fromInstaMember = likeablePerson.getFromInstaMember();
        InstaMember toInstaMember = likeablePerson.getToInstaMember();

        toInstaMember.increaseLikesCount(fromInstaMember.getGender(), likeablePerson.getAttractiveTypeCode());

        InstaMemberSnapshot snapshot = toInstaMember.snapshot("Like");

        saveSnapshot(snapshot);

        // 알림
    }

    public void whenBeforeCancelLike(LikeablePerson likeablePerson) {
        InstaMember fromInstaMember = likeablePerson.getFromInstaMember();
        InstaMember toInstaMember = likeablePerson.getToInstaMember();

        toInstaMember.decreaseLikesCount(fromInstaMember.getGender(), likeablePerson.getAttractiveTypeCode());

        InstaMemberSnapshot snapshot = toInstaMember.snapshot("CancelLike");

        saveSnapshot(snapshot);
    }

    public void whenAfterFromInstaMemberChangeGender(InstaMember instaMember, String oldGender) {
        instaMember
                .getFromLikeablePeople()
                .forEach(likeablePerson -> {
                    InstaMember toInstaMember = likeablePerson.getToInstaMember();
                    toInstaMember.decreaseLikesCount(oldGender, likeablePerson.getAttractiveTypeCode());
                    toInstaMember.increaseLikesCount(instaMember.getGender(), likeablePerson.getAttractiveTypeCode());

                    InstaMemberSnapshot snapshot = toInstaMember.snapshot("FromInstaMemberChangeGender");

                    saveSnapshot(snapshot);
                });
    }
}