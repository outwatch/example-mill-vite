create table device_profile(
  device_id text not null primary key,
  public_device_id text not null unique
) strict;

create table message(
  message_id integer primary key autoincrement not null, -- rowid
  content text not null unique,
  on_device text,
  at_place text, -- TODO: geo coordinate with spatial index
  check ((on_device is null) <> (at_place is null))
) strict;
create index idx_message_on_device on message(on_device);

create table message_history(
  created_at int not null default (unixepoch('subsec')),
  message_id integer not null references message(message_id),
  on_device text,
  at_place text, -- TODO: geo coordinate with spatial index
  check ((on_device is null) <> (at_place is null))
) strict;

-- create table inbox(
--   message_id int not null primary key references message(message_id),
--   device_id text not null
-- ) strict;
-- create index idx_inbox_device_id on inbox(device_id);


create table trust(
  device_id text not null,
  contact_device_id text not null,
  unique(device_id, contact_device_id)
) strict;
