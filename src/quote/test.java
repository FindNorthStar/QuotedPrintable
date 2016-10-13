package quote;

/**
 * Created by FindNS on 2016/10/12.
 */
public class test {



    public static void main(String[] args){
        boolean isOneResult=false;
        BenchmarkResult result = new BenchmarkResult();
        setZero(result,isOneResult);
        System.out.println(isOneResult);
        System.out.println(result.getTestName());
    }

    private static void setZero(BenchmarkResult result, boolean isOneResult) {
        result.setTestName("this is a test");
        isOneResult=true;
        return;
    }
}
