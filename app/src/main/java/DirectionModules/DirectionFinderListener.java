package DirectionModules;

import java.util.List;

/**
 * Created by doura on 4/3/2017.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
