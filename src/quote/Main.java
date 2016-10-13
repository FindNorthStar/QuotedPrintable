package quote;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws DecoderException, IOException {
        QuotedPrintableCodec quotedPrintableCodec = new QuotedPrintableCodec();

        String filePath = "D:\\大三下\\00001 - 副本.vcf";
        Scanner scanner = new Scanner(new File(filePath),"UTF-8");

        OutputStreamWriter outputStreamWriter;
        outputStreamWriter = new OutputStreamWriter(new FileOutputStream("D:\\大三下\\peopleTel.txt",true),"utf-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        while(scanner.hasNextLine()){
            String string = scanner.nextLine();
            String[] strings = string.split(";");
            if (strings[0].equals("FN")){
                String[] toDecode = string.split(":");
                String name = quotedPrintableCodec.decode(toDecode[1]);
                bufferedWriter.write("name:"+name+":");
            }
            else if (strings[0].equals("TEL")){
                String[] toDecode = string.split(":");
                String tel = toDecode[1];
                bufferedWriter.write("tel:"+tel+"\n");
            }
        }

        bufferedWriter.close();
        outputStreamWriter.close();
    }
}
