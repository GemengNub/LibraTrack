package school.librarylogging;

public class User{
        public String name, role = "User";
        public int id;

    public User() {}

    public User(String name, String role, int id) {
        this.name = name;
        this.role = role;
        this.id = id;
    }

    private String assignRole(String username){
        return switch(username){
            case "admin" -> "ADMINISTRATOR";
            case "Lib1" -> "LIBRARIAN";
            default -> "NONE";
        };
    }

    public String getRole() {
        role = assignRole(name);
        return role;
    }
}
