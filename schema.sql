create table message(
  message_id int primary key, -- rowid
  content text not null unique
) strict;

create table inbox(
  message_id int not null primary key references message(id),
  user_id text not null
) strict;

create table contacts(
  user_id text not null,
  contact_user_id text not null,
  unique(user_id, contact_user_id)
) strict;
