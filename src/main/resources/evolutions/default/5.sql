# --- !Ups

ALTER TABLE translations ADD COLUMN movie_id varchar;
ALTER TABLE translations ADD COLUMN quote varchar;
ALTER TABLE translations DROP CONSTRAINT fk_translations_quotes;
ALTER TABLE translations ADD CONSTRAINT fk_translations_movies
  FOREIGN KEY(movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE;
DROP TABLE quotes;


# --- !Downs

