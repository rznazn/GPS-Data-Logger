package com.example.android.gpsdatalogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sport on 4/28/2017.
 */

public class WamFormater {

    public WamFormater(){}

    public String formatToWam(String eventTime, String azimuth, String lattitude, String longitude,
                              String note, boolean trueForEventFalseForNote) throws ParseException {
        String[] dateTime = eventTime.split(" ");
        String dateToLog = dateTime[0];
        String timeToLog = dateTime[1];

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
        Date eventDate = dateFormat.parse(eventTime);
        long eventTimeInMil = eventDate.getTime();
        long eventEndTimeInMil = eventTimeInMil + 10000;
         String eventEndTimeString =  dateFormat.format(eventEndTimeInMil);

        String[] endDateTime = eventEndTimeString.split(" ");
        String endDateToLog = endDateTime[0];
        String endTimeToLog = endDateTime[1];

        String[] latSplit = lattitude.split(":");
        String northOrSouth = "N";
        int wamLatDegrees = Integer.valueOf( latSplit[0]) * 100;
        if (wamLatDegrees <0){
            wamLatDegrees = wamLatDegrees * -1;
            northOrSouth = "S";

        }
        double wamLatMinutes = Double.valueOf(latSplit[1]);
        double wamLatFormatted = wamLatDegrees + wamLatMinutes;

        String[] lonSplit = longitude.split(":");
        String eastOrWest = "E";
        double wamLonDegrees = Double.valueOf( lonSplit[0]) * 100;
        if (wamLonDegrees <0){
            wamLonDegrees = wamLonDegrees * -1;
            northOrSouth = "W";

        }
        double wamLonMinutes = Double.valueOf(lonSplit[1]);
        double wamLonFormatted = wamLonDegrees + wamLonMinutes;

        if (trueForEventFalseForNote) {
            return "ACTION\\"
                    + dateToLog + "\\"
                    + timeToLog + "\\000\\\\\\999\\\\\\\\\\\\\\"
                    +  note + "\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\1\\TEXT_LINEB_LL\\"
                    +  wamLatFormatted + "\\" + northOrSouth + "\\"
                    + wamLonFormatted + "\\" + eastOrWest + "\\123.0\\27\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"
                    + azimuth + "\\2.5\\\\\\0.1\\"
                    + endDateToLog + "\\" + endTimeToLog + "\\241\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\" +
                    "\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\GDD\n";
        } else {
            return "ACTION\\"
                    + dateToLog + "\\"
                    + timeToLog + "\\000\\\\\\999\\\\\\\\\\\\\\"
                    +  note + "\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\1\\TEXT_LL\\"
                    +  wamLatFormatted + "\\" + northOrSouth + "\\"
                    + wamLonFormatted + "\\" + eastOrWest + "\\123.0\\27\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"
                    + azimuth + "\\2.5\\\\\\0.1\\"
                    + endDateToLog + "\\" + endTimeToLog + "\\000\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\" +
                    "\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\GDD\n";
        }
    }
}

