package query

import (
	"context"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

// Executor executes queries built by SelectBuilder
type Executor struct {
	pool *pgxpool.Pool
}

// NewExecutor creates a new query executor
func NewExecutor(pool *pgxpool.Pool) *Executor {
	return &Executor{pool: pool}
}

// Query executes a select query and returns rows
func (e *Executor) Query(ctx context.Context, builder *SelectBuilder) (pgx.Rows, error) {
	sql, args := builder.ToSQL()
	return e.pool.Query(ctx, sql, args...)
}

// QueryRow executes a select query and returns a single row
func (e *Executor) QueryRow(ctx context.Context, builder *SelectBuilder) pgx.Row {
	sql, args := builder.ToSQL()
	return e.pool.QueryRow(ctx, sql, args...)
}

// Count executes a count query
func (e *Executor) Count(ctx context.Context, builder *SelectBuilder) (int64, error) {
	sql, args := builder.CountSQL()
	var count int64
	err := e.pool.QueryRow(ctx, sql, args...).Scan(&count)
	return count, err
}

// Exec executes a raw SQL command
func (e *Executor) Exec(ctx context.Context, sql string, args ...interface{}) error {
	_, err := e.pool.Exec(ctx, sql, args...)
	return err
}

// RawQuery executes a raw SQL query
func (e *Executor) RawQuery(ctx context.Context, sql string, args ...interface{}) (pgx.Rows, error) {
	return e.pool.Query(ctx, sql, args...)
}

// RawQueryRow executes a raw SQL query returning single row
func (e *Executor) RawQueryRow(ctx context.Context, sql string, args ...interface{}) pgx.Row {
	return e.pool.QueryRow(ctx, sql, args...)
}
