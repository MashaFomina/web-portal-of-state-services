package portal.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import portal.errors.NoRightsException;
import portal.model.entities.Child;
import portal.model.institutions.*;
import portal.model.user.*;
import portal.repositories.institutions.*;
import portal.repositories.users.UserRepository;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class InstitutionController {
    Logger log = LoggerFactory.getLogger(ProjectsController.class);
    @Autowired
    private MedicalInstitutionRepository medicalInstitutionRepository;
    @Autowired
    private InstitutionBaseRepository<Institution> institutionRepository;
    @Autowired
    private UserRepository userBaseRepository;
    @Autowired
    private EducationalInstitutionRepository educationalInstitutionRepository;

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

    @RequestMapping(value = "/institutions", method = RequestMethod.GET)
    public String getInstitutions(Principal principal, Model model) {
        return "citizen/institutions";
    }

    @RequestMapping(value = "/institutionview", method = RequestMethod.GET)
    public String getInstitution(@RequestParam(value = "id", required = true) Long id, Principal principal, Model model) {
        //User user = userBaseRepository.findByUsername("citizen");
        //User user = userBaseRepository.findByUsername("edur");
        //User user = userBaseRepository.findByUsername("medr");
        User user = userBaseRepository.findByUsername(principal.getName());
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
                //User user = userBaseRepository.findByUsername("citizen");
                User user = userBaseRepository.findByUsername(principal.getName());
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
    public @ResponseBody List<String> getCities(Principal principal, @RequestParam(value = "is_edu", required = true) boolean is_edu) {
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



    @RequestMapping(value = "/save_institution_info", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> saveInstitutionInformation(Principal principal, @RequestParam Long institutionId, @RequestParam String title, @RequestParam String city, @RequestParam String district, @RequestParam String address, @RequestParam String telephone, @RequestParam String fax) {
        //User user = userBaseRepository.findByUsername("edur");
        User user = userBaseRepository.findByUsername(principal.getName());
        if (user.isMedicalRepresentative() || user.isEducationalRepresentative()) {
            Institution institution = institutionRepository.findById(institutionId);
            institution.edit(title, city, district, telephone, fax, address);
            institutionRepository.save(institution);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
}
