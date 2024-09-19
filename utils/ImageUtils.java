package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import interfaces.Output;

public class ImageUtils {
    // Variables for Gaussian Sampling
    int gaussianKernalSize = 5;
    double gaussianStandardDeviation = 1.5;
    double[][] gaussianKernal = new double[5][5]; 

    public ImageUtils() {
        // For gaussian Sampling - DownSampling
        double sum = 0.0;
        for (int x = 0; x < gaussianKernalSize; x++) {
            for (int y = 0; y < gaussianKernalSize; y++) {
                gaussianKernal[x][y] = gaussianFunction(x - gaussianKernalSize / 2, y - gaussianKernalSize / 2);
                sum += gaussianKernal[x][y];
            }
        }
        // Normalize the kernel
        for (int x = 0; x < gaussianKernalSize; x++) {
            for (int y = 0; y < gaussianKernalSize; y++) {
                gaussianKernal[x][y] /= sum;
            }
        }
    }

    // Saves output in output folder
    public void saveImage(BufferedImage image, String format, String filePath) {
        try {
            File outputFile = new File(filePath);
            ImageIO.write(image, format, outputFile);
            System.out.println("Image saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

    // Downsampling
    // Specific Sampling - Remove pixels depending on the scale ratio
    public BufferedImage SpecificSampling(Output output, int width, int height, BufferedImage image) {
        BufferedImage resultImage = new BufferedImage(output.width, output.height, BufferedImage.TYPE_INT_RGB);
        int imageX = 0;
        int imageY = 0;
        float xRatio = (float)width/output.width;
        float yRatio = (float)height/output.height;
        for (int y = 0; y < output.height; y++) {
            for (int x = 0; x < output.width; x++) {
                imageX = (int)(x * xRatio);
                imageY = (int)(y * yRatio);
                resultImage.setRGB(x, y, image.getRGB(imageX, imageY));
            }
        }
        return resultImage;
    }

    // Gaussian Sampling - Take average of pixels around the main image, using gaussian weights and then use the average
    public double gaussianFunction(int x, int y) {
        double coefficient = 1.0 / (2 * Math.PI * gaussianStandardDeviation * gaussianStandardDeviation);
        double exponent = -((x * x + y * y) / (2 * gaussianStandardDeviation * gaussianStandardDeviation));
        return coefficient * Math.exp(exponent);
    }    

    public int getGaussianWeightedAverage(int x, int y, BufferedImage image) {
        double sumRed = 0, sumGreen = 0, sumBlue = 0;
        double totalWeight = 0;
        int halfSize = gaussianKernalSize / 2;
    
        for (int i = -halfSize; i <= halfSize; i++) {
            for (int j = -halfSize; j <= halfSize; j++) {
                int imageX = x + i;
                int imageY = y + j;
    
                if (imageX >= 0 && imageX < image.getWidth() && imageY >= 0 && imageY < image.getHeight()) {
                    int rgb = image.getRGB(imageX, imageY);
                    double weight = gaussianKernal[i + halfSize][j + halfSize];
    
                    sumRed += ((rgb >> 16) & 0xFF) * weight;
                    sumGreen += ((rgb >> 8) & 0xFF) * weight;
                    sumBlue += (rgb & 0xFF) * weight;
                    totalWeight += weight;
                }
            }
        }

        if (totalWeight == 0) {
            totalWeight = 1;
        }
        
        int red = (int)(sumRed / totalWeight);
        int green = (int)(sumGreen / totalWeight);
        int blue = (int)(sumBlue / totalWeight);
        return (red << 16) | (green << 8) | blue;
    }    

    public BufferedImage GaussianWeightedSampling(Output output, int width, int height, BufferedImage image) {
        BufferedImage resultImage = new BufferedImage(output.width, output.height, BufferedImage.TYPE_INT_RGB);
        float xRatio = (float) width / output.width;
        float yRatio = (float) height / output.height;
    
        for (int y = 0; y < output.height; y++) {
            for (int x = 0; x < output.width; x++) {
                int imageX = Math.min((int) (x * xRatio), width - 1);
                int imageY = Math.min((int) (y * yRatio), height - 1);
                
                resultImage.setRGB(x, y, getGaussianWeightedAverage(imageX, imageY, image));
            }
        }
        return resultImage;
    }
    
    // Upsampling
    // Nearest neighbour sampling
    public BufferedImage NearestNeighborSampling(Output output, int width, int height, BufferedImage image) {
        BufferedImage resultImage = new BufferedImage(output.width, output.height, BufferedImage.TYPE_INT_RGB);
        int imageX = 0;
        int imageY = 0;
        float xRatio = (float)output.width/width;
        float yRatio = (float)output.height/height;
        for (int y = 0; y < output.height; y++) {
            for (int x = 0; x < output.width; x++) {
                imageX = (int)(x / xRatio);
                imageY = (int)(y / yRatio);
                resultImage.setRGB(x, y, image.getRGB(imageX, imageY));
            }
        }
        return resultImage;
    }

    // Bilinear Interpolation
    public float interpolatePixels(float x, float y, BufferedImage image) {
        // Horizontal Interpolation
        float val1red = 0;
        float val1green = 0;
        float val1blue = 0;
        int intX = (int)x;
        int intY = (int)y;
        if (intX-1 >= 0 && intY-1 >= 0 && intX+1 < image.getWidth()) {
            val1red = (int)((image.getRGB(intX-1, intY-1) >> 16) & 0xFF) + ((int)((image.getRGB(intX+1, intY-1)  >> 16) & 0xFF) - (int)((image.getRGB(intX-1, intY-1)  >> 16) & 0xFF)) * (x - intX);
            val1green = (int)((image.getRGB(intX-1, intY-1) >> 8) & 0xFF) + ((int)((image.getRGB(intX+1, intY-1)  >> 8) & 0xFF) - (int)((image.getRGB(intX-1, intY-1)  >> 8) & 0xFF)) * (x - intX);
            val1blue = (int)((image.getRGB(intX-1, intY-1)) & 0xFF) + ((int)((image.getRGB(intX+1, intY-1)) & 0xFF) - (int)((image.getRGB(intX-1, intY-1)) & 0xFF)) * (x - intX);
        } else {
            if (intX-1 < 0 && intY - 1 >= 0) {
                val1red = (int)((image.getRGB(intX+1, intY-1)  >> 16) & 0xFF);
                val1green = (int)((image.getRGB(intX+1, intY-1)  >> 8) & 0xFF);
                val1blue = (int)((image.getRGB(intX+1, intY-1)) & 0xFF);
            } else if (intX + 1 >= image.getWidth() && intY - 1 >= 0) {
                val1red = (int)((image.getRGB(intX-1, intY-1)  >> 16) & 0xFF);
                val1green = (int)((image.getRGB(intX-1, intY-1)  >> 8) & 0xFF);
                val1blue = (int)((image.getRGB(intX-1, intY-1)) & 0xFF);
            } else {
                val1red = (int)((image.getRGB(intX, intY)  >> 16) & 0xFF);
                val1green = (int)((image.getRGB(intX, intY)  >> 8) & 0xFF);
                val1blue = (int)((image.getRGB(intX, intY)) & 0xFF);
            }
        }
        float val2red = 0;
        float val2green = 0;
        float val2blue = 0;
        if (intX-1 >= 0 && intY+1 < image.getHeight() && intX+1 < image.getWidth()) {
            val2red = (int)((image.getRGB(intX-1, intY+1) >> 16) & 0xFF) + ((int)((image.getRGB(intX+1, intY+1) >> 16) & 0xFF) - (int)((image.getRGB(intX-1, intY+1) >> 16) & 0xFF) * (x - intX));
            val2green = (int)((image.getRGB(intX-1, intY+1) >> 8) & 0xFF) + ((int)((image.getRGB(intX+1, intY+1) >> 8) & 0xFF) - (int)((image.getRGB(intX-1, intY+1) >> 8) & 0xFF) * (x - intX));
            val2blue = (int)((image.getRGB(intX-1, intY+1)) & 0xFF) + ((int)((image.getRGB(intX+1, intY+1)) & 0xFF) - (int)((image.getRGB(intX-1, intY+1)) & 0xFF) * (x - intX));
        } else {
            if (intX-1 < 0 && intY + 1 < image.getHeight()) {
                val2red = (int)((image.getRGB(intX+1, intY+1) >> 16) & 0xFF);
                val2green = (int)((image.getRGB(intX+1, intY+1) >> 8) & 0xFF);
                val2blue = (int)((image.getRGB(intX+1, intY+1)) & 0xFF);
            } else if (intX + 1 >= image.getWidth() && intY + 1 < image.getHeight()) {
                val2red = (int)((image.getRGB(intX-1, intY+1) >> 16) & 0xFF);
                val2green = (int)((image.getRGB(intX-1, intY+1) >> 8) & 0xFF);
                val2blue = (int)((image.getRGB(intX-1, intY+1)) & 0xFF);
            } else {
                val2red = (int)((image.getRGB(intX, intY) >> 16) & 0xFF);
                val2green = (int)((image.getRGB(intX, intY) >> 8) & 0xFF);
                val2blue = (int)((image.getRGB(intX, intY)) & 0xFF);
            }
        }
        // Vertical Interpolation between horizontally interpolated values (val1 and val2)
        float resultred = val1red + ((val2red - val1red) * (y - intY));
        float resultgreen = val1green + ((val2green - val1green) * (y - intY));
        float resultblue = val1blue + ((val2blue - val1blue) * (y - intY));
        return (((int)resultred << 16) | ((int)resultgreen << 8) | (int)(resultblue));
    }

    public BufferedImage BilinearInterpolation(Output output, int width, int height, BufferedImage image) {
        BufferedImage resultImage = new BufferedImage(output.width, output.height, BufferedImage.TYPE_INT_RGB);
        float imageX = 0;
        float imageY = 0;
        float xRatio = (float)output.width/width;
        float yRatio = (float)output.height/height;
        for (int y = 0; y < output.height; y++) {
            for (int x = 0; x < output.width; x++) {
                imageX = (x / xRatio);
                imageY = (y / yRatio);
                float interpolatedPixelValue = interpolatePixels((int)imageX, (int)imageY, image);
                resultImage.setRGB(x, y, (int) interpolatedPixelValue);
            }
        }
        return resultImage;
    }
}
