package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.event.EventAfterLike;
import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.event.EventBeforeCancelLike;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        if(fromInstaMember.getFromLikeablePeople().size()>=10){ // 호감리스트 길이가 11명이상 인지 확인
            return RsData.of("F-2", "11명 이상의 호감상대를 등록할 수 없습니다.");
        }

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .modifyUnlockDate(AppConfig.genLikeablePersonModifyUnlockDate())
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        publisher.publishEvent(new EventAfterLike(this, likeablePerson));
        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    // 검증을 통해 매력포인트 수정하는 서비스
    @Transactional
    public RsData<LikeablePerson> modify(LikeablePerson likeablePerson, int attractiveTypeCode) {
        String preAttractiveType = likeablePerson.getAttractiveTypeDisplayName(); // 현재 매력포인트
        boolean canModifyAttractiveType = checkSameAttractiveType(likeablePerson, attractiveTypeCode);
        if(canModifyAttractiveType){ // 현재 매력 포인트와 선택한 매력포인트가 다를 경우 호감사유 수정
            likeablePerson.updateAttractionTypeCode(attractiveTypeCode);
            modifyAttractionTypeCode(likeablePerson, attractiveTypeCode);
            String nowAttractiveType = likeablePerson.getAttractiveTypeDisplayName(); // 선택한 매력포인트 한글 패치(?)
            return RsData.of("S-1", "%s에 대한 호감사유를 %s에서 %s으로 변경합니다.".formatted(likeablePerson.getFromInstaMemberUsername(), preAttractiveType, nowAttractiveType), likeablePerson);
        }
        else{ // 현재 매력 포인트와 선택한 매력포인트가 같을 경우 Fail
            return RsData.of("F-2", "이미 등록한 호감상대는 등록할 수 없습니다.");
        }

    }

    // 현재의 매력포인트와 선택한 매력포인트가 같은지 확인
    @Transactional
    public boolean checkSameAttractiveType(LikeablePerson modifyAttractiveType, int attractiveTypeCode) {
        int preAttractiveTypeCode = modifyAttractiveType.getAttractiveTypeCode(); // 현재 매력포인트 코드
        if(preAttractiveTypeCode==attractiveTypeCode){ // 현재 매력 포인트와 선택한 매력포인트가 같을 경우 Fail
            return false;
        }
        else{ // 현재 매력 포인트와 선택한 매력포인트가 다를 경우 호감사유 수정
            modifyAttractiveType.updateAttractionTypeCode(attractiveTypeCode);
            likeablePersonRepository.save(modifyAttractiveType);
            return true;
        }
    }

    // 호감표시리스트에 있는지 확인
    public Optional<LikeablePerson> canModify(Member member, String username) {
        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();
        return likeablePersonRepository.findByFromInstaMemberAndToInstaMember(fromInstaMember, toInstaMember);
    }

    // 호감표시할 수 있는지 확인하는 서비스
    public RsData canActorLike(Member member, String username) {
        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }
        return RsData.of("S-1", "추가 가능합니다.");
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        publisher.publishEvent(new EventBeforeCancelLike(this, likeablePerson));
        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(toInstaMemberUsername));
    }

    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        if (!Objects.equals(actor.getInstaMember().getId(), likeablePerson.getFromInstaMember().getId()))
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }

    @Transactional
    public RsData<LikeablePerson> modifyLike(Member actor, Long id, int attractiveTypeCode) {
        LikeablePerson likeablePerson = findById(id).orElseThrow();
        RsData canModifyRsData = canModifyLike(actor, likeablePerson);

        if (canModifyRsData.isFail()) {
            return canModifyRsData;
        }

        String username = likeablePerson.getToInstaMember().getUsername();
        String oldAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();

        modifyAttractionTypeCode(likeablePerson, attractiveTypeCode);

        String newAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();
        return RsData.of("S-1", "%s님에 대한 호감사유를 %s에서 %s(으)로 수정하였습니다.".formatted(username, oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName), likeablePerson);
    }

    public RsData canModifyLike(Member actor, LikeablePerson likeablePerson) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (!Objects.equals(likeablePerson.getFromInstaMember().getId(), fromInstaMember.getId())) {
            return RsData.of("F-2", "해당 호감표시를 취소할 권한이 없습니다.");
        }


        return RsData.of("S-1", "호감표시취소가 가능합니다.");
    }

    private void modifyAttractionTypeCode(LikeablePerson likeablePerson, int attractiveTypeCode) {
        int oldAttractiveTypeCode = likeablePerson.getAttractiveTypeCode();
        RsData rsData = likeablePerson.updateAttractionTypeCode(attractiveTypeCode);

        if (rsData.isSuccess()) {
            publisher.publishEvent(new EventAfterModifyAttractiveType(this, likeablePerson, oldAttractiveTypeCode, attractiveTypeCode));
        }
    }
}