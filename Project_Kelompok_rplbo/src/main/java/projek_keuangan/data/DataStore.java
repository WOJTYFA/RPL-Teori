package projek_keuangan.data;

import projek_keuangan.item.keuanganItem;
import projek_keuangan.item.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    private static List<User> users = new ArrayList<>();
    private static Map<String, List<keuanganItem>> userTodos = new HashMap<>();

    public static boolean addUser(User user) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) return false;
        users.add(user);
        userTodos.put(user.getUsername(), new ArrayList<>());
        return true;
    }

    public static User findUser(String username, String password) {
        return users.stream().filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }

    public static List<keuanganItem> getTodos(String username) {
        return userTodos.get(username);
    }

    public static void addTodo(String username, keuanganItem item) {
        userTodos.get(username).add(item);
    }

    public static void removeTodo(String username, keuanganItem item) {
        userTodos.get(username).remove(item);
    }

    public static void editTodo(String username, keuanganItem oldItem, keuanganItem newItem) {
        int idx = userTodos.get(username).indexOf(oldItem);
        if (idx >= 0) userTodos.get(username).set(idx, newItem);
    }
}


