package dk.kvalitetsit.fut.dao;

import dk.kvalitetsit.fut.dao.entity.HelloEntity;

import java.util.List;

public interface HelloDao {
    void insert(HelloEntity helloEntity);

    List<HelloEntity> findAll();
}