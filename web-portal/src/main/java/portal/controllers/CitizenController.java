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
    Logger log = LoggerFactory.getLogger(ProjectsController.class);
    @Autowired
    private MedicalInstitutionRepository medicalInstitutionRepository;
    @Autowired
    private EducationalInstitutionRepository educationalInstitutionRepository;
    @Autowired
    private CitizenRepository citizenRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EduRequestRepository eduRequestRepository;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;

    private DateFormat dateFormatWithoutTime = new SimpleDateFormat("yyyy-MM-dd");

    class responseResult {
        private Long id;

        public responseResult(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setMessage(Long id) {
            this.id = id;
        }
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String getProfile(Principal principal, Model model) {
        //System.out.println("principal.getName(): " + principal.getName()); if (principal.getName() != null)
        Citizen citizen = citizenRepository.findByUsername("citizen");
        model.addAttribute("userForm", citizen);
        System.out.println("Count of notifications: " + citizen.getNotifications().size());
        return "citizen/profile";
    }

    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    public String profileUpdate(@Valid @ModelAttribute("userForm") Citizen userForm, BindingResult bindingResult, Model model) {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "citizen/profile";
        }
        userService.save(userForm);

        return "citizen/profile";
    }

    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
    public String getTickets(Principal principal, Model model) {
        //System.out.println("principal.getName(): " + principal.getName()); if (principal.getName() != null)
        Citizen citizen = citizenRepository.findByUsername("citizen");
        model.addAttribute("citizen", citizen);
        System.out.println("Count of tickets: " + citizen.getTickets().size());
        return "citizen/tickets";
    }

    @RequestMapping(value = "/refuse_ticket",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> refuseTicketByCitizen(Principal principal, @RequestParam(value = "ticket_id", required = true) int ticket_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername("citizen");
            Ticket ticket = ticketRepository.findById(new Long(ticket_id));
            if (ticket != null) {
                citizen.cancelTicket(ticket);
                ticketRepository.save(ticket);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoRightsException ex) {
            log.debug("Error while citizen refuse ticket!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }


    @RequestMapping(value = "/requests", method = RequestMethod.GET)
    public String getRequests(Principal principal, Model model) {
        //System.out.println("principal.getName(): " + principal.getName()); if (principal.getName() != null)
        Citizen citizen = citizenRepository.findByUsername("citizen");
        model.addAttribute("requests", citizen.getEduRequests());
        System.out.println("Count of requests: " + citizen.getEduRequests().size());
        return "citizen/edu_requests";
    }

    @RequestMapping(value = "/remove_request", method = RequestMethod.GET)
    public ResponseEntity<?> removeEduRequest(Principal principal, @RequestParam(value = "request_id", required = true) int request_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername("citizen");
            EduRequest request = eduRequestRepository.findById(new Long(request_id));
            if (request != null) {
                citizen.removeEduRequest(request);
                eduRequestRepository.delete(request);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoRightsException ex) {
            log.debug("Error while citizen remove educational request!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/accept_request", method = RequestMethod.GET)
    public ResponseEntity<?> acceptEduRequestByParent(Principal principal, @RequestParam(value = "request_id", required = true) int request_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername("citizen");;
            EduRequest request = eduRequestRepository.findById(new Long(request_id));
            if (request != null) {
                citizen.acceptEduRequest(request);
                eduRequestRepository.save(request);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoRightsException ex) {
            log.debug("Error while citizen accept educational request!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/childs", method = RequestMethod.GET)
    public String getChilds(Principal principal, Model model) {
        //System.out.println("principal.getName(): " + principal.getName()); if (principal.getName() != null)
        Citizen citizen = citizenRepository.findByUsername("citizen");
        model.addAttribute("childs", citizen.getChilds().values());
        System.out.println("Count of childs: " + citizen.getChilds().size());
        return "citizen/childs";
    }

    @RequestMapping(value = "/remove_child", method = RequestMethod.GET)
    public ResponseEntity<?> removeEduRequest1(Principal principal, @RequestParam(value = "child_id", required = true) int child_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername("citizen");
            Child child = childRepository.findById(new Long(child_id));
            if (child != null) {
                if (citizen.removeChildInfo(child)) {
                    childRepository.delete(child);
                }
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoRightsException ex) {
            log.debug("Error while citizen remove child!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/add_child", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addChildInformation(Principal principal, @RequestParam String fullName, @RequestParam String birthCertificate, @RequestParam String birthDate) {
        System.out.println("addChildInformation");
        try {
            Citizen citizen = citizenRepository.findByUsername("citizen");;
            Child result = citizen.createChildInfo(fullName, birthCertificate, dateFormatWithoutTime.parse(birthDate));
            //citizenRepository.save(citizen);
            childRepository.save(result);
            if (result != null) {
                return new ResponseEntity<>(new responseResult(result.getId()), HttpStatus.OK);
            }
        } catch (ParseException ex) {
            log.debug("Error while citizen add child information!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/add_request", method = RequestMethod.GET)
    public ResponseEntity<?> addEduRequest(Principal principal, @RequestParam(value = "institutionId", required = true) Long institutionId, @RequestParam(value = "childId", required = true) Long childId, @RequestParam(value = "classNumber", required = true) int classNumber) {
        try {
            Citizen citizen = citizenRepository.findByUsername("citizen");;
            EducationalInstitution institution = educationalInstitutionRepository.findById(institutionId);
            Child child = childRepository.findById(childId);
            EduRequest request = citizen.createEduRequest(child, institution, classNumber);
            if (request != null) {
                eduRequestRepository.save(request);
                //educationalInstitutionRepository.save(institution);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoFreeSeatsException | NoRightsException ex) {
            log.debug("Error while citizen add educational request!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }
    /*@RequestMapping(value = "/project", method = RequestMethod.GET)
    public String viewProject(@RequestParam(value = "project_id", required = true) int project_id, Model model) {
        log.debug("In ProjectsController... View project ...");
        model.addAttribute("project_id", 0);
        model.addAttribute("file_separator", File.separator);
        return "viewProject";
    }*/

    /*@RequestMapping(value = "/getFile",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getFile(@RequestParam String file, @RequestParam long project_id)
    {
        return new ResponseEntity<>(new ProjectsController.getFileResult("message", true, "content"), HttpStatus.OK);
    }*/
/*
    @RequestMapping(value = "/saveAndCheck",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> saveAndCheck(@RequestParam String title,
                                               @RequestParam String desc,
                                               @RequestParam String attributes,
                                               @RequestParam long project_id,
                                               @RequestParam String files
    ) throws //DockerException, InterruptedException, DockerCertificateException, IOException
    {


        return new ResponseEntity<>(new CheckResult(message, success), HttpStatus.OK);
    }

    @RequestMapping(value = "/addAndCheck",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> addAndCheck(@RequestParam String title,
                                              @RequestParam String desc,
                                              @RequestParam String owner,
                                              @RequestParam String repo,
                                              @RequestParam String commitOrBranch,
                                              @RequestParam String type,
                                              @RequestParam String username,
                                              @RequestParam String password,
                                              @RequestParam String attributes
    ) {

        return new ResponseEntity<>(new CheckResult(message, success), HttpStatus.OK);
    }
    */
/*public @ResponseBody Greeting sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
*/
}
