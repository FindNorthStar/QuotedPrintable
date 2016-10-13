import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by FindNS on 2016/10/10.
 */
public class GsonTest {
    public static void main(String[] args) throws FileNotFoundException {
        /*People p = new People();
        p.setAge(20);
        p.setName("People");
        p.setSetName(true);*/
        Gson gson = new Gson();
        /*String json = gson.toJson(p);
        System.out.println(json);
        People pe = gson.fromJson(json,People.class);
        System.out.println(pe.toString());*/

        String filePath = "racefuzzerOutputCount.json";
        Scanner scanner = new Scanner(new File(filePath),"UTF-8");

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            BenchmarkResult br = gson.fromJson(line,BenchmarkResult.class);
            System.out.println("testName : "+br.getTestName()+" hybridRaceNum : "+br.getHybridNumOfRaces()
                    +" raceFuzzerNum : "+br.getRaceFuzzerNumOfRaces()+" avgTime : "+br.getAvgRaceFuzzertime());
        }

        scanner.close();

    }
}
