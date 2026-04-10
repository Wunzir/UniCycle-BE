-- Migration: Create Listings Table
-- Description: Stores marketplace items linked to users

CREATE TABLE listings (
    -- Primary Key
                          id BIGSERIAL PRIMARY KEY,

    -- Foreign Key to Users Table
                          user_id BIGINT NOT NULL,

    -- Core Listing Details
                          name VARCHAR(255) NOT NULL,
                          category VARCHAR(100) NOT NULL, -- e.g., 'electronics', 'furniture'
                          picture VARCHAR(1024),          -- URL or storage path placeholder
                          description TEXT,
                          price DECIMAL(12, 2) NOT NULL,  -- Standard currency format
                          status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SOLD

    -- Audit Timestamps
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Constraints
                          CONSTRAINT fk_listing_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users (id)
                                  ON DELETE CASCADE
);

-- Indexing for Performance
-- This is critical for the "Get all listings from University" query
CREATE INDEX idx_listings_user_id ON listings(user_id);

-- Optional: Index for filtering by status (e.g., only show ACTIVE items on Home)
CREATE INDEX idx_listings_status ON listings(status);