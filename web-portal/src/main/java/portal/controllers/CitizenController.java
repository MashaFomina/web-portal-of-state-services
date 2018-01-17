package portal.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import portal.model.institutions.Institution;
import portal.model.user.Citizen;
import portal.model.entities.*;
import portal.model.user.User;
import portal.repositories.institutions.EducationalInstitutionRepository;
import portal.repositories.institutions.InstitutionBaseRepository;
import portal.repositories.institutions.MedicalInstitutionRepository;
import portal.repositories.users.*;
import portal.repositories.entities.*;
import portal.services.SecurityService;
import portal.services.UserService;
import portal.validator.UserValidator;
import portal.model.institutions.*;
import portal.errors.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CitizenController {
    @Autowired
    private CitizenRepository citizenRepository;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String getProfile(Principal principal, Model model) {
        return getProfileCommon(principal, model);
    }

    @RequestMapping(value = "/profile/update", method = RequestMethod.GET)
    public String getProfileUpdate(Principal principal, Model model) {
        return getProfileCommon(principal, model);
    }

    public String getProfileCommon(Principal principal, Model model) {
        Citizen citizen = citizenRepository.findByUsername(principal.getName());
        model.addAttribute("userForm", citizen);
        System.out.println("Count of notifications: " + citizen.getNotifications().size());
        return "citizen/profile";
    }

    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    public String profileUpdate(Principal principal, @Valid @ModelAttribute("userForm") Citizen userForm, BindingResult bindingResult, Model model) {
        userValidator.validateUpdatedUser(userForm, bindingResult, principal.getName());
        if (bindingResult.hasErrors()) {
            return "citizen/profile";
        }
        System.out.println("principal.getName(): " + principal.getName());
        Citizen citizen = citizenRepository.findByUsername(principal.getName());
        boolean usernameChanged = citizen.getUsername() != userForm.getUsername();
        citizen.setUsername(userForm.getUsername());
        citizen.setPassword(userForm.getPassword());
        citizen.setFullName(userForm.getFullName());
        citizen.setEmail(userForm.getEmail());
        citizen.setPolicy(userForm.getPolicy());
        citizen.setPassport(userForm.getPassport());
        citizen.setBirthDate(userForm.getBirthDate());
        userService.save(citizen);
        if (usernameChanged) {
            securityService.autologin(citizen.getUsername(), userForm.getPassword());
        }
        return "citizen/profile";
    }

    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
    public String getTickets(Principal principal, Model model) {
        Citizen citizen = citizenRepository.findByUsername(principal.getName());
        model.addAttribute("citizen", citizen);
        System.out.println("Count of tickets: " + citizen.getTickets().size());
        return "citizen/tickets";
    }

    @RequestMapping(value = "/requests", method = RequestMethod.GET)
    public String getRequests(Principal principal, Model model) {
        Citizen citizen = citizenRepository.findByUsername(principal.getName());
        model.addAttribute("requests", citizen.getEduRequests());
        System.out.println("Count of requests: " + citizen.getEduRequests().size());
        return "citizen/edu_requests";
    }

    @RequestMapping(value = "/childs", method = RequestMethod.GET)
    public String getChilds(Principal principal, Model model) {
        Citizen citizen = citizenRepository.findByUsername(principal.getName());
        model.addAttribute("childs", citizen.getChilds().values());
        System.out.println("Count of childs: " + citizen.getChilds().size());
        return "citizen/childs";
    }
}
