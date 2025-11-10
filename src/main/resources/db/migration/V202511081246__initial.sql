create extension if not exists "uuid-ossp";

create function update_timestamp() returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create table users(
    -- UUID v7
    id uuid primary key not null default uuid_generate_v4(),
    username varchar(255) not null unique,
    password_hash varchar(255) not null,
    email varchar(255) not null unique,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create trigger update_users_timestamp
    before update on users
    for each row execute function update_timestamp();