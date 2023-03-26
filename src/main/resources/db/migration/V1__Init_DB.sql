create sequence article_seq start with 1 increment by 50;

create sequence post_seq start with 1 increment by 50;

create sequence usr_seq start with 51 increment by 50;

create table article (
    id bigint not null,
    filename varchar(255),
    fulltext varchar(10000),
    timestamp timestamp(6) with time zone,
    title varchar(100) not null,
    user_id bigint,
    primary key (id)
);

create table post (
    id bigint not null,
    filename varchar(255),
    fulltext varchar(10000),
    timestamp timestamp(6) with time zone,
    title varchar(100) not null,
    user_id bigint,
    primary key (id)
);

create table user_followers (
    follower bigint not null,
    channel_id bigint not null,
    primary key (channel_id, follower)
);

create table user_role (
    user_id bigint not null,
    roles varchar(255)
);

create table usr (
    id bigint not null,
    about varchar(255),
    active boolean not null,
    avatar varchar(255),
    email varchar(255),
    email_activation_code varchar(255),
    fullname varchar(255),
    password varchar(100) not null,
    username varchar(20) not null,
    primary key (id)
);

alter table if exists article add constraint article_user_fk foreign key (user_id) references usr;

alter table if exists post add constraint post_user_fk foreign key (user_id) references usr;

alter table if exists user_followers add constraint channel_user_fk foreign key (channel_id) references usr;

alter table if exists user_followers add constraint follower_user_fk foreign key (follower) references usr;

alter table if exists user_role add constraint role_user_fk foreign key (user_id) references usr;