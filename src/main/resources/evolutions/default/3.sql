# --- !Ups

CREATE TABLE translations (
  quote_id varchar NOT NULL,
  translation_service varchar NOT NULL,
  translation_chain varchar[] NOT NULL,
  translation varchar,
  PRIMARY KEY (quote_id, translation_service, translation_chain),
  CONSTRAINT fk_translations_quotes FOREIGN KEY(quote_id) REFERENCES quotes(quote_id) ON DELETE CASCADE
);

ALTER TABLE quotes
  DROP COLUMN translated_quote;

# --- !Downs
