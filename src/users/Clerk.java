package users;

import java.util.Collections;
import java.util.Scanner;

import program.Main;
import program.HelperFunctions;

import program.Data.UserType;
import program.Data.Category;
import program.Data.Item;

public class Clerk extends User{

	private Scanner input = Main.input;
	
	public Clerk(String username, String password, String name, String lastName, UserType type) {
		super(username, password, name, lastName, type);
	}
	
	@Override
	public void userMenu() {
		//Call logIn function
		logIn();
		//List of menu options to display to the clerk
		String[] options = {"View categories", "Create new category", "Delete existing category", "List items", "Categorize items", "Queue items for shipment", "Take delivery", "Logout"};
		
		//Clerk choice
		int choice = 0;
		do {
			System.out.println("----------CLERK MENU----------");
			choice = HelperFunctions.displayMenu(options);
			switch(choice) {
			case 1:
				listCategories();
				break;
			case 2:
				createCategory();
				break;
			case 3:
				deleteCategory();
				break;
			case 4:
				displayItems();
				break;
			case 5:
				categorizeItems();
				break;
			case 6: 
				queueItemsForShipment();
				break;
			case 7:
				takeDelivery();
				break;
			case 8:
				logOut();
				break;
			}
		}while(choice != 8);
		
	}
	
	@Override
	public void logIn() {
		//Log Event
		Main.logEvent("clerk login: " + username);
	}

	@Override
	public void logOut() {
		//Log Event
		Main.logEvent("clerk logout: " + username);
	}
	
	private void listCategories() {
		System.out.println("----------LIST CATEGORIES----------");
		//Check if we have categories to display
		if(Main.categories.size() == 0) {
			//If we don't have categories, display a message
			System.out.println("There are no categories to show...");
		}
		//Go through each category and print ID + Name
		for(Category category : Main.categories) {
			System.out.println(category.toString());
		}
		//Print an empty line and pause the program
		System.out.println("");
		HelperFunctions.pause();
	}
	
	private void createCategory() {
		System.out.println("----------CREATE CATEGORY----------");
		//Get last category ID
		int lastID;
		try {
			lastID = Main.categories.get(Main.categories.size()-1).id;
		}catch(Exception e) {
			lastID = 0;
		}
		//Ask the Clerk to enter a category name
		String categoryName;
		System.out.println("Enter category name: ");
		
		int id;
		do {
			//get clerk input and check if category does not exist
			categoryName = input.next();
			if(categoryName.equals("cancel")) {
				return;
			}
			id = HelperFunctions.findCategoryByName(Main.categories, categoryName);
			if(id != 0) {
				//If we found a category with the same name, ask the clerk to try again
				System.out.println("Category already exists, try again: ");
			}
		}while(id != 0);
		
		//Once the category is created successfully, add it to the list of categories and write to file
		Main.categories.add(new Category(lastID + 1, categoryName));
		Main.writeCategoriesToFile();
		//Log event
		Main.logEvent(username + " created category: " + categoryName);
		
		//Display a message
		System.out.println("Category created successfully!");
		HelperFunctions.pause();
	}
	
	private void deleteCategory() {
		System.out.println("----------DELETE CATEGORY----------");
		//if we don't have categories, then we don't have anything to delete
		if(Main.categories.size() == 0) {
			System.out.println("There are no categories to delete!");
			HelperFunctions.pause();
			return;
		}
		//If we have categories, ask the clerk for input
		int id;
		String categoryName;
		System.out.println("Enter category name: ");
		do {
			categoryName = input.next();
			if(categoryName.equals("cancel")) {
				return;
			}
			id = HelperFunctions.findCategoryByName(Main.categories, categoryName);
			//check if we have the category, if not, display a message
			if(id == 0) {
				System.out.println("Category does not exist, try again: ");
			}
		}while( id == 0);
		
		//Once we found the category, delete it
		Main.categories.remove(id-1);
		//Remove category from items
		updateItemCategories(id, 0);
		
		
		//Go through each category
		for(Category category : Main.categories) {
			if(category.id > id) {
				//if category id is bigger than the category we removed, lower the category id by one
				int oldID = category.id;
				category.id -= 1;
				//update item categories
				updateItemCategories(oldID, category.id);
			}
		}
		
		
		//write categories and items to file
		Main.writeCategoriesToFile();
		Main.writeItemToFile();
		
		//Log Event
		Main.logEvent(username + " removed category: " + categoryName);
		
		//Display a message
		System.out.println("Category successfully removed!");
		HelperFunctions.pause();
		
	}
	
	private void displayItems() {
		System.out.println("----------DISPLAY ITEMS----------");
		
		//Check if we have any items
		if(Main.items.size() == 0) {
			//If there are no items, display a message
			System.out.println("There are no items to display");
			HelperFunctions.pause();
		}
		
		for(Category c : Main.categories) {
			System.out.println(c.name + ": ");
			for(Item item : Main.items) {
				if(item.category == c.id) {
					item.calculateTotalMass();
					System.out.println("  " + item.toString());
				}
			}
		}
		
		System.out.println("Uncategorized: ");
		for(Item i : Main.items) {
			if(i.category == 0) {
				System.out.println(i.toString());
			}
		}
		
		System.out.println("");
		HelperFunctions.pause();
	}
	
	private void categorizeItems() {
		System.out.println("----------SORT ITEMS----------");
		
		//Check if we have items
		if(Main.items.size() == 0) {
			//If we don't, display a message
			System.out.println("There are no items to sort");
			HelperFunctions.pause();
			return;
		}
		//number of items we categorized
		int numberOfItems = 0;
		
		//Check if we have categories
		if(Main.categories.size() == 0) {
			//If we don't, display a message
			System.out.println("There are no categories to select from");
			HelperFunctions.pause();
			return;
		}
		
		//If we have items and categories, we can sort items
		
		//create option for each category
		String[] options = new String[Main.categories.size()+1];
		for(int i=0; i<=Main.categories.size(); i++) {
			if(i != Main.categories.size()) {
				options[i] = Main.categories.get(i).name;
			}else if(i == Main.categories.size()) {
				options[i] = "Cancel Categorization";
			}
		}
		
		//Go through each item
		for(Item item : Main.items) {
			if(item.category != 0) {
				//If item has a category, skip it
				continue;
			}
			int choice = HelperFunctions.displayMenu(options, "Select category for: " + item.name);
			//If we did not choose the last option (cancel)
			
			if(choice == options.length) {
				return;
			}
			item.category = choice;
			numberOfItems++;
		}
		
		if(numberOfItems == 0) {
			System.out.println("There are no items to categorize");
		}
		//Save items to file
		Main.writeItemToFile();
		//Log Event
		Main.logEvent(username + " categorized " + numberOfItems + " items");
		
	}
	
	private void queueItemsForShipment() {
		
		int numberOfItems = 0;
		
		while(true) {
			//Check if we have any items
			if(Main.items.size() == 0) {
				//If we don't have items, display a message
				System.out.println("There are no items in invetory");
				HelperFunctions.pause();
				return;
			}
			//Display a message to the clerk
			System.out.println("Enter item name: (enter \"stop\" to finish)");
			//Get input
			String itemName = input.next();
			//If the input is "stop", exit the function
			if(itemName.equals("stop")) {
				break;
			}
			//find the item in item list
			int itemID = HelperFunctions.findItemByName(Main.items, itemName);
			Item item = null;
			if(itemID != -1) {
				item = Main.items.get(itemID);
			}
			if(item != null) {
				//Ask the clerk to enter the quantity of items
				System.out.println("Enter quantity: ");
				int quantity = 0;
				
				//Check if quantity is a valid input and in range
				do {
					try {
						quantity = input.nextInt();
					} catch (Exception e) {
						System.out.println("Invalid input, try again: ");
						input.next();
						quantity = -1;
					}
					
					if(quantity > item.numberOfUnits || quantity <= 0) {
						System.out.println("quantity out or range, try again: ");
						continue;
					}
				} while (quantity == -1 || quantity > item.numberOfUnits || quantity <= 0);
				
				//Reduce the item
				item.numberOfUnits -= quantity;
				//Create a copy of the item, but with quantity we chose
				//If we don't have this item in the system anymore, remove it
				if(item.numberOfUnits == 0) {
					Main.items.remove(item);
				}
				//Add the item to the queue
				int outboundIndex = HelperFunctions.findItemByName(Main.outboundItems, item.name);
				if(outboundIndex == -1) {
					Item queueItem = new Item(item.name, item.massPerUnit, quantity, item.category);
					Main.outboundItems.add(queueItem);
				}else {
					Main.outboundItems.get(outboundIndex).numberOfUnits += quantity;
				}
				
				//Count how many items we removed
				numberOfItems += quantity;
				
			}
		}
		//Write items to file
		Main.writeItemToFile();
		//Log Event
		Main.logEvent(username + " queued " + numberOfItems + " items for shipment");
		//Display a message
		System.out.println("Successfully queued " + numberOfItems + " items for shipment");
		HelperFunctions.pause();
	}
	
	private void takeDelivery() {
		
		//Check if we have any inbound items
		if(Main.inboundItems.size() == 0) {
			//If we don't, then we don't have any deliveries and stop the function
			System.out.println("There are no deliveries");
			HelperFunctions.pause();
			return;
		}
		
		//Display all items on delivery
		System.out.println("Delivered items: ");
		for(Item item : Main.inboundItems) {
			System.out.println(item.numberOfUnits + "x " + item.name);
		}
		
		//Ask the clerk if they want to take delivery
		System.out.println("do you want to take delivery of these items? (Y - Yes, N - No)");
		String choice;
		do {
			choice = input.next().toUpperCase();
			if(!choice.equals("Y") && !choice.equals("N")) {
				//If the clerk did not enter Y or N, display a message
				System.out.println("Invalid input, try again: ");
			}
		}while(!choice.equals("Y") && !choice.equals("N"));
		
		//If the clerk took delivery, add items to main list
		if(choice.equals("Y")) {
			//count how many items were added
			int numberOfItems = 0;
			//Go through each item in inbound queue
			for(Item item : Main.inboundItems) {
				//count the item
				numberOfItems += item.numberOfUnits;
				//check if we have that item already in the systems
				int id = HelperFunctions.findItemByName(Main.items, item.name);
				if( id != -1) {
					//if we do, just add the units
					Main.items.get(id).numberOfUnits += item.numberOfUnits;
				}else {
					//else, add the item to the list
					Main.items.add(item);
				}
			}
			//Once done, clear inbound queue
			Main.inboundItems.clear();
			//Sort the items alphabetically
			Collections.sort(Main.items);
			//write items to file
			Main.writeItemToFile();
			Main.logEvent(username + " took delivery of: " + numberOfItems + " items");
			//Display a message
			System.out.println("Successfully took delivery!");
			HelperFunctions.pause();
		}
	}
	
	private void updateItemCategories(int oldCategory, int newCategory) {
		//Go through each item
		for(Item item : Main.items) {
			//if item category matches oldCategory, set item category to newCategory
			if(item.category == oldCategory) {
				item.category = newCategory;
			}
		}
	}

	
}
