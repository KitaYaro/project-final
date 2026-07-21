CREATE TABLE IF NOT EXISTS REFERENCE (
    ID bigserial primary key,
    CODE varchar(32) not null,
    REF_TYPE smallint not null,
    ENDPOINT timestamp,
    STARTPOINT timestamp,
    TITLE varchar(1024) not null,
    AUX varchar
);
