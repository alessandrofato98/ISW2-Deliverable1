package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

public class GenerateTicketCsv {

	private static final String CSVPATH = "../data.csv";
	
	private Dictionary<String, Integer> dates;
	private CsvDate firstDate;
	private CsvDate lastDate;
	
	public GenerateTicketCsv() {
		
		/*
		 * dates is and hash table:
		 * 	- the key is a string that represent a date in format YYYY-M
		 * 	- the value is the number occurrences in this date
		 */
		dates = new Hashtable<>();
		firstDate = new CsvDate(2100, 1); 
		lastDate = new CsvDate(1980, 1);
	}
	
	/*
	 * Input format is (example): 2014-02-15T06:02:56.000+0000
	 * 
	 * This function add the date in the hash table and 
	 * increment by 1 the occurrences of the date
	 *  
	 */
	public void addDate(String dateString) {
		
		// get the year-month from the input string
		String[] arrOfStr = dateString.split("-");		
		
		CsvDate key = new CsvDate(
				Integer.parseInt(arrOfStr[0]), 
				Integer.parseInt(arrOfStr[1]));
		
		// control whether exists the input date into the dictionary
		Integer value = dates.get(key.getDateStr());
		
		if(value != null) {
			// if the date is already in the Dictionary, increment the number by one
			dates.put(key.getDateStr(), value + 1);
		} else {
			// otherwise, add the new date
			dates.put(key.getDateStr(), 1);

			// calculates what is the first and the last date among the added ones
			if(key.isAfter(lastDate)) {
				lastDate = key;
			} 
			if(firstDate.isAfter(key)) {
				firstDate = key;
			}
		}
			
	}
	
	/*
	 * Transform the hash table into a csv file with 2 columns:
	 * one for the key and the second for the value
	 */
	public void generateCsv() throws IOException {
				
		try (
			FileWriter writer = new FileWriter(CSVPATH);
		){
			
			writer.append("Date (M-Y)");
		    writer.append(',');
		    writer.append("Fixed ticket");
		    writer.append('\n');
			
		    CsvDate dateCounter = firstDate;
		    
		    do {
		    	Integer value = dates.get(dateCounter.getDateStr());
		    	
		    	if(value == null)
		    		value = 0;
		    	
			    writer.append(dateCounter.getDateStr());
			    writer.append(',');
			    writer.append(value.toString());
			    writer.append('\n');
		    	
			    dateCounter.incrementMonth();
			    
		    } while(lastDate.isAfter(dateCounter) || lastDate.isEqual(dateCounter));	
		} 
		
	}
	
	private class CsvDate {
		private Integer year;
		private Integer month;
		
		public CsvDate(Integer year, Integer month) {
			this.year = year;
			this.month = month;
		}
		
		public boolean isAfter(CsvDate csvDate) {
			boolean toReturn = false;
			
			if( (year > csvDate.getYear()) || 
				(year.equals(csvDate.getYear()) && month > csvDate.getMonth())) {
			
				toReturn = true;
			}
			
			return toReturn;
		}
		
		public boolean isEqual(CsvDate csvDate) {
			boolean toReturn = false;

			if(year.equals(csvDate.getYear()) && month.equals(csvDate.getMonth())) {
				toReturn = true;
			}
			
			return toReturn;
		}
		
		public void incrementMonth() {
			month += 1;
			
			if(month > 12) {
				month = 1;
				year += 1;
			}
		}
		
		public String getDateStr() {
			StringBuilder bld = new StringBuilder();
			bld.append(year);
			bld.append("-");
			bld.append(month);
			return bld.toString(); 
		}
		
		public Integer getYear() {
			return year;
		}
		public Integer getMonth() {
			return month;
		}
	}
	
}
