-- Flyway baseline. Conventions: V<semver-ish>__<snake_case>.sql, one change per
-- file, backward-compatible only (expand-then-contract). Never edit a released migration.
CREATE TABLE IF NOT EXISTS products (
  id          VARCHAR(36)  NOT NULL PRIMARY KEY,
  name        VARCHAR(200) NOT NULL,
  description VARCHAR(2000),
  price_cents BIGINT       NOT NULL CHECK (price_cents >= 0),
  created_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_products_name ON products (name);
