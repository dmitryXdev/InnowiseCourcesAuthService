CREATE TABLE users
(
    id       bigint primary key,
    email    varchar(255) unique not null,
    login    varchar(255) unique not null,
    password varchar(255) unique not null,
    role     varchar(255) unique not null,
    token_id bigint,
    foreign key (token_id) references refresh_tokens (id)
)