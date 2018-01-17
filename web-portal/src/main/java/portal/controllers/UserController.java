package portal.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.model.user.InstitutionRepresentative;
import portal.model.user.User;
import portal.model.user.Citizen;
import portal.repositories.users.UserRepository;
import portal.services.SecurityService;
import portal.services.UserService;
import portal.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class UserController {
    Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserRepository userBaseRepository;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model) {
        model.addAttribute("userForm", new Citizen());

        return "registration";
    }

    @RequestMapping(value = "/portal", method = RequestMethod.GET)
    public String enter(Principal principal, Model model) {
        User user = userBaseRepository.findByUsername(principal.getName());
        if (user.isCitizen())
            return "redirect:/profile";
        if (user.isMedicalRepresentative() || user.isEducationalRepresentative())
            return ("redirect:/institutionview?id=" + ((InstitutionRepresentative) user).getInstitution().getId());
        return "hello";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@Valid @ModelAttribute("userForm") Citizen userForm, BindingResult bindingResult, Model model) {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(userForm);

        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());
        System.out.println("finded Logged In Username: " + securityService.findLoggedInUsername());

        return "redirect:/portal";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "login";
    }
}
