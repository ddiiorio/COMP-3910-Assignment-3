
DROP DATABASE IF EXISTS timesheet ;
CREATE DATABASE timesheet;
DROP USER IF EXISTS 'timesheetuser'@'localhost';
DROP USER IF EXISTS 'timesheetuser'@'%';
CREATE USER 'timesheetuser'@'localhost' IDENTIFIED BY 'password';
CREATE USER 'timesheetuser'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON timesheet.* TO 'timesheetuser'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON timesheet.* TO 'timesheetuser'@'%' WITH GRANT OPTION;

USE timesheet;

DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
  emp_number int(10) unsigned NOT NULL AUTO_INCREMENT,
  firstName varchar(45) NOT NULL,
  userName varchar(45) NOT NULL,
  password varchar(45) NOT NULL,
  isAdmin bit(1) NOT NULL DEFAULT b'0',
  lastName varchar(45) NOT NULL,
  PRIMARY KEY (emp_number),
  UNIQUE KEY empNumber_UNIQUE (emp_number),
  UNIQUE KEY userName_UNIQUE (userName)
);

INSERT INTO employees VALUES 
(1,'Tony','tonyp','pass',_binary '','Pacheco'),
(2,'Danny','dannyd','password',_binary '','DiIorio'),
(3,'Bruce Link','brucel','pass',_binary '\0','Link');

DROP TABLE IF EXISTS timesheet;
CREATE TABLE timesheet (
  emp_number int(10) unsigned NOT NULL,
  end_week date NOT NULL,
  overtime decimal(10,0) unsigned NOT NULL DEFAULT '0',
  flextime decimal(10,0) unsigned NOT NULL DEFAULT '0',
  timesheet_id int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (timesheet_id),
  UNIQUE KEY timesheet_id_UNIQUE (timesheet_id),
  KEY timesheet_empNumber_idx (emp_number),
  CONSTRAINT timesheet_empNumber FOREIGN KEY (emp_number) REFERENCES employees (emp_number)
);

INSERT INTO timesheet VALUES 
(1,'2018-11-09',0,0,1),
(1,'2018-11-23',0,0,2),
(2,'2018-11-23',0,0,3),
(3,'2018-11-23',0,0,4);

DROP TABLE IF EXISTS timesheet_row;
CREATE TABLE timesheet_row (
  project_id int(10) unsigned NOT NULL,
  work_package varchar(45) NOT NULL,
  notes varchar(200) DEFAULT NULL,
  sun_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  mon_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  tue_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  wed_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  thu_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  fri_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  sat_hours decimal(10,0) unsigned NOT NULL DEFAULT '0',
  timesheet_id int(10) unsigned NOT NULL,
  timesheet_row_id int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (timesheet_row_id),
  UNIQUE KEY timesheet_row_id_UNIQUE (timesheet_row_id),
  KEY timesheet_row_timesheet_id_idx (timesheet_id),
  CONSTRAINT timesheet_row_timesheet_id FOREIGN KEY (timesheet_id) REFERENCES timesheet (timesheet_id)
);

INSERT INTO timesheet_row VALUES
(21,'wp1','',2,8,8,0,9,8,0,1,1),
(21,'wp2','',0,0,0,4,0,0,0,1,2),
(21,'wp3','',0,0,0,4,0,0,2,1,31),
(21,'wp4','',0,0,0,0,0,0,0,1,48),
(1,'wp1','',0,0,0,0,0,0,3,1,49),
(45,'std','',0,8,8,8,8,8,0,2,56),
(0,'','',0,0,0,0,0,0,0,2,57),
(0,'','',0,0,0,0,0,0,0,2,58),
(0,'','',0,0,0,0,0,0,0,2,59),
(0,'','',0,0,0,0,0,0,0,2,60),
(0,'','',0,0,0,0,0,0,0,3,61),
(0,'','',0,0,0,0,0,0,0,3,62),
(0,'','',0,0,0,0,0,0,0,3,64),
(0,'','',0,0,0,0,0,0,0,3,65),
(14,'wp1','Hard day at work',2,6,8,7,4,9,2,3,66),
(0,'','',0,0,0,0,0,0,0,4,86),
(0,'','',0,0,0,0,0,0,0,4,87),
(0,'','',0,0,0,0,0,0,0,4,88),
(0,'','',0,0,0,0,0,0,0,4,89),
(45,'','',0,2,8,0,7,9,4,4,90),
(34,'pkg1','',0,6,0,6,0,0,0,4,91);
