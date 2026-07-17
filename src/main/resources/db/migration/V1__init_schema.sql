-- users: registered accounts, password stored as bcrypt hash
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- wallets: one per user, fiat balance in virtual USD
CREATE TABLE wallets (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL UNIQUE REFERENCES users (id),
    fiat_balance NUMERIC(20, 2) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- holdings: crypto amounts per wallet per symbol
CREATE TABLE holdings (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id  UUID NOT NULL REFERENCES wallets (id),
    symbol     VARCHAR(10) NOT NULL,
    quantity   NUMERIC(38, 8) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (wallet_id, symbol)
);

-- transactions: immutable trade history
CREATE TABLE transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id),
    symbol          VARCHAR(10) NOT NULL,
    side            VARCHAR(4)  NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity        NUMERIC(38, 8) NOT NULL,
    execution_price NUMERIC(20, 2) NOT NULL,
    fiat_amount     NUMERIC(20, 2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- price_snapshots: historical prices recorded by the scheduler
CREATE TABLE price_snapshots (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol      VARCHAR(10) NOT NULL,
    price       NUMERIC(20, 2) NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_user ON transactions (user_id, created_at DESC);
CREATE INDEX idx_snapshots_symbol ON price_snapshots (symbol, captured_at DESC);