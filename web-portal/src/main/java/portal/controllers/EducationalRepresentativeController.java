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

@RequestMapping("/edur")
@Controller
public class EducationalRepresentativeController {
    Logger log = LoggerFactory.getLogger(EducationalRepresentativeController.class);
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
        User user = userBaseRepository.findByUsername(principal.getName());
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
        User user = userBaseRepository.findByUsername(principal.getName());
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
        User user = userBaseRepository.findByUsername(principal.getName());
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
        User user = userBaseRepository.findByUsername(principal.getName());
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
}
