package com.ll.gramgram.boundedContext.member.controller;


import com.ll.gramgram.boundedContext.home.controller.HomeController;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class MemberControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 폼")
    void t001() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/member/join"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("showJoin"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="text" name="username"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="password" name="password"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        id="btn-join-1"
                        """.stripIndent().trim())));
    }

    @Test
    @DisplayName("회원가입")
    void t002() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10")
                        .param("password", "1234")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/member/login?msg=**"));

        Member member = memberService.findByUsername("user10").orElse(null);

        assertThat(member).isNotNull();
    }

    @Test
    @DisplayName("회원가입시에 올바른 데이터를 넘기지 않으면 400")
    void t003() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());

        // WHEN
        resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("password", "1234")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());

        // WHEN
        resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10" + "a".repeat(30))
                        .param("password", "1234")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());

        // WHEN
        resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10")
                        .param("password", "1234" + "a".repeat(30))
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 폼")
    void t004() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/member/login"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("showLogin"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="text" name="username"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="password" name="password"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        id="btn-login-1"
                        """.stripIndent().trim())));
    }

    @Test
    // @Rollback(value = false) // DB에 흔적이 남는다.
    @DisplayName("로그인 처리")
    void t005() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/member/login")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user1")
                        .param("password", "1234")
                )
                .andDo(print());

        MvcResult mvcResult = resultActions.andReturn();
        HttpSession session = mvcResult.getRequest().getSession(false);
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        User user = (User) securityContext.getAuthentication().getPrincipal();

        assertThat(user.getUsername()).isEqualTo("user1");

        // THEN
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**"));
    }

    @Test
    @DisplayName("로그인 후에 내비바에 로그인한 회원의 username")
    @WithUserDetails("user1")
    void t006() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(HomeController.class))
                .andExpect(handler().methodName("showMain"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        0002
                        """.stripIndent().trim())));
    }
}