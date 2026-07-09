-- Create loans table
CREATE TABLE IF NOT EXISTS loans (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  book_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  returned_at TIMESTAMPTZ
);

-- Optionally add an index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans(user_id);
