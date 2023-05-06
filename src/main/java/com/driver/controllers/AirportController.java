package com.driver.controllers;


import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class AirportController {
    public  Map<String, Airport> airportMap = new HashMap<>();
    public  Map<Integer,Flight> flightMap = new HashMap<>();
    public Map<Integer,Passenger> passengerMap =new HashMap<>();
    public Map<Integer,List<Integer>> flightToPassenger =new HashMap<>();

    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport){

        //Simply add airport details to your database
        //Return a String message "SUCCESS"
           airportMap.put(airport.getAirportName(),airport);
        return "SUCCESS";


    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName
         int terminal=0;
         String ans=null;
         for(Airport air:airportMap.values()) {

             if (air.getNoOfTerminals() > terminal) {
                 ans = air.getAirportName();
                 terminal = air.getNoOfTerminals();
             } else if (air.getNoOfTerminals() == terminal) {
                 if (air.getAirportName().compareTo(ans) < 0) {
                     ans = air.getAirportName();
                 }

             }
         }
             return ans;

    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity, @RequestParam("toCity")City toCity) {

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        double distance = Double.POSITIVE_INFINITY;
        for (Flight flight : flightMap.values()) {

            if (flight.getFromCity().equals(fromCity) && flight.getToCity().equals(toCity)) {
                distance = Math.min(distance, flight.getDuration());
            }
        }
        if (distance == Double.POSITIVE_INFINITY) {
            return -1;
        }
        return distance;
    }
    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date,@RequestParam("airportName")String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        int count=0;
          Airport airport=airportMap.get(airportName);
          if(Objects.isNull(airport)){
              return 0;
          }
          City city=airport.getCity();
          for(Flight flight : flightMap.values()){
              if(date.equals(flight.getFlightDate()))
              if(flight.getFromCity().equals(city) || flight.getToCity().equals(city)){
                  int flightId=flight.getFlightId();
                  count=count+flightToPassenger.get(flightId).size();
              }
          }

        return count;
    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId")Integer flightId){
        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int fare=0;
       if(flightToPassenger.containsKey(flightId)){
         int total=  flightToPassenger.get(flightId).size();
         fare=3000+total*50;
       }

       return fare;

    }


    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        if(flightToPassenger.get(flightId).size()>flightMap.get(flightId).getMaxCapacity()){
            return "FAILURE";
        }
        if(flightToPassenger.get(flightId).contains(passengerId)){
            return "FAILURE";
        }


        return null;
    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId
        if(Objects.nonNull(flightToPassenger.get(flightId)) &&(flightToPassenger.get(flightId).size()<flightMap.get(flightId).getMaxCapacity())){


            List<Integer> passengers =  flightToPassenger.get(flightId);

            if(passengers.contains(passengerId)){
                return "FAILURE";
            }

            passengers.add(passengerId);
            flightToPassenger.put(flightId,passengers);
            return "SUCCESS";
        }
        else if(Objects.isNull(flightToPassenger.get(flightId))){
            flightToPassenger.put(flightId,new ArrayList<>());
            List<Integer> passengers =  flightToPassenger.get(flightId);

            if(passengers.contains(passengerId)){
                return "FAILURE";
            }

            passengers.add(passengerId);
            flightToPassenger.put(flightId,passengers);
            return "SUCCESS";

        }
        return "FAILURE";
    }

    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId")Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        int count=0;
         for(Map.Entry<Integer,List<Integer>> map:flightToPassenger.entrySet()){


             List<Integer> list =map.getValue();
             for(Integer lists : list){
                 if(lists==passengerId){
                     count++;
                 }
             }

         }
        return count;
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody Flight flight){

        //Return a "SUCCESS" message string after adding a flight.
        flightMap.put(flight.getFlightId(),flight);
       return "SUCCESS";
    }


    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId")Integer flightId){

        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName
        if(flightMap.containsKey(flightId)){
           City city= flightMap.get(flightId).getFromCity();
           for(Airport airport :airportMap.values()){
               if(airport.getAirportName().equals(city))
                   return airport.getAirportName();
           }
        }

        return null;
    }


    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId")Integer flightId){

        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight
        int size=flightToPassenger.get(flightId).size();
        int variable=(size*(size+1))*25;
        int fixed=3000*size;
        int totalfare=fixed+variable;

        return totalfare;
    }


    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger){

        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully.
        passengerMap.put(passenger.getPassengerId(),passenger);

       return "SUCCESS";
    }


}
