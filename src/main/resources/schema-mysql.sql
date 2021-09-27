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
    comment_notification bit not null,
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

drop table if exists post;
create table post (
    id bigint not null auto_increment,
    account_id bigint,
    contents longtext,
    created_at datetime(6),
    hits bigint default 0,
    post_type varchar(255),
    title varchar(255) not null,
    updated_at datetime(6),
    primary key (id),
    foreign key (account_id) references account (id)
) engine=InnoDB;

drop table if exists comment;
create table comment (
    id bigint not null auto_increment,
    contents longtext,
    created_at datetime(6),
    updated_at datetime(6),
    account_id bigint not null,
    post_id bigint not null,
    primary key (id),
    foreign key (account_id) references account (id),
    foreign key (post_id_id) references post (id)
) engine=InnoDB;

drop table if exists notification;
create table notification (
    id bigint not null auto_increment,
    checked bit not null,
    created_at datetime(6),
    link varchar(255),
    message varchar(255),
    account_id bigint,
    primary key (id),
    foreign key (account_id) references account (id)
) engine=InnoDB