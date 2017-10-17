# Steganography
Encodes text files into the pixel data of images

In this repository is a png image that looks ordinary. It actually contains Romeo and Juliet, Macbeth, the iTunes Terms and Conditions, my resume, the source code for this program, and the first million digits of pi.
How? It is a steganographic image.

Note-
Images into which text is encoded MUST be in png format as JPEG compression will corrupt the encoded data.
The high resolution of the image, the more data can be inserted.
If a non-steganographic image is attempted to be decoded, giberrish will be created.
There is an occasional issue with quotations and with single quotes where there are not supported by 8-bit encoding (UTF-8) so they may come out corrupted.

Some terminology-
Where cryptography is the practice of hiding information in gibberish that is obvious to a person but inaccessible, steganography hides data in plain sight but the user does not know that they are looking at hidden information. 

This program encodes data into the last two bits of the red, green, and blue pixel values. This means that a pixel can contain 6 bits of data. The change to the color tone of a pixel is hardly noticible. 

Until a certain point the more data that is crammed into an image, no change is noticible. But after a certain point, if zoomed in more artifacts are noticible. Busy images and high resolution images mean that more cramming is possible with less noticible effects.


This program contains two parts...

Encode-
When provided a text file and an image, the program will calculate the amount of bytes in the text file and the number of pixels in the image. With that information, the program calculates the inerval between encoded pixels. It then encodes the file size of the text file in the first 6 pixels. From the 7th pixel, the program encodes a pixel with 6 bytes of data from the text file, then moves the proper amount to the next pixel (the amount specified by the interval) until it has encoded the entire text file in the image.

Decode-
When provided an image, the program reads the size of the text fil it is supposed to create from the first 6 pixels. Knowing that and the size of the image, it can deduce the interval at which pixels are encoded and starts decoding starting from the 7th pixel and moving over the amount specified by the interval. It then writes the decoded data to a file.


To Compile-
$ javac -d bin src/my/project/Steganography/*.java

To Run-
$ java -cp bin my.project.Steganography.Steganography
