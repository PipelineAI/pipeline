-- Logback: the reliable, generic, fast and flexible logging framework.
-- Copyright (C) 1999-2010, QOS.ch. All rights reserved.
--
-- See http://logback.qos.ch/license.html for the applicable licensing 
-- conditions.

-- This SQL script creates the required tables by ch.qos.logback.classic.db.DBAppender
-- for Sybase SQLAnywhere.  Tested on SQLAnywhere 10.0.1.

DROP TABLE logging_event_property 
DROP TABLE logging_event_exception 
DROP TABLE logging_event 

CREATE TABLE logging_event 
( 
  timestmp         bigint NOT NULL,
  formatted_message  LONG VARCHAR NOT NULL,
  logger_name       VARCHAR(254) NOT NULL,
  level_string      VARCHAR(254) NOT NULL,
  thread_name       VARCHAR(254),
  reference_flag    SMALLINT,
  arg0              VARCHAR(254),
  arg1              VARCHAR(254),
  arg2              VARCHAR(254),
  arg3              VARCHAR(254),  
  caller_filename   VARCHAR(254) NOT NULL,
  caller_class      VARCHAR(254) NOT NULL,
  caller_method     VARCHAR(254) NOT NULL,
  caller_line       varCHAR(4) NOT NULL,
  event_id          int NOT NULL DEFAULT AUTOINCREMENT,
  PRIMARY KEY(event_id) 
) 			

CREATE TABLE logging_event_property 
  ( 
	event_id          int NOT NULL REFERENCES logging_event(event_id), 
	mapped_key        VARCHAR(254) NOT NULL, 
	mapped_value      LONG VARCHAR, 
	PRIMARY KEY(event_id, mapped_key) 
  ) 

CREATE TABLE logging_event_exception 
  ( 
	event_id         int NOT NULL REFERENCES logging_event(event_id) , 
	i                SMALLINT NOT NULL, 
	trace_line       VARCHAR(254) NOT NULL, 
	PRIMARY KEY(event_id, i) 
  )
