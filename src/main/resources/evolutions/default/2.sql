# --- !Ups

ALTER TABLE quotes
  ADD COLUMN translated_quote varchar;

# --- !Downs
