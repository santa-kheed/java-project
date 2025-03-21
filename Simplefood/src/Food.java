import java.util.Scanner;

public class Food{
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        final int number_of_foodtypes = 5;
        //create food type array
        String[ ] foodTypes = new String[number_of_foodtypes];
        addFood(foodTypes, input);
        //call the method to print food
        displayFood(foodTypes);

        while(true){
            System.out.println("Enter a food to search for or (type 'exit') stop searching");

            String searchFood = input.nextLine();

            if(searchFood.equals("exit")){
                break;
            }

            String foundFood = LinearSearch(foodTypes, searchFood);

            if(foundFood != null){
                System.out.println("food found "+ foundFood);
            }
            else{
                System.out.println(searchFood+" not found");
            }
            
        }


        
    }

    public static void addFood(String[] foodTypes, Scanner input){
        System.out.println("Add any food of your choice");
        for(int i = 0; i<foodTypes.length; i++){
            System.out.println("Food type "+(i+1)+ " is ");
            foodTypes[i] = input.nextLine();
        }
    }

    public static void displayFood(String[] foodTypes){
        System.out.println("The food added are: ");
        for(String item : foodTypes){
            System.out.println(item);
        }

    }

    public static String LinearSearch(String[] foodTypes, String searchFood){
        for(String food : foodTypes){
            //equalsignore case is a metod to see the content in th food and compare without referenceing
            if(food.equalsIgnoreCase(searchFood)){
                return food;
            }
        
        }
        return null;
        

    }
}
