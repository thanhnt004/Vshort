package org.example.domain.entity;

import java.util.Set;

public class Role {
    private Integer id;
    private String name;
    private String description;

    // Abstract hóa bảng trung gian role_permissions
    private Set<Permission> permissions;

    public Role(Integer id, String name, String description, Set<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Set<Permission> getPermissions() { return permissions; }
}