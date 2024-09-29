package main;

import java.awt.*;
// import java.awt.event.WindowAdapter;
// import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import interfaces.Output;
import interfaces.Output1;
import interfaces.Output2;
import interfaces.Output3;
import utils.ImageUtils;

public class ImageDisplay {

    JFrame ogImageFrame;
    JLabel ogImageLabel;
    int ogImageHeight;
    int ogImageWidth;
    JFrame resultImageFrame;
    JLabel resultImageLabel;
    BufferedImage image;
    String path;
    int reSampleMethod;
    String sampleType;
    Output output;

    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(0);

                long len = frameLength;
                byte[] bytes = new byte[(int) len];

                raf.read(bytes);

                int ind = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        byte r = bytes[ind];
                        byte g = bytes[ind + height * width];
                        byte b = bytes[ind + height * width * 2];

                        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                        // int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                        img.setRGB(x, y, pix);
                        ind++;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showImage(String[] args){
        // Read in CLI Arguments
        path = args[0];
        ogImageWidth = Integer.parseInt(args[1]);
        ogImageHeight = Integer.parseInt(args[2]);
        reSampleMethod = Integer.parseInt(args[3]);
        switch (args[4]) {
            case "O1":
                output = new Output1();
                break;
            case "O2":
                output = new Output2();
                break;
            case "O3":
                output = new Output3();
                break;
            default:
                output = new Output(ogImageWidth, ogImageHeight);
                break;
        }
        if(ogImageWidth > output.width || ogImageHeight > output.height) {
            sampleType = "DownSample";
        } else {
            sampleType = "UpSample";
        }

		// Read in the specified image
		BufferedImage ogImage = new BufferedImage(ogImageWidth, ogImageHeight, BufferedImage.TYPE_INT_RGB);
		readImageRGB(ogImageWidth, ogImageHeight, path, ogImage);

        // Main Logic of upscaling or downscaling

        ImageUtils imageUtils = new ImageUtils();
        ResultImage resultImage = new ResultImage(null);

        // Decide Sampling method
        if(sampleType.equals("DownSample")) {
            if (reSampleMethod == 1) {
                resultImage.value = imageUtils.SpecificSampling(output, ogImageWidth, ogImageHeight, ogImage);
            } else {
                resultImage.value = imageUtils.GaussianWeightedSampling(output, ogImageWidth, ogImageHeight, ogImage);
            }
        } else {
            if (reSampleMethod == 1) {
                resultImage.value = imageUtils.NearestNeighborSampling(output, ogImageWidth, ogImageHeight, ogImage);
            } else {
                resultImage.value = imageUtils.BilinearInterpolation(output, ogImageWidth, ogImageHeight, ogImage);
            }
        }

        resultImageFrame = new JFrame("Result Image");
        GridBagLayout gLayout2 = new GridBagLayout();
        resultImageFrame.getContentPane().setLayout(gLayout2);

        resultImageLabel = new JLabel(new ImageIcon(resultImage.value));

        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.anchor = GridBagConstraints.CENTER;
        c2.weightx = 0.5;
        c2.gridx = 0;
        c2.gridy = 0;

        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 1;
        resultImageFrame.getContentPane().add(resultImageLabel, c2);

        resultImageFrame.pack();
        resultImageFrame.setVisible(true);

        // Save result image to files
        // int lastSeparatorIndex = path.lastIndexOf('/'); // Use '\\' for Windows paths
        // String fileNameWithExtension = (lastSeparatorIndex == -1) ? path : path.substring(lastSeparatorIndex + 1);
        // int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
        // String fileNameWithoutExtension = (lastDotIndex == -1) ? fileNameWithExtension : fileNameWithExtension.substring(0, lastDotIndex);
        // imageUtils.saveImage(resultImage.value, "png", "./img/output/" + sampleType + "_" + fileNameWithoutExtension + "_type_" + reSampleMethod + "_" + output.width + " X " + output.height + ".png");
        
        // Terminate Program
        resultImageFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Displays both images one after another, close first one (Original Image to see the Result Image)
		// ogImageFrame = new JFrame("Original Image");
		// GridBagLayout gLayout1 = new GridBagLayout();
		// ogImageFrame.getContentPane().setLayout(gLayout1);

		// ogImageLabel = new JLabel(new ImageIcon(ogImage));

		// GridBagConstraints c1 = new GridBagConstraints();
		// c1.fill = GridBagConstraints.HORIZONTAL;
		// c1.anchor = GridBagConstraints.CENTER;
		// c1.weightx = 0.5;
		// c1.gridx = 0;
		// c1.gridy = 0;

		// c1.fill = GridBagConstraints.HORIZONTAL;
		// c1.gridx = 0;
		// c1.gridy = 1;
		// ogImageFrame.getContentPane().add(ogImageLabel, c1);

		// ogImageFrame.pack();
		// ogImageFrame.setVisible(true);

        // ogImageFrame.addWindowListener(new WindowAdapter() {
        //     @Override
        //     public void windowClosing(WindowEvent e) {
                
    //         }
    //     });
	}
}
