CREATE DATABASE IF NOT EXISTS web_state_services;
USE web_state_services;

# drop all tables
DROP TABLE IF EXISTS representatives;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS edu_requests;
DROP TABLE IF EXISTS feedbacks;
DROP TABLE IF EXISTS educational_institutions_seats;
DROP TABLE IF EXISTS institutions;
DROP TABLE IF EXISTS childs;
DROP TABLE IF EXISTS citizens;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS role;

# create tables
CREATE TABLE role (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password CHAR(100),  # used org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL
);
CREATE TABLE user_role (
  user_id int(11) NOT NULL,
  role_id int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_user_role_roleid_idx` (`role_id`),
  FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE notifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user INT NOT NULL,
  notification VARCHAR(1000) NOT NULL,
  created datetime NOT NULL,
  FOREIGN KEY (user) REFERENCES users(id)
);

CREATE TABLE citizens (
  id INT NOT NULL PRIMARY KEY,
  policy VARCHAR(16) NOT NULL,
  passport VARCHAR(10) NOT NULL,
  birth_date datetime NOT NULL,
  FOREIGN KEY citizens(id) REFERENCES users(id)
);

CREATE TABLE childs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  parent INT NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  birth_certificate VARCHAR(10) NOT NULL,
  birth_date datetime NOT NULL,
  FOREIGN KEY (parent) REFERENCES citizens(id)
);

CREATE TABLE institutions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(300) NOT NULL,
  city VARCHAR(100) NOT NULL,
  district VARCHAR(100) NOT NULL,
  telephone VARCHAR(16) NOT NULL,
  fax VARCHAR(60) NOT NULL,
  address VARCHAR(200) NOT NULL,
  is_edu TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE educational_institutions_seats (
  institution_id INT NOT NULL,
  class_number INT NOT NULL,
  seats INT NOT NULL,
  busy_seats INT NOT NULL,
  PRIMARY KEY (institution_id, class_number),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE feedbacks (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user INT NOT NULL,
  feedback_text VARCHAR(1000) NOT NULL,
  created datetime NOT NULL,
  institution_id INT NOT NULL,
  to_user INT DEFAULT NULL,
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (to_user) REFERENCES users(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE edu_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  status ENUM("OPENED", "ACCEPTED_BY_INSTITUTION", "ACCEPTED_BY_PARENT", "REFUSED", "CHILD_IS_ENROLLED") NOT NULL,
  child INT, # NOT NULL,
  parent INT NOT NULL,
  institution_id INT NOT NULL,
  creation_date datetime NOT NULL,
  appointment datetime DEFAULT NULL,
  class_number int NOT NULL,
  FOREIGN KEY (child) REFERENCES childs(id),
  FOREIGN KEY (parent) REFERENCES citizens(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE doctors (
  id INT NOT NULL PRIMARY KEY,
  position VARCHAR(100) NOT NULL,
  summary VARCHAR(1000) NOT NULL,
  institution_id INT NOT NULL,
  approved TINYINT(1) DEFAULT 0,
  FOREIGN KEY doctors(id) REFERENCES users(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE tickets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user INT DEFAULT NULL,
  child INT DEFAULT NULL,
  institution_id INT NOT NULL,
  doctor INT, # NOT NULL,
  ticket_date datetime NOT NULL,
  visited TINYINT(1) DEFAULT 0,
  summary VARCHAR(1000) DEFAULT NULL,
  FOREIGN KEY (user) REFERENCES citizens(id),
  FOREIGN KEY (child) REFERENCES childs(id),
  #FOREIGN KEY (doctor) REFERENCES doctors(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE representatives (
  id INT NOT NULL PRIMARY KEY,
  institution_id INT NOT NULL,
  approved TINYINT(1) DEFAULT 0,
  FOREIGN KEY representatives(id) REFERENCES users(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

# user roles
INSERT INTO role (name) VALUES ('ROLE_CITIZEN'), ('ROLE_ADMIN'), ('ROLE_DOCTOR'), ('ROLE_EDU_REPRESENTATIVE'), ('ROLE_MEDICAL_REPRESENTATIVE');

# inser data for tests
INSERT INTO users(username, full_name, email, password) VALUES ("admin", "admin", "admin@mail.com", "$2a$11$cYWRCoAzhlbww/DSHBpiUeWCsQeQklGVl1IZ.j/t0KdtRJhwnaE3G");
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "admin"), (SELECT id FROM role WHERE name = "ROLE_ADMIN"));

INSERT INTO users(username, full_name, email, password) VALUES ("citizen", "citizen", "citizen@mail.com", "$2a$11$j.xMc4x8kZ.C90cQATove.cEGk/uCX6DeSSdb0CHpiaZv0Jj/CmgG");
INSERT INTO citizens (id, policy, passport, birth_date) VALUES ((SELECT id FROM users WHERE username = "citizen"), "1234567891234566", "4050123450", "1995-01-04");
INSERT INTO childs (parent, full_name, birth_certificate, birth_date) VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), "F I O", "IJ12293948", "2015-01-14");
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "citizen"), (SELECT id FROM role WHERE name = "ROLE_CITIZEN"));

INSERT INTO users(username, full_name, email, password) VALUES ("citizen1", "citizen1", "citizen1@mail.com", "$2a$11$WAkdfaAb9H9Q4r7cIkfC.OTF.Lckjjtp.pTNicVgk6UvzWseKiNC2");
INSERT INTO citizens (id, policy, passport, birth_date) VALUES ((SELECT id FROM users WHERE username = "citizen1"), "1234567891234567", "4050123450", "1995-01-04");
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "citizen1"), (SELECT id FROM role WHERE name = "ROLE_CITIZEN"));

INSERT INTO institutions (title, city, district, telephone, fax, address, is_edu)  VALUES ("school № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69", 1);
INSERT INTO institutions (title, city, district, telephone, fax, address, is_edu)  VALUES ("school № 2", "Saint-Petersburg", "Moskovskiy", "88127777779", "88127777779", "pr. Veteranov h. 79", 1);
INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES ((SELECT id FROM institutions WHERE title = "school № 1"), 1, 50, 10);
INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES ((SELECT id FROM institutions WHERE title = "school № 2"), 1, 20, 5);
INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES ((SELECT id FROM institutions WHERE title = "school № 1"), 2, 50, 10);
INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES ((SELECT id FROM institutions WHERE title = "school № 2"), 2, 20, 5);
INSERT INTO institutions (title, city, district, telephone, fax, address, is_edu)  VALUES ("school № 3", "Moscow", "Kirovskyi", "88127777787", "88127777777", "pr. Veteranov h. 68", 1);
INSERT INTO institutions (title, city, district, telephone, fax, address, is_edu)  VALUES ("school № 4", "Moscow", "Moskovskiy", "88127777799", "88127777779", "pr. Veteranov h. 74", 1);
INSERT INTO institutions (title, city, district, telephone, fax, address, is_edu)  VALUES ("school № 5", "Saint-Petersburg", "Kirovskyi", "88127777877", "88127797777", "pr. Veteranov h. 89", 1);

INSERT INTO edu_requests (status, child, parent, institution_id, creation_date, class_number) VALUES ("OPENED", (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT parent FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "school № 1"), now(), 1);
INSERT INTO edu_requests (status, child, parent, institution_id, creation_date, class_number) VALUES ("OPENED", (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT parent FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "school № 2"), now(), 1);

INSERT INTO feedbacks (user, feedback_text, created,institution_id) VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), "bad institution!", now(), (SELECT id FROM institutions WHERE title = "school № 1"));

INSERT INTO users(username, full_name, email, password) VALUES ("edur", "edur", "edur@mail.com", "$2a$11$CtW12sYpLQcJZ4toj5O41OAQaldfsa90ZVrsuqIbBYKjsrlK6tqr.");
INSERT INTO representatives (id, institution_id, approved) VALUES ((SELECT id FROM users WHERE username = "edur"), (SELECT id FROM institutions WHERE title = "school № 1"), 1);
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "edur"), (SELECT id FROM role WHERE name = "ROLE_EDU_REPRESENTATIVE"));

INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 2", "Saint-Petersburg", "Moskovskiy", "88127777778", "88127777778", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 3", "Moscow", "Kirovskyi", "88127777778", "88127777778", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 4", "Moscow", "Moskovskiy", "88127777778", "88127777778", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 5", "Some city", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 6", "Some city", "Moskovskiy", "88127777777", "88127777777", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 7", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 869");

INSERT INTO feedbacks (user, feedback_text, created,institution_id) VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), "bad institution!", now(), (SELECT id FROM institutions WHERE title = "hospital № 1"));

INSERT INTO users(username, full_name, email, password) VALUES ("medr", "medr", "medr@mail.com", "$2a$11$rxuDUBwvv2fX3haK2vxjn.P3Dg4BBMWxfnPXPphuGRf5qyD3Qod9i");
INSERT INTO representatives (id, institution_id, approved) VALUES ((SELECT id FROM users WHERE username = "medr"), (SELECT id FROM institutions WHERE title = "hospital № 1"), 1);
INSERT INTO users(username, full_name, email, password) VALUES ("medr1", "medr1", "medr1@mail.com", "$2a$11$c9n49VJG4d8uVY6cIG5/Ju8WYxQZZXHJpW.EC512j2CqhcBkJWUnm");
INSERT INTO representatives (id, institution_id, approved) VALUES ((SELECT id FROM users WHERE username = "medr1"), (SELECT id FROM institutions WHERE title = "hospital № 2"), 1);
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "medr"), (SELECT id FROM role WHERE name = "ROLE_MEDICAL_REPRESENTATIVE"));
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "medr1"), (SELECT id FROM role WHERE name = "ROLE_MEDICAL_REPRESENTATIVE"));

INSERT INTO users(username, full_name, email, password) VALUES ("doctor", "doctor", "doctor@mail.com", "$2a$11$33dTrJy4R1oNuWb11/qDu.RWwgaGpyXpWa7UEqs0/sF/IkcpZFBqe");  
INSERT INTO users(username, full_name, email, password) VALUES ("doctor1", "doctor1", "doctor1@mail.com", "$2a$11$5rTuaM4vyG62VrnkV08Hhup9ICS5lWNGlmJyFtwQU6QzHGyBKGoLm");           
INSERT INTO users(username, full_name, email, password) VALUES ("doctor2", "doctor2", "doctor2@mail.com", "$2a$11$gVeZKErOjlRXb.LySFmf3exKuW0lMtsIO2kvdLyvj8PMhCZAQYopW");      
INSERT INTO doctors (id, position, summary, institution_id, approved) VALUES ((SELECT id FROM users WHERE username = "doctor"), "therapist", "good doctor", (SELECT id FROM institutions WHERE title = "hospital № 1"), 1);
INSERT INTO doctors (id, position, summary, institution_id, approved) VALUES ((SELECT id FROM users WHERE username = "doctor1"), "therapist", "good doctor", (SELECT id FROM institutions WHERE title = "hospital № 2"), 1);
INSERT INTO doctors (id, position, summary, institution_id, approved) VALUES ((SELECT id FROM users WHERE username = "doctor2"), "therapist", "good doctor", (SELECT id FROM institutions WHERE title = "hospital № 1"), 1);
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "doctor"), (SELECT id FROM role WHERE name = "ROLE_DOCTOR"));
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "doctor1"), (SELECT id FROM role WHERE name = "ROLE_DOCTOR"));
INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = "doctor2"), (SELECT id FROM role WHERE name = "ROLE_DOCTOR"));

INSERT INTO tickets (user, child, institution_id, doctor, ticket_date)  VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), null, (SELECT id FROM institutions WHERE title = "hospital № 1"), (SELECT id FROM users WHERE username = "doctor"), now());
INSERT INTO tickets (user, child, institution_id, doctor, ticket_date)  VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "hospital № 1"), (SELECT id FROM users WHERE username = "doctor"), now());
INSERT INTO tickets (user, child, institution_id, doctor, ticket_date)  VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "hospital № 1"), (SELECT id FROM users WHERE username = "doctor"), now());
INSERT INTO tickets (user, child, institution_id, doctor, ticket_date)  VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "hospital № 1"), (SELECT id FROM users WHERE username = "doctor2"), now());
INSERT INTO tickets (user, child, institution_id, doctor, ticket_date, visited, summary)  VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "hospital № 1"), (SELECT id FROM users WHERE username = "doctor"), now(), 1, "good!");
INSERT INTO tickets (user, child, institution_id, doctor, ticket_date)  VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), (SELECT id FROM childs WHERE birth_certificate = "IJ12293948"), (SELECT id FROM institutions WHERE title = "hospital № 1"), (SELECT id FROM users WHERE username = "doctor2"), now() + INTERVAL 5 MINUTE);

INSERT INTO notifications (user, notification, created) VALUES ((SELECT id FROM citizens WHERE policy = "1234567891234566"), "The ticket was canceled!", now());