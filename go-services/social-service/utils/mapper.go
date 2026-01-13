package utils

import (
	"reflect"

	"github.com/jinzhu/copier"
)

// MapStruct copies src to dst using copier
func MapStruct(dst, src any) error {
	return copier.Copy(dst, src)
}

// MapSlice converts slice of src type to slice of dst type
func MapSlice[D, S any](src []S) []D {
	dst := make([]D, len(src))
	for i, s := range src {
		var d D
		_ = copier.Copy(&d, &s)
		dst[i] = d
	}
	return dst
}

// IsNil checks if value is nil
func IsNil(v any) bool {
	if v == nil {
		return true
	}
	rv := reflect.ValueOf(v)
	switch rv.Kind() {
	case reflect.Ptr, reflect.Interface, reflect.Map, reflect.Slice, reflect.Chan, reflect.Func:
		return rv.IsNil()
	}
	return false
}

// Contains checks if slice contains element
func Contains[T comparable](slice []T, elem T) bool {
	for _, v := range slice {
		if v == elem {
			return true
		}
	}
	return false
}

// Unique returns unique elements from slice
func Unique[T comparable](slice []T) []T {
	seen := make(map[T]bool)
	result := make([]T, 0)
	for _, v := range slice {
		if !seen[v] {
			seen[v] = true
			result = append(result, v)
		}
	}
	return result
}

// Filter filters slice by predicate
func Filter[T any](slice []T, predicate func(T) bool) []T {
	result := make([]T, 0)
	for _, v := range slice {
		if predicate(v) {
			result = append(result, v)
		}
	}
	return result
}

// Map transforms slice elements
func Map[T, R any](slice []T, transform func(T) R) []R {
	result := make([]R, len(slice))
	for i, v := range slice {
		result[i] = transform(v)
	}
	return result
}
