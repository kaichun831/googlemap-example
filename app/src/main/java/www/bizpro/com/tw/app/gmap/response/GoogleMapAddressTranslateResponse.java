package www.bizpro.com.tw.app.gmap.response;

import java.io.Serializable;
import java.util.List;

public class GoogleMapAddressTranslateResponse implements Serializable {

    /**
     * results : [{"address_components":[{"long_name":"30","short_name":"30","types":["street_number"]},{"long_name":"華豐一街","short_name":"華豐一街","types":["route"]},{"long_name":"協成里","short_name":"協成里","types":["administrative_area_level_4","political"]},{"long_name":"新社區","short_name":"新社區","types":["administrative_area_level_3","political"]},{"long_name":"台中市","short_name":"台中市","types":["administrative_area_level_1","political"]},{"long_name":"台灣","short_name":"TW","types":["country","political"]},{"long_name":"426","short_name":"426","types":["postal_code"]}],"formatted_address":"426台灣台中市新社區華豐一街30號","geometry":{"location":{"lat":24.1947845,"lng":120.8173534},"location_type":"ROOFTOP","viewport":{"northeast":{"lat":24.1961334802915,"lng":120.8187023802915},"southwest":{"lat":24.19343551970849,"lng":120.8160044197085}}},"place_id":"ChIJo50bscceaTQR20Bzh76O5Xw","plus_code":{"compound_code":"5RV8+WW 台灣台中市新社區","global_code":"7QP25RV8+WW"},"types":["street_address"]}]
     * status : OK
     */

    private String status;
    private List<ResultsBean> results;

    public List<ResultsBean> getResults() {
        return results;
    }

    public static class ResultsBean implements Serializable {
        /**
         * address_components : [{"long_name":"30","short_name":"30","types":["street_number"]},{"long_name":"華豐一街","short_name":"華豐一街","types":["route"]},{"long_name":"協成里","short_name":"協成里","types":["administrative_area_level_4","political"]},{"long_name":"新社區","short_name":"新社區","types":["administrative_area_level_3","political"]},{"long_name":"台中市","short_name":"台中市","types":["administrative_area_level_1","political"]},{"long_name":"台灣","short_name":"TW","types":["country","political"]},{"long_name":"426","short_name":"426","types":["postal_code"]}]
         * formatted_address : 426台灣台中市新社區
         * geometry : {"location":{"lat":24.1947845,"lng":120.8173534},"location_type":"ROOFTOP","viewport":{"northeast":{"lat":24.1961334802915,"lng":120.8187023802915},"southwest":{"lat":24.19343551970849,"lng":120.8160044197085}}}
         * place_id : ChIJo50bscceaTQR20Bzh76O5Xw
         * plus_code : {"compound_code":"5RV8+WW 台灣台中市新社區","global_code":"7QP25RV8+WW"}
         * types : ["street_address"]
         */

        private String formatted_address;
        private GeometryBean geometry;

        public GeometryBean getGeometry() {
            return geometry;
        }

        private String place_id;
        private PlusCodeBean plus_code;
        private List<AddressComponentsBean> address_components;
        private List<String> types;
        public static class GeometryBean implements Serializable {
            /**
             * location : {"lat":24.1947845,"lng":120.8173534}
             * location_type : ROOFTOP
             * viewport : {"northeast":{"lat":24.1961334802915,"lng":120.8187023802915},"southwest":{"lat":24.19343551970849,"lng":120.8160044197085}}
             */

            private LocationBean location;
            private String location_type;
            private ViewportBean viewport;

            public LocationBean getLocation() {
                return location;
            }

            public static class LocationBean implements Serializable {
                /**
                 * lat : 24.1947845
                 * lng : 120.8173534
                 */

                private double lat;
                private double lng;

                public double getLat() {
                    return lat;
                }

                public double getLng() {
                    return lng;
                }
            }

            public static class ViewportBean implements Serializable {
                /**
                 * northeast : {"lat":24.1961334802915,"lng":120.8187023802915}
                 * southwest : {"lat":24.19343551970849,"lng":120.8160044197085}
                 */

                private NortheastBean northeast;
                private SouthwestBean southwest;

                public static class NortheastBean implements Serializable {
                    /**
                     * lat : 24.1961334802915
                     * lng : 120.8187023802915
                     */

                    private double lat;
                    private double lng;
                }

                public static class SouthwestBean implements Serializable {
                    /**
                     * lat : 24.19343551970849
                     * lng : 120.8160044197085
                     */

                    private double lat;
                    private double lng;
                }
            }
        }

        public static class PlusCodeBean implements Serializable {
            /**
             * compound_code : 5RV8+WW 台灣台中市新社區
             * global_code : 7QP25RV8+WW
             */

            private String compound_code;
            private String global_code;
        }

        public static class AddressComponentsBean implements Serializable {
            /**
             * long_name : 30
             * short_name : 30
             * types : ["street_number"]
             */

            private String long_name;
            private String short_name;
            private List<String> types;
        }
    }
}
