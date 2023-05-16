package com.ll.gramgram.base.initData;

import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import com.ll.gramgram.standard.util.Ut;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService,
            InstaMemberService instaMemberService,
            LikeablePersonService likeablePersonService
    ) {
        return args -> {
            Member memberAdmin = memberService.join("admin", "1234").getData();
            Member memberUser1 = memberService.join("user1", "1234").getData();
            Member memberUser2 = memberService.join("user2", "1234").getData();
            Member memberUser3 = memberService.join("user3", "1234").getData();
            Member memberUser4 = memberService.join("user4", "1234").getData();
            Member memberUser5 = memberService.join("user5", "1234").getData();

            instaMemberService.connect(memberUser2, "insta_user2", "M");
            instaMemberService.connect(memberUser3, "insta_user3", "W");
            instaMemberService.connect(memberUser4, "insta_user4", "M");
            instaMemberService.connect(memberUser5, "insta_user5", "W");

            likeablePersonService.like(memberUser3, "insta_user4", 1);
            likeablePersonService.like(memberUser3, "insta_user100", 2);
            // 원활한 테스트와 개발을 위해서 자동으로 만들어지는 호감이 삭제, 수정이 가능하도록 쿨타임해제
            LikeablePerson likeablePersonToinstaUser4 = likeablePersonService.like(memberUser3, "insta_user4", 1).getData();
            Ut.reflection.setFieldValue(likeablePersonToinstaUser4, "modifyUnlockDate", LocalDateTime.now().minusSeconds(1));
            LikeablePerson likeablePersonToinstaUser100 = likeablePersonService.like(memberUser3, "insta_user100", 2).getData();
            Ut.reflection.setFieldValue(likeablePersonToinstaUser100, "modifyUnlockDate", LocalDateTime.now().minusSeconds(1));
        };
    }
}