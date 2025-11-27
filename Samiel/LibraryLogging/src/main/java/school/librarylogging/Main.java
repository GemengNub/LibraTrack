package school.librarylogging;
// Backend for Automated Library Books Logging through QR Codes

// QR CODE READER/WRITER IMPORT
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

// IO SYSTEMS IMPORT
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    protected static final User user = new User();
    public static QrCode qrCode = new QrCode();
    private static final Scanner in = new Scanner(System.in);

    static void loginHandler(){
        /*
         * Method for handling login logic (Console-based - legacy)
         * */
        String forbiddenChars = "^[^<>/\\\\^();:'\"\\[\\]\\{\\}\\|]+$";
        String userInput = "", passwordInput = "";

        try{
            System.out.println("What is your username?");
            userInput = in.nextLine();
            System.out.println("What is your password?");
            passwordInput = in.nextLine();
            if(!userInput.matches(forbiddenChars) || !passwordInput.matches(forbiddenChars)){
                throw new InputMismatchException("Cannot contain special characters");
            }
        }
        catch(InputMismatchException e){
            System.out.println(e.getMessage());
            loginHandler();
        }

        if (Login.isLoggedIn(userInput, passwordInput)) {
            System.out.println("Welcome " + user.name);
        } else {
            System.out.println("You are not authorized!");
            loginHandler();
        }
    }

    static class Login {

        private static int foundId;
        public static boolean isLoggedIn(String username, String password) {
            /*
             * Found ID Logic:
             * 1. Search database for corresponding username
             * IF: Found -> foundId = ID
             * -> Display ID
             * ELSE: foundId = -1 or NULL
             * -> Display that user is not found
             * ALL FOUND ID'S SHOULD BE FROM DATABASE !!!! (need fixing lol)*/
            if (username.equals("admin") && password.equals("AdminAdmin_123")) {
                user.name = username;
                user.id = foundId;
                user.role = user.getRole();
                return true;
            }
            else if(/*  Click Event + Input */username.equals("Lib1") && password.equals("Librarian_123")){
                user.name = username;
                user.id = foundId;
                user.role = user.getRole();
                return true;
            }
            else if(/*Clicked on*/ username.equalsIgnoreCase("user")){
                user.name = username;
                user.id = foundId;
                user.role = user.getRole();
                return true;
            }
            else {
                return false;
            }
        }

        public static String userDetails(){ // Method to return user details retrieved from Database

            /*  return User.name + " " + User.role + " " + User.id;
             *   OR
             *   User.role = role in the database
             *   then return the info
             * */
            //return null;
            // Console logging purposes nlng sguro to? haha
            return user.name + " " + user.role + " " + user.id;
        }
    }

    public static void main(String[] args) {
        /*
        *
        * Start of program flow
        *
        * Launch Java Swing Login Form as the entry point
        * */
        LoginForm.showLoginForm();
        DatabaseHandler.main(args);
    }
}