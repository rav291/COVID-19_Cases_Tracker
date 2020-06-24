package com.astralis.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.astralis.coronavirustracker.models.LocationStats;

@Service // This annot. tells spring to create an instance of this class
public class CoronaVirusDataService {
	
	private static String Data_Source_url ="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	
	private List<LocationStats> allStats = new ArrayList<LocationStats>(); 
	

	public List<LocationStats> getAllStats() {
		return allStats;
	}


	@PostConstruct // This annot. tells spring that this method needs to be executed when the app starts
	@Scheduled(cron="* * 1 * * *") // format --> sec:min:hour:day:week:year 
	public void fetchVirusData() throws IOException, InterruptedException
	{
		List<LocationStats> newStats = new ArrayList<LocationStats>(); 
		
		HttpClient client = HttpClient.newHttpClient();
		
		HttpRequest request = HttpRequest.newBuilder()
							  .uri(URI.create(Data_Source_url))
							  .build();	
		
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		StringReader csvBodyReader = new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		
		for (CSVRecord record : records) {
		    LocationStats locationStat = new LocationStats();
		    
		    locationStat.setCountry(record.get("Country/Region"));
		    locationStat.setState(record.get("Province/State"));
		    
		    
		    int latestCases = Integer.parseInt(record.get(record.size()-1));
		    int prevDayCases = Integer.parseInt(record.get(record.size()-2));
		    
		    locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size()-1)));
		    
		    locationStat.setDiffFromPrevDay(latestCases-prevDayCases);
		    
		    // System.out.println(locationStat); Automatically executes the toString method in LocationStat
		    
		    newStats.add(locationStat);
		}
		this.allStats = newStats;
	}
	
	
	
	
}
