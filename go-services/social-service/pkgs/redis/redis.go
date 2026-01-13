package redis

import (
	"context"
	"encoding/json"
	"time"

	"github.com/redis/go-redis/v9"
)

type IRedis interface {
	Get(ctx context.Context, key string, value any) error
	Set(ctx context.Context, key string, value any) error
	SetWithExpiration(ctx context.Context, key string, value any, expiration time.Duration) error
	Remove(ctx context.Context, keys ...string) error
	Keys(ctx context.Context, pattern string) ([]string, error)
	RemovePattern(ctx context.Context, pattern string) error
	Exists(ctx context.Context, key string) (bool, error)
}

type Config struct {
	Address  string
	Password string
	Database int
}

type redisClient struct {
	client *redis.Client
}

func NewRedis(cfg Config) (IRedis, error) {
	client := redis.NewClient(&redis.Options{
		Addr:     cfg.Address,
		Password: cfg.Password,
		DB:       cfg.Database,
	})

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Ping(ctx).Err(); err != nil {
		return nil, err
	}

	return &redisClient{client: client}, nil
}

func (r *redisClient) Get(ctx context.Context, key string, value any) error {
	val, err := r.client.Get(ctx, key).Bytes()
	if err != nil {
		return err
	}
	return json.Unmarshal(val, value)
}

func (r *redisClient) Set(ctx context.Context, key string, value any) error {
	data, err := json.Marshal(value)
	if err != nil {
		return err
	}
	return r.client.Set(ctx, key, data, 0).Err()
}

func (r *redisClient) SetWithExpiration(ctx context.Context, key string, value any, expiration time.Duration) error {
	data, err := json.Marshal(value)
	if err != nil {
		return err
	}
	return r.client.Set(ctx, key, data, expiration).Err()
}

func (r *redisClient) Remove(ctx context.Context, keys ...string) error {
	return r.client.Del(ctx, keys...).Err()
}

func (r *redisClient) Keys(ctx context.Context, pattern string) ([]string, error) {
	return r.client.Keys(ctx, pattern).Result()
}

func (r *redisClient) RemovePattern(ctx context.Context, pattern string) error {
	keys, err := r.Keys(ctx, pattern)
	if err != nil {
		return err
	}
	if len(keys) > 0 {
		return r.Remove(ctx, keys...)
	}
	return nil
}

func (r *redisClient) Exists(ctx context.Context, key string) (bool, error) {
	result, err := r.client.Exists(ctx, key).Result()
	if err != nil {
		return false, err
	}
	return result > 0, nil
}
