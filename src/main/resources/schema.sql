CREATE TABLE IF NOT EXISTS stores (
    id      BIGSERIAL PRIMARY KEY,
    code    VARCHAR(100) NOT NULL UNIQUE,
    name    VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    region  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS products (
    id    BIGSERIAL PRIMARY KEY,
    sku   VARCHAR(100) NOT NULL UNIQUE,
    name  VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS sales (
    id        BIGSERIAL PRIMARY KEY,
    store_id  BIGINT NOT NULL REFERENCES stores (id),
    sale_date TIMESTAMP NOT NULL DEFAULT NOW(),
    total     NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS sale_details (
    id         BIGSERIAL PRIMARY KEY,
    sale_id    BIGINT NOT NULL REFERENCES sales (id),
    product_id BIGINT NOT NULL REFERENCES products (id),
    quantity   INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL
);

CREATE OR REPLACE FUNCTION calculate_total_sales_by_store(p_store_id BIGINT)
RETURNS NUMERIC AS $$
    SELECT COALESCE(SUM(total), 0) FROM sales WHERE store_id = p_store_id;
$$ LANGUAGE SQL;
