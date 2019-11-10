CREATE TABLE person
(
    person_id  uuid PRIMARY KEY,
    first_name character varying NOT NULL,
    last_name  character varying NOT NULL,
    email      character varying
);

CREATE TABLE team
(
    team_id       serial PRIMARY KEY,
    name          character varying        NOT NULL,
    slug          character varying UNIQUE NOT NULL,
    creation_date date                     NOT NULL DEFAULT now()
);

CREATE TABLE team_person
(
    team_id    integer NOT NULL,
    person_id  uuid    NOT NULL,
    joined_at  date    NOT NULL DEFAULT now(),
    is_manager bool    NOT NULL DEFAULT false,
    PRIMARY KEY (team_id, person_id),
    FOREIGN KEY (team_id) REFERENCES team,
    FOREIGN KEY (person_id) REFERENCES person
);