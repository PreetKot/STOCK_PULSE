ALTER TABLE products
    ADD CONSTRAINT ck_products_category CHECK (category IN ('ELECTRONICS', 'APPAREL', 'HOME', 'UNSPECIFIED')),
    ADD CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'PRICE_REVIEW_PENDING', 'OUT_OF_STOCK', 'INACTIVE'));

ALTER TABLE pricing_suggestions
    ADD CONSTRAINT ck_pricing_direction CHECK (direction IN ('INCREASE', 'DECREASE', 'HOLD')),
    ADD CONSTRAINT ck_pricing_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    ADD CONSTRAINT ck_pricing_trigger CHECK (trigger_reason IN ('INITIAL', 'INVENTORY_LOW', 'DEMAND_SPIKE', 'MANUAL'));

ALTER TABLE reorder_suggestions
    ADD CONSTRAINT ck_reorder_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    ADD CONSTRAINT ck_reorder_trigger CHECK (trigger_reason IN ('INITIAL', 'INVENTORY_LOW', 'DEMAND_SPIKE', 'MANUAL'));

CREATE INDEX ix_products_status_category ON products(status, category);
CREATE INDEX ix_pricing_product_status ON pricing_suggestions(product_id, status);
CREATE INDEX ix_reorder_product_status ON reorder_suggestions(product_id, status);
CREATE INDEX ix_pricing_review_queue ON pricing_suggestions(status, created_at DESC);
CREATE INDEX ix_reorder_review_queue ON reorder_suggestions(status, created_at DESC);
