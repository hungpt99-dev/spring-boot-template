-- Transactional outbox. Conventions: V<n>__<snake_case>.sql, backward-compatible only.
CREATE TABLE IF NOT EXISTS outbox_events (
  id             UUID         NOT NULL PRIMARY KEY,
  aggregate_type VARCHAR(100) NOT NULL,
  aggregate_id   VARCHAR(100) NOT NULL,
  type           VARCHAR(200) NOT NULL,
  topic          VARCHAR(200) NOT NULL,
  payload        TEXT         NOT NULL,
  created_at     TIMESTAMPTZ  NOT NULL,
  published_at   TIMESTAMPTZ,
  attempts       INT          NOT NULL DEFAULT 0,
  last_error     VARCHAR(2000)
);

CREATE INDEX IF NOT EXISTS ix_outbox_unpublished
  ON outbox_events (created_at) WHERE published_at IS NULL;
