package portal.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import portal.errors.InvalidAppointmentDateException;
import portal.errors.InvalidDataForSavingSeatsException;
import portal.errors.NoFreeSeatsException;
import portal.errors.NoRightsException;
import portal.model.entities.EduRequest;
import portal.model.institutions.EducationalInstitution;
import portal.model.institutions.Institution;
import portal.model.institutions.MedicalInstitution;
import portal.model.user.Citizen;
import portal.model.user.EducationalRepresentative;
import portal.model.user.InstitutionRepresentative;
import portal.model.user.User;
import portal.repositories.entities.EduRequestRepository;
import portal.repositories.institutions.EducationalInstitutionRepository;
import portal.repositories.institutions.InstitutionBaseRepository;
import portal.repositories.institutions.MedicalInstitutionRepository;
import portal.repositories.users.UserRepository;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Controller
public class EducationalRepresentativeController {
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


    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private DateFormat dateFormatWithoutTime = new SimpleDateFormat("yyyy-MM-dd");

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
                    eduRequestRepository.save(request);
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
                    eduRequestRepository.save(request);
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
                    eduRequestRepository.save(request);
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
                    eduRequestRepository.save(request);
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
}
