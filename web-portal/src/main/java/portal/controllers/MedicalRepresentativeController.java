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
    Logger log = LoggerFactory.getLogger(ProjectsController.class);
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
            //User user = userBaseRepository.findByUsername("medr");
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
            //User user = userBaseRepository.findByUsername("medr");
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
            //User user = userBaseRepository.findByUsername("medr");
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
            //User user = userBaseRepository.findByUsername("medr");
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

    @RequestMapping(value = "/add_doctor", method = RequestMethod.POST)//produces = {MediaType.}
    public ResponseEntity<?> addDoctor(Principal principal, @RequestParam(value = "username", required = true) String username, @RequestParam(value = "password", required = true) String password, @RequestParam(value = "fullName", required = true) String fullName, @RequestParam(value = "email", required = true) String email, @RequestParam(value = "position", required = true) String position, @RequestParam(value = "summary", required = true) String summary) {
        //user.addDoctor(newDoctor);
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
            //User user = userBaseRepository.findByUsername("medr");
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

    class responseResult {
        private Long id;
        private String title;
        private String telephone;
        private String fax;
        private String address;

        public responseResult(Long id, String title, String fax, String telephone, String address) {
            this.id = id;
            this.title = title;
            this.telephone = telephone;
            this.fax = fax;
            this.address = address;
        }

        public Long getId() {
            return id;
        }
        public String getTitle() {
            return title;
        }
        public String getTelephone() {
            return telephone;
        }
        public String getFax() {
            return fax;
        }
        public String getAddress() {
            return address;
        }
    }
/*
    @RequestMapping(value = "/institutions", method = RequestMethod.GET)
    public String getInstitutions(Principal principal, Model model) {
        return "citizen/institutions";
    }

    @RequestMapping(value = "/institutionview", method = RequestMethod.GET)
    public String getInstitution(@RequestParam(value = "id", required = true) Long id, Principal principal, Model model) {
        //User user = userBaseRepository.findByUsername("citizen");
        User user = userBaseRepository.findByUsername("edur");
        Institution institution = institutionRepository.findById(id);
        boolean isEdu = institution.isEdu();
        model.addAttribute("user", user.isCitizen() ? (Citizen) user : user);
        model.addAttribute("institution", isEdu ? ((EducationalInstitution) institution) : ((MedicalInstitution) institution));
        model.addAttribute("isEdu", isEdu);
        if (user.isCitizen())
            model.addAttribute("other_variants", isEdu ? educationalInstitutionRepository.findAllByDistrict(institution.getCity(), institution.getDistrict()) : medicalInstitutionRepository.findAllByDistrict(institution.getCity(), institution.getDistrict()));
        return "institution";
    }

    @RequestMapping(value = "/add_feedback", method = RequestMethod.GET)
    public ResponseEntity<?> addFeedbackByUser(Principal principal, @RequestParam(value = "institutionId", required = true) Long institutionId, @RequestParam(value = "text", required = true) String text, @RequestParam(value = "toUserId", required = true) Long toUserId) {
        try {
            if (text.length() > 0) {
                User user = userBaseRepository.findByUsername("citizen");
                User userTo = toUserId > 0 ? userBaseRepository.findById(toUserId) : null;
                Institution institution = institutionRepository.findById(institutionId);
                if (user != null) {
                    if (userTo == null) user.addFeedback(institution, text);
                    else user.addFeedbackTo(institution, text, userTo);
                    userBaseRepository.save(user);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        } catch (NoRightsException ex) {
            log.debug("Error while citizen add feedback!");
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/get_cities", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    List<String> getCities(Principal principal, @RequestParam(value = "is_edu", required = true) boolean is_edu) {
        List<String> cities = new ArrayList<>();
        if (is_edu) {
            cities = educationalInstitutionRepository.getCities();
        }
        else {
            cities = medicalInstitutionRepository.getCities();
        }
        return cities;
    }

    @RequestMapping(value = "/get_districts", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody List<String> getDistricts(Principal principal, @RequestParam(value = "is_edu", required = true) boolean is_edu, @RequestParam(value = "city", required = true) String city) {
        List<String> districts = new ArrayList<>();
        if (is_edu) {
            districts = educationalInstitutionRepository.getCityDistricts(city);
        }
        else {
            districts = medicalInstitutionRepository.getCityDistricts(city);
        }
        return districts;
    }

    @RequestMapping(value = "/get_institutions", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody List<responseResult> getInstitutions(Principal principal, @RequestParam(value = "is_edu", required = true) boolean is_edu, @RequestParam(value = "city", required = true) String city, @RequestParam(value = "district", required = true) String district) {
        List<responseResult> institutions = new ArrayList<>();
        if (is_edu) {
            for (EducationalInstitution e : educationalInstitutionRepository.findAllByDistrict(city, district))
                institutions.add(new responseResult(e.getId(), e.getTitle(), e.getFax(), e.getTelephone(), e.getAddress()));
        }
        else {
            for (MedicalInstitution m :medicalInstitutionRepository.findAllByDistrict(city, district))
                institutions.add(new responseResult(m.getId(), m.getTitle(), m.getFax(), m.getTelephone(), m.getAddress()));
        }
        return institutions;
    }
*/

/*
    @RequestMapping(value = "/add_seats_info", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> saveInstitutionInformation(Principal principal, @RequestParam Long institutionId, @RequestParam int classNumber, @RequestParam int totalSeats, @RequestParam int busySeats) {
        try {
            EducationalInstitution institution = educationalInstitutionRepository.findById(institutionId);
            if ((classNumber > 11 || classNumber < 1) || totalSeats < 1 || busySeats < 1 || busySeats > totalSeats)
                throw new InvalidDataForSavingSeatsException();
            institution.setSeats(classNumber, totalSeats, busySeats);
            educationalInstitutionRepository.save(institution);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (InvalidDataForSavingSeatsException ex) {
            log.debug("Error while educational representative saves seats!");
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/accept_request_repr", method = RequestMethod.GET)
    public ResponseEntity<?> acceptEduRequestByRepresentative(Principal principal, @RequestParam(value = "request_id", required = true) Long request_id) {
        User user = userBaseRepository.findByUsername("edur");
        if (user.isEducationalRepresentative()) {
            try {
                EducationalInstitution institution = (EducationalInstitution) ((InstitutionRepresentative) user).getInstitution();
                EduRequest request =  eduRequestRepository.findById(request_id);
                if (request != null) {
                    institution.acceptEduRequest(request);
                    educationalInstitutionRepository.save(institution);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } catch (NoRightsException ex) {
                log.debug("Error while representative accept educational request!");
            }
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/make_appointment", method = RequestMethod.GET)
    public ResponseEntity<?> acceptEduRequestByRepresentative(Principal principal, @RequestParam(value = "request_id", required = true) Long request_id, @RequestParam(value = "date", required = true) String date) {
        User user = userBaseRepository.findByUsername("edur");
        if (user.isEducationalRepresentative()) {
            try {
                EducationalInstitution institution = (EducationalInstitution) ((InstitutionRepresentative) user).getInstitution();
                EduRequest request =  eduRequestRepository.findById(request_id);
                if (request != null) {
                    institution.acceptEduRequest(request);
                    Date dateAppointment;
                    dateAppointment = dateFormat.parse(date);
                    institution.makeAppointment(request, dateAppointment);
                    educationalInstitutionRepository.save(institution);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } catch (ParseException | InvalidAppointmentDateException | NoRightsException ex) {
                log.debug("Error while representative make appointment for request!");
            }
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/refuse_request", method = RequestMethod.GET)
    public ResponseEntity<?> refuseEduRequestByRepresentative(Principal principal, @RequestParam(value = "request_id", required = true) Long request_id) {
        User user = userBaseRepository.findByUsername("edur");
        if (user.isEducationalRepresentative()) {
            try {
                EducationalInstitution institution = (EducationalInstitution) ((InstitutionRepresentative) user).getInstitution();
                EduRequest request =  eduRequestRepository.findById(request_id);
                if (request != null) {
                    institution.refuseEduRequest(request);
                    educationalInstitutionRepository.save(institution);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } catch (NoRightsException ex) {
                log.debug("Error while representative refuse educational request!");
            }
        }
        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/enroll_request", method = RequestMethod.GET)
    public ResponseEntity<?> enrollEduRequestByRepresentative(Principal principal, @RequestParam(value = "request_id", required = true) Long request_id) {
        User user = userBaseRepository.findByUsername("edur");
        if (user.isEducationalRepresentative()) {
            try {
                EducationalInstitution institution = (EducationalInstitution) ((InstitutionRepresentative) user).getInstitution();
                EduRequest request =  eduRequestRepository.findById(request_id);
                if (request != null) {
                    institution.makeChildEnrolled(request);
                    educationalInstitutionRepository.save(institution);
                    // get set of requests for automatic removing requests of other institutions from database
                    List<EduRequest> requests = new ArrayList(request.getParent().getEduRequests());
                    Iterator<EduRequest> i = requests.iterator();
                    while (i.hasNext()) {
                        EduRequest r = i.next(); // must be called before you can call i.remove()
                        if (!r.equals(request) && request.getChild().equals(r.getChild())) {
                            eduRequestRepository.delete(r);
                        }
                    }
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } catch (NoFreeSeatsException | NoRightsException ex) {
                log.debug("Error while representative enroll child!");
            }
        }

        return new ResponseEntity<Error>(HttpStatus.CONFLICT);
    }
    */
}