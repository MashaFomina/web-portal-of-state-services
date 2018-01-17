package portal.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import portal.errors.NoFreeSeatsException;
import portal.errors.NoRightsException;
import portal.model.entities.Child;
import portal.model.entities.EduRequest;
import portal.model.entities.Ticket;
import portal.model.institutions.EducationalInstitution;
import portal.model.user.Citizen;
import portal.repositories.entities.ChildRepository;
import portal.repositories.entities.EduRequestRepository;
import portal.repositories.entities.TicketRepository;
import portal.repositories.institutions.EducationalInstitutionRepository;
import portal.repositories.institutions.MedicalInstitutionRepository;
import portal.repositories.users.CitizenRepository;
import portal.services.UserService;
import portal.validator.UserValidator;

import javax.validation.Valid;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@RequestMapping("/citizen")
@Controller
public class CitizenControllerAjax {
    Logger log = LoggerFactory.getLogger(CitizenControllerAjax.class);
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

    @RequestMapping(value = "/refuse_ticket",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> refuseTicketByCitizen(Principal principal, @RequestParam(value = "ticket_id", required = true) int ticket_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
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

    @RequestMapping(value = "/take_ticket",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> takeTicketByCitizen(Principal principal, @RequestParam(value = "ticket_id", required = true) Long ticket_id, @RequestParam(value = "child_id", required = true) Long child_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
            if (citizen != null) {
                Ticket ticket = ticketRepository.findById(ticket_id);
                if (ticket != null) {
                    ticketRepository.save(ticket);
                    if (child_id < 1) {
                        citizen.acceptTicket(ticket);
                    }
                    else {
                        Child child = childRepository.findById(child_id);
                        citizen.acceptTicketForChild(ticket, child);
                    }
                    citizenRepository.save(citizen);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        } catch (NoRightsException ex) {
            log.debug("Error while citizen take ticket!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/remove_request", method = RequestMethod.GET)
    public ResponseEntity<?> removeEduRequest(Principal principal, @RequestParam(value = "request_id", required = true) int request_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
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
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
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

    @RequestMapping(value = "/remove_child", method = RequestMethod.GET)
    public ResponseEntity<?> removeEduRequest1(Principal principal, @RequestParam(value = "child_id", required = true) int child_id) {
        try {
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
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
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
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
            Citizen citizen = citizenRepository.findByUsername(principal.getName());
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
}
