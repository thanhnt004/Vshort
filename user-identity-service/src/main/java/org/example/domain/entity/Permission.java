package org.example.domain.entity;

public class Permission {
    private Integer id;
    private String name;
    private String module;

    public Permission(Integer id, String name, String module) {
        this.id = id;
        this.name = name;
        this.module = module;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getModule() { return module; }
}