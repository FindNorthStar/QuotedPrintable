import java.util.ArrayList;
import java.util.List;

/**
 * Created by FindNS on 2016/10/9.
 */
public class BenchmarkResult {
    public String testName;
    public double maxRaceFuzzerTime;
    public double minRaceFuzzerTime;
    public double avgRaceFuzzerTime;
    public double sumRaceFuzzerTime;
    public double hybridTime;
    public List<Double> testTime;
    public int hybridNumOfRaces;
    public int hybridNumOfDataRaces;
    public int hybridNumOfLockRaces;
    public int raceFuzzerNumOfRaces;
    public int numOfExceptions;
    public double hittingProbability;

    public BenchmarkResult(){
        testTime = new ArrayList<>();
        numOfExceptions = 0;
        hittingProbability = 0.0;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public double getMaxRaceFuzzerTime() {
        return maxRaceFuzzerTime;
    }

    public void setMaxRaceFuzzerTime(double maxRaceFuzzerTime) {
        this.maxRaceFuzzerTime = maxRaceFuzzerTime;
    }

    public double getMinRaceFuzzerTime() {
        return minRaceFuzzerTime;
    }

    public void setMinRaceFuzzerTime(double minRaceFuzzerTime) {
        this.minRaceFuzzerTime = minRaceFuzzerTime;
    }

    public double getSumRaceFuzzerTime() {
        return sumRaceFuzzerTime;
    }

    public void setSumRaceFuzzerTime(double sumRaceFuzzerTime) {
        this.sumRaceFuzzerTime = sumRaceFuzzerTime;
    }

    public double getHybridTime() {
        return hybridTime;
    }

    public void setHybridTime(double hybridTime) {
        this.hybridTime = hybridTime;
    }

    public int getHybridNumOfRaces() {
        return hybridNumOfRaces;
    }

    public void setHybridNumOfRaces(int hybridNumOfRaces) {
        this.hybridNumOfRaces = hybridNumOfRaces;
    }

    public int getRaceFuzzerNumOfRaces() {
        return raceFuzzerNumOfRaces;
    }

    public void setRaceFuzzerNumOfRaces(int raceFuzzerNumOfRaces) {
        this.raceFuzzerNumOfRaces = raceFuzzerNumOfRaces;
    }

    public int getNumOfExceptions() {
        return numOfExceptions;
    }

    public void setNumOfExceptions(int numOfExceptions) {
        this.numOfExceptions = numOfExceptions;
    }

    public int getHybridNumOfDataRaces() {
        return hybridNumOfDataRaces;
    }

    public void setHybridNumOfDataRaces(int hybridNumOfDataRaces) {
        this.hybridNumOfDataRaces = hybridNumOfDataRaces;
    }

    public int getHybridNumOfLockRaces() {
        return hybridNumOfLockRaces;
    }

    public void setHybridNumOfLockRaces(int hybridNumOfLockRaces) {
        this.hybridNumOfLockRaces = hybridNumOfLockRaces;
    }

    public List<Double> getTestTime() {
        return testTime;
    }

    public void setTestTime(List<Double> testTime) {
        this.testTime = testTime;
    }

    public double getAvgRaceFuzzerTime() {
        return avgRaceFuzzerTime;
    }

    public void setAvgRaceFuzzerTime(double avgRaceFuzzerTime) {
        this.avgRaceFuzzerTime = avgRaceFuzzerTime;
    }

    public double getHittingProbability() {
        return hittingProbability;
    }

    public void setHittingProbability(double hittingProbability) {
        this.hittingProbability = hittingProbability;
    }
}
