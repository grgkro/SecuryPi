package com.example.SunbrelloSimpleServer;

import org.springframework.web.bind.annotation.*;


import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@CrossOrigin
public class GPSController {
 Integer signal = 0;
    Logger logger = Logger.getLogger(GPSController.class.getName());

    /**
     * @return: receives and returns a signal
     */
//    @RequestMapping(value = "/postSignal", method = RequestMethod.POST)
//    public int postSignal(@RequestBody GPSDto signal) {
//        logger.log(Level.INFO, "Signal received = " + signal.getSignal());
//        return signal.getSignal();
//    }

    /**
     * @return: receives and returns a msg
     */
    @RequestMapping(value = "/getGPS", method = RequestMethod.GET)
    public String postMsg(@RequestBody GPSDto gpsDto) {
        logger.log(Level.INFO, "Message received = " + gpsDto.getGPS());
        logger.log(Level.INFO, "From id = " + gpsDto.getId());
        return gpsDto.getGPS();
    }

}
