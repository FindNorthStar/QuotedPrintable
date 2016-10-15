import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by FindNS on 2016/10/14.
 */
public class SearchBenchmarkResult {

    public static void main(String args[]) throws FileNotFoundException, ClassNotFoundException, IllegalAccessException {
        String jsonPath = args[0];
        Scanner scanner = new Scanner(new File(jsonPath),"UTF-8");
        Gson gson = new Gson();

        HashMap<String,BenchData> benchDataMap = gson.fromJson(scanner.nextLine(),
                new TypeToken<HashMap<String,BenchData>>() {
                }.getType());

        Class bench = Class.forName("BenchData");
        Field[] fields = bench.getDeclaredFields();

        if(args[1].equals("listname")){
            for(Map.Entry<String, BenchData> entry : benchDataMap.entrySet()){
                System.out.println(entry.getKey());
            }
        }
        else if(args[1].equals("listfields")){
            for(Field f : fields){
                System.out.println(f.getName());
            }
        }
        else{
            boolean isFind = false;
            for(Map.Entry<String, BenchData> entry : benchDataMap.entrySet()){
                if(entry.getKey().equals(args[1])){
                    for(Field f : fields){
                        if(f.getName().equals(args[2])){
                            System.out.println(entry.getKey()+"."+f.getName()+" = "+f.get(entry.getValue()));
                            isFind = true;
                            break;
                        }
                    }
                }
                if(isFind){
                    break;
                }
            }
            if(!isFind){
                System.out.println("No Results Found!");
            }
        }

        /*for(Field f : fields){
            System.out.println(f.getName());
        }


        for(Map.Entry<String, BenchData> entry : benchDataMap.entrySet()){
            System.out.println("key = "+entry.getKey()+", value = "+entry.getValue());
        }*/

    }
}
