drop table if exists person;
create table person (name string, temperature real, timestamp string);
insert into person values ('A', 101.2, datetime('now', '-5 day'));
insert into person values ('A', 100.5, datetime('now', '-4 day'));
insert into person values ('A', 100.6, datetime('now', '-3 day'));
insert into person values ('A', 101.7, datetime('now', '-2 day'));
insert into person values ('A', 101.2, datetime('now', '-1 day'));
	 
insert into person values ('B', 100.2, datetime('now', '-5 day'));
insert into person values ('B', 99.5, datetime('now', '-4 day'));
insert into person values ('B', 99.6, datetime('now', '-3 day'));
insert into person values ('B', 99.2, datetime('now', '-2 day'));
insert into person values ('B', 99.7, datetime('now', '-1 day'));
