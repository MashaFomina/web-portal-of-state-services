package portal.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import portal.errors.InvalidAppointmentDateException;
import portal.errors.InvalidDataForSavingSeatsException;
import portal.errors.NoFreeSeatsException;
import portal.errors.*;
import portal.model.entities.EduRequest;
import portal.model.institutions.EducationalInstitution;
import portal.model.institutions.Institution;
import portal.model.institutions.MedicalInstitution;
import portal.model.user.*;
import portal.model.entities.*;
import portal.repositories.entities.EduRequestRepository;
import portal.repositories.entities.TicketRepository;
import portal.repositories.institutions.EducationalInstitutionRepository;
import portal.repositories.institutions.InstitutionBaseRepository;
import portal.repositories.institutions.MedicalInstitutionRepository;
import portal.repositories.users.DoctorRepository;
import portal.repositories.users.RoleRepository;
import portal.repositories.users.UserRepository;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/medr")
@Controller
public class MedicalRepresentativeController  {
    Logger log = LoggerFactory.getLogger(MedicalRepresentativeController.class);
    @Autowired
    private MedicalInstitutionRepository medicalInstitutionRepository;
    @Autowired
    private InstitutionBaseRepository<Institution> institutionRepository;
    @Autowired
    private UserRepository userBaseRepository;
    @Autowired
    private EducationalInstitutionRepository educationalInstitutionRepository;
    @Autowired
    private EduRequestRepository eduRequestRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private DateFormat dateFormatWithoutTime = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/remove_ticket", method = RequestMethod.GET)
    public ResponseEntity<?> removeTicket(Principal principal, @RequestParam(value = "ticket_id", required = true) Long ticket_id) {
        try {
            User user = userBaseRepository.findByUsername(principal.getName());
            if (user.isMedicalRepresentative()) {
                Ticket ticket = ticketRepository.findById(ticket_id);
                if (ticket != null) {
                    MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
                    institution.deleteTicket(ticket);
                    medicalInstitutionRepository.save(institution);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        } catch (NoRightsException ex) {
            log.debug("Error while representative remove ticket!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/set_ticket_visited", method = RequestMethod.GET)
    public ResponseEntity<?> setTicketVisited(Principal principal, @RequestParam(value = "ticket_id", required = true) Long ticket_id, @RequestParam(value = "summary", required = true) String summary) {
        User user = userBaseRepository.findByUsername(principal.getName());
        if (user.isMedicalRepresentative()) {
            Ticket ticket = ticketRepository.findById(ticket_id);
            MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
            if (ticket != null && ticket.getInstitution().equals(institution)) {
                ticket.setVisited(true, summary);
                medicalInstitutionRepository.save(institution);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/remove_tickets", method = RequestMethod.GET)
    public ResponseEntity<?> removeTickets(Principal principal, @RequestParam(value = "doctorId", required = true) Long doctorId, @RequestParam(value = "date", required = true) String date) {
        try {
            User user = userBaseRepository.findByUsername(principal.getName());
            if (user.isMedicalRepresentative()) {
                Doctor doctor = doctorRepository.findById(doctorId);
                Date ticketsDate = date.length() > 0 ? dateFormatWithoutTime.parse(date) : null;
                if (doctor != null) {
                    MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
                    institution.deleteTickets(doctor, ticketsDate);
                    medicalInstitutionRepository.save(institution);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        } catch (NoRightsException | ParseException ex) {
            log.debug("Error while representative remove tickets!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/add_tickets", method = RequestMethod.GET)
    public ResponseEntity<?> addTickets(Principal principal, @RequestParam(value = "doctorId", required = true) Long doctorId, @RequestParam(value = "startDate", required = true) String start, @RequestParam(value = "endDate", required = true) String end, @RequestParam(value = "interval", required = true) int interval) {
        try {
            User user = userBaseRepository.findByUsername(principal.getName());
            Doctor doctor = doctorRepository.findById(doctorId);
            if (user.isMedicalRepresentative() && doctor != null) {
                Date startDate, endDate;
                startDate = dateFormat.parse(start);
                endDate = dateFormat.parse(end);
                MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
                institution.addTickets(doctor, startDate, endDate, interval);
                medicalInstitutionRepository.save(institution);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (InvalidTicketsDatesException | ParseException | NoRightsException ex) {
            log.debug("Error while representative add tickets!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/add_ticket", method = RequestMethod.GET)
    public ResponseEntity<?> addTicket(Principal principal, @RequestParam(value = "doctorId", required = true) Long doctorId, @RequestParam(value = "date", required = true) String date) {
        try {
            User user = userBaseRepository.findByUsername(principal.getName());
            Doctor doctor = doctorRepository.findById(doctorId);
            if (user.isMedicalRepresentative() && doctor != null) {
                MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
                Date dateTicket;
                dateTicket = dateFormat.parse(date);
                institution.addTicket(doctor, dateTicket);
                medicalInstitutionRepository.save(institution);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (InvalidTicketsDatesException | ParseException | NoRightsException ex) {
            log.debug("Error while representative add ticket!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/add_doctor", method = RequestMethod.POST)
    public ResponseEntity<?> addDoctor(Principal principal, @RequestParam(value = "username", required = true) String username, @RequestParam(value = "password", required = true) String password, @RequestParam(value = "fullName", required = true) String fullName, @RequestParam(value = "email", required = true) String email, @RequestParam(value = "position", required = true) String position, @RequestParam(value = "summary", required = true) String summary) {
        try {
            User user = userBaseRepository.findByUsername(principal.getName());
            MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
            Doctor newDoctor = new Doctor(username, bCryptPasswordEncoder.encode(password), fullName, email, institution, position, summary, true);
            HashSet<Role> roles = new HashSet<Role>();
            roles.add(roleRepository.findByName("ROLE_DOCTOR"));
            newDoctor.setRoles(roles);
            doctorRepository.save(newDoctor);
            return new ResponseEntity<>(newDoctor.getId(), HttpStatus.OK);
        } catch (Exception ex) {
            log.debug("Error while representative add doctor!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/remove_doctor", method = RequestMethod.GET)
    public ResponseEntity<?> removeDoctor(Principal principal, @RequestParam(value = "doctorId", required = true) Long doctorId) {
        try {
            User user = userBaseRepository.findByUsername(principal.getName());
            Doctor doctor = doctorRepository.findById(doctorId);
            if (user.isMedicalRepresentative() && doctor != null) {
                MedicalInstitution institution = (MedicalInstitution) ((InstitutionRepresentative) user).getInstitution();
                institution.removeDoctor(doctor);
                medicalInstitutionRepository.save(institution);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoRightsException ex) {
            log.debug("Error while representative remove doctor!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }
}