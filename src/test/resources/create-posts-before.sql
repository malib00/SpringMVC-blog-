delete from post;

insert into post (id, title, user_id) values
(1, 'title1', 51),
(51, 'title2', 51),
(101, 'title3', 101),
(151, 'title22', 101);

alter sequence post_seq restart with 301;