CREATE OR REPLACE FUNCTION calculate_total_sales_by_store(p_store_id BIGINT)
RETURNS NUMERIC AS $$
    SELECT COALESCE(SUM(total), 0) FROM sales WHERE store_id = p_store_id;
$$ LANGUAGE SQL;
