package cgeo.geocaching.unifiedmap.googlemaps;

import cgeo.geocaching.maps.google.v2.GoogleMapObjects;
import cgeo.geocaching.models.Route;
import cgeo.geocaching.unifiedmap.AbstractPositionLayer;
import cgeo.geocaching.utils.MapLineUtils;

import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

class GoogleMapsPositionLayer extends AbstractPositionLayer<LatLng> {

    public static final float ZINDEX_POSITION = 10;
    public static final float ZINDEX_TRACK = 6;
    public static final float ZINDEX_ROUTE = 5;
    public static final float ZINDEX_DIRECTION_LINE = 5;
    public static final float ZINDEX_POSITION_ACCURACY_CIRCLE = 3;
    public static final float ZINDEX_HISTORY = 2;

    private final GoogleMapObjects positionObjs;
    private final GoogleMapObjects trackObjs;
    private final GoogleMapObjects historyObjs;

    GoogleMapsPositionLayer(final GoogleMap googleMap, final View root) {
        super(root, LatLng::new);
        positionObjs = new GoogleMapObjects(googleMap);
        trackObjs = new GoogleMapObjects(googleMap);
        historyObjs = new GoogleMapObjects(googleMap);
    }

    public void setCurrentPositionAndHeading(final Location location, final float heading) {
        setCurrentPositionAndHeadingHelper(location, heading, (directionLine) -> positionObjs.addPolyline(new PolylineOptions()
            .addAll(directionLine)
            .color(MapLineUtils.getDirectionColor())
            .width(MapLineUtils.getDirectionLineWidth())
            .zIndex(ZINDEX_DIRECTION_LINE)
        ));
    }

    // ========================================================================
    // route / track handling

    @Override
    public void updateIndividualRoute(final Route route) {
        super.updateIndividualRoute(route, Route::getAllPointsLatLng);
    }

    @Override
    public void updateTrack(final String key, final Route track) {
        super.updateTrack(key, track, Route::getAllPointsLatLng);
    };

    // ========================================================================
    // repaint methods

    @Override
    protected void repaintPosition() {
        super.repaintPosition();
        positionObjs.removeAll();
        if (currentLocation == null) {
            return;
        }

        final LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        // accuracy circle
        final float accuracy = currentLocation.getAccuracy();
        if (accuracy > 0.001f) {
            positionObjs.addCircle(new CircleOptions()
                .center(latLng)
                .strokeColor(MapLineUtils.getAccuracyCircleColor())
                .strokeWidth(3)
                .fillColor(MapLineUtils.getAccuracyCircleFillColor())
                .radius(accuracy)
                .zIndex(ZINDEX_POSITION_ACCURACY_CIRCLE)
            );
        }

        // position and heading arrow
        positionObjs.addMarker(new MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(positionAndHeadingArrow))
            .position(latLng)
            .rotation(currentHeading)
            .anchor(0.5f, 0.5f)
            .flat(true)
            .zIndex(ZINDEX_POSITION)
        );

    };

    @Override
    protected void repaintHistory() {
        historyObjs.removeAll();
        repaintHistoryHelper((points) -> historyObjs.addPolyline(new PolylineOptions()
            .addAll(points)
            .color(MapLineUtils.getTrailColor())
            .width(MapLineUtils.getHistoryLineWidth())
            .zIndex(ZINDEX_HISTORY)
        ));
    };

    @Override
    protected void repaintRouteAndTracks() {
        trackObjs.removeAll();
        repaintRouteAndTracksHelper((segment, isTrack) -> trackObjs.addPolyline(new PolylineOptions()
            .addAll(segment)
            .color(isTrack ? MapLineUtils.getTrackColor() : MapLineUtils.getRouteColor())
            .width(isTrack ? MapLineUtils.getTrackLineWidth() : MapLineUtils.getRouteLineWidth())
            .zIndex(isTrack ? ZINDEX_TRACK : ZINDEX_ROUTE)
        ));
    };

}
