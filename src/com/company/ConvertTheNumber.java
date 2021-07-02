package com.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class ConvertTheNumber {

    readInput readInput;
    writeOutput writeOutput = new writeOutput();
    int unsignedX = 0;
    int signedPositiveX = 0;
    int signedNegativeX = 0;
    int floatingInt;
    int floatingFrac;
    StringBuilder finalFormHex;
    ArrayList<String> totalHexValues = new ArrayList<>();


    public void SeparateInput() throws IOException {
        Scanner sc= new Scanner(System.in);
        System.out.print("\nPlease enter pathname for input text file,");
        System.out.print("\033[1m Format-> C:/Users/desktop/input.txt : \033[0m");
        String pathname = sc.nextLine();
        readInput = new readInput();
        readInput.readFile(pathname);
        System.out.print("Byte ordering (Endian)->\033[1m [Little] or [Big]: \033[0m");
        String byteOrder = sc.nextLine();
        System.out.print("Floating point size (1, 2, 3 or 4): ");
        int sizeFloating = sc.nextInt();
        System.out.println();


        for (int i = 0; i< readInput.input.size(); i++){

            if (readInput.input.get(i).contains("u")){
                //unsigned control
                unsignedX = Integer.parseInt(readInput.input.get(i).trim().substring(0, readInput.input.get(i).length()-1));
                ConvertUnsignedIntToHex(unsignedX,byteOrder);

            }else if(readInput.input.get(i).contains(".")) {
                //floating point control

                int signBit;
                //negative floating point
                if (readInput.input.get(i).contains("-")){
                    String negativeFloating = readInput.input.get(i).trim().substring(1, readInput.input.get(i).length());
                    String[] floatingNumber = negativeFloating.split("\\.");
                    floatingInt = Integer.parseInt(floatingNumber[0]);
                    floatingFrac = Integer.parseInt(floatingNumber[1]);
                    signBit = 1;
                }
                //positive floating point
                else {
                    String[] floatingNumber = readInput.input.get(i).trim().split("\\.");
                    floatingInt = Integer.parseInt(floatingNumber[0]);
                    floatingFrac = Integer.parseInt(floatingNumber[1]);
                    signBit = 0;
                }

                int intPart = floatingInt;
                int length = String.valueOf(floatingFrac).length();
                double fracPart =  (floatingFrac/Math.pow(10,length));
                ConvertFloatingToHex(intPart, fracPart, signBit, sizeFloating,byteOrder);

            }else{
                //signed control
                if (readInput.input.get(i).contains("-")){
                    //negative signed
                    signedNegativeX = Integer.parseInt(readInput.input.get(i).trim().substring(1, readInput.input.get(i).length()));
                    ConvertNegativeNumberToHex(signedNegativeX,byteOrder);

                }else{
                    if(Integer.parseInt(readInput.input.get(i).trim())==0) {
                        //zero handle
                        totalHexValues.add("00 00");
                    }
                    else{
                        //positive signed
                        signedPositiveX = Integer.parseInt(readInput.input.get(i).trim());
                        ConvertPositiveNumberToHex(signedPositiveX,byteOrder);

                    }
                }
            }
        }
        writeOutput.createOutputFile(totalHexValues);
    }

    private void ConvertFloatingToHex(int intPart, double fracPart, int signBit, int sizeFloating,String byteOrder){
        ArrayList<Integer> intPartList = new ArrayList<>();
        ArrayList<Integer> fracPartList = new ArrayList<>();
        ArrayList<Integer> mantissa = new ArrayList<>();
        finalFormHex = new StringBuilder();

        int i = 0;
        //positive signed olan inputu binary gösterime convert işlemi (ters)

        if (intPart == 0){

            intPartList.add(0);
            intPartList.add(0);
        }

        while (intPart > 0){
            intPartList.add(intPart % 2);
            intPart = intPart / 2;
        }

        Collections.reverse(intPartList);

        //fraction part binary dönüşümü
        while (fracPart !=0.0){
            fracPart = fracPart*2;
            fracPartList.add((int)(fracPart));
            fracPart = fracPart - (int)fracPart;
        }

        int E = intPartList.size()-1;

        intPartList.remove(0);
        mantissa.addAll(intPartList);
        mantissa.addAll(fracPartList);

        Integer[] fracpartArraybefore = mantissa.toArray(new Integer[0]);

        if(sizeFloating==1){
            Integer[] fracpartArrayafter = new Integer[4];
            int fracPartSize=fracpartArraybefore.length;
            boolean condition = false;
            ArrayList<Integer> finalBinaryForm = new ArrayList<>();
            ArrayList<Integer> finalfracForm = new ArrayList<>();

            if(fracPartSize>4){
                ArrayList<Integer> roundPart = new ArrayList<>();
                ArrayList<Integer> roundedPart = new ArrayList<>();

                for (int k=4;k<fracPartSize;k++){
                    roundPart.add(fracpartArraybefore[k]);
                }
                for (int k=0;k<4;k++){
                    roundedPart.add(fracpartArraybefore[k]);
                }
                Integer[] roundedPartArr = roundedPart.toArray(new Integer[0]);
                Integer[] roundPartArr = roundPart.toArray(new Integer[0]);
                Integer[] roundPartArrUp = roundPart.toArray(new Integer[0]);
                roundPartArrUp[0]=0;
                for(int x : roundPartArrUp){
                    if(x == 1){
                        condition = true;
                        break;
                    }
                }
                //<100... (round down)
                if(roundPartArr[0]==0){
                    for (i=0;i<4;i++){
                        fracpartArrayafter[i]=fracpartArraybefore[i];
                    }
                }
                //>100... (round up)
                else if(roundPartArr[0]==1&&condition){
                    //rounded part'a 1 ekleme
                    for (i=roundedPartArr.length-1;i>0;i--){
                        if(i==roundedPartArr.length-1 && roundedPartArr[3]==0){
                            roundedPartArr[3]=1;
                            break;
                        }
                        else{
                            if(roundedPartArr[i]==0)
                                roundedPartArr[i]=1;
                            else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                roundedPartArr[i] = 0;
                                roundedPartArr[i-1]=1;
                                break;
                            }
                            else{
                                roundedPartArr[i]=0;
                            }
                        }
                    }
                    System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 4);

                }
                //halfway =100... (round nearest even)
                else if(roundPartArr[0]==1&&!condition){
                    if(roundedPartArr[3]==0){
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 4);
                    }
                    else if(roundedPartArr[3]==1){
                        //rounded part'a 1 ekleme
                        for (i=roundedPartArr.length-1;i>0;i--){
                            if(i==roundedPartArr.length-1 && roundedPartArr[3]==0){
                                roundedPartArr[3]=1;
                                break;
                            }
                            else{
                                if(roundedPartArr[i]==0)
                                    roundedPartArr[i]=1;
                                else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                    roundedPartArr[i] = 0;
                                    roundedPartArr[i-1]=1;
                                    break;
                                }
                                else{
                                    roundedPartArr[i]=0;
                                }
                            }
                        }
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 4);
                    }
                }
            }
            else if(fracPartSize==4){
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, 4);
            }
            //fraction bit 4den küçükse sağdan sıfır takviyesi
            else {
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, fracPartSize);
                for (int b=fracPartSize;b<fracpartArrayafter.length;b++)
                    fracpartArrayafter[b]=0;
            }

            int bias = 3;
            int exp = E + bias;
            ArrayList<Integer> expToBinary = new ArrayList<>();
            while (exp > 0){
                expToBinary.add(exp % 2);
                exp = exp /2;
            }
            Collections.reverse(expToBinary);

            finalfracForm.addAll(Arrays.asList(fracpartArrayafter));
            finalBinaryForm.add(signBit);
            finalBinaryForm.addAll(expToBinary);
            finalBinaryForm.addAll(finalfracForm);
            //hex çevrimi için basamak tamamlama
            if(finalBinaryForm.size()!=8){
                int temp = 8-finalBinaryForm.size();
                for(int p=0; p<temp;p++)
                    finalBinaryForm.add(0);
            }

            Integer[] hexForm = finalBinaryForm.toArray(new Integer[0]);
            int[] bin_part1 = new int[4];
            int[] bin_part2 = new int[4];

            finalFormHex = new StringBuilder();

            int k=0;

            for (i = 4; i <= 7; i++) {
                bin_part2[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 0; i <= 3; i++) {
                bin_part1[k] = (hexForm[i]);
                k++;
            }
            if(byteOrder.equalsIgnoreCase("Little")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            }
            else if(byteOrder.equalsIgnoreCase("Big")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
                ;
            }
            totalHexValues.add(String.valueOf(finalFormHex));

        }
        else if(sizeFloating==2){
            Integer[] fracpartArrayafter = new Integer[7];
            int fracPartSize=fracpartArraybefore.length;
            boolean condition = false;
            ArrayList<Integer> finalBinaryForm = new ArrayList<>();
            ArrayList<Integer> finalfracForm = new ArrayList<>();

            if(fracPartSize>7){
                ArrayList<Integer> roundPart = new ArrayList<>();
                ArrayList<Integer> roundedPart = new ArrayList<>();

                for (int k=7;k<fracPartSize;k++){
                    roundPart.add(fracpartArraybefore[k]);
                }
                for (int k=0;k<7;k++){
                    roundedPart.add(fracpartArraybefore[k]);
                }
                Integer[] roundedPartArr = roundedPart.toArray(new Integer[0]);
                Integer[] roundPartArr = roundPart.toArray(new Integer[0]);
                Integer[] roundPartArrUp = roundPart.toArray(new Integer[0]);
                roundPartArrUp[0]=0;
                for(int x : roundPartArrUp){
                    if(x == 1){
                        condition = true;
                        break;
                    }
                }
                //<100... (round down)
                if(roundPartArr[0]==0){
                    for (i=0;i<7;i++){
                        fracpartArrayafter[i]=fracpartArraybefore[i];
                    }
                }
                //>100... (round up)
                else if(roundPartArr[0]==1&&condition){
                    //rounded part'a 1 ekleme
                    for (i=roundedPartArr.length-1;i>0;i--){
                        if(i==roundedPartArr.length-1 && roundedPartArr[6]==0){
                            roundedPartArr[6]=1;
                            break;
                        }
                        else{
                            if(roundedPartArr[i]==0)
                                roundedPartArr[i]=1;
                            else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                roundedPartArr[i] = 0;
                                roundedPartArr[i-1]=1;
                                break;
                            }
                            else{
                                roundedPartArr[i]=0;
                            }
                        }
                    }
                    System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 7);

                }
                //halfway =100... (round nearest even)
                else if(roundPartArr[0]==1&&!condition){
                    if(roundedPartArr[6]==0){
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 7);
                    }
                    else if(roundedPartArr[6]==1){
                        //rounded part'a 1 ekleme
                        for (i=roundedPartArr.length-1;i>0;i--){
                            if(i==roundedPartArr.length-1 && roundedPartArr[6]==0){
                                roundedPartArr[6]=1;
                                break;
                            }
                            else{
                                if(roundedPartArr[i]==0)
                                    roundedPartArr[i]=1;
                                else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                    roundedPartArr[i] = 0;
                                    roundedPartArr[i-1]=1;
                                    break;
                                }
                                else{
                                    roundedPartArr[i]=0;
                                }
                            }
                        }
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 7);
                    }
                }
            }
            else if(fracPartSize==7){
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, 7);
            }
            //fraction bit 7den küçükse sağdan sıfır takviyesi
            else {
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, fracPartSize);
                for (int b=fracPartSize;b<fracpartArrayafter.length;b++)
                    fracpartArrayafter[b]=0;
            }

            int bias = 127;
            int exp = E + bias;
            ArrayList<Integer> expToBinary = new ArrayList<>();
            while (exp > 0){
                expToBinary.add(exp % 2);
                exp = exp /2;
            }
            Collections.reverse(expToBinary);

            for (int t : fracpartArrayafter)
            {
                finalfracForm.add(t);
            }
            finalBinaryForm.add(signBit);
            finalBinaryForm.addAll(expToBinary);
            finalBinaryForm.addAll(finalfracForm);
            //hex çevrimi için basamak tamamlama
            if(finalBinaryForm.size()!=16){
                int temp = 16-finalBinaryForm.size();
                for(int p=0; p<temp;p++)
                    finalBinaryForm.add(0);
            }

            Integer[] hexForm = finalBinaryForm.toArray(new Integer[0]);
            int[] bin_part1 = new int[4];
            int[] bin_part2 = new int[4];
            int[] bin_part3 = new int[4];
            int[] bin_part4 = new int[4];
            finalFormHex = new StringBuilder();

            int k=0;
            for (i = 12; i <= 15; i++) {
                bin_part4[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 8; i <= 11; i++) {
                bin_part3[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 4; i <= 7; i++) {
                bin_part2[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 0; i <= 3; i++) {
                bin_part1[k] = (hexForm[i]);
                k++;
            }
            if(byteOrder.equalsIgnoreCase("Little")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
            }
            else if(byteOrder.equalsIgnoreCase("Big")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
            }
            totalHexValues.add(String.valueOf(finalFormHex));

        }
        else if(sizeFloating==3){
            Integer[] fracpartArrayafter = new Integer[13];
            int fracPartSize=fracpartArraybefore.length;
            boolean condition = false;
            ArrayList<Integer> finalBinaryForm = new ArrayList<>();
            ArrayList<Integer> finalfracForm = new ArrayList<>();

            if(fracPartSize>13){
                ArrayList<Integer> roundPart = new ArrayList<>();
                ArrayList<Integer> roundedPart = new ArrayList<>();

                for (int k=13;k<fracPartSize;k++){
                    roundPart.add(fracpartArraybefore[k]);
                }
                for (int k=0;k<13;k++){
                    roundedPart.add(fracpartArraybefore[k]);
                }
                Integer[] roundedPartArr = roundedPart.toArray(new Integer[0]);
                Integer[] roundPartArr = roundPart.toArray(new Integer[0]);
                Integer[] roundPartArrUp = roundPart.toArray(new Integer[0]);
                roundPartArrUp[0]=0;
                for(int x : roundPartArrUp){
                    if(x == 1){
                        condition = true;
                        break;
                    }
                }
                //<100... (round down)
                if(roundPartArr[0]==0){
                    for (i=0;i<13;i++){
                        fracpartArrayafter[i]=fracpartArraybefore[i];
                    }
                }
                //>100... (round up)
                else if(roundPartArr[0]==1&&condition){
                    //rounded part'a 1 ekleme
                    for (i=roundedPartArr.length-1;i>0;i--){
                        if(i==roundedPartArr.length-1 && roundedPartArr[12]==0){
                            roundedPartArr[12]=1;
                            break;
                        }
                        else{
                            if(roundedPartArr[i]==0)
                                roundedPartArr[i]=1;
                            else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                roundedPartArr[i] = 0;
                                roundedPartArr[i-1]=1;
                                break;
                            }
                            else{
                                roundedPartArr[i]=0;
                            }
                        }
                    }
                    System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 13);

                }
                //halfway =100... (round nearest even)
                else if(roundPartArr[0]==1&&!condition){
                    if(roundedPartArr[12]==0){
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 13);
                    }
                    else if(roundedPartArr[12]==1){
                        //rounded part'a 1 ekleme
                        for (i=roundedPartArr.length-1;i>0;i--){
                            if(i==roundedPartArr.length-1 && roundedPartArr[12]==0){
                                roundedPartArr[12]=1;
                                break;
                            }
                            else{
                                if(roundedPartArr[i]==0)
                                    roundedPartArr[i]=1;
                                else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                    roundedPartArr[i] = 0;
                                    roundedPartArr[i-1]=1;
                                    break;
                                }
                                else{
                                    roundedPartArr[i]=0;
                                }
                            }
                        }
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 13);
                    }
                }
            }
            else if(fracPartSize==13){
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, 13);
            }
            //fraction bit 7den küçükse sağdan sıfır takviyesi
            else {
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, fracPartSize);
                for (int b=fracPartSize;b<fracpartArrayafter.length;b++)
                    fracpartArrayafter[b]=0;
            }

            int bias = 511;
            int exp = E + bias;
            ArrayList<Integer> expToBinary = new ArrayList<>();
            while (exp > 0){
                expToBinary.add(exp % 2);
                exp = exp /2;
            }
            Collections.reverse(expToBinary);

            for (int t : fracpartArrayafter)
            {
                finalfracForm.add(t);
            }
            finalBinaryForm.add(signBit);
            finalBinaryForm.addAll(expToBinary);
            finalBinaryForm.addAll(finalfracForm);
            //hex çevrimi için basamak tamamlama
            if(finalBinaryForm.size()!=24){
                int temp = 24-finalBinaryForm.size();
                for(int p=0; p<temp;p++)
                    finalBinaryForm.add(0);
            }

            Integer[] hexForm = finalBinaryForm.toArray(new Integer[0]);
            int[] bin_part1 = new int[4];
            int[] bin_part2 = new int[4];
            int[] bin_part3 = new int[4];
            int[] bin_part4 = new int[4];
            int[] bin_part5 = new int[4];
            int[] bin_part6 = new int[4];

            finalFormHex = new StringBuilder();

            int k=0;
            k=0;
            for (i = 20; i <= 23; i++) {
                bin_part6[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 16; i <= 19; i++) {
                bin_part5[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 12; i <= 15; i++) {
                bin_part4[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 8; i <= 11; i++) {
                bin_part3[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 4; i <= 7; i++) {
                bin_part2[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 0; i <= 3; i++) {
                bin_part1[k] = (hexForm[i]);
                k++;
            }
            if(byteOrder.equalsIgnoreCase("Little")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part5)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part6)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
            }
            else if(byteOrder.equalsIgnoreCase("Big")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part5)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part6)));
            }
            totalHexValues.add(String.valueOf(finalFormHex));

        }
        else if(sizeFloating==4){
            Integer[] fracpartArrayafter = new Integer[19];
            int fracPartSize=fracpartArraybefore.length;
            boolean condition = false;
            ArrayList<Integer> finalBinaryForm = new ArrayList<>();
            ArrayList<Integer> finalfracForm = new ArrayList<>();

            if(fracPartSize>19){
                ArrayList<Integer> roundPart = new ArrayList<>();
                ArrayList<Integer> roundedPart = new ArrayList<>();

                for (int k=19;k<fracPartSize;k++){
                    roundPart.add(fracpartArraybefore[k]);
                }
                for (int k=0;k<19;k++){
                    roundedPart.add(fracpartArraybefore[k]);
                }
                Integer[] roundedPartArr = roundedPart.toArray(new Integer[0]);
                Integer[] roundPartArr = roundPart.toArray(new Integer[0]);
                Integer[] roundPartArrUp = roundPart.toArray(new Integer[0]);
                roundPartArrUp[0]=0;
                for(int x : roundPartArrUp){
                    if(x == 1){
                        condition = true;
                        break;
                    }
                }
                //<100... (round down)
                if(roundPartArr[0]==0){
                    for (i=0;i<19;i++){
                        fracpartArrayafter[i]=fracpartArraybefore[i];
                    }
                }
                //>100... (round up)
                else if(roundPartArr[0]==1&&condition){
                    //rounded part'a 1 ekleme
                    for (i=roundedPartArr.length-1;i>0;i--){
                        if(i==roundedPartArr.length-1 && roundedPartArr[18]==0){
                            roundedPartArr[18]=1;
                            break;
                        }
                        else{
                            if(roundedPartArr[i]==0)
                                roundedPartArr[i]=1;
                            else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                roundedPartArr[i] = 0;
                                roundedPartArr[i-1]=1;
                                break;
                            }
                            else{
                                roundedPartArr[i]=0;
                            }
                        }
                    }
                    System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 19);

                }
                //halfway =100... (round nearest even)
                else if(roundPartArr[0]==1&&!condition){
                    if(roundedPartArr[18]==0){
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 19);
                    }
                    else if(roundedPartArr[18]==1){
                        //rounded part'a 1 ekleme
                        for (i=roundedPartArr.length-1;i>0;i--){
                            if(i==roundedPartArr.length-1 && roundedPartArr[18]==0){
                                roundedPartArr[18]=1;
                                break;
                            }
                            else{
                                if(roundedPartArr[i]==0)
                                    roundedPartArr[i]=1;
                                else if(roundedPartArr[i]==1 && roundedPartArr[i-1]==0) {
                                    roundedPartArr[i] = 0;
                                    roundedPartArr[i-1]=1;
                                    break;
                                }
                                else{
                                    roundedPartArr[i]=0;
                                }
                            }
                        }
                        System.arraycopy(roundedPartArr, 0, fracpartArrayafter, 0, 19);
                    }
                }
            }
            else if(fracPartSize==19){
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, 19);
            }
            //fraction bit 19den küçükse sağdan sıfır takviyesi
            else {
                System.arraycopy(fracpartArraybefore, 0, fracpartArrayafter, 0, fracPartSize);
                for (int b=fracPartSize;b<fracpartArrayafter.length;b++)
                    fracpartArrayafter[b]=0;
            }

            int bias = 2047;
            int exp = E + bias;
            ArrayList<Integer> expToBinary = new ArrayList<>();
            while (exp > 0){
                expToBinary.add(exp % 2);
                exp = exp /2;
            }
            Collections.reverse(expToBinary);

            for (int t : fracpartArrayafter) {
                finalfracForm.add(t);
            }
            finalBinaryForm.add(signBit);
            finalBinaryForm.addAll(expToBinary);
            finalBinaryForm.addAll(finalfracForm);
            //hex çevrimi için basamak tamamlama
            if(finalBinaryForm.size()!=32){
                int temp = 32-finalBinaryForm.size();
                for(int p=0; p<temp;p++)
                    finalBinaryForm.add(0);
            }

            Integer[] hexForm = finalBinaryForm.toArray(new Integer[0]);
            int[] bin_part1 = new int[4];
            int[] bin_part2 = new int[4];
            int[] bin_part3 = new int[4];
            int[] bin_part4 = new int[4];
            int[] bin_part5 = new int[4];
            int[] bin_part6 = new int[4];
            int[] bin_part7 = new int[4];
            int[] bin_part8 = new int[4];

            finalFormHex = new StringBuilder();

            int k=0;
            for (i = 28; i <= 31; i++) {
                bin_part6[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 24; i <= 27; i++) {
                bin_part6[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 20; i <= 23; i++) {
                bin_part6[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 16; i <= 19; i++) {
                bin_part5[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 12; i <= 15; i++) {
                bin_part4[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 8; i <= 11; i++) {
                bin_part3[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 4; i <= 7; i++) {
                bin_part2[k] = (hexForm[i]);
                k++;
            }
            k=0;
            for (i = 0; i <= 3; i++) {
                bin_part1[k] = (hexForm[i]);
                k++;
            }
            if(byteOrder.equalsIgnoreCase("Little")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part7)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part8)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part5)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part6)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
            }
            else if(byteOrder.equalsIgnoreCase("Big")){
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part5)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part6)));
                finalFormHex.append(" ");
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part7)));
                finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part8)));
            }
            totalHexValues.add(String.valueOf(finalFormHex));

        }
        else{
            System.out.println("\nFloating point size must be one of the following: 1,2,3,4");
            System.exit(0);
        }

    }

    private void ConvertPositiveNumberToHex(int value, String order) {
        int[] bin_num = new int[16];
        int[] bin_part1 = new int[4];
        int[] bin_part2 = new int[4];
        int[] bin_part3 = new int[4];
        int[] bin_part4 = new int[4];
        finalFormHex = new StringBuilder();
        int i = 0;
        //positive signed olan inputu binary gösterime convert işlemi (ters)
        while (value > 0){
            bin_num[i] = value % 2;
            value = value / 2;
            i++;
        }
        int t,k;
        //yukarıda yaptığımız ters haldeki gösterimi doğrusuna çevirme loop'u
        for (k = 0; k < bin_num.length / 2; k++) {
            t = bin_num[k];
            bin_num[k] = bin_num[bin_num.length - k - 1];
            bin_num[bin_num.length - k - 1] = t;
        }
        //sign bit için ilk index 0 olmalı
        if (bin_num[0]==1) {
            bin_num[0]=0;
        }
        //hex'e çevirmeden önce ayıracağımız 4lü binary parçaları oluşturma
        k=0;
        for (i = 12; i <= 15; i++) {
            bin_part4[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 8; i <= 11; i++) {
            bin_part3[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 4; i <= 7; i++) {
            bin_part2[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 0; i <= 3; i++) {
            bin_part1[k] = (bin_num[i]);
            k++;
        }

        if(order.equalsIgnoreCase("Little")){
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
            finalFormHex.append(" ");
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
        }
        else if(order.equalsIgnoreCase("Big")){
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
            finalFormHex.append(" ");
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
        }
        totalHexValues.add(String.valueOf(finalFormHex));

    }

    private void ConvertNegativeNumberToHex(int value, String order) {
        int[] bin_num = new int[16];
        int[] bin_part1 = new int[4];
        int[] bin_part2 = new int[4];
        int[] bin_part3 = new int[4];
        int[] bin_part4 = new int[4];
        finalFormHex = new StringBuilder();
        int i=0,k;

        //binary gösterim bulma (ters)
        while (value > 0){
            bin_num[i] = value % 2;
            value = value / 2;
            i++;
        }
        //gösterimi doğru forma sokma
        for (i=0; i < bin_num.length / 2; i++) {
            k = bin_num[i];
            bin_num[i] = bin_num[bin_num.length-i-1];
            bin_num[bin_num.length-i-1] = k;
        }
        //negatif gösterim için 1 ve 0ları birbiri ile değiştirme
        for (i=0; i<bin_num.length; i++){
            if (bin_num[i] == 1)
                bin_num[i] = 0;
            else
                bin_num[i] = 1;
        }
        //değiştirme işleminden sonra son adım olarak 1 ekleme
        for (i=bin_num.length-1;i>0;i--){
            if(i==bin_num.length-1 && bin_num[15]==0){
                bin_num[15]=1;
                break;
            }
            else{
                if(bin_num[i]==0)
                    bin_num[i]=1;
                else if(bin_num[i]==1 && bin_num[i-1]==0) {
                    bin_num[i] = 0;
                    bin_num[i-1]=1;
                    break;
                }
                else{
                    bin_num[i]=0;
                }
            }
        }
        //sign bit için ilk index 1 olmalı
        if (bin_num[0]!=1) {
            bin_num[0]=1;
        }
        k=0;
        //16'lı binary dizisini 4e böl
        for (i = 12; i <= 15; i++) {
            bin_part4[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 8; i <= 11; i++) {
            bin_part3[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 4; i <= 7; i++) {
            bin_part2[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 0; i <= 3; i++) {
            bin_part1[k] = (bin_num[i]);
            k++;
        }

        //order inputuna göre dizilim yerleştirme
        if(order.equalsIgnoreCase("Little")){
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
            finalFormHex.append(" ");
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
        }
        else if(order.equalsIgnoreCase("Big")){
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
            finalFormHex.append(" ");
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
        }
        totalHexValues.add(String.valueOf(finalFormHex));
    }

    private void ConvertUnsignedIntToHex(int value, String order){

        /* binary gösterimini saklayacağımız array*/
        int[] bin_num = new int[16];
        /* hex'e çevirmek için 4lü bitler halinde parçalara ayıracağımız arrayler */
        int[] bin_part1 = new int[4];
        int[] bin_part2 = new int[4];
        int[] bin_part3 = new int[4];
        int[] bin_part4 = new int[4];

        finalFormHex = new StringBuilder();

        int i = 0;
        //unsigned integer olan inputu binary gösterime convert işlemi (ters)
        while (value > 0){
            bin_num[i] = value % 2;
            value = value / 2;
            i++;
        }
        int k,t;
        //yukarıda yaptığımız ters haldeki gösterimi doğrusuna çevirme loop'u
        for (k = 0; k < bin_num.length / 2; k++) {
            t = bin_num[k];
            bin_num[k] = bin_num[bin_num.length - k - 1];
            bin_num[bin_num.length - k - 1] = t;
        }
        //hex'e çevirmeden önce ayıracağımız 4lü binary parçaları oluşturma
        k=0;
        for (i = 12; i <= 15; i++) {
            bin_part4[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 8; i <= 11; i++) {
            bin_part3[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 4; i <= 7; i++) {
            bin_part2[k] = (bin_num[i]);
            k++;
        }
        k=0;
        for (i = 0; i <= 3; i++) {
            bin_part1[k] = (bin_num[i]);
            k++;
        }

        if(order.equalsIgnoreCase("Little")){
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
            finalFormHex.append(" ");
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
        }
        else if(order.equalsIgnoreCase("Big")){
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part1)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part2)));
            finalFormHex.append(" ");
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part3)));
            finalFormHex.append(decimalToHex(binaryArrayToDecimal(bin_part4)));
        }
        totalHexValues.add(String.valueOf(finalFormHex));
    }

    private int binaryArrayToDecimal(int[] arr){
        int decimal=0;
        int k=0;
        for(int i=3;i>=0;i--){
            decimal+=arr[i]*(int)(Math.pow(2, k));
            k++;
        }
        return decimal;
    }
    
    private String decimalToHex(int decimal){
        String hex;
        switch (decimal){
            case 10:
                hex="A";
                break;
            case 11:
                hex="B";
                break;
            case 12:
                hex="C";
                break;
            case 13:
                hex="D";
                break;
            case 14:
                hex="E";
                break;
            case 15:
                hex="F";
                break;
            default:
                hex=Integer.toString(decimal);
        }
        return hex;
    }
}

