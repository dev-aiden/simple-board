drop database if exists simple_board;
create database simple_board;

drop table if exists account;
create table account (
    id bigint not null auto_increment,
    email varchar(255) not null,
    email_check_token varchar(255) not null,
    email_check_token_generated_at datetime(6),
    email_verified bit not null,
    joined_at datetime(6),
    login_id varchar(255) not null,
    nickname varchar(255) not null,
    password varchar(255) not null,
    profile_image longtext,
    primary key (id),
    CONSTRAINT UK_login_id UNIQUE (login_id),
    CONSTRAINT UK_email UNIQUE (email),
    CONSTRAINT UK_nickname UNIQUE (nickname)
) engine=InnoDB;

drop table if exists persistent_logins;
create table persistent_logins (
    series varchar(64) not null,
    last_used datetime(6) not null,
    token varchar(64) not null,
    username varchar(64) not null,
    primary key (series)
) engine=InnoDB;