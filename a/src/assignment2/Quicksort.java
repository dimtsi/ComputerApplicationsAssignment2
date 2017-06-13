package assignment2;

import java.util.ArrayList;

public class Quicksort { //a quicksort algorithm
	     
	    private ArrayList<Double[]> array = new ArrayList<Double[]>();
	    private int length;
	    
	    public void sort(ArrayList<Double[]> inputArr) {
	         
	        if (inputArr == null || inputArr.size() == 0) {
	            return;
	        }
	        this.array = inputArr;
	        length = inputArr.size();
	        quickSort(0, length - 1);
	    }
	 
	    private void quickSort(int lowerIndex, int higherIndex) {
	         
	        int i = lowerIndex;
	        int j = higherIndex;
	        // calculate pivot number, I am taking pivot as middle index number
	        double pivot =array.get(lowerIndex+(higherIndex-lowerIndex)/2)[1];
	        // Divide into two arrays
	        while (i <= j) {
	            while (array.get(i)[1] < pivot) {
	                i++;
	            }
	            while (array.get(j)[1] > pivot) {
	                j--;
	            }
	            if (i <= j) {
	                exchangeNumbers(i, j);
	                i++;
	                j--;
	            }
	        }
	        if (lowerIndex < j)
	            quickSort(lowerIndex, j);
	        if (i < higherIndex)
	            quickSort(i, higherIndex);
	    }
	 
	    private void exchangeNumbers(int i, int j) {
	    	Double[] temp;
	    	
	        temp = array.get(i);
	        array.set(i, array.get(j));
	        array.set(j,temp);
	    }
}
