create table device_profile(
  device_id integer primary key, -- rowid
  device_secret text not null unique,
  device_address text not null unique
) strict;

create table contact(
  device_id int not null,
  contact_device_id int not null,
  unique(device_id, contact_device_id) -- TODO: additional index with reversed order
) strict;

create table message(
  message_id integer primary key, -- rowid
  content text not null unique,
  on_device int references device_profile(device_id),
  at_location int references location(location_id),
  check ((on_device is null) <> (at_location is null))
) strict;
create index idx_message_on_device on message(on_device);
create index idx_message_at_location on message(at_location);

create table message_history(
  created_at_millis int not null default (unixepoch('subsec') * 1000),
  message_id integer not null references message(message_id),
  on_device int references device_profile(device_id),
  at_location int references location(location_id),
  check ((on_device is null) <> (at_location is null))
) strict;

create table location(
  location_id integer primary key, -- rowid
  lat real not null,
  lon real not null,
  -- https://en.wikipedia.org/wiki/web_mercator_projection
  x real not null generated always as (
      6378137.0 * (lon * pi() / 180.0)
  ), -- TODO: stored
  y real not null generated always as (
      6378137.0 * ln(tan((pi() / 4.0) + (lat * pi() / 360.0)))
  ) -- TODO: stored
) strict;

create virtual table spatial_index using rtree(location_id, minx, maxx, miny, maxy);

create trigger location_insert after insert on location begin
    insert into spatial_index(location_id, minx, maxx, miny, maxy)
    values (new.location_id, new.x, new.x, new.y, new.y);
end;

create trigger location_update after update on location
begin
    update spatial_index
    set minx = new.x, maxx = new.x, miny = new.y, maxy = new.y
    where location_id = old.location_id;
end;

create trigger location_delete after delete on location
begin
    delete from spatial_index where location_id = old.location_id;
end;
