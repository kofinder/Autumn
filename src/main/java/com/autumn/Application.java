package com.autumn;

import com.autumn.beans.Autowired;
import com.autumn.beans.AutumnApplication;
import com.autumn.beans.PostConstruct;
import com.autumn.beans.Repository;
import com.autumn.beans.Service;

import java.io.IOException;

import com.autumn.web.MiniDispatcher;
import com.autumn.web.converter.JsonMessageConverter;
import com.autumn.web.mapping.GetMapping;
import com.autumn.web.mapping.RestController;

@Service
class UserService {
    public void print() {
        System.out.println("UserService running");
    }
}

@Repository
class UserRepository {
    public void save() {
        System.out.println("UserRepository saving");
    }
}

@RestController
class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        System.out.println("UserController initialized");
    }

    @GetMapping("/api/users")
    public String execute() {
        userService.print();
        userRepository.save();

        return "Hello world";
    }
}

@AutumnApplication
public class Application {
    public static void main(String[] args) {
        AutumnApplicationRunner.run(Application.class);
        var dispatcher = new MiniDispatcher();
        dispatcher.registerController(UserController.class);
        dispatcher.addConverter(new JsonMessageConverter());

        try {
            dispatcher.start(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
