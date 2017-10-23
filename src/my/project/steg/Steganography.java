package my.project.steg;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * Driver class that controls encoding or decoding of steanographic images.
 * 
 * @author Aleksandr Popov
 *
 */
public class Steganography {

	public static void main(String[] args) {

		Scanner keyboard = new Scanner(System.in);

		String userInput;

		while (true) {

			System.out.println("\nDo you wish to encode an image or decode an image? ([encode] or [decode])");

			userInput = keyboard.nextLine().trim();

			// user wants to encode
			if (userInput.equalsIgnoreCase("encode")) {

				Encode encoder = null;

				while (true) {
					System.out.println(
							"\nWhat is the name of the text file you want to encode? (Please include extension)");
					File file = new File(keyboard.nextLine());

					System.out.println("\nWhat is the name of the png image you want to encode the text file in?");
					userInput = keyboard.nextLine();

					File image = new File(userInput);
					File image2 = new File(userInput + ".png");

					// creates encoder object with the text file and image
					try {

						encoder = new Encode(image, file);

					} catch (IOException e1) {

						try {

							encoder = new Encode(image2, file);
						} catch (IOException e2) {

							System.out.println("\nInvalid input.");
							continue;
						}
					}

					break;
				}

				// not enough pixels in the image to encode the file
				if (!encoder.canEncode()) {

					System.out.println("Cannot encode the message into this image (not enough pixels).");
					System.exit(0);
				}

				// generates steganographic image
				else {

					encoder.makeStegImage();

					System.out.println("\n\nMessage has been encoded and saved as \"secret.png\", stopping execution.");
					System.exit(0);
				}
			}

			// user wants to decode
			else if (userInput.equalsIgnoreCase("decode")) {

				System.out.println("\nWhat is the name of the png image you want to decode?");
				userInput = keyboard.nextLine();

				File secret1 = new File(userInput);
				File secret2 = new File(userInput + ".png");

				Decode decoder = null;

				// creates decoder object with encoded image
				try {

					decoder = new Decode(secret1);

				} catch (IOException e1) {

					try {

						decoder = new Decode(secret2);
					} catch (IOException e2) {

						System.out.println("\nInvalid input.");
						continue;
					}
				}

				// decodes steganographic image
				decoder.makeText();

				System.out.println("\n\nMesage has been decoded and saved as \"Decoded\", stopping execution.");
				System.exit(0);
			}

			// invalid command
			else {

				System.out.println("Unrecognized command. Please try again\n");
			}
		}
	}
}
