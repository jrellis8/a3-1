import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 */
class SockClient {
  static Socket sock = null;
  static String host = "localhost";
  static int port = 8888;
  static OutputStream out;
  // Using and Object Stream here and a Data Stream as return. Could both be the same type I just wanted
  // to show the difference. Do not change these types.
  static ObjectOutputStream os;
  static DataInputStream in;
  public static void main (String args[]) {

    if (args.length != 2) {
      System.out.println("Expected arguments: <host(String)> <port(int)>");
      System.exit(1);
    }

    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port|sleepDelay] must be an integer");
      System.exit(2);
    }

    try {
      connect(host, port); // connecting to server
      System.out.println("Client connected to server.");
      boolean requesting = true;
      while (requesting) {
        System.out.println("What would you like to do: 1 - echo, 2 - add, 3 - addmany, 4 - charcount, 5 - storyboard (0 to quit)");
        Scanner scanner = new Scanner(System.in);
        int choice = Integer.parseInt(scanner.nextLine());
        // You can assume the user put in a correct input, you do not need to handle errors here
        // You can assume the user inputs a String when asked and an int when asked. So you do not have to handle user input checking
        JSONObject json = new JSONObject(); // request object
        switch(choice) {
          case 0:
            System.out.println("Choose quit. Thank you for using our services. Goodbye!");
            requesting = false;
            break;
          case 1:
            System.out.println("Choose echo, which String do you want to send?");
            String message = scanner.nextLine();
            json.put("type", "echo");
            json.put("data", message);
            break;
          case 2:
            System.out.println("Choose add, enter first number:");
            String num1 = scanner.nextLine();
            json.put("type", "add");
            json.put("num1", num1);

            System.out.println("Enter second number:");
            String num2 = scanner.nextLine();
            json.put("num2", num2);
            break;
          case 3:
            System.out.println("Choose addmany, enter as many numbers as you like, when done choose 0:");
            JSONArray array = new JSONArray();
            String num = "1";
            while (!num.equals("0")) {
              num = scanner.nextLine();
              array.put(num);
              System.out.println("Got your " + num);
            }
            json.put("type", "addmany");
            json.put("nums", array);
            break;
          case 4:
            System.out.println("Choose charcount, enter a string to count characters,");
            System.out.println("if you are looking for a specific character, please specify.\n");

            boolean findchar = false;           // value is false to denote general character counting
            char find = ' ';                    // if findchar is true -- character in String to search for e.g. "s"
            
            // determine whether the user is looking for a specific value
            char specific = 's';
            while (specific != 'y' && specific != 'n') {

              System.out.println("Are you looking for a specific character? y/n");
              specific = scanner.next().charAt(0);

              if (specific == 'y') { 
                findchar = true; 
                System.out.println("What specific character are you looking for?");
                find = scanner.next().charAt(0);
                scanner.nextLine();
                System.out.println("Please enter your string.");
                String count = scanner.nextLine();
                json.put("type", "charcount");
                json.put("findchar", true);
                json.put("find", find);
                json.put("count", count);
              }

              if (specific == 'n') { 
                findchar = false; 
                System.out.println("Please enter your string.");
                String count = scanner.nextLine();
                json.put("type", "charcount");
                json.put("findchar", false);
                json.put("count", count);
              }

              if (specific != 'y' && specific != 'n') {
                System.out.println("Error: Please enter a lowercase y or n");
              }
            }
            os.writeObject(json.toString());
            os.flush();
            break;
          case 5:
            System.out.println("Choose storyboard. Please choose from the following options;");
            System.out.println("1 - Add to storyboard, 2 - View storyboard (0 to quit)");

            int option = Integer.parseInt(scanner.nextLine());
            JSONObject req = new JSONObject();
            req.put("type", "storyboard");

            if (option == 1) {
              req.put("view", false);
              System.out.println("Enter Username: ");
              String name = scanner.nextLine();
              req.put("name", name);
              System.out.println("Enter your sentence to add to the storyboard: ");
              String story = scanner.nextLine();
              req.put("story", story);
            } else if (option == 2) {
              req.put("view", true);
            } else {
              System.out.println("Error, please try again.");
              break;
            }

            os.writeObject(req.toString());
            os.flush();

            String response = in.readUTF();
            JSONObject res = new JSONObject(response);

            if (res.getBoolean("ok")) {
              if (res.getBoolean("view")) {
                JSONArray storyboard = res.getJSONArray("storyboard");
                JSONArray users = res.getJSONArray("users");
                System.out.println("Storyboard: ");
                for (int i = 0; i < storyboard.length(); i++) {
                  System.out.println(users.getString(i) + ": " + storyboard.getString(i));
                }
              } else {
                System.out.println("Your sentence has been added!");
              }  
            } else {
              System.out.println("Error: " + res.getString("message"));    
            }
            break;
        }
        if(!requesting) {
          continue;
        }

        // write the whole message
        os.writeObject(json.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        // handle the response
        // - not doing anything other than printing payload
        // !! you will most likely need to parse the response for the other 2 services!
        String i = (String) in.readUTF();
        JSONObject res = new JSONObject(i);
        System.out.println("Got response: " + res);
        if (res.getBoolean("ok")){
          if (res.getString("type").equals("echo")) {
            System.out.println(res.getString("echo"));
          } else if (res.getString("type").equals("add") || res.getString("type").equals("charcount")) {
            System.out.println(res.getInt("result"));
          } else if (res.getString("type").equals("charcount")) {
            System.out.println(res.getInt("count"));
          } else {
            System.out.println(res.getInt("Error. Please try again."));
          }
        } else {
          System.out.println(res.getString("message"));
        }
      }
      // want to keep requesting services so don't close connection
      //overandout();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void overandout() throws IOException {
    //closing things, could
    in.close();
    os.close();
    sock.close(); // close socked after sending
  }

  public static void connect(String host, int port) throws IOException {
    // open the connection
    sock = new Socket(host, port); // connect to host and socket on port 8888

    // get output channel
    out = sock.getOutputStream();

    // create an object output writer (Java only)
    os = new ObjectOutputStream(out);

    in = new DataInputStream(sock.getInputStream());
  }
}
