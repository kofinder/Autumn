package com.test;

import java.util.List;
import java.util.stream.Collectors;

import com.autumn.beans.Autowired;
import com.autumn.beans.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(int id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    /** Update an existing user */
    public User update(int id, User user) {
        User existing = userRepository.findById(id);
        if (existing != null) {
            existing.setName(user.getName());
            existing.setEmail(user.getEmail());
            return userRepository.save(existing);
        }
        return null;
    }

    /** Partially update user fields (PATCH) */
    public User partialUpdate(int id, User user) {
        User existing = userRepository.findById(id);
        if (existing != null) {
            if (user.getName() != null && !user.getName().isEmpty()) {
                existing.setName(user.getName());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                existing.setEmail(user.getEmail());
            }
            return userRepository.save(existing);
        }
        return null;
    }

    public boolean delete(int id) {
        return userRepository.deleteById(id);
    }

    public List<User> findByName(String name) {
        return userRepository.findAll().stream()
                .filter(u -> u.getName() != null && u.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }
}
