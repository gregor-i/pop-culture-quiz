# Fractal schema

# --- !Ups

CREATE TABLE movies (
  movie_id varchar NOT NULL, -- ie tt1345836
  state varchar,
  PRIMARY KEY (movie_id)
);

CREATE TABLE quotes (
  quote_id varchar NOT NULL,
  movie_id varchar NOT NULL,
  data varchar NOT NULL,
  PRIMARY KEY (quote_id),
  CONSTRAINT fk_quotes_movies FOREIGN KEY(movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE
);

# --- !Downs
