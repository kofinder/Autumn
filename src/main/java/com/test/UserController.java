package com.test;

import com.autumn.beans.Autowired;
import com.autumn.beans.DeleteMapping;
import com.autumn.beans.GetMapping;
import com.autumn.beans.PatchMapping;
import com.autumn.beans.PathVariable;
import com.autumn.beans.PostMapping;
import com.autumn.beans.PutMapping;
import com.autumn.beans.RequestBody;
import com.autumn.beans.RequestParam;
import com.autumn.beans.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/api/users")
    public List<User> getAll(@RequestParam(value = "name", required = false) String name) {
        if (name != null) {
            return userService.findByName(name);
        }
        return userService.getAll();
    }

    @GetMapping("/api/users/{id}")
    public User getById(@PathVariable("id") int id) {
        return userService.getById(id);
    }

    @PostMapping("/api/users")
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/api/users/{id}")
    public User update(@PathVariable("id") int id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/api/users/{id}")
    public String delete(@PathVariable("id") int id) {
        return userService.delete(id) ? "Deleted" : "Not found";
    }

    @PatchMapping("/api/users/{id}")
    public User partialUpdate(@PathVariable("id") int id, @RequestBody User user) {
        return userService.partialUpdate(id, user);
    }
}
