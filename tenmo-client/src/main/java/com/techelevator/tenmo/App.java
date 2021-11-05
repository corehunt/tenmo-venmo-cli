package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TransferQueue;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private User user;
    private BigDecimal balanceAsBigD;


    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private BigDecimal viewCurrentBalance() {
		try {
			ResponseEntity<String> result = restTemplate.exchange(API_BASE_URL + "/currentBalance", HttpMethod.GET, makeAuthEntity(), String.class);
			String balance = result.getBody();
			System.out.println("Your current balance is: $" + balance);
			double parseBalance = Double.parseDouble(balance);
			balanceAsBigD = new BigDecimal(parseBalance);
		}catch(RestClientResponseException | ResourceAccessException e){
			System.out.println(e.getMessage());
		}
		return balanceAsBigD;
	}

	private void viewTransferHistory() {
		try {
			ResponseEntity<String> result = restTemplate.exchange(API_BASE_URL + "/transferHistory", HttpMethod.GET, makeAuthEntity(), String.class);
			String transferList = result.getBody();
			System.out.println(transferList);


		} catch (RestClientResponseException | ResourceAccessException e) {
			System.out.println(e.getMessage());
		}
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
	}

	private void sendBucks() {
		System.out.println("Please select from the list who you'd like to send money to (using their ID): ");
		ResponseEntity<User[]> allUsers = restTemplate.exchange(API_BASE_URL + "/listusers", HttpMethod.GET, makeAuthEntity(), User[].class);
		User[] users = allUsers.getBody();
		for(User u : users){
			System.out.println(u.getId() + " " +  u.getUsername());
		}
		// response from user for who to send to
		System.out.println("");
		int userId = console.getUserInputInteger("User ID");
		double amountFromUser = 0.00;
		while(amountFromUser == 0.00) {
			BigDecimal results = finalRequestAmt();
			amountFromUser = results.doubleValue();
		}
		BigDecimal amt2Send = new BigDecimal(amountFromUser);







//		boolean goOn = false;
//

//		viewCurrentBalance();
//		while(!goOn) {
//			if(amt2Send.compareTo(balanceAsBigD) != 1) {
//				goOn = true;
//			} else {
//				System.out.println("You are trying to send more money than is your account. Please try again!");
//				console.getUserInput("Amount to send");
//			}
//		}


		Transfer sendBucksTransfer = new Transfer();
		sendBucksTransfer.setTransferTypeId(2);
		sendBucksTransfer.setTransferStatusId(2);
		sendBucksTransfer.setAccountFrom(currentUser.getUser().getId());
		sendBucksTransfer.setAccountTo(userId);
		sendBucksTransfer.setAmount(amt2Send);


		//another http request for who to send money to. POST to send object (who to send to, and how much)
		ResponseEntity<String> result = restTemplate.exchange(API_BASE_URL + "/send", HttpMethod.POST, makeTransferEntity(sendBucksTransfer), String.class);

		
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
	private HttpEntity<Void> makeAuthEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(headers);
	}

	private HttpEntity<Transfer> makeTransferEntity(Transfer sendBucks) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(currentUser.getToken());
		return new HttpEntity<>(sendBucks, headers);
	}

	private BigDecimal finalRequestAmt() {
		double amountFromUser = console.getUserInputInteger("Amount to send");
		BigDecimal finalAmount = new BigDecimal(amountFromUser);

		viewCurrentBalance();
		if(finalAmount.compareTo(balanceAsBigD) != 1){
			return finalAmount;
		} else {
			return new BigDecimal(0);
		}

	}
}
