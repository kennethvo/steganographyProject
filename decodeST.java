import java.io.*;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.Math;
import java.io.FileWriter;

public class decodeST {
    public static String ENDING_MESSAGE = "&*09^%$__"; // super arbitrary. Just marks the end of the message
                                                       // could and should probably also use a header at the beginnign
                                                       // of the message to indicate size, but this is good enough for
                                                       // now

    /*public static void main(String[] args) {
    	try {
        	File output = new File("outputMessage.txt");
        	if(output.createNewFile()) {
        		System.out.println("File created");
        	}
        	else {
        		System.out.println("File already exists");
        	}
        }
        catch(IOException e){
        	System.out.println("File error");
        }
    	
    	// attempt to get image to decode
        try {
            // gets the name of the image file to decode
            System.out.println("Enter the name of the image you want to decode: ");
            Scanner userInput = new Scanner(System.in);
            String fileName = userInput.nextLine();
            userInput.close();

            // uses image path as FileInputStream argument
            InputStream inputFile = new FileInputStream(fileName);
            BufferedImage imageFile = ImageIO.read(inputFile);

            // decode message
            System.out.println("Now trying to decode...");

            // gets the contents of the message from beginning at the start of the file
            // until the ENDING_MESSAGE delimiter
            
            FileWriter writer = new FileWriter("outputMessage.txt");
            String message = decodeMessage(imageFile);
            message = message.substring(0, message.indexOf(ENDING_MESSAGE));
        	writer.write(message);
            writer.close();
            System.out.println("Decoding completed, check outputMessage.txt");
        }
        // image not found, inform user
        catch (IOException e) {
            System.out.println("Image not found");
        }
    }
    */
    // Use masking to get final bit of argument integer
    public static int decodeBit(int number, int lsbIndex) {
    	//get the value of the bit at the current index of LSBs
        return (number & lsbIndex) >> ((int)(Math.log(lsbIndex) / Math.log(2))); 
    }

    public String decodeMessage(BufferedImage image) {
        int pixelValue = 0;
        int height = image.getHeight();
        int width = image.getWidth();
        int[] message = new int[height * width * 24]; // each pixel is 1 bit, so total bits is height * width. * 24 b/c 3
                                                     // bits/pix and a total of 8 bits that can be encoded per pixel
        int lsbIndex = 1; //used to access other orders of LSBs
        int count = 0;
        
        while(count < message.length) {
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                if (count < message.length)// messageLength is the length of the message in bits, found in encodeMessage
	                                           // method
	                {
	                    pixelValue = image.getRGB(x, y);
	                    message[count++] = decodeBit((pixelValue >> 16) & 0xFF, lsbIndex); // Get the LSB of red
	                    message[count++] = decodeBit((pixelValue >> 8) & 0xFF, lsbIndex); // Get the LSB of green
	                    message[count++] = decodeBit(pixelValue & 0xFF, lsbIndex); // Get the LSB of blue
	
	                }
	            }
	        }
	        //double lsbIndex
	        lsbIndex *= 2;
        }
        
        return bitsToMessage(message);
    }

    // Converts bits of message into ASCII text
    public static String bitsToMessage(int[] bits) {
        StringBuilder message = new StringBuilder();
        
        // Process the bits in chunks of 8
        for (int i = 0; i < bits.length; i += 8) {
            // Ensure we have 8 bits to form a byte
            if (i + 7 < bits.length) {
                int asciiValue = 0;

                // Convert the 8 bits to a single ASCII value
                for (int j = 0; j < 8; j++) {
                    asciiValue = (asciiValue << 1) | bits[i + j]; // Shift left and add the current bit
                }

                // Convert ASCII value to character and append to message
                message.append((char) asciiValue);
            }
        }

        // Return converted message
        return message.toString();
    }
}