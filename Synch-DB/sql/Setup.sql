-- First, create the actual database.
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
    PRIMARY KEY (filename)
);

CREATE TABLE writ_info(
    name            VARCHAR(250) NOT NULL, -- Must be unique, checked in program.
    date            DATE NOT NULL,
    kept_secret     BIT NOT NULL,
    twitter_posted  BIT NOT NULL,
    insta_posted    BIT NOT NULL,
    text            MEDIUMTEXT NOT NULL,
    PRIMARY KEY (name)
);

CREATE TABLE users(
    name        VARCHAR(250) NOT NULL,
    pw          VARCHAR(250) NOT NULL,
    PRIMARY KEY (name)
);

CREATE TABLE tags(
    tag_id      INT NOT NULL AUTO_INCREMENT,
    name        VARCHAR(250) NOT NULL,
    desription  TEXT NOT NULL,
    PRIMARY KEY (tag_id)
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
    tag_id          INT NOT NULL,
    pic_filename    VARCHAR(250) NOT NULL
);

CREATE TABLE tags_writs(
    tag_id      INT NOT NULL,
    writ_name   VARCHAR(250) NOT NULL
);
