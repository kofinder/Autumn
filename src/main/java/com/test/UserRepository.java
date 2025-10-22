package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autumn.beans.Repository;

@Repository
class UserRepository {
    private final Map<Integer, User> database = new HashMap<>();
    private int sequence = 1;

    public List<User> findAll() {
        return new ArrayList<>(database.values());
    }

    public User findById(int id) {
        return database.get(id);
    }

    public User save(User user) {
        if (user.getId() == 0) {
            user.setId(sequence++);
        }
        database.put(user.getId(), user);
        return user;
    }

    public boolean deleteById(int id) {
        return database.remove(id) != null;
    }

}
