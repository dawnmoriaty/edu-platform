package di

import (
	"github.com/eduplatform/go-services/social-service/internal/api"
	"github.com/eduplatform/go-services/social-service/internal/api/handlers"
	"github.com/eduplatform/go-services/social-service/internal/application"
	"github.com/eduplatform/go-services/social-service/internal/config"
	"github.com/eduplatform/go-services/social-service/internal/infrastructure/database"
	"github.com/eduplatform/go-services/social-service/internal/infrastructure/repository"
	"github.com/gin-gonic/gin"
	"go.uber.org/dig"
)

// Container wraps dig.Container with helper methods
type Container struct {
	*dig.Container
}

// New creates a new DI container with all dependencies registered
func New(cfg *config.Config) (*Container, error) {
	c := &Container{dig.New()}

	// Register all modules in order
	modules := []func(*Container, *config.Config) error{
		registerConfig,
		registerInfrastructure,
		registerRepositories,
		registerServices,
		registerHandlers,
		registerRouter,
	}

	for _, module := range modules {
		if err := module(c, cfg); err != nil {
			return nil, err
		}
	}

	return c, nil
}

// ProvideAll registers multiple constructors at once
func (c *Container) ProvideAll(constructors ...interface{}) error {
	for _, constructor := range constructors {
		if err := c.Provide(constructor); err != nil {
			return err
		}
	}
	return nil
}

// registerConfig registers configuration
func registerConfig(c *Container, cfg *config.Config) error {
	return c.Provide(func() *config.Config { return cfg })
}

// registerInfrastructure registers database and other infra
func registerInfrastructure(c *Container, _ *config.Config) error {
	return c.Provide(database.NewConnection)
}

// registerRepositories registers all repositories
func registerRepositories(c *Container, _ *config.Config) error {
	return c.ProvideAll(
		repository.NewPostRepository,
		repository.NewCommentRepository,
		repository.NewLikeRepository,
		repository.NewFollowRepository,
	)
}

// registerServices registers all application services
func registerServices(c *Container, _ *config.Config) error {
	return c.ProvideAll(
		application.NewPostService,
		application.NewCommentService,
		application.NewLikeService,
		application.NewFollowService,
	)
}

// registerHandlers registers all HTTP handlers
func registerHandlers(c *Container, _ *config.Config) error {
	return c.ProvideAll(
		handlers.NewPostHandler,
		handlers.NewCommentHandler,
		handlers.NewLikeHandler,
		handlers.NewFollowHandler,
	)
}

// registerRouter registers the gin router
func registerRouter(c *Container, _ *config.Config) error {
	return c.Provide(func() *gin.Engine {
		return gin.Default()
	})
}

// SetupRoutes configures all routes with injected handlers
func (c *Container) SetupRoutes() error {
	return c.Invoke(func(
		router *gin.Engine,
		postHandler *handlers.PostHandler,
		commentHandler *handlers.CommentHandler,
		likeHandler *handlers.LikeHandler,
		followHandler *handlers.FollowHandler,
	) {
		api.SetupRoutes(router, postHandler, commentHandler, likeHandler, followHandler)
	})
}

// GetRouter returns the configured gin.Engine
func (c *Container) GetRouter() (*gin.Engine, error) {
	var router *gin.Engine
	err := c.Invoke(func(r *gin.Engine) {
		router = r
	})
	return router, err
}
