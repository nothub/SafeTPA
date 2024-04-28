package lol.hub.safetpa.util;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Paths {

    public static boolean isValid(String path) {
        try {
            if (Path.of(path).toString().isBlank()) return false;
        } catch (InvalidPathException ex) {
            return false;
        }
        return true;
    }

}
