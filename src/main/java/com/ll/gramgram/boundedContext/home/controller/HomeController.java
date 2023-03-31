package com.ll.gramgram.boundedContext.home.controller;

import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Enumeration;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;
    @GetMapping("/")
    public String showMain(Model model, Principal principal) {
        if ( principal != null ) {
            Member loginedMember = memberService.findByUsername(principal.getName()).orElseThrow();
            model.addAttribute("loginedMember", loginedMember);
        }

        return "usr/home/main";
    }

    @GetMapping("/debugSession")
    @ResponseBody
    public String showDebugSession(HttpSession session) {
        StringBuilder sb = new StringBuilder("Session content:\n");

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            sb.append(String.format("%s: %s\n", attributeName, attributeValue));
        }

        return sb.toString().replaceAll("\n", "<br>");
    }
}
