package com.crs.fake;

import com.crs.dao.AdminDAO;
import com.crs.model.Admin;
import java.util.Optional;

public class InMemoryAdminDAO implements AdminDAO {
    private final FakeDataStore db;
    public InMemoryAdminDAO(FakeDataStore db) { this.db = db; }

    @Override public Optional<Admin> findById(String id) { return Optional.ofNullable(db.admins.get(id)); }
}
