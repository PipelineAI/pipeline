# Logback: the reliable, generic, fast and flexible logging framework.
# Copyright (C) 1999-2010, QOS.ch. All rights reserved.
#
# See http://logback.qos.ch/license.html for the applicable licensing 
# conditions.

# This SQL script creates the required tables by ch.qos.logback.classic.db.DBAppender
#
# It is intended for IBM DB2 databases.
#
# WARNING  WARNING WARNING  WARNING 
# =================================
# This SQL script has not been tested on an actual DB2
# instance. It may contain errors or even invalid SQL
# statements.

DROP TABLE  logging_event_property;
DROP TABLE  logging_event_exception;
DROP TABLE  logging_event;

CREATE TABLE logging_event 
  (
    timestmp          BIGINT NOT NULL,
   	formatted_message VARCHAR(4000) NOT NULL,
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
    caller_line       CHAR(4) NOT NULL,
    event_id           INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1)
  );

CREATE TABLE logging_event_property
  (
    event_id	      INTEGER NOT NULL,
    mapped_key        VARCHAR(254) NOT NULL,
    mapped_value      VARCHAR(1024),
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );

CREATE TABLE logging_event_exception
  (
    event_id         INTEGER NOT NULL,
    i                SMALLINT NOT NULL,
    trace_line       VARCHAR(254) NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );
