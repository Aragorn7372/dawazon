package dev.luisvives.dawazon.users.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("auth/me")
public class UserController {
    @GetMapping({"","/"})
    public String index(Model model) {
        return "/web/user/editUserAdmin";
    }


}
