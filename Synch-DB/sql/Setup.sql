-- First, create the database.
CREATE DATABASE db_synchro;
USE db_synchro;

-- Tables with actual information about media.
CREATE TABLE pic_info(
    filename        VARCHAR(250) NOT NULL, -- Must be unique. Check for this in program.
    name            VARCHAR(250) NOT NULL, -- This is the name to be display on website.
    date            DATE NOT NULL,
    explanation     TEXT NOT NULL,
    kept_secret     BIT NOT NULL,
    twitter_posted  BIT NOT NULL,
    insta_posted    BIT NOT NULL,
    category        VARCHAR(250) NOT NULL,

    PRIMARY KEY (filename)
);

CREATE TABLE writ_info(
    name            VARCHAR(250) NOT NULL, -- Must be unique, checked in program.
    date            DATE NOT NULL,
    kept_secret     BIT NOT NULL,
    twitter_posted  BIT NOT NULL,
    insta_posted    BIT NOT NULL,
    text            MEDIUMTEXT NOT NULL,
    category        VARCHAR(250) NOT NULL,

    PRIMARY KEY (name)
);

CREATE TABLE users(
    name    VARCHAR(250) NOT NULL,
    pw      VARCHAR(250) NOT NULL,

    PRIMARY KEY (name)
);

-- Table that saves the name for every front page pic for the picture categories.
CREATE TABLE front_pics(
    category_name   VARCHAR(250) NOT NULL,
    pic_filename    VARCHAR(250) NOT NULL
);

-- Tables for n-m relationships. 
CREATE TABLE user_pics(
    user_name       VARCHAR(250) NOT NULL,
    pic_filename    VARCHAR(250) NOT NULL
);

CREATE TABLE user_writs(
    user_name   VARCHAR(250) NOT NULL,
    writ_name   VARCHAR(250) NOT NULL
);

CREATE TABLE tags_pics(
    tag_name        VARCHAR(250) NOT NULL,
    pic_filename    VARCHAR(250) NOT NULL
);
ALTER TABLE tags_pics ADD UNIQUE (tag_name, pic_filename);

CREATE TABLE tags_writs(
    tag_name    VARCHAR(250) NOT NULL,
    writ_name   VARCHAR(250) NOT NULL
);
ALTER TABLE tags_writs ADD UNIQUE (tag_name, writ_name); -- nur einzigarte Kombination der beiden Spalte zulässig

