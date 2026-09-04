ALTER TABLE orders
  ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';

ALTER TABLE orders
  ALTER COLUMN currency DROP DEFAULT;

ALTER TABLE orders
  ADD CONSTRAINT orders_amount_max_check CHECK (amount <= 1000000.00),
  ADD CONSTRAINT orders_currency_check CHECK (currency IN ('USD', 'KHR'));

GRANT SELECT, INSERT, UPDATE, DELETE ON orders TO ${appRole};
