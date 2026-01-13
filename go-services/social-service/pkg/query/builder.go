package query

import (
	"fmt"
	"strings"
)

// SortDirection represents sort order
type SortDirection string

const (
	ASC  SortDirection = "ASC"
	DESC SortDirection = "DESC"
)

// OrderBy represents an ORDER BY clause
type OrderBy struct {
	Field     string
	Direction SortDirection
}

// SelectBuilder builds SELECT queries - similar to jOOQ DSL
type SelectBuilder struct {
	table      string
	columns    []string
	joins      []string
	conditions []Condition
	orderBy    []OrderBy
	limit      int
	offset     int
	groupBy    []string
	having     Condition
}

// Select creates a new SelectBuilder
func Select(columns ...string) *SelectBuilder {
	if len(columns) == 0 {
		columns = []string{"*"}
	}
	return &SelectBuilder{
		columns: columns,
	}
}

// From sets the table
func (b *SelectBuilder) From(table string) *SelectBuilder {
	b.table = table
	return b
}

// Join adds a JOIN clause
func (b *SelectBuilder) Join(join string) *SelectBuilder {
	b.joins = append(b.joins, "JOIN "+join)
	return b
}

// LeftJoin adds a LEFT JOIN clause
func (b *SelectBuilder) LeftJoin(join string) *SelectBuilder {
	b.joins = append(b.joins, "LEFT JOIN "+join)
	return b
}

// RightJoin adds a RIGHT JOIN clause
func (b *SelectBuilder) RightJoin(join string) *SelectBuilder {
	b.joins = append(b.joins, "RIGHT JOIN "+join)
	return b
}

// Where adds conditions
func (b *SelectBuilder) Where(conditions ...Condition) *SelectBuilder {
	b.conditions = append(b.conditions, conditions...)
	return b
}

// OrderByAsc adds ascending order
func (b *SelectBuilder) OrderByAsc(field string) *SelectBuilder {
	b.orderBy = append(b.orderBy, OrderBy{Field: field, Direction: ASC})
	return b
}

// OrderByDesc adds descending order
func (b *SelectBuilder) OrderByDesc(field string) *SelectBuilder {
	b.orderBy = append(b.orderBy, OrderBy{Field: field, Direction: DESC})
	return b
}

// Limit sets the limit
func (b *SelectBuilder) Limit(limit int) *SelectBuilder {
	b.limit = limit
	return b
}

// Offset sets the offset
func (b *SelectBuilder) Offset(offset int) *SelectBuilder {
	b.offset = offset
	return b
}

// GroupBy adds GROUP BY clause
func (b *SelectBuilder) GroupBy(fields ...string) *SelectBuilder {
	b.groupBy = append(b.groupBy, fields...)
	return b
}

// Having adds HAVING clause
func (b *SelectBuilder) Having(condition Condition) *SelectBuilder {
	b.having = condition
	return b
}

// ToSQL generates the SQL query and arguments
func (b *SelectBuilder) ToSQL() (string, []interface{}) {
	var parts []string
	var args []interface{}
	argIndex := 1

	// SELECT
	parts = append(parts, "SELECT "+strings.Join(b.columns, ", "))

	// FROM
	parts = append(parts, "FROM "+b.table)

	// JOINS
	for _, join := range b.joins {
		parts = append(parts, join)
	}

	// WHERE
	if len(b.conditions) > 0 {
		whereParts := make([]string, len(b.conditions))
		for i, cond := range b.conditions {
			sql, condArgs := cond.ToSQL()
			// Reindex placeholders
			for j := 1; j <= len(condArgs); j++ {
				sql = strings.Replace(sql, fmt.Sprintf("$%d", j), fmt.Sprintf("$%d", argIndex), 1)
				argIndex++
			}
			whereParts[i] = sql
			args = append(args, condArgs...)
		}
		parts = append(parts, "WHERE "+strings.Join(whereParts, " AND "))
	}

	// GROUP BY
	if len(b.groupBy) > 0 {
		parts = append(parts, "GROUP BY "+strings.Join(b.groupBy, ", "))
	}

	// HAVING
	if b.having != nil {
		sql, havingArgs := b.having.ToSQL()
		for j := 1; j <= len(havingArgs); j++ {
			sql = strings.Replace(sql, fmt.Sprintf("$%d", j), fmt.Sprintf("$%d", argIndex), 1)
			argIndex++
		}
		parts = append(parts, "HAVING "+sql)
		args = append(args, havingArgs...)
	}

	// ORDER BY
	if len(b.orderBy) > 0 {
		orderParts := make([]string, len(b.orderBy))
		for i, o := range b.orderBy {
			orderParts[i] = fmt.Sprintf("%s %s", o.Field, o.Direction)
		}
		parts = append(parts, "ORDER BY "+strings.Join(orderParts, ", "))
	}

	// LIMIT
	if b.limit > 0 {
		parts = append(parts, fmt.Sprintf("LIMIT $%d", argIndex))
		args = append(args, b.limit)
		argIndex++
	}

	// OFFSET
	if b.offset > 0 {
		parts = append(parts, fmt.Sprintf("OFFSET $%d", argIndex))
		args = append(args, b.offset)
	}

	return strings.Join(parts, " "), args
}

// CountSQL generates a COUNT query
func (b *SelectBuilder) CountSQL() (string, []interface{}) {
	var parts []string
	var args []interface{}
	argIndex := 1

	parts = append(parts, "SELECT COUNT(*)")
	parts = append(parts, "FROM "+b.table)

	for _, join := range b.joins {
		parts = append(parts, join)
	}

	if len(b.conditions) > 0 {
		whereParts := make([]string, len(b.conditions))
		for i, cond := range b.conditions {
			sql, condArgs := cond.ToSQL()
			for j := 1; j <= len(condArgs); j++ {
				sql = strings.Replace(sql, fmt.Sprintf("$%d", j), fmt.Sprintf("$%d", argIndex), 1)
				argIndex++
			}
			whereParts[i] = sql
			args = append(args, condArgs...)
		}
		parts = append(parts, "WHERE "+strings.Join(whereParts, " AND "))
	}

	return strings.Join(parts, " "), args
}
