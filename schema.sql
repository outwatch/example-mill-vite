create table user_profile(
  user_id text not null primary key,
  user_name text not null
) strict;

create table message(
  message_id integer primary key autoincrement not null, -- rowid
  content text not null unique
) strict;

create table inbox(
  message_id int not null primary key references message(message_id),
  user_id text not null
) strict;
create index idx_inbox_user_id on inbox(user_id);

create table contacts(
  user_id text not null,
  contact_user_id text not null,
  unique(user_id, contact_user_id)
) strict;
