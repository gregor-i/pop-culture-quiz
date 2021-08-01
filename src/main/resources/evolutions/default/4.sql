# --- !Ups

ALTER TABLE movies DROP COLUMN state;
ALTER TABLE movies ADD COLUMN data varchar;
ALTER TABLE movies ADD COLUMN quotes varchar;

# --- !Downs

