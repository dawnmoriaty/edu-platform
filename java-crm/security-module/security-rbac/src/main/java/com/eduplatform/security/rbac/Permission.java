package com.eduplatform.security.rbac;

public class Permission {
    private Long id;
    private String name;
    private String resource;
    private String action;

    public Permission() {
    }

    public Permission(Long id, String name, String resource, String action) {
        this.id = id;
        this.name = name;
        this.resource = resource;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFullName() {
        return resource + ":" + action;
    }
}
