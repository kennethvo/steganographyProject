import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class SteganographyApplication extends JFrame {
    
    private JTextField imagePathField, messagePathField;
    private JTextArea outputArea;
    private JButton encodeButton, decodeButton, selectImageButton, selectTextButton;
    private JFileChooser fileChooser;

    public static String ENDING_MESSAGE = "&*09^%$__";
    
    public SteganographyApplication() {
        // Set up frame
        setTitle("Steganography Application");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //look into what this does
        setLayout(new BorderLayout());

        // Panel for file selection and encoding/decoding
        JPanel filePanel = new JPanel(new GridLayout(2, 3, 5, 5));
        
        imagePathField = new JTextField(20);
        messagePathField = new JTextField(20);
        
        selectImageButton = new JButton("Select Image");
        selectTextButton = new JButton("Select Text");
        
        encodeButton = new JButton("Encode Message");
        decodeButton = new JButton("Decode Message");

        // Add components to the file panel
        filePanel.add(new JLabel("Image File:"));
        filePanel.add(selectImageButton);
        filePanel.add(imagePathField);
        
        
        filePanel.add(new JLabel("Message File (for encoding):"));
        filePanel.add(selectTextButton);
        filePanel.add(messagePathField);
        

        // Output area
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);

        // Add components to the frame
        add(filePanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(encodeButton);
        buttonPanel.add(decodeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // File chooser for selecting image/text files
        fileChooser = new JFileChooser();

        // Event listeners
        selectImageButton.addActionListener(e -> selectFile(imagePathField));
        selectTextButton.addActionListener(e -> selectFile(messagePathField));
        encodeButton.addActionListener(e -> encodeMessage());
        decodeButton.addActionListener(e -> decodeMessage());
        
        setVisible(true);
    }

    private void selectFile(JTextField textField) {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void encodeMessage() {
        String imagePath = imagePathField.getText();
        String messagePath = messagePathField.getText();
        if (imagePath.isEmpty() || messagePath.isEmpty()) {
            outputArea.setText("Please select both an image and a message file.");
            return;
        }
        // Call your encoding function here
        // Example: boolean success = Encoder.encodeMessage(imagePath, messagePath);

        try{
            InputStream inputFile = new FileInputStream(imagePath);
            BufferedImage imageFile = ImageIO.read(inputFile);

            int maxLength = ((24 * imageFile.getHeight() * imageFile.getWidth()) / 8) - ENDING_MESSAGE.length();
            
            outputArea.setText("Maximum message length = " + (maxLength) + " characters.");
            String message = readMessageFromFile(messagePath);

            if (message.length() + ENDING_MESSAGE.length() > maxLength) {
                outputArea.setText("Message too long!");
                System.exit(1);
            }

            String fileName = imagePath.substring(imagePath.lastIndexOf('\\') + 1);
            String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
            fileName = fileName.substring(0, fileName.indexOf("."));

            encodeST encodingObject = new encodeST();
            ImageIO.write(encodingObject.encodeMessage(imageFile, message + ENDING_MESSAGE), fileType, new File(fileName + "ENCODED." + fileType));
            outputArea.setText("Message encoded into image file: " + fileName + "ENCODED." + fileType);
        }
        
        catch (IOException e) {
            outputArea.setText("Image not found");
        }

        outputArea.setText("Encoding completed. Check the output file.");
    }

    private void decodeMessage() {
        String imagePath = imagePathField.getText();
        if (imagePath.isEmpty()) {
            outputArea.setText("Please select an image file.");
            return;
        }

        InputStream inputFile;
        try {
            inputFile = new FileInputStream(imagePath);
            BufferedImage imageFile = ImageIO.read(inputFile);
         
        
            decodeST decodingObject = new decodeST();
            FileWriter writer = new FileWriter("outputMessage.txt");
            String message = decodingObject.decodeMessage(imageFile);
            message = message.substring(0, message.indexOf(ENDING_MESSAGE));
            writer.write(message);
            writer.close();
            outputArea.setText("Decoding completed, the message is:\n" + message);
            outputArea.setCaretPosition(0);

        }catch (IOException e) {
            outputArea.setText("Image not found");
        }
        
    }

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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SteganographyApplication::new);
    }
}