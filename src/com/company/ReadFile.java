package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ReadFile {

    public ArrayList<String> input;
    File file;
    BufferedReader bufferedReader;

    public void readFile() throws IOException {

        file = new File("C:/Users/hsncn/Desktop/input.txt");
        bufferedReader = new BufferedReader(new FileReader(file));
        input = new ArrayList<>();

        String line;
        while ((line = bufferedReader.readLine()) != null){
            input.add(line);
        }
    }
}
