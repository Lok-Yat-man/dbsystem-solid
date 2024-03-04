import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InputBusiness {
    public static void main(String[] args) {
        try {
            FileReader fileReader = new FileReader("E:\\桌面文件\\Desktop_File\\毕业\\毕业设计\\archive\\yelp_academic_dataset_business.json");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String s = "";
            int k = 0;
            while ((s = bufferedReader.readLine()) != null) {
                System.out.println(s);
                k++;
                if (k > 3)
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
