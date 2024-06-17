create table device_profile(
  device_id integer primary key autoincrement, -- rowid
  device_secret text not null unique,
  device_address text not null unique
) strict;

create table message(
  message_id integer primary key autoincrement, -- rowid
  content text not null unique,
  on_device int,
  at_place text, -- TODO: geo coordinate with spatial index
  check ((on_device is null) <> (at_place is null))
) strict;
create index idx_message_on_device on message(on_device);

create table message_history(
  created_at_millis int not null default (unixepoch('subsec') * 1000),
  message_id integer not null references message(message_id),
  on_device int,
  at_place text, -- TODO: geo coordinate with spatial index
  check ((on_device is null) <> (at_place is null))
) strict;

create table contact(
  device_id int not null,
  contact_device_id int not null,
  unique(device_id, contact_device_id) -- TODO: additional index with reversed order
) strict;
