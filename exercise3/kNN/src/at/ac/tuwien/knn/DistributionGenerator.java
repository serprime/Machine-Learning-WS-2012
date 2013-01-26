package at.ac.tuwien.knn;

import org.uncommons.maths.random.GaussianGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;

public class DistributionGenerator {

    public static void main(String[] args) throws FileNotFoundException {
        run();
    }

    public static void run() throws FileNotFoundException {
        PrintStream out = null;
        try {
            File file = new File("data/knn-3-gauss.arff");
            System.out.println(file.getAbsolutePath());
            out = new PrintStream(new FileOutputStream(file));
            System.setOut(out);

            out.println("@RELATION knn-data\n" +
                    "\n" +
                    "@ATTRIBUTE x     NUMERIC\n" +
                    "@ATTRIBUTE y     NUMERIC\n" +
                    "@ATTRIBUTE class {1,2,3}\n" +
                    "\n" +
                    "@DATA" +
                    "\n");

            generateGaussFor(10, 0, 10, 0, "1", 30);
            generateGaussFor(10, 0, 10, 30, "2", 30);
            generateGaussFor(10, -30, 10, 0, "3", 30);
        } finally {
            out.close();
        }


    }

    private static void generateGaussFor(int xWidth, int xCenter, int yWidth, int yCenter, String classLabel, int count) {
        GaussianGenerator gg = new GaussianGenerator(0, 1, new Random());
        for (int i = 0; i < count; i++) {
            int x = (int) (gg.nextValue() * xWidth + xCenter);
            int y = (int) (gg.nextValue() * yWidth + yCenter);
            System.out.println(x + "," + y + "," + classLabel);
        }
    }
}
