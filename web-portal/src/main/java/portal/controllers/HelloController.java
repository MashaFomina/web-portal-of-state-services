package portal.controllers;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import portal.model.entities.Ticket;
import portal.model.user.*;
import portal.repositories.entities.EduRequestRepository;
import portal.repositories.entities.FeedbackRepository;
import portal.repositories.entities.TicketRepository;
import portal.repositories.users.RoleRepository;
import portal.repositories.users.UserRepository;
import portal.services.SecurityService;
import portal.repositories.institutions.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class HelloController {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserRepository userBaseRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EducationalInstitutionRepository educationalInstitutionRepository;
    @Autowired
    private MedicalInstitutionRepository medicallInstitutionRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private EduRequestRepository eduRequestRepository;

    Logger log = LoggerFactory.getLogger(HelloController.class);

    @RequestMapping(value = "/portal", method = RequestMethod.GET)
    public String hello(Principal principal, Model model) {
        log.debug("In HelloController..");
        User user = userBaseRepository.findByUsername(principal.getName());
        if (user.isCitizen())
            return "redirect:/profile";
        if (user.isMedicalRepresentative() || user.isEducationalRepresentative())
            return ("redirect:/institutionview?id=" + ((InstitutionRepresentative) user).getInstitution().getId());
        return "hello";
    }

    @RequestMapping(value = "/checktemp", method = RequestMethod.GET)
    public String check(Principal principal, Model model) {
        /*
        Citizen citizen = new Citizen();
        citizen.setUsername("citizenNew1");
        citizen.setEmail("citizenNew@mail.ru");
        citizen.setPassport("asdwfe");
        citizen.setFullName("asdwfefweffr");
        citizen.setPassword("asdwfefweffr");
        citizen.setPolicy("sddaa");
        java.util.Date currentDate = new java.util.Date();
        citizen.setBirthDate(currentDate);
        System.out.println("Try save new citizen!");
        HashSet<Role> roles = new HashSet<Role>();
        roles.add(roleRepository.findByName("ROLE_CITIZEN"));
        citizen.setRoles(roles);
        userBaseRepository.save(citizen);
        */

        /*
        User user = userBaseRepository.findByUsername("citizen");
        if (user instanceof Citizen) {
            System.out.println("citizen inctance!");
            System.out.println(user.getUsername());
            Citizen c = (Citizen) user;
            EduRequest r = null;
            for (EduRequest r1: c.getEduRequests()) {
                r = r1;
                System.out.println("child name: " + r1.getChild().getFullName() + ", cn: " + r1.getClassNumber() + ", insId: " + r1.getInstitution().getId());
            }
            if (r != null) {
                try {
                    c.addEduRequest(new EduRequest(null, r.getChild(), r.getParent(), r.getInstitution(), new Date(), null, 2));
                    c.addEduRequest(new EduRequest(null, r.getChild(), r.getParent(), r.getInstitution(), new Date(), null, 2));
                    c.createEduRequest(r.getChild(), r.getInstitution(), new Integer(2));
                    c.createEduRequest(r.getChild(), r.getInstitution(), new Integer(2));
                    c.removeEduRequest(r);
                }
                catch (NoFreeSeatsException | NoRightsException ex1) {
                }
            }
            userBaseRepository.save(c);

            Notification n1 = null;
            for (Notification n: c.getNotifications()) {
                n1 = n;
                System.out.println("Notification: " + n.getNotification());
            }
            if (n1 != null) {
                c.addNotification("new notif!!!");
                c.getNotifications().remove(n1);
            }

            userBaseRepository.save(c);

            Child f1 = null;
            System.out.println("child keys: " + String.join(",", c.getChilds().keySet()));
            for (Child f: c.getChilds().values()) {
                f1 = f;
                System.out.println("child name: " + f.getFullName() + ", bc: " + f.getBirthCertificate());
            }
            if (f1 != null) {
                try {
                    c.addChild(new Child(c, "new added", "added", new Date()));
                    c.addChild(new Child(c, "new added 1", "added 1", new Date()));
                    c.removeChildInfo(f1);
                } catch (NoRightsException ex) {
                }
            }
            userBaseRepository.save(c);
        }
        */

        /*
        System.out.println("Cities for edu: " + String.join(", ", educationalInstitutionRepository.getCities()));
        EducationalInstitution e = educationalInstitutionRepository.findById(new Long(1));
        System.out.println("title: " + e.getTitle());

        for (Map.Entry<Integer, Integer> entry : e.getSeats().entrySet()) {
            Integer cN = entry.getKey();
            if (e.getBusySeats().containsKey(cN)) {
                System.out.println("seats: " + cN + " - " + entry.getValue() + " - " + e.getBusySeats().get(cN));
            }
        }

        for (Seats s: e.getListSeats()) {
            System.out.println("list seats: " + s.getClassNumber() + " - " + s.getSeats() + " - " + s.getBusySeats());
        }

        e.setTitle("seve work!");
        //e.setSeats(4, 20, 16);
        //e.setSeats(5, 20, 16);
        Map<Integer, Integer> seats = new HashMap<>();
        Map<Integer, Integer> busySeats = new HashMap<>();
        seats.put(new Integer(10), new Integer(30));
        seats.put(new Integer(6), new Integer(80));
        seats.put(new Integer(9), new Integer(90));
        busySeats.put(new Integer(10), new Integer(15));
        busySeats.put(new Integer(6), new Integer(20));
        busySeats.put(new Integer(9), new Integer(30));
        e.setSeats(seats, busySeats);
        EduRequest r = null;
        for (EduRequest r1: e.getEduRequests()) {
            r = r1;
            System.out.println("child name: " + r1.getChild().getFullName() + ", cn: " + r1.getClassNumber() + ", insId: " + r1.getInstitution().getId());
        }
        if (r != null) {
            try {
                e.createEduRequest(new EduRequest(null, r.getChild(), r.getParent(), e, new Date(), null, 2));
                e.createEduRequest(new EduRequest(null, r.getChild(), r.getParent(), e, new Date(), null, 2));
                e.removeEduRequest(r);
            }
            catch (NoRightsException ex) {
            }
        }
        Feedback f1 = null;
        for (Feedback f: e.getFeedbacks()) {
            f1 = f;
            System.out.println("Feedback: " + f.toString());
        }
        if (f1 != null) {
            try {
                e.saveFeedback(new Feedback(new Date(), f1.getUser(), e, "added feedback!"));
            } catch (NoRightsException ex) {
            }
        }
        educationalInstitutionRepository.save(e);
        */

        /*
        System.out.println("Cities for med: " + String.join(", ", medicallInstitutionRepository.getCities()));
        ArrayList<MedicalInstitution> m = medicallInstitutionRepository.findAllByDistrict("Saint-Petersburg", "Kirovskyi");
        for (MedicalInstitution some: m) {
            System.out.println("Found med: " + some.getTitle());
        }
        MedicalInstitution i = medicallInstitutionRepository.findById(new Long(6));
        System.out.println("title: " + i.getTitle());

        Ticket r = null;
        for (Ticket r1: i.getTickets()) {
            r = r1;
            System.out.println("ticket doctor name: " + r1.getDoctor().getFullName() + ", user: " + r1.getUser().getFullName() + ", td: " + r1.getDate());
        }
        if (r != null) {
            try {
                i.addTicket(new Ticket(r.getDoctor(), r.getDate()));
                i.addTicket(new Ticket(r.getDoctor(), r.getDate()));
                i.removeTicket(r);
            }
            catch (NoRightsException ex) {
            }
        }
        Doctor f1 = null;
        for (Doctor d: i.getDoctors()) {
            f1 = d;
            System.out.println("Doctor: " + d.getFullName() + ", pos: " + d.getPosition());
        }
        if (f1 != null) {
            try {
                i.addDoctor(new Doctor("doctor5", "doctor5", "doctor5", "doctor5@mail.ru", i, "theraphist", "good", false));
                i.addDoctor(new Doctor("doctor6", "doctor5", "doctor5", "doctor5@mail.ru", i, "theraphist", "good", false));
                i.removeDoctor(f1);
            } catch (NoRightsException ex) {
            }
        }
        medicallInstitutionRepository.save(i);
        */

        /*
        feedbackRepository.deleteFeedbacksForUser(new Long(2));
        eduRequestRepository.deleteEduRequestsForChild(new Long(1));

        LocalDate currentDate = LocalDate.now();
        LocalDate monthAgo = currentDate.minus(1, ChronoUnit.MONTHS);
        ArrayList<Ticket> tickets;
        tickets = ticketRepository.findAllForDoctor(new Long(9), Date.from(monthAgo.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        for (Ticket r1: tickets) {
            System.out.println("ticket doctor name: " + r1.getDoctor().getFullName() + ", user: " + r1.getUser().getFullName() + ", td: " + r1.getDate());
        }

        try {
            SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd");
            String str = new String("2017-11-18");
            Date startDateTime = fromUser.parse(str);
            Date endDateTime = fromUser.parse(str);
            endDateTime.setHours(23);
            endDateTime.setMinutes(59);
            endDateTime.setSeconds(59);
            ticketRepository.deleteTicketsByDoctorAndDate(new Long(8), startDateTime, endDateTime);
            ticketRepository.deleteTicketsByDoctor(new Long(9));
            ticketRepository.cancelTicketsForChild(new Long(1));
        } catch (ParseException e) {
        }
        */

        User user = userBaseRepository.findByUsername("medr");
        if (user.isMedicalRepresentative()) {
            MedicalRepresentative medr = (MedicalRepresentative) user;
            System.out.println("Title: " + medr.getInstitution().getTitle());
        }
        return "hello";
    }
}