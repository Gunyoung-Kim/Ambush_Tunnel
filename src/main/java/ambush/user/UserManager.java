package ambush.user;

import java.util.HashSet;

public class UserManager {
	
	private static UserManager instance;
	
	private final HashSet<String> users = new HashSet<>();
	
	private UserManager() {
		
	}
	
	public UserManager getInstance() {
		if(instance == null) {
			synchronized(UserManager.class) {
				if(instance== null)
					instance = new UserManager();
			}
		}
		return instance;
	}
	
	public boolean addUser(String name) {
		if(users.contains(name)) {
			return false;
		} else {
			users.add(name);
			return true;
		}
	}
	
	public void removeUser(String name) {
		users.remove(name);
	}
}
