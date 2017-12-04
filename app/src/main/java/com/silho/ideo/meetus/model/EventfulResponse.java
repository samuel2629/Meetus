package com.silho.ideo.meetus.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Samuel on 29/11/2017.
 */
@Root(name = "search", strict = false)
public class EventfulResponse {

    @Element(name = "events")
    public EventList event;

    @Root(name = "events", strict = false)
    public static class EventList {

        @ElementList(inline = true, name = "event", required = false)
        public List<Event> mEvents;
    }

    @Root(name = "event", strict = false)
    public static class Event {

        @Element(name = "title", required = false)
        public String title;

        @Element(name = "description", required = false)
        public String description;

        @Element(name = "start_time", required = false)
        public String startTime;

        @Element(name = "latitude", required = false)
        public float latitude;

        @Element(name = "longitude", required = false)
        public float longitude;

        @Element(name = "venue_address", required = false)
        public String address;

        @Element(name = "venue_name", required = false)
        public String placeName;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getStartTime() {
            return startTime;
        }

        public float getLatitude() {
            return latitude;
        }

        public float getLongitude() {
            return longitude;
        }

        public String getAddress() {
            return address;
        }

        public String getPlaceName() {
            return placeName;
        }
    }
}