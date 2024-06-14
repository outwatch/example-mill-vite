CREATE TABLE "User" (
    "id" TEXT NOT NULL PRIMARY KEY,
    "createdAt" INT NOT NULL DEFAULT (unixepoch('subsec')*1000)
    , isAdmin integer not null default false
) STRICT;

CREATE TABLE post (
    "id" INTEGER NOT NULL PRIMARY KEY,
    "content" TEXT NOT NULL,
    "createdAt" INT NOT NULL DEFAULT (unixepoch('subsec')*1000), deletedAt integer default null
) STRICT;
