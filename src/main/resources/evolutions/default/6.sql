# --- !Ups

ALTER TABLE movies ALTER COLUMN data TYPE jsonb USING data::jsonb;
ALTER TABLE movies ALTER COLUMN quotes TYPE jsonb USING quotes::jsonb;
ALTER TABLE translations ALTER COLUMN quote TYPE jsonb USING quote::jsonb;
ALTER TABLE translations ALTER COLUMN translation TYPE jsonb USING translation::jsonb;

# --- !Downs
