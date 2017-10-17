package my.project.Steganography;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.FileOutputStream;

/**
 * 
 * Blueprint for a decoder object that takes a
 * steganographic image and decodes the data
 * into a text file
 * 
 * @author Aleksandr Popov
 *
 */
public class Decode {

	private BufferedImage encoded = null;
	private int fileSize = 0;
	
	/**
	 * 
	 * Constructor for a decoder object that takes in
	 * a steganographic image
	 * 
	 * @param imageStr png image that is actually a steganographic image
	 * @throws IOException
	 */
	public Decode(File imageStr) throws IOException{
		
		encoded = ImageIO.read(imageStr);	
	}
	
	/**
	 * 
	 * Decodes the file size of the encoded image from the first 6 pixels.
	 * From there, the method know what the interval between
	 * encoded pixels is and reads that data.
	 * 
	 */
	public void makeText(){
		
		int tempRGBVal;
		
		//reads the size of the file encoded in first 6 pixels
		for (int i = 0; i < 6; i++){
			
			tempRGBVal = encoded.getRGB(keyToRow(i), keyToCol(i));
			
			if (i == 5){
				
				fileSize <<= 2;
				fileSize |= (tempRGBVal & 0x30000)>> 16;
				break;
			}
			
			fileSize <<= 6;
			
			
			for (int j = 0; j < 3; j ++){
				
				fileSize |= ((tempRGBVal & 0b11) << (j * 2));
				tempRGBVal >>>= 8;
			}
		}
		
		//decodes the data in the image based off the interval between encoded pixels
		byte[] decodedText = decodeMainImage();
		
		FileOutputStream output = null;
		
		//makes a new file
		try {
			output = new FileOutputStream("Decoded");
		} catch (FileNotFoundException e) {
			
			System.out.println("Decoded file could not be created.");
			System.exit(0);
		}
		
		//writes decoded data to that file
		try {
			output.write(decodedText);
		} catch (IOException e) {
			
			System.out.println("Could not write decoded text to file.");
			System.exit(0);
		}
		
	}
	
	/**
	 * 
	 * Decodes the encoded pixels offset by an interval after the first
	 * six pixels.
	 * 
	 * @return array of byte data, each of which corresponds to an encoded
	 * character
	 */
	private byte[] decodeMainImage(){
		
		int iterator = 0;
		int lastArrayPos = 0;
		int currChar = 0;
		int decodedChar;
		
		int tempRGBVal;
		
		int red;
		int green;
		int blue;
		
		long currentPixel = 6;
		
		byte[] decodedText = new byte[fileSize];
		
		long availableBits  = (((encoded.getHeight() * encoded.getWidth()) - 6) * 6);
		long bitsInFile = (fileSize * 8);
		
		long interval = availableBits / bitsInFile;
		
		//loops over all encoded pixels
		for (int i = 0; i < (fileSize * 8) / 6; i ++){
			
			iterator++;
			
			tempRGBVal = encoded.getRGB(keyToRow(currentPixel), keyToCol(currentPixel));
			
			tempRGBVal &= 0xFFFFFF;
			
			currChar <<= 6;
			
			//places encoded data into a variable
			red = (tempRGBVal >> 16) & 0b11;
			green = (tempRGBVal >> 8) & 0b11;
			blue = tempRGBVal & 0b11;
			
			currChar |= (red << 4) + (green << 2) + blue;
			
			//once four encoded pixels have been read
			if (iterator == 4){
				
				//writes 3 bytes of data into array of decoded byte data
				for (int j = 0; j < 3; j++){
					
					decodedChar = (currChar & 0xFF0000);
					decodedChar >>= 16;
					currChar <<= 8;
					
					decodedText[lastArrayPos] = (byte)decodedChar ;
					lastArrayPos++;
				}
				
				iterator = 0;
				currChar = 0;
			}
			
			currentPixel += interval;
		}
		
		return decodedText;
	}
	
	/* helps translate the pixel number into which row
	 * it appears in the image
	 */
	private int keyToRow(long key){
		
		return (int) (key % encoded.getWidth());
	}
	
	/*
	 * helps translate the pixel number into which
	 * column it appears in the image
	 * 
	 */
	private int keyToCol(long key){
		
		return (int) (key / encoded.getWidth());
	}
}
