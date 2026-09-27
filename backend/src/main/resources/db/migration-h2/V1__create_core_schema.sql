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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE pricing_suggestions (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id),
    current_price NUMERIC(12,2) NOT NULL CHECK (current_price > 0),
    recommended_price NUMERIC(12,2) NOT NULL CHECK (recommended_price > 0),
    direction VARCHAR(16) NOT NULL,
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    reasoning VARCHAR(1000) NOT NULL,
    status VARCHAR(16) NOT NULL,
    trigger_reason VARCHAR(32) NOT NULL,
    pending_key VARCHAR(128) AS (CASE WHEN status = 'PENDING' THEN product_id || ':' || trigger_reason ELSE id END),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reorder_suggestions (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id),
    current_stock INTEGER NOT NULL CHECK (current_stock >= 0),
    recommended_quantity INTEGER NOT NULL CHECK (recommended_quantity > 0),
    lead_time_days INTEGER CHECK (lead_time_days >= 0),
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    reasoning VARCHAR(1000) NOT NULL,
    status VARCHAR(16) NOT NULL,
    trigger_reason VARCHAR(32) NOT NULL,
    pending_key VARCHAR(128) AS (CASE WHEN status = 'PENDING' THEN product_id || ':' || trigger_reason ELSE id END),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_pending_pricing_trigger ON pricing_suggestions(pending_key);
CREATE UNIQUE INDEX uq_pending_reorder_trigger ON reorder_suggestions(pending_key);
CREATE INDEX ix_products_status_category ON products(status, category);
CREATE INDEX ix_pricing_product_status ON pricing_suggestions(product_id, status);
CREATE INDEX ix_reorder_product_status ON reorder_suggestions(product_id, status);
CREATE INDEX ix_pricing_review_queue ON pricing_suggestions(status, created_at DESC);
CREATE INDEX ix_reorder_review_queue ON reorder_suggestions(status, created_at DESC);

ALTER TABLE products ADD CONSTRAINT ck_products_category CHECK (category IN ('ELECTRONICS', 'APPAREL', 'HOME', 'UNSPECIFIED'));
ALTER TABLE products ADD CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'PRICE_REVIEW_PENDING', 'OUT_OF_STOCK', 'INACTIVE'));
ALTER TABLE pricing_suggestions ADD CONSTRAINT ck_pricing_direction CHECK (direction IN ('INCREASE', 'DECREASE', 'HOLD'));
ALTER TABLE pricing_suggestions ADD CONSTRAINT ck_pricing_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));
ALTER TABLE pricing_suggestions ADD CONSTRAINT ck_pricing_trigger CHECK (trigger_reason IN ('INITIAL', 'INVENTORY_LOW', 'DEMAND_SPIKE', 'MANUAL'));
ALTER TABLE reorder_suggestions ADD CONSTRAINT ck_reorder_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));
ALTER TABLE reorder_suggestions ADD CONSTRAINT ck_reorder_trigger CHECK (trigger_reason IN ('INITIAL', 'INVENTORY_LOW', 'DEMAND_SPIKE', 'MANUAL'));
