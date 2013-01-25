package at.ac.tuwien.knn.data;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;

public class DataSetParser {
	public static DataSet parseFile(File dataFile) {
		Collection<DataPoint> datapoints = new LinkedList<DataPoint>();

		BufferedReader fileReader;
		try {
			fileReader = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(dataFile))));
			String line;
			boolean dataLines = false;
			int i=0;
			while ((line = fileReader.readLine()) != null && i < 10) {
				if(dataLines && !line.startsWith("//")){
					String[] columns = line.split(",");
					double x = Double.valueOf(columns[0]);
					double y = Double.valueOf(columns[1]);
					int classification = Integer.valueOf(columns[2]);
					datapoints.add(new DataPoint(x,y,classification));
				}
				if(line.startsWith("@data")){
					dataLines = true;
				}
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new DataSet(datapoints);
	}
}
