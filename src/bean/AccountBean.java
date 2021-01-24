package bean;

import java.security.NoSuchAlgorithmException;

public class AccountBean {
	private UserBean user;
	private AddressBean address;

	public AccountBean(UserBean user, AddressBean address) throws NoSuchAlgorithmException {
		super();
		this.user = user;
		this.address = address;
	}

	public UserBean getUser() {
		return user;
	}

	public void setUser(UserBean user) {
		this.user = user;
	}

	public AddressBean getAddress() {
		return address;
	}

	public void setAddress(AddressBean address) {
		this.address = address;
	}
}
