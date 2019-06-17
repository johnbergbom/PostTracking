CREATE TABLE tracking (
	id int8 NOT null,
	tracking_code varchar(64) not null,
	email varchar(64) not null,
	state integer not null,
	last_changed timestamp without time zone not null,
	subject_suffix varchar(64),
	seller_name varchar(64),
	product_name varchar(256),
	last_track_check timestamp without time zone not null
);
CREATE SEQUENCE tracking_id_seq;
ALTER TABLE ONLY tracking ADD CONSTRAINT tracking_pkey PRIMARY KEY (id);
ALTER TABLE tracking ADD CONSTRAINT tracking_track_key UNIQUE (tracking_code);



-- JUST CREATED

ALTER TABLE tracking ADD COLUMN last_track_check timestamp without time zone;
update tracking set last_track_check = last_changed;
alter table tracking alter column last_track_check set not null;
