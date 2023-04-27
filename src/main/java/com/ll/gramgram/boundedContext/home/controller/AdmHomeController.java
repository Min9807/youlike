package com.ll.gramgram.boundedContext.home.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/adm")
@PreAuthorize("hasAuthority('admin')")
public class AdmHomeController {
    @GetMapping("")
    public String showMain() {
        return "adm/home/main";
    }
}
