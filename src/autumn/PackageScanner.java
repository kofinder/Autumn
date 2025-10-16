package autumn;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PackageScanner {

    public static Set<Class<?>> getClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = packageName.replace('.', '/');
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource == null)
                return classes;

            File directory = new File(resource.toURI());
            if (!directory.exists())
                return classes;

            for (File file : directory.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    classes.add(Class.forName(className));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
        return classes;
    }
}
