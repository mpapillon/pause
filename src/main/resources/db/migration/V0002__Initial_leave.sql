CREATE TABLE leave
(
    leave_id        uuid PRIMARY KEY,
    member_id       uuid     NOT NULL,
    start_date      date     NOT NULL,
    end_date        date     NOT NULL,
    half_start_date boolean  NOT NULL DEFAULT false,
    half_end_date   boolean  NOT NULL DEFAULT false,
    type            smallint NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member
);