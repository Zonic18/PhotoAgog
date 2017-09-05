package zonic.photoagog.utils;

import java.util.List;

/**
 * Created by maithani on 05-09-2017.
 */

public interface InteractionListener {


    void hasDataLoaded(boolean state);

    void sendData(Object data);

    void sendData(List<?> list, String type);

}
