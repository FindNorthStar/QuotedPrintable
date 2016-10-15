import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by FindNS on 2016/10/10.
 */
public class GsonTest {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        String filePath = "racefuzzerCount.json";
        Scanner scanner = new Scanner(new File(filePath),"UTF-8");

        OutputStreamWriter outputStreamWriter;
        outputStreamWriter = new OutputStreamWriter(new FileOutputStream("testBench.json",true),"UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        List<BenchmarkResult> benchmarkResults = new ArrayList<>();

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            BenchmarkResult br = gson.fromJson(line,BenchmarkResult.class);
            benchmarkResults.add(br);
            /*System.out.println("testName : "+br.getTestName()+" hybridRaceNum : "+br.getHybridNumOfRaces()
                    +" raceFuzzerNum : "+br.getRaceFuzzerNumOfRaces()+" avgTime : "+br.getAvgRaceFuzzerTime());*/
        }

        HashMap<String,BenchData> benchDataMap = new HashMap<>();

        for(BenchmarkResult br : benchmarkResults){
            BenchData bd = new BenchData();
            bd.maxRaceFuzzerTime=br.maxRaceFuzzerTime;
            bd.minRaceFuzzerTime=br.minRaceFuzzerTime;
            bd.avgRaceFuzzerTime=br.avgRaceFuzzerTime;
            bd.sumRaceFuzzerTime=br.sumRaceFuzzerTime;
            bd.hybridTime=br.hybridTime;
            bd.hybridNumOfRaces=br.hybridNumOfRaces;
            bd.hybridNumOfDataRaces=br.hybridNumOfDataRaces;
            bd.hybridNumOfLockRaces=br.hybridNumOfLockRaces;
            bd.raceFuzzerNumOfRaces=br.raceFuzzerNumOfRaces;
            bd.numOfExceptions=br.numOfExceptions;
            bd.hittingProbability=br.hittingProbability;
            benchDataMap.put(br.getTestName(),bd);
        }

        bufferedWriter.write(gson.toJson(benchDataMap));

        System.out.println("Json file generated successfully");

        scanner.close();
        bufferedWriter.close();
        outputStreamWriter.close();

    }
}
