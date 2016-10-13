import com.google.gson.Gson;

import java.io.*;
import java.util.*;

/**
 * Created by FindNS on 2016/10/9.
 */
public class RaceFuzzerCount {

    public static void main(String[] args) throws IOException {
        //String filePath = "D:\\IDEA\\calfuzzer-ksen007\\racefuzzerOutputUTF8.txt";
        String filePath = args[0];
        Scanner scanner = new Scanner(new File(filePath),"UTF-8");

        System.out.println("File Path is : "+filePath);

        OutputStreamWriter outputStreamWriter;
        /*outputStreamWriter = new OutputStreamWriter(new FileOutputStream(
                "racefuzzerOutputCount.json",true),"UTF-8");*/
        String outputPath = args[1];
        outputStreamWriter = new OutputStreamWriter(new FileOutputStream(
                outputPath+".json",true),"UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        List<BenchmarkResult> benchmarkResults = new ArrayList<>();

        while(scanner.hasNextLine()){
            String startLine = scanner.nextLine();
            String[] startLineSplit = startLine.split("_");
            //maybe t_test in future
            if(startLineSplit[0].equals("test")){
                BenchmarkResult result = new BenchmarkResult();
                result.setTestName(startLine.substring(0,startLine.length()-1));//remove the last colon(:)

                //count the real races in racefuzzer step
                Map<Integer,Integer> realRaceMap = new HashMap<>();
                boolean isOneResult = false;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if(line.equals("analysis-once:")){
                        boolean isHybridEnd = false;
                        while(scanner.hasNextLine()) {
                            String hybridLine = scanner.nextLine();
                            if(hybridLine.contains("# of data races")){
                                int num = 0;
                                String[] hybridLineSplit = hybridLine.split(" ");
                                for(String s : hybridLineSplit){
                                    try {
                                        int raceNum = Integer.parseInt(s);
                                        if(num == 0){
                                            result.setHybridNumOfDataRaces(raceNum);
                                        }
                                        else if(num == 1){
                                            result.setHybridNumOfLockRaces(raceNum);
                                            result.setHybridNumOfRaces(result.getHybridNumOfDataRaces()
                                                    + result.getHybridNumOfLockRaces());
                                            break;
                                        }
                                        num++;
                                    }
                                    catch (NumberFormatException e){
                                        continue;
                                    }
                                }
                            }
                            else if(hybridLine.contains("[stopwatch]")) {
                                String[] hybridLineSplit = hybridLine.split(" ");
                                for (String s : hybridLineSplit) {
                                    try {
                                        double hybridTime = Double.parseDouble(s);
                                        result.setHybridTime(hybridTime);
                                        isHybridEnd = true;
                                        break;
                                    }
                                    catch (NumberFormatException e) {
                                        continue;
                                    }
                                }
                            }
                            if(isHybridEnd){
                                break;
                            }
                        }
                    }
                    else if(line.equals("active-loop:")){
                        //int iterationNum;
                        if(result.getHybridNumOfRaces() == 0) {
                            isOneResult = setZeroTimeAndRace(result);
                            break;
                        }
                        else {
                            boolean isStopWatch = false;
                            while (scanner.hasNextLine()) {
                                String activeLoopLine = scanner.nextLine();
                                if(isStopWatch && !activeLoopLine.contains("[echo]")){
                                    break;
                                }
                                isStopWatch = false;
                                if (activeLoopLine.contains("Error:Iteration")) {
                                    int errorNum = 0;
                                    String[] activeLoopSplit = activeLoopLine.split(" ");
                                    errorNum = getErrorNum(activeLoopSplit);

                                    while (scanner.hasNextLine()) {
                                        String raceLine = scanner.nextLine();
                                        if (raceLine.contains("Real data race detected")) {
                                            if (!realRaceMap.containsKey(errorNum)) {
                                                realRaceMap.put(errorNum, 1);
                                            }
                                        } else if (raceLine.contains("[stopwatch]")) {
                                            String[] raceLineSplit = raceLine.split(" ");
                                            for (String s : raceLineSplit) {
                                                try {
                                                    double raceFuzzerTime = Double.parseDouble(s);
                                                    result.getTestTime().add(raceFuzzerTime);
                                                    break;
                                                }
                                                catch (NumberFormatException e) {
                                                    continue;
                                                }
                                            }
                                            isStopWatch = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //calculate num of real races and max/min/avg/sum racefuzzer time
                        isOneResult = setOneResult(result,realRaceMap);
                        break;
                    }
                    else if(line.equals("predictest-loop:")){
                        if(result.getHybridNumOfRaces() == 0) {
                            isOneResult = setZeroTimeAndRace(result);
                            break;
                        }
                        else{
                            int stopWatchCount = 0;
                            while (scanner.hasNextLine()) {
                                String activeLoopLine = scanner.nextLine();
                                if(stopWatchCount == 2 && !activeLoopLine.contains("[echo]")) {
                                    break;
                                }
                                stopWatchCount = 0;
                                if (activeLoopLine.contains("Error:Iteration")) {
                                    int errorNum = 0;
                                    String[] activeLoopSplit = activeLoopLine.split(" ");
                                    errorNum = getErrorNum(activeLoopSplit);

                                    while (scanner.hasNextLine()) {
                                        String raceLine = scanner.nextLine();
                                        if (raceLine.contains("Data race detected")) {
                                            if (!realRaceMap.containsKey(errorNum)) {
                                                realRaceMap.put(errorNum, 1);
                                            }
                                        } else if (raceLine.contains("[stopwatch]")) {
                                            stopWatchCount++;
                                            String[] raceLineSplit = raceLine.split(" ");
                                            for (String s : raceLineSplit) {
                                                try {
                                                    double raceFuzzerTime = Double.parseDouble(s);
                                                    result.getTestTime().add(raceFuzzerTime);
                                                    break;
                                                }
                                                catch (NumberFormatException e) {
                                                    continue;
                                                }
                                            }
                                            if(stopWatchCount == 2) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        isOneResult = setOneResult(result,realRaceMap);
                        break;
                    }
                    //insert a new result to benchmark
                }
                if(isOneResult){
                    benchmarkResults.add(result);
                }
                //debug printf
            }
        }

        Gson gson = new Gson();

        for(BenchmarkResult br : benchmarkResults){
            String json = gson.toJson(br);
            bufferedWriter.write(json+"\n");
            /*System.out.println("testName : "+br.getTestName()+" hybridRaceNum : "+br.getHybridNumOfRaces()
                    +" raceFuzzerNum : "+br.getRaceFuzzerNumOfRaces()+" avgTime : "+br.getAvgRaceFuzzertime());*/
        }

        scanner.close();
        bufferedWriter.close();
        outputStreamWriter.close();
    }

    private static boolean setOneResult(BenchmarkResult result, Map<Integer, Integer> realRaceMap) {
        result.setMaxRaceFuzzerTime(Collections.max(result.getTestTime()));
        result.setMinRaceFuzzerTime(Collections.min(result.getTestTime()));
        double averageTime = 0.0;
        for(double d : result.getTestTime()){
            averageTime += d;
        }
        result.setSumRaceFuzzerTime(averageTime);
        averageTime = averageTime / result.getTestTime().size();
        result.setAvgRaceFuzzertime(averageTime);
        result.setRaceFuzzerNumOfRaces(realRaceMap.size());
        return true;
    }

    private static int getErrorNum(String[] activeLoopSplit) {
        int num = 0;
        for (String s : activeLoopSplit) {
            try {
                if(s.contains(":")){
                    String[] sequence = s.split(":");
                    int tempNum = Integer.parseInt(sequence[0]);
                    if (num == 0) {
                        return tempNum;
                    }
                }
            }
            catch (NumberFormatException e) {
                continue;
            }
        }
        return 0;
    }

    public static boolean setZeroTimeAndRace(BenchmarkResult result){
        result.setMaxRaceFuzzerTime(0.0);
        result.setMinRaceFuzzerTime(0.0);
        result.setAvgRaceFuzzertime(0.0);
        result.setSumRaceFuzzerTime(0.0);
        result.setRaceFuzzerNumOfRaces(0);
        return true;
    }

                                /*  if(activeLoopLine.contains("[echo] Iteration:")){
                                String[] activeLoopSplit = activeLoopLine.split(" ");
                                for(String s : activeLoopSplit){
                                    try {
                                        iterationNum = Integer.parseInt(s);
                                        break;
                                    }
                                    catch(NumberFormatException e){
                                        continue;
                                    }
                                }
                            }
                            else */
}

