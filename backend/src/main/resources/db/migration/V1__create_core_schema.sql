CREATE TABLE products (
    id VARCHAR(64) PRIMARY KEY,
    sku VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(32) NOT NULL,
    current_price NUMERIC(12,2) NOT NULL CHECK (current_price > 0),
    stock_level INTEGER NOT NULL CHECK (stock_level >= 0),
    reorder_threshold INTEGER NOT NULL CHECK (reorder_threshold >= 0),
    demand_velocity INTEGER NOT NULL CHECK (demand_velocity >= 0),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pricing_suggestions (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id),
    current_price NUMERIC(12,2) NOT NULL CHECK (current_price > 0),
    recommended_price NUMERIC(12,2) NOT NULL CHECK (recommended_price > 0),
    direction VARCHAR(16) NOT NULL,
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    reasoning TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    trigger_reason VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE reorder_suggestions (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id),
    current_stock INTEGER NOT NULL CHECK (current_stock >= 0),
    recommended_quantity INTEGER NOT NULL CHECK (recommended_quantity > 0),
    lead_time_days INTEGER CHECK (lead_time_days >= 0),
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    reasoning TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    trigger_reason VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_pending_pricing_trigger ON pricing_suggestions(product_id, trigger_reason) WHERE status = 'PENDING';
CREATE UNIQUE INDEX uq_pending_reorder_trigger ON reorder_suggestions(product_id, trigger_reason) WHERE status = 'PENDING';