package senser;

public class AircraftSentence {
    protected String aircraft;

    public AircraftSentence(String aircraft){

        this.aircraft = aircraft;
    }

    public String getAircraftAsString(){

        return this.aircraft;
    }

    @Override
    public String toString(){
        return this.aircraft;
    }
}
