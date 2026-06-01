CREATE TABLE refresh_tokens(
    id bigserial primary key,
    token text unique not null,
    expires_at timestamp not null
)