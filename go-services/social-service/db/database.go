package db

import (
	"context"
	"time"

	db "social-service/db/sqlc"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Database struct {
	Pool    *pgxpool.Pool
	Queries *db.Queries
}

func NewDatabase(databaseURL string) (*Database, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	config, err := pgxpool.ParseConfig(databaseURL)
	if err != nil {
		return nil, err
	}

	// Connection pool settings
	config.MaxConns = 50
	config.MinConns = 5
	config.MaxConnLifetime = 30 * time.Minute
	config.MaxConnIdleTime = 5 * time.Minute

	pool, err := pgxpool.NewWithConfig(ctx, config)
	if err != nil {
		return nil, err
	}

	// Test connection
	if err := pool.Ping(ctx); err != nil {
		pool.Close()
		return nil, err
	}

	queries := db.New(pool)

	return &Database{
		Pool:    pool,
		Queries: queries,
	}, nil
}

func (d *Database) Close() {
	d.Pool.Close()
}

// GetQueries returns the sqlc queries instance
func (d *Database) GetQueries() *db.Queries {
	return d.Queries
}

// GetPool returns the pgxpool instance for transactions
func (d *Database) GetPool() *pgxpool.Pool {
	return d.Pool
}
