import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Scanner;

public class encodeST {

    public static String ENDING_MESSAGE = "&*09^%$__"; // super arbitrary BUT NEEDS TO MATCH. Just marks the end of the
                                                       // message
                                                       // could and should probably also use a header at the beginning
                                                       // of the message to indicate size, but this is good enough for
                                                       // now

    public static void main(String[] args) {
        try {
            // get the file name from the user
            System.out.println("Enter the name of the image file you want to encode: ");
            Scanner userInput = new Scanner(System.in);
            String fileName = userInput.nextLine();

            // use image path as FileInputStream argument
            InputStream inputFile = new FileInputStream(fileName);
            BufferedImage imageFile = ImageIO.read(inputFile);
            
            //each pixel can hold up to 24 bits divided by 8 to return the number of bytes equivalent to the characters
            int maxLength = ((24 * imageFile.getHeight() * imageFile.getWidth()) / 8) - ENDING_MESSAGE.length();
            
            System.out.println("Maximum message length = " + (maxLength) + " characters.");

            // Get the text file name from the user ****
            System.out.println("Enter the name of the text file containing the message: ");
            String textFileName = userInput.nextLine();
            userInput.close();

            // Read the message from the text file
            String message = readMessageFromFile(textFileName);

            // checks if message is too long for image
            if (message.length() + ENDING_MESSAGE.length() > maxLength) {
                System.out.println("Message too long!");
                System.exit(1);
            }

            System.out.println("Message being encoded = " + message);

            // extracts the file name excluding the extension while fileType stores the extension
            String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
            fileName = fileName.substring(0, fileName.indexOf("."));
            

            // encode the message into new file with ending, then writes it to new file
            ImageIO.write(encodeMessage(imageFile, message + ENDING_MESSAGE), fileType, new File(fileName + "ENCODED." + fileType));
            System.out.println("Message encoded into image file: " + fileName + "ENCODED." + fileType);
        }

        catch (IOException e) {
            System.out.println("Image not found");
        }
    }

    // Read the message from a text file
    private static String readMessageFromFile(String fileName) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                messageBuilder.append(line).append(System.lineSeparator()); // Append line breaks
            }
        }
        return messageBuilder.toString().trim(); // Trim to remove any trailing line breaks
    }

    // given a byte and the number we want the LSB to become, we encode a 0 or 1
    // based on newValue
    // lsbIndex is used to access different orders of LSBs
    public static int encodeBit(int number, int newValue, int lsbIndex) {
        if (newValue == 0) {
            number = (number & ~(lsbIndex)); // bitwise operator that 0's out the last bit of the number
        } else if (newValue == 1) {
            number = (number & ~(lsbIndex)) | lsbIndex; // bitwise operator that 0's out last bit then makes it 1
        } else {
            System.exit(0);
        } // error

        return number;
    }

    public static BufferedImage encodeMessage(BufferedImage image, String message) {
        int pixelValue = 0;
        int red, green, blue;

        int height = image.getHeight();
        int width = image.getWidth();

        // converts message to bits
        int[] messageBits = messageToBits(message);
        
        // create new ARGB buffered image (RGB for BMPs)
        int type = image.getType();
        if(type == 0){
            type = BufferedImage.TYPE_INT_RGB;
        }
        BufferedImage output = new BufferedImage(width, height, type);
        
        //used to access different orders of LSBs
        int lsbIndex = 1;
        int count = 0;
        
        //keep looping through output image until all of the message has been encoded
        //each loop encodes one bit to the left of the last LSB (LSB, then 2nd most LSB, then 3rd most LSB, etc.)
        while(count < messageBits.length) {
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	            	//get pixel value from original image if all of the LSBs of the original have not been visited
	            	if(lsbIndex == 1) {
	            		pixelValue = image.getRGB(x, y);
	            	}
	            	//access other orders of LSBs if all most LSBs have been accessed
	            	else {
	            		pixelValue = output.getRGB(x, y);
	            	}
	                //alpha = (pixelValue >> 24) & 0xFF; // bitwise operation moves necessary value down to
	                red = (pixelValue >> 16) & 0xFF; // last 8 bits, then ANDS it with 0xFF (11111111)
	                green = (pixelValue >> 8) & 0xFF; // to only select the last 8 bits
	                blue = pixelValue & 0xFF;
	
	                if (count < messageBits.length) {
	                    // Encode the next three bits into red, green, and blue channels
	                    if (count < messageBits.length) {
	                        red = encodeBit(red, messageBits[count++], lsbIndex); // Red LSB
	                    }
	                    if (count < messageBits.length) {
	                        green = encodeBit(green, messageBits[count++], lsbIndex); // Green LSB
	                    }
	                    if (count < messageBits.length) {
	                        blue = encodeBit(blue, messageBits[count++], lsbIndex); // Blue LSB
	                    }
	
	                    //int newPixelValue = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8)
	                            //| (blue & 0xFF);
	                    int newPixelValue = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
	                    output.setRGB(x, y, newPixelValue);
	                } else {
	                    output.setRGB(x, y, pixelValue);
	                }
	            }
	        }
	        //double lsbIndex
            lsbIndex *= 2;
        }
        return output;
    }

    // converts a string to bits
    public static int[] messageToBits(String message) {
        int[] messageBits = new int[message.length() * 8]; // each character is 8 bits
        int count = 0;
        int ascii;

        // iterates through each bit in each character
        for (int i = 0; i < message.length(); i++) {
            ascii = message.charAt(i);

            for (int j = 7; j >= 0; j--) {
                messageBits[count++] = (ascii >> j) & 1; // moves necessary bit to last bit, then ANDS it with 1 to
                                                         // select it
            }
        }
        return messageBits;
    }
}
