package com.wardware.givingtracker;

import java.text.NumberFormat;
import java.text.ParseException;

public class GivingRecord implements Comparable<GivingRecord>
{
    public static final String HEADER_CSV = "Date, General, Missions, Building";
    private String date;
    private String name;
    private Double general;
    private Double missions;
    private Double building;
    private Double total;
    
    public GivingRecord()
    {
        general = 0.0;
        missions = 0.0;
        building = 0.0;
        total = 0.0;
    }

    public GivingRecord(String date, String name, Double general, Double missions, Double building)
    {
        this.date = date;
        this.name = name;
        this.general = general;
        this.missions = missions;
        this.building = building;
        this.total = general + missions + building;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Double getGeneral()
    {
        return general;
    }

    public void setGeneral(Double general)
    {
        this.general = general;
        updateTotal();
    }

    public Double getMissions()
    {
        return missions;
    }

    public void setMissions(Double missions)
    {
        this.missions = missions;
        updateTotal();
    }

    public Double getBuilding()
    {
        return building;
    }

    public void setBuilding(Double building)
    {
        this.building = building;
        updateTotal();
    }
    
    public Double getTotal()
    {
        return total;
    }
    
    private void updateTotal()
    {
        total = general + missions + building;
    }
    
    public void update(GivingRecord record)
    {
        setDate(record.getDate());
        setName(record.getName());
        setGeneral(record.getGeneral());
        setMissions(record.getMissions());
        setBuilding(record.getBuilding());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((general == null) ? 0 : general.hashCode());
        result = prime * result + ((missions == null) ? 0 : missions.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((building == null) ? 0 : building.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GivingRecord other = (GivingRecord) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (general == null) {
            if (other.general != null)
                return false;
        } else if (!general.equals(other.general))
            return false;
        if (missions == null) {
            if (other.missions != null)
                return false;
        } else if (!missions.equals(other.missions))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (this.building == null) {
            if (other.building != null)
                return false;
        } else if (!this.building.equals(other.building))
            return false;
        return true;
    }
    
    public String toBasicString()
    {
        return date + ", " + name + ", " + 
            NumberFormat.getCurrencyInstance().format(general) + ", " + 
            NumberFormat.getCurrencyInstance().format(missions) + ", " +
            NumberFormat.getCurrencyInstance().format(building);
    }

    @Override
    public String toString()
    {
        return "GivingRecord [date=" + date + 
                        ", name=" + name +
                        ", general=" + general + 
                        ", missions=" + missions + 
                        ", building=" + building +
                        ", total=" + total + "]";
    }
    
    public String toCsv()
    {
        return date + 
               "," + name +
               "," + general + 
               "," + missions + 
               "," + building +
               "," + total;
    }
    
    public String toReportCsv()
    {
        return date + 
               "," + general + 
               "," + missions + 
               "," + building +
               "," + total;
    }
    
    public static GivingRecord fromCsv(String csv) throws ParseException
    {
        try {
            final String[] tokens = csv.split(",");
            final String date = tokens[0];
            final String name = tokens[1];
            final Double general = Double.parseDouble(tokens[2]);
            final Double missions = Double.parseDouble(tokens[3]);
            final Double building = Double.parseDouble(tokens[4]);
            return new GivingRecord(date, name, general, missions, building);
        } catch (Exception e) {
            throw new ParseException(csv, 0);
        }
    }

    @Override
    public int compareTo(GivingRecord other)
    {
        if (date.equals(other.date)) {
            return name.compareTo(other.name);
        } 
        return date.compareTo(other.date);
    }
}
