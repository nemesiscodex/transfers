-- naive_transfer
create table naive_transfer(
    id uuid primary key not null default uuid_generate_v4(),
    user_id uuid not null,
    recipient_id uuid not null,
    amount decimal(18, 2) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index idx_naive_transfer_user_id on naive_transfer(user_id);
create index idx_naive_transfer_recipient_id on naive_transfer(recipient_id);

create trigger update_naive_transfer_timestamp
    before update on naive_transfer
    for each row execute function update_timestamp();

-- naive_ledger
create table naive_ledger(
    id uuid primary key not null default uuid_generate_v4(),
    user_id uuid not null,
    amount decimal(18, 2) not null,
    transfer_id uuid null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index idx_naive_ledger_user_id on naive_ledger(user_id);
create index idx_naive_ledger_transfer_id on naive_ledger(transfer_id);

create trigger update_naive_ledger_timestamp
    before update on naive_ledger
    for each row execute function update_timestamp();

-- naive_balance
create table naive_balance(
    id uuid primary key not null default uuid_generate_v4(),
    user_id uuid not null,
    amount decimal(18, 2) not null,
    open_ledger_id uuid not null,
    close_ledger_id uuid null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index idx_naive_balance_user_id on naive_balance(user_id);
-- unique only for closed ledger id null
create unique index idx_naive_balance_user_id_close_ledger_id on naive_balance(user_id)
    where close_ledger_id is null;

create trigger update_naive_balance_timestamp
    before update on naive_balance
    for each row execute function update_timestamp();
