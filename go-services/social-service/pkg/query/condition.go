package query

import (
	"fmt"
	"strings"
)

// Condition represents a WHERE condition
type Condition interface {
	ToSQL() (string, []interface{})
}

// ============================================
// Simple Conditions
// ============================================

type simpleCondition struct {
	field    string
	operator string
	value    interface{}
}

func (c *simpleCondition) ToSQL() (string, []interface{}) {
	return fmt.Sprintf("%s %s $1", c.field, c.operator), []interface{}{c.value}
}

// Eq creates an equals condition
func Eq(field string, value interface{}) Condition {
	return &simpleCondition{field: field, operator: "=", value: value}
}

// NotEq creates a not equals condition
func NotEq(field string, value interface{}) Condition {
	return &simpleCondition{field: field, operator: "!=", value: value}
}

// Gt creates a greater than condition
func Gt(field string, value interface{}) Condition {
	return &simpleCondition{field: field, operator: ">", value: value}
}

// Gte creates a greater than or equals condition
func Gte(field string, value interface{}) Condition {
	return &simpleCondition{field: field, operator: ">=", value: value}
}

// Lt creates a less than condition
func Lt(field string, value interface{}) Condition {
	return &simpleCondition{field: field, operator: "<", value: value}
}

// Lte creates a less than or equals condition
func Lte(field string, value interface{}) Condition {
	return &simpleCondition{field: field, operator: "<=", value: value}
}

// ============================================
// Like Conditions
// ============================================

type likeCondition struct {
	field   string
	pattern string
	not     bool
}

func (c *likeCondition) ToSQL() (string, []interface{}) {
	op := "ILIKE"
	if c.not {
		op = "NOT ILIKE"
	}
	return fmt.Sprintf("%s %s $1", c.field, op), []interface{}{c.pattern}
}

// Like creates a LIKE condition
func Like(field string, pattern string) Condition {
	return &likeCondition{field: field, pattern: pattern}
}

// Contains creates a LIKE %value% condition
func Contains(field string, value string) Condition {
	return &likeCondition{field: field, pattern: "%" + value + "%"}
}

// StartsWith creates a LIKE value% condition
func StartsWith(field string, value string) Condition {
	return &likeCondition{field: field, pattern: value + "%"}
}

// EndsWith creates a LIKE %value condition
func EndsWith(field string, value string) Condition {
	return &likeCondition{field: field, pattern: "%" + value}
}

// ============================================
// NULL Conditions
// ============================================

type nullCondition struct {
	field string
	isNot bool
}

func (c *nullCondition) ToSQL() (string, []interface{}) {
	if c.isNot {
		return fmt.Sprintf("%s IS NOT NULL", c.field), nil
	}
	return fmt.Sprintf("%s IS NULL", c.field), nil
}

// IsNull creates an IS NULL condition
func IsNull(field string) Condition {
	return &nullCondition{field: field, isNot: false}
}

// IsNotNull creates an IS NOT NULL condition
func IsNotNull(field string) Condition {
	return &nullCondition{field: field, isNot: true}
}

// ============================================
// IN Conditions
// ============================================

type inCondition struct {
	field  string
	values []interface{}
	not    bool
}

func (c *inCondition) ToSQL() (string, []interface{}) {
	if len(c.values) == 0 {
		if c.not {
			return "TRUE", nil
		}
		return "FALSE", nil
	}

	placeholders := make([]string, len(c.values))
	for i := range c.values {
		placeholders[i] = fmt.Sprintf("$%d", i+1)
	}

	op := "IN"
	if c.not {
		op = "NOT IN"
	}
	return fmt.Sprintf("%s %s (%s)", c.field, op, strings.Join(placeholders, ", ")), c.values
}

// In creates an IN condition
func In(field string, values ...interface{}) Condition {
	return &inCondition{field: field, values: values, not: false}
}

// NotIn creates a NOT IN condition
func NotIn(field string, values ...interface{}) Condition {
	return &inCondition{field: field, values: values, not: true}
}

// ============================================
// Between Condition
// ============================================

type betweenCondition struct {
	field string
	from  interface{}
	to    interface{}
}

func (c *betweenCondition) ToSQL() (string, []interface{}) {
	return fmt.Sprintf("%s BETWEEN $1 AND $2", c.field), []interface{}{c.from, c.to}
}

// Between creates a BETWEEN condition
func Between(field string, from, to interface{}) Condition {
	return &betweenCondition{field: field, from: from, to: to}
}

// ============================================
// Logical Conditions
// ============================================

type andCondition struct {
	conditions []Condition
}

func (c *andCondition) ToSQL() (string, []interface{}) {
	if len(c.conditions) == 0 {
		return "TRUE", nil
	}

	parts := make([]string, len(c.conditions))
	var allArgs []interface{}
	argIndex := 1

	for i, cond := range c.conditions {
		sql, args := cond.ToSQL()
		// Reindex placeholders
		for j := 1; j <= len(args); j++ {
			sql = strings.Replace(sql, fmt.Sprintf("$%d", j), fmt.Sprintf("$%d", argIndex), 1)
			argIndex++
		}
		parts[i] = sql
		allArgs = append(allArgs, args...)
	}

	return "(" + strings.Join(parts, " AND ") + ")", allArgs
}

// And creates an AND condition
func And(conditions ...Condition) Condition {
	return &andCondition{conditions: conditions}
}

type orCondition struct {
	conditions []Condition
}

func (c *orCondition) ToSQL() (string, []interface{}) {
	if len(c.conditions) == 0 {
		return "FALSE", nil
	}

	parts := make([]string, len(c.conditions))
	var allArgs []interface{}
	argIndex := 1

	for i, cond := range c.conditions {
		sql, args := cond.ToSQL()
		// Reindex placeholders
		for j := 1; j <= len(args); j++ {
			sql = strings.Replace(sql, fmt.Sprintf("$%d", j), fmt.Sprintf("$%d", argIndex), 1)
			argIndex++
		}
		parts[i] = sql
		allArgs = append(allArgs, args...)
	}

	return "(" + strings.Join(parts, " OR ") + ")", allArgs
}

// Or creates an OR condition
func Or(conditions ...Condition) Condition {
	return &orCondition{conditions: conditions}
}

type notCondition struct {
	condition Condition
}

func (c *notCondition) ToSQL() (string, []interface{}) {
	sql, args := c.condition.ToSQL()
	return "NOT (" + sql + ")", args
}

// Not negates a condition
func Not(condition Condition) Condition {
	return &notCondition{condition: condition}
}
