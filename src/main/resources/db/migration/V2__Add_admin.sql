insert into usr (id, username, password, active)
    values (1,'admin','$2a$08$RX.bW5G13/1AgWlO9xnWu.urjrcatjmIDP5PxejpxjXuU9ulM4Ogi', true);

insert into user_role (user_id,roles)
    values (1,'USER'), (1,'MODERATOR'), (1,'ADMIN');
