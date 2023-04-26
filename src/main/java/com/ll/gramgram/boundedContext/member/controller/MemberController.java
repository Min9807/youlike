package com.ll.gramgram.boundedContext.member.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
    }

    @AllArgsConstructor
    @Getter
    public static class JoinForm {
        @NotBlank
        @Size(min = 4, max = 30)
        private final String username;
        @NotBlank
        @Size(min = 4, max = 30)
        private final String password;
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/join")
    public String join(@Valid JoinForm joinForm) {
        RsData<Member> joinRs = memberService.join(joinForm.getUsername(), joinForm.getPassword());

        if (joinRs.isFail()) {
            return rq.historyBack(joinRs);
        }

        return rq.redirectWithMsg("/member/login", joinRs);
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String showLogin() {
        return "usr/member/login";
    }

    @PreAuthorize("isAuthenticated()") // 로그인 해야만 접속가능
    @GetMapping("/me") // 로그인 한 나의 정보 보여주는 페이지
    public String showMe(Model model) {
        if (!rq.getMember().hasConnectedInstaMember()) {
            return rq.historyBack("먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember instaMember = rq.getMember().getInstaMember();

        long likesByGenderWomen = instaMember
                .getToLikeablePeople()
                .stream()
                .filter(likeablePerson -> likeablePerson.getToInstaMember().getGender().equals("W"))
                .count();

        long likesByGenderMen = instaMember
                .getToLikeablePeople()
                .stream()
                .filter(likeablePerson -> likeablePerson.getToInstaMember().getGender().equals("M"))
                .count();

        long likesByAttractiveTypeCode1 = instaMember
                .getToLikeablePeople()
                .stream()
                .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == 1)
                .count();

        long likesByAttractiveTypeCode2 = instaMember
                .getToLikeablePeople()
                .stream()
                .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == 2)
                .count();

        long likesByAttractiveTypeCode3 = instaMember
                .getToLikeablePeople()
                .stream()
                .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == 3)
                .count();

        model.addAttribute("likes", likesByGenderWomen + likesByGenderMen);
        model.addAttribute("likesByGenderWomen", likesByGenderWomen);
        model.addAttribute("likesByGenderMen", likesByGenderMen);
        model.addAttribute("likesByAttractiveTypeCode1", likesByAttractiveTypeCode1);
        model.addAttribute("likesByAttractiveTypeCode2", likesByAttractiveTypeCode2);
        model.addAttribute("likesByAttractiveTypeCode3", likesByAttractiveTypeCode3);

        return "usr/member/me";
    }
}