ALTER TABLE article DROP COLUMN filename;
ALTER TABLE article ADD COLUMN image_file_uuid varchar(255);

ALTER TABLE post DROP COLUMN filename;
ALTER TABLE post ADD COLUMN image_file_uuid varchar(255);

ALTER TABLE usr DROP COLUMN avatar;
ALTER TABLE usr ADD COLUMN avatar_image_uuid varchar(255);

create table image_file (
    uuid varchar(255) not null,
    url varchar(255),
    primary key (uuid)
);

alter table if exists article add constraint article_image_file_fk foreign key (image_file_uuid) references image_file;

alter table if exists post add constraint post_image_file_fk foreign key (image_file_uuid) references image_file;

alter table if exists usr add constraint user_image_file_fk foreign key (avatar_image_uuid) references image_file;