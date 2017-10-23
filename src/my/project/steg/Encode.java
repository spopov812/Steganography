package my.project.steg;

import java.awt.image.BufferedImage;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * 
 * Blueprint for a object that can encode a steganographic image.
 * 
 * @author Aleksandr Popov
 *
 */
public class Encode {

	private BufferedImage image = null;
	private long availableBits;
	private long numBitsToEncode;
	private RandomAccessFile file;
	private byte[] fileAsBytes = null;

	/**
	 * 
	 * Constructor that takes a text file and encodes it into an image's pixel,
	 * altering the image imperceptibly. It takes 4 pixels to encode 3 bytes
	 * (characters). Each pixel can store 6 bits of encoded data (2 for each of
	 * the rgb values).
	 * 
	 * @param imageStr
	 *            image into which data will be encoded. The last two bits of
	 *            some of the pixels rgb data will be changed to encode text.
	 * 
	 * @param file
	 *            text file that will be encoded in the pixel data of the image.
	 * 
	 * @throws IOException
	 */
	public Encode(File imageStr, File file) throws IOException {

		image = ImageIO.read(imageStr);

		availableBits = image.getHeight() * image.getWidth() - 6;
		availableBits *= 6;

		PrintWriter pw = null;

		// makes sure the filesize is divisible by 3
		if (file.length() % 3 != 0) {

			try {

				// appends proper amount of spaces to the file
				FileWriter fw = new FileWriter(file, true);
				pw = new PrintWriter(fw);
				pw.print(file.length() % 3 == 1 ? "  " : " ");

			} catch (IOException e) {
				e.printStackTrace();

			} finally {
				if (pw != null) {
					pw.close();
				}
			}
		}

		this.file = new RandomAccessFile(file, "r");

		// number of bits in the file
		numBitsToEncode = (this.file.length()) * 8;

		fileAsBytes = new byte[(int) (numBitsToEncode / 8)];

		// converts the file bytes into an array of byte data
		this.file.readFully(fileAsBytes);
	}

	/**
	 * 
	 * Determines if there are enough pixels in the image to encode the file.
	 * 
	 * @return returns true if there are enough pixels in the image to encode
	 *         the file, false otherwise.
	 */
	public boolean canEncode() {

		return availableBits >= numBitsToEncode;
	}

	/**
	 * 
	 * Calls two methods that encode the text file size in the image and then
	 * actually encodes the text data into the image with a proper interval
	 * between the pixels. Also prints the final steganographic png.
	 * 
	 */
	public void makeStegImage() {

		encodeFileSize();

		encodeFile();

		// prints secret image
		try {

			BufferedImage bi = image;
			File outputfile = new File("secret.png");
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {

			System.out.println("Final file could not be created");
			System.exit(0);
		}

	}

	/**
	 * 
	 * Encodes the size of the text file into the first 6 pixels.
	 * 
	 */
	private void encodeFileSize() {

		int[] bitsToEncode = new int[16];
		int tempRGBVal = 0;

		int lastArrayIndex = 0;

		int red;
		int green;
		int blue;

		long fileLength = 0;

		try {
			fileLength = file.length();
		} catch (IOException e) {

			System.out.println("File could not be encoded.");
			System.exit(0);
		}

		// array of two bits of data that will then be encoded
		// as the file size
		for (int j = 15; j > -1; j--) {

			bitsToEncode[j] = (int) (fileLength & 0b11);
			fileLength >>= 2;
		}

		// encodes interval into first 6 pixels
		for (long i = 0; i < 6; i++) {

			tempRGBVal = image.getRGB(keyToRow(i), keyToCol(i));

			// bits are in rightmost position and last two bits are zeroes
			red = ((tempRGBVal >>> 16) & 0xFC);
			green = ((tempRGBVal >>> 8) & 0xFC);
			blue = (tempRGBVal & 0xff) & 0xFC;

			tempRGBVal = 0;

			// encodes proper values into the last two bits
			// of the rgb pixel values
			red |= bitsToEncode[lastArrayIndex];
			lastArrayIndex++;

			try {
				green |= bitsToEncode[lastArrayIndex];
				lastArrayIndex++;

				blue |= bitsToEncode[lastArrayIndex];
				lastArrayIndex++;
			} catch (ArrayIndexOutOfBoundsException oob) {

				green = (tempRGBVal >>> 8);
				blue = (tempRGBVal & 0xff);
			}

			tempRGBVal |= (red << 16);
			tempRGBVal |= (green << 8);
			tempRGBVal |= blue;

			image.setRGB(keyToRow(i), keyToCol(i), tempRGBVal);
		}
	}

	/**
	 * 
	 * Writes actual text file into the image after the first 6 pixels with a
	 * proper interval.
	 * 
	 */
	private void encodeFile() {

		int tempRGBVal = 0;
		long currentPixel = 6;
		int currChar = 0;
		int iterator = 0;
		int lastArrayIndex = 0;

		int red;
		int blue;
		int green;

		// interval at which encoded pixels will be spaced out
		long interval = availableBits / numBitsToEncode;

		int[] bitsToEncode = new int[12];

		// gets next 3 bytes from file
		for (int i = 0; i < fileAsBytes.length; i++) {

			// inserts 3 bytes into an variable
			currChar |= fileAsBytes[i];
			currChar <<= 8;
			iterator++;

			// once 3 bytes have been gotten
			if (iterator == 3) {

				currChar >>= 8;

				// array created to ease encoding of pixel data
				for (int j = 11; j > -1; j--) {

					bitsToEncode[j] = currChar & 0b11;
					currChar >>= 2;
				}

				// uses 4 pixels to encode those 3 bytes
				for (int j = 0; j < 4; j++) {

					tempRGBVal = image.getRGB(keyToRow(currentPixel), keyToCol(currentPixel));

					// extracts rgb data and zeroes last two bits
					red = ((tempRGBVal >> 16) & 0xFC);
					green = ((tempRGBVal >> 8) & 0xFC);
					blue = (tempRGBVal & 0xff) & 0xFC;

					tempRGBVal = 0;

					// encodes proper values into last two bits of rgb pixel
					// data
					red |= bitsToEncode[lastArrayIndex];
					lastArrayIndex++;

					green |= bitsToEncode[lastArrayIndex];
					lastArrayIndex++;

					blue |= bitsToEncode[lastArrayIndex];
					lastArrayIndex++;

					tempRGBVal |= red << 16;
					tempRGBVal |= green << 8;
					tempRGBVal |= blue;

					image.setRGB(keyToRow(currentPixel), keyToCol(currentPixel), tempRGBVal);

					currentPixel += interval;
				}

				lastArrayIndex = 0;
				iterator = 0;
				currChar = 0;
			}
		}
	}

	/*
	 * helps translate the pixel number into which row it appears in the image
	 */
	private int keyToRow(long key) {

		return (int) (key % image.getWidth());
	}

	/*
	 * helps translate the pixel number into which column it appears in the
	 * image
	 * 
	 */
	private int keyToCol(long key) {

		return (int) (key / image.getWidth());
	}
}
