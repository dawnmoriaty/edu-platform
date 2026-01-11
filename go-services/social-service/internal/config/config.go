package config

import (
	"github.com/spf13/viper"
)

type Config struct {
	Server   ServerConfig
	Database DatabaseConfig
	GRPC     GRPCConfig
}

type ServerConfig struct {
	Port string
	Host string
}

type DatabaseConfig struct {
	Host     string
	Port     string
	User     string
	Password string
	DBName   string
}

type GRPCConfig struct {
	JavaHost string
	JavaPort string
}

func Load() (*Config, error) {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	viper.AddConfigPath("./config")
	viper.AddConfigPath("../config")

	// Set defaults
	viper.SetDefault("server.port", "8001")
	viper.SetDefault("server.host", "0.0.0.0")
	viper.SetDefault("database.host", "localhost")
	viper.SetDefault("database.port", "5432")
	viper.SetDefault("database.user", "root")
	viper.SetDefault("database.password", "123456")
	viper.SetDefault("database.dbname", "edu_crm_db")
	viper.SetDefault("grpc.java_host", "localhost")
	viper.SetDefault("grpc.java_port", "9090")

	// Read from environment
	viper.AutomaticEnv()
	viper.SetEnvPrefix("SOCIAL")

	// Read config file
	if err := viper.ReadInConfig(); err != nil {
		// Config file not found; ignore error if desired
	}

	config := &Config{
		Server: ServerConfig{
			Port: viper.GetString("server.port"),
			Host: viper.GetString("server.host"),
		},
		Database: DatabaseConfig{
			Host:     viper.GetString("database.host"),
			Port:     viper.GetString("database.port"),
			User:     viper.GetString("database.user"),
			Password: viper.GetString("database.password"),
			DBName:   viper.GetString("database.dbname"),
		},
		GRPC: GRPCConfig{
			JavaHost: viper.GetString("grpc.java_host"),
			JavaPort: viper.GetString("grpc.java_port"),
		},
	}

	return config, nil
}
