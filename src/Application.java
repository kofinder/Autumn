import autumn.AutumnBeanFactory;
import autumn.beans.Autowired;
import autumn.beans.AutumnApplication;
import autumn.beans.Controller;
import autumn.beans.PostConstruct;
import autumn.beans.Repository;
import autumn.beans.Service;

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

@Controller
class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        System.out.println("UserController initialized");
    }

    public void execute() {
        userService.print();
        userRepository.save();
    }
}

@AutumnApplication
class Application {
    public static void main(String[] args) {
        var factory = AutumnBeanFactory.getInstance();
        var controller = factory.getInstanceOf(UserController.class);
        controller.execute();
    }
}
