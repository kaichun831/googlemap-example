package www.bizpro.com.tw.app.gmap.response;

import java.util.List;


public class PathResponse {
    private List<GeocodedWaypointsBean> geocoded_waypoints;
    private List<RoutesBean> routes;
    private String status;

    public List<GeocodedWaypointsBean> getGeocoded_waypoints() {
        return geocoded_waypoints;
    }

    public List<RoutesBean> getRoutes() {
        return routes;
    }

    public String getStatus() {
        return status;
    }

    public static class GeocodedWaypointsBean {
        private String geocoder_status;
        private String place_id;
        private List<String> types;
    }
    public static class RoutesBean {
        private BoundsBean bounds;
        private String copyrights;
        private List<LegsBean> legs;
        private OverviewPolylineBean overview_polyline;

        public OverviewPolylineBean getOverview_polyline() {
            return overview_polyline;
        }

        private String summary;
        private List<?> warnings;
        private List<?> waypoint_order;

        public List<LegsBean> getLegs() {
            return legs;
        }

        public void setLegs(List<LegsBean> legs) {
            this.legs = legs;
        }

        public static class BoundsBean {
            private NortheastBean northeast;
            private SouthwestBean southwest;

            public static class NortheastBean {
                private double lat;
                private double lng;
            }
            public static class SouthwestBean {
                private double lat;
                private double lng;
            }
        }
        public static class OverviewPolylineBean {
            private String points;

            public String getPoints() {
                return points;
            }
        }
        public static class LegsBean {
            private DistanceBean distance;
            private DurationBean duration;
            private String end_address;
            private EndLocationBean end_location;
            private String start_address;
            private StartLocationBean start_location;

            private List<StepsBean> steps;

            public List<StepsBean> getSteps() {
                return steps;
            }

            public void setSteps(List<StepsBean> steps) {
                this.steps = steps;
            }

            private List<?> traffic_speed_entry;
            private List<?> via_waypoint;
            public static class DistanceBean {
                private String text;
                private int value;
            }
            public static class DurationBean {
                private String text;
                private int value;
            }
            public static class EndLocationBean {
                private double lat;
                private double lng;
            }
            public static class StartLocationBean {
                private double lat;
                private double lng;
            }
            public static class StepsBean {
                private DistanceBean distance;
                private DurationBean duration;
                private EndLocationBean end_location;

                public EndLocationBean getEnd_location() {
                    return end_location;
                }

                private String html_instructions;
                private PolylineBean polyline;
                private StartLocationBean start_location;

                public StartLocationBean getStart_location() {
                    return start_location;
                }

                public void setStart_location(StartLocationBean start_location) {
                    this.start_location = start_location;
                }

                private String travel_mode;
                private String maneuver;
                public static class DistanceBean {
                    private String text;
                    private int value;
                }
                public static class DurationBean {
                    private String text;
                    private int value;
                }
                public static class EndLocationBean {
                    private double lat;
                    private double lng;
                }
                public static class PolylineBean {
                    private String points;
                }
                public static class StartLocationBean {
                    private double lat;
                    private double lng;

                    public double getLat() {
                        return lat;
                    }

                    public void setLat(double lat) {
                        this.lat = lat;
                    }

                    public double getLng() {
                        return lng;
                    }

                    public void setLng(double lng) {
                        this.lng = lng;
                    }
                }
            }
        }
    }
}
