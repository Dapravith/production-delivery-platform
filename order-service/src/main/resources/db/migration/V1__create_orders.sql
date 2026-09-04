CREATE TABLE IF NOT EXISTS orders (
  id UUID PRIMARY KEY,
  customer_id VARCHAR(255) NOT NULL,
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_orders_customer_created ON orders(customer_id, created_at DESC);
