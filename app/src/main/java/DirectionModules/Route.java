package DirectionModules;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

/**
 * Created by doura on 4/3/2017.
 * classe qui va contenir la distance, durée, adresse source, adresse de destination
 * et leurs coordonnées LatLng, ainsi que tous les points de la route.
 */
public class Route {

    public Distance rDistance;
    public Duration rDuration;
    public String rEndAddress;
    public LatLng rEndLocation;
    public String rStartAddress;
    public LatLng rStartLocation;

    public List<LatLng> points;

    public void setDistance(Distance distance) {
        this.rDistance = distance;
    }

    public void setDuration(Duration duration) {
        this.rDuration = duration;
    }

    public void setEndAddress(String endAddress) {
        this.rEndAddress = endAddress;
    }

    public void setEndLocation(LatLng endLocation) {
        this.rEndLocation = endLocation;
    }

    public void setStartAddress(String startAddress) {
        this.rStartAddress = startAddress;
    }

    public void setStartLocation(LatLng startLocation) {
        this.rStartLocation = startLocation;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public Distance getrDistance() {
        return rDistance;
    }

    public Duration getrDuration() {
        return rDuration;
    }

    public String getrEndAddress() {
        return rEndAddress;
    }

    public LatLng getrEndLocation() {
        return rEndLocation;
    }

    public String getrStartAddress() {
        return rStartAddress;
    }

    public LatLng getrStartLocation() {
        return rStartLocation;
    }

    public List<LatLng> getPoints() {
        return points;
    }
}
