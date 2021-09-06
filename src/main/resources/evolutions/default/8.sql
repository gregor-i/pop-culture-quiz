# --- !Ups

CREATE SEQUENCE translations_ids;
ALTER TABLE translations ADD COLUMN id integer;
ALTER TABLE translations ALTER COLUMN id SET DEFAULT nextval('translations_ids');
UPDATE translations SET id = nextval('translations_ids');
ALTER TABLE translations ALTER COLUMN id SET NOT NULL;
ALTER TABLE translations DROP CONSTRAINT translations_pkey;
ALTER TABLE translations ADD PRIMARY KEY (id);
UPDATE translations SET speech = NULL;

# --- !Downs
