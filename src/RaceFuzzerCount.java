import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by FindNS on 2016/10/9.
 */
public class RaceFuzzerCount {

    /**
     *
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException {
        /*
        specify input file, use UTF-8 format
         */
        String filePath = args[0];
        Scanner scanner = new Scanner(new File(filePath),"UTF-8");

        System.out.println("File Path is : "+filePath);

        /*
        sepcify output file, use UTF-8 format
         */
        OutputStreamWriter outputStreamWriter;

        String outputPath = args[1];
        outputStreamWriter = new OutputStreamWriter(new FileOutputStream(
                outputPath+".json",true),"UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        /*
        result is stored in list
         */
        List<BenchmarkResult> benchmarkResults = new ArrayList<>();

        int lineNum = 0;
        /*
        process each line
         */
        while(scanner.hasNextLine()){
            String startLine = scanner.nextLine();
            lineNum++;

            String[] startLineSplit = startLine.split("_");
            //maybe t_test in future
            if(startLineSplit[0].equals("test")){
                BenchmarkResult result = new BenchmarkResult();
                result.setTestName(startLine.substring(0,startLine.length()-1));//remove the last colon(:)

                System.out.println("Processing "+result.getTestName()+" in line "+lineNum);

                //count the real races in racefuzzer step
                Map<Integer,Integer> realRaceMap = new HashMap<>();
                Map<String,Integer> exceptionMap = new HashMap<>();
                boolean isOneResult = false;
                int totalIterationTimes = 0;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    lineNum++;

                    /*
                    3 types of statement:
                    analysis-once:
                    active-loop:
                    predictest-loop:
                     */
                    if(line.equals("analysis-once:")){
                        boolean isHybridEnd = false;
                        while(scanner.hasNextLine()) {
                            String hybridLine = scanner.nextLine();
                            lineNum++;

                            //extract hybrid race number and lock number
                            if(hybridLine.contains("# of data races")){
                                processNumOfDataLockRaces(hybridLine,result);
                            }
                            //extract hybrid running time
                            else if(hybridLine.contains("[stopwatch]")) {
                                isHybridEnd = processHybridStopWatch(hybridLine,result);
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
                                lineNum++;

                                if(isStopWatch && !activeLoopLine.contains("[echo]")){
                                    break;
                                }
                                isStopWatch = false;
                                if (activeLoopLine.contains("Error:Iteration")) {
                                    totalIterationTimes++;
                                    int errorNum = 0;
                                    String[] activeLoopSplit = activeLoopLine.split(" ");
                                    errorNum = getErrorNum(activeLoopSplit);

                                    while (scanner.hasNextLine()) {
                                        String raceLine = scanner.nextLine();
                                        lineNum++;

                                        /*
                                        3 types of statements:
                                        Real data race detected -> count race number
                                        Exception -> count exception number
                                        [stopwatch] -> count running time
                                         */
                                        if (raceLine.contains("Real data race detected")) {
                                            if (!realRaceMap.containsKey(errorNum)) {
                                                realRaceMap.put(errorNum, 1);
                                            }
                                        }
                                        else if (raceLine.contains("Exception")){
                                            String exceptionName = raceLine + " " + scanner.nextLine();
                                            lineNum++;

                                            if(!exceptionMap.containsKey(exceptionName)){
                                                exceptionMap.put(exceptionName,1);
                                            }
                                        }
                                        else if(raceLine.contains("[stopwatch]")) {
                                            isStopWatch = processActiveLoopStopWach(raceLine,result);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //calculate num of real races and max/min/avg/sum racefuzzer time
                        isOneResult = setOneResult(result,realRaceMap,exceptionMap,totalIterationTimes);
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
                                lineNum++;

                                if(stopWatchCount == 2 && !activeLoopLine.contains("[echo]")) {
                                    break;
                                }
                                stopWatchCount = 0;
                                if (activeLoopLine.contains("Error:Iteration")) {
                                    totalIterationTimes++;
                                    int errorNum = 0;
                                    String[] activeLoopSplit = activeLoopLine.split(" ");
                                    errorNum = getErrorNum(activeLoopSplit);

                                    while (scanner.hasNextLine()) {
                                        String raceLine = scanner.nextLine();
                                        lineNum++;

                                        /*
                                        3 types of statements:
                                        Data race detected -> count race number
                                        Exception -> count exception number
                                        [stopwatch] -> count running time
                                         */
                                        if (raceLine.contains("Data race detected")) {
                                            if (!realRaceMap.containsKey(errorNum)) {
                                                realRaceMap.put(errorNum, 1);
                                            }
                                        } else if (raceLine.contains("Exception")){
                                            String exceptionName = raceLine + " " + scanner.nextLine();
                                            lineNum++;

                                            if(!exceptionMap.containsKey(exceptionName)){
                                                exceptionMap.put(exceptionName,1);
                                            }
                                        } else if (raceLine.contains("[stopwatch]")) {
                                            stopWatchCount++;
                                            processPredictestLoopStopWatch(raceLine,result);
                                            if(stopWatchCount == 2) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        isOneResult = setOneResult(result,realRaceMap,exceptionMap,totalIterationTimes);
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

        /*
        convert benchmark result to json
         */
        HashMap<String,BenchData> benchDataMap = new HashMap<>();

        Class bench = Class.forName("BenchData");
        Class benchRes = Class.forName("BenchmarkResult");
        Field[] benchFields = bench.getDeclaredFields();
        Field[] benchResFields = benchRes.getDeclaredFields();

        for(BenchmarkResult br : benchmarkResults){
            BenchData bd = new BenchData();
            for(Field bf : benchFields){
                for(Field brf : benchResFields){
                    if(bf.getName().equals(brf.getName())){
                        bf.set(bd,brf.get(br));
                        break;
                    }
                }
            }
            benchDataMap.put(br.getTestName(),bd);
        }

        bufferedWriter.write(gson.toJson(benchDataMap));

        System.out.println("Json file generated successfully");

        scanner.close();
        bufferedWriter.close();
        outputStreamWriter.close();
    }

    /**
     *
     * @param raceLine
     * @param result
     */
    private static void processPredictestLoopStopWatch(String raceLine, BenchmarkResult result) {
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
    }

    /**
     *
     * @param raceLine
     * @param result
     * @return
     */
    private static boolean processActiveLoopStopWach(String raceLine, BenchmarkResult result) {
        String[] raceLineSplit = raceLine.split(" ");
        for (String s : raceLineSplit) {
            try {
                if((!s.contains("timer")) && s.contains(":")){
                    //process more than time format such as 1:22.976
                    //hope running time not beyond 1 hour
                    String timeSplit[] = s.split(":");
                    double minCount = Double.parseDouble(timeSplit[0]);
                    double secCount = Double.parseDouble(timeSplit[1]);
                    double raceFuzzerTime = 60 * minCount + secCount;
                    result.getTestTime().add(raceFuzzerTime);
                }
                else {
                    double raceFuzzerTime = Double.parseDouble(s);
                    result.getTestTime().add(raceFuzzerTime);
                }
                break;
            }
            catch (NumberFormatException e) {
                continue;
            }
        }
        return true;
    }

    /**
     *
     * @param hybridLine
     * @param result
     * @return
     */
    private static boolean processHybridStopWatch(String hybridLine, BenchmarkResult result) {
        String[] hybridLineSplit = hybridLine.split(" ");
        for (String s : hybridLineSplit) {
            try {
                if((!s.contains("timer")) && s.contains(":")){
                    //process more than time format such as 1:22.976
                    //hope running time not beyond 1 hour
                    String timeSplit[] = s.split(":");
                    double minCount = Double.parseDouble(timeSplit[0]);
                    double secCount = Double.parseDouble(timeSplit[1]);
                    double hybridtime = 60 * minCount + secCount;
                    result.setHybridTime(hybridtime);
                }
                else {
                    double hybridTime = Double.parseDouble(s);
                    result.setHybridTime(hybridTime);
                }
                return true;
            }
            catch (NumberFormatException e) {
                continue;
            }
        }
        return false;
    }

    /**
     *
     * @param hybridLine
     * @param result
     */
    private static void processNumOfDataLockRaces(String hybridLine, BenchmarkResult result) {
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

    /**
     *
     * @param result
     * @param realRaceMap
     * @param exceptionMap
     * @param totalIterationTimes
     * @return
     */
    private static boolean setOneResult(BenchmarkResult result,
                                        Map<Integer, Integer> realRaceMap,
                                        Map<String, Integer> exceptionMap,
                                        int totalIterationTimes) {
        result.setMaxRaceFuzzerTime(Collections.max(result.getTestTime()));
        result.setMinRaceFuzzerTime(Collections.min(result.getTestTime()));
        double averageTime = 0.0;
        for(double d : result.getTestTime()){
            averageTime += d;
        }
        result.setSumRaceFuzzerTime(averageTime);
        averageTime = averageTime / result.getTestTime().size();
        result.setAvgRaceFuzzerTime(averageTime);
        result.setRaceFuzzerNumOfRaces(realRaceMap.size());
        result.setNumOfExceptions(exceptionMap.size());
        result.setHittingProbability((realRaceMap.size() * 1.0) / totalIterationTimes);
        return true;
    }

    /**
     *
     * @param activeLoopSplit
     * @return
     */
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

    /**
     *
     * @param result
     * @return
     */
    public static boolean setZeroTimeAndRace(BenchmarkResult result){
        result.setMaxRaceFuzzerTime(0.0);
        result.setMinRaceFuzzerTime(0.0);
        result.setAvgRaceFuzzerTime(0.0);
        result.setSumRaceFuzzerTime(0.0);
        result.setRaceFuzzerNumOfRaces(0);
        return true;
    }

}

