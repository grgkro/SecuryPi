package com.example.SunbrelloSimpleServer;

import org.springframework.web.bind.annotation.*;


import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@CrossOrigin
public class GPSController {
    Logger logger = Logger.getLogger(GPSController.class.getName());

    /**
     * @return: receives and returns a msg
     */
    @RequestMapping(value = "/postGPS", method = RequestMethod.POST)
    public String postMsg(@RequestBody GPSDto gpsDto) {
        logger.log(Level.INFO, "Lat. received = " + gpsDto.getLatitude());
        logger.log(Level.INFO, "Long. received = " + gpsDto.getLongitude());
        logger.log(Level.INFO, "Message received = " + gpsDto.getSignal());
        return "GPS received";
    }

}
